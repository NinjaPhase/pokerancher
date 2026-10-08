package com.pokewilds.world.tile;

import com.pokewilds.entity.Entity;
import com.pokewilds.scene.GameScene;

/**
 * <p>A {@code Tile} contains the data for a singular tile.</p>
 */
public class Tile {
    public static final int[] TILE_CONNECTIONS = new int[]{
            384, //0
            256, //1  U
            385, //2  R
            257, //3  UR
            0,   //4  D
            128, //5  UD
            1,   //6  RD
            129, //7  URD
            387, //8  L
            259, //9  UL
            386, //10 RL
            258, //11 URL
            3,   //12 LD
            131, //13 ULD
            2,   //14 RDL
            130, //15 URDL
    };
    public static final int LAYER_GROUND = 0;
    public static final int LAYER_WAVES = 1;
    public static final int LAYER_OVERLAY = 2;
    public static final int LAYER_UOBJ = 3;
    public static final int LAYER_OOBJ = 4;
    public static final int LAYER_OVERHANG = 5;
    public static final int LAYER_COUNT = 6;

    public static final int TILE_WATER = 0;
    public static final int TILE_GRASS = 1;
    public static final int TILE_SOFT_SAND = 2;
    public static final int TILE_SAND = 3;
    public static final int TILE_SNOW = 4;
    public static final int TILE_TREE_BOTTOM_LEFT = 5;
    public static final int TILE_TREE_BOTTOM_RIGHT = 6;
    public static final int TILE_TREE_MID_LEFT = 7;
    public static final int TILE_TREE_MID_RIGHT = 8;
    public static final int TILE_TREE_TOP_LEFT = 9;
    public static final int TILE_TREE_TOP_RIGHT = 10;
    public static final int TILE_TALL_GRASS = 11;

    public static final int FLAG_SOLID = 0x1;
    public static final int FLAG_WATER = 0x2;
    public static final int FLAG_ANIMATED_LOOP = 0x4;

    protected int layer;
    protected int[][] tiles;
    protected int[] connectedTiles;
    protected int tileFlags;

    public static boolean isSolid(int flags) {
        return (flags & Tile.FLAG_SOLID) > 0;
    }

    public static boolean isWater(int flags) {
        return (flags & Tile.FLAG_WATER) > 0;
    }

    protected Tile() {
    }

    public Tile(final int layer, final int tileIndex) {
        this(layer, 0, tileIndex);
    }

    public Tile(final int layer, final int tileFlags, final int tileIndex) {
        this.layer = layer;
        this.tiles = new int[][]{new int[]{tileIndex}};
        this.tileFlags = tileFlags;
    }

    public Tile(final int layer, final int tileFlags, final int... tileIndices) {
        this.layer = layer;
        this.tiles = new int[tileIndices.length][1];
        for (int i = 0; i < this.tiles.length; i++) {
            this.tiles[i][0] = tileIndices[i];
        }
        this.tileFlags = tileFlags;
    }

    public Tile(final int layer, final int tileFlags, final int[]... tileStates) {
        this.layer = layer;
        this.tiles = new int[tileStates.length][];
        for (int i = 0; i < this.tiles.length; i++) {
            this.tiles[i] = new int[tileStates[i].length];
            System.arraycopy(tileStates[i], 0, this.tiles[i], 0, tileStates[i].length);
        }
        this.tileFlags = tileFlags;
    }

    /**
     * <p>
     * Called when something occurs on this tile.
     * </p>
     *
     * @param event The event that has occured.
     * @param x     The absolute x position of the tile.
     * @param y     The absolute y position of the tile.
     * @param scene The game scene.
     * @param e     The entity.
     * @return Whether to updated adjacent tiles.
     */
    public boolean onTileEvent(TileEvent event, int x, int y, GameScene scene, Entity e) {
        return false;
    }

    public int getTileIndex(final int frame, final int state) {
        return this.tiles[frame % this.getFrameCount()][state % this.tiles[frame % this.getFrameCount()].length];
    }

    public int getLayer() {
        return this.layer;
    }

    public int getFrameCount() {
        return this.tiles.length;
    }

    public int getFlags() {
        return this.tileFlags;
    }

}
