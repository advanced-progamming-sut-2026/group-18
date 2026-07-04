package com.compileordie.pvz2.models.repositories.configs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

public final class ConfigLoader {
    private static final Json json = new Json();

    static {
        json.setIgnoreUnknownFields(true);
    }

    private ConfigLoader() {
    }

    public static <T> T load(String path, Class<T> clazz) {
        FileHandle file = Gdx.files.internal(path);
        if (!file.exists()) {
            throw new RuntimeException("Missing config file: " + path);
        }

        return json.fromJson(clazz, file);
    }
}
