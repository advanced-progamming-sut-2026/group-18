package com.compileordie.pvz2.models.entities.zombies.variants.mobility;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.missions.quests.QuestEvent;
import com.compileordie.pvz2.models.missions.quests.QuestManager;

public class ProspectorZombie extends Zombie {
    public static final int WAVE_COST = 200;
    private final double timeToExplode = 20.0;
    private boolean dynamiteActive;
    private double dynamiteTimer;
    public boolean isReversedDirection;
    private double homeColumnX;

    public ProspectorZombie(double health, double speed, int attackPower, int row, double startX,
                            double x, double y, double xSpeed, double ySpeed) {
        // فراخوانی دقیق سازنده ۱۲ پارامتری کلاس MobilityZombie شما
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, ZombieType.PROSPECTOR_ZOMBIE);

        this.homeColumnX = Constants.Game.PROSPECTOR_BOOM_X;
        this.dynamiteActive = true;
        this.dynamiteTimer = 0.0;
        this.isReversedDirection = false;
    }

    @Override
    public void tick() {
        super.tick();
        if (dynamiteActive) {
            float dt = 1 * Constants.Game.TIME_COEFFICIENT;
            dynamiteTimer += dt;
            if (dynamiteTimer >= timeToExplode) {
                dynamiteActive = false;
                isReversedDirection = true;
                setX(homeColumnX);
            }
        }
    }


    @Override
    public void move(int ticks) {
        if (this.isReversedDirection) {
            setXSpeed(-getStableSpeed());
        } else {
            setXSpeed(getStableSpeed());
        }
        super.move(ticks);
    }

    @Override
    public void takeDamage(double amount, DamageType damageType, PlantType plantType) {
        if (isDead()) return;
        boolean wasFrozenByIceBlock = isFrozenByIce;
        amount = absorbIceDamage(amount);
        if (wasFrozenByIceBlock && amount <= 0) return;
        if (damageType!=DamageType.POISON) takedDamage = true;
        if (damageType == DamageType.FIRE){
            this.removeStatusEffect(EffectType.FROZEN);
            this.removeStatusEffect(EffectType.CHILLED);
        }
        this.health -= amount;
        if (damageType == DamageType.ICE) {
            dynamiteActive = false;
            isReversedDirection = false;
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
