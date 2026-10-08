package com.pokewilds.util.net;

public class MultiplayerPacket {
    // CLIENT PACKETS
    public static final byte PING_PACKET = 0;
    public static final byte LAN_BROADCAST_PACKET = 1;
    public static final byte CONNECTION_PACKET = 2;
    public static final byte REQUEST_CHUNK_PACKET = 3;
    public static final byte MOVE_PACKET = 4;

    // HOST PACKETS
    public static final byte PONG_PACKET = 5;
    public static final byte LAN_RESPONSE_PACKET = 6;
    public static final byte CONNECTION_RESPONSE_PACKET = 7;
    public static final byte CHUNK_PACKET = 8;
    public static final byte ENTITY_STATE_PACKET = 9;
}
