package com.compileordie.pvz2.models.game.board;

import com.compileordie.pvz2.models.entities.projectiles.Projectile;

import java.util.ArrayList;

public class GameBoard {
    public int totalRows;
    public int totalCols;
    public ArrayList<Lane> lanes;
    public ArrayList<Projectile> projectiles;
    // TODO: Add a reference to zombie manager and tick it.

    public GameBoard(int totalRows, int totalCols) {
        this.totalRows = totalRows;
        this.totalCols = totalCols;
        this.lanes = new ArrayList<>();
        for (int i = 0; i < totalRows; i++) {
            lanes.add(new Lane(i, totalCols));
        }
    }

    public Lane getLane(int index) {
        return lanes.get(index);
    }

    public void tick(int ticks) {
        for (Lane lane : lanes) {
            lane.tick(ticks, this);
        }
    }
}
