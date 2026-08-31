package com.compileordie.pvz2;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.compileordie.pvz2.config.PreferencesManager;
import com.compileordie.pvz2.controllers.menus.auth.LoginMenuController;
import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;
import com.compileordie.pvz2.views.ScreenManager;
import com.compileordie.pvz2.views.ScreenType;
import com.compileordie.pvz2.views.helpers.ToastManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends Game {
    private SpriteBatch transitionBatch;
    private TextureRegion transitionRegion;
    private float transitionTimer = 0f;
    private boolean skipNextTransitionDelta = false;

    @Override
    public void create() {
        ConfigManager.init();
        ScreenManager.init(this);
        ToastManager.init();

        // Auto-login logic ("Stay logged in"): the account now lives on the server, so a real
        // device-only file lookup can no longer authenticate anyone. Instead we replay a normal
        // login against the server using the credentials the user asked us to remember locally.
        String savedUsername = PreferencesManager.getRememberedUsername();
        String savedPassword = PreferencesManager.getRememberedPassword();
        if (savedUsername != null && savedPassword != null) {
            Result<Void> result = LoginMenuController.loginUser(savedUsername, savedPassword, true);
            if (result.isSuccess) {
                ScreenManager.setMenuScreen(ScreenType.MAIN);
                System.out.println("Auto-logged in as '" + savedUsername + "'.");
            } else {
                // Remembered credentials no longer work (password changed elsewhere, account
                // gone, server unreachable, etc.) - don't keep retrying forever, just forget them.
                PreferencesManager.clearDefaultUser();
                ScreenManager.setMenuScreen(ScreenType.SIGNUP);
            }
        } else {
            ScreenManager.setMenuScreen(ScreenType.SIGNUP);
        }
    }

    // Override render to draw the active screen first, then the toasts
    @Override
    public void render() {
        super.render();

        // Crossfade overlay: Fades previous screen snapshot out over the live next screen
        if (transitionRegion != null) {
            if (skipNextTransitionDelta) {
                // First frame after screen switch contains the heavy asset-load hitch; ignore its delta
                skipNextTransitionDelta = false;
            } else {
                // Clamp delta to ~30 FPS minimum step to prevent frame drops from eating the transition
                float delta = Math.min(Gdx.graphics.getDeltaTime(), 0.033f);
                transitionTimer += delta;
            }

            float transitionDuration = 0.5f;
            float progress = transitionTimer / transitionDuration;

            if (progress >= 1f) {
                transitionRegion.getTexture().dispose();
                transitionRegion = null;
            } else {
                float alpha = 1f - Interpolation.fade.apply(progress);
                if (transitionBatch == null) {
                    transitionBatch = new SpriteBatch();
                }
                transitionBatch.getProjectionMatrix().setToOrtho2D(
                    0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()
                );
                transitionBatch.begin();
                transitionBatch.setColor(1f, 1f, 1f, alpha);
                transitionBatch.draw(transitionRegion, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                transitionBatch.end();
            }
        }

        ToastManager.render(Gdx.graphics.getDeltaTime());
    }

    // Override resize to ensure both the active screen and the toast overlay scale properly
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        ToastManager.resize(width, height);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (transitionBatch != null) transitionBatch.dispose();
        if (transitionRegion != null) transitionRegion.getTexture().dispose();
    }

    public void setScreenWithTransition(Screen nextScreen) {
        if (getScreen() != null && Gdx.graphics.getWidth() > 0 && Gdx.graphics.getHeight() > 0) {
            if (transitionRegion != null) {
                transitionRegion.getTexture().dispose();
                transitionRegion = null;
            }
            try {
                // ScreenUtils already captures the buffer with correct orientation
                transitionRegion = ScreenUtils.getFrameBufferTexture(
                    0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight()
                );
                transitionTimer = 0f;
                skipNextTransitionDelta = true; // Prevents loading hitch from eating the fade timer
            } catch (Throwable ignored) {
                transitionRegion = null;
            }
        }
        setScreen(nextScreen);
    }
}
