package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class BucketHeadZombie extends StandardZombie {

    public BucketHeadZombie(int health, double speed, int attackPower, int row, double startX, int initialArmor, double x, double y, double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, initialArmor, x, y, xSpeed, ySpeed, ZombieType.BUCKETHEAD);
    }
}
