package com.compileordie.pvz2.controllers.menus.progression;

import com.compileordie.pvz2.controllers.AppController;
import com.compileordie.pvz2.views.helpers.Menu;

public class GreenhouseMenuController {
    public static String exitMenu() {
        return AppController.changeMenu(Menu.GAME);
    }

    public static String showGreenhouse() {
        // TODO: To be implemented.
        return "[TODO] This command is not implemented yet.";
    }

    public static String plantPlot(String x, String y) {
        // TODO: To be implemented.
        return "[TODO] This command is not implemented yet.";
    }

    public static String collectPot(String x, String y) {
        // TODO: To be implemented.
        return "[TODO] This command is not implemented yet.";
    }

    public static String growPot(String x, String y) {
        // TODO: To be implemented.
        return "[TODO] This command is not implemented yet.";
    }

    public static String enterShop() {
        return AppController.changeMenu(Menu.SHOP);
    }
}
