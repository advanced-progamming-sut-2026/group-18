package com.compileordie.pvz2.models.game.board;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.controllers.PlantSpawner;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.obstacles.ObstacleType;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.PlantTemplate;
import com.compileordie.pvz2.models.entities.plants.enums.PlantCategory;
import com.compileordie.pvz2.models.entities.plants.enums.PlantTag;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.entities.zombies.services.manager.ZombieManager;
import com.compileordie.pvz2.models.entities.zombies.services.manager.ZombieTickContext;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.Tomb;
import com.compileordie.pvz2.models.game.economy.EconomyManager;
import com.compileordie.pvz2.models.game.economy.EconomyType;
import com.compileordie.pvz2.models.game.economy.PlantFood;
import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.models.game.minigames.vasebreaker.SeedPacket;
import com.compileordie.pvz2.models.game.minigames.vasebreaker.Vase;
import com.compileordie.pvz2.models.game.waves.WaveManager;
import com.compileordie.pvz2.models.game.waves.WaveType;
import com.compileordie.pvz2.models.missions.quests.QuestEvent;
import com.compileordie.pvz2.models.missions.quests.QuestManager;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.models.user.Player; // Arsam

import java.util.*;
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
    public ArrayList<SeedPacket> seedPackets;
    public ArrayList<PlantFood> plantFoods;
    public int tideLevel;
    public int maxTideLevel;
    public boolean specialIsLost;
    public int lostPlants;
    public int tickCounter;
    public int registeredShapes;
    private List<Plant> plants;

    // --- Quest tracking (scoped to this level/session; read by GameJudge at level-clear time) ---
    // 1-indexed columns/rows that have had a plant placed on them at any point this level.
    public Set<Integer> plantedColumns = new HashSet<>();
    public Set<Integer> plantedRows = new HashSet<>();
    // Every distinct PlantType placed this level (used for the "NO_FAMILY" check).
    public Set<PlantType> plantedTypes = new HashSet<>();
    // Distinct ZOMBIE_KILLED_BY_PLANT contexts this level (e.g. "PEASHOOTER", "MOWER"),
    // used to check the "ONLY_FAMILY" (Family Slayer) condition.
    public Set<String> killContexts = new HashSet<>();
    public int sunProducingPlantsPlanted = 0;
    public boolean nightPlantPlanted = false;

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
        this.seedPackets = new ArrayList<>();
        this.plantFoods = new ArrayList<>();
        this.waveManager = new WaveManager(this, waveType, waveNumber, shouldStartWaves);
        this.tideLevel = 0;
        this.maxTideLevel = ConfigManager.gameplay().maxTideLevel;
        this.specialIsLost = false;
        this.lostPlants = 0;
        this.tickCounter = 0;
        this.registeredShapes = 0;
        this.plants = new ArrayList<>();
    }

    // Arsam
    public Player getPlayer() {
        return AppModel.player;
    }

    public Lane getLane(int index) {
        return lanes.get(index);
    }

    public Lane getLane(float y) {
        return getLane((int) Math.floor((y - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT));
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

        for (Zombie zombie : getAllZombies()) {
            ZombieType type = zombie.getType();
            Player player = AppModel.player;

            if (!player.unlockedZombies.contains(type)) {
                player.unlockedZombies.add(type);
                new UserDatabase().save(player);
            }
        }

        economyManager.tick(ticks);
        waveManager.tick(ticks);
        tickCounter += ticks;

        List<Plant> currentPlants = new ArrayList<>();
        for (Plant plant : getAllPlants()) {
            if (plant != null && (!plant.isAlive() || plant.isDead())) {
                for (Tile tile : getAllTiles()) {
                    if (tile.plant == plant) {
                        tile.plant = null;
                    }
                }
            }
            if (plant != null && !plant.isDead() && plant.isAlive()) {
                currentPlants.add(plant);
            }
        }
        Iterator<Plant> iterator = plants.iterator();
        while (iterator.hasNext()) {
            Plant plant = iterator.next();
            if (!currentPlants.contains(plant)) {
                lostPlants++;
                iterator.remove(); // Safely removes from `plants` so it is only counted once
            }
        }
        for (Plant plant : currentPlants) {
            if (!plants.contains(plant)) {
                plants.add(plant);
            }
        }
    }

    public Tile getTile(int row, int column) {
        try {
            return lanes.get(row).tiles.get(column);
        } catch (Exception e) {
            return null;
        }
    }

    public Tile getTile(float x, float y) {
        return getTile((int) Math.floor((y - Constants.Game.PADDING_Y) / Constants.Game.TILE_HEIGHT),
            (int) Math.floor((x - Constants.Game.PADDING_X) / Constants.Game.TILE_WIDTH));
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

    public ArrayList<Vase> getAllVases() {
        return getAllTiles().stream()
            .filter(tile -> tile.vase != null)
            .map(tile -> tile.vase)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Projectile> getActiveProjectiles() {
        return projectiles;
    }

    /**
     * Records the bookkeeping needed for the various level-condition quests (Family Slayer,
     * Flourishing in Limits, Cloudy Day, Night or Morning, One Less Column, Defenseless Row/Cross,
     * Master Demolisher) and dispatches EXPLOSIVE_PLANTED where relevant. Called from wherever a
     * plant is actually placed onto this board.
     */
    public void recordPlanting(PlantType plantType, Tile tile) {
        if (plantType == null || tile == null) return;

        plantedColumns.add(tile.column + 1); // 1-indexed, matching the QuestDatabaseSeeder's EMPTY_COL/ROW values
        plantedRows.add(tile.row + 1);
        plantedTypes.add(plantType);

        PlantTemplate template = PlantSpawner.getTemplate(plantType);
        if (template != null) {
            if (template.getCategory() == PlantCategory.SUN_PRODUCER) {
                sunProducingPlantsPlanted++;
            }
            if (template.getCategory() == PlantCategory.EXPLOSIVE) {
                QuestManager.dispatch(QuestEvent.EXPLOSIVE_PLANTED, 1, null);
            }
            if (template.getTags() != null && template.getTags().contains(PlantTag.NIGHT)) {
                nightPlantPlanted = true;
            }
        }
    }

    /** Records which plant (or "MOWER") scored a kill this level, for the Family Slayer quest. */
    public void recordKillContext(String context) {
        if (context != null) killContexts.add(context.trim().toUpperCase());
    }
}
