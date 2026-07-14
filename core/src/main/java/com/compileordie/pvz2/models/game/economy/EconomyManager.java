package com.compileordie.pvz2.models.game.economy;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.game.board.GameBoard;

import java.util.ArrayList;

import static com.badlogic.gdx.math.MathUtils.random;

public class EconomyManager {
    public ArrayList<Sun> suns;
    private int tickCounter;
    private int ticksUntilNextNaturalSun;

    public EconomyManager() {
        this.suns = new ArrayList<>();
        this.tickCounter = 0;
        this.ticksUntilNextNaturalSun = calculateNextSpawnIntervalTicks();
    }

    public void tick(int ticks, GameBoard gameBoard) {
        // Handle structural drop counter countdowns
        ticksUntilNextNaturalSun -= ticks;
        if (ticksUntilNextNaturalSun <= 0) {
            spawnNaturalSun();
            ticksUntilNextNaturalSun = calculateNextSpawnIntervalTicks();
        }

        // Safely process active entity updates moving backwards
        for (int i = suns.size() - 1; i >= 0; i--) {
            Sun sun = suns.get(i);
            sun.tick(ticks, gameBoard);

            if (!sun.isAlive()) {
                suns.remove(i);
            }
        }

        tickCounter += ticks;
    }

    private int calculateNextSpawnIntervalTicks() {
        float time = this.tickCounter * Constants.Game.TIME_COEFFICIENT;
        float secondsInterval = Math.max(6 + 0.05f * time, 12f);
        return (int) Math.floor(secondsInterval / Constants.Game.TIME_COEFFICIENT);
    }

    public void spawnNaturalSun() {
        // Choose a completely randomized horizontal grid column location
        float x = random.nextFloat(Constants.Game.BOARD_COLS / 3f, Constants.Game.BOARD_COLS);
        float y = random.nextFloat(Constants.Game.BOARD_ROWS, Constants.Game.BOARD_ROWS + 1);
        float ground = random.nextFloat(Constants.Game.BOARD_ROWS / 4f);

        // Generate probability parameters
        int rollout = random.nextInt(100);
        SunType selectedType;

        if (rollout < 80) {
            selectedType = SunType.NORMAL; // 80%
        } else if (rollout < 95) {
            selectedType = SunType.SPECIAL; // 15%
        } else {
            selectedType = SunType.RADIOACTIVE; // 5%
        }

        Sun naturalSun = new Sun(x, y, selectedType, true, ground);
        suns.add(naturalSun);

        AppModel.addAfterPrompt("New " + selectedType + " sun is dropping at position (" + x + ", " + y + ")");
    }
}
