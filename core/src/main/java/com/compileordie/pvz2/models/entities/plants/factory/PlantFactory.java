package com.compileordie.pvz2.models.entities.plants.factory;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.PlantTemplate;
import com.compileordie.pvz2.models.entities.plants.strategies.attack.AttackStrategy;
import com.compileordie.pvz2.models.entities.plants.strategies.food.PlantFoodEffectStrategy;

public class PlantFactory {

    public static Plant createPlant(PlantTemplate template, double x, double y) {

        AttackStrategy attackStrategy = AttackStrategyFactory.createStrategy(
            template.getAttackStrategyType(),
            template.getProjectileType(),
            template.getLaneOffsets(),
            template.getShootVectors(),
            template.getRangeTiles(),
            3,
            template.isAoE(),
            template.isInstantKill()
        );

        PlantFoodEffectStrategy foodStrategy = FoodEffectFactory.createEffect(
            template.getFoodEffectType(),
            template.getProjectileType(),
            template.getFoodEffectValue(),
            template
        );

        return new Plant(
            template.getName(),
            template.getCategory(),
            template.getTags(),
            x,
            y,
            template.getBaseHp(),
            template.getBaseDamage(),
            template.getCost(),
            template.getActionIntervalTicks(),
            attackStrategy,
            foodStrategy,
            template.getUpgradeMap()
        );
    }
}
