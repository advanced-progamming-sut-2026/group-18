package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class HomingProjectile extends Projectile {
    private final Zombie target;
    private final double maxSpeed;

    public HomingProjectile(double x, double y, double speed, int damage, Zombie target) {
        super(x, y, speed, damage, DamageType.NORMAL); // Or HYPNOTIC for Caulipower
        this.target = target;
        this.maxSpeed = speed;
    }

    @Override
    public void tick(GameBoard board, double delta) {
        if (isDead) return;

        // If target dies mid-flight, act like a normal projectile and fly straight
        if (target != null && !target.isDead()) {
            double dirX = target.getX() - this.x;
            double dirY = target.getY() - this.y;
            double distance = Math.hypot(dirX, dirY);

            // Normalize vector and apply speed
            if (distance > 0.1) {
                this.xSpeed = (dirX / distance) * maxSpeed;
                this.ySpeed = (dirY / distance) * maxSpeed;
            }
        }

        super.tick(board, delta);
    }
}
