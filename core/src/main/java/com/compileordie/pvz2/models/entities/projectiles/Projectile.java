package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.game.board.GameBoard;

public abstract class Projectile {
    protected double x;
    protected double y;
    protected double xSpeed;
    protected double ySpeed = 0; // Used for homing and bouncing
    protected int damage;
    protected DamageType type;

    protected boolean isDead = false;
    protected boolean isReversed = false;

    public Projectile(double x, double y, double xSpeed, int damage, DamageType type) {
        this.x = x;
        this.y = y;
        this.xSpeed = xSpeed;
        this.damage = damage;
        this.type = type;
    }

    // Called every frame by the GameBoard
    public void tick(GameBoard board, double delta) {
        if (isDead) return;

        x += xSpeed * delta;
        y += ySpeed * delta;

        // Clean up if it flies off the screen
        if (x < -1.0 || x > board.totalCols * Constants.Game.TILE_SIZE + 2.0) {
            isDead = true;
        }
    }

    // --- Hooks for ZombieCombatServiceImpl ---
    public double getX() { return x; }
    public double getY() { return y; }

    // Calculates the current row based on Y coordinate for the collision loop
    public int getRow() { return (int) (y / Constants.Game.TILE_SIZE); }

    public double getXSpeed() { return xSpeed; }
    public void setXSpeed(double xSpeed) { this.xSpeed = xSpeed; }

    public int getDamage() { return damage; }
    public DamageType getType() { return type; }

    public boolean isDead() { return isDead; }
    public boolean isReversed() { return isReversed; }
    public void setReversed(boolean reversed) { this.isReversed = reversed; }

    /**
     * Called by the combat service upon collision.
     * Overridden by Piercing and Bouncing projectiles!
     */
    public void destroy() {
        this.isDead = true;
    }
}
