package com.compileordie.pvz2.server.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * فاز ۱ - تصمیم: پسورد سمت سرور با SHA-256 هش می‌شود (نه Argon2 که کلاینت فعلا استفاده می‌کند).
 * دلیل: Argon2 یک کتابخانه‌ی خارجی با وابستگی native است؛ اضافه کردنش به یک سرور ساده‌ی
 * سوکت-محور بدون build tool، بی‌جهت پیچیده‌اش می‌کند. SHA-256 برای یک پروژه‌ی درسی کافی است.
 * (کلاینت هم از قبل متد hashPasswordSHA256 را در AuthManager دارد؛ منطق اینجا دقیقا همانی است.)
 */
public final class PasswordHasher {

    private PasswordHasher() {
    }

    public static String sha256Hex(String plainTextPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainTextPassword.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 در دسترس نیست", e);
        }
    }
}
