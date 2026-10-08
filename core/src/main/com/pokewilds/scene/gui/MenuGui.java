package com.pokewilds.scene.gui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.pokewilds.scene.GameScene;
import com.pokewilds.scene.TitleScreen;

public class MenuGui extends Gui {

    private final String[] options;
    private int selected;

    public MenuGui(GameScene scene) {
        super(scene);
        this.options = new String[]{
                "POKEMON", "BAG", "RED", "SAVE", "OPTION", "TITLE", "EXIT"
        };
    }

    @Override
    public boolean update(float deltaTime) {
        return false;
    }

    @Override
    public void render(SpriteBatch batch, Camera uiCamera) {
        float maxSize = 0;
        for (int i = 0; i < options.length; i++) {
            maxSize = Math.max(maxSize, this.game.getMainFont().createLayout('\ue001' + options[i]).width);
        }
        float height = (options.length * 15.0f) + 22f;
        this.scene.getMenuPatch().draw(batch, uiCamera.viewportWidth - (maxSize + 20f), -height, maxSize + 20f, height);
        for (int i = 0; i < options.length; i++) {
            String val = (this.selected == i ? '\ue001' : '\ue000') + options[i];
            this.game.getMainFont()
                     .draw(batch, val, uiCamera.viewportWidth - (maxSize + 10f), -12f - (i * 16.0f),
                           new Color(0x606060ff), new Color(0xD0D0C8ff));
        }
    }

    @Override
    public boolean keyDown(int keyCode) {
        if (keyCode == Input.Keys.W) {
            this.selected = Math.max(this.selected - 1, 0);
        } else if (keyCode == Input.Keys.S) {
            this.selected = Math.min(this.selected + 1, this.options.length - 1);
        } else if (keyCode == Input.Keys.Z) {
            String val = this.options[this.selected];
            if (val.equalsIgnoreCase("POKEMON")) {
                this.scene.addGui(new PokemonSelectGui(this.scene));
            } else if (val.equalsIgnoreCase("TITLE")) {
                this.scene.getGame().setScreen(new TitleScreen(this.scene.getGame()));
            } else if (val.equalsIgnoreCase("EXIT")) {
                this.scene.removeGui(this);
            }
        } else if (keyCode == Input.Keys.X) {
            this.scene.removeGui(this);
        }
        return true;
    }

    @Override
    public boolean keyUp(int keyCode) {
        return true;
    }

    @Override
    public boolean buttonDown(Controller controller, int keyCode) {
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int keyCode) {
        return false;
    }

    @Override
    public void dispose() {

    }
}
