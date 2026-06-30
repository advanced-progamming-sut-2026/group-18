package com.compileordie.pvz2.models.entities.zombies.variants.vehicle;

import com.compileordie.pvz2.models.entities.zombies.variants.standard.ImpZombie;
import java.util.ArrayList;
import java.util.List;

public class Barrel extends MovableObject {

    private final int row;
    private double positionX;
    private final double positionY;
    private final List<ImpZombie> spawnedImps = new ArrayList<>();

    public Barrel(int barrelHealth, int row, double positionX, double positionY) {
        super(barrelHealth);
        this.row = row;
        this.positionX = positionX;
        this.positionY = positionY;
    }

    /**
     * همگام‌سازی مختصات دبه با حرکت زامبی (زمانی که زامبی زنده است و آن را هل می‌دهد)
     */
    public void updatePosition(double newX) {
        this.positionX = newX;
    }

    /**
     * داک: اعمال آسیب به دبه. چه زمانی که زامبی زنده است و چه زمانی که دبه روی زمین جا مانده است.
     */
    public void takeDamage(int amount, int impHealth, double impSpeed, int impAttackPower) {
        if (this.isDestroyed) return; // اگر از قبل خراب شده کاری نکن

        this.health -= amount;
        if (this.health <= 0) {
            this.health = 0;
            this.isDestroyed = true;
            // داک: در صورت خراب شدن دبه، دو imp در سطر فعلی از دبه در میایند
            triggerImpSpawn(impHealth, impSpeed, impAttackPower);
        }
    }

    private void triggerImpSpawn(int impHealth, double impSpeed, int impAttackPower) {
        for (int i = 0; i < 2; i++) {
            spawnedImps.add(new ImpZombie(
                impHealth, impSpeed, impAttackPower,
                this.row, this.positionX, this.positionX, this.positionY, 0, 0
            ));
        }
    }

    /**
     * سرویس بازی از این متد برای برداشتن امپ‌های تولید شده و افزودن آن‌ها به مپ استفاده می‌کند.
     */
    public List<ImpZombie> pollSpawnedImps() {
        List<ImpZombie> imps = new ArrayList<>(this.spawnedImps);
        this.spawnedImps.clear(); // لیست را خالی میکند تا دوباره تولید نشوند
        return imps;
    }

    /**
     * داک: تیرها از آن رد نمی‌شوند ولی زامبی‌ها چرا.
     * این پرچم به سرویس مدیریت برخورد شما می‌گوید زامبی‌های هم‌رزم می‌توانند بدون توقف از روی این دبه رد شوند.
     */
    public boolean isPassableByZombies() {
        return true;
    }

    public int getRow() { return row; }
    public double getPositionX() { return positionX; }
}
