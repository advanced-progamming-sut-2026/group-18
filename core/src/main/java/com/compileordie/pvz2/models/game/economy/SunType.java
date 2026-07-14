package com.compileordie.pvz2.models.game.economy;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.variants.Plant;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;

import java.util.ArrayList;

public enum SunType {
    NORMAL(ConfigManager.economy().normalSunValue) {
        @Override
        public void tick(int ticks, Sun self, GameBoard gameBoard) {
        }
    },
    SPECIAL(ConfigManager.economy().specialSunValue) {
        @Override
        public void tick(int ticks, Sun self, GameBoard gameBoard) {
        }
    },
    RADIOACTIVE(ConfigManager.economy().radioactiveSunValue) {
        @Override
        public void tick(int ticks, Sun self, GameBoard gameBoard) {
            if (self.getY() > self.GROUND_LEVEL) return;

            self.type = SunType.NORMAL;
            explode(self, gameBoard);
            AppModel.addAfterPrompt("RadioActive sun reached the ground and became a normal sun.");

        }
    };

    public final int value;

    SunType(int value) {
        this.value = value;
    }

    abstract public void tick(int ticks, Sun self, GameBoard gameBoard);

    /**
     * Executes a radioactive blast covering a 5x5 zombie area and a 3x3 plant area centered around the sun's cell.
     */
    public void explode(Sun self, GameBoard gameBoard) {
        int row = self.getTileRow();
        int column = self.getTileColumn();

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

    @Override
    public String toString() {
        return this.name().toLowerCase().replace('_', ' ');
    }
}
