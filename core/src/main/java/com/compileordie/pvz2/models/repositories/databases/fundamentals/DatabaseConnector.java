package com.compileordie.pvz2.models.repositories.databases.fundamentals;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

public abstract class DatabaseConnector<T> {
    protected final String filePath;
    protected final Json json;

    public DatabaseConnector(String filePath) {
        this.filePath = filePath;
        this.json = new Json();
        this.json.setOutputType(OutputType.json);
        this.json.setIgnoreUnknownFields(true);
    }

    public abstract T load();
}
