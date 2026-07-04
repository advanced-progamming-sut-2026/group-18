package com.compileordie.pvz2.controllers.menus.auth;

import com.compileordie.pvz2.config.PreferencesManager;
import com.compileordie.pvz2.controllers.AppController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.models.user.authentication.AuthManager;
import com.compileordie.pvz2.views.helpers.Menu;

public class LoginMenuController {
    private static PendingChange pendingChange = null;

    private LoginMenuController() {
    }

    public static String loginUser(String username, String password, boolean stay) {
        switch (AuthManager.authenticateStepOne(username, password.toCharArray())) {
            case USER_NOT_FOUND -> {
                return "[ERROR] User not found.";
            }
            case WRONG_PASSWORD -> {
                return "[ERROR] Wrong password.";
            }
        }

        if (stay) {
            PreferencesManager.setDefaultUser(username);
        } else {
            PreferencesManager.clearDefaultUser();
        }

        AuthManager.loginPlayer(username);
        return "Logged in as '" + username + "' successfully! Now you can change to Main menu.";
    }

    public static String enterMenu(String name) {
        Menu menu = Menu.getByName(name);
        if (menu == null) {
            return "[ERROR] Wrong menu name.";
        }
        if (menu != Menu.MAIN) {
            return "[ERROR] You can only enter Main Menu from here.";
        }
        if (AppModel.isLoggedOut()) {
            return "[ERROR] You need to login first.";
        }

        return AppController.changeMenu(menu);
    }

    public static String exitMenu() {
        return AppController.changeMenu(Menu.SIGNUP);
    }

    public static String forgetPassword(String username, String email) {
        Player user = AuthManager.getUserByUsername(username);
        if (!user.username.equals(username)) {
            return "[ERROR] User not found, try another username.";
        }
        if (!user.email.equals(email)) {
            return "[ERROR] Email does not match.";
        }

        pendingChange = new PendingChange(user, false);
        AppModel.addAfterPrompt(user.securityQuestion);
        AppModel.addAfterPrompt("Use the command bellow: (answer is case-insensitive)");
        AppModel.addAfterPrompt("answer -a <answer>");
        return "Now answer the following security.";
    }

    public static String answerQuestionUser(String answer) {
        if (pendingChange == null) {
            return "[ERROR] You must complete the 'forgot password' command first!";
        }
        if (pendingChange.answered()) {
            return "[ERROR] You have already answered the security question! Type in your new password:";
        }
        if (!pendingChange.user.isSecurityAnswerCorrect(answer)) {
            return "[ERROR] Answer is wrong, please try again.";
        }

        pendingChange = new PendingChange(pendingChange.user, true);
        return "Answer was correct. Now enter your new password:";
    }

    public static boolean isWaitingForNewPassword() {
        return pendingChange != null && pendingChange.answered();
    }

    public static String setNewPassword(String newPassword) {
        String username = pendingChange.user.username;
        AuthManager.setNewPassword(username, newPassword.toCharArray());
        return "Password successfully changed for user  '" + username + "'!";
    }

    private record PendingChange(
        Player user,
        boolean answered
    ) {
    }
}
