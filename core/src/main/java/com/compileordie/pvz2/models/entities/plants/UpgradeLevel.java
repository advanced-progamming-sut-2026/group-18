package com.compileordie.pvz2.models.entities.plants;

public class UpgradeLevel {
    public final int hpBonus;
    public final int damageBonus;
    public final int costReduction;
    public final int extraSunYield;
    // For upgrades like "Prod. Time -2s" or "Arm Time -3s"
    public final double actionIntervalReductionTicks;
    // For upgrades like "Cooldown -5s"
    public final double rechargeReductionTicks;
    public final boolean doubleSunChance;
    // NEW: For upgrades like "Grow Time -5s" (Sun-shroom)
    public final double growTimeReductionTicks;
    public final double chillTimeBonusTicks;
    public boolean targetPriorityUp;
    public final int pierceBonus;
    public final double atkSpeedBonusPercentage;
    public final int poisonDmgTickBonus;
    public final double plantFoodChanceBonus;
    public final double rangeBonus;
    public final double lifespanBonusTicks;
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
                        double lifespanBonusTicks) {
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
    }
}
