package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

import java.util.HashSet;
import java.util.Set;

public class BowlingBounceProjectile extends Projectile {

    private final Set<Zombie> alreadyHit = new HashSet<>();

    public BowlingBounceProjectile(double x, double y, int damage) {
        super(x, y, 7.0, damage, DamageType.NORMAL);
        this.ySpeed = 0.0;
        this.setSourcePlantType(PlantType.BOWLING_WALL_NUT);
        this.ignoreObstacles = true;
    }

    @Override
    public void tick(GameBoard board, double delta) {
        super.tick(board, delta);
        if (isDead) return;

        // Bounce off the Top and Bottom of the lawn
        double minY = Constants.Game.PADDING_Y;
        double maxY = Constants.Game.PADDING_Y + (board.totalRows * Constants.Game.TILE_HEIGHT);

        if (this.y <= minY || this.y >= maxY) {
            this.ySpeed = -this.ySpeed;
            this.y = Math.max(minY, Math.min(this.y, maxY));
            this.alreadyHit.clear(); // Clear memory after wall bounce
        }
    }

    // --- NEW: OVERRIDE THE ENGINE'S DEFAULT COLLISION! ---
    @Override
    public void onHit(Zombie target, GameBoard board) {
        if (alreadyHit.contains(target)) return;

        target.takeDamage(this.damage, this.type, this.sourcePlantType);
        alreadyHit.add(target);

        // BOUNCE!
        if (this.ySpeed == 0) {
            this.ySpeed = (Math.random() > 0.5 ? 1 : -1) * this.xSpeed;
        } else {
            this.ySpeed = -this.ySpeed;
        }

        // CRITICAL: Notice we DO NOT call this.destroy()! The ball survives!
    }
}
