package com.pokewilds.api.world.biome;

/**
 * <p>
 * The {@code BiomeVoid} is the null biome, it is the biome that is generated if there is no match with the noise
 * channels to an existing biome.
 * </p>
 */
public class BiomeVoid extends Biome {

    @Override
    public void generateTile(int x, int y) {

    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public int getPriority() {
        return Biome.PRIORITY_DEFAULT;
    }
}
