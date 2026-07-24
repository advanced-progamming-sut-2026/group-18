package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class BlockheadZombie extends StandardZombie {
    public static final int waveCost = 700;

    public BlockheadZombie(double health, double speed, int attackPower, int row, double startX, double initialArmor, double x, double y, double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, initialArmor, x, y, xSpeed, ySpeed, ZombieType.BLOCKHEAD);
    }

}
