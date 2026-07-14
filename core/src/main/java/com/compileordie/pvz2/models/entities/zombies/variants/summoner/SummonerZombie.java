package com.compileordie.pvz2.models.entities.zombies.variants.summoner;

import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

public abstract class SummonerZombie extends Zombie {
    protected double summonCooldown;
    protected double currentCooldownTimer;

    public SummonerZombie(int health, double speed, int attackPower, int row, double startX,
                          double x, double y, double xSpeed, double ySpeed, double initialCooldown, ZombieType type) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, type);
        this.summonCooldown = initialCooldown;
        this.currentCooldownTimer = initialCooldown;
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;

        // کاهش مستمر کوول‌داون در هر تیک بازی
        if (this.currentCooldownTimer > 0) {
            this.currentCooldownTimer -= 0.05; // فرض بر دلتای استاندارد تیک‌ها
            if (this.currentCooldownTimer < 0) this.currentCooldownTimer = 0;
        }
    }

    public boolean canSummon() {
        return this.currentCooldownTimer <= 0 && !isDead();
    }

    public void resetSummonCooldown() {
        this.currentCooldownTimer = this.summonCooldown;
    }

    public abstract void summon();
}
