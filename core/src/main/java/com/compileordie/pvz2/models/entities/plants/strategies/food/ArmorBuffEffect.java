package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.user.Player;

public class ArmorBuffEffect implements PlantFoodEffectStrategy {
    private final int bonusArmorHp;

    public ArmorBuffEffect(int bonusArmorHp) {
        this.bonusArmorHp = bonusArmorHp;
    }

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        // Heals the plant to full base HP (including upgrades) AND adds the armor!
        // This naturally prevents infinite stacking if fed multiple times.
        plant.setCurrentHp(plant.getBaseHp() + bonusArmorHp);

        // --- GRAPHICS PHASE HOOK ---
        // To draw the metal/crystal armor, the UI team just checks:
        // if (plant.getCurrentHp() > plant.getBaseHp())

        // Effect resolved! Reset the feed flag.
        plant.resetFeed();
    }
}
