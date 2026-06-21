package com.compileordie.pvz2.views.menus.progression;

import com.compileordie.pvz2.controllers.menus.progression.TravelLogMenuController;
import com.compileordie.pvz2.views.helpers.Command;
import com.compileordie.pvz2.views.helpers.MenuView;

public class TravelLogMenuView implements MenuView {
    @Override
    public String handleCommandCore(String command) {
        if (Command.MENU_EXIT.matches(command)) {
            return TravelLogMenuController.exitMenu();
        }
        if (Command.TRAVEL_LOG_PAGE.matches(command)) {
            String name = Command.TRAVEL_LOG_PAGE.getGroup(command, "name");
            return TravelLogMenuController.showPage(name);
        }
        return null;
    }
}
