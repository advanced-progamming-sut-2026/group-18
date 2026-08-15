package com.compileordie.pvz2.models.game.judges;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;

public enum LossCondition {
    STANDARD,
    SAVE_OUR_SEEDS {
        @Override
        public boolean evaluate(GameBoard gameBoard) {
            return super.evaluate(gameBoard) || gameBoard.specialIsLost;
        }
    },
    DEAD_LINE,
    TIMED_WAR {
        @Override
        public boolean evaluate(GameBoard gameBoard) {
            return super.evaluate(gameBoard)
                || gameBoard.tickCounter * Constants.Game.TIME_COEFFICIENT > ConfigManager.gameplay().timedWarMax;
        }
    },
    LOVE_YOUR_PLANTS {
        @Override
        public boolean evaluate(GameBoard gameBoard) {
            return super.evaluate(gameBoard) || gameBoard.lostPlants >= ConfigManager.gameplay().loveYourPlantsMaxLost;
        }
    },
    I_ZOMBIE {
        @Override
        public boolean evaluate(GameBoard gameBoard) {
            return super.evaluate(gameBoard)
                || gameBoard.getAllZombies().isEmpty() && gameBoard.economyManager.sunAmount < 150;
        }
    };

    public boolean evaluate(GameBoard gameBoard) {
        return gameBoard.getAllZombies().stream().anyMatch(zombie -> zombie.succeeded);
    }
}
