package com.compileordie.pvz2.models.entities.plants;
import com.compileordie.pvz2.models.entities.plants.enums.AttackStrategyType;
import com.compileordie.pvz2.models.entities.plants.enums.PlantCategory;
import com.compileordie.pvz2.models.entities.plants.enums.PlantFoodEffectType;
import com.compileordie.pvz2.models.entities.plants.enums.PlantTag;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import java.util.List;
import java.util.Map;
public class PlantTemplate {
    private String name;
    private PlantCategory category;
    private List<PlantTag> tags;
    private int cost;
    private int baseHp;
    private int baseDamage;
    private int actionIntervalTicks;
    private int rechargeTicks;
    private Map<Integer, UpgradeLevel> upgradeMap;
    private AttackStrategyType attackStrategyType;
    private Class<? extends Projectile> projectileType;
    private List<Integer> laneOffsets;
    private List<double[]> shootVectors;
    private double rangeTiles;
    private boolean isAoE;
    private boolean isInstantKill;
    private PlantFoodEffectType foodEffectType;
    private int foodEffectValue;
    public PlantTemplate() {}
    ////////////////////////////setters & getters & helpers\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public PlantCategory getCategory() { return category; }
    public void setCategory(PlantCategory category) { this.category = category; }
    public List<PlantTag> getTags() { return tags; }
    public void setTags(List<PlantTag> tags) { this.tags = tags; }
    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }
    public int getBaseHp() { return baseHp; }
    public void setBaseHp(int baseHp) { this.baseHp = baseHp; }
    public int getBaseDamage() { return baseDamage; }
    public void setBaseDamage(int baseDamage) { this.baseDamage = baseDamage; }
    public int getActionIntervalTicks() { return actionIntervalTicks; }
    public void setActionIntervalTicks(int actionIntervalTicks) { this.actionIntervalTicks = actionIntervalTicks; }
    public int getRechargeTicks() { return rechargeTicks; }
    public void setRechargeTicks(int rechargeTicks) { this.rechargeTicks = rechargeTicks; }
    public Map<Integer, UpgradeLevel> getUpgradeMap() { return upgradeMap; }
    public void setUpgradeMap(Map<Integer, UpgradeLevel> upgradeMap) { this.upgradeMap = upgradeMap; }
    public AttackStrategyType getAttackStrategyType() { return attackStrategyType; }
    public void setAttackStrategyType(AttackStrategyType attackStrategyType) { this.attackStrategyType = attackStrategyType; }
    public Class<? extends Projectile> getProjectileType() { return projectileType; }
    public void setProjectileType(Class<? extends Projectile> projectileType) { this.projectileType = projectileType; }
    public List<Integer> getLaneOffsets() { return laneOffsets; }
    public void setLaneOffsets(List<Integer> laneOffsets) { this.laneOffsets = laneOffsets; }
    public List<double[]> getShootVectors() { return shootVectors; }
    public void setShootVectors(List<double[]> shootVectors) { this.shootVectors = shootVectors; }
    public double getRangeTiles() { return rangeTiles; }
    public void setRangeTiles(double rangeTiles) { this.rangeTiles = rangeTiles; }
    public boolean isAoE() { return isAoE; }
    public void setAoE(boolean aoE) { isAoE = aoE; }
    public boolean isInstantKill() { return isInstantKill; }
    public void setInstantKill(boolean instantKill) { this.isInstantKill = instantKill; }
    public PlantFoodEffectType getFoodEffectType() { return foodEffectType; }
    public void setFoodEffectType(PlantFoodEffectType foodEffectType) { this.foodEffectType = foodEffectType; }
    public int getFoodEffectValue() { return foodEffectValue; }
    public void setFoodEffectValue(int foodEffectValue) { this.foodEffectValue = foodEffectValue; }
}
