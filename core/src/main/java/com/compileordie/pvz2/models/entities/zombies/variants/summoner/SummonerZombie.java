package com.compileordie.pvz2.models.entities.zombies.variants.summoner;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

public abstract class SummonerZombie extends Zombie {
    protected double summonTime;
    protected double summonTimer;

    public SummonerZombie(double health, double speed, int attackPower, int row, double startX,
                          double x, double y, double xSpeed, double ySpeed, ZombieType type, double summonTime) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, type);
        this.summonTime = summonTime;
        this.summonTimer = 0;
    }

    @Override
    public void tick() {
        super.tick();
        float dt = 1 * Constants.Game.TIME_COEFFICIENT;
        summonTimer += dt;
        if (summonTimer >= summonTime) {
            summonTimer = 0;
            summon();
        }
    }

    @Override
    public void takeDamage(double amount, DamageType damageType) {
        if (isDead()) return;
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }

    public abstract void summon();
}
