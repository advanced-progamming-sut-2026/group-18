package com.compileordie.pvz2.views.menus.network;

import com.compileordie.pvz2.controllers.menus.network.NetworkMenuController;
import com.compileordie.pvz2.views.helpers.Command;
import com.compileordie.pvz2.views.helpers.MenuView;

public class NetworkMenuView implements MenuView {
    @Override
    public String handleCommandCore(String command) {
        if (Command.MENU_EXIT.matches(command)) {
            return NetworkMenuController.exitMenu();
        }
        return null;
    }
}
