package com.compileordie.pvz2.controllers.menus.home;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.models.user.Player;

public class SettingsMenuController {
    private SettingsMenuController() {
    }

    public static void saveSettings(int difficulty, int speed, boolean gridBox, boolean debug) {
        Player player = AppModel.player;
        player.difficultyLevel = difficulty;
        player.gameSpeedCoefficient = speed;
        player.showGridBox = gridBox;
        player.debugMode = debug;
        new UserDatabase(player.username).save(player);
    }
}
