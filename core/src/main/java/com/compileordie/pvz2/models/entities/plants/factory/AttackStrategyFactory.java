package com.compileordie.pvz2.models.entities.plants.factory;

import com.compileordie.pvz2.models.entities.plants.enums.AttackStrategyType;
import com.compileordie.pvz2.models.entities.plants.strategies.attack.*;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;

import java.util.List;

public class AttackStrategyFactory {

    /**
     * Builds the exact attack strategy needed without knowing what the plant is.
     *
     * @param type The enum type of the strategy (from your CSV)
     * @param projectileClass The class of the projectile to spawn (e.g., PoisonProjectile.class)
     * @param laneOffsets List of lanes to shoot in (0 = current, 1 = up, -1 = down). Solves Threepeater/Starfruit.
     * @param projectileCount How many projectiles to spawn per attack (Solves Pea Pod).
     * @param rangeTiles The range for Melee, Lobber splash, or Mine explosions.
     * @param isAoE Boolean flag for Melee strategies (Bonk Choy vs Phat Beet).
     * @param isInstaKill Boolean flag for Chomper.
     * @return The fully configured AttackStrategy
     */
    public static AttackStrategy createStrategy(
        AttackStrategyType type,
        Class<? extends Projectile> projectileClass,
        List<Integer> laneOffsets,
        int projectileCount,
        double rangeTiles,
        boolean isAoE,
        boolean isInstaKill) {

        switch (type) {
            case DIRECT_SHOOT:
                // Completely replaces MultiShoot, Diagonal, and Star strategies
                return new DirectShootStrategy(laneOffsets, projectileClass, projectileCount);

            case HOMING:
                // Defaults to RANDOM. Upgrades can change this dynamically inside HomingStrategy.
                return new HomingStrategy(projectileClass, HomingStrategy.TargetingMode.RANDOM);

            case MELEE:
                return new MeleeStrategy(rangeTiles, isAoE, isInstaKill);

            case MINE:
                return new MineStrategy(rangeTiles);

            case LOBBER:
                return new LobberStrategy(projectileClass, rangeTiles);

            case SUN_PRODUCE:
                return new SunProduceStrategy();

            default:
                // For Wall-nuts or passive plants that don't attack
                return null;
        }
    }
}
