package com.compileordie.pvz2.models.entities.projectiles;

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

        if (target != null && !target.isDead()) {
            double dirX = target.getX() - this.x;
            double dirY = target.getY() - this.y;
            double distance = Math.hypot(dirX, dirY);

            if (distance > 0.1) {
                this.xSpeed = (dirX / distance) * maxSpeed;
                this.ySpeed = (dirY / distance) * maxSpeed;
            }
        }

        super.tick(board, delta);
    }

    @Override
    public void onHit(Zombie target, GameBoard board) {
        if (this.sourcePlantType == PlantType.CAULIPOWER) {
            // 1. Charm the zombie permanently
            target.addEffect(new StatusEffect(EffectType.HYPNOTIZED, 99999));
            // 2. Destroy the projectile WITHOUT calling super.onHit() so it takes 0 damage!
            this.destroy();
        } else {
            // For future homing plants (like Homing Thistle) that actually do physical damage
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
