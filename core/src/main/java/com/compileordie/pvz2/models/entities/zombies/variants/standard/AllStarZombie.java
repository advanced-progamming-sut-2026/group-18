package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class AllStarZombie extends StandardZombie {
    public static final int WAVE_COST = 1000;
    private final double chargeSpeedScale;
    private boolean isCharging;

    public AllStarZombie(double health, double speed, int attackPower, int row, double startX
        , double chargeSpeedScale, double x, double y,
                         double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, 0, x, y, xSpeed, ySpeed, ZombieType.ALL_STAR);
        this.chargeSpeedScale = chargeSpeedScale;
        startCharge();
    }

    public void startCharge() {
        this.isCharging = true;
        recalculateSpeed();
    }

    public void stopCharge() {
        this.isCharging = false;
        recalculateSpeed();
    }

    // در آینده موقع بر خورد آل استار موقع محاسبه دمیج کافیست نوشته شود : damage + onPlantCollision()
    public double onPlantCollision() {
        if (isCharging) {
            stopCharge(); // تکل تمام می‌شود
            return 99999.0; // در واقع میزان دمیج برای اولین برخورد با گیاه
        }
        return 0;
    }

    // در آینده موقع بر خورد آل استار موقع محاسبه دمیج کافیست نوشته شود : damage + onPlantCollision()
    public double onZombieCollision() {
        if (isCharging) {
            return 99999.0;
        }
        return 0;
    }

    public void recalculateSpeed() {
        if (isCharging) {
            setXSpeed(getXSpeed() * chargeSpeedScale);
        } else {
            setXSpeed(getStableSpeed());
        }
    }

    public boolean isCharging() {
        return this.isCharging;
    }
}
