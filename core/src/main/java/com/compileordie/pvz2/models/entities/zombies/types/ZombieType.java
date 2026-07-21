package com.compileordie.pvz2.models.entities.zombies.types;

import com.compileordie.pvz2.models.game.levels.ChapterType;
import com.compileordie.pvz2.utils.Toolbox;

public enum ZombieType {
    // === مشترک بین همه مپ‌ها ===
    STANDARD(150, null),
    CONEHEAD(150, null),
    BUCKETHEAD(150, null),
    KNIGHT(150, null),
    BLOCKHEAD(150, null),
    GARGANTUAR(150, null),
    IMP(150, null),
    ALL_STAR(150, null),
    ARCADE_ZOMBIE(150, null),
    PARASOL_ZOMBIE(150, null),
    TURQUOISE_ZOMBIE(150, null),
    PROSPECTOR_ZOMBIE(150, null),
    PIANIST_ZOMBIE(150, null),
    NEWSPAPER_ZOMBIE(150, null),
    BARREL_ROLLER(150, null),

    // === مصر باستان ===
    RA_ZOMBIE(150, ChapterType.ANCIENT_EGYPT),
    EXPLORER_ZOMBIE(150, ChapterType.ANCIENT_EGYPT),
    TOMBRAISER(150, ChapterType.ANCIENT_EGYPT),

    // === غارهای یخی ===
    DODO_RIDER(150, ChapterType.FROSTBITE_CAVES),
    HUNTER_ZOMBIE(150, ChapterType.FROSTBITE_CAVES),
    TROGLOBITE(150, ChapterType.FROSTBITE_CAVES),

    // === ساحل ===
    FISHERMAN_ZOMBIE(150, ChapterType.BIG_WAVE_BEACH),
    SNORKEL_ZOMBIE(150, ChapterType.BIG_WAVE_BEACH),
    OCTOPUS_ZOMBIE(150, ChapterType.BIG_WAVE_BEACH),

    // === قرون وسطی ===
    JESTER_ZOMBIE(150, ChapterType.DARK_AGES),
    WIZARD_ZOMBIE(150, ChapterType.DARK_AGES),
    KING_ZOMBIE(150, ChapterType.DARK_AGES),
    IMP_DRAGON(150, ChapterType.DARK_AGES);

    public final int waveCost;
    public final ChapterType chapter;

    ZombieType(int waveCost, ChapterType chapter) {
        this.waveCost = waveCost;
        this.chapter = chapter;
    }

    public static ZombieType getByName(String name) {
        for (ZombieType zombieType : ZombieType.values()) {
            if (zombieType.toString().equalsIgnoreCase(name)) {
                return zombieType;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return Toolbox.enumToString(this, true);
    }
}
