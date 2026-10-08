package com.pokewilds.scene.transition;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.pokewilds.Constants;

public class FadeOutIn {
    private static Texture FADE_TEXTURE;
    private final int fadeOutFrameCount, delayFrameCount, fadeInFrameCount;
    private float t;
    private int frame;

    public static void dispose() {
        if (FADE_TEXTURE == null) {
            return;
        }
        FADE_TEXTURE.dispose();
        FADE_TEXTURE = null;
    }

    public FadeOutIn(final int fadeOut, final int delay, final int fadeIn) {
        if (FADE_TEXTURE == null) {
            Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            p.drawPixel(0, 0, 0xffffffff);
            FADE_TEXTURE = new Texture(p);
        }
        this.fadeOutFrameCount = fadeOut;
        this.delayFrameCount = delay;
        this.fadeInFrameCount = fadeIn;
    }

    public boolean update(float deltaTime) {
        if (frame >= fadeOutFrameCount + delayFrameCount + fadeInFrameCount) {
            return true;
        }
        t += deltaTime;
        while (t >= Constants.FRAME) {
            frame = Math.min(frame + 1, fadeOutFrameCount + delayFrameCount + fadeInFrameCount);
            t -= Constants.FRAME;
            System.out.println(this.frame + " - " + this.getState());
        }
        return false;
    }

    public void drawFade(Batch batch, float w, float h) {
        if (FADE_TEXTURE == null) {
            return;
        }
        Color prev = batch.getColor().cpy();
        if (frame < fadeOutFrameCount) {
            batch.setColor(0f, 0f, 0f, (float) frame / (float) fadeInFrameCount);
            batch.draw(FADE_TEXTURE, 0f, -h, w, h);
        } else if (frame < fadeOutFrameCount + delayFrameCount) {
            batch.setColor(0f, 0f, 0f, 1f);
            batch.draw(FADE_TEXTURE, 0f, -h, w, h);
        } else {
            int fc = this.frame - this.delayFrameCount - this.fadeOutFrameCount;
            batch.setColor(0f, 0f, 0f, 1f - (float) fc / (float) fadeOutFrameCount);
            batch.draw(FADE_TEXTURE, 0f, -h, w, h);
        }
        batch.setColor(prev);
    }

    public boolean shouldRender() {
        return this.getState() == State.FADE_IN || this.getState() == State.DONE;
    }

    public boolean isDone() {
        return this.getState() == State.DONE;
    }

    public State getState() {
        if (this.frame < this.fadeOutFrameCount) {
            return State.FADE_OUT;
        } else if (this.frame < this.fadeOutFrameCount + this.delayFrameCount) {
            return State.DELAY;
        } else if (this.frame < this.fadeOutFrameCount + this.delayFrameCount + this.fadeInFrameCount) {
            return State.FADE_IN;
        }
        return State.DONE;
    }

    public enum State {
        FADE_OUT,
        DELAY,
        FADE_IN,
        DONE
    }
}
