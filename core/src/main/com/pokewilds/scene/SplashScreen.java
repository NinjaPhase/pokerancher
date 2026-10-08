package com.pokewilds.scene;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.pokewilds.PokeWilds;
import com.pokewilds.world.tile.TileRegistry;

public class SplashScreen extends Scene {
    private static final float STEP_TIMER = 0.05f;

    private final PokeWilds game;
    private float animationTimer;
    private float grayScale;

    public SplashScreen(final PokeWilds game) {
        this.game = game;
        this.game.getFontManager().loadDefaults();
        TileRegistry.loadDefaults();
    }

    @Override
    public void update(final float deltaTime) {
        animationTimer += deltaTime;
        while (animationTimer >= STEP_TIMER) {
            grayScale += 0.1f;
            animationTimer -= STEP_TIMER;
        }
        if (MathUtils.isEqual(1.1f, grayScale)) {
            game.setScreen(new TitleScreen(game));
        }
    }

    @Override
    public void render() {
        ScreenUtils.clear(grayScale, grayScale, grayScale, 1);
    }

    @Override
    public void dispose() {

    }

}
