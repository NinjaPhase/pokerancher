package com.pokewilds;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.ninjaphase.SampleMod;
import com.pokewilds.api.world.gen.WorldType;
import com.pokewilds.plugin.ModLoader;
import com.pokewilds.pokemon.SpeciesRegistry;
import com.pokewilds.scene.ClientGameScene;
import com.pokewilds.scene.HostGameScene;
import com.pokewilds.scene.Scene;
import com.pokewilds.scene.SplashScreen;
import com.pokewilds.scene.transition.FadeOutIn;
import com.pokewilds.util.*;
import com.pokewilds.world.DefaultWorldType;
import com.pokewilds.world.biome.BiomeRegistry;
import com.pokewilds.world.tile.TileRegistry;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * <p>
 * The {@code PokeWilds} class is the main entry-point for the LibGDX game, as such it contains the boiler
 * plate code for the entire game.
 * </p>
 */
public class PokeWilds extends ApplicationAdapter {
    public static boolean IS_DEV = false;
    public static int IMMEDIATE_MODE = 0;
    private static PokeWilds INSTANCE;

    private final FontManager fontManager;
    private final TextureManager textureManager;
    private final ModLoader modLoader;
    private final Queue<Runnable> contextQueue;
    private Scene currentScene;
    private DatagramSocket lanSocket;

