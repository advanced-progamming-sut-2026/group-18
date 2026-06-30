package com.compileordie.pvz2.models.plants.base;


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
}

