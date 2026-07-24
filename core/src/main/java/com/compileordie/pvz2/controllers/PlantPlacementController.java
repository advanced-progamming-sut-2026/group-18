package com.compileordie.pvz2.controllers;

import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.PlantTemplate;
import com.compileordie.pvz2.models.entities.plants.factory.PlantFactory;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Tile;
import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.models.game.economy.SeedPacket;

public class PlantPlacementController {

    /**
     * Called by the UI when a player tries to plant.
     *
     * @return true if successfully planted, false if failed (insufficient sun, tile full, etc.)
     */
    public boolean attemptPlacement(SeedPacket packet, Tile targetTile, Player player, GameBoard board) {
        // 1. Check if the seed packet is on cooldown
        if (!packet.isAvailable()) {
            System.out.println("Seed packet is still recharging!");
            return false;
        }

        PlantTemplate template = packet.getTemplate();

        // 2. Check if the player has enough sun
        if (player.getSunCount() < template.getCost()) {
            System.out.println("Not enough sun! Requires: " + template.getCost());
            return false;
        }

        // 3. Check if the tile is valid for this plant
        // (e.g., Water plants need water tiles, ground plants need ground or Lily Pads)
        if (!isTileValidForPlant(template, targetTile)) {
            System.out.println("Cannot plant here!");
            return false;
        }

        // 4. Deduct Sun
        player.spendSun(template.getCost());

        // 5. Build the Plant using our masterpiece Factory
        Plant newPlant = PlantFactory.createPlant(template, targetTile.getX(), targetTile.getY());

        // 6. Register it on the board
        board.addPlant(newPlant);
        targetTile.setPlant(newPlant); // If your tiles track what is on them

        // 7. Trigger specific immediate effects (Like Mints!)
        if (newPlant.getAttackStrategy() instanceof com.compileordie.pvz2.models.entities.plants.strategies.attack.MintActivateStrategy) {
            // Force the mint to execute its buff immediately upon placement
            newPlant.getAttackStrategy().attack(newPlant, board, 0);
        }

        // 8. Put the packet on cooldown
        packet.startCooldown();

        System.out.println("Successfully planted " + template.getName() + "!");
        return true;
    }

    /**
     * Helper to enforce placement rules (Lily Pads, Grave blocking, etc.)
     */
    private boolean isTileValidForPlant(PlantTemplate template, Tile tile) {
        // If the tile has a grave, nothing can be planted except a Grave Buster
        if (tile.hasGrave()) {
            return template.getName().equals("Grave Buster");
        }

        // If the tile already has a plant
        if (tile.hasPlant()) {
            Plant existingPlant = tile.getPlant();

            // Pumpkin can be planted OVER other plants
            if (template.getName().equals("Pumpkin")) {
                return !existingPlant.getName().equals("Pumpkin");
            }
            // Other plants can be planted ON TOP of Lily Pads
            if (existingPlant.getName().equals("Lily Pad") && !template.getName().equals("Lily Pad")) {
                return true;
            }

            return false; // Tile is occupied
        }

        // Add water tile checks here if you have a pool level
        // if (tile.isWater() && !template.getName().equals("Lily Pad") && !template.getName().equals("Tangle Kelp")) return false;

        return true;
    }
}
