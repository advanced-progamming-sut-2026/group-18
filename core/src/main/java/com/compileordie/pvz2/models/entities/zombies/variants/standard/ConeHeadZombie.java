package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class ConeHeadZombie extends StandardZombie {
    public static final int WAVE_COST = 200;

    public ConeHeadZombie(double health,
                          double speed,
                          int attackPower,
                          int row,
                          double startX,
                          double initialArmor,
                          double x,
                          double y,
                          double xSpeed,
                          double ySpeed) {
        super(health, speed, attackPower, row, startX, initialArmor, x, y, xSpeed, ySpeed, ZombieType.CONEHEAD);
    }

}
