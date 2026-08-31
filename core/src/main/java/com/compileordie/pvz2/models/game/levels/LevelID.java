package com.compileordie.pvz2.models.game.levels;

import com.compileordie.pvz2.utils.Toolbox;

import java.util.Set;

public enum LevelID {
    STANDARD_ANCIENT_EGYPT(ChapterType.ANCIENT_EGYPT, LevelType.STANDARD, 3),
    CONVEYOR_BELT(ChapterType.ANCIENT_EGYPT, LevelType.SPECIAL, 4),
    LOCKED_PLANTS(ChapterType.ANCIENT_EGYPT, LevelType.SPECIAL, 5),
    BOSS_ANCIENT_EGYPT(ChapterType.ANCIENT_EGYPT, LevelType.ZOMBOSS, 0),
    STANDARD_FROSTBITE_CAVES(ChapterType.FROSTBITE_CAVES, LevelType.STANDARD, 3),
    SAVE_OUR_SEEDS(ChapterType.FROSTBITE_CAVES, LevelType.SPECIAL, 4),
    LOVE_YOUR_PLANTS(ChapterType.FROSTBITE_CAVES, LevelType.SPECIAL, 5),
    BOSS_FROSTBITE_CAVES(ChapterType.FROSTBITE_CAVES, LevelType.ZOMBOSS, 0),
    STANDARD_BIG_WAVE_BEACH(ChapterType.BIG_WAVE_BEACH, LevelType.STANDARD, 3),
    PLANT_WHAT_YOU_GET(ChapterType.BIG_WAVE_BEACH, LevelType.SPECIAL, 4),
    TIMED_WAR(ChapterType.BIG_WAVE_BEACH, LevelType.SPECIAL, 5),
    BOSS_BIG_WAVE_BEACH(ChapterType.BIG_WAVE_BEACH, LevelType.ZOMBOSS, 0),
    STANDARD_DARK_AGES(ChapterType.DARK_AGES, LevelType.STANDARD, 3),
    NIGHT_OPS(ChapterType.DARK_AGES, LevelType.SPECIAL, 4),
    DEAD_LINE(ChapterType.DARK_AGES, LevelType.SPECIAL, 5),
    BOSS_DARK_AGES(ChapterType.DARK_AGES, LevelType.ZOMBOSS, 0),
    VASE_BREAKER(0),
    WALNUT_BOWLING(5),
    I_ZOMBIE(0),
    BEGHOULED(-1),
    ZOMBOTANY(5);

    public final ChapterType chapterType;
    public final LevelType levelType;
    public final int waveNumber;
    private static final LevelID[] VALUES = values();

    LevelID(ChapterType chapterType, LevelType levelType, int waveNumber) {
        this.chapterType = chapterType;
        this.levelType = levelType;
        this.waveNumber = waveNumber;
    }

    LevelID(int waveNumber) {
        this.chapterType = ChapterType.MINIGAME;
        this.levelType = LevelType.MINIGAME;
        this.waveNumber = waveNumber;
    }

    public boolean needsPlantSelection() {
        return !Set.of(CONVEYOR_BELT, VASE_BREAKER, I_ZOMBIE, WALNUT_BOWLING, BEGHOULED, LOCKED_PLANTS).contains(this);
    }

    // Used by the "Night or Morning" quest to detect daytime levels. Follows the same
    // literal-identifier convention already used elsewhere in this enum/codebase
    // (e.g. EconomyManager's `gameBoard.levelID == LevelID.WALNUT_BOWLING` check).
    public boolean isNightLevel() {
        return this.name().contains("NIGHT");
    }

    public LevelID next() {
        return VALUES[(this.ordinal() + 1) % VALUES.length];
    }

    public static LevelID getByName(String name) {
        for (LevelID levelId : VALUES) {
            if (levelId.name().equalsIgnoreCase(name)) {
                return levelId;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return Toolbox.enumToString(this, true);
    }
}
