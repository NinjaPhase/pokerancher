package com.pokewilds.world.biome;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * A registry for all biomes in the game.
 * </p>
 */
public class BiomeRegistry {

    private static final HashMap<String, Biome> BIOMES = new HashMap<>();
    private static final List<Biome> BIOME_LIST = new ArrayList<>();

    public static void loadDefaults() {
        register("ocean", Biome.OCEAN);
        register("polar", new Biome.Builder().elevation(0.43f).temperature(0.0f).testColor(Color.WHITE).create());
        register("forest", new Biome.Builder().elevation(0.43f).temperature(0.2f).testColor(Color.GREEN).create());
        register("desert", new Biome.Builder().elevation(0.43f).temperature(0.4f).testColor(Color.YELLOW).create());
    }

    /**
     * <p>
     * Registers a biome with the given key.
     * </p>
     *
     * @param key   The key to register the biome with.
     * @param biome The biome to register.
     */
    public static void register(String key, Biome biome) {
        BIOMES.put(key, biome);
        BIOME_LIST.add(biome);
    }

    /**
     * <p>
     * Gets a biome by its key.
     * </p>
     *
     * @param key The key of the biome to get.
     *
     * @return The biome with the given key.
     */
    public static Biome get(String key) {
        return BIOMES.get(key);
    }

    /**
     * <p>
     * Gets all biomes in the registry.
     * </p>
     *
     * @return A set of all biomes in the registry.
     */
    public static List<Biome> getBiomes() {
        return BIOME_LIST;
    }

    private BiomeRegistry() {
    }

}
