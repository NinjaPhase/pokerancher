package com.pokewilds.util.net;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

public interface PacketHandler {
    void handlePacket(SocketAddress fromAddress, PacketType packetType, ByteBuffer buffer) throws IOException;

    void onPacketError(Exception e);
}
