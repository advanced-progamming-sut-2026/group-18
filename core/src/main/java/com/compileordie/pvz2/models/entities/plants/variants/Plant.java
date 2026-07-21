package com.compileordie.pvz2.models.entities.plants.variants;

import com.compileordie.pvz2.models.entities.GameEntity;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.entities.plants.enums.PlantCategory;
import com.compileordie.pvz2.models.entities.plants.enums.PlantTag;
import com.compileordie.pvz2.models.entities.plants.strategies.attack.AttackStrategy;
import com.compileordie.pvz2.models.entities.plants.strategies.food.PlantFoodEffectStrategy;

import java.util.ArrayList;
import java.util.List;

public abstract class Plant extends GameEntity {
    private String name;
    private List<PlantTag> tags = new ArrayList<>();
    private int solarCost;
    private int baseHp;
    private int maxHp;
    private int baseDamage;
    private double actionInterval;
    private double rechargeInterval;
    private int level;
    private int seedPackets;
    private int cooldownTicksPassed; // if it reaches rechargeInterval * 10 => the card is available again
    private int actionTicksAccumulator; // if it reaches actionInterval * 10 => the plant takes action again
    private boolean boosted;
    private AttackStrategy attackStrategy;
    private PlantFoodEffectStrategy plantFoodEffect;


    //=======================
    // new!
    private int isFreezedByHunter = 0;
    private boolean isFreezedByZombieHunter = false;
    private boolean isFreezedByOcto = false;
    // new!
    //=======================


    protected Plant(PlantTemplate plantTemplate,
                    double x,
                    double y,
                    AttackStrategy attackStrategy,
                    PlantFoodEffectStrategy plantFoodEffect
    ) {
        // Wire up the coordinates to the team's GameEntity engine (Speed is 0 for rooted plants)
        super(x, y, 0, 0);

        this.name = plantTemplate.getName();
        this.tags = plantTemplate.getTags();
        this.solarCost = plantTemplate.getSolarCost();
        this.baseHp = plantTemplate.getBaseHP();
        this.maxHp = plantTemplate.getBaseHP();
        this.baseDamage = plantTemplate.getBaseDamage();
        this.actionInterval = plantTemplate.getActionInterval();
        this.rechargeInterval = plantTemplate.getRecharge();
        this.level = 1;
        this.seedPackets = 0;
        this.cooldownTicksPassed = 0;
        this.actionTicksAccumulator = 0;
        this.boosted = false;
        this.attackStrategy = attackStrategy;
        this.plantFoodEffect = plantFoodEffect;
    }

    // Abstract methods to be implemented by specific plant families
    public abstract void tickCore(GameBoard board, int tickDelta);
    public abstract void applyLevelUpgrade(int newLevel);
    public abstract String getAbilityDescription();
    public abstract PlantCategory getCategory();

    // --- CONCRETE LIFECYCLE METHODS ---

    public void tick(GameBoard board, int tickDelta) {
        // 1. Advance the card cooldown (recharge)
        if (cooldownTicksPassed < rechargeInterval * 10) {
            cooldownTicksPassed += tickDelta;
        }

        // 2. Advance the action timer (e.g., shooting rate or sun production rate)
        actionTicksAccumulator += tickDelta;

        // 3. Check if the plant is ready to act
        if (actionTicksAccumulator >= actionInterval * 10) {
            if (attackStrategy != null) {
                attackStrategy.attack(this, board, tickDelta);
            }
            actionTicksAccumulator = 0; // Reset timer after acting
        }

        // 4. Delegate to specific child classes for any unique behavior
        tickCore(board, tickDelta);
    }

    public void takeDamage(int damage) {
        this.baseHp -= damage;
        if (this.baseHp <= 0) {
            this.baseHp = 0;
            this.die(); // Sync with GameEntity lifecycle
        }
    }

    public boolean isDead() {
        return this.baseHp <= 0 || !this.isAlive();
    }

    // --- GETTERS & SETTERS ---
    public int getBaseDamage() { return baseDamage; }
    public void setBaseDamage(int baseDamage) { this.baseDamage = baseDamage; }

    public int getBaseHp() { return baseHp; }
    public void setBaseHp(int baseHp) { this.baseHp = baseHp; }

    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }

    public String getName() { return name; }





    //=======================
    // new!
    public int getFreezedByHunter() { return  isFreezedByHunter; }
    public void increaseFreezedByHunter() { isFreezedByHunter += 1;}
    public boolean shouldBeFreezedByHunter() {
        if (isFreezedByHunter == 3){
            isFreezedByHunter = 0;
            isFreezedByZombieHunter = true;
            return true;
        }
        return false;
    }
    public void setIsFreezedByHunterCounter(int a) { isFreezedByHunter = a; }
    public boolean isFreezedByHunter() { return isFreezedByZombieHunter; }
    public boolean isFreezedByOcto() { return isFreezedByOcto; }
    public void setIsFreezedByHunter(boolean s) { isFreezedByZombieHunter = s; }
    public void setFreezedByOcto(boolean f) { isFreezedByOcto = f; }
    public boolean shouldBeFreezedByOcto() { return isFreezedByOcto; }
    // new!
    //=======================


}
