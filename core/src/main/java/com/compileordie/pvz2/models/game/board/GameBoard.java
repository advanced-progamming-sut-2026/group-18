package com.compileordie.pvz2.models.game.board;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.obstacles.ObstacleType;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.entities.zombies.services.manager.ZombieManager;
import com.compileordie.pvz2.models.entities.zombies.services.manager.ZombieTickContext;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.Tomb;
import com.compileordie.pvz2.models.game.economy.EconomyManager;
import com.compileordie.pvz2.models.game.economy.EconomyType;
import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.models.game.minigames.vasebreaker.SeedPacket;
import com.compileordie.pvz2.models.game.minigames.vasebreaker.Vase;
import com.compileordie.pvz2.models.game.waves.WaveManager;
import com.compileordie.pvz2.models.game.waves.WaveType;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;
import com.compileordie.pvz2.models.user.Player; // Arsam

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GameBoard {
    public LevelID levelID;
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
    public int registeredShapes;

    public GameBoard(LevelID levelID,
                     int totalRows,
                     int totalCols,
                     EconomyType economyType,
                     WaveType waveType,
                     Map<PlantType, Boolean> selectionDeck,
                     int waveNumber,
                     boolean shouldStartWaves) {
        this.levelID = levelID;
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
        this.waveManager = new WaveManager(this, waveType, waveNumber, shouldStartWaves);
        this.tideLevel = 0;
        this.maxTideLevel = ConfigManager.gameplay().maxTideLevel;
        this.specialIsLost = false;
        this.lostPlants = 0;
        this.tickCounter = 0;
        this.registeredShapes = 0;
    }

    // Arsam
    public Player getPlayer() {
        return AppModel.player;
    }

    public Lane getLane(int index) {
        return lanes.get(index);
    }

    public Lane getLane(float y) {
        return getLane((int) Math.floor(y / Constants.Game.TILE_HEIGHT));
    }

    public void tick(int ticks) {
        for (Lane lane : lanes) {
            lane.tick(ticks);
        }
        zombieManager.tick(new ZombieTickContext(ticks, this));
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            projectile.tick(this, ticks);

            if (projectile.isDead()) {
                projectiles.remove(i);
            }
        }

        economyManager.tick(ticks);
        waveManager.tick(ticks);
        tickCounter += ticks;
    }

    public Tile getTile(int row, int column) {
        try {
            return lanes.get(row).tiles.get(column);
        } catch (Exception e) {
            return null;
        }
    }

    public Tile getTile(float x, float y) {
        // نکته‌ی مهم (باگ پیدا شده): قبلاً اینجا y هم بر Constants.Game.TILE_SIZE (که
        // عرض/اندازه‌ی ستونه، = 1f) تقسیم می‌شد. ولی جایگاه واقعی لاین‌ها (ردیف‌ها) روی
        // محور Y بر اساس Constants.Game.TILE_HEIGHT (=1.285f) محاسبه می‌شه (مثلا در
        // WaveType.placeZombiesRandomly: y = (row-1)*TILE_HEIGHT + bottomLineMeter).
        // چون TILE_SIZE != TILE_HEIGHT، تقسیم y بر TILE_SIZE ایندکس ردیف اشتباهی
        // می‌ساخت (مثلا برای زامبی‌های ردیف‌های بالاتر، ایندکس محاسبه‌شده از محدوده‌ی
        // معتبر لاین‌ها (0..totalRows-1) خارج می‌شد)، getTile(row, column) هم چون
        // exception رو catch و null برمی‌گردونه، این null بی‌سروصدا به بالادست
        // (مثلا ZombieManager.processMiniTickMovement) می‌رسید و چون اونجا چک null
        // نمی‌شد، باعث NullPointerException و کرش کل بازی می‌شد.
        return getTile((int) (y / Constants.Game.TILE_HEIGHT), (int) (x / Constants.Game.TILE_WIDTH));
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
        Set<Zombie> seenZombies = new HashSet<>();
        return lanes.stream()
            .flatMap(lane -> lane.zombies.stream())
            .filter(seenZombies::add) // اگر زامبی تکراری باشد، اضافه نمی‌شود و رد می‌شود
            .collect(Collectors.toCollection(ArrayList::new));
    }
//    public ArrayList<Zombie> getAllZombies() {
//        return lanes.stream()
//            .flatMap(lane -> lane.zombies.stream())
//            .collect(Collectors.toCollection(ArrayList::new));
//    }

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
