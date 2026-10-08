package com.pokewilds.util.net;

/**
 * <p>
 * The {@code PacketType} is a packet which is sent across the server or
 * client.
 * <p>
 * Each packet has either a fixed-size or a variable-size denoted by -1 or
 * -2.-1 means the size is given as a byte, -2 means the size is given as a
 * short.
 * </p>
 */
public enum PacketType {
    PING(0),
    PONG(0),
    LAN_REQUEST_PACKET(1),
    LAN_RESPONSE_PACKET(-2),
    CONNECTION_REQUEST_PACKET(1),
    CONNECTION_RESPONSE_PACKET(1),
    CHUNK_REQUEST_PACKET(1),
    CHUNK_RESPONSE_PACKET(1),
    MOVEMENT_REQUEST(1),
    ENTITY_STATE_UPDATE(1),
    ENTITY_REMOVE(1),
    TILE_UPDATE(1),
    TILE_EFFECT_SPAWN(1),
    TILE_EFFECT_REMOVE(1),
    COMPRESSED_PACKET(-1);

    public final int size;

    public static PacketType fromCode(final int packetCode) {
        if (!PacketType.isValid(packetCode)) {
            return null;
        }
        return PacketType.values()[packetCode];
    }

    public static boolean isValid(final int packetCode) {
        return packetCode >= 0 && packetCode < PacketType.values().length;
    }

    PacketType(int size) {
        this.size = size;
    }

    public byte id() {
        return (byte) this.ordinal();
    }
}
