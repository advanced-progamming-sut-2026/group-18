package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.missions.quests.QuestEvent;
import com.compileordie.pvz2.models.missions.quests.QuestManager;

public class HunterZombie extends CapableZombie {
    public static final int WAVE_COST = 500;
    public static final float ABILITY_COOLDOWN = 0.7f;
    public static final double ABILITY_RANGE = Constants.Game.TILE_WIDTH * 1.5;
    private boolean shouldAttack = false;
    private boolean shouldShut = false;
    private double timer = 0;
    private int shutCounter = 0;

    public HunterZombie(double health, double speed, int attackPower, int row, double startX,
                        double x, double y,
                        double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, ZombieType.HUNTER_ZOMBIE);
    }

    @Override
    public void move(int ticks) {
        if (shouldAttack) return;
        super.move(ticks);
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead()) return;
        //---
        float dt = 1 * Constants.Game.TIME_COEFFICIENT;
        if (shouldAttack) {
            timer += dt;
            //---
            if (shutCounter == 3) {
                if (timer >= ABILITY_COOLDOWN) {
                    shutCounter = 0;
                    shouldAttack = false;
                    shouldShut = false;
                    timer = 0;
                }
            } else if (timer >= ABILITY_COOLDOWN) {
                shouldShut = true;
                shutCounter++;
                timer = 0;
            }
        }
    }


    @Override
    public void takeDamage(double amount, DamageType damageType, PlantType plantType) {
        if (isDead()) return;
        this.health -= amount;
        if (damageType == DamageType.FIRE){
            this.removeFrozen();
        }
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

    public boolean getShouldAttack() {
        return shouldAttack;
    }

    public void setShouldAttack(boolean a) {
        shouldAttack = a;
    }

    public boolean getShouldShut() {
        return shouldShut;
    }

    public void setShouldShut(boolean a) {
        shouldShut = a;
    }
}
