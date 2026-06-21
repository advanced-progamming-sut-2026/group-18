package com.compileordie.pvz2.controllers.menus.progression;

import com.compileordie.pvz2.controllers.AppController;
import com.compileordie.pvz2.views.helpers.Menu;

public class ShopMenuController {
    public static String exitMenu() {
        return AppController.changeMenu(Menu.COLLECTION);
    }

    public static String showList() {
        // TODO: To be implemented.
        return "[TODO] This command is not implemented yet.";
    }

    public static String showDaily() {
        // TODO: To be implemented.
        return "[TODO] This command is not implemented yet.";
    }

    public static String buy(String id, String count, String type) {
        // TODO: To be implemented.
        return "[TODO] This command is not implemented yet.";
    }
}
