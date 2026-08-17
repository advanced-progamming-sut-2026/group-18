package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.models.entities.plants.enums.ProjectileType;
import com.compileordie.pvz2.models.entities.zombies.StatusEffect;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class ButterProjectile extends LobbedProjectile {

    public ButterProjectile(double x, double y, double targetX, double speed, int damage, double splashRadius) {
        // Inherits the Parabola math perfectly!
        super(x, y, targetX, speed, damage, splashRadius);

        // Tags it as BUTTER so obstacles/shields know what it is
        this.enumType = ProjectileType.BUTTER;
    }

    @Override
    public void onHit(Zombie target, GameBoard board) {
        // 1. Standard Lobber Damage
        target.takeDamage(this.damage, this.type, this.sourcePlantType);

        // 2. The Butter Stun! (80 ticks = 8 seconds of complete immobilization)
        target.addEffect(new StatusEffect(EffectType.STUNNED, 80));
    }
}
