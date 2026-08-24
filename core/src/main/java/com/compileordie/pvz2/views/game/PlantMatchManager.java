package com.compileordie.pvz2.views.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.enums.PlantCategory;
import com.compileordie.pvz2.models.entities.projectiles.LobbedProjectile;
import com.compileordie.pvz2.models.entities.projectiles.PiercingProjectile;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Lane;
import com.compileordie.pvz2.models.game.board.Tile;
import pvz.libpvz.pam.ClipRef;
import pvz.libpvz.pam.PamPlayer;

import java.util.*;

public class PlantMatchManager {

    private final GameRenderStates states;
    private final PlantAssetManager assetManager;
    private boolean isPaused;
    private float globalAnimTime = 0f;

    // --- UPGRADED: True Distance Trackers for Hit Explosions ---
    private static class ProjectileHitTracker {
        float lastDrawX;
        float lastDrawY;
        double startX;            // FIX: Locks in the spawn position!
        double distanceTraveled;  // FIX: Pure distance, fixes reverse projectiles!
        PlantType type;
        int lastPierceCount = -1; // FIX: Tracks Cactus midair hits!
    }

    private static class HitAnim {
        float x, y, animTime;
        String pamPath, clipName;
        HitAnim(float x, float y, String pamPath, String clipName) {
            this.x = x; this.y = y; this.pamPath = pamPath; this.clipName = clipName; this.animTime = 0f;
        }
    }

    private final Map<Projectile, ProjectileHitTracker> trackedProjectiles = new HashMap<>();
    private final List<HitAnim> hitAnims = new ArrayList<>();

    public PlantMatchManager(GameRenderStates states, PlantAssetManager assetManager) {
        this.states = states;
        this.assetManager = assetManager;
    }

    public void setPaused(boolean p) {
        this.isPaused = p;
    }

    public void draw(SpriteBatch batch, PamPlayer player, float delta) {
        if (AppModel.gameSession == null || AppModel.gameSession.gameBoard == null) return;
        GameBoard board = AppModel.gameSession.gameBoard;

        if (!isPaused) {
            globalAnimTime += delta;
        }

        drawPlants(batch, board, delta);
        drawProjectiles(batch, player, board, delta);
    }

    // ==========================================
    // PART 1: PLANTS
    // ==========================================
    private void drawPlants(SpriteBatch batch, GameBoard board, float delta) {
        for (Lane lane : board.lanes) {
            for (Tile tile : lane.tiles) {
                Plant plant = tile.plant;
                if (plant == null || !plant.isAlive()) continue;
                drawSinglePlant(batch, plant, delta);
            }
        }
    }

    private void drawSinglePlant(SpriteBatch batch, Plant plant, float delta) {
        GameRenderStates.PlantRenderState state = states.plantRenderStates.computeIfAbsent(
            plant, p -> new GameRenderStates.PlantRenderState());

        if (!isPaused && !plant.isFrozen()) {
            state.animTime += delta;
        }

        PlantType pType = null;
        try {
            String formattedName = plant.getName().toUpperCase().replace("-", "_").replace(" ", "_");
            pType = PlantType.valueOf(formattedName);
        } catch (IllegalArgumentException e) {
            pType = PlantType.getByName(plant.getName());
        }

        if (pType == null) return;

        String targetClip = determinePlantClip(plant, state);

        if (state.currentClip == null || !targetClip.equals(state.currentClip)) {
            state.currentClip = targetClip;
            state.animTime = 0f;
        }

        ClipRef currentClipRef = assetManager.loadPlantClip(pType, state.currentClip);
        if (currentClipRef != null) {
            float drawX = (float) (plant.getX() * Constants.UI.METER_TO_PIX);
            float drawY = (float) (plant.getY() * Constants.UI.METER_TO_PIX);

            Matrix4 original = batch.getTransformMatrix().cpy();
            Matrix4 scaled = original.cpy()
                .translate(drawX, drawY, 0)
                .scale(0.73f, 0.73f, 1f)
                .translate(-drawX, -drawY, 0);

            batch.setTransformMatrix(scaled);
            assetManager.drawPlant(batch, currentClipRef, state.animTime, drawX, drawY, true);
            batch.setTransformMatrix(original);
        }
    }

