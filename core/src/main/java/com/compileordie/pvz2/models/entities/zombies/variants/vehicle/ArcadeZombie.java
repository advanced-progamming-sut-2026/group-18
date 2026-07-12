package com.compileordie.pvz2.models.entities.zombies.variants.vehicle;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class ArcadeZombie extends VehicleZombie {

    public ArcadeZombie(int health, double speed, int attackPower, int row, double startX,
                        double x, double y, int xSpeed, int ySpeed, double delta, int bucketHeadHealth) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, delta, new ArcadeMachine(bucketHeadHealth));
    }

    public void pushMachine() {
        super.pushVehicle();
    }

    @Override
    public void move() {
        if (!isVehicleDestroyed) {
            pushMachine();
        } else {
            setX(getX() - this.currentSpeed);
        }
    }

    /**
     * مدیریت رویداد برخورد فیزیکی با موانع (گیاهان یا زامبی‌های هیپنوتیزم شده)
     * @return boolean اگر true باشد یعنی دستگاه سالم است و گیاه باید درجا له (حذف) شود
     */
    public boolean onCollisionDetected() {
        if (!this.isVehicleDestroyed) {
            pushMachine();
            return true; // سیگنال لایه سرویس برای نابودی آنی گیاه برخورد کرده
        } else {
            startEating(); // دستگاه شکسته، پس مثل زامبی عادی ایستاده و می‌جود
            return false;
        }
    }

    public void onCabinetBreak(int damageAmount) {
        if (this.vehicle instanceof ArcadeMachine) {
            ArcadeMachine machine = (ArcadeMachine) this.vehicle;
            machine.takeDamage(damageAmount);

            if (machine.isDestroyed()) {
                onVehicleDestroyed();
            }
        }
    }

    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;

        // تا زمان سلامت دستگاه، کل دمیج‌ها به عنوان سپر جذب کابین آرکید می‌شوند
        if (!this.isVehicleDestroyed) {
            onCabinetBreak(amount);
        } else {
            this.health -= amount;
        }

        if (this.health < 0) this.health = 0;
    }
}
