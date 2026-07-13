package com.compileordie.pvz2.models.game.board;

import com.compileordie.pvz2.models.entities.obstacles.Obstacle;
import com.compileordie.pvz2.models.entities.plants.variants.Plant;

public class Tile {
    public int index;
    public TileType type;
    public Plant plant;
    public Obstacle obstacle;

    public Tile(int index, TileType type, Plant plant, Obstacle obstacle) {
        this.index = index;
        this.type = type;
        this.plant = plant;
        this.obstacle = obstacle;
    }

    public void tick(int ticks, GameBoard gameBoard) {
        type.tick(ticks, gameBoard);
        if (plant != null) plant.tick(ticks, gameBoard);
        if (obstacle != null) obstacle.tick(ticks, gameBoard);
    }

    public boolean isPlantable() {
        return type.isPlantable && plant == null && obstacle == null;
    }
}
