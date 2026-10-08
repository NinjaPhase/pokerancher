package com.pokewilds.pokemon;

import java.util.HashMap;
import java.util.Map;

public final class MoveRegistry {

    private static final Map<Integer, Move> MOVE_MAP = new HashMap<>();
    private static final Map<String, Move> MOVE_NAME_MAP = new HashMap<>();

    public static void loadDefaults() {

    }

    public static void register(int id, String name, Move move) {
        if (MOVE_MAP.containsKey(id)) {
            throw new RuntimeException("species registry already contains key: " + id);
        }
        MOVE_MAP.put(id, move);
        MOVE_NAME_MAP.put(name, move);
    }

    public static Move get(int id) {
        return MOVE_MAP.get(id);
    }

    public static Move get(String name) {
        return MOVE_NAME_MAP.get(name);
    }

    private MoveRegistry() {

    }
}
