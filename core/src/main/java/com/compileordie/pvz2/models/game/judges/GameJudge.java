package com.compileordie.pvz2.models.game.judges;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Lane;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;

public class GameJudge {
    public GameBoard gameBoard;
    public WinCondition winCondition;
    public LossCondition lossCondition;

    public GameJudge(GameBoard gameBoard, WinCondition winCondition, LossCondition lossCondition) {
        this.gameBoard = gameBoard;
        this.winCondition = winCondition;
        this.lossCondition = lossCondition;
    }

    public GameFlow judge() {
        float deadline = lossCondition == LossCondition.DEAD_LINE ?
            ConfigManager.gameplay().deadlineShift - 0.1f : -0.1f;

        for (Lane lane : gameBoard.lanes) {
            for (Zombie zombie : lane.zombies) {
                if (zombie.getX() < deadline) {
                    lane.isLost = true;
                    break;
                }
            }
        }

        if (winCondition.evaluate(gameBoard)) {
            AppModel.addAfterPrompt("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
            return GameFlow.WIN;
        } else if (lossCondition.evaluate(gameBoard)) {
            AppModel.addAfterPrompt("The zombie ate your brain; LOSER!!!");
            return GameFlow.LOSS;
        } else {
            return GameFlow.CONTINUE;
        }
    }
}
