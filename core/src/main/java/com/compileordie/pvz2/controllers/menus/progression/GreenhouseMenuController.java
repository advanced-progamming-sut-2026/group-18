package com.compileordie.pvz2.controllers.menus.progression;

import com.badlogic.gdx.utils.TimeUtils;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.models.missions.Pot;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.models.user.Player;

import java.util.Random;

public class GreenhouseMenuController {
    private static final long HOUR_IN_MILLIS = 60L * 60L * 1000L;
    private static final Random RANDOM = new Random();

    private GreenhouseMenuController() {
    }

    public static Result<String> plantPlot(int index) {
        Player player = AppModel.player;
        if (index < 0 || index >= player.greenhousePots.size()) {
            return Result.failure("This pot is locked or invalid. Unlock more pots in the shop");
        }
        Pot pot = player.greenhousePots.get(index);
        if (!pot.isEmpty) {
            return Result.failure("This pot already has a plant in it");
        }

        long now = TimeUtils.millis();

        // 50% chance for Marigold, 50% chance for an unlocked plant
        if (RANDOM.nextBoolean() || player.unlockedPlants == null || player.unlockedPlants.isEmpty()) {
            pot.isEmpty = false;
            pot.isMarigold = true;
            pot.targetPlant = null;
            pot.readyTimeMillis = now + (ConfigManager.economy().greenhouseMarigoldTime * HOUR_IN_MILLIS);
            new UserDatabase(player.username).save(player);
            return Result.success(
                "Planted a Marigold! It will be ready in " + ConfigManager.economy().greenhouseMarigoldTime + " hours"
            );
        } else {
            pot.isEmpty = false;
            pot.isMarigold = false;
            pot.targetPlant = player.unlockedPlants.get(RANDOM.nextInt(player.unlockedPlants.size()));
            pot.readyTimeMillis = now + (ConfigManager.economy().greenhousePlantTime * HOUR_IN_MILLIS);
            new UserDatabase(player.username).save(player);
            return Result.success("Planted a " + pot.targetPlant.toString() + "!");
        }
    }

    public static Result<String> collectPot(int index) {
        Player player = AppModel.player;
        if (index < 0 || index >= player.greenhousePots.size()) return Result.failure("This pot is locked");

        Pot pot = player.greenhousePots.get(index);
        if (pot.isEmpty) return Result.failure("This pot is empty");
        if (TimeUtils.millis() < pot.readyTimeMillis) return Result.failure("The plant is not ready yet");

        String resultMessage;
        if (pot.isMarigold) {
            player.coins += ConfigManager.economy().greenhouseMarigoldCoins;
            resultMessage = "Harvested a Marigold! You received "
                + ConfigManager.economy().greenhouseMarigoldCoins + " coins";
        } else {
            if (player.plantBoosts == null) player.plantBoosts = new java.util.HashMap<>();
            player.plantBoosts.put(pot.targetPlant, true);
            resultMessage = "Harvested a " + pot.targetPlant.toString() + "! A boost has been stored";
        }

        // Reset pot
        pot.isEmpty = true;
        pot.isMarigold = false;
        pot.targetPlant = null;
        pot.readyTimeMillis = 0;

        new UserDatabase(player.username).save(player);
        return Result.success(resultMessage);
    }

    public static Result<String> growPot(int index) {
        Player player = AppModel.player;
        if (index < 0 || index >= player.greenhousePots.size()) return Result.failure("This pot is locked");

        Pot pot = player.greenhousePots.get(index);
        if (pot.isEmpty) return Result.failure("This pot is empty");

        long now = TimeUtils.millis();
        if (now >= pot.readyTimeMillis) return Result.failure("This plant is already fully grown!");

        long diff = pot.readyTimeMillis - now;
        int costInDiamonds = (int) Math.ceil((double) diff / HOUR_IN_MILLIS);
        if (player.diamonds < costInDiamonds) {
            return Result.failure("Insufficient diamonds. You need " + costInDiamonds + " diamonds");
        }

        player.diamonds -= costInDiamonds;
        pot.readyTimeMillis = now; // Instantly ready

        new UserDatabase(player.username).save(player);
        return Result.success("Accelerated growth for " + costInDiamonds + " diamonds!");
    }
}
