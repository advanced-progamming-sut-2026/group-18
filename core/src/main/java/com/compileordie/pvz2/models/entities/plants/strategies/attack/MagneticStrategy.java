package com.compileordie.pvz2.models.entities.plants.strategies.attack;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;

public class MagneticStrategy implements AttackStrategy {

    @Override
    public void attack(Plant plant, GameBoard board, int tickDelta) {
        if (!plant.isArmed()) return;

        double rangePixels = plant.getRangeTiles() * Constants.Game.TILE_HEIGHT;
        boolean stoleArmor = false;

        for (Zombie z : board.getAllZombies()) {
            if (z.isDead()) continue;

            double dist = Math.hypot(z.getX() - plant.getX(), z.getY() - plant.getY());

            //TODO:
//            if (dist <= rangePixels) {
//                // If the zombie has metal, stripMetalArmor() returns true!
//                if (z.stripMetalArmor()) {
//                    stoleArmor = true;
//                    break; // ONLY STEAL ONE ITEM AT A TIME!
//                }
            }
        }
        // TODO:
        // If no metal was found in range, hold the action so we don't waste the cooldown!
//        if (!stoleArmor) {
//            plant.holdAction = true;
//        }
    }

