package com.compileordie.pvz2.models.plants.base;

import com.compileordie.pvz2.models.plants.enums.PlantCategory;
import com.compileordie.pvz2.models.plants.strategies.attack.AttackStrategy;
import com.compileordie.pvz2.models.plants.strategies.food.PlantFoodEffectStrategy;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class ShooterPlants extends Plant {

    private String persianAbilityDescription;

    public ShooterPlants(PlantTemplate plantTemplate,
                         int x,
                         int y,
                         AttackStrategy attackStrategy,
                         PlantFoodEffectStrategy plantFoodEffect) {
        super(plantTemplate, x, y, attackStrategy, plantFoodEffect);
        // Grabbing the Persian text directly from your CSV data!
        this.persianAbilityDescription = plantTemplate.getBaseAbility();
    }

    @Override
    public void tickCore(GameBoard board, int tickDelta) {
        // Core shared logic for shooters can go here later if needed
    }

    @Override
    public void applyLevelUpgrade(int newLevel) {
        // STUBBED: Parsing strings like "HP +150" from the CSV takes too much time right now.
        // We will implement this ONLY after the core shooting loop works.
    }

    @Override
    public String getAbilityDescription() {
        return this.persianAbilityDescription;
    }

    @Override
    public PlantCategory getCategory() {
        return PlantCategory.SHOOTERS;
    }
}
