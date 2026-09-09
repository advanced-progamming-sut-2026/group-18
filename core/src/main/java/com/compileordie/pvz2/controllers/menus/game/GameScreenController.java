package com.compileordie.pvz2.controllers.menus.game;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.controllers.PlantSpawner;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.obstacles.IceBlock;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.PlantTemplate;
import com.compileordie.pvz2.models.entities.plants.enums.ProjectileType;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.Tomb;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Tile;
import com.compileordie.pvz2.models.game.economy.EconomyType;
import com.compileordie.pvz2.models.game.economy.PlantCard;
import com.compileordie.pvz2.models.game.economy.Sun;
import com.compileordie.pvz2.models.game.economy.ZombieCard;
import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.models.game.minigames.vasebreaker.Vase;
import com.compileordie.pvz2.models.missions.quests.QuestEvent;
import com.compileordie.pvz2.models.missions.quests.QuestManager;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;
import com.compileordie.pvz2.models.repositories.configs.PlantConfigRepository;
import com.compileordie.pvz2.network.NetworkClient;
import com.compileordie.pvz2.network.protocol.Message;
import com.compileordie.pvz2.network.protocol.MessageType;
import com.compileordie.pvz2.views.helpers.ToastManager;

import java.util.ArrayList;
import java.util.List;

public class GameScreenController {
    public static PlantCard selectedPlantCard = null;
    public static boolean isShovelSelected = false;
    public static boolean isPlantFoodSelected = false;
    private static PlantConfigRepository configRepo;
    public static ZombieCard selectedZombieCard = null;

    private GameScreenController() {
    }

    private static PlantConfigRepository getConfigRepo() {
        if (configRepo == null) {
            configRepo = new PlantConfigRepository();
            try {
                configRepo.loadFromCSV(Constants.Paths.Configs.PLANTS);
            } catch (Throwable ignored) {
            }
        }
        return configRepo;
    }

    public static Tile getTileAt(float screenX, float screenY, Viewport viewport) {
        if (AppModel.gameSession == null || AppModel.gameSession.gameBoard == null) return null;
        Vector3 worldPoint = new Vector3(screenX, screenY, 0);
        viewport.unproject(worldPoint);

        float meterX = worldPoint.x / Constants.UI.METER_TO_PIX;
        float meterY = worldPoint.y / Constants.UI.METER_TO_PIX;

        return AppModel.gameSession.gameBoard.getTile(meterX, meterY);
    }

    public static Vase getVaseAt(float screenX, float screenY, Viewport viewport) {
        Tile tile = getTileAt(screenX, screenY, viewport);
        if (tile == null) return null;
        return tile.vase;
    }

    public static void selectPlantCard(PlantCard card) {
        isShovelSelected = false;
        isPlantFoodSelected = false;
        if (selectedPlantCard == card) {
            selectedPlantCard = null;
        } else {
            selectedPlantCard = card;
        }
    }

    public static void toggleShovel() {
        isShovelSelected = !isShovelSelected;
        if (isShovelSelected) {
            selectedPlantCard = null;
            isPlantFoodSelected = false;
        }
    }

    public static void togglePlantFood() {
        isPlantFoodSelected = !isPlantFoodSelected;
        if (isPlantFoodSelected) {
            selectedPlantCard = null;
            isShovelSelected = false;
        }
    }

    public static void cancelSelection() {
        selectedPlantCard = null;
        selectedZombieCard = null;
        isShovelSelected = false;
        isPlantFoodSelected = false;
    }

    public static void reset() {
        cancelSelection();
    }

    public static void handleTileClick(Tile tile) {
        if (tile == null) {
            cancelSelection();
            return;
        }
        if (AppModel.gameSession == null || AppModel.gameSession.gameBoard == null) return;

        if (AppModel.currentLevel == LevelID.I_ZOMBIE && AppModel.isReceiverClient) {
            handleIZombieTileClick(tile);
            return;
        }

        if (isPlantFoodSelected) {
            handlePlantFoodAction(tile);
            return;
        }

        if (isShovelSelected) {
            handleShovelAction(tile);
            return;
        }

        if (selectedPlantCard != null) {
            handlePlantAction(tile);
        }
    }

    public static void handleIZombieTileClick(Tile tile) {
        if (selectedZombieCard == null) return;

        var economy = AppModel.gameSession.gameBoard.economyManager;

        if (economy.sunAmount < selectedZombieCard.cost) {
            ToastManager.showError("Not enough sun!");
            return;
        }

        if (tile.column < Constants.Game.BOARD_COLS - 3) {
            ToastManager.showError("Zombies must be placed on the right side of the lawn!");
            return;
        }

        // Send targeted spawn request to the opponent
        Message spawnMessage = new Message(MessageType.IZOMBIE_SPAWN_REQUEST)
            .put("target", AppModel.opponentUsername)
            .put("zombieType", selectedZombieCard.zombieType.name())
            .put("row", String.valueOf(tile.row))
            .put("col", String.valueOf(tile.column))
            .put("cost", String.valueOf(selectedZombieCard.cost));

        NetworkClient.getInstance().send(spawnMessage);
        selectedZombieCard.resetTimer();
        cancelSelection();
    }

