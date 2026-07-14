package com.compileordie.pvz2.models.entities.zombies.variants.summoner;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class TombraiserZombie extends SummonerZombie {
    private final int numberOfTombsToSpawn;

    public TombraiserZombie(int health, double speed, int attackPower, int row, double startX,
                            double x, double y, double xSpeed, double ySpeed,
                            int numberOfTombsToSpawn, double timeBetweenRaisings) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, timeBetweenRaisings, ZombieType.TOMBRAISER);
        this.numberOfTombsToSpawn = numberOfTombsToSpawn;
    }


    @Override
    public void summon() {
        resetSummonCooldown();
    }

    /**
     * توسط ZombieAbilityService صدا زده می‌شود تا تعداد قبرهای مورد نیاز تولید شود.
     */
    public int launchBone() {
        if (!canSummon()) return 0;
        this.summon();
        return this.numberOfTombsToSpawn;
    }

    public int getNumberOfTombsToSpawn() { return this.numberOfTombsToSpawn; }

    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }
}
