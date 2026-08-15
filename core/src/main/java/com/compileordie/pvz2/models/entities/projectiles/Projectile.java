package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

public abstract class Projectile {
    protected double x;
    protected double y;
    protected double xSpeed;
    protected double ySpeed = 0;
    protected int damage;
    protected DamageType type;
    protected PlantType sourcePlantType;

    protected boolean isDead = false;
    protected boolean isReversed = false;

    public Projectile(double x, double y, double xSpeed, int damage, DamageType type) {
        this.x = x;
        this.y = y;
        this.xSpeed = xSpeed;
        this.damage = damage;
        this.type = type;
    }

    // NEW: The universal impact method!
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
    public int getRow() { return (int) (y / Constants.Game.TILE_SIZE); }
    public int getDamage() { return damage; }
    public DamageType getType() { return type; }
    public boolean isDead() { return isDead; }
    public void setXSpeed(double xSpeed) { this.xSpeed = xSpeed; }
    public void setYSpeed(double ySpeed) { this.ySpeed = ySpeed; }
    public void setSourcePlantType(PlantType sourcePlantType) { this.sourcePlantType = sourcePlantType; }
}
