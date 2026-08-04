package com.compileordie.pvz2.views.menus.auth;

import com.compileordie.pvz2.controllers.menus.auth.LoginMenuController;
import com.compileordie.pvz2.views.helpers.Command;
import com.compileordie.pvz2.views.helpers.MenuView;

public class LoginMenuView implements MenuView {
    @Override
    public String handleCommandCore(String command) {
        if (Command.LOGIN.matches(command)) {
            String username = Command.LOGIN.getGroup(command, "username");
            String password = Command.LOGIN.getGroup(command, "password");
            boolean stay = Command.LOGIN.getGroup(command, "stay") != null;
            return LoginMenuController.loginUser(username, password, stay);
        }
        if (Command.FORGET_PASSWORD.matches(command)) {
            String username = Command.FORGET_PASSWORD.getGroup(command, "username");
            String email = Command.FORGET_PASSWORD.getGroup(command, "email");
            return LoginMenuController.forgetPassword(username, email);
        }
        if (Command.ANSWER.matches(command)) {
            String answer = Command.ANSWER.getGroup(command, "answer");
            return LoginMenuController.answerQuestionUser(answer);
        }
        if (LoginMenuController.isWaitingForNewPassword()) {
            return LoginMenuController.setNewPassword(command);
        }
        if (Command.MENU_ENTER.matches(command)) {
            String name = Command.MENU_ENTER.getGroup(command, "name");
            return LoginMenuController.enterMenu(name);
        }
        if (Command.MENU_EXIT.matches(command)) {
            return LoginMenuController.exitMenu();
        }
        return null;
    }
}
