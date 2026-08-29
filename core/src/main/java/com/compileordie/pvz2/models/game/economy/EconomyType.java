package com.compileordie.pvz2.models.game.economy;

import com.badlogic.gdx.math.MathUtils;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;
import com.compileordie.pvz2.models.repositories.configs.PlantConfigRepository;

import java.util.ArrayList;

public enum EconomyType {
    STANDARD {
        @Override
        public void tick(int ticks, EconomyManager self, GameBoard gameBoard) {
            if (self.tickCounter == 0) {
                loadSelectionDeckToPlantCards(self);
            }
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
            int newPlantTicks =
                (int) Math.floor(ConfigManager.economy().conveyorBeltPlantTimer / Constants.Game.TIME_COEFFICIENT);

            if (self.tickCounter % newPlantTicks == 0) {
                ArrayList<PlantType> availablePlantTypes = new ArrayList<>(self.selectionDeck.keySet());
                PlantType plantType = availablePlantTypes.get(MathUtils.random(availablePlantTypes.size() - 1));
                self.plantCards.add(new PlantCard(plantType));
            }
        }
    },
    VASE_BREAKER {
        @Override
        public void tick(int ticks, EconomyManager self, GameBoard gameBoard) {
        }
    },
    PLANT_WHAT_YOU_GET {
        @Override
        public void tick(int ticks, EconomyManager self, GameBoard gameBoard) {
            if (self.tickCounter == 0) {
                self.sunAmount += ConfigManager.economy().pwygStartingSuns;
                loadSelectionDeckToPlantCards(self);
            }
            for (PlantCard plantCard : self.plantCards) {
                plantCard.resetTimer();
            }
        }
    },
    I_ZOMBIE {
        @Override
        public void tick(int ticks, EconomyManager self, GameBoard gameBoard) {
            if (self.tickCounter == 0) {
                self.sunAmount += ConfigManager.economy().izStartingSuns;
            }
        }
    },
    NIGHT {
        @Override
        public void tick(int ticks, EconomyManager self, GameBoard gameBoard) {
            if (self.tickCounter == 0) {
                loadSelectionDeckToPlantCards(self);
            }
        }
    };

    abstract public void tick(int ticks, EconomyManager self, GameBoard gameBoard);

    private static void loadSelectionDeckToPlantCards(EconomyManager self) {
        PlantConfigRepository plantConfigRepository = new PlantConfigRepository();
        plantConfigRepository.loadFromCSV(Constants.Paths.Configs.PLANTS);
        self.plantCards.clear();
        for (PlantType plantType : self.selectionDeck.keySet()) {
            self.plantCards.add(new PlantCard(plantType,
                plantConfigRepository.getTemplate(plantType).getRechargeTicks(),
                self.selectionDeck.get(plantType)));
        }
    }
}
