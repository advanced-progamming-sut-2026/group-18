package com.compileordie.pvz2.models.entities.npcs;

import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.models.game.levels.LevelType;
import java.util.Random;

@SuppressWarnings("SpellCheckingInspection")
public enum NpcType {
    BROWNCOATZOMBIE("768/FULL/NPC/BROWNCOATZOMBIE/BROWNCOATZOMBIE.PAM", "Brainz... listen carefully:", false),
    GHOSTPEPPER("768/FULL/NPC/GHOSTPEPPER/GHOSTPEPPER.PAM", "Booo! Here is your mission briefing:", true),
    ICEBURG("768/FULL/NPC/ICEBURG/ICEBURG.PAM", "Stay cool! Here are your orders:", true),
    PEASHOOTER("768/FULL/NPC/PEASHOOTER/PEASHOOTER.PAM", "Ready, aim, fire! Mission objectives:", true),
    SUNFLOWER("768/FULL/NPC/SUNFLOWER/SUNFLOWER.PAM", "Sun's up! Here is what we need to do:", true),
    WALLNUT("768/FULL/NPC/WALLNUT/WALLNUT.PAM", "Hold the line! Directive for this battle:", true),
    ZOMBOSS("768/FULL/NPC/ZOMBOSS/ZOMBOSS.PAM", "Mwahahaha! Fools, prepare for your doom:", false);

    private static final Random RANDOM = new Random();
    private final String pamPath;
    private final String greeting;
    private final boolean isFacingRight;

    NpcType(String pamPath, String greeting, boolean isFacingRight) {
        this.pamPath = pamPath;
        this.greeting = greeting;
        this.isFacingRight = isFacingRight;
    }

    public String getPamPath() {
        return pamPath;
    }

    public String getGreeting() {
        return greeting;
    }

    public String getEnterAnim() {
        return name().toLowerCase() + "_enter";
    }

    public String getTalkAnim() {
        return name().toLowerCase() + "_talk";
    }

    public String getExitAnim() {
        return name().toLowerCase() + "_exit";
    }

    public boolean isFacingRight() {
        return isFacingRight;
    }

    public static NpcType selectForLevel(LevelID levelID) {
        if (levelID == null) {
            return PEASHOOTER;
        }

        // 1. Boss Level Exception
        if (levelID.levelType == LevelType.ZOMBOSS) {
            return ZOMBOSS;
        }

        // 2. Specific Level / Minigame Overrides
        switch (levelID) {
            case WALNUT_BOWLING:
                return WALLNUT;
            case I_ZOMBIE:
                return BROWNCOATZOMBIE;
        }

        // 3. Fallback Random Selection
        NpcType[] standardNpcs = {
            BROWNCOATZOMBIE, GHOSTPEPPER, ICEBURG,
            PEASHOOTER, SUNFLOWER, WALLNUT
        };
        return standardNpcs[RANDOM.nextInt(standardNpcs.length)];
    }
}
