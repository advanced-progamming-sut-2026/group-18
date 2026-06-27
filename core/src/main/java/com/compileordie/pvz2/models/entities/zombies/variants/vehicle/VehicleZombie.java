package com.compileordie.pvz2.models.entities.zombies.variants.vehicle;

import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

public abstract class VehicleZombie extends Zombie {

    // شیء وسیله که زامبی هل می‌دهد (تایپ آن را طبق نمودار Object یا کلاس اختصاصی می‌گذاریم)
    protected MovableObject vehicle;
    protected boolean isVehicleDestroyed;
    protected double delta;

    public VehicleZombie(int health, double speed, int attackPower, int row, double startX,
                         double x, double y, int xSpeed, int ySpeed, double delta, MovableObject vehicle) {
        // فرستادن مستقیم پارامترها به کلاس ریشه (Zombie)
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed);

        this.delta = delta;
        this.vehicle = vehicle;
        this.isVehicleDestroyed = false;
    }

    /**
     * طبق نمودار UML: منطق هل دادن وسیله رو به جلو
     */
    public void pushVehicle() {
        // وسیله و زامبی با هم به سمت چپ حرکت می‌کنند
        // از سرعت فعلی (currentSpeed) کلاس ریشه استفاده می‌شود که افکت‌های یخ و... هم رویش اثر بگذارند
        this.positionX -= this.currentSpeed;
    }

    /**
     * بازنویسی متد حرکت کلاس ریشه
     */
    @Override
    public void move() {
        if (!isVehicleDestroyed) {
            pushVehicle(); // اگر وسیله سالم است، آن را هل می‌دهد
        } else {
            // اگر وسیله نابود شده، زامبی مثل یک زامبی عادی بدون وسیله راه می‌رود
            this.positionX -= this.currentSpeed;
        }
    }

    /**
     * طبق نمودار UML: واکنشی که پس از نابودی شیء/وسیله رخ می‌دهد
     * این متد توسط سرویس بازی (سرویس مدیریت وسیله‌ها) به محض صفر شدن جان وسیله صدا زده می‌شود
     */
    public void onVehicleDestroyed() {
        this.isVehicleDestroyed = true;
        // اینجا وسیله از دست می‌رود؛ در کلاس‌های فرزند رفتارهای اختصاصی (مثل آزاد شدن مینی‌زامبی‌ها) به این اضافه می‌شود
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;
        // مدیریت تیک‌های مربوط به وسیله در صورت نیاز
    }

    // گترها و سترها
    public boolean isVehicleDestroyed() {
        return isVehicleDestroyed;
    }

    public Object getVehicle() {
        return vehicle;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }
}