    public static void handleVaseClick(Vase vase) {
        vase.breakVase();
    }

    private static void handlePlantFoodAction(Tile tile) {
        if (tile.plant == null || !tile.plant.isAlive()) {
            ToastManager.showError("No plant to feed!");
            return;
        }

        if (AppModel.player == null || AppModel.player.plantFoodCount <= 0) {
            ToastManager.showError("No Plant Food left!");
            return;
        }

        AppModel.player.consumePlantFood();
        tile.plant.feed(AppModel.gameSession.gameBoard, AppModel.player);
        cancelSelection();
    }

    private static void handleShovelAction(Tile tile) {
        if (tile.plant != null && tile.plant.isAlive()) {
            tile.plant = null;
            cancelSelection();
        }
    }

    private static void handlePlantAction(Tile tile) {
        // --- 1. SPECIAL EXCEPTION PLANTING RULES ---
        if (selectedPlantCard.plantType == PlantType.HOT_POTATO) {
            boolean hasIceBlock = tile.obstacle instanceof IceBlock;
            // FIX: Check ALL slots for the ice cover!
            boolean hasFrozenPlant = (tile.plant != null && tile.plant.isFrozen())
                || (tile.pumpkin != null && tile.pumpkin.isFrozen())
                || (tile.lilyPad != null && tile.lilyPad.isFrozen());

            if (!hasIceBlock && !hasFrozenPlant) {
                ToastManager.showError("Hot Potato can only be planted on Ice!");
                return;
            }
        } else if (selectedPlantCard.plantType == PlantType.GRAVE_BUSTER) {
            if (!(tile.obstacle instanceof Tomb)) {
                ToastManager.showError("Grave Buster can only be planted on Graves!");
                return;
            }
            boolean isConveyor = AppModel.gameSession.gameBoard.economyManager.type == EconomyType.CONVEYOR_BELT
                || AppModel.currentLevel == LevelID.VASE_BREAKER;
            PlantTemplate template = getConfigRepo().getTemplate(selectedPlantCard.plantType);
            int cost = template != null ? template.getCost() : 0;

            if (!isConveyor && AppModel.gameSession.gameBoard.economyManager.sunAmount < cost) {
                ToastManager.showError("Not enough sun!");
                return;
            }

            // 1. Deduct Sun / Handle Conveyor
            if (isConveyor) {
                AppModel.gameSession.gameBoard.economyManager.plantCards.remove(selectedPlantCard);
            } else {
                AppModel.gameSession.gameBoard.economyManager.sunAmount -= cost;
                selectedPlantCard.setTimer();
            }
            ((Tomb) tile.obstacle).takeDamage(9999, ProjectileType.NORMAL);
            ToastManager.showMessage("Grave Buster instantly consumed the tomb!");

            cancelSelection();
            return; // EXIT COMPLETELY. DO NOT CALL spawnAndDeduct!
        } else if (selectedPlantCard.plantType == PlantType.LILY_PAD) {
            if (!tile.isUnderWater()) {
                ToastManager.showError("Lily Pad must be planted on Water!");
                return;
            }
            if (tile.hasLilyPad()) {
                ToastManager.showError("Tile already has a Lily Pad!");
                return;
            }
        } else if (selectedPlantCard.plantType == PlantType.PUMPKIN) {
            if (tile.hasPumpkin()) {
                ToastManager.showError("Tile already has a Pumpkin!");
                return;
            }
        }
        else if (selectedPlantCard.plantType == PlantType.SEA_SHROOM || selectedPlantCard.plantType == PlantType.TANGLE_KELP) {
            if (!tile.isUnderWater()) {
                ToastManager.showError("This plant can only be planted in water!");
                return;
            }
            if (tile.hasLilyPad()) {
                ToastManager.showError("Aquatic plants don't need a Lily Pad!");
                return;
            }
            if (tile.plant != null && tile.plant.isAlive()) {
                ToastManager.showError("Tile is already occupied!");
                return;
            }
        }

        // --- 2. NORMAL PLANTING RULES ---
        if (!List.of(PlantType.PEA_POD,
                PlantType.HOT_POTATO,
                PlantType.GRAVE_BUSTER,
                PlantType.LILY_PAD,
                PlantType.SEA_SHROOM,
                PlantType.TANGLE_KELP,
                PlantType.PUMPKIN)
            .contains(selectedPlantCard.plantType)) {
            if (tile.plant != null && tile.plant.isAlive()) {
                ToastManager.showError("Tile is already occupied!");
                return;
            }
            if (!tile.isPlantable()) {
                ToastManager.showError("Tile is not plantable!");
                return;
            }
        }
        if (AppModel.currentLevel == LevelID.WALNUT_BOWLING
            && tile.column >= Constants.Game.BOARD_COLS - ConfigManager.gameplay().bowlingLine) {
            ToastManager.showError("You can't plant past the line!");
            return;
        }

        boolean isConveyor = AppModel.gameSession.gameBoard.economyManager.type == EconomyType.CONVEYOR_BELT
            || AppModel.currentLevel == LevelID.VASE_BREAKER;
        PlantTemplate template = getConfigRepo().getTemplate(selectedPlantCard.plantType);
        int cost = template != null ? template.getCost() : 0;

        if (!isConveyor && AppModel.gameSession.gameBoard.economyManager.sunAmount < cost) {
            ToastManager.showError("Not enough sun!");
            return;
        }

        spawnAndDeduct(tile, cost, isConveyor);
    }

