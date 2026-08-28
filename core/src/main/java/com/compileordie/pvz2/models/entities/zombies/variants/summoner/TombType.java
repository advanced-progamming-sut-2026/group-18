package com.compileordie.pvz2.models.entities.zombies.variants.summoner;

import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.economy.PlantFood;
import com.compileordie.pvz2.models.game.economy.Sun;
import com.compileordie.pvz2.models.game.economy.SunType;

public enum TombType {
    NORMAL {
        @Override
        public void dieSpawn(Tomb self, GameBoard gameBoard) {
            // Does nothing when destroyed
        }
    },
    SUN {
        @Override
        public void dieSpawn(Tomb self, GameBoard gameBoard) {
            gameBoard.economyManager.suns.add(new Sun(self.getX(),
                self.getY(),
                SunType.MEDIUM,
                false,
                (float) self.getY()));
        }
    },
    PLANT_FOOD {
        @Override
        public void dieSpawn(Tomb self, GameBoard gameBoard) {
            gameBoard.plantFoods.add(new PlantFood(self.getX(), self.getY()));
        }
    };

    abstract public void dieSpawn(Tomb self, GameBoard gameBoard);
}
