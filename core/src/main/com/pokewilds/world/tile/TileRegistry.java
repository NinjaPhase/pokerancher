package com.pokewilds.world.tile;

import java.util.HashMap;
import java.util.Map;

public class TileRegistry {

    private static final Map<Integer, Tile> TILE_MAP = new HashMap<>();
    private static final Map<String, Tile> TILE_NAME_MAP = new HashMap<>();

    public static void loadDefaults() {
        TileRegistry.register(Tile.TILE_WATER, "water",
                new Tile(Tile.LAYER_GROUND, Tile.FLAG_WATER, 512, 513, 514, 515, 516, 517, 518));
        TileRegistry.register(Tile.TILE_GRASS, "grass", new Tile(Tile.LAYER_GROUND, 1));
        TileRegistry.register(Tile.TILE_TALL_GRASS, "tall_grass", new GrassTile(Tile.LAYER_GROUND, 128, 129));
        TileRegistry.register(Tile.TILE_SAND, "sand",
                new AutoTile(Tile.LAYER_GROUND, 2, new int[]{Tile.TILE_SAND, Tile.TILE_WATER}));

        TileRegistry.register(Tile.TILE_SNOW, "snow", new Tile(Tile.LAYER_GROUND, 6));

        TileRegistry.register(Tile.TILE_TREE_BOTTOM_LEFT, "tree_bottom_left",
                new Tile(Tile.LAYER_UOBJ, Tile.FLAG_SOLID, 896));
        TileRegistry.register(Tile.TILE_TREE_BOTTOM_RIGHT, "tree_bottom_right",
                new Tile(Tile.LAYER_UOBJ, Tile.FLAG_SOLID, 897));
        TileRegistry.register(Tile.TILE_TREE_MID_LEFT, "tree_mid_left",
                new Tile(Tile.LAYER_UOBJ, Tile.FLAG_SOLID, 768));
        TileRegistry.register(Tile.TILE_TREE_MID_RIGHT, "tree_mid_right",
                new Tile(Tile.LAYER_UOBJ, Tile.FLAG_SOLID, 769));
        TileRegistry.register(Tile.TILE_TREE_TOP_LEFT, "tree_top_left", new Tile(Tile.LAYER_OOBJ, 640));
        TileRegistry.register(Tile.TILE_TREE_TOP_RIGHT, "tree_top_right", new Tile(Tile.LAYER_OOBJ, 641));
    }

    public static void register(int id, String name, Tile tile) {
        if (TILE_MAP.containsKey(id)) {
            throw new RuntimeException("tile registry already contains key: " + id);
        }
        TILE_MAP.put(id, tile);
        TILE_NAME_MAP.put(name, tile);
    }

    public static Tile get(int id) {
        return TILE_MAP.get(id);
    }

    public static Tile get(String name) {
        return TILE_NAME_MAP.get(name);
    }

    private TileRegistry() {

    }

}

