package com.compileordie.pvz2.models.entities.zombies.variants;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.GameEntity;
import com.compileordie.pvz2.models.entities.zombies.StatusEffect;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class Zombie extends GameEntity {
    protected int health;
    protected int maxHealth;
    protected double movementSpeed;
    protected double currentSpeed;
    protected int attackPower;
    protected int currentRow;
    protected List<StatusEffect> activeEffects;
    protected boolean skipThisTick;
    protected boolean isEating;
    protected ZombieType type;

    public Zombie(int health, double speed, int base_damage, int row, double startX, double x, double y, double xSpeed, double ySpeed, ZombieType type) {
        // فیکس دوگانگی: مقدار startX مستقیماً به عنوان موقعیت X اولیه به لایه GameEntity فرستاده می‌شود
        super(startX, y, xSpeed, ySpeed);
        this.maxHealth = health;
        this.health = this.maxHealth;
        this.movementSpeed = speed;
        this.currentSpeed = this.movementSpeed;
        this.attackPower = base_damage;
        this.currentRow = row;
        this.activeEffects = new ArrayList<>();
        this.skipThisTick = false;
        this.isEating = false;
        this.type = type;
    }

    public void tick() {
        if (isDead()) {
            handleDeath();
            return;
        }
        skipThisTick = false;

        // آپدیت و مدیریت افکت‌ها
        Iterator<StatusEffect> iterator = activeEffects.iterator();
        while (iterator.hasNext()) {
            StatusEffect effect = iterator.next();
            effect.updateZombieTick(this);
            if (effect.isExpired()) {
                effect.removeFromZombie(this); // حذف امن و بدون تکرار
                iterator.remove();
            }
        }

        if (skipThisTick) return;

        recalculateSpeed();

        // نکته ساختاری: اگر MovementService جابجایی را مدیریت می‌کند،
        // این شرط حرکت می‌تواند از tick حذف شده و هندلینگ آن به سرویس منتقل شود.
//        if (canMove()) {
//            move(1);
//        }

    }

    public void handleDeath() {}

    @Override
    public void move(int ticks) {
        float dt = ticks * Constants.Game.TIME_COEFFICIENT;
        setXSpeed(this.currentSpeed);
        setX(getX() - getXSpeed() * dt);
    }

    public void startEating() { this.isEating = true; }
    public void stopEating() { this.isEating = false; }
    public boolean isEating() { return this.isEating; }

    public void addEffect(StatusEffect effect) {
        activeEffects.add(effect);
        effect.applyToZombie(this);
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
        if (isDead() || isEating) return false;
        for (StatusEffect effect : activeEffects) {
            if (effect.getEffectType() == EffectType.FREEZE) return false;
        }
        return true;
    }

    public void recalculateSpeed() {
        double speedModifier = 1.0;
        for (StatusEffect effect : activeEffects) {
            if (effect.getEffectType() == EffectType.CHILLED) {
                speedModifier *= 0.5;
            }
        }
        this.currentSpeed = this.movementSpeed * speedModifier;
    }

    public boolean isDead() { return this.health <= 0; }

    public boolean hasEffect(EffectType type) {
        if (activeEffects == null || activeEffects.isEmpty()) return false;
        for (StatusEffect effect : activeEffects) {
            if (effect.getEffectType() == type && effect.isApplied()) return true;
        }
        return false;
    }

    public void setSkip(boolean s) { this.skipThisTick = s; }

    // متدهای پل‌زن (Bridge Methods) برای اینکه کدهای قدیمی فرزندان که از این متدها استفاده می‌کردند آسیب نبینند
    public double getPositionX() { return getX(); }
    public void setPositionX(double positionX) { setX(positionX); }

    public int getHealth() { return health; }
    public int getAttackPower() { return attackPower; }
    public double getCurrentSpeed() { return currentSpeed; }
    public int getCurrentRow() { return currentRow; }
    public void setCurrentRow(int currentRow) { this.currentRow = currentRow; }

    public abstract void takeDamage(int amount, DamageType damageType);

    public ZombieType getType() {
        return type;
    }

    public void setType(ZombieType type) {
        this.type = type;
    }
}
