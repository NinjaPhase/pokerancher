package com.pokewilds.scene.gui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.pokewilds.Constants;
import com.pokewilds.entity.Player;
import com.pokewilds.pokemon.Pokemon;
import com.pokewilds.scene.GameScene;
import com.pokewilds.scene.transition.FadeOutIn;
import com.pokewilds.util.TextureUtils;

public class PokemonSelectGui extends Gui {
    private final Texture status;
    private final TextureAtlas statusAtlas;
    private final TextureRegion statusBg, statusFrame, hpStart, hpEmpty, hpGreen, hpEnd;
    private final NinePatch[] mainPokemon, otherPokemon;
    private final NinePatch statusBgPatch;
    private final Texture tex;
    private FadeOutIn fade;
    private int selection;
    private int frame;
    private float frameTimer;
    private boolean isLeaving;

    public PokemonSelectGui(GameScene scene) {
        super(scene);
        this.statusAtlas = game.getTextureAtlas("statusbg.atlas");
        this.status = this.statusAtlas.getTextures().first();
        this.statusBg = new TextureRegion(this.status, 0, 0, 48, 48);
        this.statusFrame = new TextureRegion(this.status, 48, 0, 48, 48);
        this.mainPokemon = new NinePatch[]{
                new NinePatch(new TextureRegion(this.status, 0, 48, 48, 48), 8, 8, 8, 8),
                new NinePatch(new TextureRegion(this.status, 0, 96, 48, 48), 8, 8, 8, 8),
                new NinePatch(new TextureRegion(this.status, 0, 144, 48, 48), 8, 8, 8, 8),
        };
        this.otherPokemon = new NinePatch[]{
                new NinePatch(new TextureRegion(this.status, 48, 48, 48, 48), 8, 8, 8, 8),
                new NinePatch(new TextureRegion(this.status, 48, 96, 48, 48), 8, 8, 8, 8),
                new NinePatch(new TextureRegion(this.status, 48, 144, 48, 48), 8, 8, 8, 8),
        };
        this.statusBgPatch = new NinePatch(this.statusFrame, 24, 0, 14, 14);
        this.hpStart = new TextureRegion(this.status, 96, 24, 15, 7);
        this.hpEmpty = new TextureRegion(this.status, 111, 24, 1, 7);
        this.hpGreen = new TextureRegion(this.status, 114, 24, 1, 7);
        this.hpEnd = new TextureRegion(this.status, 115, 24, 2, 7);
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE);
        p.drawRectangle(0, 0, 1, 1);
        this.tex = new Texture(p);
        this.fade = new FadeOutIn(20, 20, 20);
    }

    @Override
    public boolean update(float deltaTime) {
        this.fade.update(deltaTime);
        this.frameTimer += deltaTime;
        while (this.frameTimer > Constants.FRAME * 30) {
            this.frame = (this.frame + 1) % 2;
            this.frameTimer -= Constants.FRAME * 30;
        }
        if (this.isLeaving && this.fade.isDone()) {
            this.scene.removeGui(this);
            return false;
        }
        return false;
    }

    @Override
    public void render(SpriteBatch batch, Camera uiCamera) {
        // TODO: Do this more better.
        if ((fade.shouldRender() && !isLeaving) || (!fade.shouldRender() && isLeaving)) {
            TextureUtils.drawWrapped(batch, this.statusBg, 0.0f, -uiCamera.viewportHeight, uiCamera.viewportWidth,
                    uiCamera.viewportHeight);
            this.statusBgPatch.draw(batch, 0.0f, -uiCamera.viewportHeight, uiCamera.viewportWidth,
                    uiCamera.viewportHeight);

            float mid = uiCamera.viewportWidth / 2f;
            float x = mid - 112f;
            // Draw the main pokémon information
            Player p = scene.getLocalPlayer();
            Pokemon[] party = p.getParty();
            if (party[0] == null) {
                this.mainPokemon[0].draw(batch, x, -21f - 49f, 78f, 49f);
            } else {
                this.mainPokemon[selection == 0 ? 2 : 1].draw(batch, x, -21f - 49f, 78f, 49f);
                TextureRegion icon = party[0].getSpecies().defaultForm.iconNormal[frame];
                Vector2 ofs = party[0].getSpecies().defaultForm.iconNormalOffset[frame];

                batch.draw(icon.getTexture(), 4f + x - ofs.x, -41.0f - ofs.y, icon.getRegionX(), icon.getRegionY(),
                        icon.getRegionWidth(), icon.getRegionHeight());

                this.drawHpBar(batch, 9f + x, -60.0f);

                game.getSmallFont().draw(batch, "BULBASAUR", x + 24.0f, -37.0f);
                game.getSmallFont().draw(batch, "\u26008", x + 32.0f, -46.0f);
            }

            x = mid - 24f;
            for (int i = 0; i < 5; i++) {
                float y = 24f * i;
                this.otherPokemon[0].draw(batch, x, -32f - y, 142f, 22f);
            }
        }
        if (!this.fade.isDone()) {
            fade.drawFade(batch, uiCamera.viewportWidth, uiCamera.viewportHeight);
        }
    }

    @Override
    public boolean keyDown(int keyCode) {
        if (!this.fade.isDone() || this.isLeaving) {
            return true;
        }
        if (keyCode == Input.Keys.X) {
            this.fade = new FadeOutIn(20, 20, 20);
            this.isLeaving = true;
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

    private void drawHpBar(SpriteBatch batch, float x, float y) {
        batch.draw(hpStart, x, y);
        batch.draw(hpGreen, 15f + x, y, 48, 7);
        batch.draw(hpEnd, 63f + x, y);
    }
}
