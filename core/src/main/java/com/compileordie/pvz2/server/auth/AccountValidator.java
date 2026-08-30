package com.compileordie.pvz2.server.auth;

/**
 * فاز ۱ - همان قوانین UserValidator کلاینت (com.compileordie.pvz2.models.user.UserValidator)
 * را سمت سرور هم رعایت می‌کنیم، چون تصمیم نهایی درباره‌ی معتبر بودن ثبت‌نام باید سمت سرور
 * گرفته شود (کلاینت دیگر مرجع نیست). این‌جا فقط قوانین username/password کپی شده؛
 * قوانین nickname/email چون به‌عنوان بخشی از خودِ Player (سمت کلاینت) مدیریت می‌شوند،
 * سمت سرور لازم نیست دوباره بررسی شوند (سرور اصلا محتوای Player را نمی‌شناسد).
 */
public final class AccountValidator {

    private AccountValidator() {
    }

    public static String validateUsername(String username) {
        if (username == null || username.isEmpty()) {
            return "Username cannot be empty";
        }
        if (!username.matches("^[a-zA-Z0-9-]+$")) {
            return "Username can only contain letters, numbers, and hyphens (-)";
        }
        return null; // یعنی معتبر است
    }

    public static String validatePassword(String password) {
        if (password == null || password.length() < 8) {
            return "Password is too short. Must be at least 8 characters long";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter";
        }
        if (!password.matches(".*[0-9].*")) {
            return "Password must contain at least one digit";
        }
        if (!password.matches(".*[!#$%^&*()=+}{\\[\\]|/\\\\:;'\",><?].*")) {
            return "Password must contain at least one special character";
        }
        return null; // یعنی معتبر است
    }
}
