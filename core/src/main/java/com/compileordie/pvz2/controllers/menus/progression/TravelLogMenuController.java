package com.compileordie.pvz2.controllers.menus.progression;

import com.compileordie.pvz2.controllers.AppController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.missions.quests.Quest;
import com.compileordie.pvz2.models.missions.quests.QuestCategory;
import com.compileordie.pvz2.models.missions.quests.QuestManager;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.views.helpers.Menu;

import java.util.*;
import java.util.stream.Collectors;

public class TravelLogMenuController {
    private TravelLogMenuController() {
    }

    public static String exitMenu() {
        return AppController.changeMenu(Menu.GAME);
    }

    public static String showPage(String name) {
        QuestCategory category = QuestCategory.getByName(name);
        if (category == null) {
            String availablePages = Arrays.stream(QuestCategory.values())
                .map(QuestCategory::toString)
                .collect(Collectors.joining(", "));

            return "[ERROR] Invalid page. Available pages are: " + availablePages + ".";
        }

        Player player = AppModel.player;
        QuestManager.checkDailyReset(player);

        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(category).append(" QUESTS ===").append(System.lineSeparator());

        boolean found = false;
        for (Quest quest : QuestManager.QUESTS) {
            if (quest.category == category) {
                found = true;
                int current = player.questProgress.getOrDefault(quest.id, 0);
                String status;

                if (player.claimedQuests.contains(quest.id)) {
                    status = "[CLAIMED]";
                } else if (current >= quest.targetAmount) {
                    status = "[COMPLETED - Ready to Claim!]";
                } else {
                    status = "[IN PROGRESS: " + current + "/" + quest.targetAmount + "]";
                }

                sb.append("ID: ").append(quest.id).append(" | ").append(quest.title).append(System.lineSeparator())
                    .append("  > ").append(quest.description).append(System.lineSeparator())
                    .append("  > Reward: ").append(quest.rewardAmount).append("x ")
                    .append(quest.rewardType).append(System.lineSeparator())
                    .append("  > Status: ").append(status).append(System.lineSeparator())
                    .append(System.lineSeparator());
            }
        }

        if (!found) return "No quests available in the " + category + " category.";
        return sb.toString().trim();
    }

    public static String claimReward(String questId) {
        Player player = AppModel.player;
        QuestManager.checkDailyReset(player);
        Quest quest = QuestManager.getQuestById(questId);
        if (quest == null) {
            return "[ERROR] Quest ID not found.";
        }
        if (player.claimedQuests.contains(quest.id)) {
            return "[ERROR] You have already claimed the reward for this quest.";
        }
        int current = player.questProgress.getOrDefault(quest.id, 0);
        if (current < quest.targetAmount) {
            return "[ERROR] Quest is not completed yet. Progress: " + current + "/" + quest.targetAmount;
        }

        String rewardMessage = quest.rewardAmount + " " + quest.rewardType;

        // Grant Reward
        if (quest.rewardType.equalsIgnoreCase("COINS")) {
            player.coins += quest.rewardAmount;
        } else if (quest.rewardType.equalsIgnoreCase("DIAMONDS")) {
            player.diamonds += quest.rewardAmount;
        } else if (quest.rewardType.equalsIgnoreCase("RANDOM_SEED")) {
            if (player.unlockedPlants != null && !player.unlockedPlants.isEmpty()) {
                Random rand = new Random();
                PlantType randomPlant = player.unlockedPlants.get(rand.nextInt(player.unlockedPlants.size()));
                if (player.seedPackets == null) player.seedPackets = new HashMap<>();
                player.seedPackets.put(randomPlant,
                    player.seedPackets.getOrDefault(randomPlant, 0) + quest.rewardAmount);

                rewardMessage = quest.rewardAmount + "x " + randomPlant.toString() + " Seed Packets";
            } else {
                return "[ERROR] You must unlock at least one plant to receive random seed packets.";
            }
        } else if (quest.rewardType.equalsIgnoreCase("RANDOM_PLANT")) {
            List<PlantType> lockedPlants = Arrays.stream(PlantType.values())
                .filter(p -> player.unlockedPlants == null || !player.unlockedPlants.contains(p))
                .toList();

            if (!lockedPlants.isEmpty()) {
                Random rand = new Random();
                PlantType newPlant = lockedPlants.get(rand.nextInt(lockedPlants.size()));
                if (player.unlockedPlants == null) player.unlockedPlants = new ArrayList<>();
                player.unlockedPlants.add(newPlant);

                rewardMessage = "1x " + newPlant.toString() + " (New Plant!)";
            } else {
                // Fallback reward if they already own every plant in the game
                player.coins += ConfigManager.economy().questFallbackReward;
                rewardMessage = ConfigManager.economy().questFallbackReward + " COINS (All plants already unlocked!)";
            }
        } else {
            // Assume it's a specific seed packet string, e.g., "PEASHOOTER"
            PlantType targetPlant = PlantType.getByName(quest.rewardType);
            if (targetPlant != null) {
                // Ensure map exists (failsafe)
                if (player.seedPackets == null) player.seedPackets = new HashMap<>();
                player.seedPackets.put(targetPlant,
                    player.seedPackets.getOrDefault(targetPlant, 0) + quest.rewardAmount);

                rewardMessage = quest.rewardAmount + "x " + targetPlant + " Seed Packets";
            } else {
                return "[ERROR] Unknown reward type configuration: " + quest.rewardType;
            }
        }

        // Mark as claimed and update stats
        player.claimedQuests.add(quest.id);

        if (quest.category == QuestCategory.DAILY) {
            player.completedTotalDailyQuests++;
        } else {
            player.completedTotalNonDailyQuests++;
        }

        new UserDatabase(player.username).save(player);
        return "Reward claimed successfully! You received " + rewardMessage + ".";
    }
}