    private static void spawnAndDeduct(Tile tile, int cost, boolean isConveyor) {
        float spawnX = tile.column * Constants.Game.TILE_WIDTH + Constants.Game.PADDING_X
            + (Constants.Game.TILE_WIDTH / 2f);
        float spawnY = tile.row * Constants.Game.TILE_HEIGHT + Constants.Game.PADDING_Y
            + (Constants.Game.TILE_HEIGHT / 2f);

        if (selectedPlantCard.plantType == PlantType.PEA_POD
            && tile.plant != null
            && tile.plant.getName().equals("Pea Pod")) {
            int currentHeads = tile.plant.getStackCount();
            if (currentHeads < 5) {
                tile.plant.addStack();
            } else {
                ToastManager.showError("Pea Pod already had 5 heads!");
            }
        } else {
            Plant newPlant = PlantSpawner.spawn(
                selectedPlantCard.plantType,
                spawnX,
                spawnY,
                selectedPlantCard.isBoosted,
                false
            );

            tile.plant = newPlant;
            AppModel.gameSession.gameBoard.recordPlanting(selectedPlantCard.plantType, tile);
            tile.plant = null; // Remove it from the main slot immediately after!

            // --- NOW ROUTE TO THE CORRECT SLOT ---
            if (selectedPlantCard.plantType == PlantType.LILY_PAD) {
                tile.lilyPad = newPlant;
            } else if (selectedPlantCard.plantType == PlantType.PUMPKIN) {
                tile.pumpkin = newPlant;
            } else if (selectedPlantCard.plantType == PlantType.HOT_POTATO
                || selectedPlantCard.plantType == PlantType.GRAVE_BUSTER) {
                tile.instantPlant = newPlant;
            } else {
                tile.plant = newPlant;
            }
            // Feeds Master Demolisher / Cloudy Day / Night or Morning / Family Slayer /
            // One Less Column / Defenseless Row / Defenseless Cross.
            AppModel.gameSession.gameBoard.recordPlanting(selectedPlantCard.plantType, tile);
        }
        QuestManager.dispatch(QuestEvent.PLANT_PLANTED, 1, AppModel.currentChapter.name());

        if (isConveyor) {
            AppModel.gameSession.gameBoard.economyManager.plantCards.remove(selectedPlantCard);
        } else {
            AppModel.gameSession.gameBoard.economyManager.sunAmount -= cost;
            selectedPlantCard.setTimer();
        }

        cancelSelection();
    }

    public static void explodeSun(GameBoard gameBoard, Sun sun) {
        ArrayList<Zombie> zombies = gameBoard.getAllZombies();
        int zombieDamageAmount = ConfigManager.economy().radioactiveSunZombieDamageAmount;
        int zombieDamageRadius = ConfigManager.economy().radioactiveSunZombieDamageArea;

        for (int i = zombies.size() - 1; i >= 0; i--) {
            Zombie zombie = zombies.get(i);
            boolean isInRangeX = Math.abs(sun.getX() - zombie.getX())
                <= zombieDamageRadius * Constants.Game.TILE_WIDTH;
            boolean isInRangeY = Math.abs(sun.getY() - zombie.getY())
                <= zombieDamageRadius * Constants.Game.TILE_HEIGHT;

            if (isInRangeX && isInRangeY) {
                zombie.takeDamage(zombieDamageAmount, DamageType.EXPLOSIVE);
            }
        }

        ArrayList<Plant> plants = gameBoard.getAllPlants();
        int plantDamageAmount = ConfigManager.economy().radioactiveSunPlantDamageAmount;
        int plantDamageRadius = ConfigManager.economy().radioactiveSunPlantDamageRange;

        for (int i = plants.size() - 1; i >= 0; i--) {
            Plant plant = plants.get(i);
            boolean isInRangeX = Math.abs(sun.getX() - plant.getX())
                <= plantDamageRadius * Constants.Game.TILE_WIDTH;
            boolean isInRangeY = Math.abs(sun.getY() - plant.getY())
                <= plantDamageRadius * Constants.Game.TILE_HEIGHT;

            if (isInRangeX && isInRangeY) {
                plant.takeDamage(plantDamageAmount);
            }
        }

        ToastManager.showMessage("A radioactive sun exploded! BOOM!");
    }
}
