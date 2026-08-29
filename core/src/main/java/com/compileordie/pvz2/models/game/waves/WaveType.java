package com.compileordie.pvz2.models.game.waves;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.enums.PlantTag;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.Tomb;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.TombType;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Lane;
import com.compileordie.pvz2.models.game.board.Tile;
import com.compileordie.pvz2.views.helpers.ToastManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public enum WaveType {
    NORMAL {
        @Override
        public void wave(WaveManager self, GameBoard gameBoard, List<ZombieType> zombies) {
        }
    },
    ANCIENT_EGYPT {
        @Override
        public void wave(WaveManager self, GameBoard gameBoard, List<ZombieType> zombies) {
            // Handled via WaveManager queue
        }
    },
    FROSTBITE_CAVE {
        @Override
        public void wave(WaveManager self, GameBoard gameBoard, List<ZombieType> zombies) {
            // 75% probability for icy winds at the start of a wave
            if (new Random().nextFloat() < 0.75f) {
                triggerIcyWind(self, gameBoard);
            }
        }
    },
    BIG_WAVE_BEACH {
        @Override
        public void wave(WaveManager self, GameBoard gameBoard, List<ZombieType> zombies) {
            changeTide(gameBoard); // Recalculates water/tide level at start of wave
        }
    },
    DARK_AGES {
        @Override
        public void wave(WaveManager self, GameBoard gameBoard, List<ZombieType> zombies) {
            spawnDarkAgesTombs(gameBoard); // Calls tomb spawner stub
            triggerNecromancy(gameBoard, self);
        }
    },
    ZOMBOTANY {
        @Override
        public void wave(WaveManager self, GameBoard gameBoard, List<ZombieType> zombies) {
            NORMAL.wave(self, gameBoard, zombies);
        }
    },
    NO_WAVES {
        @Override
        public void wave(WaveManager self, GameBoard gameBoard, List<ZombieType> zombies) {
            // Do nothing for manual/special levels (e.g. Vase breaker, Plant What You Get)
        }
    };

    abstract public void wave(WaveManager self, GameBoard gameBoard, List<ZombieType> zombies);

    protected void triggerIcyWind(WaveManager self, GameBoard gameBoard) {
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

        self.chillWindRow = targetLane.row;
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
                if (tile.isEmpty() && tile.column > 3) {
                    emptyTiles.add(tile);
                }
            }
        }

        if (emptyTiles.isEmpty()) return;

        int tombsToSpawn = Math.min(emptyTiles.size(), random.nextInt(2));
        for (int i = 0; i < tombsToSpawn; i++) {
            Tile targetTile = emptyTiles.remove(random.nextInt(emptyTiles.size()));
            Tomb tomb = new Tomb(700,
                targetTile.row,
                targetTile.column,
                (targetTile.column + 0.5f) * Constants.Game.TILE_WIDTH + Constants.Game.PADDING_X,
                (targetTile.row + 0.5f) * Constants.Game.TILE_HEIGHT + Constants.Game.PADDING_Y);
            int randomness = random.nextInt(100);
            if (randomness < 20) {
                tomb.type = TombType.SUN;
            } else if (randomness < 40) {
                tomb.type = TombType.PLANT_FOOD;
            }
            targetTile.obstacle = tomb;
        }
    }

    protected void triggerNecromancy(GameBoard gameBoard, WaveManager self) {
        ArrayList<Tomb> activeTombs = gameBoard.getAllTombs();
        if (activeTombs == null || activeTombs.isEmpty()) return;

        Random random = new Random();
        self.pendingZombieQueue.add(ZombieType.PARASOL_ZOMBIE);
        Tomb selectedTomb = activeTombs.get(random.nextInt(activeTombs.size()));
        ZombieType zombieType = self.pendingZombieQueue.poll();
        if (zombieType != null) {
            selectedTomb.spawnZombie(gameBoard, zombieType);
            ToastManager.showMessage("Necromancy! A " + zombieType + " rose from a tomb!");
        }
    }
}
