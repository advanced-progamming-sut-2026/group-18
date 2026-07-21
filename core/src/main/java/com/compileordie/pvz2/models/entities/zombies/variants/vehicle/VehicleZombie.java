package com.compileordie.pvz2.models.entities.zombies.variants.vehicle;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

public abstract class VehicleZombie extends Zombie {

    // شیء وسیله که زامبی هل می‌دهد
    protected MovableObject vehicle;
    public VehicleZombie(double health, double speed, int attackPower, int row, double startX,
                         double x, double y, double xSpeed, double ySpeed, MovableObject vehicle, ZombieType type) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, type);
        this.vehicle = vehicle;
    }

    public void pushVehicle(int ticks) {
        float dt = ticks * Constants.Game.TIME_COEFFICIENT;
        setX(getX() - getXSpeed() * dt);
    }

    @Override
    public void move(int ticks) {
        pushVehicle(ticks);
    }



    public MovableObject getVehicle() {
        return vehicle;
    }
    public boolean isVehicleDestroyed() {
        return (vehicle.getHealth() <= 0);
    }
}
