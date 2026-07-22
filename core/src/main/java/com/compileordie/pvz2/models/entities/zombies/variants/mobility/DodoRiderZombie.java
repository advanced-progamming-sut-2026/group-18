package com.compileordie.pvz2.models.entities.zombies.variants.mobility;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.vehicle.MovableObject;

import java.util.EnumSet;

public class DodoRiderZombie extends Zombie {
    private double flightTimer;
    private double flightTime;
    private MovementState state;
    private double ySpeedForFlying;
    private int flyingDurationTicks;
    private int numOfFlyTicks;
    private boolean endOfFlying = false;
    private static final EnumSet<PlantType> FLYABLE_OBSTACLES = EnumSet.of(
        PlantType.WALL_NUT,
        PlantType.ENDURIAN,
        PlantType.PUMPKIN,
        PlantType.GARLIC,
        PlantType.SWEET_POTATO,
        PlantType.POTATO_MINE,
        PlantType.PRIMAL_POTATO_MINE,
        PlantType.CHERRY_BOMB,
        PlantType.SQUASH,
        PlantType.JALAPENO,
        PlantType.GRAPESHOT,
        PlantType.DOOM_SHROOM,
        PlantType.EXPLODE_O_NUT
    );

    public DodoRiderZombie(double health, double speed, int attackPower, int row, double startX,
                           double x, double y, double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, ZombieType.DODO_RIDER);
        this.state = MovementState.WALKING;
        this.flightTimer = 0; // counter
        this.flightTime = 5;  // basic
        this.ySpeedForFlying = 0.4;
        this.flyingDurationTicks = 20; // basic
        this.numOfFlyTicks = flyingDurationTicks; // counter
    }


    // به اندازه نصف تایل با فاصله از گیاه می پره و می سقوطه
    public void onPlantCollisionWithHalfOfTileWidth(PlantType type) {
        if (state == MovementState.WALKING) {
            if (!FLYABLE_OBSTACLES.contains(type)){ return; }
            //---
            state = MovementState.FLYING;
            setYSpeed(ySpeedForFlying);
        }
    }


    @Override
    public void move(int ticks){
        if (state == MovementState.WALKING){
            super.move(ticks);
            return;
        }
        //---
        if (numOfFlyTicks > 0) {
            numOfFlyTicks -= ticks;
            super.move(ticks);
        }
        if (numOfFlyTicks <= 0){
            if (endOfFlying){
                state = MovementState.WALKING;
                super.move(ticks);
                return;
            }

            numOfFlyTicks = 0;
            flightTimer += (ticks * Constants.Game.TIME_COEFFICIENT);
            setY(0);
            super.move(ticks);
            if (flightTimer >= flightTime){
                flightTimer = 0;
                numOfFlyTicks = flyingDurationTicks;
                setY(-ySpeedForFlying);
                endOfFlying = true;
            }
        }
    }

    @Override
    public void takeDamage(double amount, DamageType damageType) {
        if (isDead()) return;
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }

    public MovementState getState() { return state; }
}
