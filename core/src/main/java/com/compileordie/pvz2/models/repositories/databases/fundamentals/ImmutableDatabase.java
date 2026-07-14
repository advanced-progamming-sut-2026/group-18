package com.compileordie.pvz2.models.repositories.databases.fundamentals;

abstract public class ImmutableDatabase<T> extends DatabaseConnector<T> {
    public ImmutableDatabase(String filePath) {
        super(filePath);
    }
}
