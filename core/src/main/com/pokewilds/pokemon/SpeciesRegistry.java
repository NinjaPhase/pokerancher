package com.pokewilds.pokemon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.pokewilds.util.TextureManager;

import java.util.HashMap;
import java.util.Map;

public final class SpeciesRegistry {

    private static final Map<Integer, Species> SPECIES_MAP = new HashMap<>();
    private static final Map<String, Species> SPECIES_NAME_MAP = new HashMap<>();

    public static void loadDefaults(TextureManager textureManager) {
        JsonReader reader = new JsonReader();
        JsonValue val = reader.parse(Gdx.files.internal("data/species.json"));
        for (JsonValue v : val) {
            register(v.getInt("id"), v.name, new Species(textureManager, v));
        }
    }

    public static void register(int id, String name, Species species) {
        if (SPECIES_MAP.containsKey(id)) {
            throw new RuntimeException("species registry already contains key: " + id);
        }
        SPECIES_MAP.put(id, species);
        SPECIES_NAME_MAP.put(name, species);
    }

    public static Species get(int id) {
        return SPECIES_MAP.get(id);
    }

    public static Species get(String name) {
        return SPECIES_NAME_MAP.get(name);
    }

    private SpeciesRegistry() {
    }

}
