package com.pokewilds.gfx;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * <p>
 * A {@code OrderedRenderable} is an object that can be updated or rendered.
 * </p>
 *
 * <p>
 * This object exists because when drawing objects, they need to be sorted by
 * the Z value first, otherwise silliness occurs.
 * </p>
 */
public interface OrderedRenderable {
    void update(float deltaTime);

    void draw(SpriteBatch batch);

    float getYIndex();
}
