package com.pokewilds.scene.battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.pokewilds.Constants;
import com.pokewilds.PokeWilds;
import com.pokewilds.pokemon.Species;
import com.pokewilds.scene.GameScene;
import com.pokewilds.scene.Scene;

public class BattleView extends Scene {

    private final GameScene gameScene;
    private final TextureAtlas battleAtlas;
    private final SpriteBatch batch;
    private final Camera camera;
    private Vector2 opponent1BasePlate, player1BasePlate;
    private BattleController battle;
    private float animTimer = 0f;
    private Species player1Pokemon, opponent1Pokemon;

    public BattleView(GameScene gameScene, SpriteBatch batch) {
        this.gameScene = gameScene;
        this.batch = batch;
        this.battleAtlas = PokeWilds.getInstance().getTextureAtlas("battle.atlas");
        float scale = Constants.getUIScale();
        this.camera = new OrthographicCamera(Math.round(Gdx.graphics.getWidth() / scale), Math.round(Gdx.graphics.getHeight() / scale));
        this.camera.position.x = Math.floorDiv((int) this.camera.viewportWidth, 2);
        this.camera.position.y = -Math.floorDiv((int) this.camera.viewportHeight, 2);
        this.camera.update();

        this.opponent1BasePlate = new Vector2(this.camera.viewportWidth, 0.0f);
        this.player1BasePlate = new Vector2(0.0f - battleAtlas.findRegion("grass_panel").getRegionWidth(), 0.0f);
        this.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void update(float deltaTime) {
        this.animTimer = Math.min(this.animTimer + deltaTime, 1.0f);
        this.camera.update();
    }

    @Override
    public void render() {
        batch.setProjectionMatrix(this.camera.combined);
        TextureAtlas.AtlasRegion bg = battleAtlas.findRegion("grass_bg");
        TextureRegion top = new TextureRegion(bg, 0, 0, 4, 17);
        TextureRegion bottom = new TextureRegion(bg, 0, 17, 4, 95);
        for (int x = 0; x < camera.viewportWidth / bg.getRegionWidth(); x++) {
            batch.draw(top, x * bg.getRegionWidth(), -top.getRegionHeight());
            for (int y = 1; y <= (camera.viewportHeight / bg.getRegionHeight()) + 1; y++) {
                batch.draw(bottom, x * bottom.getRegionWidth(), -(y * bottom.getRegionHeight()) - top.getRegionHeight());
            }
        }

        TextureAtlas.AtlasRegion panel = battleAtlas.findRegion("grass_panel");
        this.player1BasePlate.x = MathUtils.lerp(-panel.getRegionWidth(), 0.0f, this.animTimer);
        this.player1BasePlate.y = -panel.getRegionHeight() - 100.0f;
        this.opponent1BasePlate.x = MathUtils.lerp(this.camera.viewportWidth, this.camera.viewportWidth - panel.getRegionWidth(), this.animTimer);
        this.opponent1BasePlate.y = -panel.getRegionHeight() - 40.0f;
        batch.draw(panel, this.player1BasePlate.x, this.player1BasePlate.y);
        batch.draw(panel, this.opponent1BasePlate.x, this.opponent1BasePlate.y);
    }


    @Override
    public void resize(int width, int height) {
        float uiScale = Constants.getUIScale(height);
        this.camera.viewportWidth = (float) Math.round(width / uiScale);
        this.camera.viewportHeight = (float) Math.round(height / uiScale);
        this.camera.position.x = Math.floorDiv((int) this.camera.viewportWidth, 2);
        this.camera.position.y = -Math.floorDiv((int) this.camera.viewportHeight, 2);
        this.camera.update();
    }

    @Override
    public void dispose() {
        PokeWilds.getTextureManager().dropAtlas("battle.atlas");
    }

}
