package com.compileordie.pvz2.models.missions.quests;

import com.compileordie.pvz2.utils.Toolbox;

public enum QuestCategory {
    DAILY,
    EPIC,
    CRITICAL;

    public static QuestCategory getByName(String name) {
        for (QuestCategory questCategory : QuestCategory.values()) {
            if (questCategory.toString().equalsIgnoreCase(name)) {
                return questCategory;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return Toolbox.enumToString(this, true);
    }
}
