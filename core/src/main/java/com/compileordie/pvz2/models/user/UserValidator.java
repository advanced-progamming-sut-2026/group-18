package com.compileordie.pvz2.models.user;

public class UserValidator {
    private UserValidator() {
    }

    public static boolean isUsernameValid(String username) {
        if (username == null || username.isEmpty()) return false;
        return username.matches("^[a-zA-Z0-9-]+$");
    }

    public static boolean isNicknameValid(String nickname) {
        if (nickname == null) return false;
        return nickname.length() >= 3 && nickname.length() <= 30;
    }

    public static String isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return "[ERROR] Password is too short. Must be at least 8 characters long.";
        }
        if (!password.matches(".*[a-z].*")) {
            return "[ERROR] Password must contain at least one lowercase letter.";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "[ERROR] Password must contain at least one uppercase letter.";
        }
        if (!password.matches(".*[0-9].*")) {
            return "[ERROR] Password must contain at least one digit.";
        }
        if (!password.matches(".*[!#$%^&*()=+}{\\[\\]|/\\\\:;'\",><?].*")) {
            return "[ERROR] Password must contain at least one special character.";
        }

        return null;
    }

    public static boolean isEmailValid(String email) {
        if (email == null) return false;

        if (email.chars().filter(ch -> ch == '@').count() != 1) return false;

        String[] parts = email.split("@");
        if (parts.length != 2) return false;

        String local = parts[0];
        String domain = parts[1];

        // NOTE: If the local part is only 1 character long, the bellow regex fails. We handle it separately.
        String localRegex = "^[a-zA-Z0-9](?!.*\\.\\.)[a-zA-Z0-9._-]*[a-zA-Z0-9]$";
        if (local.length() == 1 && !local.matches("^[a-zA-Z0-9]$")) return false;
        if (local.length() > 1 && !local.matches(localRegex)) return false;

        String domainRegex = "^[a-zA-Z0-9](?!.*\\.\\.)[a-zA-Z0-9-]*\\.[a-zA-Z0-9-]{2,}$";
        if (!domain.matches(domainRegex)) return false;

        String forbiddenSymbols = "\\/|][}{+=)(*&^%$#";
        for (char c : forbiddenSymbols.toCharArray()) {
            if (email.indexOf(c) != -1) return false;
        }

        return true;
    }
}
