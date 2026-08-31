package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.enums.ProjectileType;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

import java.util.HashSet;
import java.util.Set;

public class GrapeProjectile extends Projectile {
    private int bouncesRemaining;
    private double lifespanTicks = 50.0; // 5 seconds (assuming 10 ticks/sec)
    private final Set<Zombie> alreadyHitZombies = new HashSet<>();

    public GrapeProjectile(double startX, double startY, double angle, int damage, int totalBounces) {
        // Pass 0 to super's xSpeed initially, we will override it right below!
        super(startX, startY, 0.0, damage, DamageType.NORMAL);

        double grapeSpeed = 0.5;

        // Natively use your base class variables!
        this.xSpeed = Math.cos(angle) * grapeSpeed;
        this.ySpeed = Math.sin(angle) * grapeSpeed;

        this.bouncesRemaining = totalBounces;
        this.enumType = ProjectileType.NORMAL;
        this.sourcePlantType = PlantType.GRAPESHOT;

        // Prevent it from crashing into tombs and deleting itself!
        this.ignoreObstacles = true;
    }

    @Override
    public void tick(GameBoard board, double delta) {
        // 1. Lifespan check
        this.lifespanTicks -= 1.0;
        if (this.lifespanTicks <= 0) {
            this.isDead = true;
            return;
        }

        // 2. Move the projectile natively using your delta math
        this.x += this.xSpeed;
        this.y += this.ySpeed;

        // 3. Screen Edge Bouncing Logic
        boolean bounced = false;
        double rightEdge = board.totalCols * Constants.Game.TILE_WIDTH;
        double topEdge = board.totalRows * Constants.Game.TILE_HEIGHT;

        // Bounce off Left/Right walls
        if (this.x <= 0 || this.x >= rightEdge) {
            this.xSpeed = -this.xSpeed;
            bounced = true;
        }

        // Bounce off Top/Bottom walls
        if (this.y <= 0 || this.y >= topEdge) {
            this.ySpeed = -this.ySpeed;
            bounced = true;
        }

        if (bounced) {
            this.bouncesRemaining--;
            if (this.bouncesRemaining < 0) {
                this.isDead = true;
                return;
            }
        }

        // 4. Hit Detection (Piercing - only hits each zombie once)
        for (Zombie z : board.getAllZombies()) {
            if (z.isDead() || alreadyHitZombies.contains(z)) continue;

            double dist = Math.hypot(z.getX() - this.x, z.getY() - this.y);
            if (dist <= 0.5 * Constants.Game.TILE_HEIGHT) {
                z.takeDamage(this.damage, this.type, this.sourcePlantType);
                alreadyHitZombies.add(z); // Add to blacklist
            }
        }
    }
    public double getGrapeXSpeed() { return this.xSpeed; }
    public double getGrapeYSpeed() { return this.ySpeed; }
}