    public static PokeWilds getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PokeWilds();
        }
        return INSTANCE;
    }

    public static TextureManager getTextureManager() {
        return getInstance().textureManager;
    }

    public static void createLANSocket(int port) throws SocketException {
        getInstance().lanSocket = new DatagramSocket(port);
    }

    public static void closeLANSocket() {
        if (getInstance().lanSocket == null) {
            return;
        }
        getInstance().lanSocket.close();
        getInstance().lanSocket = null;
    }

    public static DatagramSocket getLANSocket() {
        return getInstance().lanSocket;
    }

    /**
     * <p>Private constructor, instance should be created through {@link PokeWilds#getInstance}</p>
     */
    private PokeWilds() {
        this.textureManager = TextureManager.getInstance();
        this.fontManager = FontManager.getInstance();
        this.modLoader = ModLoader.getInstance();
        this.contextQueue = new ArrayDeque<>();
    }

    @Override
    public void create() {
        if (PokeWilds.IS_DEV) {
            Gdx.app.setLogLevel(Application.LOG_DEBUG);
        }
        BiomeRegistry.loadDefaults();
        SpeciesRegistry.loadDefaults(this.textureManager);

        WorldType.register(new DefaultWorldType());

        //        WorldGenerator gen = new WorldGenerator((new SecureRandom()).nextLong(), WorldSize.MEDIUM);
        //        WorldUtils.saveImage(gen, Gdx.files.local("worlds/test"));

        this.modLoader.loadMod(new SampleMod());
        this.modLoader.loadFolder(new File("mods"));

        if (Gdx.app.getType() == Application.ApplicationType.HeadlessDesktop) {
            try {
                TileRegistry.loadDefaults();
                FileHandle serverFiles = Gdx.files.local("saves").child("server");
                if (serverFiles.exists()) {
                    this.currentScene = new HostGameScene(this, serverFiles, 5173);
                } else {
                    this.currentScene = new HostGameScene(this, "server", 5173);
                }
            } catch (IOException e) {
                Gdx.app.error("PokeRancher", "Unable to create host game scene", e);
            }
        } else {
            if (Settings.getScreenMode() == ScreenMode.FULLSCREEN) {
                Gdx.graphics.setFullscreenMode(Settings.getDisplayMode());
            } else if (Settings.getScreenMode() == ScreenMode.BORDERLESS) {
                Gdx.graphics.setUndecorated(true);
                Gdx.graphics.setWindowedMode(Settings.getWidth(), Settings.getHeight());
            } else {
                Gdx.graphics.setUndecorated(false);
                Gdx.graphics.setWindowedMode(Settings.getWidth(), Settings.getHeight());
            }

            if (IMMEDIATE_MODE > 0) {
                this.getFontManager().loadDefaults();
                TileRegistry.loadDefaults();
                if (IMMEDIATE_MODE == 1) {
                    this.currentScene = new HostGameScene(this, Gdx.files.local("saves/test_world"));
                } else {
                    try {
                        this.currentScene = new ClientGameScene(this, InetAddress.getLocalHost());
                    } catch (Exception e) {
                        Gdx.app.error("PokeRancher", "Unable to create client game scene", e);
                    }
                }
            } else {
                this.currentScene = new SplashScreen(this);
            }
            this.currentScene.resize(Settings.getWidth(), Settings.getHeight());

            ScreenInputProcessor sin = new ScreenInputProcessor(this);
            Gdx.input.setInputProcessor(sin);
            Controllers.addListener(sin);
        }
    }

    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode(480, 320);
                Settings.setScreenMode(ScreenMode.WINDOWED);
                Settings.resize(480, 320);
            } else {
                Gdx.graphics.setFullscreenMode(
                        Gdx.graphics.getDisplayModes()[Gdx.graphics.getDisplayModes().length - 1]);
                Settings.setScreenMode(ScreenMode.FULLSCREEN);
                Settings.resize(Gdx.graphics.getDisplayModes()[Gdx.graphics.getDisplayModes().length - 1].width,
                        Gdx.graphics.getDisplayModes()[Gdx.graphics.getDisplayModes().length - 1].height);
            }
        }

        float timeScale = Gdx.input.isKeyPressed(Input.Keys.SPACE) ? 20.0f : 1.0f;
        this.currentScene.update(Gdx.graphics.getDeltaTime() * timeScale);
        if (Gdx.app.getType() != Application.ApplicationType.HeadlessDesktop) {
            this.currentScene.render();
        }

        Runnable r;
        while ((r = this.contextQueue.poll()) != null) {
            r.run();
        }
    }

    @Override
    public void resize(final int width, final int height) {
        Settings.resize(width, height);
        this.currentScene.resize(width, height);
    }

    @Override
    public void dispose() {
        FadeOutIn.dispose();
        this.currentScene.dispose();
        Settings.save();
        this.textureManager.dispose();
        this.fontManager.dispose();
    }

    /**
     * <p>
     * Adds a runnable to the context queue to be executed on the main thread.
     * </p>
     *
     * @param r The runnable to execute.
     */
    public void invokeLater(Runnable r) {
        this.contextQueue.add(r);
    }

    /**
     * <p>
     * Sets the current scene which will be processed, this is not
     * </p>
     *
     * @param screen The screen.
     */
    public void setScreen(final Scene screen) {
        this.currentScene.dispose();
        this.currentScene = screen;
    }

    /**
     * @return The current scene the game is processing.
     * @see Scene
     */
    public Scene getScene() {
        return this.currentScene;
    }

    /**
     * <p>
     * Gets a texture from the texture cache.
     * </p>
     *
     * @param name The name of the texture.
     * @return The {@link com.badlogic.gdx.graphics.Texture Texture}.
     */
    public Texture getTexture(String name) {
        return textureManager.getTexture(name);
    }

    /**
     * <p>
     * Gets a texture from the texture cache.
     * </p>
     *
     * @param name       The name of the texture.
     * @param useMipMaps Whether to use mipmaps for the texture.
     * @return The {@link com.badlogic.gdx.graphics.Texture Texture}.
     */
    public Texture getTexture(String name, boolean useMipMaps) {
        return textureManager.getTexture(name, useMipMaps);
    }

    /**
     * <p>
     * Gets a texture atlas from the texture cache.
     * </p>
     *
     * @param name The name of the texture atlas.
     * @return The {@link com.badlogic.gdx.graphics.g2d.TextureAtlas TextureAtlas}.
     */
    public TextureAtlas getTextureAtlas(String name) {
        return textureManager.getAtlas(name);
    }

    public ShadowFont getMainFont() {
        return this.fontManager.getMainFont();
    }

    public FontManager getFontManager() {
        return this.fontManager;
    }

    public ShadowFont getSmallFont() {
        return this.fontManager.getSmallFont();
    }
}
