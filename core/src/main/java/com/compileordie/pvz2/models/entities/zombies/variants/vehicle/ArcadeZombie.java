package com.compileordie.pvz2.models.entities.zombies.variants.vehicle;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class ArcadeZombie extends VehicleZombie {

    public ArcadeZombie(int health, double speed, int attackPower, int row, double startX,
                        double x, double y, int xSpeed, int ySpeed, double delta, int bucketHeadHealth) {
        // ایجاد دستگاه و فرستادن پارامترها به والد (VehicleZombie)
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, delta, new ArcadeMachine(bucketHeadHealth));
    }

    /**
     * طبق نمودار UML: هل دادن دستگاه آرکید رو به جلو
     */
    public void pushMachine() {
        super.pushVehicle();
    }

    /**
     * مدیریت رویداد برخورد فیزیکی با موانع (گیاه یا زامبی هیپنوتیزم شده)
     * این متد توسط سرویس مدیریت برخورد شما صدا زده می‌شود
     * @return boolean اگر true باشد یعنی دستگاه سالم است و هدف باید درجا له (حذف) شود
     */
    // TODO : مرتبط با سرویس
    public boolean onCollisionDetected() {
        if (!this.isVehicleDestroyed) {
            // داک: دستگاه سالم است، پس بدون توقف هل می‌دهد و هدف باید درجا له شود
            pushMachine();
            return true; // سیگنال به سرویس برای نابودی فوری هدف
        } else {
            // داک: دستگاه شکسته، پس مثل زامبی عادی ترمز می‌کند تا هدف را بجود
            startEating();
            return false; // سیگنال به سرویس که له کردنی در کار نیست
        }
    }

    /**
     * طبق نمودار UML: رویداد آسیب دیدن بدنه آرکید
     */
    public void onCabinetBreak(int damageAmount) {
        if (this.vehicle instanceof ArcadeMachine) {
            ArcadeMachine machine = (ArcadeMachine) this.vehicle;
            machine.takeDamage(damageAmount);

            if (machine.isDestroyed()) {
                onVehicleDestroyed(); // فعال شدن فلگ شکستن وسیله در کلاس پدر
            }
        }
    }

    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;

        // مکانیزم سپر: تا دستگاه سالمه، آسیب‌ها به دستگاه می‌خورد
        if (!this.isVehicleDestroyed) {
            onCabinetBreak(amount);
        } else {
            // بعد از نابودی دستگاه، آسیب به خود زامبی می‌رسد
            this.health -= amount;
            if (this.health < 0) this.health = 0;
        }
    }
}
