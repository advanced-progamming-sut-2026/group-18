package com.compileordie.pvz2.views.menus.home;

import com.compileordie.pvz2.controllers.menus.home.SettingsMenuController;
import com.compileordie.pvz2.views.helpers.Command;
import com.compileordie.pvz2.views.helpers.MenuView;

public class SettingsMenuView implements MenuView {
    @Override
    public String handleCommandCore(String command) {
        if (Command.MENU_EXIT.matches(command)) {
            return SettingsMenuController.exitMenu();
        }
        if (Command.CHANGE_DIFFICULTY.matches(command)) {
            String level = Command.CHANGE_DIFFICULTY.getGroup(command, "level");
            return SettingsMenuController.setDifficulty(level);
        }
        return null;
    }
}
