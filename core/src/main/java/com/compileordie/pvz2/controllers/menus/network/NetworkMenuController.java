package com.compileordie.pvz2.controllers.menus.network;

import com.compileordie.pvz2.controllers.AppController;
import com.compileordie.pvz2.views.helpers.Menu;

public class NetworkMenuController {
    private NetworkMenuController() {
    }

    public static String exitMenu() {
        return AppController.changeMenu(Menu.MAIN);
    }
}
