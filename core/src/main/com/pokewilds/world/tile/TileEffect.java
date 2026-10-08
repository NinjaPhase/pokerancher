package com.pokewilds.world.tile;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.pokewilds.gfx.OrderedRenderable;

/**
 * <p>
 * {@code TileEffect}'s are client-side effects which are spawned by the
 * server but then purely processed client side.
 * </p>
 *
 * <p>
 * This reduces load on the server as it doesn't have to send any data.
 * </p>
 */
public abstract class TileEffect implements OrderedRenderable {

    protected int x, y;
    protected boolean shouldDelete;

    public TileEffect(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public abstract void update(float deltaTime);

    public abstract void draw(SpriteBatch batch);

    public boolean isDone() {
        return this.shouldDelete;
    }

    public void setDelete(boolean b) {
        this.shouldDelete = b;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public float getYIndex() {
        return this.y * 16.0f;
    }
}
