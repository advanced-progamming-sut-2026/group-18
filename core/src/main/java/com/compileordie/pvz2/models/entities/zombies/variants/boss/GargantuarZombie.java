package com.compileordie.pvz2.models.entities.zombies.variants.boss;

import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.services.spawn.ZombieSpawnLootService;


public class GargantuarZombie extends Zombie {
    private boolean spawnImp = false;
    private double healthThresholdToThrowImp;
    private final int impTargetColumn;
    public static final int waveCost = 1500;

    public GargantuarZombie(double health, double speed, int attackPower, int row, double startX,
                            double x, double y, double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, ZombieType.GARGANTUAR);
        this.healthThresholdToThrowImp = health / 2.0;
        this.impTargetColumn = 3;

    }

    @Override
    public void takeDamage(double amount, DamageType damageType) {
        if (isDead()) return;

        this.health -= amount;
        if (this.health < 0) this.health = 0;

        if (this.health <= this.healthThresholdToThrowImp){
            spawnImp = true;
        }
    }

    public boolean shouldWeSpawnImp() { return spawnImp; }
    public void stopSpawnImp() { this.spawnImp = false; }
    public int getImpTargetColumn() { return impTargetColumn; }

}
