package com.compileordie.pvz2.models.entities.zombies.variants.mobility;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.missions.quests.QuestEvent;
import com.compileordie.pvz2.models.missions.quests.QuestManager;

import java.util.EnumSet;

public class DodoRiderZombie extends Zombie {
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
    private double flightTimer;
    private double flightTime;
    private MovementState state;
    private double ySpeedForFlying;
    private int flyingDurationTicks;
    private int numOfFlyTicks;
    private boolean endOfFlying = false;
    public double stableY;

    public DodoRiderZombie(double health, double speed, int attackPower, int row, double startX,
                           double x, double y, double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, ZombieType.DODO_RIDER);
        this.state = MovementState.WALKING;
        this.flightTimer = 0; // counter
        this.flightTime = 1.5;  // basic
        this.ySpeedForFlying = 0.4;
        this.flyingDurationTicks = 20; // basic
        this.numOfFlyTicks = flyingDurationTicks; // counter
        this.stableY = y;
    }


    // به اندازه نصف تایل با فاصله از گیاه می پره و می سقوطه
    public void onPlantCollisionWithHalfOfTileWidth(PlantType type) {
        if (state == MovementState.WALKING) {
            if (!FLYABLE_OBSTACLES.contains(type)) {
                return;
            }
            //---
            state = MovementState.FLYING;
            setYSpeed(ySpeedForFlying);
        }
    }


    @Override
    public void move(int ticks) {
        if (state == MovementState.WALKING) {
            setYSpeed(0);
            super.move(ticks);
            return;
        }
        //---
        if (numOfFlyTicks > 0) {
            numOfFlyTicks -= ticks;
            super.move(ticks);
        }
        if (numOfFlyTicks <= 0) {
            if (endOfFlying) {
                state = MovementState.WALKING;
                super.move(ticks);
                return;
            }

            numOfFlyTicks = 0;
            flightTimer += (ticks * Constants.Game.TIME_COEFFICIENT);
            setYSpeed(0);
            super.move(ticks);
            if (flightTimer >= flightTime) {
                flightTimer = 0;
                numOfFlyTicks = flyingDurationTicks;
                setYSpeed(-ySpeedForFlying);
                endOfFlying = true;
            }
        }
    }

    @Override
    public void takeDamage(double amount, DamageType damageType, PlantType plantType) {
        if (damageType == DamageType.FIRE){
            this.removeStatusEffect(EffectType.FROZEN);
            this.removeStatusEffect(EffectType.CHILLED);
        }
        if (isDead()) return;
        boolean wasFrozenByIceBlock = isFrozenByIce;
        amount = absorbIceDamage(amount);
        if (wasFrozenByIceBlock && amount <= 0) return;
        if (damageType!=DamageType.POISON) takedDamage = true;
        this.health -= amount;
        if (health <= 0){
            health = 0;
            if (damageType==DamageType.EXPLOSIVE) killByExplosive = true;
            if (damageType==DamageType.LawnMower){
                QuestManager.dispatch(QuestEvent.ZOMBIE_KILLED_BY_PLANT, 1, "MOWER");
            }
            else{
                if (plantType!=null) QuestManager.dispatch(QuestEvent.ZOMBIE_KILLED_BY_PLANT, 1, plantType.name());
            }
        }
    }

    public MovementState getState() {
        return state;
    }
}
