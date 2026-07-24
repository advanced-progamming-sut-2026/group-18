package com.compileordie.pvz2.models.entities.zombies.services.manager;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.variants.Plant;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.economy.Sun; // خورشیدهای رها شده روی زمین

import java.util.List;

public class ZombieTickContext {
    public int ticks;
    public GameBoard gameBoard;

    public double getDelta() {
        return ticks * Constants.Game.TIME_COEFFICIENT;
    }

    public GameBoard getGameMap() {
        return gameBoard;
    }

    public List<Zombie> getActiveZombies() {
        return gameBoard.getAllZombies();
    }

    public List<Plant> getActivePlants() {
        return gameBoard.getAllPlants();
    }

    public List<Sun> getSunsOnGround() {
        return gameBoard.economyManager.suns;
    }

    public ZombieTickContext(int ticks, GameBoard gameBoard) {
        this.gameBoard = gameBoard;
    }
}
