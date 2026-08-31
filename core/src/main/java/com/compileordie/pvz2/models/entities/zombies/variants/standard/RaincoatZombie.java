package com.compileordie.pvz2.models.entities.zombies.variants.standard;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.economy.Sun;
import com.compileordie.pvz2.models.game.economy.SunType;
import com.compileordie.pvz2.models.missions.quests.QuestEvent;
import com.compileordie.pvz2.models.missions.quests.QuestManager;

/**
 * زامبی بارونی (Raincoat): یه زامبی پشتیبان که اصلا حرکت نمی‌کنه (xSpeed=0)
 * ولی هر چند وقت یک‌بار برای زامبی‌ها خورشید تولید می‌کنه. فاصله‌ی زمانی بین
 * دو تولید، از ۲۰ ثانیه شروع می‌شه و با هر بار تولید، ۰.۸۵ برابر می‌شه (یعنی
 * هرچی بیشتر زنده بمونه، سریع‌تر خورشید تولید می‌کنه).
 *
 * از نظر مکانیزم دمیج خوردن/مردن، دقیقا مثل یه زامبی خیلی بیسیک (بدون زره)
 * عمل می‌کنه - همون منطق StandardZombie، فقط چون طبق درخواست باید مستقیم
 * extends Zombie باشه (نه StandardZombie)، اینجا عینا تکرار شده.
 */
public class RaincoatZombie extends Zombie {
    public static final double INITIAL_SUN_INTERVAL_SECONDS = 20.0;
    public static final double SUN_INTERVAL_MULTIPLIER = 0.85;

    private double sunTimer = 0;
    private double currentSunInterval = INITIAL_SUN_INTERVAL_SECONDS;

    public RaincoatZombie(double health,
                          double speed,
                          int attackPower,
                          int row,
                          double startX,
                          double x,
                          double y,
                          double xSpeed,
                          double ySpeed) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, ZombieType.RAINCOAT_ZOMBIE);
    }

    @Override
    public void tick() {
        super.tick();
        if (isDead() || stopZombieNow) return;

        sunTimer += Constants.Game.TIME_COEFFICIENT;
        if (sunTimer >= currentSunInterval) {
            sunTimer = 0;
            produceSun();
            currentSunInterval *= SUN_INTERVAL_MULTIPLIER;
        }
    }

    private void produceSun() {
        if (AppModel.gameSession == null) return;
        float ground = (float) getY();
        AppModel.gameSession.gameBoard.economyManager.suns.add(
            new Sun(getX(), getY(), SunType.MEDIUM, false, ground)
        );
    }

    @Override
    public void takeDamage(double amount, DamageType damageType, PlantType plantType) {
        if (isDead()) return;
        boolean wasFrozenByIceBlock = isFrozenByIce;
        amount = absorbIceDamage(amount);
        if (wasFrozenByIceBlock && amount <= 0) return;
        if (damageType != DamageType.POISON) takedDamage = true;

        if (damageType == DamageType.FIRE) {
            this.removeStatusEffect(EffectType.FROZEN);
            this.removeStatusEffect(EffectType.CHILLED);
        }

        this.health -= amount;
        if (health <= 0) {
            health = 0;
            if (damageType == DamageType.EXPLOSIVE) killByExplosive = true;
            if (damageType == DamageType.LawnMower) {
                QuestManager.dispatch(QuestEvent.ZOMBIE_KILLED_BY_PLANT, 1, "MOWER");
            } else {
                if (plantType != null) QuestManager.dispatch(QuestEvent.ZOMBIE_KILLED_BY_PLANT, 1, plantType.name());
            }
        }
    }
}
