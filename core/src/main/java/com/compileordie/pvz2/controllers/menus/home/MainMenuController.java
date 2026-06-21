package com.compileordie.pvz2.controllers.menus.home;

import com.compileordie.pvz2.controllers.AppController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.views.helpers.Menu;

import java.util.List;

public class MainMenuController {
    public static String enterMenu(String name) {
        Menu menu = Menu.getByName(name);
        if (menu == null) {
            return "[ERROR] Wrong menu name.";
        }
        if (!List.of(Menu.GAME, Menu.SETTINGS, Menu.NETWORK, Menu.NEWS, Menu.PROFILE).contains(menu)) {
            return "[ERROR] You can only enter Game, Settings, Network, News or Profile Menu from here.";
        }

        return AppController.changeMenu(menu);
    }

    public static String exitMenu(){
        return "[ERROR] Use the 'menu logout' command instead.";
    }

    public static String logoutUser() {
        AppModel.clearPlayer();
        return AppController.changeMenu(Menu.SIGNUP);
    }
}
