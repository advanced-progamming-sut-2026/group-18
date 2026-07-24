package com.compileordie.pvz2.models.entities.plants;

public class UpgradeLevel {
    public final int hpBonus;
    public final int damageBonus;
    public final int costReduction;
    public final double cooldownReductionTicks;

    // Custom flags for specific upgrades (like Electric Blueberry's Lvl 3)
    public final boolean targetPriorityUp;

    public UpgradeLevel(int hpBonus,
                        int damageBonus,
                        int costReduction,
                        double cooldownReductionTicks,
                        boolean targetPriorityUp) {
        this.hpBonus = hpBonus;
        this.damageBonus = damageBonus;
        this.costReduction = costReduction;
        this.cooldownReductionTicks = cooldownReductionTicks;
        this.targetPriorityUp = targetPriorityUp;
    }
}
