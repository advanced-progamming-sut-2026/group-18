package com.compileordie.pvz2.controllers.menus.auth;

import com.compileordie.pvz2.controllers.AppController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.views.helpers.Menu;

public class LoginMenuController {
    public static String enterMenu(String name) {
        Menu menu = Menu.getByName(name);
        if (menu == null) {
            return "[ERROR] Wrong menu name.";
        }
        if (menu != Menu.MAIN) {
            return "[ERROR] You can only enter Main Menu from here.";
        }
        if (!AppModel.hasPlayer()) {
            return "[ERROR] You need to login first.";
        }

        return AppController.changeMenu(menu);
    }

    public static String exitMenu() {
        return AppController.changeMenu(Menu.SIGNUP);
    }

    public static String loginUser(String username, String password, String stay) {
        // TODO: To be implemented.
        return "[TODO] This command is not implemented yet.";
    }

    public static String forgetPassword(String username, String email) {
        // TODO: To be implemented.
        return "[TODO] This command is not implemented yet.";
    }

    public static String answerQuestionUser(String answer) {
        // TODO: To be implemented.
        return "[TODO] This command is not implemented yet.";
    }
}
