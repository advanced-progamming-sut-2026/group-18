package com.compileordie.pvz2.models.game.board;

import com.compileordie.pvz2.models.entities.plants.variants.Plant;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.economy.EconomyManager;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class GameBoard {
    public int totalRows;
    public int totalCols;
    public ArrayList<Lane> lanes;
    // TODO: Add a reference to zombie manager.
    public ArrayList<Projectile> projectiles;
    public EconomyManager economyManager;

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
        // TODO: Tick zombie manager here.
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            projectile.tick(ticks, this);
        }

        economyManager.tick(ticks);
    }

    public Tile getTile(int row, int column) {
        return lanes.get(row).tiles.get(column);
    }

    public Tile getTile(float x, float y) {
        return getTile((int) Math.floor(x), (int) Math.floor(y));
    }

    public ArrayList<Zombie> getAllZombies() {
        return lanes.stream()
            .flatMap(lane -> lane.zombies.stream())
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Plant> getAllPlants() {
        return lanes.stream()
            .flatMap(lane -> lane.geAllPlants().stream())
            .collect(Collectors.toCollection(ArrayList::new));
    }
}
