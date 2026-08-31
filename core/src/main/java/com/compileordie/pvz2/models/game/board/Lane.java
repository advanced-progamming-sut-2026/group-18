package com.compileordie.pvz2.models.game.board;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.LawnMower;
import com.compileordie.pvz2.models.entities.obstacles.ObstacleType;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.Tomb;

import java.util.ArrayList;
import java.util.List;
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
        this.lawnMower = new LawnMower(this);
    }

    public void tick(int ticks) {
        for (Tile tile : tiles) {
            tile.tick(ticks);
        }
        lawnMower.tick(ticks);

        for (int i = zombies.size() - 1; i >= 0; i--) {
            if (!zombies.get(i).isAlive()) zombies.remove(i);
        }
    }

    public ArrayList<Plant> getAllPlants() {
        return tiles.stream()
            .filter(tile -> tile.plant != null)
            .map(tile -> tile.plant)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public float getLength() {
        return tileCount * Constants.Game.TILE_WIDTH;
    }

    public List<Tomb>  getAllTombs() {
        return tiles.stream()
            .filter(tile -> tile.obstacle != null && tile.obstacle.type == ObstacleType.TOMB)
            .map(tile -> (Tomb) tile.obstacle)
            .collect(Collectors.toCollection(ArrayList::new));
    }
}
