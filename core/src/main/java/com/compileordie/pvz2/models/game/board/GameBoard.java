package com.compileordie.pvz2.models.game.board;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.obstacles.ObstacleType;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.entities.zombies.services.manager.ZombieManager;
import com.compileordie.pvz2.models.entities.zombies.services.manager.ZombieTickContext;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.Tomb;
import com.compileordie.pvz2.models.game.economy.EconomyManager;
import com.compileordie.pvz2.models.game.economy.EconomyType;
import com.compileordie.pvz2.models.game.minigames.vasebreaker.SeedPacket;
import com.compileordie.pvz2.models.game.minigames.vasebreaker.Vase;
import com.compileordie.pvz2.models.game.waves.WaveManager;
import com.compileordie.pvz2.models.game.waves.WaveType;
import com.compileordie.pvz2.models.user.Player; // Arsam

import java.util.ArrayList;
import java.util.stream.Collectors;

public class GameBoard {
    public int totalRows;
    public int totalCols;
    public ArrayList<Lane> lanes;
    public ZombieManager zombieManager;
    public ArrayList<Projectile> projectiles;
    public EconomyManager economyManager;
    public WaveManager waveManager;
    public ArrayList<Vase> vases;
    public ArrayList<SeedPacket> seedPackets;
    public int tideLevel;
    public int maxTideLevel;
    public boolean specialIsLost;
    public int lostPlants;
    public int tickCounter;

    public GameBoard(int totalRows,
                     int totalCols,
                     EconomyType economyType,
                     WaveType waveType,
                     ArrayList<PlantType> selectionDeck,
                     int waveNumber,
                     int maxTideLevel) {
        this.totalRows = totalRows;
        this.totalCols = totalCols;
        this.lanes = new ArrayList<>();
        for (int i = 0; i < totalRows; i++) {
            lanes.add(new Lane(this, i, totalCols));
        }
        this.zombieManager = new ZombieManager();
        this.projectiles = new ArrayList<>();
        this.economyManager = new EconomyManager(this, economyType, selectionDeck);
        this.vases = new ArrayList<>();
        this.seedPackets = new ArrayList<>();
        this.waveManager = new WaveManager(this, waveType, waveNumber);
        this.tideLevel = 0;
        this.maxTideLevel = maxTideLevel;
        this.specialIsLost = false;
        this.lostPlants = 0;
        this.tickCounter = 0;
    }

    // Arsam
    public Player getPlayer() {
        return AppModel.player;
    }

    public Lane getLane(int index) {
        return lanes.get(index);
    }

    public Lane getLane(float y) {
        return getLane((int) Math.floor(y / Constants.Game.TILE_SIZE));
    }

    public void tick(int ticks) {
        for (Lane lane : lanes) {
            lane.tick(ticks);
        }
        // TODO: Tick zombie manager here.
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            projectile.tick(this, ticks);
        }

        zombieManager.tick(new ZombieTickContext(ticks, this));
        economyManager.tick(ticks);
        waveManager.tick(ticks);
        tickCounter += ticks;
    }

    public Tile getTile(int row, int column) {
        return lanes.get(row).tiles.get(column);
    }

    public Tile getTile(float x, float y) {
        return getTile((int) (y / Constants.Game.TILE_SIZE), (int) (x / Constants.Game.TILE_SIZE));
    }

    public ArrayList<Tile> getAllTiles() {
        return lanes.stream()
            .flatMap(lane -> lane.tiles.stream())
            .collect(Collectors.toCollection(ArrayList::new));
    }

    // Returns a list of all tiles that currently have no plant, obstacle, or tomb
    public ArrayList<Tile> getEmptyTiles() {
        return getAllTiles().stream()
            .filter(Tile::isEmpty)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    // Safely places a new plant onto the board based on its X and Y coordinates
    public void addPlant(Plant plant) {
        Tile tile = getTile((float) plant.getX(), (float) plant.getY());
        if (tile != null) {
            tile.plant = plant;
        }
    }


    public ArrayList<Zombie> getAllZombies() {
        return lanes.stream()
            .flatMap(lane -> lane.zombies.stream())
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Plant> getAllPlants() {
        return lanes.stream()
            .flatMap(lane -> lane.getAllPlants().stream())
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Tomb> getAllTombs() {
        return getAllTiles().stream()
            .filter(tile -> tile.obstacle != null && tile.obstacle.type == ObstacleType.TOMB)
            .map(tile -> (Tomb) tile.obstacle)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Projectile> getActiveProjectiles() {
        return projectiles;
    }
}
