package com.compileordie.pvz2.models.databases;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.config.Preferences;
import com.compileordie.pvz2.models.databases.fundamentals.MutableDatabase;

public class PreferencesDatabase extends MutableDatabase<Preferences> {
    public PreferencesDatabase() {
        super(Constants.Paths.PREFERENCES);
    }

    @Override
    public Preferences load() {
        FileHandle file = Gdx.files.local(filePath);

        if (file.exists()) {
            return json.fromJson(Preferences.class, file);
        } else {
            return null;
        }
    }
}
