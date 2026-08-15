package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.missions.quests.QuestEvent;
import com.compileordie.pvz2.models.missions.quests.QuestManager;

public class ExplorerZombie extends CapableZombie {
    public static final int WAVE_COST = 250;
    // TODO : در فاز گرافیک باید اصلاح دقیق بشود
    public static double enoghDistance = Constants.Game.TILE_WIDTH * 1.5;
    private boolean isTorchOn;

    public ExplorerZombie(double health, double speed, int attackPower, int row, double startX,
                          double x, double y, double xSpeed, double ySpeed) {
        // فراخوانی دقیق سازنده ۱۲ پارامتری CapableZombie موجود در فایل شما
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, ZombieType.EXPLORER_ZOMBIE);
        this.isTorchOn = true;
    }


    @Override
    public void takeDamage(double amount, DamageType damageType, PlantType plantType) {
        if (isDead()) return;

        if (damageType == DamageType.ICE) {
            isTorchOn = false;
        } else if (damageType == DamageType.FIRE) {
            this.removeStatusEffect(EffectType.FROZEN);
            this.removeStatusEffect(EffectType.CHILLED);
            isTorchOn = true;
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

    public boolean isTorchOn() {
        return this.isTorchOn;
    }

    public void offTorch() {
        isTorchOn = false;
    }

    public void onTorch() {
        isTorchOn = true;
    }
}
