package com.compileordie.pvz2.models.entities.plants.factory;

import com.compileordie.pvz2.models.entities.plants.enums.AttackStrategyType;
import com.compileordie.pvz2.models.entities.plants.strategies.attack.*;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;

import java.util.List;

public class AttackStrategyFactory {

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
            case MULTI_SHOOT:
                // Peashooter, Repeater, Threepeater
                return new DirectShootStrategy(laneOffsets, null, projectileClass, projectileCount);

            case DIAGONAL:
                // Rotobaga: 4 vectors mapping to your bottom-left coordinate system
                List<int[]> diagonalVectors = List.of(
                    new int[]{-1, 1},  // Backward-Up
                    new int[]{-1, -1}, // Backward-Down
                    new int[]{1, 1},   // Forward-Up
                    new int[]{1, -1}   // Forward-Down
                );
                return new DirectShootStrategy(List.of(0), diagonalVectors, projectileClass, projectileCount);

            case STAR:
                // Starfruit: 5 vectors
                List<int[]> starVectors = List.of(
                    new int[]{-1, 0},  // Backward
                    new int[]{0, 1},   // Up
                    new int[]{0, -1},  // Down
                    new int[]{1, 1},   // Forward-Up
                    new int[]{1, -1}   // Forward-Down
                );
                return new DirectShootStrategy(List.of(0), starVectors, projectileClass, projectileCount);

            case HOMING:
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
                return null;
        }
    }
}
