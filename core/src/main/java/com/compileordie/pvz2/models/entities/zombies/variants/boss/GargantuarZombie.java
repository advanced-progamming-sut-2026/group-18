package com.compileordie.pvz2.models.entities.zombies.variants.boss;

import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;

public class GargantuarZombie extends Zombie {
    private int phase;
    private boolean hasTriggeredSpecialAction;
    private boolean impThrown;
    private final double healthThresholdToThrowImp;
    private final int impTargetColumn;

    public GargantuarZombie(int health, double speed, int attackPower, int row, double startX,
                            double x, double y, double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, ZombieType.GARGANTUAR);
        this.phase = 1;
        this.hasTriggeredSpecialAction = false;
        this.impThrown = false;
        this.healthThresholdToThrowImp = health / 2.0;
        this.impTargetColumn = 3;
    }

    public void changePhase() { this.phase++; }

    public void triggerSpecialAction() {
        if (!this.impThrown) {
            throwImp();
        }
    }

    public void throwImp() {
        this.impThrown = true;
        this.hasTriggeredSpecialAction = true;
        changePhase();
    }

    /**
     * سیگنال له کردن گیاه به ZombieCombatService
     * @return true یعنی سرویس باید گیاه را بدون مکث نابود کند
     */
    public boolean smashPlant() {
        return true;
    }

    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;

        this.health -= amount;
        if (this.health < 0) this.health = 0;

        // بررسی خودکار شرایط پرتاب Imp بعد از دریافت آسیب
        if (!this.impThrown && this.health <= this.healthThresholdToThrowImp) {
            triggerSpecialAction();
        }
    }

    public int getPhase() { return phase; }
    public boolean isHasTriggeredSpecialAction() { return hasTriggeredSpecialAction; }
    public boolean isImpThrown() { return impThrown; }
    public int getImpTargetColumn() { return impTargetColumn; }
}
