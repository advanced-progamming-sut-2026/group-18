package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.enums.PlantTag;
import com.compileordie.pvz2.models.game.board.GameBoard;

import java.util.List;

public class MintActivateStrategy implements AttackStrategy {

    private final PlantTag familyTag; // E.g., PlantTag.PEA, PlantTag.FIRE
    private final int buffDurationTicks;
    private boolean hasActivated = false;
    private double activeTimer = 0;

    public MintActivateStrategy(PlantTag familyTag, int buffDurationTicks) {
        this.familyTag = familyTag;
        this.buffDurationTicks = buffDurationTicks;
    }

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        if (!hasActivated) {
            // Apply buff to all plants on the board that share the family tag
            List<Plant> allPlants = board.getAllPlants();
            for (Plant p : allPlants) {
                if (p.hasTag(familyTag)) {
                    // Temporarily boost their level/stats (you can add a specific applyMintBuff() in Plant.java)
                    p.applyLevelUpgrade(p.getLevel() + 3); // Example: boost by 3 levels temporarily
                }
            }
            hasActivated = true;
        }

        // Mints typically stay on the board for a short duration then disappear
        activeTimer += tickDelta;
        if (activeTimer >= buffDurationTicks) {
            plant.setCurrentHp(0); // Mint expires and leaves the board
        }
    }
}