    private String determinePlantClip(Plant plant, GameRenderStates.PlantRenderState state) {
        String name = plant.getName();

        if (plant.getCategory() == PlantCategory.SUN_PRODUCER || name.equals("Gold Bloom")) {
            if (plant.isFed()) return getPlantFoodClip(name, plant, state);
            return getSunProduceClip(name, plant);
        }

        if (plant.getCategory() == PlantCategory.SHOOTER || plant.getCategory() == PlantCategory.HOMING) {
            return getShooterClip(name, plant, state);
        }

        return "idle";
    }

    private String getPlantFoodClip(String name, Plant plant, GameRenderStates.PlantRenderState state) {
        // As long as the engine says isFed is true, loop this animation!
        if (name.equals("Mega Gatling Pea")) {
            return "plantfood";
        }

        return "idle"; // Fallback for other plants until we implement them
    }

    private String getSunProduceClip(String name, Plant plant) {
        if (name.equals("Gold Bloom")) return "attack";

        double timeUntilAction = plant.getActionIntervalTicks() - plant.getCurrentActionTimer();

        if (name.equals("Sun-shroom")) {
            int stage = plant.getGrowthStage();
            float actionTicks = (stage == 3) ? 19.3f : 18.3f;
            if (timeUntilAction > 0 && timeUntilAction <= actionTicks) {
                return "special_stage" + stage;
            }
            return "idle2_stage" + stage;
        }

        float actionTicks = 0f;
        switch (name) {
            case "Sunflower": actionTicks = 20.0f; break;
            case "Twin Sunflower": actionTicks = 15.0f; break;
            case "Primal Sunflower": actionTicks = 17.0f; break;
        }

        if (timeUntilAction > 0 && timeUntilAction <= actionTicks) {
            return "special";
        }
        return "idle";
    }

    private String getShooterClip(String name, Plant plant, GameRenderStates.PlantRenderState state) {
        if (plant.isFed()) return getPlantFoodClip(name, plant, state);

        boolean isAttacking = !plant.holdAction && plant.getCurrentActionTimer() < 10.0;

        switch (name) {

            case "Fume-shroom" -> {
                return isAttacking ? "special" : "idle2";
            }

            case "Puff-shroom" -> {
                boolean isFumeAttacking = !plant.holdAction && plant.getCurrentActionTimer() < 40.0;
                return isFumeAttacking ? "special_stage1" : "idle2_stage1";
            }

            case "Mega Gatling Pea" -> {
                return isAttacking ? "attack_stage2" : "idle_stage2";
            }

            case "Goo Peashooter", "Cactus" -> {
                return isAttacking ? "attack" : "idle3";
            }

            case "Starfruit", "Fire Peashooter" , "Sea-shroom" , "Cabbage-pult" -> {
                return isAttacking ? "attack" : "idle2";
            }
            case "Bowling Bulb" -> {
                if (plant.isWindingUp) {
                    if (plant.currentlyFiringBulb == 3) return "special3";
                    if (plant.currentlyFiringBulb == 2) return "special2";
                    return "special";
                } else {
                    if (plant.bulbs.size() < 3) {
                        if (plant.getRegenThreshold() - plant.bulbRegenTimer < 40) {
                            if (!plant.bulbs.contains(1)) return "reload";
                            if (!plant.bulbs.contains(2)) return "reload2";
                            if (!plant.bulbs.contains(3)) return "reload3";
                        }
                    }
                    return "idle";
                }
            }
            case "Electric Blueberry" -> {
                return isAttacking ? "attack" : "idle4_3";
            }
            case "Caulipower" -> {
                return isAttacking ? "attack" : "idle4_1";
            }
            case "Citron" -> {
                double timer = plant.getCurrentActionTimer();
                double maxInterval = plant.getActionIntervalTicks();
                if (timer < 10.0) return "attack";
                if (timer < 30.0) return "recovery";
                if (timer < maxInterval) return "charge";
                if (plant.holdAction) return "idle";
                return "idle";
            }
            case "Pea Pod" -> {
                int stack = plant.getStackCount();
                if (stack < 1) stack = 1;
                if (stack > 5) stack = 5;

                if (isAttacking) {
                    return stack == 1 ? "attack" : "attack " + stack;
                } else {
                    return stack == 1 ? "idle" : "idle" + stack;
                }
            }
        }

        if (name.equals("Split Pea") && isAttacking) {
            if (plant.isShootingForward && plant.isShootingBackward) return "attack2";
            if (plant.isShootingBackward) return "attack3";
            return "attack";
        }

        if (isAttacking) {
            return "attack";
        }

        return "idle";
    }

