package com.compileordie.pvz2.views.menus.auth;

import com.compileordie.pvz2.controllers.menus.auth.SignupMenuController;
import com.compileordie.pvz2.views.helpers.Command;
import com.compileordie.pvz2.views.helpers.MenuView;

public class SignupMenuView implements MenuView {
    @Override
    public String handleCommandCore(String command) {
        if (Command.MENU_ENTER.matches(command)) {
            String name = Command.MENU_ENTER.getGroup(command, "name");
            return SignupMenuController.enterMenu(name);
        }
        if (Command.MENU_EXIT.matches(command)) {
            return SignupMenuController.exitMenu();
        }
        if (Command.REGISTER.matches(command)) {
            String username = Command.REGISTER.getGroup(command, "username");
            String password = Command.REGISTER.getGroup(command, "password");
            String passwordConfirm = Command.REGISTER.getGroup(command, "passwordConfirm");
            String nickname = Command.REGISTER.getGroup(command, "nickname");
            String email = Command.REGISTER.getGroup(command, "email");
            String gender = Command.REGISTER.getGroup(command, "gender");
            return SignupMenuController.registerUser(username, password, passwordConfirm, nickname, email, gender);
        }
        if (Command.PICK_QUESTION.matches(command)) {
            String number = Command.PICK_QUESTION.getGroup(command, "number");
            String answer = Command.PICK_QUESTION.getGroup(command, "answer");
            String answerConfirm = Command.PICK_QUESTION.getGroup(command, "answerConfirm");
            return SignupMenuController.pickQuestionUser(number, answer, answerConfirm);
        }
        return null;
    }
}
