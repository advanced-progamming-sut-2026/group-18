package com.compileordie.pvz2.models.entities.zombies.variants;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.GameEntity;
import com.compileordie.pvz2.models.entities.zombies.StatusEffect;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
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
    public boolean isCombatingWithHypnotized;
    protected double health;
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
        this.stableSpeed = getXSpeed();
        this.attackPower = base_damage;
        this.currentRow = row;
        this.activeEffects = new ArrayList<>();
        this.skipThisTick = false;
        this.isEating = false;
        this.isCombatingWithHypnotized = false;
        this.type = type;

        // Check if this zombie variant starts with metal armor
        if (type != null) {
            String name = type.name().toUpperCase();
            if (name.contains("BUCKET") || name.contains("FOOTBALL") || name.contains("KNIGHT")
                || name.contains("MACHINERY")) {
                this.hasMetalArmor = true;
            }
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
        Lane lane = AppModel.gameSession.gameBoard.lanes.get((int) Math.floor(getY() / Constants.Game.TILE_HEIGHT));
        if (lane.lawnMower == null || lane.lawnMower.isTriggered) {
            QuestManager.dispatch(QuestEvent.ZOMBIE_KILLED_NO_MOWER_FIRST_COL, 1, null);
        }
    }

    // تیک ما در کلاس والد زامبی صرفا برای هندل کردن مرگ و افکت ها هست
    public void tick() {
        if (isDead() || this.health <= 0) {
            die();
            handleDeath();
            return;
        }
        if (skipThisTick) return;

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
        if (isEating || isCombatingWithHypnotized) return;
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

    public boolean hasEffect(EffectType type) {
        if (activeEffects == null || activeEffects.isEmpty()) return false;
        for (StatusEffect effect : activeEffects) {
            if (effect.getEffectType() == type && effect.isApplied()) return true;
        }
        return false;
    }

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

    public int getCurrentRow() {
        return currentRow;
    }

    public void setCurrentRow(int currentRow) {
        this.currentRow = currentRow;
    }

    public abstract void takeDamage(double amount, DamageType damageType);

    public ZombieType getType() {
        return type;
    }

    public void setType(ZombieType type) {
        this.type = type;
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
}
