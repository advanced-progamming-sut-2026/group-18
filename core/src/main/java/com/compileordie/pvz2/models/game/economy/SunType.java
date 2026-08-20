package com.compileordie.pvz2.models.game.economy;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;

public enum SunType {
    TINY(5) { // Added for base Sun Bean
        @Override
        public void tick(int ticks, Sun self, GameBoard gameBoard) {}
    },
    SMALL(10) { // Added for upgraded Sun Bean
        @Override
        public void tick(int ticks, Sun self, GameBoard gameBoard) {}
    },
    NORMAL(ConfigManager.economy().normalSunValue) { // 25 Suns
        @Override
        public void tick(int ticks, Sun self, GameBoard gameBoard) {}
    },
    MEDIUM(50) { // Added for standard Sunflower & Sun-shroom Stage 2
        @Override
        public void tick(int ticks, Sun self, GameBoard gameBoard) {}
    },
    LARGE(75) { // Added for Primal Sunflower & Sun-shroom Stage 3
        @Override
        public void tick(int ticks, Sun self, GameBoard gameBoard) {}
    },
    SPECIAL(ConfigManager.economy().specialSunValue) { // 100 Suns
        @Override
        public void tick(int ticks, Sun self, GameBoard gameBoard) {}
    },
    RADIOACTIVE(ConfigManager.economy().radioactiveSunValue) {
        @Override
        public void tick(int ticks, Sun self, GameBoard gameBoard) {
            // If it hits the ground, it safely becomes a NORMAL (25) sun
            if (self.getY() > self.groundLevel || self.target != null) return;
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
