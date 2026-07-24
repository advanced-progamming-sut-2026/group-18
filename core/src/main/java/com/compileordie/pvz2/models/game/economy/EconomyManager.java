package com.compileordie.pvz2.models.game.economy;

import com.badlogic.gdx.math.MathUtils;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Tile;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;

import java.util.ArrayList;

public class EconomyManager {
    public GameBoard gameBoard;
    public EconomyType type;
    public ArrayList<Sun> suns;
    public ArrayList<PlantType> selectionDeck;
    public ArrayList<PlantCard> plantCards;
    public int sunAmount;
    public int totalSunsGenerated;
    public int ticksUntilNextNaturalSun;
    public int tickCounter;

    public EconomyManager(GameBoard gameBoard, EconomyType economyType, ArrayList<PlantType> selectionDeck) {
        this.gameBoard = gameBoard;
        this.type = economyType;
        this.suns = new ArrayList<>();
        this.selectionDeck = selectionDeck;
        this.plantCards = new ArrayList<>();
        this.sunAmount = 0;
        this.totalSunsGenerated = 0;
        this.tickCounter = 0;
        this.ticksUntilNextNaturalSun = calculateNextSpawnIntervalTicks();
    }

    public void tick(int ticks) {
        // Handled polymorphically by EconomyType enum (STANDARD handles sky suns)
        type.tick(ticks, this, gameBoard);

        // Safely process active entity updates moving backwards
        for (int i = suns.size() - 1; i >= 0; i--) {
            Sun sun = suns.get(i);
            sun.tick(ticks, gameBoard);

            if (!sun.isAlive()) {
                suns.remove(i);
            }
        }

        for (int t = plantCards.size() - 1; t >= 0; t--) {
            PlantCard plantCard = plantCards.get(t);
            plantCard.tick(ticks);
        }

        tickCounter += ticks;
    }

    public int calculateNextSpawnIntervalTicks() {
        float time = this.tickCounter * Constants.Game.TIME_COEFFICIENT;

        // Interval scales from 6s up to 12s cap over time
        float secondsInterval = Math.min(6 + 0.05f * time, 12f);

        // Apply player Difficulty Level modifier (higher DL -> longer interval)
        if (gameBoard != null && AppModel.player != null) {
            secondsInterval *= AppModel.player.getDLIncrease();
        }

        return (int) Math.floor(secondsInterval / Constants.Game.TIME_COEFFICIENT);
    }

    public void spawnNaturalSun() {
        // Choose a completely randomized horizontal grid column location
        float x = MathUtils.random(Constants.Game.BOARD_COLS / 3f, Constants.Game.BOARD_COLS);
        float y = MathUtils.random(Constants.Game.BOARD_ROWS, Constants.Game.BOARD_ROWS + 1);
        float ground = MathUtils.random(Constants.Game.BOARD_ROWS / 4f);

        // Generate probability parameters
        int rollout = MathUtils.random(100);
        SunType selectedType;

        if (rollout < 80) {
            selectedType = SunType.NORMAL; // 80%
        } else if (rollout < 95) {
            selectedType = SunType.SPECIAL; // 15%
        } else {
            selectedType = SunType.RADIOACTIVE; // 5%
        }

        Sun naturalSun = new Sun(x, y, selectedType, true, ground);
        suns.add(naturalSun);

        AppModel.addAfterPrompt("New " + selectedType +
            " sun is dropping at position (" + (int) x + ", " + (int) y + ")");
    }

    /**
     * Helper method executes a radioactive blast covering a 5x5 zombie area and a 3x3 plant area around the sun.
     */
    private void explode(Sun sun) {
        int row = sun.getTileRow();
        int column = sun.getTileColumn();

        ArrayList<Zombie> zombies = gameBoard.getAllZombies();
        int radioactiveSunZombieDamageAmount = ConfigManager.economy().radioactiveSunZombieDamageAmount;
        int zombieDamageRadius = ConfigManager.economy().radioactiveSunZombieDamageArea / 2;

        for (int i = zombies.size() - 1; i >= 0; i--) {
            Zombie zombie = zombies.get(i);
            boolean isInRangeX = Math.abs(row - zombie.getTileRow()) <= zombieDamageRadius;
            boolean isInRangeY = Math.abs(column - zombie.getTileColumn()) <= zombieDamageRadius;

            if (isInRangeX && isInRangeY) {
                zombie.takeDamage(radioactiveSunZombieDamageAmount, DamageType.EXPLOSIVE);
            }
        }

        ArrayList<Plant> plants = gameBoard.getAllPlants();
        int radioactiveSunPlantDamageAmount = ConfigManager.economy().radioactiveSunPlantDamageAmount;
        int plantDamageRadius = ConfigManager.economy().radioactiveSunPlantDamageRange / 2;

        for (int i = plants.size() - 1; i >= 0; i--) {
            Plant plant = plants.get(i);
            boolean isInRangeX = Math.abs(row - plant.getTileRow()) <= plantDamageRadius;
            boolean isInRangeY = Math.abs(column - plant.getTileColumn()) <= plantDamageRadius;

            if (isInRangeX && isInRangeY) {
                plant.takeDamage(radioactiveSunPlantDamageAmount);
            }
        }

        AppModel.addAfterPrompt("A radioactive sun exploded! BOOM!");
    }

