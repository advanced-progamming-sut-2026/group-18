package com.compileordie.pvz2.config;

import com.compileordie.pvz2.models.repositories.databases.PreferencesDatabase;

public class PreferencesManager {
    private PreferencesManager() {
    }

    /**
     * "Stay logged in" را روی این دستگاه ذخیره می‌کند. توجه: این یک قابلیت local-only
     * device convenience است (دقیقا هم‌رده‌ی کوکی «مرا به خاطر بسپار» در مرورگرها)، نه یک
     * session token واقعی از سرور - سرور فعلا هیچ مکانیزم توکنی ندارد و هر بار باید کامل
     * login بزنیم. چون کل پروتکل همین الان هم روی سوکت خام بدون TLS منتقل می‌شود (پسورد
     * در AUTH_LOGIN/AUTH_REGISTER هم به همین شکل رد و بدل می‌شود)، این سطح از ذخیره‌سازی
     * محلی با مدل امنیتی فعلی این پروژه‌ی درسی هم‌خوان است.
     */
    public static void rememberCredentials(String username, String password) {
        PreferencesDatabase database = new PreferencesDatabase();
        GamePreferences preferences = database.load();

        if (preferences == null) {
            preferences = new GamePreferences();
        }

        preferences.rememberedUsername = username;
        preferences.rememberedPassword = password;
        database.save(preferences);
    }

    public static String getRememberedUsername() {
        PreferencesDatabase database = new PreferencesDatabase();
        GamePreferences preferences = database.load();
        return preferences != null ? preferences.rememberedUsername : null;
    }

    public static String getRememberedPassword() {
        PreferencesDatabase database = new PreferencesDatabase();
        GamePreferences preferences = database.load();
        return preferences != null ? preferences.rememberedPassword : null;
    }

    public static void clearDefaultUser() {
        rememberCredentials(null, null);
    }
}
