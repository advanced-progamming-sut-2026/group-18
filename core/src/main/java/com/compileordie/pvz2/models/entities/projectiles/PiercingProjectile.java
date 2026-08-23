package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.obstacles.Obstacle;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

import java.util.HashSet;
import java.util.Set;

public class PiercingProjectile extends Projectile {
    private int pierceRemaining;

    // Memory banks to prevent multi-hit glitches on the same target!
    private final Set<Zombie> hitZombies = new HashSet<>();
    private final Set<Obstacle> hitObstacles = new HashSet<>();

    public PiercingProjectile(double x, double y, double speed, int damage, int maxPierces) {
        super(x, y, speed, damage, DamageType.NORMAL);
        this.pierceRemaining = maxPierces;
    }

    // --- NEW: Expose the remaining pierces to the graphics engine! ---
    public int getPierceRemaining() {
        return pierceRemaining;
    }

    @Override
    public void tick(GameBoard board, double delta) {
        super.tick(board, delta);

        // CLEANUP: Destroy unlimited thorns when they fly off the right edge of the screen!
        if (this.x > board.totalCols * Constants.Game.TILE_WIDTH + 200) {
            this.isDead = true;
        }
    }

    @Override
    public void onHit(Zombie target, GameBoard board) {
        // Only deal damage if we haven't already pierced this exact zombie!
        if (hitZombies.contains(target)) return;

        hitZombies.add(target);
        target.takeDamage(this.damage, this.type, this.sourcePlantType);

        this.destroy(); // Consumes a pierce projectile
    }

    @Override
    public void onObstacleHit(Obstacle obstacle) {
        // Only deal damage if we haven't already pierced this exact obstacle!
        if (hitObstacles.contains(obstacle)) return;

        hitObstacles.add(obstacle);
        super.onObstacleHit(obstacle); // Deals damage and calls destroy()
    }

    @Override
    public void destroy() {
        pierceRemaining--;
        if (pierceRemaining <= 0) {
            this.isDead = true; // Only dies when it completely runs out of pierces
        }
    }
}
