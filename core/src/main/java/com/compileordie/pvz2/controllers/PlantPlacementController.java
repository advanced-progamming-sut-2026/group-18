package com.compileordie.pvz2.controllers;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.PlantTemplate;
import com.compileordie.pvz2.models.entities.plants.enums.PlantTag;
import com.compileordie.pvz2.models.entities.plants.factory.PlantFactory;
import com.compileordie.pvz2.models.entities.plants.strategies.attack.MintActivateStrategy;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Tile;
import com.compileordie.pvz2.models.game.economy.SeedPacket;
import com.compileordie.pvz2.models.user.Player;

// Clean imports for the Quest System
import com.compileordie.pvz2.models.missions.quests.QuestEvent;
import com.compileordie.pvz2.models.missions.quests.QuestManager;

public class PlantPlacementController {
    /**
     * Called by the UI when a player tries to plant.
     *
     * @return true if successfully planted, false if failed (insufficient sun, tile full, etc.)
     */
    public boolean attemptPlacement(SeedPacket packet, Tile targetTile, Player player, GameBoard board) {
        PlantTemplate template = packet.getTemplate();

        // 1. Validate placement
        if (!canPlacePlant(packet, template, targetTile, board)) {
            return false;
        }

        // 2. Deduct Sun
        board.economyManager.sunAmount -= template.getCost();

        // 3. Create and register the plant
        Plant newPlant = createAndRegisterPlant(template, targetTile, board);

        // 4. Apply immediate gameplay effects (mints, boosts)
        applyPostPlacementEffects(newPlant, template, player, board);

        // 5. Put the packet on cooldown
        packet.startCooldown();

        System.out.println("Successfully planted " + template.getName() + "!");
        return true;
    }

    /**
     * Checks all preconditions required to place a plant.
     */
    private boolean canPlacePlant(SeedPacket packet, PlantTemplate template, Tile targetTile, GameBoard board) {
        if (!packet.isAvailable()) {
            System.out.println("Seed packet is still recharging!");
            return false;
        }

        if (board.economyManager.sunAmount < template.getCost()) {
            System.out.println("Not enough sun! Requires: " + template.getCost());
            return false;
        }

        // Assuming isTileValidForPlant is an existing method in your class
        if (!isTileValidForPlant(template, targetTile)) {
            System.out.println("Cannot plant here!");
            return false;
        }

        return true;
    }

    /**
     * Handles coordinate calculation, factory creation, and board registration.
     */
    private Plant createAndRegisterPlant(PlantTemplate template, Tile targetTile, GameBoard board) {
        double spawnX = targetTile.column * Constants.Game.TILE_WIDTH;
        double spawnY = targetTile.row * Constants.Game.TILE_HEIGHT;

        Plant newPlant = PlantFactory.createPlant(template, spawnX, spawnY);

        board.addPlant(newPlant);
        targetTile.plant = newPlant;

        return newPlant;
    }

    /**
     * Triggers any immediate effects that occur right after a plant hits the board.
     */
    private void applyPostPlacementEffects(Plant newPlant, PlantTemplate template, Player player, GameBoard board) {
        // Trigger specific immediate effects (Like Mints)
        if (newPlant.getAttackStrategy() instanceof MintActivateStrategy) {
            newPlant.getAttackStrategy().attack(newPlant, board, 0);
        }

        // Check if the player has boosted this plant (triggers plant food effect immediately)
        PlantType pType = PlantType.getByName(template.getName());
        if (pType != null && player.plantBoosts.getOrDefault(pType, false)) {
            if (newPlant.getFoodEffectStrategy() != null) {
                newPlant.getFoodEffectStrategy().applyEffect(newPlant, board, player);
            }
        }

        // --- QUEST INJECTION: PLANTING ---
        QuestManager.dispatch(QuestEvent.PLANT_PLANTED, 1, template.getName());

        // If it's an explosive (Cherry Bomb, Jalapeno, etc.), dispatch the explosive quest!
        if (template.getTags() != null && template.getTags().contains(PlantTag.EXPLOSIVE)) {
            QuestManager.dispatch(QuestEvent.EXPLOSIVE_PLANTED, 1, null);
        }
        // ---------------------------------

        System.out.println("Successfully planted " + template.getName() + "!");
    }

    /**
     * Helper to enforce placement rules (Lily Pads, Graves, Ice, Water, etc.)
     */
    private boolean isTileValidForPlant(PlantTemplate template, Tile tile) {
        // 1. Frostbite Caves: Cannot plant on Slider or Frozen tiles
        if (tile.type != null) {
            String tileName = tile.type.name().toUpperCase();
            if (tileName.contains("SLIDER") || tileName.contains("FROZEN") || tileName.contains("ICE")) {
                return false;
            }
        }

        // 2. Grave Blocking: Only Grave Buster can be planted on Graves
        if (tile.obstacle != null && tile.obstacle.getType().name().contains("TOMB")) {
            return template.getName().equals("Grave Buster");
        }

        // 3. Big Wave Beach: Water tile rules
        boolean isWaterTile = (tile.type != null && tile.type.name().contains("WATER"));
        if (isWaterTile) {
            boolean isWaterPlant = template.getTags() != null && template.getTags().contains(PlantTag.WATER);
            boolean isLilyPadOrKelp = template.getName().equals("Lily Pad") || template.getName().equals("Tangle Kelp");

            if (!isWaterPlant && !isLilyPadOrKelp) {
                // Must have a Lily Pad already on this water tile to plant a normal plant
                if (tile.plant == null || !tile.plant.getName().equals("Lily Pad")) {
                    return false;
                }
            }
        }

        // 4. Existing Plant layering rules
        if (tile.plant != null) {
            Plant existingPlant = tile.plant;

            // Pumpkin can be planted OVER other plants
            if (template.getName().equals("Pumpkin")) {
                return !existingPlant.getName().equals("Pumpkin");
            }
            // Normal plants can be planted ON TOP of Lily Pads
            if (existingPlant.getName().equals("Lily Pad") && !template.getName().equals("Lily Pad")) {
                return true;
            }

            return false; // Tile is occupied
        }

        return true;
    }
}
