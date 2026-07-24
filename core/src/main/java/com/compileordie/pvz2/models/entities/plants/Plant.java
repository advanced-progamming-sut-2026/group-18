package com.compileordie.pvz2.models.entities.plants;

import com.compileordie.pvz2.models.entities.GameEntity;
import com.compileordie.pvz2.models.entities.plants.enums.PlantCategory;
import com.compileordie.pvz2.models.entities.plants.enums.PlantTag;
import com.compileordie.pvz2.models.entities.plants.strategies.attack.AttackStrategy;
import com.compileordie.pvz2.models.entities.plants.strategies.food.PlantFoodEffectStrategy;
import com.compileordie.pvz2.models.game.board.GameBoard;

import java.util.List;
import java.util.Map;

public class Plant extends GameEntity {
    //=======================
    // new!
    private int isFreezedByHunter = 0;
    private boolean isFreezedByZombieHunter = false;
    private boolean isFreezedByOcto = false;
    // new!
    //=======================
    private boolean isSpecial;
    private String name;
    private PlantCategory category;
    private List<PlantTag> tags;
    private int baseHp;
    private int currentHp;
    private int baseDamage;
    private int cost;
    private double actionIntervalTicks;
    private double currentActionTimer = 0;
    private int level = 1;

    // Special flags for specific AI behaviors (driven by base stats or upgrades)
    private boolean targetsHighestHp = false;

    private AttackStrategy attackStrategy;
//    private PlantFoodEffectStrategy foodStrategy;
    private Map<Integer, UpgradeLevel> upgradeMap; // Holds Lvl 2, 3, 4 rules

    public Plant(String name, PlantCategory category, List<PlantTag> tags,
                 double x, double y, int hp, int damage, int cost, double actionIntervalTicks,
                 AttackStrategy attackStrategy, PlantFoodEffectStrategy foodStrategy,
                 Map<Integer, UpgradeLevel> upgradeMap) {
        super(x, y, 0, 0); // Plants do not move
        this.name = name;
        this.category = category;
        this.tags = tags;
        this.baseHp = hp;
        this.currentHp = hp;
        this.baseDamage = damage;
        this.cost = cost;
        this.actionIntervalTicks = actionIntervalTicks;
        this.attackStrategy = attackStrategy;
        this.foodStrategy = foodStrategy;
        this.upgradeMap = upgradeMap;
    }

    public void tick(GameBoard board, int tickDelta) {
        currentActionTimer += tickDelta;

        // When the action timer fills up, execute the injected strategy!
        if (currentActionTimer >= actionIntervalTicks) {
            if (attackStrategy != null) {
                attackStrategy.attack(this, board, tickDelta);
            }
            currentActionTimer = 0;
        }
    }

    public void applyLevelUpgrade(int newLevel) {
        this.level = newLevel;
        if (upgradeMap != null && upgradeMap.containsKey(newLevel)) {
            UpgradeLevel stats = upgradeMap.get(newLevel);

            this.baseHp += stats.hpBonus;
            this.currentHp += stats.hpBonus; // Heal by the bonus amount
            this.baseDamage += stats.damageBonus;
            this.cost = Math.max(0, this.cost - stats.costReduction);
            this.actionIntervalTicks = Math.max(1.0, this.actionIntervalTicks - stats.cooldownReductionTicks);

            if (stats.targetPriorityUp) {
                this.targetsHighestHp = true;
            }
        }
    }

    //=======================
    // new!
    public int getFreezedByHunter() {
        return isFreezedByHunter;
    }

    public void increaseFreezedByHunter() {
        isFreezedByHunter += 1;
    }

    public boolean shouldBeFreezedByHunter() {
        if (isFreezedByHunter == 3) {
            isFreezedByHunter = 0;
            isFreezedByZombieHunter = true;
            return true;
        }
        return false;
    }

    public void setIsFreezedByHunterCounter(int a) {
        isFreezedByHunter = a;
    }

    public boolean isFreezedByHunter() {
        return isFreezedByZombieHunter;
    }

    public boolean isFreezedByOcto() {
        return isFreezedByOcto;
    }

    public void setFreezedByOcto(boolean f) {
        isFreezedByOcto = f;
    }

    public void setIsFreezedByHunter(boolean s) {
        isFreezedByZombieHunter = s;
    }

    public boolean shouldBeFreezedByOcto() {
        return isFreezedByOcto;
    }
    // new!
    //=======================

    public void die() {
        // Handled by your GameBoard/Tile cleanup when HP hits 0
    }

    public boolean isSpecial() {
        return isSpecial;
    }

    public void setSpecial() {
        isSpecial = true;
    }

    // --- Getters and Setters ---
    public int getBaseHp() {
        return baseHp;
    }

    public void setBaseHp(int baseHp) {
        this.baseHp = baseHp;
    }

    public void setActionIntervalTicks(double actionIntervalTicks) {
        this.actionIntervalTicks = actionIntervalTicks;
    }

    public String getName() {
        return name;
    }

    public PlantCategory getCategory() {
        return category;
    }

    public List<PlantTag> getTags() {
        return tags;
    }

    public boolean hasTag(PlantTag tag) {
        return tags != null && tags.contains(tag);
    }

    public int getBaseDamage() {
        return baseDamage;
    }

    public void setBaseDamage(int baseDamage) {
        this.baseDamage = baseDamage;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public void setCurrentHp(int hp) {
        this.currentHp = hp;
    }

    public void takeDamage(int amount) {
        this.currentHp -= amount;
    }

    public boolean isDead() {
        return this.currentHp <= 0;
    }

    public boolean targetsHighestHp() {
        return targetsHighestHp;
    }

    public int getCost() {
        return cost;
    }

    public int getLevel() {
        return level;
    }
}
