package com.compileordie.pvz2.controllers.menus.progression;

import com.badlogic.gdx.utils.TimeUtils;
import com.compileordie.pvz2.controllers.AppController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.missions.Pot;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.views.helpers.Menu;

import java.util.Random;

public class GreenhouseMenuController {
    private static final long HOUR_IN_MILLIS = 60L * 60L * 1000L;
    private static final Random RANDOM = new Random();

    private GreenhouseMenuController() {
    }

    public static String exitMenu() {
        return AppController.changeMenu(Menu.GAME);
    }

    private static int getPotIndex(String xStr, String yStr) {
        try {
            int x = Integer.parseInt(xStr);
            int y = Integer.parseInt(yStr);
            if (x < 1 || x > 5 || y < 1 || y > 4) {
                return -2; // Out of bounds code
            }
            return (y - 1) * 5 + (x - 1);
        } catch (NumberFormatException e) {
            return -1; // Parse error code
        }
    }

    public static String showGreenhouse() {
        Player player = AppModel.player;
        StringBuilder sb = new StringBuilder();
        long now = TimeUtils.millis();

        sb.append("=== GREENHOUSE TABLE ===").append(System.lineSeparator());

        for (int y = 1; y <= 4; y++) {
            for (int x = 1; x <= 5; x++) {
                int index = (y - 1) * 5 + (x - 1);
                sb.append("(").append(x).append(",").append(y).append("): ");

                if (index >= player.greenhousePots.size()) {
                    sb.append("[LOCKED]");
                } else {
                    Pot pot = player.greenhousePots.get(index);
                    if (pot.isEmpty) {
                        sb.append("[EMPTY]");
                    } else if (now >= pot.readyTimeMillis) {
                        String name = pot.isMarigold ? "Marigold" : pot.targetPlant.toString();
                        sb.append("[READY: ").append(name).append("]");
                    } else {
                        String name = pot.isMarigold ? "Marigold" : pot.targetPlant.toString();
                        long diff = pot.readyTimeMillis - now;
                        long hours = diff / HOUR_IN_MILLIS;
                        long minutes = (diff % HOUR_IN_MILLIS) / (60 * 1000);
                        sb.append("[GROWING: ")
                            .append(name)
                            .append(" - ")
                            .append(hours)
                            .append("h ")
                            .append(minutes)
                            .append("m]");
                    }
                }
                if (x < 5) sb.append(" | ");
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString().trim();
    }

    public static String plantPlot(String x, String y) {
        int index = getPotIndex(x, y);
        if (index == -1) {
            return "[ERROR] Coordinates must be integers.";
        }
        if (index == -2) {
            return "[ERROR] Coordinates out of bounds. X must be 1-5, Y must be 1-4.";
        }
        Player player = AppModel.player;
        if (index >= player.greenhousePots.size()) {
            return "[ERROR] This pot is locked. Unlock more pots in the shop.";
        }
        Pot pot = player.greenhousePots.get(index);
        if (!pot.isEmpty) {
            return "[ERROR] This pot already has a plant in it.";
        }

        long now = TimeUtils.millis();

        // 50% chance for Marigold, 50% chance for an unlocked plant
        if (RANDOM.nextBoolean() || player.unlockedPlants == null || player.unlockedPlants.isEmpty()) {
            pot.isEmpty = false;
            pot.isMarigold = true;
            pot.targetPlant = null;
            pot.readyTimeMillis = now + (ConfigManager.economy().greenhouseMarigoldTime * HOUR_IN_MILLIS);
            new UserDatabase(player.username).save(player);
            return "Planted a Marigold! It will be ready in " + ConfigManager.economy().greenhouseMarigoldTime
                + " hours.";
        } else {
            pot.isEmpty = false;
            pot.isMarigold = false;
            // Pick a random plant from the user's unlocked list
            pot.targetPlant = player.unlockedPlants.get(RANDOM.nextInt(player.unlockedPlants.size()));
            pot.readyTimeMillis = now + (ConfigManager.economy().greenhousePlantTime * HOUR_IN_MILLIS);
            new UserDatabase(player.username).save(player);
            return "Planted a " + pot.targetPlant.toString() + "! It will be ready in "
                + ConfigManager.economy().greenhousePlantTime + " hours.";
        }
    }

    public static String collectPot(String x, String y) {
        int index = getPotIndex(x, y);
        if (index < 0) {
            return "[ERROR] Invalid coordinates.";
        }
        Player player = AppModel.player;
        if (index >= player.greenhousePots.size()) {
            return "[ERROR] This pot is locked.";
        }
        Pot pot = player.greenhousePots.get(index);
        if (pot.isEmpty) {
            return "[ERROR] This pot is empty.";
        }
        if (TimeUtils.millis() < pot.readyTimeMillis) {
            return "[ERROR] The plant is not ready yet.";
        }

        String result;
        if (pot.isMarigold) {
            player.coins += ConfigManager.economy().greenhouseMarigoldCoins;
            result = "Harvested a Marigold! You received " + ConfigManager.economy().greenhouseMarigoldCoins
                + " coins.";
        } else {
            // Fallback for older saves
            if (player.plantBoosts == null) player.plantBoosts = new java.util.HashMap<>();

            player.plantBoosts.put(pot.targetPlant, true);
            result = "Harvested a " + pot.targetPlant.toString() + "! A boost has been stored.";
        }

        // Reset pot
        pot.isEmpty = true;
        pot.isMarigold = false;
        pot.targetPlant = null;
        pot.readyTimeMillis = 0;

        new UserDatabase(player.username).save(player);
        return result;
    }

    public static String growPot(String x, String y) {
        int index = getPotIndex(x, y);
        if (index < 0) {
            return "[ERROR] Invalid coordinates.";
        }
        Player player = AppModel.player;
        if (index >= player.greenhousePots.size()) {
            return "[ERROR] This pot is locked.";
        }
        Pot pot = player.greenhousePots.get(index);
        if (pot.isEmpty) {
            return "[ERROR] This pot is empty.";
        }
        long now = TimeUtils.millis();
        if (now >= pot.readyTimeMillis) {
            return "[ERROR] This plant is already fully grown!";
        }
        long diff = pot.readyTimeMillis - now;
        int costInDiamonds = (int) Math.ceil((double) diff / HOUR_IN_MILLIS);
        if (player.diamonds < costInDiamonds) {
            return "[ERROR] Insufficient diamonds. You need " + costInDiamonds + " diamonds to accelerate this plant.";
        }

        player.diamonds -= costInDiamonds;
        pot.readyTimeMillis = now; // Instantly ready

        new UserDatabase(player.username).save(player);
        return "You accelerated the growth for " + costInDiamonds
            + " diamonds! The plant is now ready to be collected.";
    }

    public static String enterShop() {
        return AppController.changeMenu(Menu.SHOP);
    }
}
