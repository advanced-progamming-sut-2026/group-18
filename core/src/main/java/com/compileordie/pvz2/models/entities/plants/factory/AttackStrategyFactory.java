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
        List<double[]> shootVectors,
        double rangeTiles,
        int maxBounces,
        boolean isAoE,
        boolean isInstantKill) {

        switch (type) {
            case MODIFIER_PASSIVE:
                return new ModifierPassiveStrategy();

            case DIRECT_SHOOT:
                return new DirectShootStrategy(laneOffsets, shootVectors, projectileClass);

            case CHARGE_SHOOT:
                return new ChargeShootStrategy(projectileClass);

            case HOMING:
                return new HomingStrategy(projectileClass, HomingStrategy.TargetingMode.RANDOM);

            case MELEE:
                return new MeleeStrategy(isAoE, isInstantKill);

            case MINE:
                return new MineStrategy(rangeTiles);

            case LOB:
                return new LobberStrategy(projectileClass, rangeTiles);

            case SQUASH:
                return new SquashStrategy();

            case SUN_PRODUCE:
                return new SunProduceStrategy();

            case TANGLE:
                return new TangleKelpStrategy();

            case BOWLING:
                return new BowlingStrategy(projectileClass, maxBounces);

            case DIGEST:
                return new DigestStrategy(rangeTiles);

            case ATTRACT:
                return new AttractStrategy();

            case MAGNETIC:
                return new MagneticStrategy();

            case INSTANT_USE:
                return new InstantUseStrategy();

            case MINT_ACTIVATE:
                return new MintActivateStrategy();
            case NONE:
                return null;
        }
        return null;
    }
}
