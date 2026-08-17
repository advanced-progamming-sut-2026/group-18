package com.compileordie.pvz2.controllers.menus.progression;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.missions.Pot;
import com.compileordie.pvz2.models.missions.shop.CurrencyType;
import com.compileordie.pvz2.models.missions.shop.DailyOffer;
import com.compileordie.pvz2.models.missions.shop.Shop;
import com.compileordie.pvz2.models.missions.shop.ShopItem;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.models.user.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ShopMenuController {
    private ShopMenuController() {
    }

    public static List<ShopItem> getPermanentItems() {
        return Shop.PERMANENT_ITEMS;
    }

    public static Result<DailyOffer> getDailyOffer() {
        Player player = AppModel.player;
        Shop.refreshDailyOfferIfNeeded(player);

        if (player.dailyOffer.targetPlant == null) {
            return Result.failure("Unlock some plants first to receive daily offers!");
        }

        return Result.success(player.dailyOffer);
    }

    public static Result<String> buyDailyOffer() {
        Player player = AppModel.player;
        DailyOffer dailyOffer = player.dailyOffer;
        Shop.refreshDailyOfferIfNeeded(player);

        if (dailyOffer.targetPlant == null) return Result.failure("No daily offer available");
        if (dailyOffer.purchased) return Result.failure("You have already purchased the daily offer today");
        if (player.coins < dailyOffer.price) return Result.failure("Insufficient coins");

        player.coins -= dailyOffer.price;
        player.seedPackets.put(dailyOffer.targetPlant,
            player.seedPackets.getOrDefault(dailyOffer.targetPlant, 0) + dailyOffer.quantity);
        dailyOffer.purchased = true;

        new UserDatabase(player.username).save(player);
        return Result.success(
            "Purchased " + dailyOffer.quantity + " " + dailyOffer.targetPlant.toString() + " seeds successfully!"
        );
    }

    public static Result<String> buyCatalogItem(String id, int count, PlantType selectedPlant) {
        if (count <= 0) return Result.failure("Count must be positive");

        Player player = AppModel.player;
        ShopItem item = Shop.getItemById(id);

        if (item == null) return Result.failure("Item ID not found");

        int totalCost = item.price * count;

        String validationError = validatePurchase(player, item, count, totalCost);
        if (validationError != null) return Result.failure(validationError);

        Result<String> result = executeItemEffect(player, item, count, selectedPlant);
        if (!result.isSuccess) return result;

        // Deduct currency
        if (item.currencyType == CurrencyType.COINS) player.coins -= totalCost;
        if (item.currencyType == CurrencyType.DIAMONDS) player.diamonds -= totalCost;

        new UserDatabase(player.username).save(player);
        return result;
    }

    private static String validatePurchase(Player player, ShopItem item, int count, int totalCost) {
        if (item.currencyType == CurrencyType.COINS && player.coins < totalCost) {
            return "Insufficient coins. You need " + totalCost + " coins";
        }
        if (item.currencyType == CurrencyType.DIAMONDS && player.diamonds < totalCost) {
            return "Insufficient diamonds. You need " + totalCost + " diamonds";
        }
        if (item.itemId.equals("pot") && (player.greenhousePots.size() + count) > item.maxOwnable) {
            return "You cannot exceed the maximum limit of " + item.maxOwnable + " pots";
        }
        if (item.itemId.equals("plant_food") && (player.plantFoodCount + count) > item.maxOwnable) {
            return "You cannot exceed the maximum limit of " + item.maxOwnable + " plant foods";
        }
        return null;
    }

    private static Result<String> executeItemEffect(Player player, ShopItem item, int count, PlantType selectedPlant) {
        switch (item.itemId) {
            case "pot":
                for (int i = 0; i < count; i++) player.greenhousePots.add(new Pot());
                break;
            case "plant_food":
                player.plantFoodCount += count;
                break;
            case "exchange":
                player.coins += (item.quantity * count);
                break;
            case "random_seed":
                if (player.unlockedPlants == null || player.unlockedPlants.isEmpty()) {
                    return Result.failure("You must unlock plants before buying random seeds");
                }
                Random random = new Random();
                Map<PlantType, Integer> gainedSeeds = new HashMap<>();
                int totalSeedsToGive = count * item.quantity;
                for (int i = 0; i < totalSeedsToGive; i++) {
                    PlantType randomPlant = player.unlockedPlants.get(random.nextInt(player.unlockedPlants.size()));
                    player.seedPackets.put(randomPlant, player.seedPackets.getOrDefault(randomPlant, 0) + 1);
                    gainedSeeds.put(randomPlant, gainedSeeds.getOrDefault(randomPlant, 0) + 1);
                }
                StringBuilder messageBuilder = new StringBuilder("Obtained: ");
                int index = 0;
                for (Map.Entry<PlantType, Integer> entry : gainedSeeds.entrySet()) {
                    messageBuilder.append(entry.getValue()).append("x ").append(entry.getKey().toString());
                    if (index < gainedSeeds.size() - 1) {
                        messageBuilder.append(", ");
                    }
                    index++;
                }
                return Result.success(messageBuilder.toString());
            case "selected_seed":
                if (selectedPlant == null) {
                    return Result.failure("You must specify a plant type for selected seeds");
                }
                if (!player.unlockedPlants.contains(selectedPlant)) {
                    return Result.failure("Selected plant is not unlocked yet");
                }
                player.seedPackets.put(selectedPlant,
                    player.seedPackets.getOrDefault(selectedPlant, 0) + (item.quantity * count));
                break;
        }
        return Result.success("Purchase successful!");
    }
}
