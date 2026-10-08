package com.pokewilds.world.tile;

/**
 * <p>
 * The {@code TileEvent} are events that can take place upon a tile, this allows for automatic tile-updates to be
 * sent and handles on either the server or the client.
 * </p>
 * <p>
 * For example, grass when stepped on changes it's image to a squished tile and plays an effect, upon exiting the
 * tile is then returned to the initial image, due to this being trivial it should be sent to the client and then
 * calculated client side.
 * </p>
 */
public enum TileEvent {
    /**
     * Called when a tile is either created, or an adjacent tile is updated.
     */
    TILE_UPDATED,
    /**
     * Called when an entity leaves the tile.
     */
    TILE_LEFT,
    /**
     * Called when an entity begins entering the tile.
     */
    TILE_ENTERED,
    /**
     * Called when an entity is fully stepped on a tile.
     */
    TILE_FINISHED
}
