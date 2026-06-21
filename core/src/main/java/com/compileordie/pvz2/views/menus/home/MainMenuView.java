package com.compileordie.pvz2.views.menus.home;

import com.compileordie.pvz2.controllers.menus.home.MainMenuController;
import com.compileordie.pvz2.views.helpers.Command;
import com.compileordie.pvz2.views.helpers.MenuView;

public class MainMenuView implements MenuView {
    @Override
    public String handleCommandCore(String command) {
        if (Command.MENU_ENTER.matches(command)) {
            String name = Command.MENU_ENTER.getGroup(command, "name");
            return MainMenuController.enterMenu(name);
        }
        if (Command.MENU_EXIT.matches(command)) {
            return MainMenuController.exitMenu();
        }
        if (Command.LOGOUT.matches(command)) {
            return MainMenuController.logoutUser();
        }
        return null;
    }
}
