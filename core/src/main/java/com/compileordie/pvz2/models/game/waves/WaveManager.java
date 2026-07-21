package com.compileordie.pvz2.models.game.waves;

import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;

public class WaveManager {
    public GameBoard gameBoard;
    public WaveType waveType;
    public int waveNumber;
    public int currentWave;
    public int waveBudget;
    public int tickCounter;

    public WaveManager(GameBoard gameBoard, WaveType waveType, int waveNumber) {
        this.gameBoard = gameBoard;
        this.waveType = waveType;
        this.waveNumber = waveNumber;
        this.currentWave = 1;
        this.waveBudget = ConfigManager.gameplay().basicWaveBudget;
        this.tickCounter = 0;
    }

    public void tick(int ticks) {
        if (waveNumber != -1 && currentWave > waveNumber) {
            tickCounter += ticks;
            return;
        }

        tickCounter += ticks;
    }
}
