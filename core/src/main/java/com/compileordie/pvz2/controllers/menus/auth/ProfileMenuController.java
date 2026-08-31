package com.compileordie.pvz2.controllers.menus.auth;

import com.compileordie.pvz2.config.PreferencesManager;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.models.user.UserValidator;
import com.compileordie.pvz2.network.AuthClient;
import com.compileordie.pvz2.network.NetworkSession;
import com.compileordie.pvz2.network.protocol.Message;

import java.io.IOException;

/**
 * فاز ۳: این کنترلر قبلا از AuthManager (مسیر کاملا محلی) استفاده می‌کرد:
 *  - تغییر نام‌کاربری فقط رجیستری محلی خالی AuthDatabase و AppModel.player.username را عوض
 *    می‌کرد؛ سرور هیچ‌وقت خبردار نمی‌شد -> از آن لحظه به بعد username کلاینت با username
 *    سشن سرور فرق می‌کرد و PLAYER_STATE_PUSH/PULL بعدی‌ها همه invalid_session می‌گرفتند.
 *  - تغییر رمز عبور فقط یک فیلد Argon2 تزئینی داخل خودِ Player را عوض می‌کرد که هیچ ربطی
 *    به هش واقعی SHA-256 سمت سرور (AccountStore) نداشت -> کاربر فکر می‌کرد رمزش عوض شده
 *    ولی برای لاگین بعدی همچنان باید رمز قدیمی را می‌زد.
 * حالا هر دو واقعا با سرور (AUTH_CHANGE_USERNAME / AUTH_CHANGE_PASSWORD) هماهنگ می‌شوند.
 */
public class ProfileMenuController {
    private ProfileMenuController() {
    }

    public static Result<Void> updateProfile(String newUsername, String newNickname, String newEmail) {
        Player currentUser = AppModel.player;

        Result<Void> result = UserValidator.isUsernameValid(newUsername);
        if (!result.isSuccess) {
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

        if (!newUsername.equals(currentUser.username)) {
            Result<Void> renameResult = changeUsername(newUsername);
            if (!renameResult.isSuccess) {
                return renameResult;
            }
        }

        currentUser.nickname = newNickname;
        currentUser.email = newEmail;
        new UserDatabase().save(currentUser);
        return Result.success();
    }

    /** یکتایی نهایی username را حالا سرور چک می‌کند، نه UserValidator.isUsernameUnique محلی (که همیشه true بود). */
    private static Result<Void> changeUsername(String newUsername) {
        String session = NetworkSession.getCurrentSession();
        Message result;
        try {
            AuthClient authClient = NetworkSession.ensureConnected();
            result = authClient.changeUsername(session, newUsername);
        } catch (IOException e) {
            return Result.failure("Could not connect to the server. Please check your connection.");
        }

        String status = result.get("status");
        switch (status == null ? "" : status) {
            case "USERNAME_TAKEN":
                return Result.failure("This username is already taken");
            case "INVALID_USERNAME":
                return Result.failure(orDefault(result.get("error"), "Invalid username"));
            case "SUCCESS":
                break;
            default:
                return Result.failure("Could not reach the server. Please try again.");
        }

        String oldUsername = AppModel.player.username;
        AppModel.player.username = newUsername;
        NetworkSession.setCurrentSession(result.get("session"));

        // اگر این کاربر "Stay logged in" را زده بود، اطلاعات ذخیره‌شده هم باید با username جدید هماهنگ شود
        // وگرنه اجرای بعدی برنامه سعی می‌کند با username قدیمی (که دیگر روی سرور وجود ندارد) لاگین کند.
        if (oldUsername.equals(PreferencesManager.getRememberedUsername())) {
            PreferencesManager.rememberCredentials(newUsername, PreferencesManager.getRememberedPassword());
        }

        return Result.success();
    }

    public static Result<Void> changePassword(String oldPassword, String newPassword) {
        if (oldPassword.equals(newPassword)) {
            return Result.failure("New password cannot be the same as the current password");
        }
        Result<Void> validation = UserValidator.isPasswordStrong(newPassword);
        if (!validation.isSuccess) {
            return validation;
        }

        String session = NetworkSession.getCurrentSession();
        Message result;
        try {
            AuthClient authClient = NetworkSession.ensureConnected();
            result = authClient.changePassword(session, oldPassword, newPassword);
        } catch (IOException e) {
            return Result.failure("Could not connect to the server. Please check your connection.");
        }

        String status = result.get("status");
        switch (status == null ? "" : status) {
            case "WRONG_PASSWORD":
                return Result.failure("Incorrect password");
            case "INVALID_PASSWORD":
                return Result.failure(orDefault(result.get("error"), "Invalid password"));
            case "SUCCESS":
                break;
            default:
                return Result.failure("Could not reach the server. Please try again.");
        }

        // اگر "Stay logged in" فعال بود، رمز جدید را هم برای ورود خودکار بعدی به‌روزرسانی می‌کنیم.
        String rememberedUser = PreferencesManager.getRememberedUsername();
        if (rememberedUser != null && rememberedUser.equals(AppModel.player.username)) {
            PreferencesManager.rememberCredentials(rememberedUser, newPassword);
        }

        return Result.success();
    }

    private static String orDefault(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }
}
