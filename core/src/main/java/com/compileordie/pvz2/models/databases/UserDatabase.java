package com.compileordie.pvz2.models.databases;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.databases.fundamentals.MutableDatabase;
import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.models.user.authentication.AuthManager;

public class UserDatabase extends MutableDatabase<Player> {
    public UserDatabase() {
        super(AuthManager.getUserPathByUsername(AppModel.getPlayer().getUsername()));
    }

    public UserDatabase(String username) {
        super(AuthManager.getUserPathByUsername(username));
    }

    @Override
    public Player load() {
        FileHandle file = Gdx.files.local(filePath);

        if (file.exists()) {
            return json.fromJson(Player.class, file);
        } else {
            return null;
        }
    }
}
