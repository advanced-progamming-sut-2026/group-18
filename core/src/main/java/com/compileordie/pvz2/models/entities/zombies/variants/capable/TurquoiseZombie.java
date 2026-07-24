package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class TurquoiseZombie extends CapableZombie {
    public static final int waveCost = 500;
    public int totalStolenSuns;
    private boolean shouldSteal = false;
    private boolean shouldLaser = false;
    private boolean shouldShut = false;
    private boolean shouldBackSun = false;
    private boolean isStealing;
    private double stealTimer;
    private double stealTime;
    private double laserTimer;
    private double lasetTime;

    public TurquoiseZombie(double health, double speed, int attackPower, int row, double startX,
                           double x, double y,
                           double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, ZombieType.TURQUOISE_ZOMBIE);
        this.isStealing = false;
        this.stealTimer = 0;
        this.stealTime = 5;
        this.laserTimer = 0;
        this.lasetTime = 5;
        this.totalStolenSuns = 0;
    }

    @Override
    public void move(int ticks) {
        if (this.isStealing) return;
        super.move(ticks);
    }

    @Override
    public void takeDamage(double amount, DamageType damageType) {
        if (isDead()) return;
        this.health -= amount;
        if (this.health <= 0) {
            this.health = 0;
            handleDeath();
        }
        ;
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;
        //---
        float dt = 1 * Constants.Game.TIME_COEFFICIENT;
        if (this.isStealing) {
            if (stealTimer % 1 == 0 && stealTimer > 0) {
                shouldSteal = true;
            }
            stealTimer += dt;
            //---
            if (stealTimer >= stealTime) {
                stealTimer = 0;
                isStealing = false;
                shouldSteal = false;
                shouldLaser = true;
            }
        }
    }

    private void stealSunFromPlayerBalance(int amount) {
    }

    @Override
    public void handleDeath() {
        shouldBackSun = true;
    }

    public void addStolen(int amount) {
        totalStolenSuns += amount;
    }

    public boolean shouldWeSteal() {
        return shouldSteal;
    }

    public void stopSteal() {
        this.shouldSteal = false;
    }

    public boolean shouldWeLaser() {
        return shouldLaser;
    }

    public void stopLaser() {
        this.shouldLaser = false;
    }

    public boolean shouldWeShut() {
        return shouldShut;
    }

    public void stopShut() {
        this.shouldShut = false;
    }

    public boolean shouldWeBackSun() {
        return shouldBackSun;
    }

    public void stopBackSun() {
        this.shouldBackSun = false;
    }

    public void startStealing() {
        this.isStealing = true;
    }
}
