package com.compileordie.pvz2.models.game.economy;

import com.badlogic.gdx.math.MathUtils;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;

import java.util.ArrayList;

public enum EconomyType {
    STANDARD {
        @Override
        public void tick(int ticks, EconomyManager self, GameBoard gameBoard) {
            self.ticksUntilNextNaturalSun -= ticks;
            if (self.ticksUntilNextNaturalSun <= 0) {
                self.spawnNaturalSun();
                self.ticksUntilNextNaturalSun = self.calculateNextSpawnIntervalTicks();
            }
        }
    },
    CONVEYOR_BELT {
        @Override
        public void tick(int ticks, EconomyManager self, GameBoard gameBoard) {
        }
    },
    FIXED_SUN {
        @Override
        public void tick(int ticks, EconomyManager self, GameBoard gameBoard) {
            if (self.tickCounter == 0) {
                self.sunAmount += ConfigManager.economy().pwygStartingSuns;
            }
        }
    },
    NIGHT {
        @Override
        public void tick(int ticks, EconomyManager self, GameBoard gameBoard) {
            int newPlantTicks =
                (int) Math.floor(ConfigManager.economy().conveyorBeltPlantTimer / Constants.Game.TIME_COEFFICIENT);

            if (self.tickCounter % newPlantTicks == 0) {
                ArrayList<PlantType> availablePlantTypes = AppModel.player.unlockedPlants;
                PlantType plantType = availablePlantTypes.get(MathUtils.random(availablePlantTypes.size() - 1));
                self.plantCards.add(new PlantCard(plantType));
            }
        }
    };

    abstract public void tick(int ticks, EconomyManager self, GameBoard gameBoard);
}
