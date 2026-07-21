package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class ImpZombie extends StandardZombie {
    public static final int waveCost = 100;
    private double originalGroundY;       // مختصات Y سطح زمین (برای تشخیص فرود دقیق روی زمین)

    public ImpZombie(double health, double speed, int attackPower, int row, double startX,
                     double x, double y, double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, 0, x, y, xSpeed, ySpeed, ZombieType.IMP);
        this.originalGroundY = y; // ذخیره مختصات خط زمین برای فرود آمدن در همان لاین
    }

    public double getOriginalGroundY() {
        return originalGroundY;
    }
    public void setOriginalGroundY(double originalGroundY) {
        this.originalGroundY = originalGroundY;
    }
}
