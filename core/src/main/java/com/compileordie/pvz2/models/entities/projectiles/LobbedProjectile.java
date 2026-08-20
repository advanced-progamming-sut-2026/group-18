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
        super.tick(board, delta);

        double distanceTraveled = Math.abs(this.x - this.startX);
        double p = distanceTraveled / this.totalDistance;

        double peakHeight = 150.0;
        this.altitude = 4 * peakHeight * p * (1 - p);

        if (p >= 1.0) {
            this.altitude = 0;

            if (this.splashRadius > 0) {
                double radiusPixels = this.splashRadius * TILE_SIZE;

                for (Zombie z : board.getAllZombies()) {
                    if (z.isDead()) continue;
                    double dist = Math.hypot(z.getX() - this.x, z.getY() - this.y);

                    if (dist <= radiusPixels) {
                        if (dist <= 0.5 * TILE_SIZE) {
                            z.takeDamage(this.damage, this.type, this.sourcePlantType);
                        } else {
                            z.takeDamage(this.aoeDamage, this.type, this.sourcePlantType);
                        }
                        // HOOK: Apply effects to anyone hit by the splash!
                        applySpecialEffect(z);
                    }
                }
            } else {
                for (Zombie z : board.getAllZombies()) {
                    if (!z.isDead() && Math.abs(z.getX() - this.x) <= 0.5 * TILE_SIZE && z.getCurrentRow() == (int)(this.y / TILE_SIZE)) {
                        z.takeDamage(this.damage, this.type, this.sourcePlantType);
                        // HOOK: Apply effects to the single target!
                        applySpecialEffect(z);
                    }
                }
            }
            this.isDead = true;
        }
    }

    @Override
    public void onHit(Zombie target, GameBoard board) {
        // Does nothing air!
    }

    @Override
    public void onObstacleHit(Obstacle obstacle) {
        // Flies over tombs
    }

    public double getSplashRadius() {
        return splashRadius;
    }

    // --- NEW: THE SPECIAL EFFECT HOOK ---
    // Subclasses like ButterProjectile or WinterMelon can override this to add stun/chill!
    protected void applySpecialEffect(Zombie target) {
        // Base cabbages and melons do nothing special.
    }
}
