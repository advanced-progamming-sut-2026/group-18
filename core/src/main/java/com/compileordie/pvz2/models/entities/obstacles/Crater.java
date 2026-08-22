package com.compileordie.pvz2.models.entities.obstacles;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Tile;

public class Crater extends Obstacle {
    private double remainingTicks;
    private final int row;
    private final int col;

    public Crater(int row, int col, double durationTicks) {
        super((col + 0.5f) * Constants.Game.TILE_WIDTH, (row + 0.5f) * Constants.Game.TILE_HEIGHT, ObstacleType.CRATER);

        this.row = row;
        this.col = col;
        this.remainingTicks = durationTicks;
    }


    @Override
    public void tick(int ticks, GameBoard gameBoard) {

        if (this.remainingTicks <= 0) return;

        this.remainingTicks -= ticks;

        if (this.remainingTicks <= 0) {
            // Simply remove ourselves from the Tile so the player can plant again!
            Tile tile = gameBoard.getTile(row, col);
            if (tile != null && tile.obstacle == this) {
                tile.obstacle = null;
            }
            this.die();
        }
    }
}
