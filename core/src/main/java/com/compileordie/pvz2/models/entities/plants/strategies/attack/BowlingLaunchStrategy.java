package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.projectiles.*;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class BowlingLaunchStrategy implements AttackStrategy {

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        if (!plant.isAlive() || plant.holdAction) return;

        String name = plant.getName();
        double startX = plant.getX();
        double startY = plant.getY();

        if (name.equals("Bowling Explode-o-nut")) {
            board.getActiveProjectiles().add(new BowlingExplodeProjectile(startX, startY, 1800));
        } else if (name.equals("Giant Wallnut")) {
            board.getActiveProjectiles().add(new BowlingGiantProjectile(startX, startY, 99999));
        } else {
            // Default: Bowling Wallnut
            board.getActiveProjectiles().add(new BowlingBounceProjectile(startX, startY, 300));
        }

        // Delete the shell from the grid!
        plant.holdAction = true;
        plant.die();
    }
}
