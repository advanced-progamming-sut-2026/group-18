package com.compileordie.pvz2.models.game.waves;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.zombies.ZombieBuilder;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.levels.ChapterType;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WaveManager {
    public GameBoard gameBoard;
    public WaveType type;
    public int waveNumber;
    public int currentWave;
    public int waveBudget;
    public double previousWaveTotalMaxHealth;
    public int tickCounter;

    public WaveManager(GameBoard gameBoard, WaveType type, int waveNumber) {
        this.gameBoard = gameBoard;
        this.type = type;
        this.waveNumber = waveNumber;
        this.currentWave = 1;
        this.waveBudget = (int) Math.floor(ConfigManager.gameplay().basicWaveBudget * AppModel.player.getDLIncrease());
        this.previousWaveTotalMaxHealth = 0;
        this.tickCounter = 0;
    }

    public void tick(int ticks) {
        if (waveNumber != -1 && currentWave > waveNumber) {
            tickCounter += ticks;
            return;
        }

        double currentHealthSum = gameBoard.getAllZombies()
            .stream()
            .filter(Zombie::isAlive)
            .mapToDouble(Zombie::getHealth)
            .sum();

        // Trigger wave 1 instantly, or next wave when 75% of previous health is depleted
        if (currentWave == 1 || currentHealthSum <= (previousWaveTotalMaxHealth * 0.25)) {
            spawnWave();
        }

        tickCounter += ticks;
    }

    public void spawnWave() {
        if (type == WaveType.NO_WAVES) return;

        // 1. Calculate Multipliers Before Purchasing
        if (isLatWave()) {
            AppModel.addAfterPrompt("The final wave has come.");
            waveBudget *= 2; // Final wave is 2x previous wave difficulty
        } else if (currentWave > 1) {
            AppModel.addAfterPrompt("Wave " + currentWave + " started.");
            waveBudget = (int) Math.floor(waveBudget * 1.25f); // Standard waves scale by 25%
        } else {
            AppModel.addAfterPrompt("Wave 1 started.");
        }

        previousWaveTotalMaxHealth = 0;

        // 2. Generate Zombies for the calculated budget
        List<ZombieType> pendingZombies = generateZombiesForBudget();

        // 3. Delegate placement to the WaveType
        type.spawnWave(this, gameBoard, pendingZombies);

        currentWave++;
    }

    private List<ZombieType> generateZombiesForBudget() {
        List<ZombieType> zombiesForWave = new ArrayList<>();
        double spent = 0;

        int consecutiveFails = 0;

        // Buy zombies until budget is filled (limit consecutive fails to prevent infinite loops)
        while (spent < waveBudget && consecutiveFails < 64) {
            // Get a random zombie that is valid for the current map
            ZombieType zombieType = getRandomZombieType();

            int cost = zombieType.waveCost;

            if (spent + cost <= waveBudget) {
                zombiesForWave.add(zombieType);
                spent += cost;
                previousWaveTotalMaxHealth += ZombieBuilder.create(zombieType, 0, 0, 0).getHealth();
                consecutiveFails = 0; // Reset fails on successful purchase
            } else {
                consecutiveFails++;
            }
        }

        return zombiesForWave;
    }

    /**
     * Filters the ZombieType enum to return a valid random zombie
     * based on the current level's chapter.
     */
    private ZombieType getRandomZombieType() {
        List<ZombieType> validTypes = new ArrayList<>();

        for (ZombieType zombieType : ZombieType.values()) {
            // Generic zombies are always added
            if (zombieType.chapter == null) {
                validTypes.add(zombieType);
            }
            // Chapter specific zombies are added only if they match the current WaveType
            else if (this.type == WaveType.ANCIENT_EGYPT && zombieType.chapter == ChapterType.ANCIENT_EGYPT) {
                validTypes.add(zombieType);
            }
            else if (this.type == WaveType.DARK_AGES && zombieType.chapter == ChapterType.DARK_AGES) {
                validTypes.add(zombieType);
            }
            else if (this.type == WaveType.BIG_WAVE_BEACH && zombieType.chapter == ChapterType.BIG_WAVE_BEACH) {
                validTypes.add(zombieType);
            }
            else if (this.type == WaveType.FROSTBITE_CAVE && zombieType.chapter == ChapterType.FROSTBITE_CAVES) {
                validTypes.add(zombieType);
            }
        }

        Random random = new Random();
        return validTypes.get(random.nextInt(validTypes.size()));
    }

    public boolean isLatWave() {
        return currentWave == waveNumber;
    }
}
