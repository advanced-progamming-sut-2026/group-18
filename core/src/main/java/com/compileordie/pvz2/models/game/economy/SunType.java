package com.compileordie.pvz2.models.game.economy;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;

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
            if (self.getY() > self.GROUND_LEVEL || self.target != null) return;

            self.type = SunType.NORMAL;
            AppModel.addAfterPrompt("RadioActive sun became a normal sun.");

        }
    };

    public final int value;

    SunType(int value) {
        this.value = value;
    }

    abstract public void tick(int ticks, Sun self, GameBoard gameBoard);

    @Override
    public String toString() {
        return this.name().toLowerCase().replace('_', ' ');
    }
}
