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
    protected double xSpeed;
    protected double ySpeed = 0;
    protected int damage;
    protected DamageType type;
    protected PlantType sourcePlantType;

    // Natively defaults to NORMAL for Peashooter, Repeater, etc.
    protected ProjectileType enumType = ProjectileType.NORMAL;

    protected boolean ignoreObstacles = false;
    protected boolean isDead = false;
    protected boolean isReversed = false;

    // Torchwood Hook ---
    protected boolean isIgnited = false;

    public Projectile(double x, double y, double xSpeed, int damage, DamageType type) {
        this.x = x;
        this.y = y;
        this.xSpeed = xSpeed;
        this.damage = damage;
        this.type = type;
    }

    public void tick(GameBoard board, double delta) {
        if (isDead) return;

        // THE FIX: Multiply by TIME_COEFFICIENT to convert ticks to actual seconds!
        this.x += this.xSpeed * delta * Constants.Game.TIME_COEFFICIENT;
        this.y += this.ySpeed * delta * Constants.Game.TIME_COEFFICIENT;

        // --- The Static Obstacle Radar ---
        if (!this.ignoreObstacles) {
            Tile currentTile = board.getTile((float) this.x, (float) this.y);

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
    public int getRow() { return (int) (y / Constants.Game.TILE_HEIGHT); }
//    public int getRow() { return (int) (y / Constants.Game.TILE_SIZE); }
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

    // Ignition Getters & Setters ---
    public boolean isIgnited() { return isIgnited; }
    public void setIgnited(boolean ignited) { this.isIgnited = ignited; }
}
