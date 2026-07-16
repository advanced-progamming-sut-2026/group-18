package com.compileordie.pvz2.views.helpers;

import com.compileordie.pvz2.controllers.AppController;
import com.compileordie.pvz2.models.AppModel;

public interface MenuView {
    String handleCommandCore(String command);

    default String handleCommand(String command) {
        if (Command.MENU_SHOW_CURRENT.matches(command)) {
            return AppController.showCurrentMenu();
        }
        String menuResult = handleCommandCore(command);
        if (menuResult != null) {
            return menuResult;
        } else {
            return "Command is either invalid or does not belong to " + AppModel.menu + " menu";
        }
    }
}
