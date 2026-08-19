package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.models.entities.plants.enums.ProjectileType;
import com.compileordie.pvz2.models.entities.zombies.StatusEffect;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

public class IceLobbedProjectile extends LobbedProjectile {

    public IceLobbedProjectile(double x, double y, double targetX, double speed, int damage, int aoeDamage, double splashRadius) {
        super(x, y, targetX, speed, damage, aoeDamage, splashRadius);

        // Tag as ICE so fire/ice interactions trigger correctly!
        this.enumType = ProjectileType.ICE;
    }

    @Override
    protected void applySpecialEffect(Zombie target) {
        // Apply a 5-second CHILLED effect to everything in the blast radius!
        target.addEffect(new StatusEffect(EffectType.CHILLED, 50));
    }
}
