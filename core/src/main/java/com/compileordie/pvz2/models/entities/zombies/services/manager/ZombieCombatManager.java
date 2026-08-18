package com.compileordie.pvz2.models.entities.zombies.services.manager;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.capable.HunterZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.capable.OctopusZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.capable.PianistZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.capable.RaZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.capable.TurquoiseZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.DodoRiderZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.MovementState;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Lane;
import com.compileordie.pvz2.models.game.economy.Sun;
import com.compileordie.pvz2.models.game.economy.SunType;

import java.util.*;

import static com.compileordie.pvz2.models.entities.zombies.services.manager.PlantEatabilityChecker.isEatable;
import static com.compileordie.pvz2.models.entities.zombies.services.manager.PlantVisibilityChecker.isVisible;

public class ZombieCombatManager {
    private static final Set<ZombieType> PIANOABLE_ZOMBIES = EnumSet.of(
        ZombieType.GARGANTUAR,
        ZombieType.OCTOPUS_ZOMBIE,
        ZombieType.PIANIST_ZOMBIE,
        ZombieType.RA_ZOMBIE,
        ZombieType.TURQUOISE_ZOMBIE,
        ZombieType.DODO_RIDER,
        ZombieType.SNORKEL_ZOMBIE,
        ZombieType.BARREL_ROLLER
    );

    float dt = Constants.Game.TIME_COEFFICIENT;
    double tileWidth = Constants.Game.TILE_WIDTH;
    double tileHeight = Constants.Game.TILE_HEIGHT;
    double smashDamage = 999999;

    public void combatTick(List<Zombie> myZombies, List<Plant> myPlants) {
        List<Zombie> zombiesCopy = new ArrayList<>(myZombies);
        List<Plant> plantsCopy = new ArrayList<>(myPlants);
        for (Zombie z : zombiesCopy) {
            for (Plant p : plantsCopy) {
                processPlantEating(z, p);
                processSpecialCombatAbilities(z, p);
            }
        }
    }

    public void projectileCollisionTick(GameBoard myMap) {
        List<Projectile> projectiles = new ArrayList<>(myMap.getActiveProjectiles());
        List<Zombie> zombies = new ArrayList<>(myMap.getAllZombies());
        for (int p = projectiles.size() - 1; p >= 0; p--) {
            Projectile proj = projectiles.get(p);
            if (proj.isDead()) continue;
            for (Zombie zombie : zombies) {
                if (zombie.isDead()) continue;
                if (proj.getRow() == zombie.getCurrentRow()) {
                    double distance = Math.abs(proj.getX() - zombie.getX());
                    if (distance <= tileWidth / 2.0) {
                        zombie.takeDamage(proj.getDamage(), proj.getType());
                        proj.destroy();
                        if (proj.isDead()) {
                            break;
                        }
                    }
                }
            }
        }
    }

    public void combatingTwoZombie(List<Zombie> myZombies) {
        List<Zombie> zombiesCopy = new ArrayList<>(myZombies);
        for (Zombie z : zombiesCopy) {
            for (Zombie z1 : zombiesCopy) {
                if (z == z1 || !(Math.abs(z.getY() - z1.getY()) <= tileHeight / 6 && Math.abs(z.getX() - z1.getX()) <= tileWidth / 6)) continue;
                if ((z.isHypnotized() && !z1.isHypnotized()) || (z1.isHypnotized() && !z.isHypnotized())) {
                    z.isCombatingWithHypnotized = true;
                    z1.isCombatingWithHypnotized = true;
                    z.takeDamage((z1.getType() == ZombieType.ALL_STAR ? smashDamage : z1.getAttackPower() * dt), DamageType.NORMAL, null);
                    z1.takeDamage((z.getType() == ZombieType.ALL_STAR ? smashDamage : z.getAttackPower() * dt), DamageType.NORMAL, null);
                }
            }
        }
    }

    public void processMiniTickAbilities(Zombie z, List<Zombie> myZombies, GameBoard myMap) {
        if (z.getType() == ZombieType.TURQUOISE_ZOMBIE) {
            if (((TurquoiseZombie) z).shouldWeSteal()) {
                myMap.economyManager.sunAmount = Math.max(myMap.economyManager.sunAmount - 25, 0);
                ((TurquoiseZombie) z).addStolen(25);
                ((TurquoiseZombie) z).stopSteal();
            }
            if (((TurquoiseZombie) z).shouldWeBackSun()) {
                for (int i = 0; i < ((TurquoiseZombie) z).totalStolenSuns / 50; i++) {
                    myMap.economyManager.suns.add(new Sun(z.getX(), z.getY(), SunType.NORMAL, false, (float) z.getY()));
                }
                ((TurquoiseZombie) z).stopBackSun();
            }
        }
        if (z.getType() == ZombieType.RA_ZOMBIE) {
            if (((RaZombie) z).shouldWeBackSun()) {
                for (int i = 0; i < ((RaZombie) z).stolenSunCount / 25; i++) {
                    myMap.economyManager.suns.add(new Sun(z.getX(), z.getY(), SunType.NORMAL, false, (float) z.getY()));
                }
                ((RaZombie) z).stopBackSun();
            }
        }
        if (z.getType() == ZombieType.PIANIST_ZOMBIE && ((PianistZombie) z).isPlaying()) {
            playingPiano(z, myZombies);
            ((PianistZombie) z).stopPlaying();
        }
        if (z.getType() == ZombieType.RA_ZOMBIE && ((RaZombie) z).shouldWeSteal()) stealingByRaZombie(z, myMap);
    }

