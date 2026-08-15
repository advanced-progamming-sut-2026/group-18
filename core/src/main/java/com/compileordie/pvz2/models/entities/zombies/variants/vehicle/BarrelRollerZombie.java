package com.compileordie.pvz2.models.entities.zombies.variants.vehicle;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.missions.quests.QuestEvent;
import com.compileordie.pvz2.models.missions.quests.QuestManager;

public class BarrelRollerZombie extends VehicleZombie {

    public static final int WAVE_COST = 500;
//    protected boolean isVehicleDestroyed = false;

    public BarrelRollerZombie(double health, double speed, int attackPower, int row, double startX,
                              double x, double y, double xSpeed, double ySpeed,
                              double barrelHealth) {
        super(health,
            speed,
            attackPower,
            row,
            startX,
            x,
            y,
            xSpeed,
            ySpeed,
            new Barrel(barrelHealth, row, x, y),
            ZombieType.BARREL_ROLLER);
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
    public void takeDamage(double amount, DamageType damageType, PlantType plantType) {
        if (isDead()) return;
        takedDamage = true;
        if (damageType == DamageType.FIRE){
            this.removeFrozen();
        }

        if (!this.isVehicleDestroyed()) {
            if (damageType == DamageType.LOBBER) {
                this.health -= amount;
                if (this.health <= 0) {

                }
            } else if (!this.vehicle.isDestroyed()) {
                if (this.vehicle instanceof Barrel) {
                    Barrel barrel = (Barrel) this.vehicle;
                    barrel.takeDamage(amount);
                }
            }
        } else {
            this.health -= amount;
        }

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
}
