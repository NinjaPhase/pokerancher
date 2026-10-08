package com.pokewilds.util;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class TextureUtils {

    public static void drawWrapped(SpriteBatch batch, TextureRegion texture, float x, float y, float w, float h) {
        for (int ty = 0; ty < h / texture.getRegionHeight(); ty++) {
            for (int tx = 0; tx < w / texture.getRegionWidth(); tx++) {
                batch.draw(texture, x + tx * texture.getRegionWidth(), y + ty * texture.getRegionHeight());
            }
        }
    }

    private TextureUtils() {
    }

}
