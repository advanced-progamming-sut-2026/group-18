package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.models.game.board.GameBoard;

abstract public class Projectile {
    abstract public void tick(int ticks, GameBoard gameBoard);
}
