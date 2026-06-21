package com.compileordie.pvz2.controllers.menus.home;

import com.compileordie.pvz2.controllers.AppController;
import com.compileordie.pvz2.views.helpers.Menu;

public class SettingsMenuController {
    public static String exitMenu() {
        return AppController.changeMenu(Menu.MAIN);
    }

    public static  String setDifficulty(String level) {
        // TODO: To be implemented.
        return "[TODO] This command is not implemented yet.";
    }
}
