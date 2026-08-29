package com.compileordie.pvz2.models.game.economy;

import com.badlogic.gdx.math.MathUtils;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.game.board.GameBoard;

import java.util.ArrayList;
import java.util.Map;

public class EconomyManager {
    public GameBoard gameBoard;
    public EconomyType type;
    public ArrayList<Sun> suns;
    public Map<PlantType, Boolean> selectionDeck;
    public ArrayList<PlantCard> plantCards;
    public int sunAmount;
    public int totalSunsGenerated;
    public int ticksUntilNextNaturalSun;
    public int tickCounter;

    public EconomyManager(GameBoard gameBoard, EconomyType economyType, Map<PlantType, Boolean> selectionDeck) {
        this.gameBoard = gameBoard;
        this.type = economyType;
        this.suns = new ArrayList<>();
        this.selectionDeck = selectionDeck;
        this.plantCards = new ArrayList<>();
        // TODO: Reset back to 0
        this.sunAmount = 1000;
        this.totalSunsGenerated = 0;
        this.tickCounter = 0;
        this.ticksUntilNextNaturalSun = calculateNextSpawnIntervalTicks();
    }

    public void tick(int ticks) {
        // Handled polymorphically by EconomyType enum (STANDARD handles sky suns)
        type.tick(ticks, this, gameBoard);

        // Safely process active entity updates moving backwards
        for (int i = suns.size() - 1; i >= 0; i--) {
            Sun sun = suns.get(i);
            sun.tick(ticks, gameBoard);

            if (!sun.isAlive()) {
                suns.remove(i);
            }
        }

        for (int t = plantCards.size() - 1; t >= 0; t--) {
            PlantCard plantCard = plantCards.get(t);
            plantCard.tick(ticks);
        }

        tickCounter += ticks;
    }

    public int calculateNextSpawnIntervalTicks() {
        float time = this.tickCounter * Constants.Game.TIME_COEFFICIENT;

        // Interval scales from 6s up to 12s cap over time
        float secondsInterval = Math.min(6 + 0.05f * time, 12f);

        // Apply player Difficulty Level modifier (higher DL -> longer interval)
        if (gameBoard != null && AppModel.player != null) {
            secondsInterval *= AppModel.player.getDLIncrease();
        }

        return (int) Math.floor(secondsInterval / Constants.Game.TIME_COEFFICIENT);
    }

    public void spawnNaturalSun() {
        // Choose a completely randomized horizontal grid column location
        float y = MathUtils.random(3 * Constants.Game.TILE_HEIGHT + 2,
            5 * Constants.Game.TILE_HEIGHT + 2) + Constants.Game.PADDING_Y;
        // 🌞 مثل هر مختصات دیگه‌ای که وارد GameBoard می‌شه (زامبی‌ها، گیاه‌ها،
        // خورشیدهای تولیدشده توسط گیاه)، اینجا هم باید PADDING_X_REALITY رو
        // به مقدار خام/منطقی (column-based) اضافه کنیم تا خورشید داخل بازه‌ی
        // واقعی تخته (world-space) بیفته، نه تو ناحیه‌ی پدینگ/چپِ صفحه. بدون
        // این، وقتی مثلا RA Zombie (که getX() ش کاملا world-space هست) این
        // خورشید رو هدف می‌گیره، اختلاف مختصات باعث می‌شه خورشید به سمت
        // اشتباهی پرواز کنه.
        float x = Constants.Game.PADDING_X_REALITY + MathUtils.random(2 * Constants.Game.TILE_WIDTH,
            8 * Constants.Game.TILE_WIDTH);
        float ground = MathUtils.random(1,
            Math.max(4, y - 3));
        /*float x = MathUtils.random(Constants.Game.BOARD_COLS / 3f, Constants.Game.BOARD_COLS);
        float y = MathUtils.random(Constants.Game.BOARD_ROWS, Constants.Game.BOARD_ROWS + 1);
        float ground = MathUtils.random(Constants.Game.BOARD_ROWS / 4f);*/

        // Generate probability parameters
        int rollout = MathUtils.random(100);
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
    }
}
