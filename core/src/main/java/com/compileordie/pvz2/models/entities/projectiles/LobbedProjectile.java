package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.models.entities.plants.enums.ProjectileType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.obstacles.Obstacle;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import static com.compileordie.pvz2.config.Constants.Game.TILE_SIZE;

public class LobbedProjectile extends Projectile {
    protected final double splashRadius;
    protected final double startX;
    protected final double targetX;
    protected final double totalDistance;
    protected final int aoeDamage;
    // NEW: The calculated height of the cabbage in the air!
    public double altitude = 0;

    public LobbedProjectile(double x, double y, double targetX, double speed, int damage, int aoeDamage, double splashRadius) {
        super(x, y, speed, damage, DamageType.LOBBER);
        this.aoeDamage = aoeDamage;
        this.splashRadius = splashRadius;
        this.enumType = ProjectileType.LOBBED;
        this.startX = x;
        this.targetX = targetX;
        this.totalDistance = Math.abs(targetX - startX);
    }

    @Override
    public void tick(GameBoard board, double delta) {
        // 1. Move horizontally forward
        super.tick(board, delta);

        // 2. Calculate progress (0.0 to 1.0)
        double distanceTraveled = Math.abs(this.x - this.startX);
        double p = distanceTraveled / this.totalDistance;

        // 3. The Parabola Math! (Peak height is 150 pixels)
        double peakHeight = 150.0;
        this.altitude = 4 * peakHeight * p * (1 - p);

        // 4. Force impact when it reaches the target X coordinate
        if (p >= 1.0) {
            this.altitude = 0;

            // --- THE DETONATION ENGINE ---
            if (this.splashRadius > 0) {
                double radiusPixels = this.splashRadius * TILE_SIZE;

                for (Zombie z : board.getAllZombies()) {
                    if (z.isDead()) continue;

                    // We use Math.hypot for 2D distance so the explosion hits zombies in adjacent lanes too!
                    double dist = Math.hypot(z.getX() - this.x, z.getY() - this.y);

                    if (dist <= radiusPixels) {
                        // Direct hit gets massive base damage, collateral gets AoE splash damage!
                        if (dist <= 0.5 * TILE_SIZE) {
                            z.takeDamage(this.damage, this.type, this.sourcePlantType);
                        } else {
                            z.takeDamage(this.aoeDamage, this.type, this.sourcePlantType);
                        }
                    }
                }
            } else {
                // Cabbage and Kernel Logic (Single Target Hit)
                for (Zombie z : board.getAllZombies()) {
                    if (!z.isDead() && Math.abs(z.getX() - this.x) <= 0.5 * TILE_SIZE && z.getCurrentRow() == (int)(this.y / TILE_SIZE)) {
                        z.takeDamage(this.damage, this.type, this.sourcePlantType);
                    }
                }
            }

            this.isDead = true;
        }
    }

    @Override
    public void onHit(Zombie target, GameBoard board) {
        // We override this to do NOTHING because standard Peashooter collisions shouldn't
        // trigger while the cabbage is flying high in the air!
        // Damage is calculated when it lands (p >= 1.0).
    }

    @Override
    public void onObstacleHit(Obstacle obstacle) {
        // Ignored! The cabbage flies right over tombs.
    }

    public double getSplashRadius() {
        return splashRadius;
    }
}
