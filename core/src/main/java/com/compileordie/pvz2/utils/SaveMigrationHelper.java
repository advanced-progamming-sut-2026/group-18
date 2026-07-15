package com.compileordie.pvz2.utils;

import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.user.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class SaveMigrationHelper {
    private SaveMigrationHelper() {
    }

    public static void migratePlayer(Player player) {
        if (player == null) return;

        // 1. Initialize any missing ArrayLists
        if (player.unlockedPlants == null) player.unlockedPlants = new ArrayList<>();
        if (player.unlockedZombies == null) player.unlockedZombies = new ArrayList<>();
        if (player.unlockedChapters == null) player.unlockedChapters = new ArrayList<>();
        if (player.unlockedLevels == null) player.unlockedLevels = new ArrayList<>();
        if (player.unlockedMiniGames == null) player.unlockedMiniGames = new ArrayList<>();
        if (player.greenhousePots == null) player.greenhousePots = new ArrayList<>();
        if (player.news == null) player.news = new ArrayList<>();

        // 2. Initialize missing Maps
        if (player.seedPackets == null) {
            player.seedPackets = new HashMap<>();
        }
        if (player.plantLevels == null) {
            player.plantLevels = new HashMap<>();
        }

        // 3. Ensure the Maps have default values for all existing PlantTypes
        // (This protects against crashes if you add a brand-new plant to the game later)
        for (PlantType plantType : PlantType.values()) {
            player.seedPackets.putIfAbsent(plantType, 0);
            player.plantLevels.putIfAbsent(plantType, 1);
        }

        // 4. Handle specific primitive defaults
        // Missing ints default to 0, which is fine for coins/gems, but difficulty should be 3
        if (player.difficultyLevel == 0) {
            player.difficultyLevel = 3;
        }
    }
}
