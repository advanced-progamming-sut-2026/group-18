package com.compileordie.pvz2.models.databases;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.databases.fundamentals.MutableDatabase;
import com.compileordie.pvz2.models.user.authentication.UserRegistry;

import java.util.ArrayList;

public class AuthDatabase extends MutableDatabase<ArrayList<UserRegistry>> {
    public AuthDatabase() {
        super(Constants.Paths.AUTH);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<UserRegistry> load() {
        FileHandle file = Gdx.files.local(filePath);
        if (file.exists()) {
            return json.fromJson(ArrayList.class, UserRegistry.class, file);
        } else {
            return new ArrayList<>();
        }
    }

    public void saveOne(UserRegistry userRegistry) {
        ArrayList<UserRegistry> database = load();
        database.add(userRegistry);
        save(database);
    }

    public UserRegistry loadOne(String username) {
        ArrayList<UserRegistry> database = load();
        for (UserRegistry userRegistry : database) {
            if (userRegistry.getUsername().equals(username)) return userRegistry;
        }
        return null;
    }
}
