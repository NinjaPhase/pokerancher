package com.pokewilds.scene.gui;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.pokewilds.PokeWilds;
import com.pokewilds.scene.GameScene;

public abstract class Gui implements Disposable {
    protected PokeWilds game;
    protected GameScene scene;

    public Gui(GameScene scene) {
        this.scene = scene;
        this.game = scene.getGame();
    }

    public abstract boolean update(float deltaTime);

    public abstract void render(SpriteBatch batch, Camera uiCamera);

    public abstract boolean keyDown(int keyCode);

    public abstract boolean keyUp(int keyCode);

    public abstract boolean buttonDown(Controller controller, int keyCode);

    public abstract boolean buttonUp(Controller controller, int keyCode);

    public abstract void dispose();
}
