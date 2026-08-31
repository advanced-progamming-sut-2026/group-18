package com.compileordie.pvz2.models.entities.npcs;

import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.models.game.levels.LevelType;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;

public class MissionProvider {
    public static String getMissionsDescription(LevelID levelID) {
        if (levelID == null) {
            return "- Unknown mission! Defend the lawn at all costs!";
        }
        if (levelID.levelType == LevelType.ZOMBOSS) {
            return "- Good luck defeating THE BOSS!";
        }
        return switch (levelID) {
            case CONVEYOR_BELT -> "- Plants will arrive on the conveyor belt";
            case LOCKED_PLANTS -> "- Choose your soldiers from the plants above";
            case SAVE_OUR_SEEDS -> "- Protect the sacred plants at all cost!";
            case TIMED_WAR -> "- Collect " + ConfigManager.gameplay().timedWarSunTarget + " suns in "
                + ConfigManager.gameplay().timedWarMax + " seconds";
            case NIGHT_OPS -> "- No suns in the night sky! Use sunflowers to generate your sun fuel!";
            case DEAD_LINE -> "- No one possessed body is allowed past the deadline!";
            case LOVE_YOUR_PLANTS -> "- Lose more than " + ConfigManager.gameplay().loveYourPlantsMaxLost
                + " plants and lose the match!";
            case PLANT_WHAT_YOU_GET -> "- You've been given a pile of suns. Do what must be done," +
                " no sun generation this time!";
            case VASE_BREAKER -> "- Break the vases and face the consequences!" +
                "\n- The game ends when either you or the vases end";
            case WALNUT_BOWLING -> "- You have been blessed with big balls! Crush them zombies";
            case I_ZOMBIE -> "- Be the villain for one time and eat the innocent plants!";
            case BEGHOULED -> "- Match the plants in rows and columns to unlock more";
            case ZOMBOTANY -> "- Have you ever heard of plant-zombies? No worries you're gonna meet in a second!";
            default -> "- Defend your lawn at all costs! No one is allowed past the lawn mowers!";
        } + "\n- Eliminate the zombie intruders!";
    }
}
