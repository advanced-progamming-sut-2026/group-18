package com.compileordie.pvz2.models.game.board;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.obstacles.Obstacle;
import com.compileordie.pvz2.models.entities.obstacles.ObstacleType;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.Tomb;
import com.compileordie.pvz2.models.game.minigames.vasebreaker.Vase;

import java.util.ArrayList;
import java.util.List;

public class Tile {
    public GameBoard gameBoard;
    public int row;
    public int column;
    public TileType type;

    // --- THE NEW STACKING ARCHITECTURE ---
    public Plant lilyPad;      // Bottom layer (Water)
    public Plant plant;        // Middle layer (Main Plant)
    public Plant pumpkin;      // Top layer (Armor)
    public Plant instantPlant; // Overlay layer (Hot Potato / Grave Buster)

    public Obstacle obstacle;
    public Vase vase;
    public boolean isOnFire;
    public double fireTime = 4;
    public double fireTimer = 0;
    public double puddleTimer = 0;
    public int puddleDamage = 0;

    public Tile(GameBoard gameBoard, int row, int column, TileType type, Plant plant, Obstacle obstacle) {
        this.gameBoard = gameBoard;
        this.row = row;
        this.column = column;
        this.type = type;
        this.plant = plant;
        this.obstacle = obstacle;
        this.isOnFire = false;
    }

    public void tick(int ticks) {
        type.tick(ticks, this, gameBoard);

        // Tick all active slots!
        if (lilyPad != null) lilyPad.tick(gameBoard, ticks);
        if (plant != null) plant.tick(gameBoard, ticks);
        if (pumpkin != null) pumpkin.tick(gameBoard, ticks);
        if (instantPlant != null) instantPlant.tick(gameBoard, ticks);
        if (obstacle != null) obstacle.tick(ticks, gameBoard);

        // Clear dead entities
        if (lilyPad != null && lilyPad.isDead()) lilyPad = null;
        if (plant != null && plant.isDead()) plant = null;
        if (pumpkin != null && pumpkin.isDead()) pumpkin = null;
        if (instantPlant != null && instantPlant.isDead()) instantPlant = null;
        if (obstacle != null && !obstacle.isAlive()) obstacle = null;

        tickPuddle(ticks);

        if (isOnFire){
            if (plant != null) plant.die();
            if (pumpkin != null) pumpkin.die();
            if (lilyPad != null) lilyPad.die();
            this.plant = null;
            this.pumpkin = null;
            this.lilyPad = null;

            fireTimer += ticks * Constants.Game.TIME_COEFFICIENT;
            if (fireTimer >= fireTime){
                fireTimer = 0;
                isOnFire = false;
            }
        }
    }

    public void tickPuddle(double delta) {
        if (puddleTimer > 0) {
            puddleTimer -= delta;
            if (puddleTimer <= 0) {
                puddleTimer = 0;
                puddleDamage = 0;
            }
        }
    }

    public Tomb getTomb() {
        if (obstacle != null && obstacle.type == ObstacleType.TOMB) {
            return (Tomb) obstacle;
        }
        return null;
    }

    public void setOnFire() {
        this.isOnFire = true;
    }

    public boolean isEmpty() {
        return plant == null && lilyPad == null && pumpkin == null
            && instantPlant == null && obstacle == null && getTomb() == null;
    }

    public boolean isPlantable() {
        return type.isPlantable && plant == null && obstacle == null && !isOnFire
            && (!isUnderWater() || (isUnderWater() && hasLilyPad()));
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

    public boolean hasLilyPad() {
        return lilyPad != null;
    }

    public boolean hasPumpkin() {
        return pumpkin != null;
    }
}
