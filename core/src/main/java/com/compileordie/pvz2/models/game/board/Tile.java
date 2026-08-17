package com.compileordie.pvz2.models.game.board;

import com.compileordie.pvz2.models.entities.obstacles.Obstacle;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.Tomb;

import java.util.ArrayList;
import java.util.List;

public class Tile {
    public GameBoard gameBoard;
    public int row;
    public int column;
    public TileType type;
    public Plant plant;
    public Obstacle obstacle;
    public Tomb tomb; // Redundant
    public boolean hasLilyPad;

    // NEW: Puddle memory
    public double puddleTimer = 0;
    public int puddleDamage = 0;

    public Tile(GameBoard gameBoard, int row, int column, TileType type, Plant plant, Obstacle obstacle) {
        this.gameBoard = gameBoard;
        this.row = row;
        this.column = column;
        this.type = type;
        this.plant = plant;
        this.obstacle = obstacle;
        this.tomb = null;
        this.hasLilyPad = false;
    }

    public void tick(int ticks) {
        type.tick(ticks, this, gameBoard);
        if (plant != null) plant.tick(gameBoard, ticks);
        if (obstacle != null) obstacle.tick(ticks, gameBoard);

        // NEW: Process the puddle fading away!
        tickPuddle(ticks);
    }

    // NEW: The tick logic for the puddle
    public void tickPuddle(double delta) {
        if (puddleTimer > 0) {
            puddleTimer -= delta;
            if (puddleTimer < 0) puddleTimer = 0; // Clean up when the 10 seconds are over
        }
    }

    public boolean isEmpty() {
        return plant == null && !hasLilyPad && obstacle == null && tomb == null;
    }

    public boolean isPlantable() {
        return type.isPlantable && plant == null && obstacle == null
            && (!isUnderWater() || (isUnderWater() && hasLilyPad));
    }

    public boolean isUnderWater() {
        return gameBoard.totalCols - gameBoard.tideLevel <= this.column;
    }

    public List<Zombie> getZombies() {
        List<Zombie> zombies = new ArrayList<>();
        for (Zombie zombie : gameBoard.getAllZombies()) {
            if (gameBoard.getTile((float) zombie.getX(), (float) zombie.getY()) == this) zombies.add(zombie);
        }
        return zombies;
    }
}
