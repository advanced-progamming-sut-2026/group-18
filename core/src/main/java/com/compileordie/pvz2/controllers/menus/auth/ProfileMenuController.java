package com.compileordie.pvz2.controllers.menus.auth;

import com.compileordie.pvz2.controllers.AppController;
import com.compileordie.pvz2.views.helpers.Menu;

public class ProfileMenuController {
    public static String exitMenu(){
        return AppController.changeMenu(Menu.MAIN);
    }

    public static String changeUsername(String newUsername) {
        // TODO: To be implemented.
        return "[TODO] This command is not implemented yet.";
    }

    public static String changeNickname(String newNickname) {
        // TODO: To be implemented.
        return "[TODO] This command is not implemented yet.";
    }

    public static String changeEmail(String newEmail) {
        // TODO: To be implemented.
        return "[TODO] This command is not implemented yet.";
    }

    public static String changePassword(String oldPassword, String newPassword) {
        // TODO: To be implemented.
        return "[TODO] This command is not implemented yet.";
    }

    public static String showPlayerInfo() {
        // TODO: To be implemented.
        return "[TODO] This command is not implemented yet.";
    }
}
