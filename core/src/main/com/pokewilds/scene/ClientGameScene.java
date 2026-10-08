package com.pokewilds.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.pokewilds.Constants;
import com.pokewilds.PokeWilds;
import com.pokewilds.entity.Direction;
import com.pokewilds.entity.Entity;
import com.pokewilds.entity.Player;
import com.pokewilds.util.Settings;
import com.pokewilds.util.net.GameSocket;
import com.pokewilds.util.net.PacketHandler;
import com.pokewilds.util.net.PacketType;
import com.pokewilds.world.WorldSize;
import com.pokewilds.world.chunk.MapChunk;
import com.pokewilds.world.tile.GrassTileEffect;
import com.pokewilds.world.tile.Tile;
import com.pokewilds.world.tile.TileEffect;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * <p>
 * The {@code ClientGameScene} contains the Game code for when a client is connected, the ClientGameScene simply
 * predicts where a client should be, but defers authority to the {@link HostGameScene host}.
 * </p>
 */
public class ClientGameScene extends GameScene implements PacketHandler {
    private final GameSocket socket;
    private final InetSocketAddress peer;
    private final List<Integer> usedChunks;
    private final Queue<MapChunk> newChunks;
    private final Map<Integer, Long> chunkRequests;
    private UUID sessionId;
    private WorldSize worldSize;
    private long lastPacketTime, lastPingTime;
    private long serverDifference;
    private int errorCount;

    public ClientGameScene(final PokeWilds game, InetAddress addr) throws IOException {
        super(game);

        this.newChunks = new ArrayDeque<>();
        this.usedChunks = new ArrayList<>();
        this.chunkRequests = new HashMap<>();
        this.socket = new GameSocket(this);
        this.socket.open(new InetSocketAddress(0));
        this.socket.connect(peer = new InetSocketAddress(addr, 5173));
        this.socket.start();
    }

    @Override
    public void update(float deltaTime) {
        boolean sentPacket = false;
        if (sessionId == null || this.player == null) {
            if (System.currentTimeMillis() - this.lastPacketTime > 1000) {
                this.sendConnectionPacket();
                this.lastPacketTime = System.currentTimeMillis();
            }
            return;
        }
        if (System.currentTimeMillis() - this.lastPingTime > Constants.PING_TIME) {
            this.sendPingPacket();
            this.lastPingTime = System.currentTimeMillis();
        }

        synchronized (this.newChunks) {
            MapChunk c;
            while ((c = this.newChunks.poll()) != null) {
                synchronized (this.loadedChunks) {
                    if (!this.loadedChunks.containsKey(c.getId())) {
                        this.loadedChunks.put(c.getId(), c);
                        c.reloadStates(this);
                    }
                }
            }
        }

        this.usedChunks.clear();
        this.usedChunks.addAll(this.loadedChunks.keySet());

        for (int ry = -Constants.LOADED_CHUNKS; ry < Constants.LOADED_CHUNKS + 1; ry++) {
            for (int rx = -Constants.LOADED_CHUNKS; rx < Constants.LOADED_CHUNKS + 1; rx++) {
                int chunkX = this.player.getMapChunkX() + rx;
                int chunkY = this.player.getMapChunkY() + ry;
                if (!this.worldSize.isValidChunk(chunkX, chunkY)) {
                    continue;
                }
                this.usedChunks.remove(MapChunk.getKey(chunkX, chunkY));
                if (this.loadedChunks.containsKey(MapChunk.getKey(chunkX, chunkY))) {
                    continue;
                }
                this.sendChunkRequestPacket(chunkX, chunkY);
            }
        }

        for (Integer chunkId : this.usedChunks) {
            this.loadedChunks.remove(chunkId);
        }

        for (int i = 0; i < this.entities.length; i++) {
            if (this.entities[i] == null) {
                continue;
            }
            this.entities[i].update(deltaTime);
        }

        for (int i = 0; i < this.tileEffects.length; i++) {
            TileEffect e = this.tileEffects[i];
            if (e == null) {
                continue;
            }
            e.update(deltaTime);
            if (e.isDone()) {
                this.renderables[MAX_ENTITIES + i] = this.tileEffects[i] = null;
            }
        }

        super.update(deltaTime);
    }

