package com.compileordie.pvz2.models.game;

public interface GameEventListener {
    void onWaveStarted(int waveIndex, int totalWaves, boolean isFinalWave, String message);
}
