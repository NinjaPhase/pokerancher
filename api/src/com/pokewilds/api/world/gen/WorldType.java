package com.pokewilds.api.world.gen;

import com.pokewilds.api.world.biome.Biome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * The {@code WorldType} interface is used to define the different types of worlds that can be generated.
 * </p>
 *
 * <p>
 * World's are per-game and are made of multiple dimensions.
 * </p>
 */
public interface WorldType {
    List<WorldType> worldTypes = new ArrayList<>();

    static void register(WorldType worldType) {
        worldTypes.add(worldType);
    }

    static List<WorldType> getWorldTypes() {
        return Collections.unmodifiableList(worldTypes);
    }

    void addBiome(Biome biome);

    String getName();

}
