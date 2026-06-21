package com.compileordie.pvz2.models;

import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.views.helpers.Menu;

public class AppModel {
    private static boolean appIsRunning = true;
    private static Menu menu = Menu.SIGNUP;
    private static Player player = null;

    public static boolean isRunning() {
        return appIsRunning;
    }

    public static void stop() {
        appIsRunning = false;
    }

    public static Menu getMenu() {
        return menu;
    }

    public static void setMenu(Menu menu) {
        AppModel.menu = menu;
    }

    public static Player getPlayer() {
        return player;
    }

    public static boolean hasPlayer() {
        return player != null;
    }

    public static void setPlayer(Player player) {
        AppModel.player = player;
    }

    public static void clearPlayer() {
        player = null;
    }
}
