package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class MintActivateStrategy implements AttackStrategy {

    private boolean hasActivated = false;
    private double activeTimer = 0;

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {

        // 1. Instantly trigger on the first frame!
        if (!hasActivated) {
            for (Plant p : board.getAllPlants()) {
                // Buff all plants of the same category (except the Mint itself!)
                if (p != plant && p.getCategory() == plant.getCategory()) {

                    // The ultimate mint power: Instant Plant Food without consuming a leaf!
                    p.feed(board, null);
                }
            }
            hasActivated = true;
        }

        // 2. The Loop Engine
        // By setting holdAction = true, we prevent Plant.java from resetting the action timer.
        // This guarantees this attack() method gets called every single tick!
        plant.holdAction = true;
        activeTimer += tickDelta;

        // Base duration (e.g., 85 ticks) + Level Upgrades!
        double totalDuration = plant.getActionIntervalTicks() + plant.getMintDurationBonusTicks();

        // 3. The Exit
        if (activeTimer >= totalDuration) {
            plant.die();
        }
    }
}
