package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.PlantTemplate;
import com.compileordie.pvz2.models.entities.plants.factory.PlantFactory;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Tile;
import com.compileordie.pvz2.models.user.Player;

import java.util.Collections;
import java.util.List;

public class SpawnClonesEffect implements PlantFoodEffectStrategy {
    private final int cloneCount;
    private final PlantTemplate baseTemplate;

    public SpawnClonesEffect(int cloneCount, PlantTemplate baseTemplate) {
        this.cloneCount = cloneCount;
        this.baseTemplate = baseTemplate;
    }

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        // 1. Instantly arm the mine/plant that just received the Plant Food!
        plant.forceArm();

        // 2. Fetch random empty tiles
        List<Tile> emptyTiles = board.getEmptyTiles();
        Collections.shuffle(emptyTiles);

        int spawned = 0;

        // 3. Spawn the clones
        for (Tile tile : emptyTiles) {
            if (spawned >= cloneCount) break;

            // --- LILY PAD SPECIFIC LOGIC ---
            if (plant.getName().equals("Lily Pad")) {
                // If the plant is Lily Pad, ONLY spawn on water!
                if (!tile.isUnderWater()) continue;
            } else {
                // If it's Potato Mine, ONLY spawn on land!
                if (tile.isUnderWater()) continue;
            }

            double spawnX = tile.column * Constants.Game.TILE_SIZE;
            double spawnY = tile.row * Constants.Game.TILE_SIZE;

            // Build the clone
            Plant clone = PlantFactory.createPlant(baseTemplate, spawnX, spawnY);

            // A. Make sure the clones have the exact same upgrades as the parent!
            clone.applyLevelUpgrade(plant.getLevel());

            // B. Instantly arm them using the clean API!
            clone.forceArm();
            board.addPlant(clone);
            spawned++;
        }

        plant.resetFeed();
    }
}
