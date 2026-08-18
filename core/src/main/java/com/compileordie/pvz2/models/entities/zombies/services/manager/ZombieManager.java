package com.compileordie.pvz2.models.entities.zombies.services.manager;

import com.compileordie.pvz2.config.Constants;
import java.util.Random;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.obstacles.Obstacle;
import com.compileordie.pvz2.models.entities.obstacles.ObstacleType;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.ZombieBuilder;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.ZomBoss.DarkZomboss;
import com.compileordie.pvz2.models.entities.zombies.variants.ZomBoss.EgyptZomboss;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.boss.GargantuarZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.SnorkelZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.Tomb;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.TombraiserZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.vehicle.Barrel;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Tile;

import java.util.*;
import static com.badlogic.gdx.math.MathUtils.random;

public class ZombieManager {
    public static boolean isImpProved = false;

    float dt = Constants.Game.TIME_COEFFICIENT;
    double tileWidth = Constants.Game.TILE_WIDTH;
    double tileHeight = Constants.Game.TILE_HEIGHT;

    // نمونۀ مدیر مبارزات برای واگذاری وظایف مربوطه
    private final ZombieCombatManager combatManager;

    public ZombieManager() {
        this.combatManager = new ZombieCombatManager();
    }

    public void tick(ZombieTickContext context) {
        GameBoard myMap = context.getGameMap();
        List<Zombie> myZombies = context.getGameMap().getAllZombies();
        List<Plant> myPlants = context.getGameMap().getAllPlants();

        List<Obstacle> myObstacles = new ArrayList<>();
        for (int r = 0; r <= 4; r++) {
            for (int c = 0; c <= 8; c++) {
                if (myMap.getTile(r, c).obstacle != null) myObstacles.add(myMap.getTile(r, c).obstacle);
            }
        }

        miniTick(myZombies, myMap);
        combatManager.combatTick(myZombies, myPlants);
        combatManager.projectileCollisionTick(myMap);
        combatManager.combatingTwoZombie(myZombies);
        spawnImpFromBarrel(myZombies, myObstacles);
        handleEgyptZomboss(myMap);
        handleDarkZomboss(myMap);

        for (int i = myZombies.size() - 1; i >= 0; i--) {
            Zombie z = myZombies.get(i);
            z.move(1);
            z.tick();
            if (z.getHealth() <= 0) {
                if (z.shouldRemooove) myZombies.remove(i);
                else z.shouldRemooove = true;
            }
        }
    }

