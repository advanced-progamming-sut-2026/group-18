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
        List<int[]> shootVectors,
        double rangeTiles,
        boolean isAoE,
        boolean isInstantKill) {

        switch (type) {
            case DIRECT_SHOOT:
                return new DirectShootStrategy(laneOffsets, shootVectors, projectileClass);

            case HOMING:
                return new HomingStrategy(projectileClass, HomingStrategy.TargetingMode.RANDOM);

            case MELEE:
                return new MeleeStrategy(rangeTiles, isAoE, isInstantKill);

            case MINE:
                return new MineStrategy(rangeTiles);

            case LOB:
                return new LobberStrategy(projectileClass, rangeTiles);

            case SUN_PRODUCE:
                return new SunProduceStrategy();

            default:
                return null;
        }
    }
}
