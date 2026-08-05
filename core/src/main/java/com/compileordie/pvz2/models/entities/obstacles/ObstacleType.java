package com.compileordie.pvz2.models.entities.obstacles;

import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.utils.Toolbox;

public enum ObstacleType {
    BARREL,
    ICE_BLOCK,
    TOMB,
    ARCADE_MACHINE;

    public void tick(int ticks, Obstacle self, GameBoard gameBoard) {
    }

    public static ObstacleType getByName(String name) {
        for (ObstacleType obstacleType : ObstacleType.values()) {
            if (obstacleType.toString().equalsIgnoreCase(name)) {
                return obstacleType;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return Toolbox.enumToString(this, true);
    }
}
