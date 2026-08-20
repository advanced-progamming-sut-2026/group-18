package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.models.entities.plants.enums.ProjectileType;
import com.compileordie.pvz2.models.entities.zombies.StatusEffect;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class PoisonProjectile extends Projectile {
    private final int poisonTickDamage;

    public PoisonProjectile(double x, double y, double speed, int damage, int poisonTickDamage) {
        super(x, y, speed, damage, DamageType.POISON);
        this.enumType = ProjectileType.POISON;
        this.poisonTickDamage = poisonTickDamage;
    }

    @Override
    public void onHit(Zombie target, GameBoard board) {
        // 1. Inject the Poison Status Effect (Lasts 50 ticks, passing dynamic damage)
        target.addEffect(new StatusEffect(EffectType.POISON, 50, this.poisonTickDamage));

        // 2. Deal the physical base impact damage and destroy the projectile
        super.onHit(target, board);
    }
}
