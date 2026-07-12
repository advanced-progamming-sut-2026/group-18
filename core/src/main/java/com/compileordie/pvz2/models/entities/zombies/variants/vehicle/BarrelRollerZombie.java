package com.compileordie.pvz2.models.entities.zombies.variants.vehicle;

import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.ImpZombie;
import java.util.ArrayList;
import java.util.List;

public class BarrelRollerZombie extends VehicleZombie {

    private final int impHealth;
    private final double impSpeed;
    private final int impAttackPower;

    public BarrelRollerZombie(int health, double speed, int attackPower, int row, double startX,
                              double x, double y, int xSpeed, int ySpeed, double delta,
                              int barrelHealth, int impHealth, double impSpeed, int impAttackPower) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, delta, new Barrel(barrelHealth, row, x, y));
        this.impHealth = impHealth;
        this.impSpeed = impSpeed;
        this.impAttackPower = impAttackPower;
    }

    public void pushBarrel() {
        super.pushVehicle();

        if (this.vehicle instanceof Barrel) {
            ((Barrel) this.vehicle).updatePosition(this.getX());
        }
    }

    @Override
    public void move() {
        if (!isVehicleDestroyed) {
            pushBarrel();
        } else {
            setX(getX() - this.currentSpeed);
        }
    }

    @Override
    public void takeDamage(int amount, DamageType damageType) {
        if (isDead()) return;

        if (!this.isVehicleDestroyed) {
            // سناریو ۱: تیر قوسی دبه را نادیده می‌گیرد و به گوشت زامبی می‌خورد
            if (damageType == DamageType.LOBBER) {
                this.health -= amount;
            }
            // سناریو ۲: دمیج‌های عادی به بدنه دبه می‌خورند
            else if (!this.vehicle.isDestroyed()) {
                if (this.vehicle instanceof Barrel) {
                    Barrel barrel = (Barrel) this.vehicle;
                    barrel.takeDamage(amount, this.impHealth, this.impSpeed, this.impAttackPower);

                    if (barrel.isDestroyed()) {
                        onVehicleDestroyed();
                    }
                }
            }
        } else {
            this.health -= amount;
        }

        if (this.health < 0) this.health = 0;
    }

    public List<ImpZombie> getSpawnedImpsFromVehicle() {
        if (this.vehicle instanceof Barrel) {
            return ((Barrel) this.vehicle).pollSpawnedImps();
        }
        return new ArrayList<>();
    }

    /**
     * داک: اگر زامبی پیش از بشکه بمیرد، بشکه به عنوان مانع ثابت روی زمین مپ جا می‌ماند
     */
    public Barrel detachBarrelOnDeath() {
        if (!this.isVehicleDestroyed && this.vehicle instanceof Barrel) {
            return (Barrel) this.vehicle;
        }
        return null;
    }
}
