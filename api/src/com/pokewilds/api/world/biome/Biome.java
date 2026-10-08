package com.pokewilds.api.world.biome;

/**
 * <p>
 * A {@code Biome} defines a discrete way to generate the world, each Biome occupies a space according to a
 * priority.
 * </p>
 *
 * <p>
 * Biomes should use the priority system to sort themselves by specificity, the {@code pokemon:void} Biome is the
 * lowest priority and occupies any space which isn't defined, it contains no features and no tiles so can't be
 * walked upon.
 * </p>
 */
public abstract class Biome {
    public static final int PRIORITY_DEFAULT = 0;
    public static final int PRIORITY_VERY_LOW = 1;
    public static final int PRIORITY_LOW = 2;
    public static final int PRIORITY_MEDIUM = 3;
    public static final int PRIORITY_HIGH = 4;
    public static final int PRIORITY_VERY_HIGH = 5;

    /**
     * <p>Generates a tile at a specific location.</p>
     *
     * @param x The absolute x position for the tile.
     * @param y The absolute y position for the tile.
     */
    public abstract void generateTile(int x, int y);

    /**
     * <p>Checks whether this biome can be generated in the conditions specified.</p>
     *
     * @return Whether the biome can be generated.
     */
    public abstract boolean isValid();

    /**
     * @return The priority of this {@code Biome} in the biome stack.
     */
    public abstract int getPriority();
}
