package com.compileordie.pvz2.controllers.menus.game;

import com.compileordie.pvz2.controllers.AppController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.game.SessionBuilder;
import com.compileordie.pvz2.models.game.levels.ChapterType;
import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.views.helpers.Menu;

public class GameMenuController {
    private GameMenuController() {
    }

    public static String enterMenu(String name) {
        Menu menu = Menu.getByName(name);
        if (menu == null) {
            return "[ERROR] Wrong menu name.";
        }
        if (menu != Menu.COLLECTION) {
            return "[ERROR] You can only enter Collection Menu from here.";
        }

        return AppController.changeMenu(menu);
    }

    public static String exitMenu() {
        return AppController.changeMenu(Menu.MAIN);
    }

    public static String enterChapter(String name) {
        ChapterType chapter = ChapterType.getByName(name);
        if (chapter == null) {
            return "[ERROR] Wrong chapter name.";
        }
        if (!AppModel.player.getUnlockedChapters().contains(chapter)) {
            return "[ERROR] You have not unlocked this chapter yet.";
        }

        AppModel.currentChapter = chapter;
        return "Successfully entered chapter " + chapter + ".";
    }

    public static String enterLevel(String name) {
        LevelID level = LevelID.getByName(name);
        if (level == null) {
            return "[ERROR] Wrong level name.";
        }
        if (level.chapterType != ChapterType.MINIGAME) {
            if (AppModel.currentChapter == null) {
                return "[ERROR] You must select a chapter first.";
            }
            if (level.chapterType != AppModel.currentChapter) {
                return "[ERROR] The level selected doesn't belong to this chapter and is not a minigame."
                    + System.lineSeparator() + "Current chapter: " + level.chapterType;
            }
            if (!AppModel.player.getUnlockedLevels().contains(level)) {
                return "[ERROR] You have not unlocked this level yet.";
            }
        }

        AppModel.currentLevel = level;
        if (level.needsPlantSelection()) {
            return "Starting level: "
                + level + System.lineSeparator() + AppController.changeMenu(Menu.PLANT_SELECTION);
        } else {
            AppModel.gameSession = SessionBuilder.create(level);
            return "Starting level: "
                + level + System.lineSeparator() + AppController.changeMenu(Menu.GAME_SESSION);
        }
    }

    public static String enterGreenhouse() {
        return AppController.changeMenu(Menu.GREENHOUSE);
    }

    public static String enterTravelLog() {
        return AppController.changeMenu(Menu.TRAVEL_LOG);
    }

    public static String enterLeaderboard() {
        return AppController.changeMenu(Menu.LEADERBOARD);
    }

    public static String showCoinWallet() {
        int coins = AppModel.player.coins;
        return "You have " + coins + " coin" + (coins == 1 ? "." : "s.");
    }

    public static String showGemWallet() {
        int diamonds = AppModel.player.diamonds;
        return "You have " + diamonds + " diamond" + (diamonds == 1 ? "." : "s.");
    }

    public static String cheatAdd(String count, String type) {
        int number;
        try {
            number = Integer.parseInt(count);
        } catch (NumberFormatException e) {
            return "[ERROR] You must enter a whole number.";
        }
        if (number <= 0) {
            return "[ERROR] You must enter a positive number.";
        }

        if (type.equalsIgnoreCase("coin")) {
            AppModel.player.coins += number;
            return "Successfully Added " + number + " coin" + (number == 1 ? "." : "s.");
        } else if (type.equalsIgnoreCase("diamond")) {
            AppModel.player.diamonds += number;
            return "Successfully Added " + number + " diamond" + (number == 1 ? "." : "s.");
        } else {
            return "[ERROR] You can either add 'coin's or 'diamond's";
        }
    }
}
