package com.compileordie.pvz2.models.game.board;

import com.compileordie.pvz2.config.Constants; // Added to support your teammate's Math.floor calculation
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.economy.EconomyManager;
import com.compileordie.pvz2.models.user.Player; // Arsam
import java.util.ArrayList;
import java.util.stream.Collectors;

public class GameBoard {
    // Arsam : need Player for food effect
    private Player player;
    public int totalRows;
    public int totalCols;
    public ArrayList<Lane> lanes;
    // TODO: Add a reference to zombie manager.
    public ArrayList<Projectile> projectiles;
    public EconomyManager economyManager;

    public GameBoard(int totalRows, int totalCols, Player player) {
        this.player = player; // Arsam
        this.totalRows = totalRows;
        this.totalCols = totalCols;
        this.lanes = new ArrayList<>();
        for (int i = 0; i < totalRows; i++) {
            lanes.add(new Lane(i, totalCols));
        }
        // BUG FIXED: The list is now safely initialized in memory!
        this.projectiles = new ArrayList<>();
    }

    // Arsam
    public Player getPlayer() {
        return player;
    }

    public Lane getLane(int index) {
        return lanes.get(index);
    }

    // NEW OVERLOADED METHOD FROM TEAMMATE
    public Lane getLane(float y) {
        return getLane((int) Math.floor(y / Constants.Game.TILE_SIZE));
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

    // ADDED: The missing getter so our strategies can spawn bullets perfectly
    public ArrayList<Projectile> getActiveProjectiles() {
        return projectiles;
    }
}
