package com.compileordie.pvz2.models.game.economy;

import com.badlogic.gdx.math.MathUtils;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;

import java.util.ArrayList;

public class EconomyManager {
    public GameBoard gameBoard;
    public ArrayList<Sun> suns;
    public int sunAmount;
    private int tickCounter;
    private int ticksUntilNextNaturalSun;

    public EconomyManager(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
        this.suns = new ArrayList<>();
        this.sunAmount = 0;
        this.tickCounter = 0;
        this.ticksUntilNextNaturalSun = calculateNextSpawnIntervalTicks();
    }

    public void tick(int ticks) {
        // Handle structural drop counter countdowns
        ticksUntilNextNaturalSun -= ticks;
        if (ticksUntilNextNaturalSun <= 0) {
            spawnNaturalSun();
            ticksUntilNextNaturalSun = calculateNextSpawnIntervalTicks();
        }

        // Safely process active entity updates moving backwards
        for (int i = suns.size() - 1; i >= 0; i--) {
            Sun sun = suns.get(i);
            sun.tick(ticks, gameBoard);

            if (!sun.isAlive()) {
                suns.remove(i);
            }
        }

        tickCounter += ticks;
    }

    private int calculateNextSpawnIntervalTicks() {
        float time = this.tickCounter * Constants.Game.TIME_COEFFICIENT;
        float secondsInterval = Math.max(6 + 0.05f * time, 12f);
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

        // TODO: Might as well use zombie manager here.
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

                if (sun.type == SunType.RADIOACTIVE && sun.getY() > sun.GROUND_LEVEL) {
                    explode(sun);
                }

                suns.remove(i);
                return true;
            }
        }

        return false;
    }
}
