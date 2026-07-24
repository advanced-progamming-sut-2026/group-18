package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class FishermanZombie extends CapableZombie {
    public static final int WAVE_COST = 600;
    public static final float ABILITY_COOLDOWN = 5.0f;

    private boolean shouldHook = false;
    private double timer = 0;

    public FishermanZombie(double health, double speed, int attackPower, int row, double startX,
                           double x, double y,
                           double xSpeed, double ySpeed) {
        super(health, 0, attackPower, row, startX, x, y, 0, 0, ZombieType.FISHERMAN_ZOMBIE);
        setXSpeed(0);
        setYSpeed(0);
    }

    @Override
    public boolean canMove() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;

        float dt = 1 * Constants.Game.TIME_COEFFICIENT;
        timer += dt;

        if (timer >= ABILITY_COOLDOWN) {
            shouldHook = true;
            timer = 0;
        }
    }

    @Override
    public void takeDamage(double amount, DamageType damageType) {
        if (isDead()) return;
        this.health -= amount;
        if (this.health <= 0) {
            this.health = 0;
            handleDeath();
        }
    }

    public boolean shouldWeHook() {
        return shouldHook;
    }

    public void stopHook() {
        this.shouldHook = false;
    }

    public void setShouldHook(boolean shouldHook) {
        this.shouldHook = shouldHook;
    }
}
