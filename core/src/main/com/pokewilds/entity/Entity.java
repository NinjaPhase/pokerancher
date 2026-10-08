package com.pokewilds.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.pokewilds.Constants;
import com.pokewilds.gfx.OrderedRenderable;
import com.pokewilds.scene.GameScene;
import com.pokewilds.world.tile.TileEvent;

/**
 * <p>A {@code Entity} is something within the game that has function.</p>
 */
public abstract class Entity implements OrderedRenderable {

    protected GameScene gameScene;
    protected int id;
    protected Vector2 position;
    protected Vector2 serverPosition;
    protected long serverTimestamp;

    protected int tileX, tileY, prevTileX, prevTileY;
    protected Direction lookDirection, moveDirection;
    protected float animationTimer;

    /**
     * <p>Constructs a new {@code Entity}.</p>
     *
     * @param gameScene The game scene this entity belongs to.
     * @param id        The entity id.
     */
    protected Entity(GameScene gameScene, int id) {
        this.gameScene = gameScene;
        this.id = id;
    }

    /**
     * <p>Updates the {@code Entity}, this contains server code for smooth positioning between server positions.</p>
     *
     * @param deltaTime The delta time.
     */
    public void update(float deltaTime) {
        float alpha = 0.0f;
        if (this.serverPosition != null) {
            long currentTime = System.currentTimeMillis();
            float timeSinceUpdate = (currentTime - serverTimestamp) / 1000f;

            alpha = Math.min(1.0f, timeSinceUpdate);

            this.position.lerp(this.serverPosition, alpha);
            if (this.position.epsilonEquals(this.serverPosition)) {
                this.serverPosition = null;
            }
        }
    }

    /**
     * <p>Draws this entity on the map.</p>
     *
     * @param batch The sprite batch to draw onto.
     */
    public abstract void draw(SpriteBatch batch);

    /**
     * <p>Explicitly sets this entities position and server position.</p>
     *
     * @param x The x position.
     * @param y The y position.
     */
    public void teleport(int x, int y) {
        this.position.set(x * 16.0f, y * 16.0f);
        this.serverTimestamp = System.currentTimeMillis();
        this.serverPosition.set(x * 16.0f, y * 16.0f);
        this.tileX = x;
        this.tileY = y;
    }

    /**
     * <p>Sets the server position, this position will be Lerped to, setting the position directly will result in an
     * instant teleportation.</p>
     *
     * @param timestamp The timestamp sent by the server. (The timestamp should be modified with expected lag).
     * @param x         The x position.
     * @param y         The y position.
     */
    public void setServerPosition(long timestamp, float x, float y) {
        this.serverTimestamp = timestamp;
        this.serverPosition = new Vector2(x, y);
    }

    /**
     * <p>Handles the event for a tile with tileX and tileY.</p>
     *
     * @param event The event to handle.
     */
    public void handleTileEvent(TileEvent event, int tileX, int tileY) {

    }

    /**
     * <p>Gets the id of this entity, the id is used for server purposes, (0-7 reserved for players).</p>
     *
     * @return The id.
     */
    public int getId() {
        return id;
    }

    /**
     * <p>Gets the entity type to give to clients.</p>
     *
     * @return The entity type.
     */
    public abstract int getEntityType();

    /**
     * <p>Whether the entity has been updated.</p>
     * <p>
     * TODO: See about specifying types of updates.
     *
     * @return Whether the entity has been updated.
     */
    public abstract boolean hasUpdated();

    /**
     * <p>Sets the tile position of the entity, if the entity is solid then the tile should be specified as
     * occupied.</p>
     *
     * @param tx The x position.
     * @param ty The y position.
     */
    public void setTilePos(int tx, int ty) {
        this.tileX = tx;
        this.tileY = ty;
    }

    /**
     * <p>The tile x position.</p>
     *
     * @return The tile x position.
     */
    public int getX() {
        return this.tileX;
    }

    /**
     * <p>The tile y position.</p>
     *
     * @return The tile y position.
     */
    public int getY() {
        return this.tileY;
    }

    /**
     * <p>The previous tile x position.</p>
     *
     * @return The previous tile x position.
     */
    public int getPrevTileX() {
        return this.prevTileX;
    }

    /**
     * <p>The previous tile y position.</p>
     *
     * @return The previous tile y position.
     */
    public int getPrevTileY() {
        return this.prevTileY;
    }

    /**
     * <p>Gets the map chunk this entity belongs in.</p>
     *
     * @return The map chunk x coordinate.
     */
    public int getMapChunkX() {
        return (int) Math.floor(this.position.x / (Constants.CHUNK_SIZE * 16.0f));
    }

    /**
     * <p>Gets the map chunk this entity belongs in.</p>
     *
     * @return The map chunks y coordinate.
     */
    public int getMapChunkY() {
        return (int) Math.floor(this.position.y / (Constants.CHUNK_SIZE * 16.0f));
    }

    /**
     * <p>The absolute position of the entity, used for tweening between tiles..</p>
     *
     * @return The absolute position.
     */
    public Vector2 getPosition() {
        return this.position;
    }

    /**
     * <p>Gets the look direction.</p>
     *
     * @return The look direction.
     */
    public Direction getLookDirection() {
        return this.lookDirection;
    }

    /**
     * <p>Sets the look direction.</p>
     *
     * @param dir The look direction.
     */
    public void setLookDirection(Direction dir) {
        this.lookDirection = dir;
    }

    public float getYIndex() {
        return this.position.y;
    }
}
