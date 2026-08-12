package com.compileordie.pvz2.views.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.compileordie.pvz2.config.Constants;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.PvzSkin;

abstract public class MenuScreen implements Screen {
    protected final Stage stage;
    protected final Skin skin;
    protected TextureBank textureBank;

    public MenuScreen() {
        ScreenViewport viewport = new ScreenViewport();
        viewport.setUnitsPerPixel(1f / Constants.UI.UPP);
        this.stage = new Stage(viewport);
        this.skin = PvzSkin.get();
        this.textureBank = new TextureBank("768", Gdx.files.internal("pvz-assets"));
    }

    abstract public void showCore();

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        stage.clear();
        showCore();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(240 / 255f, 230 / 255f, 195 / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Step and render the Scene2D UI actors overlaid on top of the background
        textureBank.update();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if (width <= 0 || height <= 0) return;
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
        textureBank.dispose();
    }
}
