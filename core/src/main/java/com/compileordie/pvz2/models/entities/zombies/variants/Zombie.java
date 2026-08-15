package com.compileordie.pvz2.models.entities.zombies.variants;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.GameEntity;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.StatusEffect;
import com.compileordie.pvz2.models.entities.zombies.ZombieBuilder;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.BucketHeadZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.KnightZombie;
import com.compileordie.pvz2.models.game.board.Lane;
import com.compileordie.pvz2.models.missions.quests.QuestEvent;
import com.compileordie.pvz2.models.missions.quests.QuestManager;
import com.compileordie.pvz2.models.user.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public abstract class Zombie extends GameEntity {
    public boolean isEating;
    public boolean succeeded;
    public double timerForHomeEating = 0;
    private double replacedSpeed = 0;
    public boolean isCombatingWithHypnotized;
    protected double health;
    // 💀 جون اولیه‌ی زامبی (بدون احتساب زره که جدا در StandardZombie.armorHealth
    // نگه‌داری می‌شه) - هیچ‌وقت بعد از سازنده تغییر نمی‌کنه؛ صرفا برای اینکه View
    // بتونه لحظه‌ی «جون به نصف رسید» رو تشخیص بده (health <= maxHealth/2) نگه‌داری می‌شه.
    protected final double maxHealth;
    protected double stableSpeed;
    protected int attackPower;
    protected int currentRow;
    protected List<StatusEffect> activeEffects;
    protected boolean skipThisTick;
    protected boolean stopZombieNow = false;
    protected boolean isHypnotized = false;
    protected ZombieType type;
    protected boolean hasMetalArmor = false; // Tracks if armor was removed by Magnet-shroom
    protected boolean isGlowing;
    public boolean killByExplosive = false;
    public boolean takedDamage = false;
    public boolean fromGarg = false;

    // --- برای انیمیشن پرتاب ایمپ توسط غول (Gargantuar) ---
    // مبدا پرتاب (نقطه‌ای که انیمیشن fly ازش شروع می‌شه)، به همون واحد متر که
    // getX()/getY() هستن. NaN یعنی این زامبی اصلا در حال پرتاب نیست/نبوده.
    private double throwOriginX = Double.NaN;
    private double throwOriginY = Double.NaN;
    private double flyInTotalDuration = 0;
    private double flyInRemaining = 0;

    public Zombie(double health,
                  double speed,
                  int base_damage,
                  int row,
                  double startX,
                  double x,
                  double y,
                  double xSpeed,
                  double ySpeed,
                  ZombieType type) {
        super(startX, y, xSpeed, ySpeed);
        this.health = health;
        this.maxHealth = health;
        this.stableSpeed = getXSpeed();
        this.attackPower = base_damage;
        this.currentRow = row;
        this.activeEffects = new ArrayList<>();
        this.skipThisTick = false;
        this.isEating = false;
        this.isCombatingWithHypnotized = false;
        this.type = type;
        this.succeeded = false;
//        this.replacedSpeed = getXSpeed();

        // Check if this zombie variant starts with metal armor
        if (type == ZombieType.BUCKETHEAD || type == ZombieType.KNIGHT) {
            this.hasMetalArmor = true;
        }
        this.isGlowing = new Random().nextInt(100) < 5;
    }

    @Override
    public void die() {
        super.die();
        AppModel.addAfterPrompt("Zombie " + type + " died!");
        if (isGlowing) {
            Player player = AppModel.player;
            player.plantFoodCount++;
            if (player.plantFoodCount > 3) player.plantFoodCount = 3;
            AppModel.addAfterPrompt("The glowing zombie dropped a plant food; you have "
                + player.plantFoodCount + " plant foods now.");
        }
        QuestManager.dispatch(QuestEvent.ZOMBIE_KILLED, 1, AppModel.currentChapter.toString());

        //----
        int tileCol = (int) Math.floor(getX() / Constants.Game.TILE_WIDTH);
        if (tileCol == 0) {
            Lane lane = AppModel.gameSession.gameBoard.lanes.get((int) Math.floor(getY() / Constants.Game.TILE_HEIGHT));
            if (lane.lawnMower == null || lane.lawnMower.isTriggered) {
                QuestManager.dispatch(QuestEvent.ZOMBIE_KILLED_NO_MOWER_FIRST_COL, 1, null);
            }
        }
        //----
    }

    // تیک ما در کلاس والد زامبی صرفا برای هندل کردن مرگ و افکت ها هست
    public void tick() {
        if (isDead() || this.health <= 0) {
            if (AppModel.gameSession.elapsedTimeFromFirstWave <= 30){
                QuestManager.dispatch(QuestEvent.ZOMBIE_KILLED_QUICKLY, 1, null);
            }
            die();
            handleDeath();
            return;
        }
//        if (takedDamage) takedDamage = false;
        if (skipThisTick) return;

        // ⏳ اگه در حال پخش انیمیشن پرتاب (fly-in) هستیم (مثلا ایمپی که تازه از
        // غول پرتاب شده)، شمارش معکوس می‌کنیم و در پایان خودکار آزادش می‌کنیم.
        // تا وقتی flyInRemaining > 0 هست، stopZombieNow=true نگه داشته می‌شه
        // (توسط startFlyInFreeze ست شده)، پس canMove()/move() این زامبی رو جابه‌جا
        // نمی‌کنن و دقیقا سرجای فرودش (target) می‌مونه.
        if (flyInRemaining > 0) {
            flyInRemaining -= Constants.Game.TIME_COEFFICIENT;
            if (flyInRemaining <= 0) {
                flyInRemaining = 0;
                stopZombieNow = false; // آزاد شدن؛ از این لحظه حرکت عادی زامبی شروع می‌شه
            }
        }

        // برای رسیدن به خانه
        if (getX()<=Constants.Game.EAT_HOME_X){
            isEating = true;
            timerForHomeEating += Constants.Game.TIME_COEFFICIENT;
            if (timerForHomeEating >= 4){
                succeeded = true;
            }
        }

        // بررسی اینکه وارد زمین شده یا ن
        if (!fromGarg) {
            if (getX() >= Constants.Game.TILE_WIDTH * 9 + Constants.Game.PADDING_X + 1) {
                if (replacedSpeed == 0) replacedSpeed = getXSpeed();
                setXSpeed(replacedSpeed * 3);
            } else {
                setXSpeed(replacedSpeed);
            }
        }


        // آپدیت و مدیریت افکت‌ها
        Iterator<StatusEffect> iterator = activeEffects.iterator();
        while (iterator.hasNext()) {
            StatusEffect effect = iterator.next();
            effect.updateZombieTick(this);
            if (effect.isExpired()) {
                iterator.remove();
            }
        }

        this.setXSpeed(isHypnotized ? -Math.abs(this.getXSpeed()) : Math.abs(this.getXSpeed()));

        // برای جابجایی - اعمال دمیح - اعمال توانایی سرویس ها هستند که پیش می برند

    }

    public void handleDeath() {
    }

    @Override
    public void move(int ticks) {
        if (isEating || isCombatingWithHypnotized || !canMove()) return;
        float dt = ticks * Constants.Game.TIME_COEFFICIENT;
        setX(getX() - getXSpeed() * dt);
        setY(getY() + getYSpeed() * dt);

        // در واقع ما سرعت زامبی رو مثبت می گیریم ولی ضریب منفی رو دستی بهش می دیم
    }

    public void startEating() {
        this.isEating = true;
    }

    public void stopEating() {
        this.isEating = false;
    }

    public boolean isEating() {
        return this.isEating;
    }

    public void addEffect(StatusEffect effect) {
        activeEffects.add(effect);
    }

    public void removeFrozen(){
        for (StatusEffect s: activeEffects){
            if (!s.isExpired() && s.getEffectType()==EffectType.FROZEN){
                s.isApplied = false;
            }
        }
    }

    public boolean hasEffect(EffectType effect){
        for (StatusEffect s: activeEffects){
            if (!s.isExpired() && s.getEffectType()==effect){
                return true;
            }
        }
        return false;
    }

    public void removeStatusEffect(EffectType type) {
        activeEffects.removeIf(effect -> {
            if (effect.getEffectType() == type) {
                effect.removeFromZombie(this);
                return true;
            }
            return false;
        });
    }

    public boolean canMove() {
        if (isDead() || isEating || stopZombieNow) return false;
        return true;
    }


    public boolean isDead() {
        return this.health <= 0;
    }

//    public boolean hasEffect(EffectType type) {
//        if (activeEffects == null || activeEffects.isEmpty()) return false;
//        for (StatusEffect effect : activeEffects) {
//            if (effect.getEffectType() == type && effect.isApplied()) return true;
//        }
//        return false;
//    }

    public void setSkip(boolean s) {
        this.skipThisTick = s;
    }

    public boolean getStopZombieNow() {
        return stopZombieNow;
    }

    public void setStopZombieNow(boolean s) {
        stopZombieNow = s;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double hp) {
        this.health = hp;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public List<StatusEffect> getActiveEffects() {
        return activeEffects;
    }

    public int getAttackPower() {
        return attackPower;
    }

    public void setAttackPower(int a) {
        this.attackPower = a;
    }

    public double getStableSpeed() {
        return stableSpeed;
    }

    /**
     * 🚨 فیکس باگ «سرعت enrage اعمال نمی‌شه»: صرفا setXSpeed() صدا زدن کافی
     * نیست، چون tick() بالاتر هر فریم (وقتی از ناحیه‌ی داش خارج شده) سرعت رو
     * دوباره از replacedSpeed (سرعت اولیه‌ی ذخیره‌شده در همون تیک اول) بازیابی
     * می‌کنه - یعنی هر تغییر دستی سرعت (مثل NewspaperZombie.enterEnrageMode)
     * همون فریم بعد بی‌صدا پاک می‌شد. این متد هم سرعت فعلی و هم مقداری که
     * tick() هر فریم بهش برمی‌گرده رو با هم آپدیت می‌کنه تا تغییر سرعت واقعا
     * دائمی بمونه.
     */
    protected void setPermanentXSpeed(double newSpeed) {
        this.replacedSpeed = newSpeed;
        setXSpeed(newSpeed);
    }

    public int getCurrentRow() {
        return currentRow;
    }

    public void setCurrentRow(int currentRow) {
        this.currentRow = currentRow;
    }

    // ==== بسیار خطرناک ولی موقت ====
    public void takeDamage(double amount, DamageType damageType){
//        this.health -= amount;
//        if (health<=0) health = 0;
        this.takeDamage(amount, damageType, null);
    }
    // ===============================

    public abstract void takeDamage(double amount, DamageType damageType, PlantType plantType);

    public ZombieType getType() {
        return type;
    }

    public void setType(ZombieType type) {
        this.type = type;
    }

    // --- برای انیمیشن پرتاب ایمپ توسط غول (Gargantuar) ---
    /**
     * این زامبی رو برای {@code seconds} ثانیه از نظر فیزیکی فریز می‌کنه (دقیقا
     * سرجای فعلی‌اش که همون مقصد/فرود نهایی‌ست می‌مونه) و مبدا پرتاب رو ثبت
     * می‌کنه تا View بتونه مسیر سهمی رو از مبدا تا همین نقطه رسم کنه.
     */
    public void startFlyInFreeze(double seconds, double originX, double originY) {
        this.flyInTotalDuration = seconds;
        this.flyInRemaining = seconds;
        this.throwOriginX = originX;
        this.throwOriginY = originY;
        this.stopZombieNow = true;
    }

    public boolean isFlyingIn() {
        return flyInRemaining > 0;
    }

    /** پیشرفت پرتاب: ۰ در لحظه‌ی شروع، ۱ درست وقتی که باید سرجای نهایی (target) باشه. */
    public float getFlyInProgress() {
        if (flyInTotalDuration <= 0) return 1f;
        float raw = 1f - (float) (flyInRemaining / flyInTotalDuration);
        return Math.max(0f, Math.min(1f, raw));
    }

    public double getThrowOriginX() {
        return throwOriginX;
    }

    public double getThrowOriginY() {
        return throwOriginY;
    }

    public boolean isHypnotized() {
        return isHypnotized;
    }

    public void setHypnotized(boolean h) {
        this.isHypnotized = h;
    }

    public boolean isCombatingWithHypnotized() {
        return isCombatingWithHypnotized;
    }

    public void setCombatingWithHypnotized(boolean c) {
        this.isCombatingWithHypnotized = c;
    }

    // --- Magnet-shroom Mechanics ---
    public boolean hasMetalArmor() {
        return this.hasMetalArmor;
    }

    public void removeMetalArmor() {
        if (this.hasMetalArmor) {
            this.hasMetalArmor = false;
            // Instantly strip armor HP bonus (e.g., reduce health to standard zombie baseline)
            this.health = Math.min(this.health, 200.0);
        }
    }

    public void mushroomAbsorption() {
        if (!hasMetalArmor) return;
        if (this.type==ZombieType.KNIGHT) {
            KnightZombie knight = (KnightZombie) this;
            knight.setArmorHealth(0);
            // 🛡️ فیکس: setArmorHealth(0) فقط armorHealth کلی رو صفر می‌کرد؛ اگه
            // helmetArmorHealth/shoulderArmorHealth جدا صفر نشن، View هنوز طبق اون
            // دو مقدار جدا فکر می‌کنه هلمت/شولدر سالمن و آرمور رو نمایش می‌ده.
            knight.helmetArmorHealth = 0;
            knight.shoulderArmorHealth = 0;
        }
        if (this.type==ZombieType.BUCKETHEAD) ((BucketHeadZombie)this).setArmorHealth(0);
        // اینجا باید گیاه به محض رویت زامبی در نزدیکی اش این متد را فراخوانی کند
    }
}
