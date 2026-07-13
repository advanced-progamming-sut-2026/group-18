package com.compileordie.pvz2.models.entities.obstacles;

import com.compileordie.pvz2.models.game.board.GameBoard;

abstract public class Obstacle {
    public ObstacleType type;

    public Obstacle(ObstacleType type) {
        this.type = type;
    }

    public void tick(int ticks, GameBoard gameBoard) {
        type.tick(ticks, gameBoard);
    }
}