    // ==========================================
    // PART 2: PROJECTILES & IMPACT EFFECTS
    // ==========================================
    private void drawProjectiles(SpriteBatch batch, PamPlayer player, GameBoard board, float delta) {
        Set<Projectile> currentProjs = new HashSet<>(board.getActiveProjectiles());

        // 1. Check Memory for Impact Explosions (When Projectiles Die)
        var iterator = trackedProjectiles.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Projectile, ProjectileHitTracker> entry = iterator.next();
            if (!currentProjs.contains(entry.getKey())) {
                ProjectileHitTracker tracker = entry.getValue();

                String hitPath = getHitAnimPamPath(tracker);
                // FIX: Bowling Bulb will return null to prevent fake pea explosions!
                if (hitPath != null) {
                    String hitClip = getHitAnimClipName(tracker);
                    hitAnims.add(new HitAnim(tracker.lastDrawX, tracker.lastDrawY, hitPath, hitClip));
                }
                iterator.remove();
            }
        }

        // 2. Draw Active Projectiles
        for (Projectile proj : currentProjs) {
            // FIX: Lock in startX the very first time the projectile appears!
            ProjectileHitTracker tracker = trackedProjectiles.computeIfAbsent(proj, p -> {
                ProjectileHitTracker t = new ProjectileHitTracker();
                t.startX = proj.getX();
                return t;
            });

            // Calculate true absolute distance traveled regardless of direction!
            tracker.distanceTraveled = Math.abs(proj.getX() - tracker.startX);
            tracker.type = proj.getSourcePlantType();

            String pamPath = getProjectilePamPath(proj);
            String clipName = getProjectileClipName(proj, tracker);
            if (pamPath == null) continue;

            float drawX = (float) ((proj.getX() + Constants.Game.PADDING_X + (Constants.Game.TILE_WIDTH / 2.0f)) * Constants.UI.METER_TO_PIX);

            // Standard height offset for tall shooters (Peashooters, Cactus, etc.)
            float heightOffset = 0.20f;

            // --- FIX: Drop the visual height for tiny shrooms! ---
            if (proj.getSourcePlantType() == PlantType.PUFF_SHROOM || proj.getSourcePlantType() == PlantType.SEA_SHROOM) {
                heightOffset = 0.037f; // Pulls it perfectly down to mouth level!
            }

// --- FIX: Apply the Parabolic Altitude to Lobbed Projectiles! ---
            float lobAltitude = (proj instanceof LobbedProjectile)
                ? (float) ((LobbedProjectile) proj).altitude
                : 0f;

            float drawY = (float) ((proj.getY() + lobAltitude + Constants.Game.PADDING_Y + (Constants.Game.TILE_HEIGHT * heightOffset)) * Constants.UI.METER_TO_PIX);

            tracker.lastDrawX = drawX;
            tracker.lastDrawY = drawY;

            // --- FIX: Cactus Piercing Mid-Air Hit Engine ---
            if (proj instanceof PiercingProjectile) {
                int currentPierce = ((PiercingProjectile) proj).getPierceRemaining();

                // If the pierce count dropped, it must have hit something this frame!
                if (tracker.lastPierceCount != -1 && currentPierce < tracker.lastPierceCount) {
                    String hitPath = getHitAnimPamPath(tracker);
                    if (hitPath != null) {
                        String hitClip = getHitAnimClipName(tracker);
                        hitAnims.add(new HitAnim(drawX, drawY, hitPath, hitClip));
                    }
                }
                tracker.lastPierceCount = currentPierce;
            }

            try {
                player.draw(batch, pamPath, clipName, globalAnimTime, drawX, drawY, 0.67f, 0.67f, true);
            } catch (Exception e) {
                com.badlogic.gdx.Gdx.app.error("PVZ-PROJECTILE", "Failed to render projectile: " + pamPath, e);
            }
        }

