package com.compileordie.pvz2.controllers.menus.progression;

import com.compileordie.pvz2.controllers.AppController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.views.helpers.Menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class CollectionMenuController {
    private CollectionMenuController() {
    }

    public static String exitMenu() {
        return AppController.changeMenu(Menu.GAME);
    }

    public static String showPlants() {
        Player player = AppModel.player;
        if (player.unlockedPlants == null || player.unlockedPlants.isEmpty()) {
            return "No plants unlocked yet.";
        }
        return "Unlocked Plants:" + System.lineSeparator() + player.unlockedPlants.stream()
            .map(Enum::toString)
            .collect(Collectors.joining(System.lineSeparator()));
    }

    public static String showAllPlants() {
        if (PlantType.values().length == 0) {
            return "[DEBUG] No plants defined in the game yet.";
        }
        return "All Game Plants:" + System.lineSeparator() + Arrays.stream(PlantType.values())
            .map(Enum::toString)
            .collect(Collectors.joining(System.lineSeparator()));
    }

    public static String showZombies() {
        Player player = AppModel.player;
        if (player.unlockedZombies == null || player.unlockedZombies.isEmpty()) {
            return "No zombies discovered yet.";
        }
        return "Discovered Zombies:" + System.lineSeparator() + player.unlockedZombies.stream()
            .map(Enum::toString)
            .collect(Collectors.joining(System.lineSeparator()));
    }

    public static String showAllZombies() {
        if (ZombieType.values().length == 0) {
            return "[DEBUG] No zombies defined in the game yet.";
        }
        return "All Game Zombies:" + System.lineSeparator() + Arrays.stream(ZombieType.values())
            .map(Enum::toString)
            .collect(Collectors.joining(System.lineSeparator()));
    }

    public static String showPlant(String name) {
        PlantType plantType = PlantType.getByName(name);
        if (plantType == null) {
            return "[ERROR] Invalid plant name.";
        }

        Player player = AppModel.player;
        boolean isUnlocked = player.unlockedPlants != null && player.unlockedPlants.contains(plantType);
        // TODO: Expand this later when Plant Config/Stats models are fully implemented.
        return "--- " + plantType + " ---" + System.lineSeparator() +
            "Status: " + (isUnlocked ? "Unlocked" : "Locked");
    }

    public static String showZombie(String name) {
        ZombieType zombieType = ZombieType.getByName(name);
        if (zombieType == null) {
            return "[ERROR] Invalid zombie name.";
        }

        Player player = AppModel.player;
        boolean isUnlocked = player.unlockedZombies != null && player.unlockedZombies.contains(zombieType);
        // TODO: Expand this later when Zombie Config/Stats models are fully implemented.
        return "--- " + zombieType + " ---" + System.lineSeparator() +
            "Status: " + (isUnlocked ? "Unlocked" : "Locked");
    }

    public static String upgradePlant(String name) {
        PlantType plantType = PlantType.getByName(name);
        if (plantType == null) {
            return "[ERROR] Invalid plant name.";
        }
        Player player = AppModel.player;
        if (player.unlockedPlants == null || !player.unlockedPlants.contains(plantType)) {
            return "[ERROR] You must unlock this plant before upgrading it.";
        }

        int currentLevel = player.plantLevels.get(plantType);
        int coinCost = currentLevel * ConfigManager.economy().plantUpgradeCoinsPerLevel;
        int packetCost = currentLevel * ConfigManager.economy().plantUpgradeSeedsPerLevel;
        int currentPackets = player.seedPackets.get(plantType);

        if (player.coins < coinCost || currentPackets < packetCost) {
            return "[ERROR] Insufficient resources. You need " + (player.coins - coinCost) +
                " more coins and " + (currentPackets - packetCost) + " more seed packets to upgrade.";
        }

        player.coins -= coinCost;
        player.seedPackets.put(plantType, currentPackets - packetCost);
        player.plantLevels.put(plantType, currentLevel + 1);

        new UserDatabase(player.username).save(player);

        return plantType + " upgraded successfully to level " + (currentLevel + 1) + "!";
    }

    public static String purchasePlant(String name) {
        int price = ConfigManager.economy().plantPurchaseCoins;

        PlantType plantType = PlantType.getByName(name);
        if (plantType == null) {
            return "[ERROR] Invalid plant name.";
        }
        Player player = AppModel.player;
        if (player.unlockedPlants != null && player.unlockedPlants.contains(plantType)) {
            return "[ERROR] Plant is already unlocked.";
        }
        if (player.coins < price) {
            return "[ERROR] Insufficient coins. You need " + (player.coins - price)
                + " more coins to purchase a new plant.";
        }

        player.coins -= price;
        if (player.unlockedPlants == null) {
            player.unlockedPlants = new ArrayList<>();
        }
        player.unlockedPlants.add(plantType);

        new UserDatabase(player.username).save(player);

        return plantType + " purchased successfully!";
    }
}
