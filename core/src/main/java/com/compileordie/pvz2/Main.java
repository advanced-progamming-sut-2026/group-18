package com.compileordie.pvz2;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.compileordie.pvz2.config.PreferencesManager;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;
import com.compileordie.pvz2.models.user.authentication.AuthManager;
import com.compileordie.pvz2.views.ScreenManager;
import com.compileordie.pvz2.views.ScreenType;
import com.compileordie.pvz2.views.helpers.ToastManager;

public class Main extends Game {
    @Override
    public void create() {
        ConfigManager.init();
        ScreenManager.init(this);
        ToastManager.init();

        // Auto-login logic
        String savedField = PreferencesManager.getDefaultUserField();
        String username = AuthManager.getUsernameByField(savedField);
        if (username != null) {
            AuthManager.loginPlayer(username);
            ScreenManager.setMenuScreen(ScreenType.MAIN);
            System.out.println("Auto-logged in as '" + username + "'.");
        } else {
            ScreenManager.setMenuScreen(ScreenType.SIGNUP);
        }
    }

    // Override render to draw the active screen first, then the toasts
    @Override
    public void render() {
        super.render();
        ToastManager.render(Gdx.graphics.getDeltaTime());
    }

    // Override resize to ensure both the active screen and the toast overlay scale properly
    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        ToastManager.resize(width, height);
    }
}
