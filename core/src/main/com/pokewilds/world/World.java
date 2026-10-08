package com.pokewilds.world;

import com.pokewilds.api.world.gen.WorldType;
import com.pokewilds.world.chunk.MapChunk;

/**
 * <p>
 * Representation for a {@code World} in-game.
 * </p>
 */
public class World {

    private final WorldType worldType;

    public World(final WorldType worldType) {
        this.worldType = worldType;
    }

    public MapChunk getChunkAt(int x, int z) {
        return null;
    }

}
