package com.compileordie.pvz2.controllers.menus.game;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.controllers.PlantSpawner;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.PlantTemplate;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Tile;
import com.compileordie.pvz2.models.game.economy.EconomyType;
import com.compileordie.pvz2.models.game.economy.PlantCard;
import com.compileordie.pvz2.models.game.economy.Sun;
import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.models.game.minigames.vasebreaker.Vase;
import com.compileordie.pvz2.models.missions.quests.QuestEvent;
import com.compileordie.pvz2.models.missions.quests.QuestManager;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;
import com.compileordie.pvz2.models.repositories.configs.PlantConfigRepository;
import com.compileordie.pvz2.views.helpers.ToastManager;

import java.util.ArrayList;

public class GameScreenController {
    public static PlantCard selectedCard = null;
    public static boolean isShovelSelected = false;
    public static boolean isPlantFoodSelected = false;
    private static PlantConfigRepository configRepo;

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

    public static void selectCard(PlantCard card) {
        isShovelSelected = false;
        isPlantFoodSelected = false;
        if (selectedCard == card) {
            selectedCard = null;
        } else {
            selectedCard = card;
        }
    }

    public static void toggleShovel() {
        isShovelSelected = !isShovelSelected;
        if (isShovelSelected) {
            selectedCard = null;
            isPlantFoodSelected = false;
        }
    }

    public static void togglePlantFood() {
        isPlantFoodSelected = !isPlantFoodSelected;
        if (isPlantFoodSelected) {
            selectedCard = null;
            isShovelSelected = false;
        }
    }

    public static void cancelSelection() {
        selectedCard = null;
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

        if (isPlantFoodSelected) {
            handlePlantFoodAction(tile);
            return;
        }

        if (isShovelSelected) {
            handleShovelAction(tile);
            return;
        }

        if (selectedCard != null) {
            handlePlantAction(tile);
        }
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
        if (tile.plant != null && tile.plant.isAlive()) {
            ToastManager.showError("Tile is already occupied!");
            return;
        }
        if (!tile.isPlantable()) {
            ToastManager.showError("Tile is not plantable!");
            return;
        }
        if (AppModel.currentLevel == LevelID.WALNUT_BOWLING
            && tile.column >= Constants.Game.BOARD_COLS - ConfigManager.gameplay().bowlingLine) {
            ToastManager.showError("You can't plant past the line!");
            return;
        }

        boolean isConveyor = AppModel.gameSession.gameBoard.economyManager.type == EconomyType.CONVEYOR_BELT
            || AppModel.currentLevel == LevelID.VASE_BREAKER;
        PlantTemplate template = getConfigRepo().getTemplate(selectedCard.plantType);
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

        if (selectedCard.plantType == PlantType.PEA_POD
            && tile.plant != null
            && tile.plant.getName().equals("Pea Pod")) {
            int currentHeads = tile.plant.getStackCount();
            if (currentHeads < 5) {
                tile.plant.addStack();
            } else {
                ToastManager.showError("Pea Pod already had 5 heads!");
            }
        } else if (selectedCard.plantType == PlantType.HOT_POTATO) {
            // TODO: To be implemented.
        } else if (selectedCard.plantType == PlantType.GRAVE_BUSTER) {
            // TODO: To be implemented.
        } else if (selectedCard.plantType == PlantType.LILY_PAD) {
            // TODO: To be implemented.
        } else {
            Plant newPlant = PlantSpawner.spawn(selectedCard.plantType, spawnX, spawnY, selectedCard.isBoosted, false);
            tile.plant = newPlant;
        }
        QuestManager.dispatch(QuestEvent.PLANT_PLANTED, 1, AppModel.currentChapter.name());

        if (isConveyor) {
            AppModel.gameSession.gameBoard.economyManager.plantCards.remove(selectedCard);
        } else {
            AppModel.gameSession.gameBoard.economyManager.sunAmount -= cost;
            selectedCard.setTimer();
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