    public boolean collect(float x, float y) {
        float reach = Constants.Game.TILE_SIZE / 4f;

        for (int i = suns.size() - 1; i >= 0; i--) {
            Sun sun = suns.get(i);
            boolean isInRangeX = Math.abs(x - sun.getX()) <= reach;
            boolean isInRangeY = Math.abs(y - sun.getY()) <= reach;

            if (isInRangeX && isInRangeY) {
                sunAmount += sun.type.value;
                totalSunsGenerated += sun.type.value;

                if (sun.type == SunType.RADIOACTIVE && sun.getY() > sun.groundLevel) {
                    explode(sun);
                }

                suns.remove(i);
                AppModel.addAfterPrompt("Sun collected at (" + (int) sun.getX() + ", " + (int) sun.getY() + ")");
                return true;
            }
        }

        return false;
    }

    /*public void plant(PlantType type, float x, float y) {
        for (PlantCard plantCard : plantCards) {
            if (plantCard.isReady() && plantCard.plantType == type) {
                // Conveyor mode bypasses sun cost check
                if (this.type != EconomyType.CONVEYOR_BELT && sunAmount < type.cost) {
                    AppModel.addAfterPrompt("Not enough sun to plant " + type);
                    return;
                }

                Tile tile = gameBoard.getTile(x, y);
                if (tile == null || tile.plant != null) {
                    AppModel.addAfterPrompt(String.format("Cannot plant at (%.2f, %.2f)!", x, y));
                    return;
                }

                // Deduct sun cost
                if (this.type != EconomyType.CONVEYOR_BELT) {
                    sunAmount -= type.cost;
                }

                // Instantiate and place plant entity
                Plant plant = type.createPlant(x, y);
                tile.plant = plant;
                gameBoard.addPlant(plant);

                // Start cooldown timer on card
                plantCard.use();

                AppModel.addAfterPrompt(type + " planted at (" + (int) x + ", " + (int) y + ")");
                break;
            }
        }
    }

    public void pluck(float x, float y) {
        Tile tile = gameBoard.getTile(x, y);
        if (tile == null) {
            AppModel.addAfterPrompt(String.format("Tile at (%.2f, %.2f) not found!", x, y));
            return;
        }

        Plant plant = tile.plant;
        if (plant == null) {
            AppModel.addAfterPrompt(String.format("Plant at (%.2f, %.2f) not found!", x, y));
            return;
        }

        PlantType plantType = plant.type;
        tile.plant = null;
        gameBoard.removePlant(plant);

        AppModel.addAfterPrompt(String.format("%s at (%.2f, %.2f) plucked!", plantType, x, y));
    }*/

    public void plant(PlantType type, float x, float y) {
        Plant plant = null;
        for (PlantCard plantCard : plantCards) {
            if (plantCard.isReady() && plantCard.plantType == type) {
                // TODO: Check if we have enough suns for that plant (if it's not conveyor belt).
                // Make the actual plant here, check if it's boosted, add it to gameboard, remove the card,
                // break out of the loop.
            }
        }
    }

    public void pluck(float x, float y) {
        Tile tile = gameBoard.getTile(x, y);
        if (tile == null) {
            AppModel.addAfterPrompt(String.format("Tile at (%.2f, %.2f) not found!", x, y));
            return;
        }
        Plant plant = tile.plant;
        if (plant == null) {
            AppModel.addAfterPrompt(String.format("Plant at (%.2f, %.2f) not found!", x, y));
            return;
        }
        tile.plant = null;
        // TODO: Add the plant type field and print the result:
        // AppModel.addAfterPrompt(String.format("%s at (%.2f, %.2f) plucked!", plant.type, x, y));
    }
}
