package com.compileordie.pvz2.models.game.effects;

import com.compileordie.pvz2.models.game.board.GameBoard;

public class EffectModifier {
    public GameBoard gameBoard;
    public EffectType type;
    public int tickCounter;

    public EffectModifier(GameBoard gameBoard, EffectType type) {
        this.gameBoard = gameBoard;
        this.type = type;
        this.tickCounter = 0;
    }

    public void tick(int ticks) {
        type.tick(ticks, this, gameBoard);

        tickCounter += ticks;
    }
}
