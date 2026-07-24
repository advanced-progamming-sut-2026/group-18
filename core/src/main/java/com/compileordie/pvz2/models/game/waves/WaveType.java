package com.compileordie.pvz2.models.game.waves;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.Plant;
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
            placeZombiesRandomly(gameBoard, zombies, 0);
        }
    },
    ANCIENT_EGYPT {
        @Override
        public void spawnWave(WaveManager self, GameBoard gameBoard, List<ZombieType> zombies) {
            if (self.currentWave == self.waveNumber) {
                // Final wave Tornadoes: Drop zombies 1 to 4 columns further in
                placeZombiesWithTornadoOffset(gameBoard, zombies);
            } else {
                placeZombiesRandomly(gameBoard, zombies, 0);
            }
        }
    },
    FROSTBITE_CAVE {
        @Override
        public void spawnWave(WaveManager self, GameBoard gameBoard, List<ZombieType> zombies) {
            // Trigger environmental effect before spawning
            triggerIcyWind(gameBoard);
            placeZombiesRandomly(gameBoard, zombies, 0);
        }
    },
    BIG_WAVE_BEACH {
        @Override
        public void spawnWave(WaveManager self, GameBoard gameBoard, List<ZombieType> zombies) {
            // Tide shifts with every wave
            changeTide(gameBoard);

            // Assume low shore ambush logic can be handled by offsetting some zombies
            // You can loop over `zombies` here and set specific spawn states if they emerge from water
            placeZombiesRandomly(gameBoard, zombies, 0);
        }
    },
    DARK_AGES {
        @Override
        public void spawnWave(WaveManager self, GameBoard gameBoard, List<ZombieType> zombies) {
            // Necromancy: Spawn new tombs at the start of the wave
            spawnDarkAgesTombs(gameBoard);

            // Split zombies: Some spawn from tombs, rest spawn normally
            int tombSpawnCount = zombies.size() / 3;
            for (int i = 0; i < tombSpawnCount; i++) {
                spawnZombieFromRandomTomb(gameBoard, zombies.get(i));
            }

            // Normal placement for the remaining zombies
            placeZombiesRandomly(gameBoard, zombies.subList(tombSpawnCount, zombies.size()), 0);
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

            double x = (gameBoard.totalCols - 0.5f - columnOffset) * Constants.Game.TILE_SIZE;
            double y = (randomLaneIndex + 0.5f) * Constants.Game.TILE_SIZE;
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
        Random random = new Random();
        List<Lane> lanes = gameBoard.lanes;
        if (lanes == null || lanes.isEmpty()) return;

        int affectedLaneCount = random.nextInt(lanes.size()) + 1;
        for (int i = 0; i < affectedLaneCount; i++) {
            Lane lane = lanes.get(random.nextInt(lanes.size()));
            for (Tile tile : lane.tiles) {
                Plant plant = tile.plant;
                // TODO: Activate mechanism:
                /*if (plant != null && !plant.hasFireTag()) {
                    plant.freezeLevel = Math.min(3, plant.freezeLevel + 1);
                }*/
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

        int tombsToSpawn = Math.min(emptyTiles.size(), random.nextInt(3) + 1);
        for (int i = 0; i < tombsToSpawn; i++) {
            Tile targetTile = emptyTiles.remove(random.nextInt(emptyTiles.size()));
            targetTile.tomb = new Tomb(700,
                targetTile.row,
                targetTile.column,
                (targetTile.row + 0.5f) * Constants.Game.TILE_SIZE,
                (targetTile.column + 0.5f) * Constants.Game.TILE_SIZE);
        }
    }
}
