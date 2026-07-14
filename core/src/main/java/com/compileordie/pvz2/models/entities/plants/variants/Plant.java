package com.compileordie.pvz2.models.entities.plants.variants;

import com.compileordie.pvz2.models.entities.GameEntity;
import com.compileordie.pvz2.models.game.board.GameBoard;

abstract public class Plant extends GameEntity {
    // TODO: To be implemented.

    public Plant(double x, double y, double xSpeed, double ySpeed) {
        super(x, y, xSpeed, ySpeed);
    }

    abstract public void tick(int ticks, GameBoard gameBoard);
    abstract public void takeDamage(int amount);
}
