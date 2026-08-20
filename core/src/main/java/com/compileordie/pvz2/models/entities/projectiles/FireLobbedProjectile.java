package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.models.entities.plants.enums.ProjectileType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

public class FireLobbedProjectile extends LobbedProjectile {

    public FireLobbedProjectile(double x, double y, double targetX, double speed, int damage, int aoeDamage, double splashRadius) {
        super(x, y, targetX, speed, damage, aoeDamage, splashRadius);

        // Tag as FIRE so it instantly melts ice blocks and un-freezes zombies!
        this.enumType = ProjectileType.FIRE;
    }

    @Override
    protected void applySpecialEffect(Zombie target) {
        // FIRE cleanses CHILLED and FROZEN status effects!
        // The parent class automatically calls this only for zombies caught in the blast radius!
        target.removeStatusEffect(EffectType.CHILLED);
        target.removeStatusEffect(EffectType.FROZEN);
    }
}
