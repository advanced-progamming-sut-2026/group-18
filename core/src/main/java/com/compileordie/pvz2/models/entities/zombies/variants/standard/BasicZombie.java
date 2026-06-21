package com.compileordie.pvz2.models.entities.zombies.variants.standard;

public class BasicZombie extends StandardZombie {
    public BasicZombie(int health, double speed, int attackPower, int row, double startX) {
        super(health, speed, attackPower, row, startX, 0);
//      super(190, 0.185, 10, row, startX, 0);
    }
}
