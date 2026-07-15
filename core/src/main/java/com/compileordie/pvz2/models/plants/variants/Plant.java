package com.compileordie.pvz2.models.plants.variants;


import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.plants.enums.PlantCategory;
import com.compileordie.pvz2.models.plants.enums.PlantTag;
import com.compileordie.pvz2.models.plants.strategies.attack.AttackStrategy;
import com.compileordie.pvz2.models.plants.strategies.food.PlantFoodEffectStrategy;

import java.util.ArrayList;
import java.util.List;

public abstract class Plant {
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
    private int positionX;
    private int positionY;
    private boolean boosted;
    private AttackStrategy attackStrategy;
    private PlantFoodEffectStrategy plantFoodEffect;

    protected Plant(PlantTemplate plantTemplate,
                      int x,
                      int y,
                      AttackStrategy attackStrategy,
                      PlantFoodEffectStrategy plantFoodEffect
    ) {
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
        this.positionX = x;
        this.positionY = y;
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
        if (this.baseHp < 0) {
            this.baseHp = 0;
        }
    }

    public boolean isDead() {
        return this.baseHp <= 0;
    }

    // --- GETTERS REQUIRED FOR STRATEGIES ---
    public int getPositionX() { return positionX; }
    public int getPositionY() { return positionY; }
    public int getBaseDamage() { return baseDamage; }
}

