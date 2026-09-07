package com.compileordie.pvz2.models.entities.zombies.services.manager;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.obstacles.Obstacle;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.zombies.StatusEffect;
import com.compileordie.pvz2.models.entities.zombies.ZombieBuilder;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.ZomBoss.DarkZomboss;
import com.compileordie.pvz2.models.entities.zombies.variants.ZomBoss.EgyptZomboss;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.boss.GargantuarZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.SnorkelZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.Tomb;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.TombraiserZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.vehicle.BarrelRollerZombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
                if (myMap.getTile(r, c).obstacle != null) {
                    myObstacles.add(myMap.getTile(r, c).obstacle);
                }
            }
        }

        miniTick(myZombies, myMap);
        combatManager.combatTick(myZombies, myPlants);
        combatManager.projectileCollisionTick(myMap);
        combatManager.combatingTwoZombie(myZombies);
        handleEgyptZomboss(myMap);
        handleDarkZomboss(myMap);

        for (int i = myZombies.size() - 1; i >= 0; i--) {
            Zombie z = myZombies.get(i);
            z.move(1);
            z.tick();
            // --- NEW: Floor Radar for Goo Peashooter Puddles! ---
            Tile currentTile = myMap.getTile((float) z.getX(), (float) z.getY());

            if (currentTile != null && currentTile.puddleTimer > 0) {
                // The secondary poison effect (using the dynamically upgraded damage!)
                z.addEffect(new StatusEffect(EffectType.POISON, 20, currentTile.puddleDamage));
                // The heavy speed reduction
                z.addEffect(new StatusEffect(EffectType.GOO_SLOW, 20));
            }
            if (z.getHealth() <= 0) {
                if (z.shouldRemooove) {
                    myZombies.remove(i);
                } else {
                    z.shouldRemooove = true;
                }
            }
            if (z.isHypnotized() && z.getX() >= Constants.Game.LANE_LENGTH) {
                z.setHealth(0);
            }
            if (z.getType() == ZombieType.BARREL_ROLLER && !z.isHypnotized() && ((BarrelRollerZombie) z).spawnImp) {
                spawnImpFromBarrel(myMap, ((BarrelRollerZombie) z));
                ((BarrelRollerZombie) z).spawnImp = false;
            }
        }
    }

    public void handleEgyptZomboss(GameBoard gb) {
        for (int i = 0; i < gb.getAllZombies().size(); i++) {
            Zombie z = gb.getAllZombies().get(i);
            if (z.getType() == ZombieType.ZOMBOSS_IN_EGYPT) {
                EgyptZomboss zombie = (EgyptZomboss) z;
                processEgyptZombossStun(zombie);
                processEgyptZombossSpawn(gb, zombie);
                processEgyptZombossBoom(gb, zombie);
                processEgyptZombossSmash(gb, zombie);
            }
        }
    }

    private void processEgyptZombossStun(EgyptZomboss zombie) {
        if (zombie.stun) {
            double dt = Constants.Game.TIME_COEFFICIENT;
            zombie.stunTimer += dt;
            if (zombie.stunTimer >= 10) {
                zombie.stun = false;
                zombie.stunTimer = 0;
            }
        }
    }

    private void processEgyptZombossSpawn(GameBoard gb, EgyptZomboss zombie) {
        if (zombie.spawnZombies) {
            if (zombie.spawnTimer == 0) {
                int ro1 = Math.random() <= 0.5 ? zombie.rowDown : zombie.rowUp;
                int ro2 = Math.random() <= 0.5 ? zombie.rowDown : zombie.rowUp;

                ZombieType type1 = Math.random() <= 0.4
                    ? ZombieType.STANDARD
                    : (Math.random() <= 0.4 ? ZombieType.RA_ZOMBIE : ZombieType.IMP_DRAGON);
                double y1 = ro1 == zombie.rowDown ? zombie.getY() : zombie.getY() + Constants.Game.TILE_HEIGHT;
                Zombie z1 = ZombieBuilder.create(type1, zombie.getX() - 3 * Constants.Game.TILE_WIDTH, y1, ro1);

                ZombieType type2 = Math.random() <= 0.4
                    ? ZombieType.BLOCKHEAD
                    : (Math.random() <= 0.4 ? ZombieType.CONEHEAD : ZombieType.IMP_DRAGON);
                double y2 = ro2 == zombie.rowDown ? zombie.getY() : zombie.getY() + Constants.Game.TILE_HEIGHT;
                Zombie z2 = ZombieBuilder.create(type2, zombie.getX() - 2 * Constants.Game.TILE_WIDTH, y2, ro2);

                gb.lanes.get(ro1).zombies.add(z1);
                gb.lanes.get(ro2).zombies.add(z2);
            }
            double dt = Constants.Game.TIME_COEFFICIENT;
            zombie.spawnTimer += dt;
            if (zombie.spawnTimer >= 3) {
                zombie.spawnZombies = false;
                zombie.spawnTimer = 0;
            }
        }
    }

    private void processEgyptZombossBoom(GameBoard gb, EgyptZomboss zombie) {
        if (zombie.boom) {
            if (zombie.boomTimer == 0) {
                int row = Math.random() <= 0.5 ? zombie.rowUp : zombie.rowDown;
                int col = Math.random() <= 0.5 ? 0 : 1;
                gb.lanes.get(row).tiles.get(col).plant = null;
                zombie.r = row;
                zombie.c = col;
                spawnTomb(gb);
            }
            double dt = Constants.Game.TIME_COEFFICIENT;
            zombie.boomTimer += dt;
            if (zombie.boomTimer >= 1.5) {
                zombie.boom = false;
                zombie.boomTimer = 0;
            }
        }
    }

    private void processEgyptZombossSmash(GameBoard gb, EgyptZomboss zombie) {
        if (zombie.smash) {
            ArrayList<Zombie> zS = new ArrayList<>();
            for (Zombie zz : gb.lanes.get(zombie.rowDown).zombies) {
                if (zz != zombie) zS.add(zz);
            }
            gb.lanes.get(zombie.rowDown).zombies.removeAll(zS);

            ArrayList<Zombie> zS2 = new ArrayList<>();
            for (Zombie zz : gb.lanes.get(zombie.rowUp).zombies) {
                if (zz != zombie) zS2.add(zz);
            }
            gb.lanes.get(zombie.rowUp).zombies.removeAll(zS2);

            for (Tile t : gb.lanes.get(zombie.rowDown).tiles) {
                t.plant = null;
            }
            for (Tile t : gb.lanes.get(zombie.rowUp).tiles) {
                t.plant = null;
            }
        }
    }

    public void spawnImpDrag(int r, int c, GameBoard map) {
        double x = Constants.Game.PADDING_X_REALITY + c * Constants.Game.TILE_WIDTH;
        double y = Constants.Game.PADDING_Y_REALITY + 0.2 + r * Constants.Game.TILE_HEIGHT;
        Zombie z = ZombieBuilder.create(ZombieType.IMP_DRAGON, x, y, r);
        map.lanes.get(r).zombies.add(z);
    }

    public void handleDarkZomboss(GameBoard gb) {
        for (int i = 0; i < gb.getAllZombies().size(); i++) {
            Zombie z = gb.getAllZombies().get(i);
            if (z.getType() == ZombieType.ZOMBOSS_IN_DARK) {
                DarkZomboss zombie = (DarkZomboss) z;
                processDarkZombossStun(zombie);
                processDarkZombossSpawn(gb, zombie);
                processDarkZombossBoom(gb, zombie);
                processDarkZombossSmash(gb, zombie);
            }
        }
    }

    private void processDarkZombossStun(DarkZomboss zombie) {
        if (zombie.stun) {
            double dt = Constants.Game.TIME_COEFFICIENT;
            zombie.stunTimer += dt;
            if (zombie.stunTimer >= 10) {
                zombie.stun = false;
                zombie.stunTimer = 0;
            }
        }
    }

    private void processDarkZombossSpawn(GameBoard gb, DarkZomboss zombie) {
        if (zombie.spawnZombies) {
            if (zombie.spawnTimer == 0) {
                int ro1 = Math.random() <= 0.5 ? zombie.rowDown : zombie.rowUp;
                int ro2 = Math.random() <= 0.5 ? zombie.rowDown : zombie.rowUp;

                ZombieType type1 = Math.random() <= 0.4
                    ? ZombieType.STANDARD
                    : (Math.random() <= 0.4 ? ZombieType.BUCKETHEAD : ZombieType.IMP_DRAGON);
                double y1 = ro1 == zombie.rowDown ? zombie.getY() : zombie.getY() + Constants.Game.TILE_HEIGHT;
                Zombie z1 = ZombieBuilder.create(type1, zombie.getX() - 3 * Constants.Game.TILE_WIDTH, y1, ro1);

                ZombieType type2 = Math.random() <= 0.4
                    ? ZombieType.KNIGHT
                    : (Math.random() <= 0.4 ? ZombieType.CONEHEAD : ZombieType.KNIGHT);
                double y2 = ro2 == zombie.rowDown ? zombie.getY() : zombie.getY() + Constants.Game.TILE_HEIGHT;
                Zombie z2 = ZombieBuilder.create(type2, zombie.getX() - 2 * Constants.Game.TILE_WIDTH, y2, ro2);

                gb.lanes.get(ro1).zombies.add(z1);
                gb.lanes.get(ro2).zombies.add(z2);
            }
            double dt = Constants.Game.TIME_COEFFICIENT;
            zombie.spawnTimer += dt;
            if (zombie.spawnTimer >= 3) {
                zombie.spawnZombies = false;
                zombie.spawnTimer = 0;
            }
        }
    }

    private void processDarkZombossBoom(GameBoard gb, DarkZomboss zombie) {
        if (zombie.boom) {
            if (zombie.boomTimer == 0) {
                Random rand = new Random();
                int r1 = rand.nextInt(5); // بازه 0 تا 4
                int c1 = rand.nextInt(3); // بازه 0 تا 2
                int r2, c2;
                do {
                    r2 = rand.nextInt(5);
                    c2 = rand.nextInt(7);
                } while (r1 == r2 && c1 == c2);

                gb.lanes.get(r1).tiles.get(c1).isOnFire = true;
                gb.lanes.get(r2).tiles.get(c2).isOnFire = true;
                zombie.r1 = r1;
                zombie.r2 = r2;
                zombie.c1 = c1;
                zombie.c2 = c2;
                spawnImpDrag(r1, c1, gb);
                spawnImpDrag(r2, c2, gb);
            }
            double dt = Constants.Game.TIME_COEFFICIENT;
            zombie.boomTimer += dt;
            if (zombie.boomTimer >= 0.5) {
                zombie.boom = false;
                zombie.boomTimer = 0;
            }
        }
    }

    private void processDarkZombossSmash(GameBoard gb, DarkZomboss zombie) {
        if (zombie.smash) {
            if (zombie.smashTimer == 0) {
                for (Tile t : gb.lanes.get(zombie.rowDown).tiles) {
                    t.isOnFire = true;
                }
                for (Tile t : gb.lanes.get(zombie.rowUp).tiles) {
                    t.isOnFire = true;
                }
            }
            double dt = Constants.Game.TIME_COEFFICIENT;
            zombie.smashTimer += dt;
            if (zombie.smashTimer >= 1) {
                zombie.smash = false;
                zombie.smashTimer = 0;
            }
        }
    }

    public void spawnImpFromGargantuar(Zombie z, List<Zombie> activeZs) {
        Zombie imp = ZombieBuilder.create(
            ZombieType.IMP, Math.max(z.getX() - 3 * tileWidth, tileWidth),
            z.getY(), z.getCurrentRow(), true);
        imp.fromGarg = true;
        double originX = z.getX() + 100.0 / Constants.UI.METER_TO_PIX;
        double originY = z.getY() + 200.0 / Constants.UI.METER_TO_PIX;
        imp.startFlyInFreeze(1, originX, originY);
        activeZs.add(imp);
        AppModel.gameSession.gameBoard.getAllZombies().add(imp);
        AppModel.gameSession.gameBoard.lanes.get(z.getCurrentRow()).zombies.add(imp);
    }

    public void spawnImpFromBarrel(GameBoard map, BarrelRollerZombie z) {
        int r1;
        int r2;
        if (z.getCurrentRow() == 0) {
            r1 = -1;
        } else {
            r1 = z.getCurrentRow() - 1;
        }
        if (z.getCurrentRow() == 4) {
            r2 = -1;
        } else {
            r2 = z.getCurrentRow() + 1;
        }
        //--------
        if (r1 != -1) {
            Zombie z1 = ZombieBuilder.create(
                ZombieType.IMP, z.getX() - 1, z.getY() - Constants.Game.TILE_HEIGHT, r1);
            map.getLane(r1).zombies.add(z1);
        }
        if (r2 != -1) {
            Zombie z2 = ZombieBuilder.create(
                ZombieType.IMP, z.getX() - 1, z.getY() + Constants.Game.TILE_HEIGHT, r2);
            map.getLane(r2).zombies.add(z2);
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
        randomTile1.obstacle = new Tomb(
            700,
            randomTile1.row,
            randomTile1.column,
            (randomTile1.column + 0.5f) * Constants.Game.TILE_WIDTH + Constants.Game.PADDING_X,
            (randomTile1.row + 0.5f) * Constants.Game.TILE_HEIGHT + Constants.Game.PADDING_Y);
        randomTile2.obstacle = new Tomb(
            700,
            randomTile2.row,
            randomTile2.column,
            (randomTile2.column + 0.5f) * Constants.Game.TILE_WIDTH + Constants.Game.PADDING_X,
            (randomTile2.row + 0.5f) * Constants.Game.TILE_HEIGHT + Constants.Game.PADDING_Y);
    }

    public void miniTick(List<Zombie> myZombies, GameBoard myMap) {
        List<Zombie> zombiesCopy = new ArrayList<>(myZombies);
        for (Zombie z : zombiesCopy) {
            // 🧠 وقتی زامبی هیپنوتایز شده، طبق درخواست، همه‌ی قابلیت‌های خاصش
            // (اسپاون ایمپ/قبر، دزدی/برگردوندن خورشید، نواختن پیانو و ...) باید
            // خاموش بشن - فقط رفتار «خوردن زامبی‌های دیگه» (که جای دیگه‌ای، در
            // combatingTwoZombie، مستقل هندل می‌شه) دست‌نخورده می‌مونه.
            // processMiniTickMovement عمدا این‌جا شامل نمی‌شه چون صرفا وضعیت
            // فیزیکی حرکته (مثلا شنای غواص)، نه یه «قابلیت» به معنای واقعی.
            if (!z.isHypnotized()) {
                processMiniTickSpawns(z, myZombies, myMap);
                combatManager.processMiniTickAbilities(z, myZombies, myMap);
            }
            processMiniTickMovement(z, myMap);
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
            try {
                int colIndex = (int) ((z.getX() - 6.47f) / Constants.Game.TILE_HEIGHT);
                currentTile = myMap.lanes.get(z.getCurrentRow()).tiles.get(colIndex);
            } catch (Exception e) {
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
