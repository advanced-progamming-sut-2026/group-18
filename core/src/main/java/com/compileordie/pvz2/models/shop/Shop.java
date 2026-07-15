package com.compileordie.pvz2.models.shop;

import com.badlogic.gdx.utils.TimeUtils;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;
import com.compileordie.pvz2.models.user.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Shop {
    public static final List<ShopItem> PERMANENT_ITEMS = new ArrayList<>();
    private static final Random RANDOM = new Random();
    private static final SimpleDateFormat DAILY_FORMATTER = new SimpleDateFormat("yyyy/MM/dd");

    static {
        // itemId, displayName, price, currencyType, quantity, maxOwnable (-1 for no cap)
        PERMANENT_ITEMS.add(new ShopItem("pot",
            "Greenhouse Pot",
            ConfigManager.economy().shopPotPrice,
            CurrencyType.COINS,
            ConfigManager.economy().shopPotCount,
            ConfigManager.economy().shopPotCap));
        PERMANENT_ITEMS.add(new ShopItem("plant_food",
            "Plant Food",
            ConfigManager.economy().shopPlantFoodPrice,
            CurrencyType.DIAMONDS,
            ConfigManager.economy().shopPlantFoodCount,
            ConfigManager.economy().shopPlantFoodCap));
        PERMANENT_ITEMS.add(new ShopItem("random_seed",
            "Random Seed Packet",
            ConfigManager.economy().shopRandomSeedPrice,
            CurrencyType.COINS,
            ConfigManager.economy().shopRandomSeedCount,
            ConfigManager.economy().shopRandomSeedCap));
        PERMANENT_ITEMS.add(new ShopItem("selected_seed",
            "Selected Seed Packet",
            ConfigManager.economy().shopSeedPrice,
            CurrencyType.DIAMONDS,
            ConfigManager.economy().shopSeedCount,
            ConfigManager.economy().shopSeedCap));
        PERMANENT_ITEMS.add(new ShopItem("exchange",
            "Currency Exchange",
            ConfigManager.economy().shopExchangeGem,
            CurrencyType.DIAMONDS,
            ConfigManager.economy().shopExchangeCoin,
            ConfigManager.economy().shopExchangeCap));
    }

    public static void refreshDailyOfferIfNeeded(Player player) {
        String today = DAILY_FORMATTER.format(new Date(TimeUtils.millis()));

        // If no offer exists, or the recorded day is different from today
        if (player.dailyOffer == null || !today.equals(player.dailyOffer.offerDate)) {
            player.dailyOffer = new DailyOffer(today);
            rollDailyPlant(player);
        }
        // If they checked the shop with 0 unlocked plants earlier today,
        // but unlocked a plant later, we must roll the plant for them now.
        else if (player.dailyOffer.targetPlant == null) {
            rollDailyPlant(player);
        }
    }

    private static void rollDailyPlant(Player player) {
        if (player.unlockedPlants != null && !player.unlockedPlants.isEmpty()) {
            player.dailyOffer.targetPlant = player.unlockedPlants.get(RANDOM.nextInt(player.unlockedPlants.size()));
        } else {
            player.dailyOffer.targetPlant = null;
        }
    }

    public static ShopItem getItemById(String itemId) {
        for (ShopItem item : PERMANENT_ITEMS) {
            if (item.itemId.equalsIgnoreCase(itemId)) {
                return item;
            }
        }
        return null;
    }
}
