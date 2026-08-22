package com.compileordie.pvz2.views.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.enums.PlantCategory;
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

    // --- NEW: Memory Trackers for Hit Explosions ---
    private static class ProjectileHitTracker {
        float lastDrawX;
        float lastDrawY;
        PlantType type;
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
            float drawX = (float) ((plant.getX() + Constants.Game.PADDING_X_REALITY + (Constants.Game.TILE_WIDTH / 2.0f)) * Constants.UI.METER_TO_PIX);
            float drawY = (float) ((plant.getY() + Constants.Game.PADDING_Y_REALITY) * Constants.UI.METER_TO_PIX);

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
        if (name.equals("Gold Bloom")) return "idle";
        if (name.equals("Sun-shroom")) return "plantfood_stage" + plant.getGrowthStage();
        if (name.equals("Rotobaga")) return "plantfood_on"; // Rotobaga only has this PF clip

        float onDuration = (name.equals("Primal Sunflower") || name.equals("Snow Pea")) ? 0.6f : 0.5f;

        if (state.currentClip == null || state.currentClip.equals("idle") ||
            (state.currentClip.startsWith("plantfood_on") && state.animTime < onDuration)) {
            return "plantfood_on";
        }
        return "plantfood";
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

        // --- ELECTRIC BLUEBERRY ---
        switch (name) {
            case "Electric Blueberry" -> {
                return isAttacking ? "attack" : "idle4_3";
            }

            case "Caulipower" -> {
                return isAttacking ? "attack" : "idle4_1";
            }

            // --- CITRON STATE MACHINE ---
            case "Citron" -> {
                double timer = plant.getCurrentActionTimer();
                double maxInterval = plant.getActionIntervalTicks();

                if (timer < 10.0) return "attack";           // Just shot!
                if (timer < 30.0) return "recovery";         // Cooldown after shot
                if (timer < maxInterval) return "charge";    // Charging up the plasma
                if (plant.holdAction) return "idle";         // Fully charged, waiting for zombie
                return "idle";         // Fully charged, waiting for zombie
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

        // 1. Check Memory for Impact Explosions (Missing Projectiles)
        var iterator = trackedProjectiles.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Projectile, ProjectileHitTracker> entry = iterator.next();
            if (!currentProjs.contains(entry.getKey())) {
                // If a Citron orb disappears, spawn the explosion hit effect!
                if (entry.getValue().type == PlantType.CITRON) {
                    hitAnims.add(new HitAnim(entry.getValue().lastDrawX, entry.getValue().lastDrawY,
                        "768/FULL/EFFECTS/T_CITRON_CITRUS_ORB_HIT/T_CITRON_CITRUS_ORB_HIT.PAM", "animation3"));
                }
                else if (entry.getValue().type == PlantType.ELECTRIC_BLUEBERRY) {
                    hitAnims.add(new HitAnim(entry.getValue().lastDrawX, entry.getValue().lastDrawY,
                        "768/INITIAL/EFFECTS/ELECTRICBLUEBERRY_CLOUD_PROJECTILE/ELECTRICBLUEBERRY_CLOUD_PROJECTILE.PAM", "attack"));
                }
                iterator.remove();
            }
        }

        // 2. Draw Active Projectiles
        for (Projectile proj : currentProjs) {
            String pamPath = getProjectilePamPath(proj);
            String clipName = getProjectileClipName(proj);
            if (pamPath == null) continue;

            float drawX = (float) ((proj.getX() + Constants.Game.PADDING_X_REALITY + (Constants.Game.TILE_WIDTH / 2.0f)) * Constants.UI.METER_TO_PIX);
            float drawY = (float) ((proj.getY() + Constants.Game.PADDING_Y_REALITY + (Constants.Game.TILE_HEIGHT * 0.20f)) * Constants.UI.METER_TO_PIX);

            // Save its location in memory just in case it explodes on the next tick!
            ProjectileHitTracker tracker = trackedProjectiles.computeIfAbsent(proj, p -> new ProjectileHitTracker());
            tracker.lastDrawX = drawX;
            tracker.lastDrawY = drawY;
            tracker.type = proj.getSourcePlantType();

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
                // isLooping is false because explosions should only play once!
                player.draw(batch, hit.pamPath, hit.clipName, hit.animTime, hit.x, hit.y, 0.67f, 0.67f, false);
            } catch (Exception e) {}

            // The animation will cleanly remove itself from memory after 0.5 seconds
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
            default -> "768/INITIAL/EFFECTS/T_PEA_PROJECTILE/T_PEA_PROJECTILE.PAM";
        };
    }

    // --- NEW: Dynamic Clip Fetcher for traveling projectiles ---
    private String getProjectileClipName(Projectile proj) {
        PlantType source = proj.getSourcePlantType();

        if (source == PlantType.CITRON) {
            double x = proj.getX();
            // Approximating distance based on absolute world X (0 to 9 tiles)
            if (x < 3.5) return "Citron_Citrus_Orb";
            if (x < 6.5) return "Citron_Citrus_Orb2";
            return "Citron_Citrus_Orb3";
        }

        if (source == PlantType.ELECTRIC_BLUEBERRY) {
            return "idle2";
        }

        if (source == PlantType.CAULIPOWER) {
            return "animation3";
        }

        return "animation"; // The fallback default for peas and normal projectiles
    }
}
