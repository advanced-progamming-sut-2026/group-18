package com.compileordie.pvz2.models.entities.zombies.variants.boss;

import com.compileordie.pvz2.config.Constants;
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
    public boolean canSpawn = true;
    private double healthThresholdToThrowImp;

    // --- برای انیمیشن fire/cannon_fire موقع پرتاب ایمپ ---
    // این عدد صرفا مدت زمانیه که View باید "fire" و بعدش "cannon_fire" رو پشت سر
    // هم پخش کنه (بعدش خودکار برمی‌گرده به حالت عادی گارگانچوا). اگه مدت واقعی
    // این دو انیمیشن (طبق فایل PAM واقعی) فرق داشت، همین عدد رو (و تقسیمش در
    // GameScreen بین fire/cannon_fire) تنظیم کن.
    public static final double FIRE_SEQUENCE_DURATION = 1.3;
    private double fireSequenceRemaining = 0;

    // --- برای اطمینان از پخش کامل حداقل یک سیکل انیمیشن eat/smash_left ---
    // مشکل: توی ZombieCombatManager، isEating هر تیک ریست می‌شه و فقط اگه
    // گارگانچوار همون لحظه دقیقا کنار یه گیاهِ زنده باشه دوباره true می‌شه.
    // وقتی دمیج هر ضربه‌ی گارگانچوار (attackPower) از سلامتی گیاه بیشتره،
    // گیاه همون یک تیک می‌میره و از تخته حذف می‌شه؛ تیک بعدی دیگه هیچ گیاهی
    // برای eating پیدا نمی‌شه، پس isEating فورا false می‌شه و View
    // (ZombieDrawer) به‌جای تمومِ سیکل eat(1.3s)+smash_left(1.8s)، همون لحظه
    // می‌پره رو walk - یعنی انیمیشن حمله عملا اصلا دیده نمی‌شه.
    // این تایمر دقیقا مثل fireSequenceRemaining بالا، مستقل از isEating واقعیِ
    // مدله: هر تیکی که گارگانچوار واقعا به یه گیاه دمیج بزنه (چه گیاه بمیره
    // چه نه) این‌جا رفرش می‌شه به مدت زمانِ کامل یک سیکل eat+smash_left، و از
    // اونجا خودش می‌شمره پایین - حتی اگه گیاه همون تیک بمیره و isEating مدل
    // فوری false بشه، View حداقل یک‌بار کامل سیکل رو نشون می‌ده.
    public static final double SMASH_SEQUENCE_MIN_DURATION = 3.1; // == EAT_DUR(1.3) + SMASH_DUR(1.8) در ZombieDrawer
    private double smashSequenceRemaining = 0;

    public GargantuarZombie(double health, double speed, int attackPower, int row, double startX,
                            double x, double y, double xSpeed, double ySpeed) {
        super(health, speed, attackPower, row, startX, x, y, xSpeed, ySpeed, ZombieType.GARGANTUAR);
        this.healthThresholdToThrowImp = health / 2.0;
        this.impTargetColumn = 3;

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

        if (this.health <= this.healthThresholdToThrowImp && this.canSpawn) {
            spawnImp = true;
            // 👈 لحظه‌ای که تصمیم به پرتاب ایمپ گرفته می‌شه، تایمر انیمیشن
            // fire+cannon_fire هم شروع می‌شه. این flag برخلاف spawnImp همون تیک
            // مصرف نمی‌شه (ZombieManager فقط spawnImp رو stop می‌کنه)، پس View
            // (GameScreen) می‌تونه چند تیک/فریم بعد هم هنوز ببینتش و انیمیشن رو
            // کامل پخش کنه.
            fireSequenceRemaining = FIRE_SEQUENCE_DURATION;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (fireSequenceRemaining > 0) {
            fireSequenceRemaining -= Constants.Game.TIME_COEFFICIENT;
            if (fireSequenceRemaining < 0) fireSequenceRemaining = 0;
        }
        if (smashSequenceRemaining > 0) {
            smashSequenceRemaining -= Constants.Game.TIME_COEFFICIENT;
            if (smashSequenceRemaining < 0) smashSequenceRemaining = 0;
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

    /**
     * آیا الان باید انیمیشن fire/cannon_fire پخش بشه (به‌جای walk/eat عادی)؟
     */
    public boolean isFiringImp() {
        return fireSequenceRemaining > 0;
    }

    /**
     * چند ثانیه از شروع توالی fire/cannon_fire گذشته (۰ = همین الان شروع شده).
     */
    public double getFireSequenceElapsed() {
        return FIRE_SEQUENCE_DURATION - fireSequenceRemaining;
    }

    /**
     * باید هر تیکی که این گارگانچوار واقعا به یه گیاه دمیج می‌زنه صدا زده
     * بشه (چه ضربه‌ی معمولی، چه ضربه‌ای که گیاه رو می‌کشه). سیکل انیمیشن
     * eat/smash_left رو برای یک دور کامل رفرش/تضمین می‌کنه.
     */
    public void notifyAttackedPlant() {
        smashSequenceRemaining = SMASH_SEQUENCE_MIN_DURATION;
    }

    /**
     * آیا هنوز باید سیکل انیمیشن eat/smash_left پخش بشه؟ (حتی اگه isEating
     * واقعیِ مدل - چون گیاه مرده - همین الان false شده باشه.)
     */
    public boolean isSmashSequenceActive() {
        return smashSequenceRemaining > 0;
    }
}
