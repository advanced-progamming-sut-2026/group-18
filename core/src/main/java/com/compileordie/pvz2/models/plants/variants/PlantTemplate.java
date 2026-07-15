package com.compileordie.pvz2.models.plants.variants;

import com.compileordie.pvz2.models.plants.enums.AttackStrategyType;
import com.compileordie.pvz2.models.plants.enums.PlantCategory;
import com.compileordie.pvz2.models.plants.enums.PlantFoodEffectType;
import com.compileordie.pvz2.models.plants.enums.PlantTag;

import java.util.List;

public class PlantTemplate {
    private int id;
    private String name;
    private PlantCategory category;
    private List<PlantTag> tags;
    private int solarCost;
    private int baseHP;
    private int baseDamage;
    private String baseAbility;
    private String plantFoodEffect;
    private String upgradeLevel2;
    private String upgradeLevel3;
    private String upgradeLevel4;
    private double actionInterval;
    private double recharge;
    private AttackStrategyType attackStrategyType;
    private PlantFoodEffectType plantFoodEffectType;

    public PlantTemplate(
        int id,
        String name,
        PlantCategory category,
        List<PlantTag> tags,
        int solarCost,
        int baseHp,
        int baseDamage,
        String baseAbility,
        String plantFoodEffect,
        String upgradeLevel2,
        String upgradeLevel3,
        String upgradeLevel4,
        double actionInterval,
        double recharge,
        AttackStrategyType attackStrategyType,
        PlantFoodEffectType plantFoodEffectType
    ) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.tags = tags;
        this.solarCost = solarCost;
        this.baseHP = baseHp;
        this.baseDamage = baseDamage;
        this.baseAbility = baseAbility;
        this.plantFoodEffect = plantFoodEffect;
        this.upgradeLevel2 = upgradeLevel2;
        this.upgradeLevel3 = upgradeLevel3;
        this.upgradeLevel4 = upgradeLevel4;
        this.actionInterval = actionInterval;
        this.recharge = recharge;
        this.attackStrategyType = attackStrategyType;
        this.plantFoodEffectType = plantFoodEffectType;
    }
    public int getId() {
        return id;
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
    public int getSolarCost() {
        return solarCost;
    }
    public int getBaseHP() {
        return baseHP;
    }
    public int getBaseDamage() {
        return baseDamage;
    }
    public String getBaseAbility() {
        return baseAbility;
    }
    public String getPlantFoodEffect() {
        return plantFoodEffect;
    }
    public String getUpgradeLevel2() {
        return upgradeLevel2;
    }
    public String getUpgradeLevel3() {
        return upgradeLevel3;
    }
    public String getUpgradeLevel4() {
        return upgradeLevel4;
    }
    public double getActionInterval() {
        return actionInterval;
    }
    public double getRecharge() {
        return recharge;
    }
    public AttackStrategyType getAttackStrategyType() {
        return attackStrategyType;
    }
    public PlantFoodEffectType getPlantFoodEffectType() {
        return plantFoodEffectType;
    }
}
