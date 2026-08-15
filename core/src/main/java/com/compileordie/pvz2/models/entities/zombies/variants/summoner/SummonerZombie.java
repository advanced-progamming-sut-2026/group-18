package com.compileordie.pvz2.models.entities.zombies.variants.summoner;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.missions.quests.QuestEvent;
import com.compileordie.pvz2.models.missions.quests.QuestManager;

public abstract class SummonerZombie extends Zombie {
    protected double summonTime;
    protected double summonTimer;

    public SummonerZombie(double health, double speed, int attackPower, int row, double startX,
                          double x, double y, double xSpeed, double ySpeed, ZombieType type, double summonTime) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, type);
        this.summonTime = summonTime;
        this.summonTimer = 0;
    }

    @Override
    public void tick() {
        super.tick();
        float dt = 1 * Constants.Game.TIME_COEFFICIENT;
        summonTimer += dt;
        if (summonTimer >= summonTime) {
            summonTimer = 0;
            summon();
        }
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
    }

    public abstract void summon();
}
