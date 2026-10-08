package com.pokewilds.util.net;

import com.badlogic.gdx.Gdx;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * <p>
 * The {@code GameSocket} is a UDP socket with extra functionality such as retries for dropped packets.
 * </p>
 */
public class GameSocket extends Thread implements Runnable {
    public static final int BUFFER_SIZE = 0xFFFF;
    public static final int MAX_INFLATED_SIZE = 256;
    public static final int COMPRESSION_LEVEL = 1;
    public static final Inflater INFLATER = new Inflater();
    public static final Deflater DEFLATER = new Deflater(COMPRESSION_LEVEL);

    private final PacketHandler handler;
    private final ByteBuffer deflateBuffer;
    private boolean isActive;
    private DatagramChannel channel;

    /**
     * <p>Constructs a new {@code GameSocket}.</p>
     *
     * @param handler The handler reads incoming packets.
     */
    public GameSocket(PacketHandler handler) {
        this.handler = handler;
        this.deflateBuffer = ByteBuffer.allocate(GameSocket.BUFFER_SIZE);
    }

    /**
     * <p>Opens a new channel on a specific address.</p>
     *
     * @param addr The address
     */
    public void open(SocketAddress addr) throws IOException {
        this.setName("GameSocket-" + addr.toString());
        this.channel = DatagramChannel.open();
        this.channel.bind(addr);
        this.channel.configureBlocking(false);
        Gdx.app.log("PokeRancher", "started game socket on [" + addr + "]");
    }

    /**
     * <p>Connects to an external socket.</p>
     *
     * @param addr The address to connect to.
     */
    public void connect(SocketAddress addr) throws IOException {
        this.channel.connect(addr);
    }

    /**
     * <p>
     * Sends a packet to either a server or client.
     * </p>
     *
     * @param addr     The address to send the packet to.
     * @param outgoing The outgoing byte buffer.
     * @throws IOException Thrown if the packet cannot be sent.
     */
    public void send(SocketAddress addr, ByteBuffer outgoing) throws IOException {
        int packetType = outgoing.get(0);
        Gdx.app.debug("PokeRancher",
                "Sending packet of type [" + PacketType.values()[packetType].toString() + "] to [" +
                        addr + "]");

        if (outgoing.limit() > GameSocket.MAX_INFLATED_SIZE) {
            Gdx.app.debug("PokeRancher", "Compressing packet");
            synchronized (this.deflateBuffer) {
                deflateBuffer.clear();
                deflateBuffer.put(PacketType.COMPRESSED_PACKET.id());
                deflateBuffer.putInt(0);
                deflateBuffer.putInt(outgoing.limit());

                DEFLATER.reset();
                DEFLATER.setInput(outgoing.array(), 0, outgoing.limit());
                DEFLATER.finish();
                int size = DEFLATER.deflate(deflateBuffer.array(), 9, deflateBuffer.capacity() - 9);

                deflateBuffer.position(1);
                deflateBuffer.putInt(size);
                deflateBuffer.position(size + 9);
                deflateBuffer.flip();

                outgoing = deflateBuffer;
            }
        }

        this.channel.send(outgoing, addr);
    }

    @Override
    public void run() {
        if (this.channel == null) {
            Gdx.app.error("PokeRancher", "Unable to start GameSocket with no open channel.");
        }
        Gdx.app.log("PokeRancher", "started game thread");

        this.isActive = true;
        ByteBuffer buffer = ByteBuffer.allocate(GameSocket.BUFFER_SIZE);
        while (this.isActive) {
            try {
                buffer.clear();
                SocketAddress fromAddress = this.channel.receive(buffer);
                if (fromAddress == null) {
                    continue;
                }
                buffer.flip();

                if (!buffer.hasRemaining()) {
                    continue;
                }
                int packetCode = buffer.get();
                PacketType type = PacketType.fromCode(packetCode);
                if (type == null) {
                    Gdx.app.log("PokeRancher",
                            "Received invalid packetcode [" + packetCode + "] from [" + fromAddress + "]");
                    continue;
                }
                Gdx.app.debug("PokeRancher", "Received packet [" + type + "] from [" + fromAddress + "]");
                if (type == PacketType.COMPRESSED_PACKET) {
                    try {
                        int compressedSize = buffer.getInt();
                        int decompressedSize = buffer.getInt();

                        System.out.println("hi");
                        synchronized (this.deflateBuffer) {
                            this.deflateBuffer.clear();

                            INFLATER.reset();
                            INFLATER.setInput(buffer.array(), buffer.position(), compressedSize);
                            int actualSize = INFLATER.inflate(this.deflateBuffer.array(), 0, decompressedSize);

                            buffer.clear();
                            buffer.put(this.deflateBuffer.array(), 0, actualSize);
                            buffer.flip();

                            type = PacketType.fromCode(buffer.get());
                        }
                    } catch (DataFormatException e) {
                        Gdx.app.error("PokeRancher", "Error decompressing packet", e);
                        continue;
                    }
                }

                this.handler.handlePacket(fromAddress, type, buffer);
            } catch (Exception e) {
                this.handler.onPacketError(e);
            }
        }

        this.close();
    }

    /**
     * <p>Closes the GameSocket.</p>
     */
    public void close() {
        this.isActive = false;

        if (this.channel == null) {
            return;
        }

        try {
            this.channel.close();
            this.channel = null;
        } catch (IOException e) {
            Gdx.app.error("PokeRancher", "Error closing the socket", e);
        }
    }

    public boolean isActive() {
        return this.isActive;
    }
}
