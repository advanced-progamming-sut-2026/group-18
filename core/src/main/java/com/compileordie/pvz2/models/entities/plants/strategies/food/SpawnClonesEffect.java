package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.badlogic.gdx.utils.Timer;
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
        // 1. Instantly arm the parent mine!
        plant.forceArm();

        // 2. Fetch random empty tiles
        List<Tile> emptyTiles = board.getEmptyTiles();
        Collections.shuffle(emptyTiles);

        // 3. Delay the actual clone spawning so it matches the "toss" animation frame!
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (plant.isDead()) return;

                int spawned = 0;
                for (Tile tile : emptyTiles) {
                    if (spawned >= cloneCount) break;

                    if (plant.getName().equals("Lily Pad") && !tile.isUnderWater()) continue;
                    if (!plant.getName().equals("Lily Pad") && tile.isUnderWater()) continue;

                    double spawnX = Constants.Game.PADDING_X + (tile.column * Constants.Game.TILE_WIDTH) + (Constants.Game.TILE_WIDTH / 2.0);
                    double spawnY = Constants.Game.PADDING_Y + (tile.row * Constants.Game.TILE_HEIGHT) + (Constants.Game.TILE_HEIGHT / 2.0);

                    Plant clone = PlantFactory.createPlant(baseTemplate, spawnX, spawnY);
                    if (clone != null) {
                        clone.applyLevelUpgrade(plant.getLevel());
                        clone.forceArm(); // Clones are instantly armed!
                        board.addPlant(clone);
                        spawned++;
                    }
                }
            }
        }, 1.0f);

        // 4. End the Plant Food state cleanly after the 3-part animation completes
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                plant.resetFeed();
            }
        }, 2.5f);
    }
}
