package com.compileordie.pvz2.config;

import com.compileordie.pvz2.models.repositories.databases.PreferencesDatabase;
import com.compileordie.pvz2.models.user.authentication.AuthManager;

public class PreferencesManager {
    private PreferencesManager() {
    }

    public static void setDefaultUser(String username) {
        PreferencesDatabase database = new PreferencesDatabase();
        GamePreferences preferences = database.load();

        if (preferences == null) {
            preferences = new GamePreferences();
        }

        preferences.defaultUserField = AuthManager.getUserFieldByUsername(username);
        database.save(preferences);
    }

    public static String getDefaultUserField() {
        PreferencesDatabase database = new PreferencesDatabase();
        GamePreferences preferences = database.load();

        if (preferences == null) {
            return null;
        }
        return preferences.defaultUserField;
    }

    public static void clearDefaultUser() {
        setDefaultUser(null);
    }
}
