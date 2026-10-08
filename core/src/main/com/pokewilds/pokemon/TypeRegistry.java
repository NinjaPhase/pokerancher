package com.pokewilds.pokemon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TypeRegistry {

    private static int typeIdx;
    private static final List<Type> TYPES = new ArrayList<>();
    private static final Map<String, Type> TYPE_NAME_MAP = new HashMap<>();

    static {
        TypeRegistry.register("normal", new Type());
    }

    public static void register(String key, Type type) {
        type.id = typeIdx++;
        TYPES.add(type);
        TYPE_NAME_MAP.put(key, type);
    }

    private TypeRegistry() {
    }

}
