package com.compileordie.pvz2.models.repositories.databases.fundamentals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

abstract public class MutableDatabase<T> extends DatabaseConnector<T> {
    public MutableDatabase(String filePath) {
        super(filePath);
    }

    public void save(T data) {
        FileHandle file = Gdx.files.local(filePath);
        file.writeString(json.prettyPrint(data, 0)
            .replace("\t", "  ")
            .replace("\n", "\n  ")
            .replace("\n  ]", "\n]"), false);
    }
}
