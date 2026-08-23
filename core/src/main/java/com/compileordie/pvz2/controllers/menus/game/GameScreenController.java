package com.compileordie.pvz2.controllers.menus.game;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.controllers.PlantSpawner;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.PlantTemplate;
import com.compileordie.pvz2.models.game.board.Tile;
import com.compileordie.pvz2.models.game.economy.PlantCard;
import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.models.repositories.configs.PlantConfigRepository;
import com.compileordie.pvz2.views.helpers.ToastManager;

public class GameScreenController {
    public static PlantCard selectedCard = null;
    public static boolean isShovelSelected = false;
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

    public static void selectCard(PlantCard card) {
        isShovelSelected = false;
        if (selectedCard == card) {
            selectedCard = null; // Toggle off if clicked again
        } else {
            selectedCard = card;
        }
    }

    public static void toggleShovel() {
        isShovelSelected = !isShovelSelected;
        if (isShovelSelected) {
            selectedCard = null;
        }
    }

    public static void cancelSelection() {
        selectedCard = null;
        isShovelSelected = false;
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

        if (isShovelSelected) {
            handleShovelAction(tile);
            return;
        }

        if (selectedCard != null) {
            handlePlantAction(tile);
        }
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

        boolean isConveyor = AppModel.currentLevel == LevelID.CONVEYOR_BELT;
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

        Plant newPlant = PlantSpawner.spawn(selectedCard.plantType, spawnX, spawnY, selectedCard.isBoosted, false);
        if (newPlant == null) return;
        tile.plant = newPlant;

        if (isConveyor) {
            AppModel.gameSession.gameBoard.economyManager.plantCards.remove(selectedCard);
        } else {
            AppModel.gameSession.gameBoard.economyManager.sunAmount -= cost;
            selectedCard.setTimer();
        }

        cancelSelection();
    }
}
