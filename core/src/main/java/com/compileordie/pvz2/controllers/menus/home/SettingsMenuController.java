package com.compileordie.pvz2.controllers.menus.home;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.models.user.Player;

public class SettingsMenuController {
    private SettingsMenuController() {
    }

    public static String setDifficulty(String level) {
        int difficulty;
        try {
            difficulty = Integer.parseInt(level);
        } catch (NumberFormatException e) {
            return "[ERROR] Difficulty must be an integer.";
        }

        if (difficulty < 1 || difficulty > 5) {
            return "[ERROR] Difficulty level must be a value between 1 and 5.";
        }

        Player player = AppModel.player;
        player.difficultyLevel = difficulty;
        new UserDatabase(player.username).save(player);

        return "Difficulty level successfully set to " + difficulty + ".";
    }
}
