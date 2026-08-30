package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.StatusEffect;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class HomingProjectile extends Projectile {
    private final Zombie target;
    private final double maxSpeed;

    public HomingProjectile(double x, double y, double speed, int damage, Zombie target) {
        super(x, y, speed, damage, DamageType.NORMAL);
        this.target = target;
        this.maxSpeed = speed;
    }

    @Override
    public void tick(GameBoard board, double delta) {
        if (isDead) return;

        if (target != null) {
            // FIX 1: If the zombie dies from something else while the cloud is moving, destroy the cloud!
            if (target.isDead()) {
                this.destroy();
                return;
            }

            // --- FIX 2: Removed Double-Padding! ---
            // The projectile's logical coordinates are now perfectly synced with world coordinates!
            double projWorldX = this.x;

            // We keep the +0.2 offset so the cloud visually aims at the zombie's chest instead of its feet!
            double projWorldY = this.y + 0.2;

            double dirX = target.getX() - projWorldX;
            double dirY = target.getY() - projWorldY;
            double distance = Math.hypot(dirX, dirY);

            // FIX 3: Self-contained 2D collision detection!
            // If the cloud is within 0.5 meters of the zombie, it triggers the hit directly!
            if (distance <= 0.5) {
                this.onHit(target, board);
                return;
            }

            if (distance > 0.1) {
                this.xSpeed = (dirX / distance) * maxSpeed;
                this.ySpeed = (dirY / distance) * maxSpeed;
            }
        } else {
            // If it never had a target, just die
            this.destroy();
            return;
        }

        super.tick(board, delta);
    }

    @Override
    public void onHit(Zombie target, GameBoard board) {
        if (this.sourcePlantType == PlantType.CAULIPOWER) {
            // Charm the zombie permanently
            target.addEffect(new StatusEffect(EffectType.HYPNOTIZED, 99999));
            this.destroy();
        } else if (this.sourcePlantType == PlantType.ELECTRIC_BLUEBERRY) {
            // ELECTRIC BLUEBERRY FIX: Deliver massive instant-kill damage!
            target.takeDamage(5000, DamageType.EXPLOSIVE, this.sourcePlantType);
            this.destroy();
        } else {
            // For future homing plants that actually do physical damage
            super.onHit(target, board);
        }
    }

    @Override
    public void setSourcePlantType(PlantType sourcePlantType) {
        super.setSourcePlantType(sourcePlantType);

        if (sourcePlantType == PlantType.CAULIPOWER || sourcePlantType == PlantType.ELECTRIC_BLUEBERRY) {
            this.ignoreObstacles = true;
        }
    }
}
