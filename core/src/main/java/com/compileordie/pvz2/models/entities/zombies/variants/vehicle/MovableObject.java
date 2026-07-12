package com.compileordie.pvz2.models.entities.zombies.variants.vehicle;

public abstract class MovableObject {
    protected int health;
    protected int maxHealth;
    protected boolean isDestroyed;

    public MovableObject(int health) {
        this.maxHealth = health;
        this.health = health;
        this.isDestroyed = false;
    }

    public void takeDamage(int amount) {
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

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }
}
