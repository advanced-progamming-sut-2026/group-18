package com.compileordie.pvz2.controllers.menus.game;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.controllers.AppController;
import com.compileordie.pvz2.controllers.PlantSpawner;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.StatusEffect;
import com.compileordie.pvz2.models.entities.zombies.ZombieBuilder;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.KnightZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.StandardZombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Lane;
import com.compileordie.pvz2.models.game.board.Tile;
import com.compileordie.pvz2.models.game.economy.EconomyManager;
import com.compileordie.pvz2.models.game.economy.EconomyType;
import com.compileordie.pvz2.models.game.economy.PlantCard;
import com.compileordie.pvz2.models.game.minigames.vasebreaker.Vase;
import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.views.helpers.Menu;

import java.util.ArrayList;
import java.util.StringJoiner;

public class GameSessionMenuController {
    private GameSessionMenuController() {
    }

    public static String advanceTime(String count) {
        int ticks;
        try {
            ticks = Integer.parseInt(count);
        } catch (NumberFormatException e) {
            return "[ERROR] You must enter a whole number.";
        }
        if (ticks <= 0) {
            return "[ERROR] You must enter a positive number.";
        }

        for (int i = 0; i < ticks; i++) {
            AppModel.gameSession.tick(1);
        }
        return "Advanced game for " + ticks + " tick" + (ticks == 1 ? "." : "s.");
    }

    public static String collectSun(String x, String y) {
        float xPosition, yPosition;
        try {
            xPosition = Float.parseFloat(x);
            yPosition = Float.parseFloat(y);
        } catch (NumberFormatException e) {
            return "[ERROR] You must enter valid numbers for x and y.";
        }
        AppModel.gameSession.gameBoard.economyManager.collect(xPosition, yPosition);
        return "";
    }

    public static String showSunAmount() {
        int suns = AppModel.gameSession.gameBoard.economyManager.sunAmount;
        return "You have " + suns + " sun" + (suns == 1 ? "." : "s.");
    }

    public static String cheatAddSun(String count) {
        int number;
        try {
            number = Integer.parseInt(count);
        } catch (NumberFormatException e) {
            return "[ERROR] You must enter a whole number.";
        }
        if (number <= 0) {
            return "[ERROR] You must enter a positive number.";
        }

        AppModel.gameSession.gameBoard.economyManager.sunAmount += number;
        return "Added " + number + " sun" + (number == 1 ? "" : "s") + "to your wallet.";
    }

    public static String releaseTheNuke() {
        ArrayList<Zombie> zombies = AppModel.gameSession.gameBoard.getAllZombies();
        for (Zombie zombie : zombies) {
            zombie.setHealth(0);
        }
        return "All zombies neutralized.";
    }

    public static String plantPlant(String type, String x, String y) {
        PlantType plant = PlantType.getByName(type);
        if (plant == null) {
            return "[ERROR] Invalid plant name.";
        }
        float xPosition, yPosition;
        try {
            xPosition = Float.parseFloat(x);
            yPosition = Float.parseFloat(y);
        } catch (NumberFormatException e) {
            return "[ERROR] You must enter valid numbers for x and y.";
        }
        Tile tile = AppModel.gameSession.gameBoard.getTile(xPosition, yPosition);
        if (tile == null) {
            return String.format("[ERROR] Tile not found at (%.1f, %.1f).", xPosition, yPosition);
        }
        if (!tile.isPlantable()) {
            return "[ERROR] Tile is not plantable.";
        }
        AppModel.gameSession.gameBoard.economyManager.plant(plant, xPosition, yPosition);
        return "";
    }

    public static String cheatRemoveCooldown() {
        for (PlantCard plantCard : AppModel.gameSession.gameBoard.economyManager.plantCards) {
            plantCard.resetTimer();
        }
        return "Removed all card cooldowns.";
    }

    public static String pluckPlant(String x, String y) {
        float xPosition, yPosition;
        try {
            xPosition = Float.parseFloat(x);
            yPosition = Float.parseFloat(y);
        } catch (NumberFormatException e) {
            return "[ERROR] You must enter valid numbers for x and y.";
        }
        AppModel.gameSession.gameBoard.economyManager.pluck(xPosition, yPosition);
        return "";
    }

