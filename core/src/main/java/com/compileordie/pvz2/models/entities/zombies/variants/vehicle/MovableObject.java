package com.compileordie.pvz2.models.entities.zombies.variants.vehicle;

import com.compileordie.pvz2.models.entities.GameEntity;
import com.compileordie.pvz2.models.entities.obstacles.Obstacle;
import com.compileordie.pvz2.models.entities.obstacles.ObstacleType;

public abstract class MovableObject extends Obstacle {
    protected double health;
    protected double maxHealth;
    protected boolean isDestroyed;

    public MovableObject(double health, double x, double y, ObstacleType type) {
        super(x, y, type);
        this.maxHealth = health;
        this.health = health;
        this.isDestroyed = false;
    }

    public void takeDamage(double amount) {
        if (isDestroyed) return;
        this.health -= amount;
        if (this.health <= 0) {
            this.health = 0;
            this.isDestroyed = true;
        }
    }

    public boolean isDestroyed() {
        return this.isDestroyed;
    }
    public double getHealth() {
        return health;
    }
    public double getMaxHealth() {
        return maxHealth;
    }
}
