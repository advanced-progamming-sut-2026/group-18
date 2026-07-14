package com.compileordie.pvz2.models.entities.zombies.variants.mobility;

import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

public abstract class MobilityZombie extends Zombie {
    protected double delta;
    protected MovementState movementState;
    protected final double underwaterSpeedModifier;

    public MobilityZombie(int health, double speed, int attackPower, int row, double startX,
                          int initialArmor, double delta, double x, double y, double xSpeed, double ySpeed,
                          double underwaterSpeedModifier, ZombieType type) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, type);
        this.underwaterSpeedModifier = underwaterSpeedModifier;
        this.movementState = MovementState.WALKING;
        this.delta = delta;
    }

    public void changeMovementState(MovementState newState) {
        if (newState != null && this.movementState != newState) {
            this.movementState = newState;
            updateMovementState();
        }
    }

    /**
     * فیکس حیاتی: محاسبه سرعت بر اساس متد recalculateSpeed کلاس مادر انجام می‌شود
     * تا افکت‌های کندکننده (CHILLED) یا یخ‌زدگی (FREEZE) توسط وضعیت حرکتی ریست نشوند.
     */
    public void updateMovementState() {
        // ابتدا محاسبه سرعت پایه با لحاظ کردن افکت‌ها
        recalculateSpeed();

        switch (this.movementState) {
            case WALKING:
            case CHARGING:
                // سرعت همان سرعت محاسبه‌شده با افکت‌هاست
                break;
            case EATING:
                this.currentSpeed = 0.0;
                break;
            case FLYING:
                // در حالت پرواز، سرعت توسط فرزند مربوطه (دودو) ضریب می‌خورد
                break;
            case UNDERWATER:
                this.currentSpeed *= this.underwaterSpeedModifier;
                break;
            default:
                break;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;

        // آپدیت مستمر سرعت با حفظ افکت‌های سرویس‌ها
        updateMovementState();
    }

    public MovementState getMovementState() { return this.movementState; }
    public void setDelta(double delta){ this.delta = delta; }
}
