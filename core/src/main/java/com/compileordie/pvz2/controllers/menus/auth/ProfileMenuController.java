package com.compileordie.pvz2.controllers.menus.auth;

import com.compileordie.pvz2.controllers.AppController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.repositories.databases.AuthDatabase;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.models.user.UserValidator;
import com.compileordie.pvz2.models.user.authentication.AuthManager;
import com.compileordie.pvz2.models.user.authentication.AuthStatus;
import com.compileordie.pvz2.models.user.authentication.UserRegistry;
import com.compileordie.pvz2.views.helpers.Menu;

import java.util.ArrayList;

public class ProfileMenuController {
    private ProfileMenuController() {
    }

    public static String exitMenu() {
        return AppController.changeMenu(Menu.MAIN);
    }

    public static String changeUsername(String newUsername) {
        Player player = AppModel.player;
        if (newUsername.equals(player.username)) {
            return "[ERROR] New username cannot be the same as the current username.";
        }
        if (!UserValidator.isUsernameValid(newUsername)) {
            return "[ERROR] Invalid username format.";
        }
        AuthDatabase authDb = new AuthDatabase();
        if (authDb.loadOne(newUsername) != null) {
            return "[ERROR] This username is already taken.";
        }

        String oldUsername = player.username;
        player.username = newUsername;

        // Update the AuthRegistry to map the new username to the existing save file UUID
        ArrayList<UserRegistry> registries = authDb.load();
        for (UserRegistry userRegistry : registries) {
            if (userRegistry.getUsername().equals(oldUsername)) {
                userRegistry.setUsername(newUsername);
                break;
            }
        }
        authDb.save(registries);

        // Save the updated player object
        new UserDatabase(newUsername).save(player);

        return "Username changed successfully.";
    }

    public static String changeNickname(String newNickname) {
        Player player = AppModel.player;
        if (newNickname.equals(player.nickname)) {
            return "[ERROR] New nickname cannot be the same as the current nickname.";
        }
        if (!UserValidator.isNicknameValid(newNickname)) {
            return "[ERROR] Invalid nickname format. Must be 3 to 30 characters.";
        }

        player.nickname = newNickname;
        new UserDatabase(player.username).save(player);

        return "Nickname changed successfully.";
    }

    public static String changeEmail(String newEmail) {
        Player player = AppModel.player;
        if (newEmail.equals(player.email)) {
            return "[ERROR] New email cannot be the same as the current email.";
        }
        if (!UserValidator.isEmailValid(newEmail)) {
            return "[ERROR] Invalid email format.";
        }

        player.email = newEmail;
        new UserDatabase(player.username).save(player);

        return "Email changed successfully.";
    }

    public static String changePassword(String oldPassword, String newPassword) {
        Player player = AppModel.player;
        if (oldPassword.equals(newPassword)) {
            return "[ERROR] New password cannot be the same as the old password.";
        }
        AuthStatus status = AuthManager.authenticateStepOne(player.username, oldPassword.toCharArray());
        if (status != AuthStatus.SUCCESS) {
            return "[ERROR] Old password is incorrect.";
        }
        String passwordStrength = UserValidator.isPasswordStrong(newPassword);
        if (passwordStrength != null) {
            return passwordStrength;
        }

        String newHash = AuthManager.hashPassword(newPassword.toCharArray());

        AuthDatabase authDb = new AuthDatabase();
        ArrayList<UserRegistry> registries = authDb.load();
        for (UserRegistry reg : registries) {
            if (reg.getUsername().equals(player.username)) {
                reg.setPasswordHash(newHash);
                break;
            }
        }
        authDb.save(registries);

        player.passwordHash = newHash;
        new UserDatabase(player.username).save(player);

        return "Password changed successfully.";
    }

    public static String showPlayerInfo() {
        Player player = AppModel.player;
        int levelsPassed = (player.unlockedLevels != null) ? player.unlockedLevels.size() : 0;

        // Note: Replace coin/gem variables with actual economy variables if stored elsewhere
        // (e.g., if Player.java is updated with `coins` and `gems` fields in the future).
        int coinsEarned = 0;
        int gemsEarned = 0;

        return "Username: " + player.username + System.lineSeparator() +
            "Nickname: " + player.nickname + System.lineSeparator() +
            "Games Played: " + player.completedMiniGames + System.lineSeparator() +
            "Coins Earned: " + coinsEarned + System.lineSeparator() +
            "Gems Earned: " + gemsEarned + System.lineSeparator() +
            "Levels Passed: " + levelsPassed + System.lineSeparator() +
            "Highest Score (Meow Point): " + player.bestScore;
    }
}
