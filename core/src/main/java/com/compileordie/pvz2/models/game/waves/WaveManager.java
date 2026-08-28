package com.compileordie.pvz2.models.game.waves;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.zombies.ZombieBuilder;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.Tomb;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Lane;
import com.compileordie.pvz2.models.game.board.Tile;
import com.compileordie.pvz2.models.game.board.TileType;
import com.compileordie.pvz2.models.game.levels.ChapterType;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;
import com.compileordie.pvz2.views.helpers.ToastManager;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class WaveManager {
    @FunctionalInterface
    public interface WaveEventListener {
        void onWaveStarted(int currentWave, int totalWaves, boolean isFinalWave, String message);
    }

    public GameBoard gameBoard;
    public WaveType type;
    public int waveNumber;
    public int currentWave;
    public int waveBudget;
    public double previousWaveTotalMaxHealth;
    public int tickCounter;
    public boolean shouldStartWaves;
    public final Queue<ZombieType> pendingZombieQueue = new ArrayDeque<>();
    private float spawnTimer = 0f;
    private WaveEventListener listener;
    private final Random random = new Random();
    private boolean isSpawningFinalWave = false;

    public WaveManager(GameBoard gameBoard, WaveType type, int waveNumber, boolean shouldStartWaves) {
        this.gameBoard = gameBoard;
        this.type = type;
        this.waveNumber = waveNumber;
        this.currentWave = 1;
        this.waveBudget = (int) Math.floor(ConfigManager.gameplay().basicWaveBudget * AppModel.player.getDLIncrease());
        this.previousWaveTotalMaxHealth = 0;
        this.tickCounter = 0;
        this.shouldStartWaves = shouldStartWaves;
    }

    public void setWaveEventListener(WaveEventListener listener) {
        this.listener = listener;
    }

    public void tick(int ticks) {
        tickCounter += ticks;

        processSpawnQueue(ticks);

        if (waveNumber != -1 && currentWave > waveNumber) {
            return;
        }

        double currentHealthSum = gameBoard.getAllZombies()
            .stream()
            .filter(Zombie::isAlive)
            .mapToDouble(Zombie::getHealth)
            .sum();

        boolean isQueueEmpty = pendingZombieQueue.isEmpty();
        boolean canStartFirstWave = shouldStartWaves && currentWave == 1;
        boolean canStartNextWave = isQueueEmpty && currentWave > 1
            && currentHealthSum <= (previousWaveTotalMaxHealth * 0.25);

        if (canStartFirstWave || canStartNextWave) {
            spawnWave();
        }
    }

    private void processSpawnQueue(int ticks) {
        if (pendingZombieQueue.isEmpty()) return;

        spawnTimer += ticks * Constants.Game.TIME_COEFFICIENT;
        double delay = ConfigManager.gameplay().spawnDelaySeconds;
        if (delay <= 0) delay = 10.0;

        while (!pendingZombieQueue.isEmpty() && spawnTimer >= delay) {
            spawnTimer -= (float) delay;
            ZombieType nextType = pendingZombieQueue.poll();
            if (nextType != null) {
                spawnSingleZombie(nextType);
            }
        }
    }

    public void spawnWave() {
        if (type == WaveType.NO_WAVES) return;

        boolean finalWave = isLastWave();
        String message;

        if (finalWave) {
            message = "The final wave has come.";
            waveBudget *= 2;
        } else if (currentWave > 1) {
            message = "Wave " + currentWave + " started.";
            waveBudget = (int) Math.floor(waveBudget * 1.25f);
        } else {
            message = "Wave 1 started.";
            if (AppModel.gameSession != null) {
                AppModel.gameSession.flagForFirstWave = true;
            }
        }

        notifyWaveStart(currentWave, waveNumber, finalWave, message);
        previousWaveTotalMaxHealth = 0;

        // 1. Run wave-wide environmental / tomb setups ONCE
        type.spawnWave(this, gameBoard, List.of());

        // 2. Generate zombie types for this wave's budget
        List<ZombieType> zombieTypes = generateZombiesForBudget();

        // 3. Queue the types for delayed spawning
        pendingZombieQueue.addAll(zombieTypes);

        // 4. Immediately spawn the lead zombie of the wave
        if (!pendingZombieQueue.isEmpty()) {
            spawnSingleZombie(pendingZombieQueue.poll());
        }
        spawnTimer = 0f;

        isSpawningFinalWave = finalWave;
        currentWave++;
    }

    public void spawnSingleZombie(ZombieType zombieType) {
        if (gameBoard == null || gameBoard.lanes == null || gameBoard.lanes.isEmpty()) return;

        // 1. Dark Ages: Necromancy (Spawns zombie from tomb with notification)
        if (type == WaveType.DARK_AGES) {
            ArrayList<Tomb> activeTombs = gameBoard.getAllTombs();
            if (activeTombs != null && !activeTombs.isEmpty() && random.nextBoolean()) {
                Tomb selectedTomb = activeTombs.get(random.nextInt(activeTombs.size()));
                selectedTomb.spawnZombie(gameBoard, zombieType);

                ToastManager.showMessage("Necromancy! A " + zombieType + " rose from a tomb!");
                return;
            }
        }

        // 2. Big Wave Beach: Spawn on flooded SHALLOW_BEACH tiles
        if (type == WaveType.BIG_WAVE_BEACH && random.nextBoolean()) {
            List<Tile> floodedShallowTiles = new ArrayList<>();
            for (Lane lane : gameBoard.lanes) {
                for (Tile tile : lane.tiles) {
                    if (tile.type == TileType.SHALLOW_BEACH && tile.isUnderWater()) {
                        floodedShallowTiles.add(tile);
                    }
                }
            }
            if (!floodedShallowTiles.isEmpty()) {
                Tile targetTile = floodedShallowTiles.get(random.nextInt(floodedShallowTiles.size()));
                double x = (targetTile.column + 0.5f) * Constants.Game.TILE_WIDTH + Constants.Game.PADDING_X;
                double y = targetTile.row * Constants.Game.TILE_HEIGHT + Constants.UI.BOTTOM_LINE_METER;

                Zombie zombie = ZombieBuilder.create(zombieType, x, y, targetTile.row);
                gameBoard.lanes.get(targetTile.row).zombies.add(zombie);

                // Show notification for underwater beach spawn
                ToastManager.showMessage("A " + zombieType + " emerged from the water!");
                return;
            }
        }

        // 3. Ancient Egypt Final Wave: Tornado offset (spawns in one of the 4 right-most columns)
        int randomLaneIndex = random.nextInt(gameBoard.lanes.size());
        double x = 18f;
        if (type == WaveType.ANCIENT_EGYPT && isSpawningFinalWave) {
            x = (gameBoard.totalCols - 0.5f - random.nextInt(2, 5))
                * Constants.Game.TILE_WIDTH + Constants.Game.PADDING_X;
        }

        double y = (randomLaneIndex) * Constants.Game.TILE_HEIGHT + Constants.UI.BOTTOM_LINE_METER;

        Zombie zombie = ZombieBuilder.create(zombieType, x, y, randomLaneIndex);
        gameBoard.lanes.get(randomLaneIndex).zombies.add(zombie);
    }

    private void notifyWaveStart(int wave, int total, boolean isFinal, String message) {
        if (listener != null) {
            listener.onWaveStarted(wave, total, isFinal, message);
        } else {
            AppModel.addAfterPrompt(message);
        }
    }

    private List<ZombieType> generateZombiesForBudget() {
        List<ZombieType> zombiesForWave = new ArrayList<>();
        double spent = 0;

        int consecutiveFails = 0;
        while (spent < waveBudget && consecutiveFails < 64) {
            ZombieType zombieType = getRandomZombieType();
            int cost = zombieType.waveCost;

            if (spent + cost <= waveBudget) {
                zombiesForWave.add(zombieType);
                spent += cost;
                previousWaveTotalMaxHealth += ZombieBuilder.create(zombieType, 0, 0, 0).getHealth();
                consecutiveFails = 0;
            } else {
                consecutiveFails++;
            }
        }

        return zombiesForWave;
    }

    private ZombieType getRandomZombieType() {
        List<ZombieType> validTypes = new ArrayList<>();

        for (ZombieType zombieType : ZombieType.values()) {
            if (zombieType.name().contains("ZOMBOSS")) {
                continue;
            }

            if (zombieType.chapter == null) {
                validTypes.add(zombieType);
            } else if (this.type == WaveType.ANCIENT_EGYPT && zombieType.chapter == ChapterType.ANCIENT_EGYPT) {
                validTypes.add(zombieType);
            } else if (this.type == WaveType.DARK_AGES && zombieType.chapter == ChapterType.DARK_AGES) {
                validTypes.add(zombieType);
            } else if (this.type == WaveType.BIG_WAVE_BEACH && zombieType.chapter == ChapterType.BIG_WAVE_BEACH) {
                validTypes.add(zombieType);
            } else if (this.type == WaveType.FROSTBITE_CAVE && zombieType.chapter == ChapterType.FROSTBITE_CAVES) {
                validTypes.add(zombieType);
            }
        }

        return validTypes.get(random.nextInt(validTypes.size()));
    }

    public boolean isLastWave() {
        return currentWave == waveNumber;
    }
}
