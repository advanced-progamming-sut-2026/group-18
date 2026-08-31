package com.compileordie.pvz2.models.game;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.economy.EconomyType;
import com.compileordie.pvz2.models.game.judges.GameFlow;
import com.compileordie.pvz2.models.game.judges.GameJudge;
import com.compileordie.pvz2.models.game.judges.LossCondition;
import com.compileordie.pvz2.models.game.judges.WinCondition;
import com.compileordie.pvz2.models.game.levels.ChapterType;
import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.models.game.waves.WaveType;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.models.user.Player;

import java.util.Map;

public class GameSession {
    public LevelID levelID;
    public Player player;
    public GameBoard gameBoard;
    public GameJudge gameJudge;


    public boolean flagForFirstWave = false;
    public float elapsedTimeFromFirstWave = 99999f;

    public GameSession(LevelID levelID,
                       EconomyType economyType,
                       WaveType waveType,
                       WinCondition winCondition,
                       LossCondition lossCondition,
                       Map<PlantType, Boolean> selectionDeck) {
        this.levelID = levelID;
        this.player = AppModel.player;
        this.gameBoard = new GameBoard(levelID,
            Constants.Game.BOARD_ROWS,
            Constants.Game.BOARD_COLS,
            economyType,
            waveType,
            selectionDeck,
            levelID.waveNumber,
            levelID != LevelID.PLANT_WHAT_YOU_GET);
        this.gameJudge = new GameJudge(gameBoard, winCondition, lossCondition);
    }

    public void tick(int ticks) {
        for (Zombie zombie : gameBoard.getAllZombies()) {
            ZombieType zombieType = zombie.getType();
            if (!player.unlockedZombies.contains(zombieType)) {
                player.unlockedZombies.add(zombieType);
            }
        }

        //---
        if (AppModel.gameSession.flagForFirstWave){
            if (Math.abs(elapsedTimeFromFirstWave-99999)<=0.01){
                elapsedTimeFromFirstWave = 0;
            }
            elapsedTimeFromFirstWave += ticks*Constants.Game.TIME_COEFFICIENT;

        }
        //---
        player.playtime += ticks * Constants.Game.TIME_COEFFICIENT;
        gameBoard.tick(ticks);

        GameFlow judgeResult = gameJudge.judge();
        if (judgeResult == GameFlow.WIN || judgeResult == GameFlow.LOSS) {
            afterSession(judgeResult);
        }
    }

    public void afterSession(GameFlow gameResult) {
        switch (gameResult) {
            case WIN -> {
                AppModel.wonLastGame = true;

                LevelID nextLevelID = levelID.next();
                if (nextLevelID.chapterType != ChapterType.MINIGAME && !player.unlockedLevelIDs.contains(nextLevelID)) {
                    player.unlockedLevelIDs.add(nextLevelID);
                }

                player.playedGames++;
                new UserDatabase().save(player);
            }
            case LOSS -> {
                AppModel.wonLastGame = false;
            }
        }
        AppModel.gameSession = null;
    }
}