        // 3. Draw and Animate the Hit Explosions!
        var hitIter = hitAnims.iterator();
        while (hitIter.hasNext()) {
            HitAnim hit = hitIter.next();
            if (!isPaused) hit.animTime += delta;

            try {
                player.draw(batch, hit.pamPath, hit.clipName, hit.animTime, hit.x, hit.y, 0.67f, 0.67f, false);
            } catch (Exception e) {}

            if (hit.animTime > 1.0f) {
                hitIter.remove();
            }
        }
    }

    private String getProjectilePamPath(Projectile proj) {
        PlantType source = proj.getSourcePlantType();
        if (source == null) return "768/INITIAL/EFFECTS/T_PEA_PROJECTILE/T_PEA_PROJECTILE.PAM";

        return switch (source) {
            case SNOW_PEA -> "768/INITIAL/EFFECTS/T_SNOW_PEA/T_SNOW_PEA.PAM";
            case ROTOBAGA -> "768/FULL/EFFECTS/T_ROTORUTABAGA_PROJECTILE1/T_ROTORUTABAGA_PROJECTILE1.PAM";
            case CITRON -> "768/FULL/EFFECTS/T_CITRON_CITRUS_ORB/T_CITRON_CITRUS_ORB.PAM";
            case CAULIPOWER -> "768/INITIAL/EFFECTS/CAULIPOWER_PROJECTILE/CAULIPOWER_PROJECTILE.PAM";
            case ELECTRIC_BLUEBERRY -> "768/INITIAL/EFFECTS/ELECTRICBLUEBERRY_CLOUD_PROJECTILE/ELECTRICBLUEBERRY_CLOUD_PROJECTILE.PAM";
            case CACTUS -> "768/INITIAL/EFFECTS/T_CACTUS_PROJECTILE/T_CACTUS_PROJECTILE.PAM";
            case FIRE_PEASHOOTER -> "768/INITIAL/EFFECTS/T_FIRE_PEA/T_FIRE_PEA.PAM";
            case STARFRUIT -> "768/INITIAL/EFFECTS/T_STARFRUIT_PROJECTILE/T_STARFRUIT_PROJECTILE.PAM";
            case GOO_PEASHOOTER -> "768/INITIAL/EFFECTS/GOOPEASHOOTER_PROJECTILES/GOOPEASHOOTER_PROJECTILES.PAM";
            case MEGA_GATLING_PEA -> "768/INITIAL/EFFECTS/MEGAGATLING_PROJECTILE/MEGAGATLING_PROJECTILE.PAM";
            case SEA_SHROOM -> "768/FULL/EFFECTS/SEASHROOM_PROJECTILE/SEASHROOM_PROJECTILE.PAM";
            case PUFF_SHROOM -> "768/INITIAL/EFFECTS/T_PUFFSHROOM_PROJECTILE/T_PUFFSHROOM_PROJECTILE.PAM";
            case FUME_SHROOM -> "768/INITIAL/EFFECTS/FUMESHROOM_BUBBLES/FUMESHROOM_BUBBLES.PAM";
            case CABBAGE_PULT -> "768/INITIAL/EFFECTS/T_CABBAGEPULT_PROJECTILE/T_CABBAGEPULT_PROJECTILE.PAM";
            case BOWLING_BULB -> {
                int dmg = proj.getDamage();
                if (dmg >= 180) yield "768/FULL/EFFECTS/BOWLINGBULB_PROJECTILE3/BOWLINGBULB_PROJECTILE3.PAM";
                if (dmg >= 120) yield "768/FULL/EFFECTS/BOWLINGBULB_PROJECTILE2/BOWLINGBULB_PROJECTILE2.PAM";
                yield "768/FULL/EFFECTS/BOWLINGBULB_PROJECTILE1/BOWLINGBULB_PROJECTILE1.PAM";
            }
            default -> "768/INITIAL/EFFECTS/T_PEA_PROJECTILE/T_PEA_PROJECTILE.PAM";
        };
    }

    private String getProjectileClipName(Projectile proj, ProjectileHitTracker tracker) {
        PlantType source = proj.getSourcePlantType();
        if (source == null) return "animation";

        // --- FIX 1: Convert raw pixel distance into TILE distance! ---
        double rawDist = tracker != null ? tracker.distanceTraveled : 0;
        double dist = rawDist / Constants.Game.TILE_WIDTH;

        // --- FIX 2: Bind Cabbage-pult animations purely to the physics arc! ---
        if (source == PlantType.CABBAGE_PULT) {
            double p = 0;
            if (proj instanceof com.compileordie.pvz2.models.entities.projectiles.LobbedProjectile) {
                p = ((com.compileordie.pvz2.models.entities.projectiles.LobbedProjectile) proj).getProgress();
            }
            if (p < 0.33) return "animation";   // Going UP
            if (p < 0.66) return "animation2";  // Apex (FLAT)
            return "animation3";                // Going DOWN
        }

        if (source == PlantType.FUME_SHROOM) return "special";

        if (source == PlantType.PUFF_SHROOM) {
            if (dist < 2.0) return "animation";  // Tiles 0 to 2
            if (dist < 4.0) return "animation2"; // Tiles 2 to 4
            return "animation3";                 // Tiles 4 to 6
        }

        if (source == PlantType.SEA_SHROOM) {
            if (dist < 3.0) return "animation";
            return "animation2"; // Middle and end of the path!
        }

        if (source == PlantType.MEGA_GATLING_PEA) {
            if (dist < 6.0) return "animation";
            return "animation3"; // Last 1/3 of the path!
        }

        if (source == PlantType.GOO_PEASHOOTER) {
            if (dist < 3.5) return "projectile_t1";
            if (dist < 6.5) return "projectile_t2";
            return "projectile_t3";
        }

        if (source == PlantType.CITRON) {
            if (dist < 3.5) return "Citron_Citrus_Orb";
            if (dist < 6.5) return "Citron_Citrus_Orb2";
            return "Citron_Citrus_Orb3";
        }

        if (source == PlantType.CACTUS) {
            if (dist < 3.5) return "idle";
            if (dist < 6.5) return "idle2";
            return "idle3";
        }

        if (source == PlantType.ELECTRIC_BLUEBERRY) return "idle2";

        if (source == PlantType.CAULIPOWER) {
            if (dist < 3.5) return "animation";
            if (dist < 6.5) return "animation2";
            return "animation3";
        }

        if (source == PlantType.PEASHOOTER || source == PlantType.REPEATER ||
            source == PlantType.THREEPEATER || source == PlantType.PEA_POD ||
            source == PlantType.SNOW_PEA || source == PlantType.FIRE_PEASHOOTER || source == PlantType.STARFRUIT) {
            if (dist < 3.5) return "animation";
            if (dist < 6.5) return "animation2";
            return "animation3";
        }

        return "animation";
    }

    private String getHitAnimPamPath(ProjectileHitTracker tracker) {
        PlantType source = tracker.type;
        if (source == null) return "768/INITIAL/EFFECTS/T_SPLAT_PEA/T_SPLAT_PEA.PAM";

        return switch (source) {
            case BOWLING_BULB -> null; // FIX: Prevents fake pea splat for bouncy bulbs!
            case PUFF_SHROOM -> "768/INITIAL/EFFECTS/T_PUFFSHROOM_HIT/T_PUFFSHROOM_HIT.PAM";
            case SEA_SHROOM -> "768/FULL/EFFECTS/SEASHOOTER_FX/SEASHOOTER_FX.PAM";
            case SNOW_PEA -> "768/INITIAL/EFFECTS/T_SPLAT_SNOW_PEA/T_SPLAT_SNOW_PEA.PAM";
            case ROTOBAGA -> "768/FULL/EFFECTS/T_ROTORUTABAGA_PROJECTILE_HIT/T_ROTORUTABAGA_PROJECTILE_HIT.PAM";
            case CITRON -> "768/FULL/EFFECTS/T_CITRON_CITRUS_ORB_HIT/T_CITRON_CITRUS_ORB_HIT.PAM";
            case CACTUS -> "768/INITIAL/EFFECTS/CACTUS_PROJECTILE_HIT/CACTUS_PROJECTILE_HIT.PAM";
            case FIRE_PEASHOOTER -> "768/INITIAL/EFFECTS/T_SPLAT_FIRE_PEA/T_SPLAT_FIRE_PEA.PAM";
            case STARFRUIT -> "768/INITIAL/EFFECTS/T_STARFRUIT_PROJECTILE_HIT/T_STARFRUIT_PROJECTILE_HIT.PAM";
            case ELECTRIC_BLUEBERRY -> "768/INITIAL/EFFECTS/ELECTRICBLUEBERRY_CLOUD_PROJECTILE/ELECTRICBLUEBERRY_CLOUD_PROJECTILE.PAM";
            case GOO_PEASHOOTER -> "768/INITIAL/EFFECTS/GOOPEASHOOTER_PROJECTILES/GOOPEASHOOTER_PROJECTILES.PAM";
            case FUME_SHROOM -> "768/INITIAL/EFFECTS/FUMESHROOM_BUBBLES_HIT/FUMESHROOM_BUBBLES_HIT.PAM";
            case CABBAGE_PULT -> "768/INITIAL/EFFECTS/SPLAT_CABBAGEPULT/SPLAT_CABBAGEPULT.PAM";
            case PEASHOOTER, REPEATER, THREEPEATER, PEA_POD, SPLIT_PEA -> "768/INITIAL/EFFECTS/T_SPLAT_PEA/T_SPLAT_PEA.PAM";
            default -> "768/INITIAL/EFFECTS/T_SPLAT_PEA/T_SPLAT_PEA.PAM";
        };
    }

    private String getHitAnimClipName(ProjectileHitTracker tracker) {
        PlantType source = tracker.type;
        if (source == null) return "animation";

        // Pull the exact distance traveled at the moment of impact!
        double dist = tracker.distanceTraveled;

        if (source == PlantType.PUFF_SHROOM) {
            if (dist < 2.0) return "animation";
            if (dist < 4.0) return "animation2";
            return "animation3";
        }

        if (source == PlantType.ELECTRIC_BLUEBERRY) return "attack";

        if (source == PlantType.ROTOBAGA || source == PlantType.SPLIT_PEA
            || source == PlantType.SEA_SHROOM || source == PlantType.FUME_SHROOM || source == PlantType.CABBAGE_PULT) return "animation";

        if (source == PlantType.STARFRUIT) {
            if (dist < 3.5) return "idle";
            if (dist < 6.5) return "idle2";
            return "idle3";
        }
        if (source == PlantType.GOO_PEASHOOTER) {
            if (dist < 3.5) return "hit_t1";
            if (dist < 6.5) return "hit_t2";
            return "hit_t3";
        }

        if (dist < 3.5) return "animation";
        if (dist < 6.5) return "animation2";
        return "animation3";
    }
}
