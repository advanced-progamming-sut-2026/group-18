package com.compileordie.pvz2.models.entities.zombies.variants.standard;

public class BasicZombie extends StandardZombie {
    public BasicZombie(int health, double speed, int attackPower, int row, double startX, double x, double y, int xSpeed, int ySpeed) {
        super(health, speed, attackPower, row, startX, 0, x, y, xSpeed, ySpeed);
//      super(190, 0.185, 10, row, startX, 0);
    }
}
