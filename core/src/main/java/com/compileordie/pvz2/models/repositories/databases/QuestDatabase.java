package com.compileordie.pvz2.models.repositories.databases;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.missions.quests.Quest;
import com.compileordie.pvz2.models.repositories.databases.fundamentals.ImmutableDatabase;

import java.util.ArrayList;

public class QuestDatabase extends ImmutableDatabase<ArrayList<Quest>> {
    public QuestDatabase() {
        super(Constants.Paths.Assets.Quests.DATABASE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<Quest> load() {
        FileHandle file = Gdx.files.internal(filePath);
        if (file.exists()) {
            return json.fromJson(ArrayList.class, Quest.class, file);
        } else {
            return new ArrayList<>();
        }
    }
}
