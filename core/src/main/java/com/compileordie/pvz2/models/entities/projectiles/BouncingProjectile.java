package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class BouncingProjectile extends Projectile {
    private int bouncesRemaining;
    private Zombie lastHitZombie = null;

    public BouncingProjectile(double x, double y, double speed, int damage, int bouncesRemaining) {
        // HACK: If damage is 600, it's the Plasma Bulb! We assign EXPLOSIVE damage to bypass the Jester Zombie!
        super(x, y, speed, damage, damage >= 600 ? DamageType.EXPLOSIVE : DamageType.NORMAL);
        this.bouncesRemaining = bouncesRemaining;
    }

    @Override
    public void tick(GameBoard board, double delta) {
        // Move the projectile using the parent radar logic
        super.tick(board, delta);

        // --- NEW: Lawn Boundary Wall-Bouncing Physics ---
        double maxY = board.totalRows * Constants.Game.TILE_SIZE;

        if (this.y <= 0) {
            this.y = 0;
            this.ySpeed = Math.abs(this.ySpeed); // Hit the top, bounce DOWN
        } else if (this.y >= maxY) {
            this.y = maxY;
            this.ySpeed = -Math.abs(this.ySpeed); // Hit the bottom, bounce UP
        }
    }

    @Override
    public void onHit(Zombie target, GameBoard board) {
        // 1. Prevent the multi-hit glitch on the exact same zombie!
        if (target == lastHitZombie) return;
        lastHitZombie = target;

        // 2. Evaluate the Payload (Plasma Splash vs. Normal Bounce)
        if (this.type == DamageType.EXPLOSIVE) {
            // Plasma Bulb: 1-Tile Splash Damage
            double radius = Constants.Game.TILE_SIZE;
            for (Zombie z : board.getAllZombies()) {
                if (z.isDead()) continue;

                // If the zombie is inside the blast radius, hit them!
                if (Math.hypot(z.getX() - this.x, z.getY() - this.y) <= radius) {
                    z.takeDamage(this.damage, this.type, this.sourcePlantType);
                }
            }
        } else {
            // Normal Bulb: Single Target Damage (Jester will natively block this!)
            target.takeDamage(this.damage, this.type, this.sourcePlantType);
        }

        // 3. Trigger the bounce
        this.destroy();
    }

    @Override
    public void destroy() {
        if (bouncesRemaining > 0) {
            bouncesRemaining--;

            // Deflect diagonally up or down at half a tile speed
            double deflectSpeed = (Math.random() > 0.5 ? 1.0 : -1.0) * (Constants.Game.TILE_SIZE * 0.5);
            this.ySpeed = deflectSpeed;

        } else {
            // Out of bounces, finally die!
            this.isDead = true;
        }
    }
}
