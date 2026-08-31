package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.enums.ProjectileType;
import com.compileordie.pvz2.models.entities.zombies.StatusEffect;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

import static com.compileordie.pvz2.config.Constants.Game.TILE_WIDTH;

public class IceProjectile extends Projectile {

    public final double chillDurationTicks;

    public IceProjectile(double x, double y, double speed, int damage, double chillDurationTicks) {
        super(x, y, speed, damage, DamageType.ICE);
        this.chillDurationTicks = chillDurationTicks;
        this.enumType = ProjectileType.ICE;
    }

    @Override
    public void onHit(Zombie target, GameBoard board) {
        // 1. The primary target gets chilled
        target.addEffect(new StatusEffect(EffectType.CHILLED, (int) this.chillDurationTicks));

        // 2. The 1x1 Splash Chill Effect
        int tileCol = (int) Math.floor(target.getX() / TILE_WIDTH);
        int tileRow = target.getCurrentRow();

        for (Zombie z : board.getAllZombies()) {
            if (z != target && !z.isDead() && z.occupiesRow(tileRow)) {
                int zCol = (int) Math.floor(z.getX() / TILE_WIDTH);
                if (zCol == tileCol) {
                    z.addEffect(new StatusEffect(EffectType.CHILLED, (int) this.chillDurationTicks));
                }
            }
        }

        // 3. Call the parent method to deal the physical damage and destroy the projectile!
        super.onHit(target, board);
    }
}
