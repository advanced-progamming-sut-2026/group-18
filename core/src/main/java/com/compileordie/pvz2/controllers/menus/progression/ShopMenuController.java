package com.compileordie.pvz2.controllers.menus.progression;

import com.compileordie.pvz2.controllers.AppController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.greenhouse.Pot;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.models.shop.CurrencyType;
import com.compileordie.pvz2.models.shop.Shop;
import com.compileordie.pvz2.models.shop.ShopItem;
import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.views.helpers.Menu;

import java.util.Random;

public class ShopMenuController {
    private ShopMenuController() {
    }

    public static String exitMenu() {
        return AppController.changeMenu(Menu.COLLECTION);
    }

    public static String showList() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-15s | %-25s | %-10s | %-10s%n", "Item ID", "Name", "Cost", "Yield"));
        sb.repeat("-", 65).append(System.lineSeparator());

        for (ShopItem item : Shop.PERMANENT_ITEMS) {
            String cost = item.price + " " + item.currencyType;
            String yield = (item.itemId.equals("exchange") ? item.quantity + " coins" : "x" + item.quantity);
            sb.append(String.format("%-15s | %-25s | %-10s | %-10s%n", item.itemId, item.displayName, cost, yield));
        }
        return sb.toString().trim();
    }

    public static String showDaily() {
        Player player = AppModel.player;
        Shop.refreshDailyOfferIfNeeded(player);

        if (player.dailyOffer.targetPlant == null) {
            return "Daily Offer: Unavailable. Unlock some plants first!";
        }

        return "--- DAILY OFFER ---" + System.lineSeparator() +
            "Item: " + player.dailyOffer.quantity + "x Seed Packets for "
            + player.dailyOffer.targetPlant + System.lineSeparator() +
            "Price: " + player.dailyOffer.price + " coins" + System.lineSeparator() +
            "Status: " + (player.dailyOffer.purchased ? "Sold Out" : "Available");
    }

    public static String buy(String id, String countStr, String type) {
        Player player = AppModel.player;
        int count;

        try {
            count = Integer.parseInt(countStr);
            if (count <= 0) {
                return "[ERROR] Count must be positive.";
            }
        } catch (NumberFormatException e) {
            return "[ERROR] Count must be an integer.";
        }

        // Handle Daily Offer Purchase
        if (id.equalsIgnoreCase("daily")) {
            Shop.refreshDailyOfferIfNeeded(player);
            if (player.dailyOffer.targetPlant == null) {
                return "[ERROR] No daily offer available.";
            }
            if (player.dailyOffer.purchased) {
                return "[ERROR] You have already purchased the daily offer today.";
            }
            if (count > 1) {
                return "[ERROR] You can only buy 1 daily offer.";
            }
            if (player.coins < player.dailyOffer.price) {
                return "[ERROR] Insufficient coins.";
            }

            player.coins -= player.dailyOffer.price;
            player.seedPackets.put(player.dailyOffer.targetPlant,
                player.seedPackets.getOrDefault(player.dailyOffer.targetPlant,
                    0) + player.dailyOffer.quantity);
            player.dailyOffer.purchased = true;

            new UserDatabase(player.username).save(player);
            return "Successfully purchased the daily offer for " + player.dailyOffer.targetPlant + "!";
        }

        // Handle Permanent Catalog Purchases
        ShopItem item = Shop.getItemById(id);
        if (item == null) {
            return "[ERROR] Item ID not found.";
        }

        int totalCost = item.price * count;
        if (item.currencyType == CurrencyType.COINS && player.coins < totalCost) {
            return "[ERROR] Insufficient coins. You need " + totalCost + " coins.";
        }
        if (item.currencyType == CurrencyType.DIAMONDS && player.diamonds < totalCost) {
            return "[ERROR] Insufficient diamonds. You need " + totalCost + " diamonds.";
        }

        // Max Capacity Checks
        if (item.itemId.equals("pot") && (player.greenhousePots.size() + count) > item.maxOwnable) {
            return "[ERROR] You cannot exceed the maximum limit of " + item.maxOwnable + " pots.";
        }
        if (item.itemId.equals("plant_food") && (player.plantFoodCount + count) > item.maxOwnable) {
            return "[ERROR] You cannot exceed the maximum limit of " + item.maxOwnable + " plant foods.";
        }

        // Execution Routing
        if (item.currencyType == CurrencyType.COINS) player.coins -= totalCost;
        if (item.currencyType == CurrencyType.DIAMONDS) player.diamonds -= totalCost;

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
                    return "[ERROR] You must unlock plants before buying random seeds.";
                }
                for (int i = 0; i < count; i++) {
                    PlantType randomPlant = player.unlockedPlants.get(
                        new Random().nextInt(player.unlockedPlants.size())
                    );
                    player.seedPackets.put(randomPlant,
                        player.seedPackets.getOrDefault(randomPlant, 0) + item.quantity);
                }
                break;
            case "selected_seed":
                if (type == null || type.isEmpty()) {
                    return "[ERROR] You must specify a plant type (-t) for selected seeds.";
                }
                PlantType targetPlant = PlantType.getByName(type);
                if (targetPlant == null || !player.unlockedPlants.contains(targetPlant)) {
                    return "[ERROR] Plant not found or not unlocked yet.";
                }
                player.seedPackets.put(targetPlant,
                    player.seedPackets.getOrDefault(targetPlant, 0) + (item.quantity * count));
                break;
        }

        new UserDatabase(player.username).save(player);
        return "Successfully purchased " + count + "x " + item.displayName + ".";
    }
}
