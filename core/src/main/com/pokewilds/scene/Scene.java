package com.pokewilds.scene;

import com.badlogic.gdx.controllers.Controller;

/**
 * <p>
 * A {@code Scene} is a categorical part of the game, such as the main menu or main gameplay, as such there can only
 * be one active scene at a time.
 * </p>
 */
public abstract class Scene {

    public abstract void update(final float deltaTime);

    public abstract void render();

    public abstract void dispose();

    public void resize(int width, int height) {
    }

    public boolean keyDown(int keyCode) {
        return false;
    }

    public boolean keyUp(int keyCode) {
        return false;
    }

    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    public boolean buttonDown(Controller controller, int buttonCode) {
        return false;
    }

    public boolean buttonUp(Controller controller, int buttonCode) {
        return false;
    }

    public boolean keyTyped(char character) {
        return false;
    }
}
