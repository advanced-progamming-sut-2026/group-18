package com.compileordie.pvz2.models.entities.projectiles;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.game.board.GameBoard;

public abstract class Projectile {
    protected double x;
    protected double y;
    protected double xSpeed;
    protected double ySpeed = 0; // NEW: Handles diagonal/vertical movement
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

    public void tick(GameBoard board, double delta) {
        if (isDead) return;

        x += xSpeed * delta;
        y += ySpeed * delta; // NEW: Applies vertical speed

        if (x < -1.0 || x > board.totalCols * Constants.Game.TILE_SIZE + 2.0) {
            isDead = true;
        }
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getRow() { return (int) (y / Constants.Game.TILE_SIZE); }

    public double getXSpeed() { return xSpeed; }
    public void setXSpeed(double xSpeed) { this.xSpeed = xSpeed; }

    public double getYSpeed() { return ySpeed; }
    public void setYSpeed(double ySpeed) { this.ySpeed = ySpeed; } // NEW: Setter for vectors

    public int getDamage() { return damage; }
    public DamageType getType() { return type; }
    public boolean isDead() { return isDead; }

    public boolean isReversed() { return isReversed; }
    public void setReversed(boolean reversed) { this.isReversed = reversed; }

    public void destroy() {
        this.isDead = true;
    }
}
