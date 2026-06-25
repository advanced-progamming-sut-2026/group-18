package com.compileordie.pvz2.config;

import com.compileordie.pvz2.models.databases.PreferencesDatabase;
import com.compileordie.pvz2.models.user.authentication.AuthManager;

public class PreferencesManager {
    private PreferencesManager() {
    }

    public static void setDefaultUser(String username) {
        PreferencesDatabase database = new PreferencesDatabase();
        Preferences preferences = database.load();
        preferences.defaultUserField = AuthManager.getUserFieldByUsername(username);
        database.save(preferences);
    }

    public static void clearDefaultUser() {
        setDefaultUser(null);
    }
}
