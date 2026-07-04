package com.compileordie.pvz2;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.compileordie.pvz2.config.PreferencesManager;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;
import com.compileordie.pvz2.models.user.authentication.AuthManager;
import com.compileordie.pvz2.views.AppView;
import com.compileordie.pvz2.views.helpers.Menu;

public class Main {
    static void main(String[] args) {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();

        new HeadlessApplication(new ApplicationAdapter() {
            @Override
            public void create() {
                // Here Gdx.files is successfully initialized.
                ConfigManager.init();

                // Auto-login logic
                String savedField = PreferencesManager.getDefaultUserField();
                String username = AuthManager.getUsernameByField(savedField);

                if (username != null) {
                    AuthManager.loginPlayer(username);
                    AppModel.menu = Menu.MAIN;
                    System.out.println("Auto-logged in as '" + username + "'.");
                }

                // Note: Running a blocking Scanner loop here will block the LibGDX
                // main thread, but for a pure CLI phase, this is generally acceptable.
                AppView.run();
            }
        }, config);
    }
}
