package com.compileordie.pvz2.controllers.menus.auth;

import com.compileordie.pvz2.config.PreferencesManager;
import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.models.user.UserValidator;
import com.compileordie.pvz2.models.user.authentication.AuthManager;

public class LoginMenuController {
    private LoginMenuController() {
    }

    public static Result<Void> loginUser(String username, String password, boolean stay) {
        switch (AuthManager.authenticate(username, password.toCharArray())) {
            case USER_NOT_FOUND -> {
                return Result.failure("User not found");
            }
            case WRONG_PASSWORD -> {
                return Result.failure("Wrong password");
            }
        }

        if (stay) {
            PreferencesManager.setDefaultUser(username);
        } else {
            PreferencesManager.clearDefaultUser();
        }

        AuthManager.loginPlayer(username);
        return Result.success();
    }

    public static Result<String> fetchSecurityQuestion(String username, String email) {
        Player user = AuthManager.getUserByUsername(username);
        if (user == null) {
            return Result.failure("User not found");
        }
        if (!user.email.equals(email)) {
            return Result.failure("Email does not match");
        }

        return Result.success(user.securityQuestion);
    }

    public static Result<Void> resetPassword(String username, String email, String securityAnswer, String newPassword) {
        Player user = AuthManager.getUserByUsername(username);
        if (user == null) {
            return Result.failure("User not found");
        }
        if (!user.email.equals(email)) {
            return Result.failure("Email does not match");
        }
        if (!user.isSecurityAnswerCorrect(securityAnswer)) {
            return Result.failure("Incorrect security answer");
        }

        Result<Void> result = UserValidator.isPasswordStrong(newPassword);
        if (!result.isSuccess) {
            return result;
        }

        AuthManager.setNewPassword(username, newPassword.toCharArray());
        return Result.success();
    }
}
