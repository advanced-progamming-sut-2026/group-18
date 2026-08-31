package com.compileordie.pvz2.models.game.levels;

import com.compileordie.pvz2.utils.Toolbox;

public enum LevelType {
    STANDARD,
    SPECIAL,
    ZOMBOSS,
    MINIGAME;

    public static LevelType getByName(String name) {
        for (LevelType levelType : LevelType.values()) {
            if (levelType.toString().equalsIgnoreCase(name)) {
                return levelType;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return Toolbox.enumToString(this, true);
    }
}
