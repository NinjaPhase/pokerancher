package com.pokewilds.world;

/**
 * <p>
 * The {@code WorldSize} is the amount of tiles each world has.
 * </p>
 */
public enum WorldSize {
    TINY(32, 32, 0.002, 0.004),
    SMALL(64, 64, 0.0005, 0.0002),
    MEDIUM(128, 128, 0.0005, 0.002),
    LARGE(256, 256, 0.002, 0.004),
    EXTREME(512, 512, 0.002, 0.004);

    public final int width, height;
    public final double heightFrequency, temperatureFrequency;

    /**
     * <p>Constructs a new {@code WorldSize}.</p>
     *
     * @param width  The width.
     * @param height The height.
     */
    WorldSize(int width, int height, double heightFrequency, double temperatureFrequency) {
        this.width = width;
        this.height = height;
        this.heightFrequency = heightFrequency;
        this.temperatureFrequency = temperatureFrequency;
    }

    /**
     * <p>
     * Whether the X and Y chunk position is valid.
     * </p>
     *
     * @param chunkX The Map Chunk x.
     * @param chunkY The Map Chunk y.
     *
     * @return {@code true} if the chunk is valid, otherwise {@code false}.
     */
    public boolean isValidChunk(int chunkX, int chunkY) {
        return chunkX >= 0 && chunkX < this.width && chunkY >= 0 && chunkY <= this.height;
    }

    /**
     * <p>
     * The name of the {@code WorldSize} instance in a human readable string.
     * </p>
     *
     * @return The name.
     */
    public String getName() {
        switch (this) {
            case TINY:
                return "Tiny";
            case SMALL:
                return "Small";
            case MEDIUM:
                return "Medium";
            case LARGE:
                return "Large";
            case EXTREME:
                return "Extreme";
        }
        return null;
    }
}
