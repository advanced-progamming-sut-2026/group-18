package com.compileordie.pvz2.views.menus.auth;

import com.compileordie.pvz2.controllers.menus.auth.ProfileMenuController;
import com.compileordie.pvz2.views.helpers.Command;
import com.compileordie.pvz2.views.helpers.MenuView;

public class ProfileMenuView implements MenuView {
    @Override
    public String handleCommandCore(String command) {
        if (Command.MENU_EXIT.matches(command)) {
            return ProfileMenuController.exitMenu();
        }
        if (Command.CHANGE_USERNAME.matches(command)) {
            String username = Command.CHANGE_USERNAME.getGroup(command, "username");
            return ProfileMenuController.changeUsername(username);
        }
        if (Command.CHANGE_NICKNAME.matches(command)) {
            String nickname = Command.CHANGE_NICKNAME.getGroup(command, "nickname");
            return ProfileMenuController.changeNickname(nickname);
        }
        if (Command.CHANGE_EMAIL.matches(command)) {
            String email = Command.CHANGE_EMAIL.getGroup(command, "email");
            return ProfileMenuController.changeEmail(email);
        }
        if (Command.CHANGE_PASSWORD.matches(command)) {
            String oldPassword = Command.CHANGE_PASSWORD.getGroup(command, "oldPassword");
            String newPassword = Command.CHANGE_PASSWORD.getGroup(command, "newPassword");
            return ProfileMenuController.changePassword(oldPassword, newPassword);
        }
        if (Command.SHOW_INFO.matches(command)) {
            return ProfileMenuController.showPlayerInfo();
        }
        return null;
    }
}
