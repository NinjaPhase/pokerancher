package com.pokewilds.world.tile;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.pokewilds.Constants;
import com.pokewilds.gfx.OrderedRenderable;

public class GrassTileEffect extends TileEffect implements OrderedRenderable {
    private final int[] TILES = {256, 257, 384, 385};

    private final Texture tilesetTexture;
    private float time;
    private int frame;

    public GrassTileEffect(Texture tilesetTexture, int x, int y) {
        super(x, y);
        this.tilesetTexture = tilesetTexture;
    }

    @Override
    public void update(float deltaTime) {
        if (frame < TILES.length) {
            time += deltaTime;
            if (time > Constants.FRAME * 8) {
                frame++;
                time -= Constants.FRAME * 8;
            }
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (this.frame == 0) {
            return;
        }
        int srcX = TILES[this.frame - 1] % (tilesetTexture.getWidth() / 16);
        int srcY = TILES[this.frame - 1] / (tilesetTexture.getHeight() / 16);
        batch.draw(tilesetTexture, this.x * 16.0f, -this.y * 16.0f, srcX * 16, srcY * 16, 16, 16);
    }

    @Override
    public boolean isDone() {
        return this.shouldDelete && frame == TILES.length - 1;
    }

    @Override
    public float getYIndex() {
        return super.getYIndex() + 1.0f;
    }

}
