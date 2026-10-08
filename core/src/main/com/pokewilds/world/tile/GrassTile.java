package com.pokewilds.world.tile;

import com.pokewilds.entity.Entity;
import com.pokewilds.scene.GameScene;

public class GrassTile extends Tile {

    public GrassTile(int layer, int tileIndex, int crushedTileIndex) {
        super(layer, 0, new int[][]{new int[]{tileIndex, crushedTileIndex}});
    }

    @Override
    public boolean onTileEvent(TileEvent event, int x, int y, GameScene scene, Entity e) {
        if (event == TileEvent.TILE_ENTERED) {
            if (scene.setTileState(this.layer, x, y, 1, true)) {
                scene.createTileEffect(new GrassTileEffect(scene.getTilesetTexture(), x, y));
                return false;
            }
        } else if (event == TileEvent.TILE_LEFT) {
            if (scene.setTileState(this.layer, x, y, 0, true)) {
                scene.deleteTileEffect(GrassTileEffect.class, x, y);
                return false;
            }
        }
        return false;
    }
}
