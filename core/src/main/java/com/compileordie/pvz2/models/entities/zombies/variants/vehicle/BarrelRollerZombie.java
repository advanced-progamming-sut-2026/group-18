package com.compileordie.pvz2.models.entities.zombies.variants.vehicle;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.ImpZombie;
import java.util.ArrayList;
import java.util.List;

public class BarrelRollerZombie extends VehicleZombie {

    public static final int waveCost = 500;
//    protected boolean isVehicleDestroyed = false;

    public BarrelRollerZombie(double health, double speed, int attackPower, int row, double startX,
                              double x, double y, double xSpeed, double ySpeed,
                              double barrelHealth) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, new Barrel(barrelHealth, row, x, y), ZombieType.BARREL_ROLLER);
    }

    public void pushBarrel(int ticks) {
        super.pushVehicle(ticks);

        if (this.vehicle instanceof Barrel) {
            ((Barrel) this.vehicle).updatePosition(this.getX());
        }
    }

    @Override
    public void move(int ticks) {

        if (!isVehicleDestroyed()) {
            pushBarrel(ticks);
        } else {
            ((Barrel) this.vehicle).updatePosition(this.getX());
            float dt = ticks * Constants.Game.TIME_COEFFICIENT;
            setX(getX() - getXSpeed() * dt);
        }
    }

    @Override
    public void takeDamage(double amount, DamageType damageType) {
        if (isDead()) return;

        if (!this.isVehicleDestroyed()) {
            if (damageType == DamageType.LOBBER) {
                this.health -= amount;
                if (this.health <= 0){

                }
            }
            else if (!this.vehicle.isDestroyed()) {
                if (this.vehicle instanceof Barrel) {
                    Barrel barrel = (Barrel) this.vehicle;
                    barrel.takeDamage(amount);
                }
            }
        } else {
            this.health -= amount;
        }

        if (this.health < 0) this.health = 0;
    }
}
