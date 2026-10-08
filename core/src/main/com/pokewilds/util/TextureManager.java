package com.pokewilds.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * The {@code TextureManager} is used to handle all the textures and dispose of them when necessary.
 * </p>
 */
public class TextureManager implements Disposable {
    private static final TextureManager INSTANCE = new TextureManager();

    private final Map<String, Texture> textures;
    private final Map<String, TextureAtlas> atlases;

    public static TextureManager getInstance() {
        return INSTANCE;
    }

    private TextureManager() {
        this.textures = new HashMap<>();
        this.atlases = new HashMap<>();
    }

    public Texture getTexture(String name) {
        if (this.textures.containsKey(name)) {
            return this.textures.get(name);
        }
        Texture t = new Texture(Gdx.files.internal("graphics/" + name));
        textures.put(name, t);
        return t;
    }

    public Texture getTexture(String name, boolean useMipMaps) {
        if (this.textures.containsKey(name)) {
            return this.textures.get(name);
        }
        Texture t = new Texture(Gdx.files.internal("graphics/" + name), useMipMaps);
        textures.put(name, t);
        return t;
    }

    public TextureAtlas getAtlas(String name) {
        if (this.atlases.containsKey(name)) {
            return this.atlases.get(name);
        }
        TextureAtlas t = new TextureAtlas(Gdx.files.internal("graphics/" + name));
        atlases.put(name, t);
        return t;
    }

    public void dropAtlas(String name) {
        if (!this.atlases.containsKey(name)) {
            return;
        }
        this.atlases.remove(name).dispose();
    }

    public void dropTexture(String name) {
        if (!this.textures.containsKey(name)) {
            return;
        }
        this.textures.remove(name).dispose();
    }

    @Override
    public void dispose() {
        List<Disposable> toDispose = new ArrayList<>();
        toDispose.addAll(this.textures.values());
        toDispose.addAll(this.atlases.values());
        this.textures.clear();
        this.atlases.clear();
        for (Disposable t : toDispose) {
            t.dispose();
        }
    }
}
