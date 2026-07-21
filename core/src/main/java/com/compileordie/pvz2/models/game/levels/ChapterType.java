package com.compileordie.pvz2.models.game.levels;

import com.compileordie.pvz2.utils.Toolbox;

public enum ChapterType {
    ANCIENT_EGYPT,
    DARK_AGES,
    BIG_WAVE_BEACH,
    FROSTBITE_CAVES,
    MINIGAME;

    public static ChapterType getByName(String name) {
        for (ChapterType chapterType : ChapterType.values()) {
            if (chapterType.toString().equalsIgnoreCase(name)) {
                return chapterType;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return Toolbox.enumToString(this, true);
    }
}
