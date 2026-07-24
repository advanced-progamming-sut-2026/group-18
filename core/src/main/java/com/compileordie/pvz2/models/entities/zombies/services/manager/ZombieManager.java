package com.compileordie.pvz2.models.entities.zombies.services.manager;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.obstacles.Obstacle;
import com.compileordie.pvz2.models.entities.obstacles.ObstacleType;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.ZombieBuilder;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.boss.GargantuarZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.capable.HunterZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.capable.PianistZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.capable.RaZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.capable.TurquoiseZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.DodoRiderZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.MovementState;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.SnorkelZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.Tomb;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.TombraiserZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.vehicle.Barrel;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Tile;
import com.compileordie.pvz2.models.game.economy.Sun;
import com.compileordie.pvz2.models.game.economy.SunType;

import java.util.*;

import static com.badlogic.gdx.math.MathUtils.random;
import static com.compileordie.pvz2.models.entities.zombies.services.manager.PlantEatabilityChecker.isEatable;
import static com.compileordie.pvz2.models.entities.zombies.services.manager.PlantVisibilityChecker.isVisible;

public class ZombieManager {

    //---
    private static final Set<ZombieType> PIANOABLE_ZOMBIES = EnumSet.of(
        ZombieType.STANDARD, ZombieType.NEWSPAPER_ZOMBIE, ZombieType.PARASOL_ZOMBIE, ZombieType.BLOCKHEAD, ZombieType.BUCKETHEAD, ZombieType.TOMBRAISER
    );
    //---
    float dt = Constants.Game.TIME_COEFFICIENT;
    double tileWidth = Constants.Game.TILE_WIDTH;
    double tileHeight = Constants.Game.TILE_HEIGHT;
    double smashDamage = 999999;

    public ZombieManager() {
    }

    public void tick(ZombieTickContext context) {
        double delta = (context.getDelta() != 0 ? context.getDelta() : 1 * Constants.Game.TIME_COEFFICIENT);
        GameBoard myMap = context.getGameMap();
        List<Sun> mySuns = context.getSunsOnGround();
        List<Zombie> myZombies = context.getActiveZombies();
        List<Plant> myPlants = context.getActivePlants();
        //--------------------
        List<Tile> myTiles = new ArrayList<>();
        for (int r = 0; r <= 4; r++) {
            for (int c = 0; c <= 8; c++) {
                myTiles.add(myMap.getTile(r, c));
            }
        }
        List<Obstacle> myObstacles = new ArrayList<>();
        for (int r = 0; r <= 4; r++) {
            for (int c = 0; c <= 8; c++) {
                if (myMap.getTile(r, c).obstacle != null) myObstacles.add(myMap.getTile(r, c).obstacle);
            }
        }
        //=====================================================
        //=====================================================


        //=========== 1.خواص زامبی ها ==========
        miniTick(myZombies, myMap);

        //=========== 2.خوردن گیاهان توسط زامبی ها ==========
        combatTick(myZombies, myPlants);

        //=========== 3.مبارزه دو زامبی ==========
        combatingTwoZombie(myZombies);

        //=========== 4.اسپاون کردن ایمپ ==========
        spawnImpFromBarrel(myZombies, myObstacles);

        //========== 5.تیک عادی زامبی ها ==========
        for (Zombie z : myZombies) {
            z.move(1);
            z.tick();
        }

    }

    public void spawnImpFromGargantuar(Zombie z, List<Zombie> activeZs) {
        activeZs.add(ZombieBuilder.create(ZombieType.IMP, Math.max(z.getX() - 3 * tileWidth, tileWidth), z.getY(), z.getCurrentRow()));
    }

    public void spawnImpFromBarrel(List<Zombie> activeZs, List<Obstacle> myObstacles) {
        for (Obstacle b : myObstacles) {
            if (b.type == ObstacleType.BARREL) {
                if (((Barrel) b).isDestroyed() && ((Barrel) b).shouldWeSpawnImp()) {
                    activeZs.add(ZombieBuilder.create(ZombieType.IMP, b.getX(), ((Barrel) b).getRow() == 4 ? b.getY() : b.getY() + tileHeight, Math.min(((Barrel) b).getRow() + 1, 4)));
                    activeZs.add(ZombieBuilder.create(ZombieType.IMP, b.getX(), ((Barrel) b).getRow() == 0 ? b.getY() : b.getY() - tileHeight, Math.max(((Barrel) b).getRow() - 1, 0)));
                    ((Barrel) b).stopSpawnImp();
                }
            }
        }
    }

