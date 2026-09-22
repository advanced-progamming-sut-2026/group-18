package com.compileordie.pvz2.controllers.menus.auth;

import com.compileordie.pvz2.config.PreferencesManager;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.models.user.UserValidator;
import com.compileordie.pvz2.models.user.authentication.AuthManager;
import com.compileordie.pvz2.network.AuthClient;
import com.compileordie.pvz2.network.NetworkSession;
import com.compileordie.pvz2.network.PlayerSerializer;
import com.compileordie.pvz2.network.protocol.Message;

import java.io.IOException;

public class LoginMenuController {
    private LoginMenuController() {
    }

    /**
     * فاز ۱ - گام ۱.۲: به‌جای صدا زدن مستقیم AuthManager (که سمت کلاینت روی فایل محلی کار
     * می‌کرد)، حالا یک پیام AUTH_LOGIN به سرور فرستاده می‌شود و AppModel.player از روی
     * دیتایی که سرور برمی‌گرداند ساخته می‌شود (گام ۱.۴: دیگر از فایل لوکال لود نمی‌شود).
     *
     * توجه: این متد همچنان synchronous/blocking است (دقیقا مثل قبل که فایل می‌خواند) تا
     * امضای متد و رفتار LoginMenuScreen عوض نشود؛ چون این عملیات فقط با کلیک روی دکمه‌ی
     * Login اجرا می‌شود (نه هر فریم)، چند صدم ثانیه معطلی برای رفت‌وبرگشت شبکه‌ی لوکال
     * قابل‌قبول است. اگر بعدا لازم شد کاملا async شود، باید این فراخوانی را داخل یک ترد
     * جدا اجرا کرد و نتیجه را با Gdx.app.postRunnable به render thread برگرداند.
     */
    public static Result<Void> loginUser(String username, String password, boolean stay) {
        Message result;
        try {
            AuthClient authClient = NetworkSession.ensureConnected();
            result = authClient.login(username, password);
            AppModel.isOnline = true;
        } catch (IOException e) {
            AppModel.isOnline = false;
            Player localUser = AuthManager.getUserByUsername(username);

            if (localUser != null) {
                AppModel.player = localUser;
                if (stay) {
                    PreferencesManager.rememberCredentials(username, password);
                } else {
                    PreferencesManager.clearDefaultUser();
                }
                return Result.success();
            }
            return Result.failure("Server unreachable and no local profile found for offline play.");
        }

        String status = result.get("status");
        if (status == null) {
            return Result.failure("Server did not respond. Please try again.");
        }

        // --- LAZY SYNC LOGIC ---
        // 1. SYNC NEW OFFLINE ACCOUNTS
        if ("USER_NOT_FOUND".equals(status)) {
            Player localUser = AuthManager.getUserByUsername(username);
            if (localUser != null && localUser.pendingServerSync) {
                try {
                    AuthClient authClient = NetworkSession.ensureConnected();
                    Message regResult = authClient.register(username, password);

                    if ("SUCCESS".equals(regResult.get("status"))) {
                        // Account registered. Push local JSON to server.
                        String session = regResult.get("session");
                        NetworkSession.ensureConnected().pushPlayerData(session, PlayerSerializer.serialize(localUser));

                        // Re-login to grab the official session and update the result payload
                        result = authClient.login(username, password);
                        status = result.get("status"); // Update status for the switch block below

                        // Clear local sync flag
                        localUser.pendingServerSync = false;
                        new com.compileordie.pvz2.models.repositories.databases.UserDatabase(username).save(localUser);
                    }
                } catch (IOException ignored) {}
            }
        }

        // 2. SYNC EXISTING ACCOUNTS WITH OFFLINE PROGRESSION
        if ("SUCCESS".equals(status)) {
            Player localUser = AuthManager.getUserByUsername(username);
            if (localUser != null && localUser.pendingServerSync) {
                try {
                    String session = result.get("session");
                    NetworkSession.ensureConnected().pushPlayerData(session, PlayerSerializer.serialize(localUser));

                    localUser.pendingServerSync = false;
                    new com.compileordie.pvz2.models.repositories.databases.UserDatabase(username).save(localUser);

                    // Overwrite the outdated server JSON in the result payload with our local one
                    result.put("data", PlayerSerializer.serialize(localUser));
                } catch (IOException ignored) {}
            }
        }
        // --- END LAZY SYNC LOGIC ---

        switch (status) {
            case "USER_NOT_FOUND":
                return Result.failure("User not found");
            case "WRONG_PASSWORD":
                return Result.failure("Wrong password");
            case "SUCCESS":
                break;
            default:
                // شامل ERROR (مثلا timeout) یا هر status ناشناخته‌ی دیگر
                return Result.failure("Could not reach the server. Please try again.");
        }

        Player player = PlayerSerializer.deserialize(result.get("data"));
        if (player == null) {
            return Result.failure("Your account exists but has no player data on the server yet.");
        }

        if (stay) {
            PreferencesManager.rememberCredentials(username, password);
        } else {
            PreferencesManager.clearDefaultUser();
        }

        AppModel.player = player;
        NetworkSession.setCurrentSession(result.get("session"));

        return Result.success();
    }

    /**
     * توجه مهم: این دو متد (فراموشی رمز عبور) فعلا دست‌نخورده و محلی مانده‌اند.
     * چون سرور فاز ۱ فقط username/password/blob دیتای بازیکن را می‌شناسد، نه
     * securityQuestion/securityAnswer را (که داخل خود Player دفن شده و سرور محتوایش را
     * نمی‌بیند). داکیومنت فاز ۳ صراحتا «فراموشی رمز» را جزو گام‌های الزامی فاز ۱ نیاورده؛
     * برای همین این بخش فعلا با همان AuthManager/UserDatabase محلی کار می‌کند و باگ
     * شناخته‌شده‌اش این است: روی یک دستگاه دیگر (که فایل محلی کاربر را ندارد) کار نمی‌کند.
     * اگر لازم شد، در فاز بعدی یک AUTH_FORGOT_PASSWORD به پروتکل اضافه می‌کنیم.
     */
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
