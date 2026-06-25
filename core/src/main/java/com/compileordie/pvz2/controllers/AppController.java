package com.compileordie.pvz2.controllers;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.views.helpers.Menu;

import java.util.StringJoiner;

public class AppController {
    private AppController() {
    }

    public static String getBeforePrompt() {
        StringJoiner joiner = new StringJoiner(System.lineSeparator());

        while (AppModel.hasBeforePrompt()) {
            joiner.add(AppModel.getBeforePrompt());
        }

        return joiner.toString();
    }

    public static String getAfterPrompt() {
        StringJoiner joiner = new StringJoiner(System.lineSeparator());

        while (AppModel.hasAfterPrompt()) {
            joiner.add(AppModel.getAfterPrompt());
        }

        return joiner.toString();
    }

    public static String showCurrentMenu() {
        return "You're in the " + AppModel.getMenu().getName() + " menu now.";
    }

    public static String changeMenu(Menu menu) {
        AppModel.setMenu(menu);
        return "Menu changed successfully!" + System.lineSeparator() + showCurrentMenu();
    }
}
