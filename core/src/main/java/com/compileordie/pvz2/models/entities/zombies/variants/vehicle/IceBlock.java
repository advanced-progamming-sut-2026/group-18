package com.compileordie.pvz2.models.entities.zombies.variants.vehicle;

public class IceBlock extends MovableObject {

    private final int row;
    private double positionX;

    public IceBlock(int health, int row, double positionX) {
        super(health);
        this.row = row;
        this.positionX = positionX;
    }

    /**
     * آپدیت موقعیت بلوک‌های یخ وقتی زامبی آن‌ها را هل می‌دهد
     */
    public void updatePosition(double newX) {
        this.positionX = newX;
    }

    @Override
    public void takeDamage(int amount) {
        if (this.isDestroyed) return;

        this.health -= amount;
        if (this.health <= 0) {
            this.health = 0;
            this.isDestroyed = true;
        }
    }

    /**
     * بلوک یخ سد راه تیرهاست، اما زامبی‌های هم‌جبهه می‌توانند از آن عبور کنند
     */
    public boolean isPassableByZombies() {
        return true;
    }

    public int getRow() {
        return row;
    }

    public double getPositionX() {
        return positionX;
    }
}
