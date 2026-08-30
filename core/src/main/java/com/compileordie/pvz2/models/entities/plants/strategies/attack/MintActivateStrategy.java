package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class MintActivateStrategy implements AttackStrategy {

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {

        // 1. Instantly trigger on the first frame!
        if (plant.windupTimer == 0) {
            for (Plant p : board.getAllPlants()) {
                // Buff all plants of the same category (except the Mint itself!)
                if (p != plant && p.getCategory() == plant.getCategory()) {
                    p.feed(board, null);
                }
            }
            // Signal the graphics engine that we are alive and active
            plant.isWindingUp = true;
        }

        // 2. The Timer Engine
        plant.holdAction = true; // Lock the engine so this method runs every tick
        plant.windupTimer += tickDelta;

        // Base duration (e.g., 85 ticks) + Level Upgrades!
        double totalDuration = plant.getActionIntervalTicks() + plant.getMintDurationBonusTicks();

        // 3. The Outro Phase
        // Once the duration is hit, we set isWindingUp to false so the graphics engine plays "outro"
        if (plant.windupTimer >= totalDuration) {
            plant.isWindingUp = false;
        }

        // 4. The Exit
        // Give the outro clip exactly 20 ticks (~1 second) to physically play on screen before deleting the plant!
        if (plant.windupTimer >= totalDuration + 33.0) {
            plant.die();
        }
    }
}
