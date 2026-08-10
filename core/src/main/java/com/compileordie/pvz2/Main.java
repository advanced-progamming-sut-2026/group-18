package com.compileordie.pvz2;

import com.badlogic.gdx.Game;
import com.compileordie.pvz2.config.PreferencesManager;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;
import com.compileordie.pvz2.models.user.authentication.AuthManager;
import com.compileordie.pvz2.views.ScreenManager;
import com.compileordie.pvz2.views.screens.MainMenuScreen;
import com.compileordie.pvz2.views.screens.SignupMenuScreen;

public class Main extends Game {
    @Override
    public void create() {
        ConfigManager.init();
        ScreenManager.init(this);

        // Auto-login logic
        String savedField = PreferencesManager.getDefaultUserField();
        String username = AuthManager.getUsernameByField(savedField);
        if (username != null) {
            AuthManager.loginPlayer(username);
            setScreen(new MainMenuScreen());
            System.out.println("Auto-logged in as '" + username + "'.");
        } else {
            setScreen(new SignupMenuScreen());
        }
    }
}
