package com.compileordie.pvz2.controllers.menus.auth;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.models.user.Gender;
import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.models.user.UserValidator;
import com.compileordie.pvz2.models.user.authentication.AuthManager;
import com.compileordie.pvz2.network.AuthClient;
import com.compileordie.pvz2.network.NetworkSession;
import com.compileordie.pvz2.network.PlayerSerializer;
import com.compileordie.pvz2.network.protocol.Message;

import java.io.IOException;

public class SignupMenuController {
    private SignupMenuController() {
    }

    /**
     * فاز ۱ - گام ۱.۱/۱.۲/۱.۴:
     *  ۱. فرمت‌های ساده (username/password/nickname/email/gender) همچنان سمت کلاینت چک می‌شوند
     *     چون فوری‌اند و نیازی به سرور ندارند (تجربه‌ی کاربری بهتر: خطای فوری بدون رفت‌وبرگشت شبکه).
     *  ۲. یکتایی username دیگر محلی چک نمی‌شود (چون کلاینت دیگر مستقیم به AuthDatabase دسترسی
     *     ندارد)؛ این چک نهایی و معتبر را سرور انجام می‌دهد (AUTH_REGISTER -> USERNAME_TAKEN).
     *  ۳. اگر سرور ثبت‌نام را قبول کرد، کلاینت (که همه‌ی کلاس‌های دامنه‌ی بازی مثل Player را دارد)
     *     یک Player جدید با مقادیر پیش‌فرض می‌سازد و بلافاصله با PLAYER_STATE_PUSH آن را
     *     به سرور می‌فرستد تا از همون لحظه‌ی اول، سرور تنها منبع حقیقت باشد.
     */
    public static Result<Void> registerUser(String username,
                                            String password, String passwordConfirm,
                                            String nickname, String email, String gender,
                                            String question, String answer) {
        Result<Void> result;
        result = UserValidator.isUsernameValid(username);
        if (!result.isSuccess) {
            return result;
        }
        // توجه: UserValidator.isUsernameUnique دیگر صدا زده نمی‌شود چون به AuthDatabase محلی
        // وابسته است؛ یکتایی را حالا سرور به‌صورت نهایی چک می‌کند.
        result = UserValidator.isPasswordStrong(password);
        if (!result.isSuccess) {
            return result;
        }
        result = UserValidator.isNicknameValid(nickname);
        if (!result.isSuccess) {
            return result;
        }
        result = UserValidator.isEmailValid(email);
        if (!result.isSuccess) {
            return result;
        }
        Gender genderType = switch (gender.toLowerCase()) {
            case "male" -> Gender.MALE;
            case "female" -> Gender.FEMALE;
            default -> null;
        };
        if (genderType == null) {
            return Result.failure("Invalid gender type");
        }
        if (!password.equals(passwordConfirm)) {
            return Result.failure("Passwords do not match");
        }
        if (answer.isEmpty()) {
            return Result.failure("Please fill the security answer");
        }

        Message registerResult = null;
        try {
            AuthClient authClient = NetworkSession.ensureConnected();
            registerResult = authClient.register(username, password);
            AppModel.isOnline = true;
        } catch (IOException e) {
            AppModel.isOnline = false;
            if (AuthManager.getUserByUsername(username) != null) {
                return Result.failure("Username already exists locally. Connect to server to verify.");
            }
        }

        if (AppModel.isOnline) {
            String status = registerResult.get("status");
            if (status == null) return Result.failure("Server did not respond. Please try again.");

            switch (status) {
                case "USERNAME_TAKEN":
                    return Result.failure("This username is already taken");
                case "INVALID_USERNAME":
                    return Result.failure(orDefault(registerResult.get("error"), "Invalid username"));
                case "INVALID_PASSWORD":
                    return Result.failure(orDefault(registerResult.get("error"), "Invalid password"));
                case "SUCCESS":
                    break;
                default:
                    return Result.failure("Could not reach the server. Please try again.");
            }

            NetworkSession.setCurrentSession(registerResult.get("session"));
        }

        String localPasswordHash = AuthManager.hashPassword(password.toCharArray());
        Player player = new Player(username, localPasswordHash, nickname, email, genderType, question, answer);

        if (AppModel.isOnline) {
            String serializedPlayer = PlayerSerializer.serialize(player);
            try {
                Message pushResult = NetworkSession.ensureConnected().pushPlayerData(NetworkSession.getCurrentSession(), serializedPlayer);
                if (!"SUCCESS".equals(pushResult.get("status"))) {
                    return Result.failure("Registered, but could not save initial player data.");
                }
            } catch (IOException e) {
                return Result.failure("Registered, but could not save initial player data.");
            }
        } else {
            player.pendingServerSync = true;
            new UserDatabase(username).save(player);
        }

        AppModel.player = player;
        return Result.success();
    }

    private static String orDefault(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }
}
