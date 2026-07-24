package com.compileordie.pvz2.models.entities.plants.strategies.food;

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
        List<Tile> emptyTiles = board.getEmptyTiles();
        Collections.shuffle(emptyTiles);

        int spawned = 0;
        for (Tile tile : emptyTiles) {
            if (spawned >= cloneCount) break;

            Plant clone = PlantFactory.createPlant(baseTemplate, tile.getX(), tile.getY());
            clone.tick(board, 99999);
            board.addPlant(clone);
            spawned++;
        }
    }
}
