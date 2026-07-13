package com.compileordie.pvz2.models.game.board;

import com.compileordie.pvz2.models.entities.LawnMower;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

import java.util.ArrayList;

public class Lane {
    public int index;
    public int tileCount;
    public ArrayList<Zombie> zombies;
    public ArrayList<Tile> tiles;
    public LawnMower lawnMower;

    public Lane(int index, int tileCount) {
        this.index = index;
        this.zombies = new ArrayList<>();
        this.tiles = new ArrayList<>();
        this.tileCount = tileCount;
        for (int i = 0; i < tileCount; i++) {
            tiles.add(new Tile(i, TileType.UNINITIALIZED, null, null));
        }
    }

    public void tick(int ticks, GameBoard gameBoard) {
        for (Tile tile : tiles) {
            tile.tick(ticks, gameBoard);
        }
        lawnMower.tick(ticks, gameBoard);
    }
}
