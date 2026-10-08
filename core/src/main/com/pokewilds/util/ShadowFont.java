package com.pokewilds.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

/**
 * <p>
 * A {@code ShadowFont} is a font which uses a secondary font to cast a
 * shadow onto the primary font, this is done so that the main font and
 * shadow can have separate colours.
 * </p>
 *
 * <small>Note: This could be done with a shader but was done in a simpler less
 * performant way to save time.</small>
 */
public class ShadowFont implements Disposable {
    public static final Color GRAY = new Color(0x606060ff), SHADOW_GRAY = new Color(0xd0d0c8ff);

    private Color mainColor;
    private Color backColor;
    private BitmapFont main, back;

    /**
     * <p>Constructs a {@code ShadowFont} with a main font and a back font.</p>
     *
     * @param main Represents the main font.
     * @param back Represents the shadow font.
     */
    public ShadowFont(BitmapFont main, BitmapFont back) {
        this.main = main;
        this.back = back;
        this.mainColor = new Color(0xFFFFFFFF);
        this.backColor = new Color(0.43f, 0.43f, 0.43f, 1.0f);
    }

    public void forWhiteBG() {
        this.mainColor = ShadowFont.GRAY;
        this.backColor = ShadowFont.SHADOW_GRAY;
    }

    /**
     * <p>
     * Draws the text given at x and y.
     * </p>
     *
     * @param batch The batch to draw on.
     * @param str   The string.
     * @param x     The x position.
     * @param y     The y position.
     */
    public void draw(SpriteBatch batch, CharSequence str, float x, float y) {
        back.setColor(backColor);
        back.draw(batch, str, x, y);
        main.setColor(mainColor);
        main.draw(batch, str, x, y);
    }

    /**
     * <p>
     * Draws the text given at x and y with a specific colour.
     * </p>
     *
     * @param batch     The sprite batch.
     * @param str       The string to draw.
     * @param x         The x position.
     * @param y         The y position.
     * @param mainColor The main colour.
     * @param backColor The shadow colour.
     */
    public void draw(SpriteBatch batch, CharSequence str, float x, float y, Color mainColor, Color backColor) {
        back.setColor(backColor);
        back.draw(batch, str, x, y);
        main.setColor(mainColor);
        main.draw(batch, str, x, y);
    }

    /**
     * <p>
     * Sets the primary colour using RGBA.
     * </p>
     *
     * @param r The red component.
     * @param g The green component.
     * @param b The blue component.
     * @param a The alpha component.
     */
    public void setColor(float r, float g, float b, float a) {
        this.mainColor.r = r;
        this.mainColor.g = g;
        this.mainColor.b = b;
        this.mainColor.a = a;
    }

    /**
     * <p>
     * Sets the shadow colour using RGBA values.
     * </p>
     *
     * @param r The red component.
     * @param g The green component.
     * @param b The blue component.
     * @param a The alpha component.
     */
    public void setShadow(float r, float g, float b, float a) {
        this.backColor.r = r;
        this.backColor.g = g;
        this.backColor.b = b;
        this.backColor.a = a;
    }

    /**
     * <p>
     * Creates a new {@link GlyphLayout}.
     * </p>
     *
     * <small>Note: This returns an entirely new object so should be reused if
     * possible.</small>
     *
     * @param sequence The sequence of characters to use.
     *
     * @return A new {@link GlyphLayout}.
     */
    public GlyphLayout createLayout(CharSequence sequence) {
        return new GlyphLayout(this.main, sequence);
    }

    @Override
    public void dispose() {
        if (this.main != null) {
            this.main.dispose();
            this.main = null;
        }
        if (this.back != null) {
            this.back.dispose();
            this.back = null;
        }
    }

    /**
     * <p>Gets the underlying primary font.</p>
     *
     * @return The underlying primary font.
     */
    public BitmapFont getPrimaryFont() {
        return this.main;
    }
}
