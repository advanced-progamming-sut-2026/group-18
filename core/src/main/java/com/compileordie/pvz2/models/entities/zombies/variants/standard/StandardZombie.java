package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.missions.quests.QuestEvent;
import com.compileordie.pvz2.models.missions.quests.QuestManager;

public abstract class StandardZombie extends Zombie {
    protected double armorHealth;
    // 🛡️ ماکزیمم HP اولیه‌ی زره (ثابت، هیچ‌وقت کم نمی‌شه) - صرفا برای محاسبه‌ی
    // درصد سلامت زره (armorHealth / maxArmorHealth) استفاده می‌شه تا View بتونه
    // مرحله‌ی صدمه‌دیدگی بصری زره (norm/damage_01/damage_02) رو تعیین کنه.
    protected double maxArmorHealth;

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
        this.maxArmorHealth = initialArmor;
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

    public double getMaxArmorHealth() {
        return maxArmorHealth;
    }

    @Override
    public void takeDamage(double amount, DamageType damageType, PlantType plantType) {
        if (isDead()) return;
        takedDamage = true;
        double newAmount = amount;

        if (damageType == DamageType.FIRE){
            this.removeFrozen();
        }

        // فیکس: آسیب‌های نادیده‌گیرنده زره مستقیماً به گوشت زامبی می‌خورند
        if (damageType != DamageType.BYPASS_ARMOR && hasArmor()) {
            newAmount = takeArmorDamage(amount);
        }

        this.health -= newAmount;
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
