package com.compileordie.pvz2.utils;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.compileordie.pvz2.models.missions.News;

public class UtilityCenter {
    static void main(String[] args) {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();

        new HeadlessApplication(new ApplicationAdapter() {
            @Override
            public void create() {
                for (int i = 0; i < 20; i++) {
                    NewsUtility.addNews("smabedi", new News("Title #" + i, "Number " + i * i * i * i));
                }
                Gdx.app.exit();
            }
        }, config);
    }
}
