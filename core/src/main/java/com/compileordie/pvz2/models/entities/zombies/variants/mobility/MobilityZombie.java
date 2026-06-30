package com.compileordie.pvz2.models.entities.zombies.variants.mobility;

import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

public abstract class MobilityZombie extends Zombie {
    protected double delta;

    // طبق نمودار UML: فیلد وضعیت حرکتی
    protected MovementState movementState;

    // پارامتر انعطاف‌پذیر برای تغییر سرعت در وضعیت‌های خاص (مثلاً کند شدن زیر آب)
    protected final double underwaterSpeedModifier;

    public MobilityZombie(int health, double speed, int attackPower, int row, double startX,
                          int initialArmor, double delta, double x, double y, int xSpeed, int ySpeed,
                          double underwaterSpeedModifier) {
        // فرستادن پارامترها به کلاس والد اصلی زامبی‌ها بدون هیچ عدد فرضی
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed);

        this.underwaterSpeedModifier = underwaterSpeedModifier;
        // وضعیت اولیه همه زامبی‌های حرکتی در بدو ورود
        this.movementState = MovementState.WALKING;
        this.delta = delta;
    }

    /**
     * طبق نمودار UML: تغییر وضعیت حرکتی زامبی به یک وضعیت جدید
     */
    public void changeMovementState(MovementState newState) {
        if (newState != null) {
            this.movementState = newState;
            updateMovementState();
        }
    }

    /**
     * طبق نمودار UML: به‌روزرسانی پارامترهای داخلی زامبی بر اساس وضعیت فعلی
     */
    public void updateMovementState() {
        switch (this.movementState) {
            case WALKING:
                this.currentSpeed = this.movementSpeed;
                break;
            case EATING:
                // زامبی برای جویدن کاملاً متوقف می‌شود (سرعت صفر واقعی فیزیکی)
                this.currentSpeed = 0.0;
                break;
            case FLYING:
                // منطق سرعت پرواز در کلاس فرزند (Dodo) به صورت پارامتریک مدیریت می‌شود
                break;
            case UNDERWATER:
                // اعمال ضریب جابه‌جایی زیر آب به صورت کاملاً پارامتریک
                this.currentSpeed = this.movementSpeed * this.underwaterSpeedModifier;
                break;
            default:
                break;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;

        // همگام‌سازی مداوم وضعیت حرکتی با تیک‌های بازی
        updateMovementState();
    }

    public MovementState getMovementState() {
        return this.movementState;
    }

    public void setDelta(double delta){
        this.delta = delta;
    }

}
