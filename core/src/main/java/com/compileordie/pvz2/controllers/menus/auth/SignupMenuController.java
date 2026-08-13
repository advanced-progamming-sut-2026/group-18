package com.compileordie.pvz2.controllers.menus.auth;

import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.models.user.Gender;
import com.compileordie.pvz2.models.user.UserValidator;
import com.compileordie.pvz2.models.user.authentication.AuthManager;

public class SignupMenuController {
    private SignupMenuController() {
    }

    public static Result<Void> registerUser(String username,
                                            String password, String passwordConfirm,
                                            String nickname, String email, String gender,
                                            String question, String answer) {
        Result<Void> result;
        result = UserValidator.isUsernameValid(username);
        if (!result.isSuccess) {
            return result;
        }
        result = UserValidator.isUsernameUnique(username);
        if (!result.isSuccess) {
            return result;
        }
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

        AuthManager.signupPlayer(username, password.toCharArray(), nickname, email, genderType, question, answer);
        AuthManager.loginPlayer(username);
        return Result.success();
    }
}
