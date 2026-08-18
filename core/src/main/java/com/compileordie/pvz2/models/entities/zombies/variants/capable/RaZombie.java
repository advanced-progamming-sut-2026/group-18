package com.compileordie.pvz2.models.entities.zombies.variants.capable;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.game.economy.Sun;
import com.compileordie.pvz2.models.game.economy.SunType;
import com.compileordie.pvz2.models.missions.quests.QuestEvent;
import com.compileordie.pvz2.models.missions.quests.QuestManager;

public class RaZombie extends CapableZombie {
    public static final int WAVE_COST = 100;
    public int stolenSunCount;
    private double stealTime;
    private double stealTimer;
    private boolean shouldSteal = false;
    private boolean shouldBackSun = false;
    // 🌞 طبق منطق واقعی: وقتی شروع به دزدی می‌کنه، باید دقیقا ۱۰ ثانیه کامل
    // بایسته (صرف‌نظر از اینکه خورشیدی برای دزدیدن پیدا می‌کنه یا نه)، نه اینکه
    // به محض نبودِ خورشید در همون لحظه، دزدی رو قطع کنه. این تایمر مستقل از
    // stealTimer (که فاصله‌ی بین دو نوبت دزدیه) همین مدت ایستادن رو می‌شمره.
    public static final double STEAL_STAND_DURATION = 10.0;
    private double stealStandRemaining = 0;

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
        takedDamage = true;
        this.health -= amount;
        if (damageType == DamageType.FIRE){
            this.removeFrozen();
        }
        if (this.health <= 0) {
            this.health = 0;
            if (damageType==DamageType.EXPLOSIVE) killByExplosive = true;
            if (damageType==DamageType.LawnMower){
                QuestManager.dispatch(QuestEvent.ZOMBIE_KILLED_BY_PLANT, 1, "MOWER");
            }
            else{
                if (plantType!=null) QuestManager.dispatch(QuestEvent.ZOMBIE_KILLED_BY_PLANT, 1, plantType.name());
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
            stealStandRemaining = STEAL_STAND_DURATION;
        }
        // ⏳ تا وقتی در حال دزدیه، این ۱۰ ثانیه رو می‌شمریم؛ فقط وقتی کامل تموم
        // بشه واقعا shouldSteal رو false می‌کنیم - نه زودتر (نگاه کن به
        // ZombieManager.stealingByRaZombie که دیگه خودش زودهنگام stopStealing
        // صدا نمی‌زنه).
        if (shouldSteal) {
            stealStandRemaining -= dt;
            if (stealStandRemaining <= 0) {
                stealStandRemaining = 0;
                shouldSteal = false;
            }
        }
    }


    @Override
    public void handleDeath() {
        for (int i = 0; i < stolenSunCount / 25; i++) {
            AppModel.gameSession.gameBoard.economyManager.suns.add(new Sun(getX(), getY(), SunType.NORMAL, false, (float) getY()));
        }
//        shouldBackSun = true;
    }

    public boolean shouldWeSteal() {
        return shouldSteal;
    }

    public void stopStealing() {
        shouldSteal = false;
        stealStandRemaining = 0;
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
