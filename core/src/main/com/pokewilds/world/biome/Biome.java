package com.pokewilds.world.biome;

import java.awt.*;

/**
 * <p>
 * A {@code Biome} is a region of the world that has a specific set of properties.
 * </p>
 */
public class Biome {
    public static final Biome OCEAN = new Biome.Builder().temperature(0.0f)
                                                         .elevation(0.0f)
                                                         .testColor(Color.CYAN)
                                                         .create();

    private float temperature, elevation;
    private Color testColor;

    public Biome() {

    }

    public float getElevation() {
        return elevation;
    }

    public float getTemperature() {
        return temperature;
    }

    public Color getTestColor() {
        return testColor;
    }

    public static class Builder {

        final Biome internal;

        public Builder() {
            this.internal = new Biome();
        }

        public Builder temperature(float temperature) {
            internal.temperature = temperature;
            return this;
        }

        public Builder elevation(float elevation) {
            internal.elevation = elevation;
            return this;
        }

        public Builder testColor(Color testColor) {
            internal.testColor = testColor;
            return this;
        }

        public Biome create() {
            return internal;
        }
    }
}
