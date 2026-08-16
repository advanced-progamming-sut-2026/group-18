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
                        int pierceBonus) {
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
    }
}
