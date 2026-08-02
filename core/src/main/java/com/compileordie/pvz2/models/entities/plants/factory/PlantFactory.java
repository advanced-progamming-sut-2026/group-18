package com.compileordie.pvz2.models.entities.plants.factory;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.PlantTemplate;
import com.compileordie.pvz2.models.entities.plants.strategies.attack.AttackStrategy;
import com.compileordie.pvz2.models.entities.plants.strategies.food.PlantFoodEffectStrategy;

public class PlantFactory {

    /**
     * This is the only method you need to build any of the 69 plants.
     * It reads the template data (parsed from main_plantscsv.csv) and assembles the parts.
     *
     * @param template The base stats and config for this specific plant type.
     * @param x        The X coordinate on the board.
     * @param y        The Y coordinate on the board.
     * @return A fully functional, data-driven Plant object.
     */
    public static Plant createPlant(PlantTemplate template, double x, double y) {

        // 1. Ask the AttackStrategyFactory to build the parameterized attack logic
        AttackStrategy attackStrategy = AttackStrategyFactory.createStrategy(
            template.getAttackStrategyType(),
            template.getProjectileType(),
            template.getLaneOffsets(),
            template.getProjectileCount(),
            template.getRangeTiles(),
            template.isAoE(),
            template.isInstaKill()
        );

        // 2. Ask the FoodEffectFactory to build the ultimate ability logic
        // (Assuming you have a similar setup for your PlantFoodEffectStrategy)
        PlantFoodEffectStrategy foodStrategy = FoodEffectFactory.createEffect(
            template.getFoodEffectType(),
            template.getProjectileType(),
            template.getFoodEffectValue(), // e.g., how many projectiles to burst, or AoE radius
            template
        );

        // 3. Assemble the final concrete Plant object! No specific classes required.
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