    private void stealingByRaZombie(Zombie z, GameBoard map) {
        Iterator<Sun> iterator = map.economyManager.suns.iterator();
        while (iterator.hasNext()) {
            Sun sun = iterator.next();
            if (sun.type == SunType.NORMAL && sun.target == z && Math.abs(sun.getX() - (z.getX()-0.2)) <= tileWidth / 3 && Math.abs(sun.getY() - (z.getY()+1.3)) <= tileHeight / 3) {
                ((RaZombie) z).addStolen(25);
                sun.target = null;
                iterator.remove();
            } else if (sun.target == null && sun.type==SunType.NORMAL) {
                sun.target = z;
            }
        }
    }

    private void playingPiano(Zombie z, List<Zombie> myZombies) {
        int i = 0;
        List<Zombie> zombiesCopy = new ArrayList<>(myZombies);
        for (Zombie pied : zombiesCopy) {
            if (!PIANOABLE_ZOMBIES.contains(pied.getType()) && !pied.isEating && !pied.isCombatingWithHypnotized && !pied.isHypnotized() && Math.abs(z.getX()-pied.getX())<=4*tileWidth) {
                int currentRow = pied.getCurrentRow();
                int targetRow;
                float newY;
                if (Math.random() <= 0.5) {
                    int a = (currentRow>=1? -1 : +1);
                    targetRow = currentRow + a;
                    newY = (float) (pied.getY() + a*tileHeight);
                } else{
                    int a = (currentRow<=3? +1 : -1);
                    targetRow = currentRow + a;
                    newY = (float) (pied.getY() + a*tileHeight);
                }
                var lanes = AppModel.gameSession.gameBoard.lanes;
                if (targetRow >= 0 && targetRow < lanes.size()) {
                    Lane currentLane = lanes.get(currentRow);
                    Lane nextLane = lanes.get(targetRow);
                    if (currentLane.zombies.contains(pied)) {
                        currentLane.zombies.remove(pied);
                        pied.setY(newY);
                        pied.setCurrentRow(targetRow);
                        nextLane.zombies.add(pied);
                        i++;
                    }
                }
                if (i>=6) break;
            }
        }
    }

    private void processPlantEating(Zombie z, Plant p) {
        if (Math.abs(z.getY() - p.getY()) <= tileHeight / 6) {
            if (Math.abs(z.getX() - p.getX()) <= tileWidth / 6) {
                if ((z.getType() == ZombieType.DODO_RIDER && ((DodoRiderZombie) z).getState() == MovementState.FLYING) || !isEatable(p)) {
                } else {
                    z.isEating = true;
                    p.takeDamage((int) ((z.getType() == ZombieType.ALL_STAR ? smashDamage : z.getAttackPower() * dt)));
                }
            }
        }
    }

    private void processSpecialCombatAbilities(Zombie z, Plant p) {
        if (z.getType() == ZombieType.DODO_RIDER && (Math.abs(z.getY() - p.getY()) <= tileHeight / 6 && Math.abs(z.getX() - p.getX()) <= tileWidth / 1.7) && isVisible(p) && !p.hasActiveCover()) {
            ((DodoRiderZombie) z).onPlantCollisionWithHalfOfTileWidth(PlantType.getByName(p.getName()));
        }
        if (z.getType() == ZombieType.TURQUOISE_ZOMBIE && (Math.abs(z.getY() - p.getY()) <= tileHeight / 6 && Math.abs(z.getX() - p.getX()) <= tileWidth * 3.7) && isVisible(p) && !p.hasActiveCover()) {
            ((TurquoiseZombie) z).startStealing();
            if (((TurquoiseZombie) z).shouldWeLaser()) {
                p.takeDamage((int) smashDamage);
                ((TurquoiseZombie) z).stopLaser();
            }
        }
        if (z.getType() == ZombieType.EXPLORER_ZOMBIE && (Math.abs(z.getY() - p.getY()) <= tileHeight / 6 && Math.abs(z.getX() - p.getX()) <= tileWidth * 1) && isVisible(p)) {
            p.takeDamage((int) smashDamage);
        }
        if (z.getType() == ZombieType.HUNTER_ZOMBIE && (Math.abs(z.getY() - p.getY()) <= tileHeight / 6 && Math.abs(z.getX() - p.getX()) <= HunterZombie.ABILITY_RANGE) && isVisible(p) && !p.hasActiveCover()) {
            ((HunterZombie) z).setShouldAttack(true);
            if (((HunterZombie) z).getShouldShut()) {
                p.addChill();
                ((HunterZombie) z).setShouldShut(false);
                ((HunterZombie) z).startThrowAnimation();
            }
        }
        if (z.getType() == ZombieType.OCTOPUS_ZOMBIE && (Math.abs(z.getY() - p.getY()) <= tileHeight / 6 && Math.abs(z.getX() - p.getX()) <= OctopusZombie.ABILITY_RANGE) && isVisible(p) && !p.hasActiveCover()) {
            p.applyOctopus(400.0);
            ((OctopusZombie) z).startTossAnimation();
        }
    }
}
