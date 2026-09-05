package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.enums.ProjectileType;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Tile;
import com.compileordie.pvz2.models.entities.obstacles.Obstacle;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.Tomb;
import com.compileordie.pvz2.models.entities.zombies.variants.vehicle.Barrel;

public abstract class Projectile {
    protected double x;
    protected double y;
    protected double startX;
    protected double xSpeed;
    protected double ySpeed = 0;
    protected int damage;
    protected DamageType type;
    protected PlantType sourcePlantType;
    protected ProjectileType enumType = ProjectileType.NORMAL;
    protected boolean ignoreObstacles = false;
    protected boolean isDead = false;
    protected boolean isReversed = false;
    protected boolean isPuddle = false;
    protected boolean isIgnited = false;
    protected boolean isTileHit = false;

    public Projectile(double x, double y, double xSpeed, int damage, DamageType type) {
        this.x = x;
        this.y = y;
        this.startX = x;
        this.xSpeed = xSpeed;
        this.damage = damage;
        this.type = type;
    }

    public void tick(GameBoard board, double delta) {
        if (isDead) return;

        // Multiply by TIME_COEFFICIENT to convert ticks to actual seconds!
        this.x += this.xSpeed * delta * Constants.Game.TIME_COEFFICIENT;
        this.y += this.ySpeed * delta * Constants.Game.TIME_COEFFICIENT;

        // --- NEW: Short-Range Spore Evaporation! ---
        if (this.sourcePlantType == PlantType.SEA_SHROOM || this.sourcePlantType == PlantType.PUFF_SHROOM || this.sourcePlantType == PlantType.FUME_SHROOM) {
            double maxRangePixels = 6.0 * Constants.Game.TILE_WIDTH; // Dies after exactly 6 tiles!
            if (Math.abs(this.x - this.startX) >= maxRangePixels) {
                this.destroy(); // Evaporate in midair!
                return;
            }
        }

        // --- FIX 1: BOUNDARY LIMIT ---
        // Account for the new padded coordinates so bullets can actually reach the end of the lawn!
        double rightBoundary = Constants.Game.PADDING_X + (10 * Constants.Game.TILE_WIDTH);
        if (this.x > rightBoundary || this.x < -2 * Constants.Game.TILE_WIDTH) {
            this.destroy();
            return;
        }

        // --- The Static Obstacle Radar ---
        if (!this.ignoreObstacles) {
            // --- FIX 2: Remove Double-Padding! ---
            // Teammate baked the padding in, so these are ALREADY the correct world coordinates!
            float worldX = (float) this.x;
            float worldY = (float) this.y;

            Tile currentTile = board.getTile(worldX, worldY);

            if (currentTile != null && currentTile.obstacle != null) {
                this.onObstacleHit(currentTile.obstacle);
            }
        }
    }

    // The universal obstacle impact payload
    public void onObstacleHit(Obstacle obstacle) {
        if (obstacle instanceof Tomb) {
            ((Tomb) obstacle).takeDamage(this.damage, this.enumType);
            this.destroy();
        }
        else if (obstacle instanceof Barrel) {
            // Passing default imp stats since the projectile only cares about breaking the barrel
            ((Barrel) obstacle).takeDamage(this.damage, 200, 2.5, 15);
            this.destroy();
        }
    }

    // The universal impact method!
    public void onHit(Zombie target, GameBoard board) {
        // 1. Deal standard damage
        target.takeDamage(this.damage, this.type, this.sourcePlantType);

        // 2. Destroy the projectile
        this.destroy();
    }

    public void destroy() {
        this.isDead = true;
    }

    // --- Standard Getters & Setters ---
    public double getX() { return x; }
    public double getY() { return y; }

    // --- FIX 3: THE LOGICAL ROW FIX ---
    // Subtract the new PADDING_Y before dividing so the engine registers the correct row!
    public int getRow() {
        return (int) Math.floor((this.y - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT);
    }

    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; } //  For Torchwood damage scaling
    public DamageType getType() { return type; }
    public void setType(DamageType type) { this.type = type; } //  Allows changing to FIRE damage
    public boolean isDead() { return isDead; }
    public void setXSpeed(double xSpeed) { this.xSpeed = xSpeed; }
    public void setYSpeed(double ySpeed) { this.ySpeed = ySpeed; }
    public void setSourcePlantType(PlantType sourcePlantType) { this.sourcePlantType = sourcePlantType; }
    public PlantType getSourcePlantType() { return sourcePlantType; }
    public boolean getIgnoreObstacles() { return ignoreObstacles; }
    public double getYSpeed() { return ySpeed; }
    public double getXSpeed() { return xSpeed; }
    public boolean isIgnited() { return isIgnited; }
    public void setIgnited(boolean ignited) { this.isIgnited = ignited; }
    public boolean isTileHit() { return isTileHit; }
    public void setTileHit(boolean tileHit) { this.isTileHit = tileHit; }
    public boolean isPuddle() { return isPuddle; }
    public void setPuddle(boolean puddle) { this.isPuddle = puddle; }
}
