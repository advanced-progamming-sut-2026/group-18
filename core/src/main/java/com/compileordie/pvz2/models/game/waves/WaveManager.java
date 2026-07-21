package com.compileordie.pvz2.models.game.waves;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;

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

        if (currentWave == 1 || currentHealthSum <= (previousWaveTotalMaxHealth * 0.25)) {
            spawnWave();
        }

        tickCounter += ticks;
    }

    public void spawnWave() {
        // TODO: Do common necessary things before and after type.spawnWave
        type.spawnWave(this, gameBoard);
        // TODO: Do common necessary things before and after type.spawnWave
    }
}
