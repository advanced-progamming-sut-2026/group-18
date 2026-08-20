package com.compileordie.pvz2.models.user;

import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.models.repositories.databases.AuthDatabase;

public class UserValidator {
    private UserValidator() {
    }

    public static Result<Void> isUsernameValid(String username) {
        if (username == null || username.isEmpty()) {
            return Result.failure("Username cannot be empty");
        }

        if (!username.matches("^[a-zA-Z0-9-]+$")) {
            return Result.failure("Username can only contain letters, numbers, and hyphens (-)");
        }

        return Result.success();
    }

    public static Result<Void> isUsernameUnique(String username) {
        if (new AuthDatabase().loadOne(username) != null) {
            return Result.failure("This username is already taken");
        }

        return Result.success();
    }

    public static Result<Void> isNicknameValid(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return Result.failure("Nickname cannot be empty");
        }

        if (nickname.length() < 3 || nickname.length() > 30) {
            return Result.failure("Nickname must be between 3 and 30 characters");
        }

        return Result.success();
    }

    public static Result<Void> isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return Result.failure("Password is too short. Must be at least 8 characters long");
        }
        if (!password.matches(".*[a-z].*")) {
            return Result.failure("Password must contain at least one lowercase letter");
        }
        if (!password.matches(".*[A-Z].*")) {
            return Result.failure("Password must contain at least one uppercase letter");
        }
        if (!password.matches(".*[0-9].*")) {
            return Result.failure("Password must contain at least one digit");
        }
        if (!password.matches(".*[!#$%^&*()=+}{\\[\\]|/\\\\:;'\",><?].*")) {
            return Result.failure("Password must contain at least one special character");
        }

        return Result.success();
    }

    public static Result<Void> isEmailValid(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Result.failure("Email cannot be empty");
        }

        if (email.chars().filter(ch -> ch == '@').count() != 1) {
            return Result.failure("Email must contain exactly one '@' symbol");
        }

        String[] parts = email.split("@");
        if (parts.length != 2) {
            return Result.failure("Invalid email format");
        }

        String local = parts[0];
        String domain = parts[1];

        String localRegex = "^[a-zA-Z0-9](?!.*\\.\\.)[a-zA-Z0-9._-]*[a-zA-Z0-9]$";
        if (local.length() == 1 && !local.matches("^[a-zA-Z0-9]$")) {
            return Result.failure("Email prefix must start and end with a letter or number");
        }
        if (local.length() > 1 && !local.matches(localRegex)) {
            return Result.failure("Email prefix contains invalid characters or consecutive dots");
        }

        String domainRegex = "^[a-zA-Z0-9](?!.*\\.\\.)[a-zA-Z0-9-]*\\.[a-zA-Z0-9-]{2,}$";
        if (!domain.matches(domainRegex)) {
            return Result.failure("Email domain is invalid");
        }

        String forbiddenSymbols = "\\/|][}{+=)(*&^%$#";
        for (char c : forbiddenSymbols.toCharArray()) {
            if (email.indexOf(c) != -1) {
                return Result.failure("Email contains forbidden symbols");
            }
        }

        return Result.success();
    }
}
