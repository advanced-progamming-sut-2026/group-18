package com.compileordie.pvz2.utils;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

public class DatabaseSeeder {
    static void main(String[] args) {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();

        new HeadlessApplication(new ApplicationAdapter() {
            @Override
            public void create() {
                QuestDatabaseSeeder.seed();
                Gdx.app.exit();
            }
        }, config);
    }
}
