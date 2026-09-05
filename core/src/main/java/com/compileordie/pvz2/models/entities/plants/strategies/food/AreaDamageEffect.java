package com.compileordie.pvz2.models.entities.plants.strategies.food;

import com.badlogic.gdx.utils.Timer;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.projectiles.*;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Tile;
import com.compileordie.pvz2.models.user.Player;

import java.util.List;

public class AreaDamageEffect implements PlantFoodEffectStrategy {
    private final int damageAmount;

    public AreaDamageEffect(int damageAmount) {
        this.damageAmount = damageAmount;
    }

    @Override
    public void applyEffect(Plant plant, GameBoard board, Player player) {
        String name = plant.getName();

        if (name.equals("Bowling Bulb")) {
            plant.reloadAllBulbs();
            scheduleBowlingBulb(plant, board, 1.0f, 0, 40);
            scheduleBowlingBulb(plant, board, 2.0f, 1, 120);
            scheduleBowlingBulb(plant, board, 3.0f, 2, 180);

            Timer.schedule(new Timer.Task() {
                @Override
                public void run() { plant.resetFeed(); }
            }, 4.0f);
            return;
        }

        else if (name.equals("Cabbage-pult") || name.equals("Kernel-pult") || name.equals("Melon-pult") || name.equals("Winter Melon") || name.equals("Pepper-pult")) {

            final int pfDamage = plant.getBaseDamage() * (name.equals("Cabbage-pult") ? 4 : 2);

            List<Zombie> zList = board.getAllZombies().stream().filter(z -> !z.isDead()).collect(java.util.stream.Collectors.toList());
            java.util.Collections.shuffle(zList);

            int maxTargets = zList.size();
            if (name.equals("Cabbage-pult")) maxTargets = Math.min(7, zList.size());
            else if (name.equals("Pepper-pult")) maxTargets = Math.min(3, zList.size());

            final int limit = maxTargets;
            if (limit == 0) {
                plant.resetFeed();
                return;
            }

            final double splashRadius = (name.equals("Melon-pult") || name.equals("Winter Melon") || name.equals("Pepper-pult")) ? 1.5 : 0.0;

            final Class<? extends Projectile> pClass;
            if (name.equals("Kernel-pult")) pClass = ButterProjectile.class;
            else if (name.equals("Winter Melon")) pClass = IceLobbedProjectile.class;
            else if (name.equals("Pepper-pult")) pClass = FireLobbedProjectile.class;
            else pClass = LobbedProjectile.class;

            Timer.schedule(new Timer.Task() {
                int fired = 0;
                @Override
                public void run() {
                    if (plant.isDead() || fired >= limit) {
                        plant.resetFeed();
                        this.cancel();
                        return;
                    }

                    Zombie target = zList.get(fired);
                    if (!target.isDead()) {
                        double targetX = target.getX() - (Constants.Game.TILE_WIDTH * 0.45);
                        try {
                            Projectile proj = pClass.getDeclaredConstructor(double.class, double.class, double.class, double.class, int.class, int.class, double.class)
                                .newInstance(plant.getX(), target.getY(), targetX, 3.5, pfDamage, plant.getAoeDamage(), splashRadius);
                            proj.setSourcePlantType(PlantType.getByName(name));
                            board.getActiveProjectiles().add(proj);
                        } catch (Exception e) {}
                    }
                    fired++;
                }
            }, 0f, 0.3f, limit);
            return;
        }

        // --- 2. TANGLE KELP PULL ---
        else if (name.equals("Tangle Kelp")) {
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    if (plant.isDead()) return;
                    List<Zombie> wZombies = new java.util.ArrayList<>();
                    for (Zombie z : board.getAllZombies()) {
                        if (!z.isDead()) {
                            Tile t = board.getTile((float) z.getX(), (float) z.getY());
                            if (t != null && t.isUnderWater()) wZombies.add(z);
                        }
                    }
                    // --- NEW: FALLBACK IF YOU ARE TESTING ON LAND ---
                    if (wZombies.isEmpty()) {
                        wZombies.addAll(board.getAllZombies().stream().filter(z -> !z.isDead()).toList());
                    }
                    java.util.Collections.shuffle(wZombies);
                    int limit = Math.min(4, wZombies.size());
                    for (int i = 0; i < limit; i++) {
                        Zombie z = wZombies.get(i);
                        z.takeDamage(99999, DamageType.NORMAL, PlantType.TANGLE_KELP);

                        // SPAWN THE TANGLE KELP ATTACK ANIMATION ON TOP OF THE ZOMBIE!
                        spawnVisualHit(board, z.getX(), z.getY(), PlantType.TANGLE_KELP, true);
                    }
                    plant.resetFeed();
                }
            }, 1.5f);
            return;
        }

        // --- 3. MELEE PULSES ---
        else if (name.equals("Phat Beet") || name.equals("Kiwibeast")) {
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    if (plant.isDead()) return;
                    if (name.equals("Kiwibeast")) plant.forceMaxGrowth();

                    // 1. SPAWN THE MASSIVE PF PULSE ON THE PLANT!
                    spawnVisualHit(board, plant.getX(), plant.getY(), PlantType.getByName(name), true);

                    double radius = 1.5 * Constants.Game.TILE_HEIGHT;
                    for (Zombie z : board.getAllZombies()) {
                        if (!z.isDead() && Math.hypot(z.getX() - plant.getX(), z.getY() - plant.getY()) <= radius) {

                            z.takeDamage(900, DamageType.NORMAL, PlantType.getByName(name));

                            // 2. SPAWN THE TILE_HIT UNIQUELY UNDER THE ZOMBIE!
                            spawnTileHit(board, z.getX(), z.getY(), PlantType.getByName(name), true);
                        }
                    }
                    plant.resetFeed();
                }
            }, 1.5f);
            return;
        }

        else if (name.equals("Bonk Choy") || name.equals("Wasabi Whip")) {
            Timer.schedule(new Timer.Task() {
                int punches = 0;
                @Override
                public void run() {
                    if (plant.isDead()) { this.cancel(); return; }
                    double radius = 1.5 * Constants.Game.TILE_HEIGHT;
                    for (Zombie z : board.getAllZombies()) {
                        if (!z.isDead() && Math.hypot(z.getX() - plant.getX(), z.getY() - plant.getY()) <= radius) {
                            z.takeDamage(30, DamageType.NORMAL, PlantType.getByName(name));
                        }
                    }
                    punches++;
                    if (punches >= 30) {
                        plant.resetFeed();
                        this.cancel();
                    }
                }
            }, 0.5f, 0.08f, 30);
            return;
        }

        plant.resetFeed();
    }

    private void scheduleBowlingBulb(Plant plant, GameBoard board, float delay, int index, int dmg) {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (plant.isDead()) return;
                try {
                    // FIX 1: plant.template is null at runtime! Hardcode BouncingProjectile.class!
                    // FIX 2: Multiplied the dmg by 10 so the Plasma bulbs deal massive Plant Food damage!
                    Projectile p = BouncingProjectile.class
                        .getDeclaredConstructor(double.class, double.class, double.class, int.class, int.class)
                        .newInstance(plant.getX() + (index * 0.5 * Constants.Game.TILE_WIDTH), plant.getY(), 6.0, dmg * 10, 5);

                    p.setSourcePlantType(PlantType.BOWLING_BULB);
                    p.setXSpeed(6.0); // --- THIS FORCES THE PLANT FOOD GRAPHICS! ---
                    board.getActiveProjectiles().add(p);
                } catch (Exception e) {
                    System.err.println("❌ ERROR: Could not spawn Bowling Bulb PF Projectile!");
                    e.printStackTrace();
                }
            }
        }, delay);
    }

    // Creates an invisible dummy projectile that immediately dies, tricking the graphics engine into rendering an impact effect!
    public static void spawnVisualHit(GameBoard board, double x, double y, PlantType type, boolean isPF) {
        try {
            Projectile dummy = new NormalProjectile(x, y, isPF ? 6.0 : 0.0, 0); // 0 damage hides it!
            dummy.setXSpeed(isPF ? 6.0 : 0.0); // --- FIX: FORCES PLANT FOOD GRAPHICS! ---
            dummy.setSourcePlantType(type);
            board.getActiveProjectiles().add(dummy);
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() { dummy.destroy(); }
            }, 0.05f);
        } catch (Exception e) {}
    }

    // Creates a secondary dummy projectile flagged specifically for localized TILE_HIT graphics!
    public static void spawnTileHit(GameBoard board, double x, double y, PlantType type, boolean isPF) {
        try {
            Projectile dummy = new NormalProjectile(x, y, isPF ? 6.0 : 0.0, 0);
            dummy.setXSpeed(isPF ? 6.0 : 0.0);
            dummy.setSourcePlantType(type);
            dummy.setTileHit(true); // <-- THE DEDICATED FLAG
            board.getActiveProjectiles().add(dummy);
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() { dummy.destroy(); }
            }, 0.05f);
        } catch (Exception e) {}
    }

    public static void spawnPuddle(GameBoard board, double x, double y) {
        try {
            Projectile dummy = new NormalProjectile(x, y, 0.0, 0);
            dummy.setXSpeed(0.0);
            dummy.setPuddle(true);
            dummy.setSourcePlantType(PlantType.HOT_POTATO);
            board.getActiveProjectiles().add(dummy);

            com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                @Override
                public void run() { dummy.destroy(); }
            }, 0.05f);
        } catch (Exception e) {}
    }
}
