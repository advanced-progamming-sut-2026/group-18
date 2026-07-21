package com.compileordie.pvz2.models.game;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.economy.EconomyType;
import com.compileordie.pvz2.models.game.judges.GameFlow;
import com.compileordie.pvz2.models.game.judges.GameJudge;
import com.compileordie.pvz2.models.game.judges.LossCondition;
import com.compileordie.pvz2.models.game.judges.WinCondition;
import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.models.game.waves.WaveType;
import com.compileordie.pvz2.models.user.Player;

import java.util.ArrayList;

public class GameSession {
    public LevelID levelID;
    public Player player;
    public GameBoard gameBoard;
    public GameJudge gameJudge;

    public GameSession(LevelID levelID,
                       EconomyType economyType,
                       WaveType waveType,
                       WinCondition winCondition,
                       LossCondition lossCondition,
                       ArrayList<PlantType> selectionDeck) {
        this.levelID = levelID;
        this.player = AppModel.player;
        this.gameBoard = new GameBoard(Constants.Game.BOARD_ROWS,
            Constants.Game.BOARD_COLS,
            economyType,
            waveType,
            selectionDeck,
            levelID.waveNumber);
        this.gameJudge = new GameJudge(gameBoard, winCondition, lossCondition);
    }

    public void tick(int ticks) {
        player.playtime += ticks * Constants.Game.TIME_COEFFICIENT;
        gameBoard.tick(ticks);

        GameFlow judgeResult = gameJudge.judge();
        if (judgeResult == GameFlow.WIN || judgeResult == GameFlow.LOSS) {
            afterSession(judgeResult);
        }
    }

    public void afterSession(GameFlow gameResult) {
        // TODO: To be implemented.
    }
}
