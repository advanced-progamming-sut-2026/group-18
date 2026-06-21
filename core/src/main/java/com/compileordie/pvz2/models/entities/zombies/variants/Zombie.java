package com.compileordie.pvz2.models.entities.zombies.variants;

import com.compileordie.pvz2.models.entities.GameEntity;
import com.compileordie.pvz2.models.entities.zombies.StatusEffect;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class Zombie {
    protected int health;
    protected int maxHealth;
    protected double movementSpeed;
    protected double currentSpeed;
    protected int attackPower;
    protected int currentRow;
    protected double positionX;
    protected List<StatusEffect> activeEffects;
    protected boolean skipThisTick;

    // فیلد وضعیت خوردن گیاه برای مدیریت توقف حرکت
    protected boolean isEating;

    public Zombie(int health, double speed, int base_damage, int row, double startX) {
        this.maxHealth = health;
        this.health = this.maxHealth;
        this.movementSpeed = speed;
        this.currentSpeed = this.movementSpeed;
        this.attackPower = base_damage;
        this.currentRow = row;
        this.positionX = startX;
        this.activeEffects = new ArrayList<>();
        this.skipThisTick = false;
        this.isEating = false;
    }

    // آپدیت وضعیت زامبی و افکت‌های آن در هر فریم
    public void tick() {
        // بررسی زنده بودن یا نبودن
        if (isDead()) {
            handleDeath();
            return;
        }
        skipThisTick = false;

        // آپدیت افکت‌های حال حاضر
        Iterator<StatusEffect> iterator = activeEffects.iterator();
        while (iterator.hasNext()) {
            StatusEffect effect = iterator.next();
            effect.updateZombieTick(this);
            if (effect.isExpired()) {
                effect.removeFromZombie(this);
                iterator.remove(); // حذف امن
            }
        }
        if (skipThisTick) return;

        // باز محاسبه سرعت زامبی
        recalculateSpeed();

        // جابجایی
        if (canMove()) {
            move();
        }
    }

    // اتفاقاتی که پس از مرگ برای زامبی می‌افتد
    public void handleDeath() {}

    public void move() {
        this.positionX -= currentSpeed; // حرکت به سمت چپ (مزرعه گیاهان)
    }

    // متدهای مربوط به وضعیت خوردن گیاه توسط زامبی
    public void startEating() {
        this.isEating = true;
    }

    public void stopEating() {
        this.isEating = false;
    }

    public boolean isEating() {
        return this.isEating;
    }

    // افکت به زامبی می‌رسد
    public void addEffect(StatusEffect effect) {
        activeEffects.add(effect);
        effect.applyToZombie(this);
    }

    // حذف کردن افکت از روی زامبی
    public void removeStatusEffect(EffectType type) {
        activeEffects.removeIf(effect -> {
            if (effect.getEffectType() == type) {
                effect.removeFromZombie(this); // حذف کردن زامبی از روی افکت
                return true;
            }
            return false;
        });
    }

    public boolean canMove() {
        // اگر زامبی مرده باشد یا در حال خوردن گیاه باشد، نمی‌تواند حرکت کند
        if (isDead() || isEating) return false;

        for (StatusEffect effect : activeEffects) {
            if (effect.getEffectType() == EffectType.FREEZE) return false;
        }
        return true;
    }

    public void recalculateSpeed() {
        double speedModifier = 1.0;

        // بررسی افکت سرمازدگی
        for (StatusEffect effect : activeEffects) {
            if (effect.getEffectType() == EffectType.CHILLED) {
                speedModifier *= 0.5; // کاهش ۵۰ درصدی سرعت در صورت سرمای گیاه زمستانی
            }
        }

        this.currentSpeed = this.movementSpeed * speedModifier;
    }

    public boolean isDead() {
        return this.health <= 0;
    }

    public boolean hasEffect(EffectType type) {
        if (activeEffects == null || activeEffects.isEmpty()) {
            return false;
        }
        for (StatusEffect effect : activeEffects) {
            if (effect.getEffectType() == type && effect.isApplied()) {
                return true;
            }
        }
        return false;
    }

    public void setSkip(boolean s) {
        this.skipThisTick = s;
    }

    // Getters and Setters
    public int getHealth() { return health; }
    public int getAttackPower() { return attackPower; }
    public double getCurrentSpeed() { return currentSpeed; }
    public double getPositionX() { return positionX; }
    public void setPositionX(double positionX) { this.positionX = positionX; }
    public int getCurrentRow() { return currentRow; }
    public void setCurrentRow(int currentRow) { this.currentRow = currentRow; }

    // متد انتزاعی اعمال دمیج با دیکته اصلاح‌شده
    public abstract void takeDamage(int amount, DamageType damageType);
}
