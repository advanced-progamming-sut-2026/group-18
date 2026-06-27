package com.compileordie.pvz2.models.entities.zombies.variants.vehicle;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class TroglobiteZombie extends VehicleZombie {

    public TroglobiteZombie(int health, double speed, int attackPower, int row, double startX,
                            double x, double y, int xSpeed, int ySpeed, double delta, int iceBlockHealth) {
        // ساخت بلوک یخ و پاس دادن به کلاس پدر
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, delta, new IceBlock(iceBlockHealth, row, x));
    }

    public void pushIceBlock() {
        super.pushVehicle(); // جابجایی غارنشین

        // آپدیت همزمان مختصات یخ‌ها روی زمین
        if (this.vehicle instanceof IceBlock) {
            ((IceBlock) this.vehicle).updatePosition(this.positionX);
        }
    }

    @Override
    public void move() {
        if (!isVehicleDestroyed) {
            pushIceBlock();
        } else {
            this.positionX -= this.currentSpeed; // اگر یخ‌ها بشکنند، غارنشین عادی راه می‌رود
        }
    }

    /**
     * داک: در صورت برخورد یخ ها با گیاهان یا زامبی های هیپنوتیزم شده، درجا از بین می روند.
     * @return true به معنی دستور نابودی فوری (Instant Kill) برای سرویس بازی است.
     */
    public boolean onCollisionDetected() {
        if (!this.isVehicleDestroyed) {
            // یخ سالم است: هدف باید درجا نابود شود و زامبی توقف نمی‌کند
            return true;
        } else {
            // یخ شکسته است: غارنشین مثل زامبی عادی ترمز می‌کند تا هدف را بجود
            startEating();
            return false;
        }
    }

    /**
     * مدیریت کامل سناریوهای آسیب (دقیقاً مشابه دبه‌ای)
     */
    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;

        if (!this.isVehicleDestroyed) {
            // سناریو ۱: تیر قوسی (LOBBER) یخ‌ها را نادیده گرفته و به خود غارنشین می‌خورد
            if (damageType == DamageType.LOBBER) {
                this.health -= amount;
            }
            // سناریو ۲: آسیب‌های دیگر به یخ‌ها می‌خورند
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
            // سناریو ۳: یخ‌ها قبلاً از بین رفته‌اند، آسیب مستقیم به زامبی وارد می‌شود
            this.health -= amount;
        }

        if (this.health < 0) this.health = 0;
    }

    /**
     * سناریوی جا ماندن یخ‌ها در صورت مرگ زامبی با تیر قوسی.
     * سرویس بازی هنگام مرگ این زامبی، این متد را صدا زده و یخ را روی زمین مپ باقی می‌گذارد.
     */
    public IceBlock detachIceBlockOnDeath() {
        if (!this.isVehicleDestroyed && this.vehicle instanceof IceBlock) {
            return (IceBlock) this.vehicle;
        }
        return null;
    }
}
