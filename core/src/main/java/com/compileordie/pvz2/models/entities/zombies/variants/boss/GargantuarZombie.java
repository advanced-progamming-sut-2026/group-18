package com.compileordie.pvz2.models.entities.zombies.variants.boss;

import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.missions.quests.QuestEvent;
import com.compileordie.pvz2.models.missions.quests.QuestManager;


public class GargantuarZombie extends Zombie {
    public static final int WAVE_COST = 1500;
    private final int impTargetColumn;
    private boolean spawnImp = false;
    private double healthThresholdToThrowImp;

    public GargantuarZombie(double health, double speed, int attackPower, int row, double startX,
                            double x, double y, double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, ZombieType.GARGANTUAR);
        this.healthThresholdToThrowImp = health / 2.0;
        this.impTargetColumn = 3;

    }

    @Override
    public void takeDamage(double amount, DamageType damageType, PlantType plantType) {
        if (isDead()) return;

        if (damageType == DamageType.FIRE){
            this.removeStatusEffect(EffectType.FROZEN);
            this.removeStatusEffect(EffectType.CHILLED);
        }

        this.health -= amount;
        if (health <= 0){
            health = 0;
            if (damageType==DamageType.LawnMower){
                QuestManager.dispatch(QuestEvent.ZOMBIE_KILLED_BY_PLANT, 1, "MOWER");
            }
            else{
                QuestManager.dispatch(QuestEvent.ZOMBIE_KILLED_BY_PLANT, 1, plantType.name());
            }
        }

        if (this.health <= this.healthThresholdToThrowImp) {
            spawnImp = true;
        }
    }

    public boolean shouldWeSpawnImp() {
        return spawnImp;
    }

    public void stopSpawnImp() {
        this.spawnImp = false;
    }

    public int getImpTargetColumn() {
        return impTargetColumn;
    }

}
