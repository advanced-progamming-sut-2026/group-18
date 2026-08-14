package com.compileordie.pvz2.controllers.menus.auth;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.models.user.UserValidator;
import com.compileordie.pvz2.models.user.authentication.AuthManager;

public class ProfileMenuController {
    private ProfileMenuController() {
    }

    public static Result<Void> updateProfile(String newUsername, String newNickname, String newEmail) {
        Player currentUser = AppModel.player;

        Result<Void> result = UserValidator.isUsernameValid(newUsername);
        if (!result.isSuccess) {
            return result;
        }
        result = UserValidator.isUsernameUnique(newUsername);
        if (!result.isSuccess && !currentUser.username.equals(newUsername)) {
            return result;
        }
        result = UserValidator.isNicknameValid(newNickname);
        if (!result.isSuccess) {
            return result;
        }
        result = UserValidator.isEmailValid(newEmail);
        if (!result.isSuccess) {
            return result;
        }

        AuthManager.setNewUsername(newUsername);
        currentUser.nickname = newNickname;
        currentUser.email = newEmail;
        new UserDatabase().save(currentUser);
        return Result.success();
    }

    public static Result<Void> changePassword(String oldPassword, String newPassword) {
        Player currentUser = AppModel.player;

        if (!AuthManager.checkPassword(oldPassword.toCharArray())) {
            return Result.failure("Incorrect password");
        }
        if (oldPassword.equals(newPassword)) {
            return Result.failure("New password cannot be the same as the current password");
        }
        Result<Void> result = UserValidator.isPasswordStrong(newPassword);
        if (!result.isSuccess) {
            return result;
        }

        AuthManager.setNewPassword(currentUser.username, newPassword.toCharArray());
        return Result.success();
    }
}
