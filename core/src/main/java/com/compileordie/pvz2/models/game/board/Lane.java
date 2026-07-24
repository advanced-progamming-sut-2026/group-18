package com.compileordie.pvz2.models.game.board;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.LawnMower;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Lane {
    public GameBoard gameBoard;
    public int row;
    public int tileCount;
    public ArrayList<Zombie> zombies;
    public ArrayList<Tile> tiles;
    public LawnMower lawnMower;
    public boolean isLost;

    public Lane(GameBoard gameBoard, int row, int tileCount) {
        this.gameBoard = gameBoard;
        this.row = row;
        this.zombies = new ArrayList<>();
        this.tiles = new ArrayList<>();
        this.tileCount = tileCount;
        for (int i = 0; i < tileCount; i++) {
            tiles.add(new Tile(gameBoard, row, i, TileType.UNINITIALIZED, null, null));
        }
        this.isLost = false;
    }

    public void tick(int ticks) {
        for (Tile tile : tiles) {
            tile.tick(ticks);
        }
        lawnMower.tick(ticks);
    }

    public ArrayList<Plant> getAllPlants() {
        return tiles.stream()
            .map(tile -> tile.plant)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public float getLength() {
        return tileCount * Constants.Game.TILE_SIZE;
    }
}
