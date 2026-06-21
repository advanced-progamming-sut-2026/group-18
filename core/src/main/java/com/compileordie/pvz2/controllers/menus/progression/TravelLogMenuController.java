package com.compileordie.pvz2.controllers.menus.progression;

import com.compileordie.pvz2.controllers.AppController;
import com.compileordie.pvz2.views.helpers.Menu;

public class TravelLogMenuController {
    public static String exitMenu() {
        return AppController.changeMenu(Menu.GAME);
    }

    public static String showPage(String name) {
        // TODO: To be implemented.
        return "[TODO] This command is not implemented yet.";
    }
}
