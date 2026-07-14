package com.compileordie.pvz2.models.repositories.databases;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.config.GamePreferences;
import com.compileordie.pvz2.models.repositories.databases.fundamentals.MutableDatabase;

public class PreferencesDatabase extends MutableDatabase<GamePreferences> {
    public PreferencesDatabase() {
        super(Constants.Paths.Saves.PREFERENCES);
    }

    @Override
    public GamePreferences load() {
        FileHandle file = Gdx.files.local(filePath);

        if (file.exists()) {
            return json.fromJson(GamePreferences.class, file);
        } else {
            return null;
        }
    }
}
