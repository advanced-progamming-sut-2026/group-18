package com.compileordie.pvz2.models.entities.zombies.variants.summoner;

public class Tomb {
    private int health;
    private final int row;
    private final double positionX;
    private boolean isDestroyed;

    public Tomb(int health, int row, double positionX) {
        this.health = health;
        this.row = row;
        this.positionX = positionX;
        this.isDestroyed = false;
    }

    /**
     * اعمال آسیب به قبر توسط تیرهای مستقیم گیاهان
     */
    public void takeDamage(int amount) {
        if (isDestroyed) return;

        this.health -= amount;
        if (this.health <= 0) {
            this.health = 0;
            this.isDestroyed = true;
        }
    }

    // گترها برای استفاده در لایه رندر و سرویس برخورد تیرها
    public int getRow() { return row; }
    public double getPositionX() { return positionX; }
    public boolean isDestroyed() { return isDestroyed; }
}
