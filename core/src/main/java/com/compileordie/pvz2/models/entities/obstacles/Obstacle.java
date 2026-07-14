package com.compileordie.pvz2.models.entities.obstacles;

import com.compileordie.pvz2.models.entities.GameEntity;
import com.compileordie.pvz2.models.game.board.GameBoard;

abstract public class Obstacle extends GameEntity {
    public ObstacleType type;

    public Obstacle(double x, double y, ObstacleType type) {
        super(x, y, 0, 0);
        this.type = type;
    }

    public void tick(int ticks, GameBoard gameBoard) {
        type.tick(ticks, this, gameBoard);
    }
}
