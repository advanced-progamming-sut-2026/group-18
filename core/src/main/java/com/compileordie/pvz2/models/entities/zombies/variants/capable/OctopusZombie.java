package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class OctopusZombie extends CapableZombie {
    public static final int WAVE_COST = 900;
    public static final double ABILITY_RANGE = Constants.Game.TILE_WIDTH * 2.2;

    public OctopusZombie(double health, double speed, int attackPower, int row, double startX,
                         double x, double y,
                         double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, ZombieType.OCTOPUS_ZOMBIE);
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;
    }

    @Override
    public void takeDamage(double amount, DamageType damageType) {
        if (isDead()) return;
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }

}
