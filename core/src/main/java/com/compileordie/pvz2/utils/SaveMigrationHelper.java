package com.compileordie.pvz2.utils;

import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.missions.Pot;
import com.compileordie.pvz2.models.user.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SaveMigrationHelper {
    private SaveMigrationHelper() {
    }

    public static void migratePlayer(Player player) {
        if (player == null) return;

        // Initialize any missing ArrayLists
        if (player.unlockedPlants == null) player.unlockedPlants = new ArrayList<>();
        if (player.unlockedZombies == null) player.unlockedZombies = new ArrayList<>();
        if (player.unlockedChapters == null) player.unlockedChapters = new ArrayList<>();
        if (player.unlockedLevels == null) player.unlockedLevels = new ArrayList<>();
        if (player.unlockedMiniGames == null) player.unlockedMiniGames = new ArrayList<>();
        if (player.greenhousePots == null) player.greenhousePots = new ArrayList<>();
        if (player.news == null) player.news = new ArrayList<>();

        // Initialize missing Maps
        if (player.seedPackets == null) {
            player.seedPackets = new HashMap<>();
        }
        if (player.plantLevels == null) {
            player.plantLevels = new HashMap<>();
        }
        if (player.plantBoosts == null) {
            player.plantBoosts = new HashMap<>();
        }
        if (player.questProgress == null) {
            player.questProgress = new HashMap<>();
        }
        if (player.claimedQuests == null) {
            player.claimedQuests = new HashSet<>();
        }

        // Ensure the Maps have default values for all existing PlantTypes
        for (PlantType plantType : PlantType.values()) {
            player.seedPackets.putIfAbsent(plantType, 0);
            player.plantLevels.putIfAbsent(plantType, 1);
            player.plantBoosts.put(plantType, false);
        }

        // Initialize Row 1 (first 5 pots) as unlocked per the design requirements
        if (player.greenhousePots.isEmpty()) {
            for (int i = 0; i < 5; i++) {
                player.greenhousePots.add(new Pot());
            }
        }

        // Handle specific primitive defaults
        // Missing ints default to 0, which is fine for coins/gems, but difficulty should be 3
        if (player.difficultyLevel == 0) {
            player.difficultyLevel = 3;
        }
    }
}
