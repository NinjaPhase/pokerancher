package com.pokewilds;

import com.badlogic.gdx.Gdx;

/**
 * <p>
 * A group of {@code Constants} which can be used by PokéWilds to adjust
 * values.
 * </p>
 */
public final class Constants {

    /**
     * <p>The MAJOR version for releases.</p>
     */
    public static final byte MAJOR_VERSION = 1;
    /**
     * <p>The MINOR version for releases.</p>
     */
    public static final byte MINOR_VERSION = 0;
    /**
     * <p>The PATCH version for releases.</p>
     */
    public static final byte PATCH_VERSION = 0;


    /**
     * <p>The size of each {@link com.pokewilds.world.chunk.MapChunk} in tiles.</p>
     */
    public static final int CHUNK_SIZE = 32;
    /**
     * <p>The amount of chunks loaded centered around the players chunk.</p>
     */
    public static final int LOADED_CHUNKS = 10;
    /**
     * <p>The amount of chunks which are saved into one file.</p>
     */
    public static final int SAVE_SIZE = 64;
    /**
     * <p>The amount of time between each save in minutes.</p>
     */
    public static final int SAVE_TIMER = 5;
    /**
     * <p>The size of each tile in pixels.</p>
     */
    public static final int TILE_SIZE = 16;
    /**
     * <p>The rect size of each tile sheet.</p>
     */
    public static final int TILE_SHEET_SIZE = 128;

    /**
     * <p>The length of a single frame, set to GBA frames.</p>
     */
    public static final float FRAME = 1f / 59.7f;

    /**
     * <p>The ping timeout in milliseconds.</p>
     */
    public static final long PING_TIMEOUT = 1000;
    /**
     * <p>The ping time in milliseconds.</p>
     */
    public static final long PING_TIME = 100;

    /**
     * <p>Feature flag multiplayer until issues are sorted.</p>
     */
    public static final boolean ALLOW_MULTIPLAYER = false;

    /**
     * <p>
     * Gets the scale using {@link Gdx#graphics} getHeight.
     * </p>
     *
     * @return The scale.
     */
    public static float getScale() {
        return getScale(Gdx.graphics.getHeight());
    }

    /**
     * <p>Modifies the scale of each component given the height of the screen.</p>
     *
     * @param height The height of the screen.
     * @return The scale format to use.
     */
    public static float getScale(int height) {
        return Math.min(Math.max((float) Math.ceil((double) height / 159.9999) - 1.0f, 1.0f), 3.0f);
    }

    /**
     * <p>
     * Gets the scale using {@link Gdx#graphics} getHeight.
     * </p>
     *
     * @return The scale.
     */
    public static float getUIScale() {
        return getScale(Gdx.graphics.getHeight());
    }

    /**
     * <p>Modifies the scale of each component given the height of the screen.</p>
     *
     * @param height The height of the screen.
     * @return The scale format to use.
     */
    public static float getUIScale(int height) {
        return Math.min(Math.max((float) Math.ceil((double) height / 159.9999) - 1.0f, 1.0f), 5.0f);
    }

    private Constants() {
    }

}
