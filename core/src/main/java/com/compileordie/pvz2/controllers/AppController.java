package com.compileordie.pvz2.controllers;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.views.helpers.Menu;

public class AppController {
    public static String getBeforePrompt() {
        // Can handle app messages before getting specific inputs (using a shared list of Strings)
        return null;
    }

    public static String getAfterPrompt() {
        // Can handle app messages before getting specific inputs (using a shared list of Strings)
        return null;
    }

    public static String showCurrentMenu() {
        return "You're in the " + AppModel.getMenu().getName() + " menu now.";
    }

    public static String changeMenu(Menu menu) {
        AppModel.setMenu(menu);
        return "Menu changed successfully!" + System.lineSeparator() + showCurrentMenu();
    }
}
