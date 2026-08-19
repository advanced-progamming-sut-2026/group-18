package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.models.entities.plants.enums.ProjectileType;
import com.compileordie.pvz2.models.entities.zombies.StatusEffect;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

public class ButterProjectile extends LobbedProjectile {

    // FIXED: Added aoeDamage to match LobbedProjectile!
    public ButterProjectile(double x, double y, double targetX, double speed, int damage, int aoeDamage, double splashRadius) {
        super(x, y, targetX, speed, damage, aoeDamage, splashRadius);
        this.enumType = ProjectileType.BUTTER;
    }

    // FIXED: Instead of using onHit, we use the hook to trigger right when it lands!
    @Override
    protected void applySpecialEffect(Zombie target) {
        // Apply the Butter Stun! (80 ticks = 8 seconds of complete immobilization)
        target.addEffect(new StatusEffect(EffectType.STUNNED, 80));
    }
}
