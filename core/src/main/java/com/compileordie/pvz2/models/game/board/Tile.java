package com.compileordie.pvz2.models.game.board;

import com.compileordie.pvz2.models.entities.obstacles.Obstacle;
import com.compileordie.pvz2.models.entities.plants.variants.Plant;

public class Tile {
    public int row;
    public int column;
    public TileType type;
    public Plant plant;
    public Obstacle obstacle;

    public Tile(int row, int column, TileType type, Plant plant, Obstacle obstacle) {
        this.row = row;
        this.column = column;
        this.type = type;
        this.plant = plant;
        this.obstacle = obstacle;
    }

    public void tick(int ticks, GameBoard gameBoard) {
        type.tick(ticks, this, gameBoard);
        if (plant != null) plant.tick(ticks, gameBoard);
        if (obstacle != null) obstacle.tick(ticks, gameBoard);
    }

    public boolean isPlantable() {
        return type.isPlantable && plant == null && obstacle == null;
    }
}
