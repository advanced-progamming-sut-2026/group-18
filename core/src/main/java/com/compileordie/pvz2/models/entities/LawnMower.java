package com.compileordie.pvz2.models.entities;

import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Lane;

public class LawnMower {
    public float x;
    public Lane lane;
    public boolean isActivated;

    public LawnMower(Lane lane) {
        this.lane = lane;
        this.isActivated = true;
    }

    public void tick(int ticks, GameBoard gameBoard) {
        // TODO: To be implemented.
    }
}