    public void handleEgyptZomboss(GameBoard gb){
        for (int i=0; i<gb.getAllZombies().size(); i++){
            Zombie z = gb.getAllZombies().get(i);
            if (z.getType()==ZombieType.ZOMBOSS_IN_EGYPT){
                EgyptZomboss zombie = (EgyptZomboss)z;
                if (zombie.stun){
                    double dt = Constants.Game.TIME_COEFFICIENT;
                    zombie.stunTimer += dt;
                    if (zombie.stunTimer >= 10){
                        zombie.stun = false;
                        zombie.stunTimer = 0;}}
                if (zombie.spawnZombies){
                    if (zombie.spawnTimer == 0){
                        int ro1 = Math.random() <=0.5 ? zombie.rowDown : zombie.rowUp;
                        int ro2 = Math.random() <=0.5 ? zombie.rowDown : zombie.rowUp;
                        Zombie z1 = ZombieBuilder.create(Math.random()<=0.4 ? ZombieType.STANDARD : Math.random()<=0.4 ? ZombieType.RA_ZOMBIE : ZombieType.IMP_DRAGON, zombie.getX()-3*Constants.Game.TILE_WIDTH, ro1==zombie.rowDown? zombie.getY() :  zombie.getY()+Constants.Game.TILE_HEIGHT, ro1);
                        Zombie z2 = ZombieBuilder.create(Math.random()<=0.4 ? ZombieType.BLOCKHEAD : Math.random()<=0.4 ? ZombieType.CONEHEAD : ZombieType.IMP_DRAGON, zombie.getX()-2*Constants.Game.TILE_WIDTH, ro2==zombie.rowDown? zombie.getY() :  zombie.getY()+Constants.Game.TILE_HEIGHT, ro2);
                        gb.lanes.get(ro1).zombies.add(z1);
                        gb.lanes.get(ro2).zombies.add(z2);}
                    double dt = Constants.Game.TIME_COEFFICIENT;
                    zombie.spawnTimer += dt;
                    if (zombie.spawnTimer >= 3){
                        zombie.spawnZombies = false;
                        zombie.spawnTimer = 0;}}
                if (zombie.boom){
                    if (zombie.boomTimer == 0){
                        int row = Math.random()<=0.5 ? zombie.rowUp : zombie.rowDown;
                        int col = Math.random()<=0.5 ? 0 : 1;
                        gb.lanes.get(row).tiles.get(col).plant = null;
                        zombie.r = row;
                        zombie.c = col;
                        spawnTomb(gb);}
                    double dt = Constants.Game.TIME_COEFFICIENT;
                    zombie.boomTimer += dt;
                    if (zombie.boomTimer >= 1.5){
                        zombie.boom = false;
                        zombie.boomTimer = 0;}}
                if (zombie.smash){
                    ArrayList<Zombie> zS = new ArrayList<>();
                    for (Zombie zz : gb.lanes.get(zombie.rowDown).zombies){if (zz!=zombie) zS.add(zz);}
                    gb.lanes.get(zombie.rowDown).zombies.removeAll(zS);
                    ArrayList<Zombie> zS2 = new ArrayList<>();
                    for (Zombie zz : gb.lanes.get(zombie.rowUp).zombies){if (zz!=zombie) zS2.add(zz);}
                    gb.lanes.get(zombie.rowUp).zombies.removeAll(zS2);
                    for (Tile t : gb.lanes.get(zombie.rowDown).tiles){t.plant = null;}
                    for (Tile t : gb.lanes.get(zombie.rowUp).tiles){t.plant = null;}}}}}


