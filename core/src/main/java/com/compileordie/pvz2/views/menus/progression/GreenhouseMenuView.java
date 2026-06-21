package com.compileordie.pvz2.views.menus.progression;

import com.compileordie.pvz2.controllers.menus.progression.GreenhouseMenuController;
import com.compileordie.pvz2.views.helpers.Command;
import com.compileordie.pvz2.views.helpers.MenuView;

public class GreenhouseMenuView implements MenuView {
    @Override
    public String handleCommandCore(String command) {
        if (Command.MENU_EXIT.matches(command)) {
            return GreenhouseMenuController.exitMenu();
        }
        if (Command.SHOW_GREENHOUSE.matches(command)) {
            return GreenhouseMenuController.showGreenhouse();
        }
        if (Command.GREENHOUSE_PLANT_POT.matches(command)) {
            String x = Command.GREENHOUSE_PLANT_POT.getGroup(command, "x");
            String y = Command.GREENHOUSE_PLANT_POT.getGroup(command, "y");
            return GreenhouseMenuController.plantPlot(x, y);
        }
        if (Command.GREENHOUSE_COLLECT.matches(command)) {
            String x = Command.GREENHOUSE_COLLECT.getGroup(command, "x");
            String y = Command.GREENHOUSE_COLLECT.getGroup(command, "y");
            return GreenhouseMenuController.collectPot(x, y);
        }
        if (Command.GREENHOUSE_GROW.matches(command)) {
            String x = Command.GREENHOUSE_GROW.getGroup(command, "x");
            String y = Command.GREENHOUSE_GROW.getGroup(command, "y");
            return GreenhouseMenuController.growPot(x, y);
        }
        if (Command.GREENHOUSE_ENTER_SHOP.matches(command)) {
            return GreenhouseMenuController.enterShop();
        }
        return null;
    }
}
