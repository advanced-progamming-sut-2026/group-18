package com.compileordie.pvz2.models.game.judges;

import com.compileordie.pvz2.models.game.board.GameBoard;

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
        // TODO: To be implemented.
        return null;
    }
}
