package com.compileordie.pvz2.models.game.minigames.vasebreaker;

import com.compileordie.pvz2.models.game.board.GameBoard;

public enum VaseType {
    NORMAL {
        @Override
        public void breakVase(Vase self, GameBoard gameBoard) {

        }
    },
    PLANT {
        @Override
        public void breakVase(Vase self, GameBoard gameBoard) {

        }
    },
    GARGANTUAR {
        @Override
        public void breakVase(Vase self, GameBoard gameBoard) {

        }
    };

    abstract public void breakVase(Vase self, GameBoard gameBoard);
}
