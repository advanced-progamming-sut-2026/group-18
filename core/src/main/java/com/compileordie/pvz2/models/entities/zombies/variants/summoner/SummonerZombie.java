package com.compileordie.pvz2.models.entities.zombies.variants.summoner;

import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

public abstract class SummonerZombie extends Zombie {

    protected double summonCooldown; // زمان باقی‌مانده از کوول‌داون فعلی

    public SummonerZombie(int health, double speed, int attackPower, int row, double startX,
                          double x, double y, int xSpeed, int ySpeed, double initialCooldown) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed);
        this.summonCooldown = initialCooldown;
    }

    /**
     * در هر تیک بازی توسط سرویس صدا زده می‌شود تا زمان کوول‌داون کاهش یابد
     */
    public void updateCooldown(double deltaTime) {
        if (this.summonCooldown > 0) {
            this.summonCooldown -= deltaTime;
            if (this.summonCooldown < 0) this.summonCooldown = 0;
        }
    }

    public boolean canSummon() {
        return this.summonCooldown <= 0 && !isDead();
    }

    public abstract void resetSummonCooldown();

    public abstract void summon();
}
