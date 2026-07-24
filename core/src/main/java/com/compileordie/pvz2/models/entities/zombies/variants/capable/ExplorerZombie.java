package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

public class ExplorerZombie extends CapableZombie {
    public static final int waveCost = 250;
    // TODO : در فاز گرافیک باید اصلاح دقیق بشود
    public static double enoghDistance = Constants.Game.TILE_WIDTH * 1.5;
    private boolean isTorchOn;

    public ExplorerZombie(int health, double speed, int attackPower, int row, double startX,
                          double x, double y, double xSpeed, double ySpeed) {
        // فراخوانی دقیق سازنده ۱۲ پارامتری CapableZombie موجود در فایل شما
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, ZombieType.EXPLORER_ZOMBIE);
        this.isTorchOn = true;
    }


    @Override
    public void takeDamage(double amount, DamageType damageType) {
        if (isDead()) return;

        if (damageType == DamageType.ICE) {
            isTorchOn = false;
        } else if (damageType == DamageType.FIRE) {
            isTorchOn = true;
        }
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }

    public boolean isTorchOn() {
        return this.isTorchOn;
    }

    public void offTorch() {
        isTorchOn = false;
    }

    private void onTorch() {
        isTorchOn = true;
    }
}
