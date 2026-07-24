package com.compileordie.pvz2.models.entities.zombies.variants.mobility;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

public class ProspectorZombie extends Zombie {
    public static final int waveCost = 200;
    private final double timeToExplode = 10.0;
    private boolean dynamiteActive;
    private double dynamiteTimer;
    private boolean isReversedDirection;
    private double homeColumnX;

    public ProspectorZombie(double health, double speed, int attackPower, int row, double startX,
                            double x, double y, double xSpeed, double ySpeed) {
        // فراخوانی دقیق سازنده ۱۲ پارامتری کلاس MobilityZombie شما
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, ZombieType.PROSPECTOR_ZOMBIE);

        this.homeColumnX = Constants.Game.TILE_WIDTH / 1.7;
        this.dynamiteActive = true;
        this.dynamiteTimer = 0.0;
        this.isReversedDirection = false;
    }

    @Override
    public void tick() {
        super.tick();
        if (dynamiteActive) {
            float dt = 1 * Constants.Game.TIME_COEFFICIENT;
            dynamiteTimer += dt;
            if (dynamiteTimer >= timeToExplode) {
                dynamiteActive = false;
                isReversedDirection = true;
                setX(homeColumnX);
            }
        }
    }


    @Override
    public void move(int ticks) {
        if (this.isReversedDirection) {
            setXSpeed(-getStableSpeed());
        } else {
            setXSpeed(getStableSpeed());
        }
        super.move(ticks);
    }

    @Override
    public void takeDamage(double amount, DamageType damageType) {
        if (isDead()) return;
        this.health -= amount;
        if (damageType == DamageType.ICE) {
            dynamiteActive = false;
            isReversedDirection = false;
        }
        if (this.health < 0) this.health = 0;
    }
}
