package com.pokewilds.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Disposable;

public final class FontManager implements Disposable {
    private static final FontManager INSTANCE = new FontManager();

    private ShadowFont mainFont;
    private ShadowFont smallFont;
    private BitmapFont uiFont;

    public static FontManager getInstance() {
        return INSTANCE;
    }

    private FontManager() {

    }

    public void loadDefaults() {
        this.mainFont = new ShadowFont(new BitmapFont(Gdx.files.local("assets/fonts/fr.fnt")),
                new BitmapFont(Gdx.files.local("assets/fonts/frshadow.fnt")));
        this.smallFont = new ShadowFont(new BitmapFont(Gdx.files.local("assets/fonts/small.fnt")),
                new BitmapFont(Gdx.files.local("assets/fonts/smallshadow.fnt")));
    }

    public void dispose() {
        if (this.mainFont != null) {
            this.mainFont.dispose();
            this.mainFont = null;
        }
        if (this.smallFont != null) {
            this.smallFont.dispose();
            this.smallFont = null;
        }
    }

    public ShadowFont getMainFont() {
        return this.mainFont;
    }

    public ShadowFont getSmallFont() {
        return this.smallFont;
    }

}
