package com.compileordie.pvz2.models.game.judges;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;

public enum LossCondition {
    STANDARD {
        @Override
        public boolean evaluate(GameBoard gameBoard) {
            return gameBoard.lanes.stream().map(lane -> lane.isLost).reduce(false, Boolean::logicalOr);
        }
    },
    SAVE_OUR_SEEDS {
        @Override
        public boolean evaluate(GameBoard gameBoard) {
            // TODO: Add isSpecial to Plant.java and set gameBoard specialIsLost in die()
            return gameBoard.specialIsLost;
        }
    },
    DEAD_LINE {
        @Override
        public boolean evaluate(GameBoard gameBoard) {
            return gameBoard.lanes.stream().map(lane -> lane.isLost).reduce(false, Boolean::logicalOr);
        }
    },
    TIMED_WAR {
        @Override
        public boolean evaluate(GameBoard gameBoard) {
            return gameBoard.tickCounter * Constants.Game.TIME_COEFFICIENT > ConfigManager.gameplay().timedWarMax;
        }
    },
    LOVE_YOUR_PLANTS {
        @Override
        public boolean evaluate(GameBoard gameBoard) {
            return gameBoard.lostPlants >= ConfigManager.gameplay().loveYourPlantsMaxLost;
        }
    },
    I_ZOMBIE {
        @Override
        public boolean evaluate(GameBoard gameBoard) {
            return gameBoard.getAllZombies().isEmpty() && gameBoard.economyManager.sunAmount < 150;
            // NOTE: Might need to change 150.
        }
    };

    abstract public boolean evaluate(GameBoard gameBoard);
}