    public static String feedPlant(String x, String y) {
        float xPosition, yPosition;
        try {
            xPosition = Float.parseFloat(x);
            yPosition = Float.parseFloat(y);
        } catch (NumberFormatException e) {
            return "[ERROR] You must enter valid numbers for x and y.";
        }
        Tile tile = AppModel.gameSession.gameBoard.getTile(xPosition, yPosition);
        if (tile == null) {
            return String.format("[ERROR] Tile not found at (%.1f, %.1f).", xPosition, yPosition);
        }
        Plant plant = tile.plant;
        if (plant == null) {
            return String.format("[ERROR] Tile found at (%.1f, %.1f) has no plant on it.", xPosition, yPosition);
        }
        if (plant.isFed) {
            return "[ERROR] Plant is already fed!";
        }
        plant.feed(AppModel.gameSession.gameBoard, AppModel.player);
        AppModel.player.plantFoodCount--;
        return String.format("Fed plant %s at (%.1f, %.1f).", plant.getName(), xPosition, yPosition);
    }

    public static String cheatAddPlantFood() {
        Player player = AppModel.player;
        if (player.plantFoodCount >= 3) {
            return "[ERROR] You already have maximum food possible (3/3).";
        }

        player.plantFoodCount++;
        return "Added 1 plant food to your wallet. (capacity " + player.plantFoodCount + "/3)";
    }

    public static String showMap() {
        GameBoard gameBoard = AppModel.gameSession.gameBoard;
        StringJoiner result = new StringJoiner(System.lineSeparator());
        result.add("Wave number: " + gameBoard.waveManager.waveNumber);
        result.add("Plant foods: " + AppModel.player.plantFoodCount);
        result.add("Sun amount: " + gameBoard.economyManager.sunAmount);
        result.add("");
        for (Tile tile : gameBoard.getAllTiles()) {
            result.add(String.format("Tile at row=%d, column=%d:", tile.row, tile.column));
            result.add("Type: " + tile.type.toString());
            if (!tile.getZombies().isEmpty()) {
                result.add("Zombie:");
                for (Zombie zombie : tile.getZombies()) {
                    result.add(String.format("%s at (%.1f, %.1f)",
                        zombie.getType().toString(),
                        zombie.getX(),
                        zombie.getY()));
                }
            } else {
                result.add("Has no zombies");
            }
            if (tile.plant != null) {
                result.add("Plant: " + tile.plant.getName());
            } else {
                result.add("Doesn't have plant");
            }
            if (tile.obstacle != null) {
                result.add("Obstacle: " + tile.obstacle.type);
            } else {
                result.add("Doesn't have obstacle");
            }
            result.add("");
        }
        for (Lane lane : gameBoard.lanes) {
            if (lane.lawnMower.isTriggered) {
                result.add("Lawn mower at row=" + lane.row + " is triggered");
            } else {
                result.add("Lawn mower at row=" + lane.row + " has not been triggered");
            }
        }
        return result.toString();
    }

    public static String showPlantsStatus() {
        EconomyManager economyManager = AppModel.gameSession.gameBoard.economyManager;
        StringJoiner result = new StringJoiner(System.lineSeparator());
        for (PlantCard plantCard : economyManager.plantCards) {
            result.add("Plant: " + plantCard.plantType);
            if (economyManager.type != EconomyType.CONVEYOR_BELT) {
                result.add("Price: " + PlantSpawner.spawn(plantCard.plantType).getCost());
            }
            if (plantCard.isReady()) {
                result.add("Is ready to plant");
            } else {
                result.add("Remaining cooldown timer: " + plantCard.timer);
            }
        }
        return result.toString();
    }

