package com.compileordie.pvz2.models.entities.zombies.variants.vehicle;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class TroglobiteZombie extends VehicleZombie {

    public TroglobiteZombie(int health, double speed, int attackPower, int row, double startX,
                            double x, double y, int xSpeed, int ySpeed, double delta, int iceBlockHealth) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, delta, new IceBlock(iceBlockHealth, row, x));
    }

    public void pushIceBlock() {
        super.pushVehicle();

        // اصلاح باگ مختصات: آپدیت همزمان یخ با getX() فیزیک سراسری
        if (this.vehicle instanceof IceBlock) {
            ((IceBlock) this.vehicle).updatePosition(this.getX());
        }
    }

    @Override
    public void move() {
        if (!isVehicleDestroyed) {
            pushIceBlock();
        } else {
            // حرکت عادی غارنشین پیاده بدون یخ
            setX(getX() - this.currentSpeed);
        }
    }

    /**
     * سیگنال‌دهی به بخش مدیریت برخوردها در سرویس‌ها
     */
    public boolean handlePlantCollision(Object plant) {
        if (isVehicleDestroyed) {
            startEating(); // اگر یخ شکسته، مثل زامبی عادی ترمز کرده و می‌جود
            return false;
        }
        return true; // اگر یخ سالم است، سیگنال برخورد و نابودی فوری گیاه را ارسال می‌کند
    }

    /**
     * مدیریت سناریوهای آسیب سپر و تیرهای قوسی
     */
    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;

        if (!this.isVehicleDestroyed) {
            // سناریو ۱: تیر قوسی (LOBBER) یخ‌ها را نادیده گرفته و به خود غارنشین می‌خورد
            if (damageType == DamageType.LOBBER) {
                this.health -= amount;
            }
            // سناریو ۲: آسیب‌های دیگر ابتدا جذب بلوک یخ می‌شوند
            else if (!this.vehicle.isDestroyed()) {
                if (this.vehicle instanceof IceBlock) {
                    IceBlock ice = (IceBlock) this.vehicle;
                    ice.takeDamage(amount);

                    if (ice.isDestroyed()) {
                        onVehicleDestroyed();
                    }
                }
            }
        } else {
            // سناریو ۳: یخ‌ها قبلاً از بین رفته‌اند، آسیب مستقیم به خود زامبی می‌رسد
            this.health -= amount;
        }

        if (this.health < 0) this.health = 0;
    }

    /**
     * سناریوی جا ماندن یخ‌ها در صورت مرگ زامبی با تیر قوسی
     */
    public IceBlock detachIceBlockOnDeath() {
        if (!this.isVehicleDestroyed && this.vehicle instanceof IceBlock) {
            return (IceBlock) this.vehicle;
        }
        return null;
    }
}
