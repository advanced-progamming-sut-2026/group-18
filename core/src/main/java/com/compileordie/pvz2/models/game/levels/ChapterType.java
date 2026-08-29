package com.compileordie.pvz2.models.game.levels;

import com.compileordie.pvz2.utils.Toolbox;

public enum ChapterType {
    ANCIENT_EGYPT("IMAGE_UI_UNIVERSE_WORLDS_EGYPT"),
    FROSTBITE_CAVES("IMAGE_UI_UNIVERSE_WORLDS_ICEAGE"),
    BIG_WAVE_BEACH("IMAGE_UI_UNIVERSE_WORLDS_BEACH"),
    DARK_AGES("IMAGE_UI_UNIVERSE_WORLDS_DARK"),
    MINIGAME("IMAGE_UI_UNIVERSE_WORLDS_TWISTER");

    public final String image;

    ChapterType(String image) {
        this.image = image;
    }

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
