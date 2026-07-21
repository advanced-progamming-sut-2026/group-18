package com.compileordie.pvz2.models.game.waves;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;

import java.util.ArrayList;
import java.util.List;

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
        if (currentWave == waveNumber) {
            System.out.println("The final wave has come.");
            waveBudget *= 2; // Final wave is 2x previous wave difficulty
        } else if (currentWave > 1) {
            System.out.println("Wave " + currentWave + " started.");
            waveBudget = (int) Math.floor(waveBudget * 1.25f); // Standard waves scale by 25%
        } else {
            System.out.println("Wave 1 started.");
        }

        previousWaveTotalMaxHealth = 0;

        // 2. Generate Zombies for the calculated budget
        List<Zombie> pendingZombies = generateZombiesForBudget();

        // 3. Delegate placement and environmental triggers to the WaveType
        type.spawnWave(this, gameBoard, pendingZombies);

        currentWave++;
    }

    private List<Zombie> generateZombiesForBudget() {
        List<Zombie> zombiesForWave = new ArrayList<>();
        double spent = 0;

        int consecutiveFails = 0;

        // Buy zombies until budget is filled (limit consecutive fails to prevent infinite loops)
        while (spent < waveBudget && consecutiveFails < 15) {
            // Assume you have a ZombieType enum and Factory
            ZombieType zombieType = ZombieType.getRandomType();
            int cost = zombieType.waveCost;

            if (spent + cost <= waveBudget) {
                Zombie zombie = ZombieFactory.create(zombieType);

                zombiesForWave.add(zombie);
                spent += cost;
                previousWaveTotalMaxHealth += zombie.getHealth();
                consecutiveFails = 0; // Reset fails on successful purchase
            } else {
                consecutiveFails++;
            }
        }

        return zombiesForWave;
    }
}