    public void spawnImpDrag(int r,int c,GameBoard map){
        Zombie z = ZombieBuilder.create(ZombieType.IMP_DRAGON, Constants.Game.PADDING_X_REALITY+c*Constants.Game.TILE_WIDTH, Constants.Game.PADDING_Y_REALITY+0.2+r*Constants.Game.TILE_HEIGHT, r);
        map.lanes.get(r).zombies.add(z);
    }
    public void handleDarkZomboss(GameBoard gb){
        for (int i=0; i<gb.getAllZombies().size(); i++){
            Zombie z = gb.getAllZombies().get(i);
            if (z.getType()==ZombieType.ZOMBOSS_IN_DARK){
                DarkZomboss zombie = (DarkZomboss) z;
                if (zombie.stun){
                    double dt = Constants.Game.TIME_COEFFICIENT;
                    zombie.stunTimer += dt;
                    if (zombie.stunTimer >= 10){ zombie.stun = false; zombie.stunTimer = 0;}}
                if (zombie.spawnZombies){
                    if (zombie.spawnTimer == 0){
                        int ro1 = Math.random() <=0.5 ? zombie.rowDown : zombie.rowUp;
                        int ro2 = Math.random() <=0.5 ? zombie.rowDown : zombie.rowUp;
                        Zombie z1 = ZombieBuilder.create(Math.random()<=0.4 ? ZombieType.STANDARD : Math.random()<=0.4 ? ZombieType.BUCKETHEAD : ZombieType.IMP_DRAGON, zombie.getX()-3*Constants.Game.TILE_WIDTH, ro1==zombie.rowDown? zombie.getY() :  zombie.getY()+Constants.Game.TILE_HEIGHT, ro1);
                        Zombie z2 = ZombieBuilder.create(Math.random()<=0.4 ? ZombieType.KNIGHT : Math.random()<=0.4 ? ZombieType.CONEHEAD : ZombieType.KNIGHT, zombie.getX()-2*Constants.Game.TILE_WIDTH, ro2==zombie.rowDown? zombie.getY() :  zombie.getY()+Constants.Game.TILE_HEIGHT, ro2);
                        gb.lanes.get(ro1).zombies.add(z1);
                        gb.lanes.get(ro2).zombies.add(z2);}
                    double dt = Constants.Game.TIME_COEFFICIENT; zombie.spawnTimer += dt;
                    if (zombie.spawnTimer >= 3){zombie.spawnZombies = false;zombie.spawnTimer = 0;}}
                if (zombie.boom){if (zombie.boomTimer == 0){
                        Random rand = new Random();
                        int r1 = rand.nextInt(5); // بازه 0 تا 4
                        int c1 = rand.nextInt(3); // بازه 0 تا 2
                        int r2, c2;
                        do {r2 = rand.nextInt(5);
                            c2 = rand.nextInt(7);} while (r1 == r2 && c1 == c2);
                        gb.lanes.get(r1).tiles.get(c1).isOnFire = true;
                        gb.lanes.get(r2).tiles.get(c2).isOnFire = true;
                        zombie.r1 = r1;
                        zombie.r2 = r2;
                        zombie.c1 = c1;
                        zombie.c2 = c2;
                        spawnImpDrag(r1, c1, gb);
                        spawnImpDrag(r2, c2, gb);}
                    double dt = Constants.Game.TIME_COEFFICIENT;
                    zombie.boomTimer += dt;
                    if (zombie.boomTimer >= 0.5){zombie.boom = false;zombie.boomTimer = 0;}}
                if (zombie.smash){
                    if (zombie.smashTimer == 0){
                        for (Tile t : gb.lanes.get(zombie.rowDown).tiles) {
                            t.isOnFire = true;}
                        for (Tile t : gb.lanes.get(zombie.rowUp).tiles) {
                            t.isOnFire = true;}}
                    double dt = Constants.Game.TIME_COEFFICIENT;
                    zombie.smashTimer += dt;
                    if (zombie.smashTimer >= 1){zombie.smash = false;zombie.smashTimer = 0;}}}}}

    public void spawnImpFromGargantuar(Zombie z, List<Zombie> activeZs) {
        Zombie imp = ZombieBuilder.create(ZombieType.IMP, Math.max(z.getX() - 3 * tileWidth, tileWidth), z.getY(), z.getCurrentRow(), true);
        imp.fromGarg = true;
        double originX = z.getX() + 100.0 / Constants.UI.METER_TO_PIX;
        double originY = z.getY() + 200.0 / Constants.UI.METER_TO_PIX;
        imp.startFlyInFreeze(1, originX, originY);
        activeZs.add(imp);
        AppModel.gameSession.gameBoard.getAllZombies().add(imp);
        AppModel.gameSession.gameBoard.lanes.get(z.getCurrentRow()).zombies.add(imp);
    }

    public void spawnImpFromBarrel(List<Zombie> activeZs, List<Obstacle> myObstacles) {
        List<Obstacle> obstaclesCopy = new ArrayList<>(myObstacles);
        for (Obstacle b : obstaclesCopy) {
            if (b.type == ObstacleType.BARREL) {
                if (((Barrel) b).isDestroyed() && ((Barrel) b).shouldWeSpawnImp()) {
                    int row1 = Math.min(((Barrel) b).getRow() + 1, 4);
                    Zombie imp1 = ZombieBuilder.create(ZombieType.IMP, b.getX(), ((Barrel) b).getRow() == 4 ? b.getY() : b.getY() + tileHeight, row1);
                    activeZs.add(imp1);
                    AppModel.gameSession.gameBoard.getAllZombies().add(imp1);
                    AppModel.gameSession.gameBoard.lanes.get(row1).zombies.add(imp1);
                    int row2 = Math.max(((Barrel) b).getRow() - 1, 0);
                    Zombie imp2 = ZombieBuilder.create(ZombieType.IMP, b.getX(), ((Barrel) b).getRow() == 0 ? b.getY() : b.getY() - tileHeight, row2);
                    activeZs.add(imp2);
                    AppModel.gameSession.gameBoard.getAllZombies().add(imp2);
                    AppModel.gameSession.gameBoard.lanes.get(row2).zombies.add(imp2);
                    ((Barrel) b).stopSpawnImp();
                }
            }
        }
    }

