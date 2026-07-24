package com.compileordie.pvz2.models.game.judges;

import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;

public enum WinCondition {
    STANDARD {
        @Override
        public boolean evaluate(GameBoard gameBoard) {
            return gameBoard.waveManager.isLatWave() && gameBoard.getAllZombies().isEmpty();
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
            return gameBoard.vases.stream().map(vase -> vase.isBroken).reduce(true, Boolean::logicalAnd);
        }
    },
    I_ZOMBIE {
        @Override
        public boolean evaluate(GameBoard gameBoard) {
            return gameBoard.lanes.stream().map(lane -> lane.isLost).reduce(true, Boolean::logicalAnd);
        }
    };



    abstract public boolean evaluate(GameBoard gameBoard);
}
