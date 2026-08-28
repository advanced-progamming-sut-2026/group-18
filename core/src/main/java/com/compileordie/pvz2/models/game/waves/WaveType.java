package com.compileordie.pvz2.models.game.waves;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.enums.PlantTag;
import com.compileordie.pvz2.models.entities.zombies.ZombieBuilder;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.Tomb;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Lane;
import com.compileordie.pvz2.models.game.board.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public enum WaveType {
    NORMAL {
        @Override
        public void spawnWave(WaveManager self, GameBoard gameBoard, List<ZombieType> zombies) {
        }
    },
    ANCIENT_EGYPT {
        @Override
        public void spawnWave(WaveManager self, GameBoard gameBoard, List<ZombieType> zombies) {
            // Handled via WaveManager queue
        }
    },
    FROSTBITE_CAVE {
        @Override
        public void spawnWave(WaveManager self, GameBoard gameBoard, List<ZombieType> zombies) {
            // 75% probability for icy winds at the start of a wave
            if (new Random().nextFloat() < 0.75f) {
                triggerIcyWind(gameBoard);
            }
        }
    },
    BIG_WAVE_BEACH {
        @Override
        public void spawnWave(WaveManager self, GameBoard gameBoard, List<ZombieType> zombies) {
            changeTide(gameBoard); // Recalculates water/tide level at start of wave
        }
    },
    DARK_AGES {
        @Override
        public void spawnWave(WaveManager self, GameBoard gameBoard, List<ZombieType> zombies) {
            spawnDarkAgesTombs(gameBoard); // Calls tomb spawner stub
        }
    },
    ZOMBOTANY {
        @Override
        public void spawnWave(WaveManager self, GameBoard gameBoard, List<ZombieType> zombies) {
            NORMAL.spawnWave(self, gameBoard, zombies);
        }
    },
    NO_WAVES {
        @Override
        public void spawnWave(WaveManager self, GameBoard gameBoard, List<ZombieType> zombies) {
            // Do nothing for manual/special levels (e.g. Vase breaker, Plant What You Get)
        }
    };

    abstract public void spawnWave(WaveManager self, GameBoard gameBoard, List<ZombieType> zombies);

    /**
     * Helper method to randomly assign a list of zombies into the 5 lanes.
     */
    protected void placeZombiesRandomly(GameBoard gameBoard, List<ZombieType> zombies, int columnOffset) {
        List<Lane> lanes = gameBoard.lanes;
        Random random = new Random();

        for (ZombieType zombieType : zombies) {
            int randomLaneIndex = random.nextInt(lanes.size());

            double x = 18f;
            double y = (randomLaneIndex) * Constants.Game.TILE_HEIGHT + Constants.UI.BOTTOM_LINE_METER;

            Zombie zombie = ZombieBuilder.create(zombieType, x, y, randomLaneIndex);
            lanes.get(randomLaneIndex).zombies.add(zombie);
        }
    }

    /**
     * Helper method to spawn a zombie directly from a Dark Ages tomb.
     */
    protected void spawnZombieFromRandomTomb(GameBoard gameBoard, ZombieType zombieType) {
        // Assuming GameBoard has a way to get all active tombs on the map
        ArrayList<Tomb> activeTombs = gameBoard.getAllTombs();

        if (activeTombs != null && !activeTombs.isEmpty()) {
            Random random = new Random();
            Tomb selectedTomb = activeTombs.get(random.nextInt(activeTombs.size()));
            selectedTomb.spawnZombie(gameBoard, zombieType);

            AppModel.addAfterPrompt("Zombie " + zombieType + " spawned from a tomb!");
        } else {
            // Fallback: If no tombs exist yet, just spawn normally
            placeZombiesRandomly(gameBoard, List.of(zombieType), 0);
        }
    }

    protected void placeZombiesWithTornadoOffset(GameBoard gameBoard, List<ZombieType> zombies) {
        placeZombiesRandomly(gameBoard, zombies, new Random().nextInt(4) + 1);
    }

    protected void triggerIcyWind(GameBoard gameBoard) {
        List<Lane> lanes = gameBoard.lanes;
        if (lanes == null || lanes.isEmpty()) return;

        Random random = new Random();
        // Pick 1 specific row
        Lane targetLane = lanes.get(random.nextInt(lanes.size()));
        for (Tile tile : targetLane.tiles) {
            Plant plant = tile.plant;
            // Document rule: "کلیه گیاهان (به جز گیاهانی که تگ آتشین دارند)..."
            if (plant != null && !plant.hasTag(PlantTag.FIRE)) {
                // This single method now handles the 3 levels and the 600 HP ice block!
                plant.addChill();

            }
        }
    }


    protected void changeTide(GameBoard gameBoard) {
        Random random = new Random();
        int maxAllowedWaterColumns = gameBoard.maxTideLevel;

        gameBoard.tideLevel = random.nextInt(maxAllowedWaterColumns) + 1;
    }

    protected void spawnDarkAgesTombs(GameBoard gameBoard) {
        Random random = new Random();
        List<Tile> emptyTiles = new ArrayList<>();

        for (Lane lane : gameBoard.lanes) {
            for (Tile tile : lane.tiles) {
                if (tile.isEmpty()) {
                    emptyTiles.add(tile);
                }
            }
        }

        if (emptyTiles.isEmpty()) return;

        int tombsToSpawn = Math.min(emptyTiles.size(), random.nextInt(2) + 1);
        for (int i = 0; i < tombsToSpawn; i++) {
            Tile targetTile = emptyTiles.remove(random.nextInt(emptyTiles.size()));
            // TODO: Make it dark ages type of tomb
            targetTile.obstacle = new Tomb(700,
                targetTile.row,
                targetTile.column,
                (targetTile.row + 0.5f) * Constants.Game.TILE_HEIGHT,
                (targetTile.column + 0.5f) * Constants.Game.TILE_WIDTH);
        }
    }
}
