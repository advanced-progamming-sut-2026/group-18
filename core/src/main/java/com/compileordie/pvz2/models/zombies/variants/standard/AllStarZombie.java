package com.compileordie.pvz2.models.zombies.variants.standard;

public class AllStarZombie extends StandardZombie {
    private boolean isCharging;
    private double chargeSpeedScale;

    public AllStarZombie(int health, double speed, int attackPower, int row, double startX, int initialArmor) {
        super(health, speed, attackPower, row, startX, initialArmor);
        this.isCharging = false;
        this.chargeSpeedScale = 3.0; // ضریب سرعت تکل
    }

    public void startCharge() {
        this.isCharging = true;
        this.currentSpeed = this.movementSpeed * chargeSpeedScale;
    }

    public void stopCharge() {
        this.isCharging = false;
        this.currentSpeed = this.movementSpeed;
    }

    public void crushPlant() {
        if (isCharging) {
            stopCharge();
            // TODO: منطق نابودی فوری گیاه
        }
    }
}
