package com.compileordie.pvz2.models.entities.zombies.variants.vehicle;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.ImpZombie;
import java.util.ArrayList;
import java.util.List;

public class BarrelRollerZombie extends VehicleZombie {

    // ویژگی‌های امپ‌های داخل این دبه خاص
    private final int impHealth;
    private final double impSpeed;
    private final int impAttackPower;

    public BarrelRollerZombie(int health, double speed, int attackPower, int row, double startX,
                              double x, double y, int xSpeed, int ySpeed, double delta,
                              int barrelHealth, int impHealth, double impSpeed, int impAttackPower) {
        // ایجاد دبه در موقعیت اولیه زامبی
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, delta, new Barrel(barrelHealth, row, x, y));
        this.impHealth = impHealth;
        this.impSpeed = impSpeed;
        this.impAttackPower = impAttackPower;
    }

    public void pushBarrel() {
        super.pushVehicle(); // جابجایی زامبی به سمت چپ

        // آپدیت همزمان موقعیت دبه با زامبی
        if (this.vehicle instanceof Barrel) {
            ((Barrel) this.vehicle).updatePosition(this.positionX);
        }
    }

    @Override
    public void move() {
        if (!isVehicleDestroyed) {
            pushBarrel();
        } else {
            this.positionX -= this.currentSpeed; // حرکت عادی زامبی پیاده بعد از نابودی دبه
        }
    }

    /**
     * داک: برخورد به مانع گیاهی یا زامبی هیپنوتیزم شده -> ترمز میکند تا بجود (له کردن ندارد)
     */
    public boolean onCollisionDetected() {
        startEating();
        return false;
    }

    /**
     * مدیریت آسیب بر اساس تمامی سناریوهای دقیق داکیومنت شما
     */
    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;

        if (!this.isVehicleDestroyed) {
            // سناریو ۱: تیر قوسی (LOBBER) دبه را نادیده گرفته و مستقیم به خود زامبی آسیب می‌زند
            if (damageType == DamageType.LOBBER) {
                this.health -= amount;
            }
            // سناریو ۲: تیر مستقیم (STANDARD) به دبه برخورد می‌کند
            else if (!this.vehicle.isDestroyed()) {
                if (this.vehicle instanceof Barrel) {
                    Barrel barrel = (Barrel) this.vehicle;
                    barrel.takeDamage(amount, this.impHealth, this.impSpeed, this.impAttackPower);

                    if (barrel.isDestroyed()) {
                        onVehicleDestroyed(); // فعال شدن پرچم وسیله شکسته در کلاس پدر
                    }
                }
            }
        } else {
            // سناریو ۳: دبه قبلاً خراب شده و دمیج مستقیماً به خود زامبی می‌خورد
            this.health -= amount;
        }

        if (this.health < 0) this.health = 0;
    }

    /**
     * متد کمکی برای سرویس بازی جهت چک کردن امپ‌های متولد شده در حین حرکت زامبی
     */
    public List<ImpZombie> getSpawnedImpsFromVehicle() {
        if (this.vehicle instanceof Barrel) {
            return ((Barrel) this.vehicle).pollSpawnedImps();
        }
        return new ArrayList<>();
    }

    /**
     * داک: اگر زامبی قبل از خراب شدن دبه بمیرد، دبه سر جای خودش باقی می‌ماند.
     * سرویس بازی شما به محض اینکه دید زامبی مرد (isDead)، این متد را صدا می‌زند تا آبجکت مستقل دبه را
     * تحویل بگیرد و به عنوان یک مانع ثابت روی زمین مپ باقی بگذارد.
     */
    public Barrel detachBarrelOnDeath() {
        if (!this.isVehicleDestroyed && this.vehicle instanceof Barrel) {
            return (Barrel) this.vehicle;
        }
        return null;
    }
}