    private void sendPingPacket() {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put(PacketType.PING.id());
        buffer.flip();
        try {
            this.socket.send(this.peer, buffer);
        } catch (IOException e) {
            Gdx.app.error("ClientGameScene", "Failed to send ping packet", e);
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        this.socket.close();
    }

    @Override
    public boolean keyDown(int keyCode) {
        if (player == null) {
            return false;
        }

        if (keyCode == Input.Keys.W) {
            this.player.setMoveDirection(Direction.UP);
            this.sendMovementPacket(Direction.UP);
            return true;
        } else if (keyCode == Input.Keys.A) {
            this.player.setMoveDirection(Direction.LEFT);
            this.sendMovementPacket(Direction.LEFT);
            return true;
        } else if (keyCode == Input.Keys.D) {
            this.player.setMoveDirection(Direction.RIGHT);
            this.sendMovementPacket(Direction.RIGHT);
            return true;
        } else if (keyCode == Input.Keys.S) {
            this.player.setMoveDirection(Direction.DOWN);
            this.sendMovementPacket(Direction.DOWN);
            return true;
        }

        return false;
    }

    @Override
    public boolean keyUp(int keyCode) {
        if (player == null) {
            return false;
        }

        if (keyCode == Input.Keys.W || keyCode == Input.Keys.A || keyCode == Input.Keys.D || keyCode == Input.Keys.S) {
            this.player.setMoveDirection(null);
            this.sendMovementPacket(null);
            return true;
        }

        return false;
    }

    private void sendConnectionPacket() {
        try {
            final ByteBuffer out = ByteBuffer.allocate(17);
            out.put(PacketType.CONNECTION_REQUEST_PACKET.id());
            out.putLong(Settings.getPid().getMostSignificantBits());
            out.putLong(Settings.getPid().getLeastSignificantBits());
            out.flip();
            this.socket.send(this.peer, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendChunkRequestPacket(final int chunkX, final int chunkY) {
        int cId = MapChunk.getKey(chunkX, chunkY);
        long time = this.chunkRequests.getOrDefault(cId, 0L);
        if (System.currentTimeMillis() - time < 1000) {
            return;
        }
        this.chunkRequests.put(cId, System.currentTimeMillis());
        try {
            final ByteBuffer out = ByteBuffer.allocate(5);
            out.put(PacketType.CHUNK_REQUEST_PACKET.id());
            out.putInt(MapChunk.getKey(chunkX, chunkY));
            out.flip();
            this.socket.send(this.peer, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMovementPacket(Direction direction) {
        try {
            ByteBuffer out = ByteBuffer.allocate(2);
            out.put(PacketType.MOVEMENT_REQUEST.id());
            out.put(direction == null ? 0 : (byte) (direction.ordinal() + 1));
            out.flip();
            this.socket.send(this.peer, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handlePacket(SocketAddress fromAddress, PacketType packetType, ByteBuffer buffer) throws IOException {
        int tX, tY;
        switch (packetType) {
            case CONNECTION_RESPONSE_PACKET:
                buffer.get();
                this.serverDifference = System.currentTimeMillis() - buffer.getLong();
                this.sessionId = new UUID(buffer.getLong(), buffer.getLong());
                this.worldSize = WorldSize.values()[buffer.get()];
                int id = buffer.get();
                int pX = buffer.getInt();
                int pY = buffer.getInt();
                this.renderables[id] = this.entities[id] = this.player = new Player(this, id, null, 0, null, pX, pY);
                this.player.setTexture(this.playerTexture);
                this.lastPacketTime = 0;
                break;
            case CHUNK_RESPONSE_PACKET:
                int chunkId = buffer.getInt();
                int chunkX = (chunkId >> 16) & 0xFFFF;
                int chunkY = chunkId & 0xFFFF;

                int[][] tiles = new int[Tile.LAYER_COUNT][Constants.CHUNK_SIZE * Constants.CHUNK_SIZE];
                int[][] tileStates = new int[Tile.LAYER_COUNT][Constants.CHUNK_SIZE * Constants.CHUNK_SIZE];

                for (int i = 0; i < Tile.LAYER_COUNT; i++) {
                    for (int j = 0; j < Constants.CHUNK_SIZE * Constants.CHUNK_SIZE; j++) {
                        tiles[i][j] = buffer.getShort();
                        tileStates[i][j] = buffer.get();
                    }
                }

                MapChunk c = new MapChunk(null, chunkX, chunkY, tiles, tileStates);
                synchronized (this.newChunks) {
                    this.newChunks.add(c);
                }
                break;
            case ENTITY_STATE_UPDATE:
                long currentTime = buffer.getLong() + this.serverDifference;
                int entityId = buffer.getShort();
                int entityType = buffer.get();
                Direction dir = Direction.values()[buffer.get()];
                tX = buffer.getInt();
                tY = buffer.getInt();
                float x = buffer.getFloat();
                float y = buffer.getFloat();

                Entity e = this.entities[entityId];
                if (e == null) {
                    Player p = new Player(this, entityId, null, 0, null, tX, tY);
                    p.setTexture(this.playerTexture);
                    this.renderables[entityId] = this.entities[entityId] = e = p;
                }
                e.setTilePos(tX, tY);
                e.setServerPosition(currentTime, x, y);
                e.setLookDirection(dir);
                break;
            case ENTITY_REMOVE:
                entityId = buffer.getShort();
                if (this.player.getId() == entityId) {
                    break;
                }
                Gdx.app.log("EntityRemove", "Removing entity " + entityId);
                this.renderables[entityId] = this.entities[entityId] = null;
                break;
            case TILE_UPDATE:
                id = buffer.getShort();
                int state = buffer.get();
                int layer = buffer.get();
                tX = buffer.getInt();
                tY = buffer.getInt();

                this.setTile(layer, tX, tY, id, state, false);
                break;
            case TILE_EFFECT_SPAWN:
                int type = buffer.get();
                tX = buffer.getInt();
                tY = buffer.getInt();
                this.createTileEffect(new GrassTileEffect(tilesetTexture, tX, tY));
                break;
            case TILE_EFFECT_REMOVE:
                type = buffer.get();
                tX = buffer.getInt();
                tY = buffer.getInt();
                this.deleteTileEffect(GrassTileEffect.class, tX, tY);
                break;
        }
    }

    @Override
    public void onPacketError(Exception e) {
        if (e instanceof PortUnreachableException) {
            Gdx.app.error("PokeWilds-Client", "Server is unreachable");
            this.game.invokeLater(() -> {
                this.game.setScreen(new TitleScreen(this.game));
            });
        } else {
            Gdx.app.error("PokeWilds-Client", "Error while receiving packet", e);
        }
    }

    public boolean isHost() {
        return false;
    }
}
