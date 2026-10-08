package com.pokewilds.world.tile;

import com.pokewilds.entity.Entity;
import com.pokewilds.scene.GameScene;

public class AutoTile extends Tile {

    public AutoTile(final int layer, final int startTile, int[] connectedTiles) {
        this.layer = layer;
        this.tiles = new int[1][16];
        for (int i = 0; i < this.tiles[0].length; i++) {
            this.tiles[0][i] = startTile + TILE_CONNECTIONS[i];
        }
        this.connectedTiles = connectedTiles;
    }

    @Override
    public boolean onTileEvent(TileEvent event, int x, int y, GameScene scene, Entity e) {
        if (event != TileEvent.TILE_UPDATED) {
            return false;
        }
        int state = 0;
        if (this.isConnected(scene.getTileAt(this.layer, x, y - 1))) {
            state |= 0b0001;
        }
        if (this.isConnected(scene.getTileAt(this.layer, x + 1, y))) {
            state |= 0b0010;
        }
        if (this.isConnected(scene.getTileAt(this.layer, x, y + 1))) {
            state |= 0b0100;
        }
        if (this.isConnected(scene.getTileAt(this.layer, x - 1, y))) {
            state |= 0b1000;
        }
        return scene.setTileState(this.layer, x, y, state, true);
    }


    /**
     * <p>Checks whether the tile is connected to another tile.</p>
     *
     * @param otherTileId The other tile.
     *
     * @return Whether the tile is connected or not.
     */
    public boolean isConnected(final int otherTileId) {
        if (otherTileId == -1) {
            return true;
        }
        for (int connectedTile : this.connectedTiles) {
            if (connectedTile != otherTileId) {
                continue;
            }
            return true;
        }
        return false;
    }
}
