package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.missions.quests.QuestEvent;
import com.compileordie.pvz2.models.missions.quests.QuestManager;

public class RaZombie extends CapableZombie {
    public static final int WAVE_COST = 100;
    public int stolenSunCount;
    private double stealTime;
    private double stealTimer;
    private boolean shouldSteal = false;
    private boolean shouldBackSun = false;

    public RaZombie(double health,
                    double speed,
                    int attackPower,
                    int row,
                    double startX,
                    double x,
                    double y,
                    double xSpeed,
                    double ySpeed) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, ZombieType.RA_ZOMBIE);
        this.stealTime = 10.0;
        this.stealTimer = 0;
        this.stolenSunCount = 0;
    }

    @Override
    public void takeDamage(double amount, DamageType damageType, PlantType plantType) {
        if (isDead()) return;
        this.health -= amount;
        if (damageType == DamageType.FIRE){
            this.removeStatusEffect(EffectType.FROZEN);
            this.removeStatusEffect(EffectType.CHILLED);
        }
        if (this.health <= 0) {
            this.health = 0;
            if (damageType==DamageType.LawnMower){
                QuestManager.dispatch(QuestEvent.ZOMBIE_KILLED_BY_PLANT, 1, "MOWER");
            }
            else{
                QuestManager.dispatch(QuestEvent.ZOMBIE_KILLED_BY_PLANT, 1, plantType.name());
            }
            handleDeath();
        }
    }

    @Override
    public void move(int ticks) {
        if (shouldSteal) return;
        super.move(ticks);
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;
        //---
        float dt = 1 * Constants.Game.TIME_COEFFICIENT;
        if (!shouldSteal) stealTimer += dt;
        if (stealTimer >= stealTime) {
            stealTimer = 0;
            shouldSteal = true;
        }
    }


    @Override
    public void handleDeath() {
        shouldBackSun = true;
    }

    public boolean shouldWeSteal() {
        return shouldSteal;
    }

    public void stopStealing() {
        shouldSteal = false;
    }

    public boolean shouldWeBackSun() {
        return shouldBackSun;
    }

    public void stopBackSun() {
        shouldBackSun = false;
    }

    public void addStolen(int amount) {
        this.stolenSunCount += amount;
    }
}
