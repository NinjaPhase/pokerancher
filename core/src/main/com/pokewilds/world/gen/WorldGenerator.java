package com.pokewilds.world.gen;

import com.pokewilds.Constants;
import com.pokewilds.world.WorldSize;
import com.pokewilds.world.biome.Biome;
import com.pokewilds.world.biome.BiomeRegistry;
import com.pokewilds.world.biome.BiomeType;
import com.pokewilds.world.noise.OpenSimplexNoise;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * The {@code WorldGenerator} is used to generate a given world, the world generator should use the same seed and
 * </p>
 */
public class WorldGenerator {

    private final OpenSimplexNoise noise;
    private final WorldSize size;
    private final long seed;

    public WorldGenerator(final long seed, final WorldSize size) {
        this.seed = seed;
        this.noise = new OpenSimplexNoise(seed);
        this.size = size;
    }

    public double getHeight(int x, int y) {
        return this.get(x, y, 0.0, 0.5, this.size.heightFrequency, 8, 0.45, 2.0);
    }

    public double getTemperature(int x, int y) {
        int midY = (size.height * Constants.CHUNK_SIZE) / 2;

        double orig = this.get(x, y, 10000.0, 0.5, this.size.temperatureFrequency, 8, 0.35, 2.0);
        double d = ((Math.abs(midY - y) - 50.) / (double) (midY));
        double dclamp = (Math.max(0., 1. - Math.min(d, 1.)));

        return orig * dclamp;
    }

    public Biome getBiomeNew(int x, int y) {
        double h = this.getHeight(x, y);
        double t = this.getTemperature(x, y);

        List<Biome> biomes = BiomeRegistry.getBiomes();
        biomes.sort((b1, b2) -> {
            if (Float.compare(b1.getElevation(), b2.getElevation()) == 0) {
                return Float.compare(b1.getTemperature(), b2.getTemperature());
            }
            return Float.compare(b1.getElevation(), b2.getElevation());
        });
        Collections.reverse(biomes);

        for (Biome biome : biomes) {
            float minHeight = biome.getElevation();
            float minTemp = biome.getTemperature();

            if (h > minHeight && t > minTemp) {
                return biome;
            }
        }

        return Biome.OCEAN;
    }

    public BiomeType getBiome(int x, int y) {
        double h = this.getHeight(x, y);
        if (h > 0.4549) {
            double t = this.getTemperature(x, y);
            if (t > 0.35) {
                return BiomeType.DESERT;
            } else if (t > 0.1) {
                return BiomeType.FOREST;
            }
            return BiomeType.SNOW;
        } else if (h > 0.43) {
            double t = this.getTemperature(x, y);
            if (t > 0.1) {
                return BiomeType.BEACH;
            }
            return BiomeType.SNOW;
        } else {
            return BiomeType.OCEAN;
        }
    }

    public double get(double x, double y, double z, double amplitude, double frequency, int octaveCount,
                      double persistence, double lacunarity) {
        if (octaveCount == 1) {
            return amplitude * this.get(x, y, z);
        }
        double value = 0.0;

        for (int i = 0; i < octaveCount; i++) {
            double sampleX = (x + (i * 50000)) * frequency;
            double sampleY = (y + (i * 50000)) * frequency;

            value += amplitude * this.get(sampleX, sampleY, z);
            amplitude *= persistence;
            frequency *= lacunarity;
        }
        if (value > 1.0) {
            value = 1.0;
        } else if (value < 0.0) {
            value = 0.0;
        }
        return value;
    }

    private double get(double x, double y, double z) {
        return (this.noise.eval(x, y, z) / 2.0) + 0.5;
    }

    public WorldSize getWorldSize() {
        return this.size;
    }
}
