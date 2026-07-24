package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class BasicZombie extends StandardZombie {
    public static final int waveCost = 100;

    public BasicZombie(double health, double speed, int attackPower, int row, double startX, double x, double y, double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, 0, x, y, xSpeed, ySpeed, ZombieType.STANDARD);
    }
}