    public void spawnTomb(GameBoard gb) {
        List<Tile> ts = new ArrayList<>();
        for (int r = 0; r <= 4; r++) {
            for (Tile t : gb.getLane(r).tiles) {
                if (t.plant == null && t.obstacle == null) {
                    ts.add(t);
                }
            }
        }
        if (ts.isEmpty()) return;
        int index1 = random.nextInt(ts.size());
        int index2;
        if (ts.size() > 1) {
            do {
                index2 = random.nextInt(ts.size());
            } while (index1 == index2);
        } else {
            index2 = index1;
        }
        Tile randomTile1 = ts.get(index1);
        Tile randomTile2 = ts.get(index2);
        Tomb tomb1 = new Tomb(700, randomTile1.row, randomTile1.column, (randomTile1.column + 0.5) * tileWidth, (randomTile1.row) * tileHeight);
        randomTile1.obstacle = tomb1;
        gb.getLane(randomTile1.row).tombs.add(tomb1);
        Tomb tomb2 = new Tomb(700, randomTile2.row, randomTile2.column, (randomTile2.column + 0.5) * tileWidth, (randomTile2.row) * tileHeight);
        randomTile2.obstacle = tomb2;
        gb.getLane(randomTile2.row).tombs.add(tomb2);
    }

    public void miniTick(List<Zombie> myZombies, GameBoard myMap) {
        List<Zombie> zombiesCopy = new ArrayList<>(myZombies);
        for (Zombie z : zombiesCopy) {
            processMiniTickSpawns(z, myZombies, myMap);
            processMiniTickMovement(z, myMap);
            combatManager.processMiniTickAbilities(z, myZombies, myMap);
        }
    }

    private void processMiniTickSpawns(Zombie z, List<Zombie> myZombies, GameBoard myMap) {
        if (z.getType() == ZombieType.GARGANTUAR && ((GargantuarZombie) z).shouldWeSpawnImp()) {
            spawnImpFromGargantuar(z, myZombies);
            ((GargantuarZombie) z).stopSpawnImp();
            ((GargantuarZombie) z).canSpawn = false;
        }
        if (z.getType() == ZombieType.TOMBRAISER) {
            if (((TombraiserZombie) z).shouldWeSpawnTomb()) {
                spawnTomb(myMap);
                ((TombraiserZombie) z).stopSpawnTomb();
            }
        }
    }

    private void processMiniTickMovement(Zombie z, GameBoard myMap) {
        if (z.getType() == ZombieType.SNORKEL_ZOMBIE) {
            Tile currentTile;
            try{currentTile = myMap.lanes.get(z.getCurrentRow()).tiles.get((int)((z.getX()- 6.47f)/ Constants.Game.TILE_HEIGHT));}
            catch (Exception e) {
                currentTile = null;
            }
            if (currentTile == null) {
                ((SnorkelZombie) z).walk();
                return;
            }
            if (SnorkelZombie.isOceanTile(currentTile.type)) {
                if (z.isEating) {
                    ((SnorkelZombie) z).surface();
                } else {
                    ((SnorkelZombie) z).underwater();
                }
            } else {
                ((SnorkelZombie) z).walk();
            }
        }
    }
}
