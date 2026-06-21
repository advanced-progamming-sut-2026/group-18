package com.compileordie.pvz2.controllers.menus.game;

import com.compileordie.pvz2.controllers.AppController;
import com.compileordie.pvz2.views.helpers.Menu;

public class GameMenuController {
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
        // TODO: To be implemented.
        return "[TODO] This command is not implemented yet.";
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
        // TODO: To be implemented.
        return "[TODO] This command is not implemented yet.";
    }

    public static String showGemWallet() {
        // TODO: To be implemented.
        return "[TODO] This command is not implemented yet.";
    }

    public static String cheatAdd(String count, String type) {
        // TODO: To be implemented.
        return "[TODO] This command is not implemented yet.";
    }
}
