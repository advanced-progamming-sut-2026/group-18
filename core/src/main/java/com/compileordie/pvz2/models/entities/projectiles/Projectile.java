package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.models.entities.GameEntity;
import com.compileordie.pvz2.models.game.board.GameBoard;

abstract public class Projectile extends GameEntity {
    public Projectile(double x, double y, int xSpeed, int ySpeed) {
        super(x, y, xSpeed, ySpeed);
    }

    abstract public void tick(int ticks, GameBoard gameBoard);
}
