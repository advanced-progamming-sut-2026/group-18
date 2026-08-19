package com.compileordie.pvz2.models.entities.plants;

public class UpgradeLevel {
    public final int hpBonus;
    public final int damageBonus;
    public final int costReduction;
    public final int extraSunYield;
    public final double actionIntervalReductionTicks;
    public final double rechargeReductionTicks;
    public final boolean doubleSunChance;
    public final double growTimeReductionTicks;
    public final double chillTimeBonusTicks;
    public boolean targetPriorityUp;
    public final int pierceBonus;
    public final double atkSpeedBonusPercentage;
    public final int poisonDmgTickBonus;
    public final double plantFoodChanceBonus;
    public final double rangeBonus;
    public final double lifespanBonusTicks;
    public final double butterChanceBonus;
    public final int aoeDamageBonus;
    public final int warmthRadiusBonus;
    public final double armTimeReductionTicks;
    public int extraCrushes;
    public final int extraBounces;
    public final int extraTargets;
    public final double freezeTimeBonusTicks;
    public final int maxSizeBonus;
    public final boolean explodesOnDeath;
    public final boolean zombieHpBuff;
    public final boolean zombieDmgBuff;
    public final boolean plantFoodOnSpawn;
    public final boolean meltArea3x3;
    public final double mintDurationBonusTicks;
    public final boolean resetFamilyCooldowns;

    public UpgradeLevel(int hpBonus,
                        int damageBonus,
                        int costReduction,
                        double actionIntervalReductionTicks,
                        double rechargeReductionTicks,
                        boolean doubleSunChance,
                        double growTimeReductionTicks,
                        int extraSunYield,
                        double chillTimeBonusTicks,
                        boolean targetPriorityUp,
                        int pierceBonus,
                        double atkSpeedBonusPercentage,
                        int poisonDmgTickBonus,
                        double plantFoodChanceBonus,
                        double rangeBonus,
                        double lifespanBonusTicks,
                        double butterChanceBonus,
                        int aoeDamageBonus,
                        int warmthRadiusBonus,
                        double armTimeReductionTicks,
                        int extraCrushes,
                        int extraBounces,
                        int extraTargets,
                        double freezeTimeBonusTicks,
                        int maxSizeBonus,
                        boolean explodesOnDeath,
                        boolean zombieHpBuff,
                        boolean zombieDmgBuff,
                        boolean plantFoodOnSpawn,
                        boolean meltArea3x3,
                        double mintDurationBonusTicks,
                        boolean resetFamilyCooldowns) {
        this.hpBonus = hpBonus;
        this.damageBonus = damageBonus;
        this.costReduction = costReduction;
        this.actionIntervalReductionTicks = actionIntervalReductionTicks;
        this.rechargeReductionTicks = rechargeReductionTicks;
        this.doubleSunChance = doubleSunChance;
        this.growTimeReductionTicks = growTimeReductionTicks;
        this.extraSunYield = extraSunYield;
        this.chillTimeBonusTicks = chillTimeBonusTicks;
        this.targetPriorityUp = targetPriorityUp;
        this.pierceBonus = pierceBonus;
        this.atkSpeedBonusPercentage = atkSpeedBonusPercentage;
        this.poisonDmgTickBonus = poisonDmgTickBonus;
        this.plantFoodChanceBonus = plantFoodChanceBonus;
        this.rangeBonus = rangeBonus;
        this.lifespanBonusTicks = lifespanBonusTicks;
        this.butterChanceBonus = butterChanceBonus;
        this.aoeDamageBonus = aoeDamageBonus;
        this.warmthRadiusBonus = warmthRadiusBonus;
        this.armTimeReductionTicks = armTimeReductionTicks;
        this.extraCrushes = extraCrushes;
        this.extraBounces = extraBounces;
        this.extraTargets = extraTargets;
        this.freezeTimeBonusTicks = freezeTimeBonusTicks;
        this.maxSizeBonus = maxSizeBonus;
        this.explodesOnDeath = explodesOnDeath;
        this.zombieHpBuff = zombieHpBuff;
        this.zombieDmgBuff = zombieDmgBuff;
        this.plantFoodOnSpawn = plantFoodOnSpawn;
        this.meltArea3x3 = meltArea3x3;
        this.mintDurationBonusTicks = mintDurationBonusTicks;
        this.resetFamilyCooldowns = resetFamilyCooldowns;
    }
}
