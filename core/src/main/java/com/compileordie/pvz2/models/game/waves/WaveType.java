package com.compileordie.pvz2.models.game.waves;

import com.compileordie.pvz2.models.game.board.GameBoard;

public enum WaveType {
    NORMAL{
        @Override
        public void spawnWave(WaveManager self, GameBoard gameBoard) {
        }
    },
    ANCIENT_EGYPT {
        @Override
        public void spawnWave(WaveManager self, GameBoard gameBoard) {
        }
    },
    FROSTBITE_CAVE {
        @Override
        public void spawnWave(WaveManager self, GameBoard gameBoard) {
        }
    },
    BIG_WAVE_BEACH {
        @Override
        public void spawnWave(WaveManager self, GameBoard gameBoard) {
        }
    },
    DARK_AGES {
        @Override
        public void spawnWave(WaveManager self, GameBoard gameBoard) {
        }
    },
    NO_WAVES {
        @Override
        public void spawnWave(WaveManager self, GameBoard gameBoard) {
        }
    };

    abstract public void spawnWave(WaveManager self, GameBoard gameBoard);
    // TODO: Subject to adding more values, tick method, etc.
}
