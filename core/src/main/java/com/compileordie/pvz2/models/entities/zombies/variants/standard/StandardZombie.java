package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.missions.quests.QuestEvent;
import com.compileordie.pvz2.models.missions.quests.QuestManager;

public abstract class StandardZombie extends Zombie {
    protected double armorHealth;

    public StandardZombie(double health,
                          double speed,
                          int attackPower,
                          int row,
                          double startX,
                          double initialArmor,
                          double x,
                          double y,
                          double xSpeed,
                          double ySpeed,
                          ZombieType type) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, type);
        this.armorHealth = initialArmor;
    }

    public double takeArmorDamage(double amount) {
        if (hasArmor()) {
            this.armorHealth -= amount;
            if (this.armorHealth <= 0) {
                double overflow = -this.armorHealth;
                removeArmor();
                return overflow;
            }
            return 0;
        }
        return amount;
    }

    public boolean hasArmor() {
        return this.armorHealth > 0;
    }

    public void removeArmor() {
        this.armorHealth = 0;
    }

    public void enterEnrageMode() {
    }

    public double getArmorHealth() {
        return armorHealth;
    }

    public void setArmorHealth(double a) {
        this.armorHealth = a;
    }

    @Override
    public void takeDamage(double amount, DamageType damageType, PlantType plantType) {
        if (isDead()) return;
        double newAmount = amount;

        if (damageType == DamageType.FIRE){
            this.removeStatusEffect(EffectType.FROZEN);
            this.removeStatusEffect(EffectType.CHILLED);
        }

        // فیکس: آسیب‌های نادیده‌گیرنده زره مستقیماً به گوشت زامبی می‌خورند
        if (damageType != DamageType.BYPASS_ARMOR && hasArmor()) {
            newAmount = takeArmorDamage(amount);
        }

        this.health -= newAmount;
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
}
