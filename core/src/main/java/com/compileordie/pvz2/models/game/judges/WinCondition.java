package com.compileordie.pvz2.models.game.judges;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;

public enum WinCondition {
    STANDARD {
        @Override
        public boolean evaluate(GameBoard gameBoard) {
            return gameBoard.tickCounter * Constants.Game.TILE_HEIGHT >= 10
                && gameBoard.waveManager.pendingZombieQueue.isEmpty()
                && gameBoard.getAllZombies().isEmpty();
            /*return gameBoard.waveManager.isLastWave()
                && gameBoard.waveManager.pendingZombieQueue.isEmpty()
                && gameBoard.getAllZombies().isEmpty();*/
        }
    },
    TIMED_WAR {
        @Override
        public boolean evaluate(GameBoard gameBoard) {
            return gameBoard.economyManager.totalSunsGenerated >= ConfigManager.gameplay().timedWarSunTarget;
        }
    },
    VASE_BREAKER {
        @Override
        public boolean evaluate(GameBoard gameBoard) {
            return gameBoard.getAllVases().stream().map(vase -> vase == null || vase.isBroken)
                .reduce(true, Boolean::logicalAnd);
        }
    },
    I_ZOMBIE {
        @Override
        public boolean evaluate(GameBoard gameBoard) {
            return gameBoard.tickCounter * Constants.Game.TIME_COEFFICIENT >= 120;
        }
    },
    BEGHOULED {
        @Override
        public boolean evaluate(GameBoard gameBoard) {
            return gameBoard.registeredShapes >= ConfigManager.gameplay().beghouledScore;
        }
    };

    abstract public boolean evaluate(GameBoard gameBoard);
}
