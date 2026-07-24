package com.compileordie.pvz2.models.game.waves;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.Tomb;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Lane;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public enum WaveType {
    NORMAL {
        @Override
        public void spawnWave(WaveManager self, GameBoard gameBoard, List<Zombie> zombies) {
            placeZombiesRandomly(gameBoard, zombies, 0);
        }
    },
    ANCIENT_EGYPT {
        @Override
        public void spawnWave(WaveManager self, GameBoard gameBoard, List<Zombie> zombies) {
            if (self.currentWave == self.waveNumber) {
                // Final wave Tornadoes: Drop zombies 1 to 4 columns further in
                placeZombiesRandomly(gameBoard, zombies, new Random().nextInt(4) + 1);
            } else {
                placeZombiesRandomly(gameBoard, zombies, 0);
            }
        }
    },
    FROSTBITE_CAVE {
        @Override
        public void spawnWave(WaveManager self, GameBoard gameBoard, List<Zombie> zombies) {
            // Trigger environmental effect before spawning
            gameBoard.triggerIcyWind();
            placeZombiesRandomly(gameBoard, zombies, 0);
        }
    },
    BIG_WAVE_BEACH {
        @Override
        public void spawnWave(WaveManager self, GameBoard gameBoard, List<Zombie> zombies) {
            // Tide shifts with every wave
            gameBoard.changeTide();

            // Assume low shore ambush logic can be handled by offsetting some zombies
            // You can loop over `zombies` here and set specific spawn states if they emerge from water
            placeZombiesRandomly(gameBoard, zombies, 0);
        }
    },
    DARK_AGES {
        @Override
        public void spawnWave(WaveManager self, GameBoard gameBoard, List<Zombie> zombies) {
            // Necromancy: Spawn new tombs at the start of the wave
            gameBoard.spawnDarkAgesTombs();

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
        public void spawnWave(WaveManager self, GameBoard gameBoard, List<Zombie> zombies) {
            // Do nothing for manual/special levels (e.g. Vase breaker, Plant What You Get)
        }
    };

    abstract public void spawnWave(WaveManager self, GameBoard gameBoard, List<Zombie> zombies);

    /**
     * Helper method to randomly assign a list of zombies into the 5 lanes.
     */
    protected void placeZombiesRandomly(GameBoard gameBoard, List<Zombie> zombies, int columnOffset) {
        List<Lane> lanes = gameBoard.lanes;
        Random random = new Random();

        for (Zombie zombie : zombies) {
            int randomLaneIndex = random.nextInt(lanes.size());

            if (columnOffset > 0) {
                // Assuming Zombie has a method to adjust spawn location for ambushes/tornadoes
                zombie.setX((gameBoard.totalCols - columnOffset) * Constants.Game.TILE_SIZE);
            }

            lanes.get(randomLaneIndex).zombies.add(zombie);
        }
    }

    /**
     * Helper method to spawn a zombie directly from a Dark Ages tomb.
     */
    protected void spawnZombieFromRandomTomb(GameBoard gameBoard, Zombie zombie) {
        // Assuming GameBoard has a way to get all active tombs on the map
        ArrayList<Tomb> activeTombs = gameBoard.getAllTombs();

        if (activeTombs != null && !activeTombs.isEmpty()) {
            Random random = new Random();
            Tomb selectedTomb = activeTombs.get(random.nextInt(activeTombs.size()));
            selectedTomb.spawnZombie(gameBoard, zombie);

            AppModel.addAfterPrompt("Zombie " + zombie.getType() + " spawned from a tomb!");
        } else {
            // Fallback: If no tombs exist yet, just spawn normally
            placeZombiesRandomly(gameBoard, List.of(zombie), 0);
        }
    }
}
