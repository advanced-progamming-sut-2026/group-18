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
import java.util.stream.Collectors;

public class SpawnClonesEffect implements PlantFoodEffectStrategy {
    private final int cloneCount;
    private final PlantTemplate baseTemplate;

    public SpawnClonesEffect(int cloneCount, PlantTemplate baseTemplate) {
        this.cloneCount = cloneCount;
        this.baseTemplate = baseTemplate;
    }

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {

        // 1. Teammate's logic: Get ALL tiles, but filter ONLY for the plantable ones!
        List<Tile> plantableTiles = board.getAllTiles().stream()
            .filter(Tile::isPlantable)
            .collect(Collectors.toList());

        // 2. Shuffle them so we get completely random locations across all lanes
        Collections.shuffle(plantableTiles);

        int spawned = 0;

        // 3. Loop through our safe, randomized list (fixes the 200-limit worry)
        for (Tile tile : plantableTiles) {
            if (spawned >= cloneCount) break;

            // Convert grid row/col to exact world coordinates
            double spawnX = tile.column * Constants.Game.TILE_SIZE;
            double spawnY = tile.row * Constants.Game.TILE_SIZE;

            // Build the clone
            Plant clone = PlantFactory.createPlant(baseTemplate, spawnX, spawnY);
            clone.tick(board, 99999); // Instantly arms Potato Mines

            // 4. Teammate's exact instruction: set the tile's plant field!
            tile.plant = clone;
            board.addPlant(clone);

            spawned++;
        }

        // Effect resolved! Reset the feed flag.
        plant.resetFeed();
    }
}