    public static String showTilesStatus(String x, String y) {
        float xPosition, yPosition;
        try {
            xPosition = Float.parseFloat(x);
            yPosition = Float.parseFloat(y);
        } catch (NumberFormatException e) {
            return "[ERROR] You must enter valid numbers for x and y.";
        }
        Tile tile = AppModel.gameSession.gameBoard.getTile(xPosition, yPosition);
        if (tile == null) {
            return String.format("[ERROR] Tile not found at (%.1f, %.1f).", xPosition, yPosition);
        }
        StringJoiner result = new StringJoiner(System.lineSeparator());
        result.add(String.format("Tile at row=%d, column=%d:", tile.row, tile.column));
        if (!tile.getZombies().isEmpty()) {
            result.add("Zombie:");
            for (Zombie zombie : tile.getZombies()) {
                result.add("Type: " + zombie.getType());
                result.add("Health: " + zombie.getHealth());
                result.add("Attack: " + zombie.getAttackPower());
            }
        } else {
            result.add("Has no zombies");
        }
        if (tile.plant != null) {
            Plant plant = tile.plant;
            result.add("Plant:");
            result.add("Type: " + plant.getName());
            result.add("Health: " + plant.getCurrentHp());
            result.add("Level: " + plant.getLevel());
        } else {
            result.add("Doesn't have plant");
        }
        return result.toString();
    }

    public static String startZombieWaves() {
        AppModel.gameSession.gameBoard.waveManager.shouldStartWaves = true;
        return "Buckle up mate, Starting zombie waves.";
    }

    public static String zombieInfo() {
        StringJoiner result = new StringJoiner(System.lineSeparator());
        for (Zombie zombie : AppModel.gameSession.gameBoard.getAllZombies()) {
            result.add(zombie.getType() + ":");
            result.add("    position: " + zombie.getTileRow() + ", " + zombie.getTileColumn());
            result.add("    health: " + zombie.getHealth());
            result.add("    armor:");
            if (zombie instanceof StandardZombie standardZombie) {
                result.add("        crown: " + standardZombie.getArmorHealth());
            }
            if (zombie instanceof KnightZombie knightZombie) {
                result.add("        shoulderArmor: " + knightZombie.shoulderArmorHealth);
            }
            result.add("    effects:");
            for (StatusEffect effect : zombie.getActiveEffects()) {
                result.add("        " + effect.getEffectType() + ": " + effect.getRemainingTime() + "s");
            }
        }
        return result.toString();
    }

    public static String spawnZombie(String type, String x, String y) {
        ZombieType zombieType = ZombieType.getByName(type);
        if (zombieType == null) {
            return "[ERROR] Unknown zombie type";
        }
        float xPosition, yPosition;
        try {
            xPosition = Float.parseFloat(x);
            yPosition = Float.parseFloat(y);
        } catch (NumberFormatException e) {
            return "[ERROR] You must enter valid numbers for x and y.";
        }
        Tile tile = AppModel.gameSession.gameBoard.getTile(xPosition, yPosition);
        if (tile == null) {
            return String.format("[ERROR] Tile not found at (%.1f, %.1f).", xPosition, yPosition);
        }

        Zombie zombie = ZombieBuilder.create(zombieType,
            xPosition,
            yPosition,
            (int) Math.floor(yPosition / Constants.Game.TILE_HEIGHT));
        AppModel.gameSession.gameBoard.lanes.get(zombie.getTileRow()).zombies.add(zombie);
        return String.format("Spawned %s at (%.1f, %.1f).", zombieType, xPosition, yPosition);
    }

    public static String quitGame() {
        AppModel.clearSessionData();
        return "Mission aborted!" + System.lineSeparator() + AppController.changeMenu(Menu.GAME);
    }

    public static String breakVase(String x, String y) {
        float xPosition, yPosition;
        try {
            xPosition = Float.parseFloat(x);
            yPosition = Float.parseFloat(y);
        } catch (NumberFormatException e) {
            return "[ERROR] You must enter valid numbers for x and y.";
        }
        Vase selectedVase = null;
        for (Vase vase : AppModel.gameSession.gameBoard.vases) {
            boolean isInRangeX = Math.abs(vase.getX() - xPosition) <= (Constants.Game.TILE_WIDTH / 2);
            boolean isInRangeY = Math.abs(vase.getY() - yPosition) <= (Constants.Game.TILE_HEIGHT / 2);
            if (isInRangeX && isInRangeY) {
                selectedVase = vase;
                break;
            }
        }
        if (selectedVase == null) {
            return "[ERROR] You must select a vase.";
        }
        selectedVase.breakVase();
        return "";
    }
}
