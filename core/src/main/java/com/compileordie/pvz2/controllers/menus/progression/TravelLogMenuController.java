package com.compileordie.pvz2.controllers.menus.progression;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.missions.quests.Quest;
import com.compileordie.pvz2.models.missions.quests.QuestCategory;
import com.compileordie.pvz2.models.missions.quests.QuestManager;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.models.user.Player;

import java.util.*;
import java.util.stream.Collectors;

public class TravelLogMenuController {
    private TravelLogMenuController() {
    }

    /**
     * Retrieves a list of quests filtered by category and sorted by priority.
     */
    public static Result<List<Quest>> getQuestsByCategory(QuestCategory category) {
        Player player = AppModel.player;
        if (player == null) {
            return Result.failure("User is not logged in.");
        }

        QuestManager.checkDailyReset(player);

        List<Quest> filteredQuests = QuestManager.QUESTS.stream()
            .filter(q -> q.category == category)
            // Sorts descending: Higher integer value = higher priority at the top
            .sorted(Comparator.comparingInt((Quest q) -> q.priority).reversed())
            .collect(Collectors.toList());

        return Result.success(filteredQuests);
    }

    /**
     * Validates and claims the reward for a completed quest.
     */
    public static Result<String> claimReward(String questId) {
        Player player = AppModel.player;
        if (player == null) return Result.failure("User is not logged in.");

        QuestManager.checkDailyReset(player);

        Quest quest = QuestManager.getQuestById(questId);
        if (quest == null) return Result.failure("Quest ID not found.");

        if (player.claimedQuests.contains(quest.id)) {
            return Result.failure("You have already claimed the reward for this quest.");
        }

        int current = player.questProgress.getOrDefault(quest.id, 0);
        if (current < quest.targetAmount) {
            return Result.failure("Quest is not completed yet. Progress: " + current + "/" + quest.targetAmount);
        }

        String rewardMessage = grantReward(player, quest);
        if (rewardMessage.startsWith("[ERROR]")) {
            return Result.failure(rewardMessage.replace("[ERROR] ", ""));
        }

        updateQuestStats(player, quest);
        new UserDatabase(player.username).save(player);

        return Result.success("Reward claimed successfully! You received " + rewardMessage + ".");
    }

    private static String grantReward(Player player, Quest quest) {
        String type = quest.rewardType.toUpperCase();
        if (type.equals("COINS")) {
            player.coins += quest.rewardAmount;
            return quest.rewardAmount + " COINS";
        } else if (type.equals("DIAMONDS")) {
            player.diamonds += quest.rewardAmount;
            return quest.rewardAmount + " DIAMONDS";
        } else if (type.equals("RANDOM_SEED")) {
            return grantRandomSeed(player, quest.rewardAmount);
        } else if (type.equals("RANDOM_PLANT")) {
            return grantRandomPlant(player);
        }
        return grantSpecificSeed(player, quest.rewardType, quest.rewardAmount);
    }

    private static String grantRandomSeed(Player player, int amount) {
        if (player.unlockedPlants == null || player.unlockedPlants.isEmpty()) {
            return "[ERROR] You must unlock at least one plant to receive random seed packets.";
        }

        PlantType randomPlant = player.unlockedPlants.get(new Random().nextInt(player.unlockedPlants.size()));
        if (player.seedPackets == null) player.seedPackets = new HashMap<>();
        player.seedPackets.put(randomPlant, player.seedPackets.getOrDefault(randomPlant, 0) + amount);

        return amount + "x " + randomPlant.toString() + " Seed Packets";
    }

    private static String grantRandomPlant(Player player) {
        List<PlantType> lockedPlants = Arrays.stream(PlantType.values())
            .filter(p -> player.unlockedPlants == null || !player.unlockedPlants.contains(p))
            .toList();

        if (lockedPlants.isEmpty()) {
            int fallback = ConfigManager.economy().questFallbackReward;
            player.coins += fallback;
            return fallback + " COINS (All plants already unlocked!)";
        }

        PlantType newPlant = lockedPlants.get(new Random().nextInt(lockedPlants.size()));
        if (player.unlockedPlants == null) player.unlockedPlants = new ArrayList<>();
        player.unlockedPlants.add(newPlant);

        return "1x " + newPlant.toString() + " (New Plant!)";
    }

    private static String grantSpecificSeed(Player player, String plantName, int amount) {
        PlantType targetPlant = PlantType.getByName(plantName);
        if (targetPlant == null) return "[ERROR] Unknown reward type configuration: " + plantName;

        if (player.seedPackets == null) player.seedPackets = new HashMap<>();
        player.seedPackets.put(targetPlant, player.seedPackets.getOrDefault(targetPlant, 0) + amount);

        return amount + "x " + targetPlant + " Seed Packets";
    }

    private static void updateQuestStats(Player player, Quest quest) {
        player.claimedQuests.add(quest.id);
        if (quest.category == QuestCategory.DAILY) {
            player.completedTotalDailyQuests++;
        } else {
            player.completedTotalNonDailyQuests++;
        }
    }
}
