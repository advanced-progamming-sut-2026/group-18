package com.compileordie.pvz2.models.entities.zombies.variants.vehicle;

import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

public abstract class VehicleZombie extends Zombie {

    // شیء وسیله که زامبی هل می‌دهد
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
        // فیکس باگ مختصات: استفاده از سیستم سراسری GameEntity برای تغییر موقعیت X زامبی به سمت چپ
        // از سرعت فعلی (currentSpeed) استفاده می‌شود تا افکت‌های کندکننده به درستی روی حرکت اثر بگذارند
        setX(getX() - this.currentSpeed);
    }

    /**
     * بازنویسی متد حرکت کلاس ریشه
     */
    @Override
    public void move() {
        // چه وسیله سالم باشد و چه نابود شده باشد، جابجایی فیزیکی زامبی یکسان است.
        pushVehicle();
    }

    /**
     * طبق نمودار UML: واکنشی که پس از نابودی شیء/وسیله رخ می‌دهد
     * این متد توسط سرویس بازی به محض صفر شدن جان وسیله صدا زده می‌شود
     */
    public void onVehicleDestroyed() {
        this.isVehicleDestroyed = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;
    }

    public MovableObject getVehicle() {
        return vehicle;
    }

    public boolean isVehicleDestroyed() {
        return isVehicleDestroyed;
    }
}