    public void combatingTwoZombie(List<Zombie> myZombies) {
        for (Zombie z : myZombies) {
            for (Zombie z1 : myZombies) {
                if (z == z1 || !(Math.abs(z.getY() - z1.getY()) <= tileHeight / 6 && Math.abs(z.getX() - z1.getX()) <= tileWidth / 6))
                    continue;
                if ((z.isHypnotized() && !z1.isHypnotized()) || (z1.isHypnotized() && !z.isHypnotized())) {
                    z.isCombatingWithHypnotized = true;
                    z1.isCombatingWithHypnotized = true;
                    z.takeDamage((z1.getType() == ZombieType.ALL_STAR ? smashDamage : z1.getAttackPower() * dt), DamageType.NORMAL);
                    z1.takeDamage((z.getType() == ZombieType.ALL_STAR ? smashDamage : z.getAttackPower() * dt), DamageType.NORMAL);
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
        int index1 = random.nextInt(ts.size());
        int index2;
        do {
            index2 = random.nextInt(ts.size());
        } while (index1 == index2);
        Tile randomTile1 = ts.get(index1);
        Tile randomTile2 = ts.get(index2);
        //---
        randomTile1.obstacle = new Tomb(700, randomTile1.row, randomTile1.column, (randomTile1.column + 0.5) * tileWidth, (randomTile1.row) * tileHeight);
        randomTile2.obstacle = new Tomb(700, randomTile2.row, randomTile2.column, (randomTile2.column + 0.5) * tileWidth, (randomTile2.row) * tileHeight);
    }

    public void stealingByRaZombie(Zombie z, GameBoard map) {
        boolean flag = false;
        Iterator<Sun> iterator = map.economyManager.suns.iterator();
        while (iterator.hasNext()) {
            Sun sun = iterator.next();
            if (sun.type == SunType.NORMAL && sun.target == z && Math.abs(sun.getX() - z.getX()) <= tileWidth / 10 && Math.abs(sun.getY() - z.getY()) <= tileHeight / 10) {
                ((RaZombie) z).addStolen(25);
                sun.target = null;
                iterator.remove();
            } else if (sun.target == null || sun.target == z) {
                sun.target = z;
                flag = true;
            }
        }
        if (!flag) ((RaZombie) z).stopStealing();
    }

    public void playingPiano(Zombie z, List<Zombie> myZombies) {
        for (Zombie pied : myZombies) {
            if (PIANOABLE_ZOMBIES.contains(pied.getType()) && !pied.isEating && !pied.isCombatingWithHypnotized && !pied.isHypnotized() && pied.getY() >= tileHeight && pied.getY() <= 3 * tileHeight) {
                if (Math.random() <= 5) {
                    pied.setY(pied.getY() - tileHeight);
                    pied.setCurrentRow(pied.getCurrentRow() - 1);
                } else {
                    pied.setY(pied.getY() + tileHeight);
                    pied.setCurrentRow(pied.getCurrentRow() + 1);
                }
            }
        }
    }

    public void miniTick(List<Zombie> myZombies, GameBoard myMap) {
        for (Zombie z : myZombies) {
            // --- اسپاون ایمپ از غول ---
            if (z.getType() == ZombieType.GARGANTUAR && ((GargantuarZombie) z).shouldWeSpawnImp()) {
                spawnImpFromGargantuar(z, myZombies);
                ((GargantuarZombie) z).stopSpawnImp();
            }
            // --- اسپاون قبر ---
            if (z.getType() == ZombieType.TOMBRAISER) {
                if (((TombraiserZombie) z).shouldWeSpawnTomb()) {
                    spawnTomb(myMap);
                    ((TombraiserZombie) z).stopSpawnTomb();
                }
            }
            // --- پردازش بالا پایین اومدن غواص ---
            if (z.getType() == ZombieType.SNORKEL_ZOMBIE) {
                if (((SnorkelZombie) z).isOceanTile((myMap.getTile((float) (z.getX()), (float) (z.getY()))).type)) {
                    if (z.isEating) {
                        ((SnorkelZombie) z).surface();
                    } else {
                        ((SnorkelZombie) z).underwater();
                    }
                } else {
                    ((SnorkelZombie) z).walk();
                }
            }
            // --- پردازش تورکوآیز ---
            if (z.getType() == ZombieType.TURQUOISE_ZOMBIE) {
                if (((TurquoiseZombie) z).shouldWeSteal()) {
                    myMap.economyManager.sunAmount = Math.max(myMap.economyManager.sunAmount - 25, 0);
                    ((TurquoiseZombie) z).addStolen(25);
                    ((TurquoiseZombie) z).stopSteal();
                }
                if (((TurquoiseZombie) z).shouldWeBackSun()) {
                    for (int i = 0; i <= ((TurquoiseZombie) z).totalStolenSuns / 50; i++) {
                        myMap.economyManager.suns.add(new Sun(z.getX(), z.getY(), SunType.NORMAL, true, (float) z.getY()));
                    }
                    ((TurquoiseZombie) z).stopBackSun();
                }
            }
            // --- پردازش را زامبی ---
            if (z.getType() == ZombieType.RA_ZOMBIE) {
                if (((RaZombie) z).shouldWeBackSun()) {
                    for (int i = 0; i < ((RaZombie) z).stolenSunCount / 25; i++) {
                        myMap.economyManager.suns.add(new Sun(z.getX(), z.getY(), SunType.NORMAL, true, (float) z.getY()));
                    }
                    ((RaZombie) z).stopBackSun();
                }
            }
            // --- جابجایی سطر توسط پیانیست ---
            if (z.getType() == ZombieType.PIANIST_ZOMBIE && ((PianistZombie) z).isPlaying()) playingPiano(z, myZombies);
            // --- خورشید دزدی را زامبی ---
            if (z.getType() == ZombieType.RA_ZOMBIE && ((RaZombie) z).shouldWeSteal()) stealingByRaZombie(z, myMap);
        }
    }

    public void combatTick(List<Zombie> myZombies, List<Plant> myPlants) {
        for (Zombie z : myZombies) {
            for (Plant p : myPlants) {
                // --- خوردن گیاه ---
                if (Math.abs(z.getY() - p.getY()) <= tileHeight / 6) {
                    if (Math.abs(z.getX() - p.getY()) <= tileWidth / 6) {
                        if ((z.getType() == ZombieType.DODO_RIDER && ((DodoRiderZombie) z).getState() == MovementState.FLYING)
                            || !isEatable(p) || p.isFreezedByHunter() || p.isFreezedByOcto()) {
                        } else {  // عملیات خوردن گیاه
                            z.isEating = true;
                            p.takeDamage((int) ((z.getType() == ZombieType.ALL_STAR ? smashDamage : z.getAttackPower() * dt)));
                        }
                    }
                }
                // --- پرواز دودوسوار ---
                if (z.getType() == ZombieType.DODO_RIDER && (Math.abs(z.getY() - p.getY()) <= tileHeight / 6 && Math.abs(z.getX() - p.getX()) <= tileWidth / 1.7) && isVisible(p) && !p.isFreezedByHunter() && !p.isFreezedByOcto()) {
                    ((DodoRiderZombie) z).onPlantCollisionWithHalfOfTileWidth(PlantType.getByName(p.getName()));
                }
                // --- دزدیدن و لیزر تورکوآیز ---
                if (z.getType() == ZombieType.TURQUOISE_ZOMBIE && (Math.abs(z.getY() - p.getY()) <= tileHeight / 6 && Math.abs(z.getX() - p.getX()) <= tileWidth * 3.7) && isVisible(p) && !p.isFreezedByHunter() && !p.isFreezedByOcto()) {
                    ((TurquoiseZombie) z).startStealing();
                    if (((TurquoiseZombie) z).shouldWeLaser()) {
                        p.takeDamage((int) smashDamage);
                        ((TurquoiseZombie) z).stopLaser();
                    }
                }
                // --- سوزاندن گیاه توسط اکسپلورر ---
                if (z.getType() == ZombieType.EXPLORER_ZOMBIE && (Math.abs(z.getY() - p.getY()) <= tileHeight / 6 && Math.abs(z.getX() - p.getX()) <= tileWidth * 1) && isVisible(p)) {
                    if (p.isFreezedByHunter()) {
                        p.setIsFreezedByHunter(false);
                        p.setIsFreezedByHunterCounter(0);
                    } else {
                        p.takeDamage((int) smashDamage);
                    }
                }
                // --- پرتاب یخ توسط هانتر ---
                if (z.getType() == ZombieType.HUNTER_ZOMBIE && (Math.abs(z.getY() - p.getY()) <= tileHeight / 6 && Math.abs(z.getX() - p.getX()) <= ((HunterZombie) z).abilityRange) && isVisible(p) && !p.isFreezedByHunter() && !p.isFreezedByOcto()) {
                    ((HunterZombie) z).setShouldAttack(true);
                    if (((HunterZombie) z).getShouldShut()) {
                        p.increaseFreezedByHunter();
                        ((HunterZombie) z).setShouldShut(false);
                    }
                }
                // --- پرتاب اختاپوس ---
                if (z.getType() == ZombieType.OCTOPUS_ZOMBIE && (Math.abs(z.getY() - p.getY()) <= tileHeight / 6 && Math.abs(z.getX() - p.getX()) <= ((HunterZombie) z).abilityRange) && isVisible(p) && !p.isFreezedByHunter() && !p.isFreezedByOcto()) {
                    p.setFreezedByOcto(true);
                }
            }
        }
    }
}
