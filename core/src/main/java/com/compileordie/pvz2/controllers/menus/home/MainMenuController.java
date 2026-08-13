package com.compileordie.pvz2.controllers.menus.home;

import com.compileordie.pvz2.config.PreferencesManager;
import com.compileordie.pvz2.models.AppModel;

public class MainMenuController {
    private MainMenuController() {
    }

    public static void logoutUser() {
        AppModel.clearPlayer();
        PreferencesManager.clearDefaultUser();
    }
}
