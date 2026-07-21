package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class BucketHeadZombie extends StandardZombie {
    public static final int waveCost = 400;
    public BucketHeadZombie(double health, double speed, int attackPower, int row, double startX, double initialArmor, double x, double y, double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, initialArmor, x, y, xSpeed, ySpeed, ZombieType.BUCKETHEAD);
    }

    public void mushroomAbsorption() {
        setArmorHealth(0);
        // اینجا باید گیاه به محض رویت زامبی در نزدیکی اش این متد را فراخوانی کند
    }
}
