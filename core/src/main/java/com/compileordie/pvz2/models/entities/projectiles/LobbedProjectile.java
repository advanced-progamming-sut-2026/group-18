package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.obstacles.Obstacle;
import com.compileordie.pvz2.models.entities.plants.enums.ProjectileType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

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
        this.xSpeed = 5.5;
    }

    @Override
    public void tick(GameBoard board, double delta) {
        super.tick(board, delta);

        double distanceTraveled = Math.abs(this.x - this.startX);
        double p = 0;

        if (this.totalDistance > 0) {
            p = distanceTraveled / this.totalDistance;
        }

        double peakHeight = Math.max(0.5, this.totalDistance * 0.15);
        this.altitude = 4 * peakHeight * p * (1 - p);

        // --- THE IMPACT POINT ---
        if (p >= 1.0) {
            this.altitude = 0;

            if (this.splashRadius > 0) {
                double radiusPixels = this.splashRadius * Constants.Game.TILE_HEIGHT;

                for (Zombie z : board.getAllZombies()) {
                    if (z.isDead()) continue;
                    double dist = Math.hypot(z.getX() - this.x, z.getY() - this.y);

                    if (dist <= radiusPixels) {
                        if (dist <= 0.77 * Constants.Game.TILE_HEIGHT) {
                            z.takeDamage(this.damage, this.type, this.sourcePlantType);
                        } else {
                            z.takeDamage(this.aoeDamage, this.type, this.sourcePlantType);
                        }
                        applySpecialEffect(z);
                    }
                }
            } else {

                // --- FIX: Subtract PADDING_Y to get the TRUE logical row! ---
                int trueLandingRow = (int) Math.floor((this.y - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT);

                for (Zombie z : board.getAllZombies()) {
                    if (!z.isDead() && Math.abs(z.getX() - this.x) <= 0.77 * Constants.Game.TILE_WIDTH
                        && z.occupiesRow(trueLandingRow)) {

                        z.takeDamage(this.damage, this.type, this.sourcePlantType);
                        applySpecialEffect(z);
                    }
                }
            }
            this.isDead = true;
        }
    }

    @Override
    public void onHit(Zombie target, GameBoard board) {
        // Does nothing in the air!
    }

    @Override
    public void onObstacleHit(Obstacle obstacle) {
        // Flies completely over tombs!
    }

    public double getSplashRadius() {
        return splashRadius;
    }

    public double getProgress() {
        if (this.totalDistance <= 0) return 1.0;
        return Math.abs(this.x - this.startX) / this.totalDistance;
    }

    protected void applySpecialEffect(Zombie target) {
        // Base cabbages and melons do nothing special.
    }
}
