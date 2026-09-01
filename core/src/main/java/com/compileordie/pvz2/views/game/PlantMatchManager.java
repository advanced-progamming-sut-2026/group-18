package com.compileordie.pvz2.views.game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.enums.PlantCategory;
import com.compileordie.pvz2.models.entities.projectiles.*;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Lane;
import com.compileordie.pvz2.models.game.board.Tile;
import pvz.libpvz.pam.ClipRef;
import pvz.libpvz.pam.PamPlayer;
import java.util.*;

import static com.compileordie.pvz2.models.AppModel.player;

public class PlantMatchManager {
    private final GameRenderStates states;
    private final PlantAssetManager assetManager;
    private boolean isPaused;
    private float globalAnimTime = 0f;
    private static class ProjectileHitTracker {
        float lastDrawX;
        float lastDrawY;
        double startX;
        double distanceTraveled;
        PlantType type;
        boolean isButter = false;
        int lastPierceCount = -1;
        boolean isIgnited = false;
        boolean isBlueFire = false;
        boolean isPlantFood = false;
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
    private final Set<Plant> trackedExplosives = new HashSet<>();
    private final Map<Plant, Double> actionTimerMemory = new HashMap<>();
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
        drawPlants(batch, player, board, delta);
        drawProjectiles(batch, player, board, delta);
    }
    private void drawPlants(SpriteBatch batch, PamPlayer player, GameBoard board, float delta) {
        Set<Plant> currentExplosives = new HashSet<>();
        Set<Plant> currentAlivePlants = new HashSet<>();
        for (Lane lane : board.lanes) {
            for (Tile tile : lane.tiles) {

// --- FIX: RENDER GOO PUDDLE CARPET AS CONTINUOUS PUZZLE PIECES ---
                if (tile.puddleTimer > 0) {
                    // Elevated slightly to 0.3f so it aligns perfectly with the plant's base
                    float fy = (float) ((Constants.Game.PADDING_Y + (tile.row * Constants.Game.TILE_HEIGHT) + (Constants.Game.TILE_HEIGHT * 0.55f)) * Constants.UI.METER_TO_PIX);

                    boolean isBeginning = (tile.column == 0 || lane.tiles.get(tile.column - 1).puddleTimer <= 0);

                    // We draw TWO overlapping segments per tile to completely bridge the 0.67f scale gap!
                    // Segment 1: Left half of the tile (Gets the round "Head" if it's the start of the puddle)
                    float fx1 = (float) ((Constants.Game.PADDING_X + (tile.column * Constants.Game.TILE_WIDTH) + (Constants.Game.TILE_WIDTH * 0.25f)) * Constants.UI.METER_TO_PIX);
                    String clip1 = isBeginning ? "animation" : "animation2";
                    try { player.draw(batch, "768/INITIAL/EFFECTS/GOOPEASHOOTER_PLANTFOOD_TILE/GOOPEASHOOTER_PLANTFOOD_TILE.PAM", clip1, globalAnimTime, fx1, fy, 0.67f, 0.67f, true); } catch (Exception e) {}

                    // Segment 2: Right half of the tile (Always gets the rectangular "Body" to connect to the next tile)
                    float fx2 = (float) ((Constants.Game.PADDING_X + (tile.column * Constants.Game.TILE_WIDTH) + (Constants.Game.TILE_WIDTH * 0.75f)) * Constants.UI.METER_TO_PIX);
                    try { player.draw(batch, "768/INITIAL/EFFECTS/GOOPEASHOOTER_PLANTFOOD_TILE/GOOPEASHOOTER_PLANTFOOD_TILE.PAM", "animation2", globalAnimTime, fx2, fy, 0.67f, 0.67f, true); } catch (Exception e) {}
                }

                Plant plant = tile.plant;
                if (plant == null || !plant.isAlive() || plant.isHidden()) continue;
                currentAlivePlants.add(plant);

                if (plant.getCategory() == PlantCategory.EXPLOSIVE || plant.getName().equals("Explode-o-nut") || (plant.getName().equals("Torchwood") && plant.getLevel() >= 3) || plant.getName().equals("Ice-shroom")) {
                    currentExplosives.add(plant);
                }

                drawSinglePlant(batch, player, plant, delta);

                if (plant.isFed()) {
                    if (plant.getName().equals("Citron")) {
                        float t = (float) (plant.plantFoodTimer * Constants.Game.TIME_COEFFICIENT);
                        if (t < 5.0f) {
                            float cx = (float) (plant.getX() * Constants.UI.METER_TO_PIX);
                            float cy = (float) ((plant.getY() + Constants.Game.TILE_HEIGHT * 0.5f) * Constants.UI.METER_TO_PIX);
                            try { player.draw(batch, "768/FULL/EFFECTS/CITRON_PLANTFOOD_LIGHTNING_CHARGE/CITRON_PLANTFOOD_LIGHTNING_CHARGE.PAM", "Citron_Plantfood_Lightning_Charge", globalAnimTime, cx, cy, 0.67f, 0.67f, true); } catch (Exception e) {}
                        }
                    }
                    if (plant.getName().equals("Snow Pea") || plant.getName().equals("Fire Peashooter")) {
                        String pam = plant.getName().equals("Snow Pea") ? "768/INITIAL/EFFECTS/SNOWPEA_PLANTFOOD/SNOWPEA_PLANTFOOD.PAM" : "768/INITIAL/EFFECTS/FIREPEASHOOTER_FIRE/FIREPEASHOOTER_FIRE.PAM";
                        String clip = plant.getName().equals("Snow Pea") ? "plantfood_on" : "idle";
                        float rowY = (float) ((plant.getY() + (Constants.Game.TILE_HEIGHT * 0.2f)) * Constants.UI.METER_TO_PIX);
                        int startSegment = (tile.column + 1) * 3;
                        int totalSegments = board.totalCols * 3;
                        for (int i = startSegment; i < totalSegments; i++) {
                            float fx = (float) ((Constants.Game.PADDING_X + 0.5f + (i * (Constants.Game.TILE_WIDTH / 3.0f))) * Constants.UI.METER_TO_PIX);
                            try { player.draw(batch, pam, clip, globalAnimTime, fx, rowY, 0.67f, 0.67f, true); } catch (Exception e) {}
                        }
                    }
                }
            }
        }

        var explIter = trackedExplosives.iterator();
        while (explIter.hasNext()) {
            Plant p = explIter.next();
            if (!currentExplosives.contains(p)) {
                float ex = (float) (p.getX() * Constants.UI.METER_TO_PIX);
                float ey = (float) ((p.getY() + (Constants.Game.TILE_HEIGHT * 0.2f)) * Constants.UI.METER_TO_PIX);
                switch (p.getName()) {
                    case "Torchwood" -> hitAnims.add(new HitAnim(ex, ey, "768/INITIAL/PLANT/TORCHWOOD/TORCHWOOD.PAM", "explosion"));
                    case "Potato Mine" -> hitAnims.add(new HitAnim(ex, ey, "768/INITIAL/EFFECTS/POTATOMINE_EXPLOSION/POTATOMINE_EXPLOSION.PAM", "animation2"));
                    case "Primal Potato Mine" -> hitAnims.add(new HitAnim(ex, ey, "768/INITIAL/EFFECTS/PRIMAL_POTATOMINE_EXPLOSION/PRIMAL_POTATOMINE_EXPLOSION.PAM", "animation3"));
                    case "Cherry Bomb", "Explode-o-nut", "Bowling Explode-o-nut" -> hitAnims.add(new HitAnim(ex, ey, "768/FULL/EFFECTS/CHERRYBOMB_EXPLOSION_REAR/CHERRYBOMB_EXPLOSION_REAR.PAM", "explosion3"));
                    case "Grapeshot" -> hitAnims.add(new HitAnim(ex, ey, "768/INITIAL/EFFECTS/ESCAPEROOT_EXPLOSION_GRAPESHOT/ESCAPEROOT_EXPLOSION_GRAPESHOT.PAM", "animation"));
                    case "Jalapeno" -> {
                        for (int i = 0; i < 18; i++) {
                            float fireX = (float) ((Constants.Game.PADDING_X + 0.5f + (i * (Constants.Game.TILE_WIDTH / 2.0f))) * Constants.UI.METER_TO_PIX);
                            hitAnims.add(new HitAnim(fireX, ey, "768/INITIAL/EFFECTS/JALAPENO_FIRE/JALAPENO_FIRE.PAM", "idle"));
                        }
                    }
                    case "Ice-shroom" -> {
                        for (int r = 0; r < board.totalRows; r++) {
                            float rowY = (float) ((Constants.Game.PADDING_Y + (r * Constants.Game.TILE_HEIGHT) + (Constants.Game.TILE_HEIGHT / 2.0f)) * Constants.UI.METER_TO_PIX);
                            for (int c = 0; c < board.totalCols; c++) {
                                float iceX = (float) ((Constants.Game.PADDING_X + (c * Constants.Game.TILE_WIDTH) + (Constants.Game.TILE_WIDTH / 2.0f)) * Constants.UI.METER_TO_PIX);
                                hitAnims.add(new HitAnim(iceX, rowY, "768/FULL/EFFECTS/ICESHROOM_MELEE_ATTACK/ICESHROOM_MELEE_ATTACK.PAM", "animation"));
                            }
                        }
                    }
                }
                explIter.remove();
            }
        }
        trackedExplosives.addAll(currentExplosives);
    }
    private void drawSinglePlant(SpriteBatch batch, PamPlayer player, Plant plant, float delta) {
        GameRenderStates.PlantRenderState state = states.plantRenderStates.computeIfAbsent(plant, p -> new GameRenderStates.PlantRenderState());
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
            Matrix4 scaled = original.cpy().translate(drawX, drawY, 0).scale(0.73f, 0.73f, 1f).translate(-drawX, -drawY, 0);
            batch.setTransformMatrix(scaled);
            assetManager.drawPlant(batch, currentClipRef, state.animTime, drawX, drawY, true);
            batch.setTransformMatrix(original);
// --- ICE BLOCK RENDERER ---
            if (plant.hasActiveCover() && plant.isFrozen()) {
                Map<String, Boolean> iceVis = new HashMap<>();

                // Hide all damage layers by default to prevent overlapping
                iceVis.put("ice_block_damage2", false);
                iceVis.put("ice_block_damage3", false);
                iceVis.put("ice_block_damage4", false);
                iceVis.put("ice_block_damage5", false);

                // Calculate the 600 HP ratio and override the correct damage part to true
                double hpRatio = plant.getCoverHp() / 600.0;
                if (hpRatio <= 0.25) {
                    iceVis.put("ice_block_damage5", true);
                } else if (hpRatio <= 0.50) {
                    iceVis.put("ice_block_damage4", true);
                } else if (hpRatio <= 0.75) {
                    iceVis.put("ice_block_damage3", true);
                } else if (hpRatio < 1.0) {
                    iceVis.put("ice_block_damage2", true);
                }

                float coverX = (float) (plant.getX() * Constants.UI.METER_TO_PIX);
                float coverY = (float) (plant.getY() * Constants.UI.METER_TO_PIX);

                try {
                    // Call the overload with per-part show/hide
                    player.draw(batch, "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_PLANT/FROSTBITE_ICE_BLOCK_PLANT.PAM", "freeze_idle", globalAnimTime, coverX, coverY, true, iceVis);
                } catch (Exception e) {}
            }
        }
    }
    private String determinePlantClip(Plant plant, GameRenderStates.PlantRenderState state) {
        String name = plant.getName();
        if (plant.isFed()) {
            String pfClip = PlantFoodClipManager.getClip(name, plant);
            if (pfClip != null) return pfClip;
        }
        if (name.equals("Torchwood")) {
            if (plant.isFed()) return "plantfood_on"; // The activation clip
            return plant.isBlueFlame() ? "plantfood" : "idle"; // Permanent powered-up idle!
        }
        if (name.toLowerCase().endsWith("mint")) {
            return getMintClip(name, plant, state);
        }
        if (plant.getCategory() == PlantCategory.SUN_PRODUCER) {
            return getSunProduceClip(name, plant);
        }
        if (plant.getCategory() == PlantCategory.SHOOTER || plant.getCategory() == PlantCategory.HOMING || plant.getCategory() == PlantCategory.STRIKE_THROUGH || plant.getCategory() == PlantCategory.LOBBER) {
            return getShooterClip(name, plant, state);
        }
        if (plant.getCategory() == PlantCategory.EXPLOSIVE) {
            return getExplosiveClip(name, plant, state);
        }
        if (plant.getCategory() == PlantCategory.MELEE) {
            return getMeleeClip(name, plant, state);
        }
        if (plant.getCategory() == PlantCategory.WALL_NUT) {
            return getDefensiveClip(name, plant, state);
        }
        return "idle";
    }
    private String getMintClip(String name, Plant plant, GameRenderStates.PlantRenderState state) {
        if (plant.windupTimer < 33.0) {
            return "intro";
        }
        if (plant.isWindingUp) {
            return "loop";
        }
        return "outro";
    }
    private String getDefensiveClip(String name, Plant plant, GameRenderStates.PlantRenderState state) {
        if (name.equals("Wall-nut")) {
            double hpRatio = (double) plant.getCurrentHp() / plant.getBaseHp();
            if (hpRatio > 0.75) return "idle";
            if (hpRatio > 0.50) return "damage";
            if (hpRatio > 0.25) return "damage2";
            return "damage3";
        }
        if (name.equals("Tall-nut")) {
            double hpRatio = (double) plant.getCurrentHp() / plant.getBaseHp();
            if (hpRatio > 0.66) return "idle";
            if (hpRatio > 0.33) return "damage";
            return "damage2";
        }
        if (name.equals("Sweet Potato")) {
            double hpRatio = (double) plant.getCurrentHp() / plant.getBaseHp();
            if (hpRatio > 0.75) return "idle";
            if (hpRatio > 0.50) return "idle_damage";
            if (hpRatio > 0.25) return "idle_damage2";
            return "idle_damage3";
        }
        if (name.equals("Explode-o-nut")) {
            double hpRatio = (double) plant.getCurrentHp() / plant.getBaseHp();
            if (hpRatio > 0.75) return "idle3";
            if (hpRatio > 0.50) return "damage";
            if (hpRatio > 0.25) return "damage2";
            return "damage3";
        }
        if (name.equals("Sun Bean")) {
            if (plant.getCurrentHp() > plant.getBaseHp()) return "plantfood";
            return "idle";
        }
        if (name.equals("Garlic")) {
            double hpRatio = (double) plant.getCurrentHp() / plant.getBaseHp();
            if (hpRatio > 0.66) return "idle2";
            if (hpRatio > 0.33) return "idle2_damage";
            return "idle2_damage2";
        }
        if (name.equals("Endurian")) {
            double hpRatio = (double) plant.getCurrentHp() / plant.getBaseHp();
            boolean isAttacking = plant.isWindingUp;
            if (hpRatio > 0.75) return isAttacking ? "attack_loop" : "idle2";
            if (hpRatio > 0.50) return isAttacking ? "attack_loop_damage" : "damage";
            if (hpRatio > 0.25) return isAttacking ? "attack_loop_damage2" : "damage2";
            return isAttacking ? "attack_loop_damage2" : "damage3";
        }
        return "idle";
    }
    private String getMeleeClip(String name, Plant plant, GameRenderStates.PlantRenderState state) {
        if (name.equals("Bonk Choy") || name.equals("Wasabi Whip")) {
            if (plant.holdAction) return "idle3";
            if (plant.windupTimer == 3.0) return "attack3";
            if (plant.windupTimer == 2.0) return "attack2";
            return "attack";
        }

        if (name.equals("Chomper")) {
            // 1. IS HE BITING? (The 1-second Windup)
            if (plant.isWindingUp) {
                double windupSec = plant.windupTimer / 60.0;
                if (windupSec < 0.5) return "bite";
                return "bite_end";
            }

            // 2. IS HE DIGESTING? (The 40-second Action Timer)
            double timer = plant.getCurrentActionTimer();
            double interval = plant.getActionIntervalTicks();

            // If timer has reached 40s, he is ready to bite again!
            if (plant.holdAction || timer >= interval) return "idle3";

            // He is digesting!
            double elapsedSec = timer / 60.0;

            if (elapsedSec < 0.5) return "special";
            if (timer > interval - 63.0) return "special_end";
            return "special_idle"; // 40 seconds of chewing!
        }
        if (name.equals("Phat Beet")) {
            if (plant.holdAction) return "idle2";
            if (plant.getCurrentActionTimer() < 25.0) return "attack";
            return "idle2";
        }
        if (name.equals("Kiwibeast")) {
            int stage = plant.getGrowthStage();
            double age = plant.getAgeTicks();
            if (age >= 480 && age < 491) return "growth_stage1";
            if (age >= 1440 && age < 1451) return "growth_stage2";
            if (!plant.holdAction && plant.getCurrentActionTimer() < 25.0) {
                if (stage == 1) return "attack_stage1";
                else if (stage == 2) return "attack_stage2";
                else return "attack_stage3";
            }
            if (stage == 1) return "idle_stage1_3";
            if (stage == 2) return "idle_stage2_3";
            return "idle_stage3_3";
        }
        return "idle";
    }
    private String getSunProduceClip(String name, Plant plant) {
        if (name.equals("Gold Bloom")) return "attack";
        if (plant.isWindingUp) {
            if (name.equals("Sun-shroom")) return "special_stage" + plant.getGrowthStage();
            return "special";
        }
        if (name.equals("Sun-shroom")) return "idle2_stage" + plant.getGrowthStage();
        return "idle";
    }
    private String getShooterClip(String name, Plant plant, GameRenderStates.PlantRenderState state) {
        boolean isAttacking = plant.isWindingUp;
        switch (name) {
            case "Fume-shroom" -> { return isAttacking ? "special" : "idle2"; }
            case "Puff-shroom" -> { return isAttacking ? "special_stage1" : "idle2_stage1"; }
            case "Mega Gatling Pea" -> { return isAttacking ? "attack_stage2" : "idle_stage2"; }
            case "Goo Peashooter" -> { return isAttacking ? "attack" : "idle3"; }
            case "Cactus" -> {
                double timer = plant.getCurrentActionTimer();
                if (isAttacking || timer < 15.0) return plant.isBlueFlame() ? "attack_plantfood" : "attack";
                return plant.isBlueFlame() ? "idle_plantfood3" : "idle3";
            }
            case "Cabbage-pult", "Melon-pult", "Winter Melon", "Pepper-pult" -> {
                double timer = plant.getCurrentActionTimer();
                if (isAttacking) return "attack";
                if (timer < 15.0) return "attack";
                if (name.equals("Cactus") || name.equals("Pepper-pult")) return "idle3";
                if (name.equals("Cabbage-pult")) return "idle2";
                return "idle";
            }
            case "Kernel-pult" -> {
                double timer = plant.getCurrentActionTimer();
                if (isAttacking) return plant.isFiringButter ? "attack2" : "attack";
                if (timer < 15.0) return plant.isFiringButter ? "attack2" : "attack";
                return "idle";
            }
            case "Starfruit", "Fire Peashooter" , "Sea-shroom" -> { return isAttacking ? "attack" : "idle2"; }
            case "Cat-tail" -> {
                if (isAttacking || plant.getCurrentActionTimer() < 15.0) return "attack";
                return "idle2";
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
            case "Electric Blueberry" -> { return isAttacking ? "attack" : "idle4_3"; }
            case "Caulipower" -> { return isAttacking ? "attack" : "idle4_1"; }
            case "Citron" -> {
                double timer = plant.getCurrentActionTimer();
                double maxInterval = plant.getActionIntervalTicks();
                double age = plant.getAgeTicks();
                if (age < maxInterval) {if (age < 70.0) return "charge";}
                if (timer < 7.0) return "attack";
                if (timer < 20.0) return "recovery";
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
        if (isAttacking) return "attack";
        return "idle";
    }
    private String getExplosiveClip(String name, Plant plant, GameRenderStates.PlantRenderState state) {
        if (name.equals("Potato Mine") || name.equals("Primal Potato Mine")) {
            if (plant.isWindingUp) return "attack";
            if (plant.getAgeTicks() < 10.0) return "plant";
            if (!plant.isArmed()) return "plant_idle";
            if (state.currentClip != null && state.currentClip.equals("plant_idle") && plant.isArmed()) {
                return "recover";
            }
            if (state.currentClip != null && state.currentClip.equals("recover")) {
                if (state.animTime < 0.75f) return "recover";
                return "idle";
            }
            return "idle";
        }
        if (name.equals("Squash")) {
            if (plant.isExhausted()) return "idle";
            if (plant.isWindingUp) return "turn";
            return "idle";
        }
        if (name.equals("Cherry Bomb") || name.equals("Grapeshot") || name.equals("Jalapeno")) {
            if (plant.windupTimer < 10.0) return "idle";
            return name.equals("Grapeshot") ? "attack_t2" : "attack";
        }
        if (name.equals("Doom-shroom")) {
            if (plant.windupTimer < 40.0) return "stage3_idle2";
            return "stage3_explode";
        }
        if (name.equals("Tangle Kelp")) {
            if (!plant.isWindingUp) return "idle3";
            if (plant.windupTimer < 15.0) return "attack_submerge";
            else if (plant.windupTimer < 45.0) return "attack";
            else return "attack_emerge";
        }
        if (name.equals("Iceberg Lettuce")) {
            if (!plant.isWindingUp) return "idle";
            return "attack";
        }
        if (name.equals("Ice-shroom")) {
            if (plant.isWindingUp) return "attack";
            return "idle2";
        }
        return "idle";
    }
    private void drawProjectiles(SpriteBatch batch, PamPlayer player, GameBoard board, float delta) {
        Set<Projectile> currentProjs = new HashSet<>(board.getActiveProjectiles());
        var iterator = trackedProjectiles.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Projectile, ProjectileHitTracker> entry = iterator.next();
            if (!currentProjs.contains(entry.getKey())) {
                ProjectileHitTracker tracker = entry.getValue();
                String hitPath = getHitAnimPamPath(tracker);
                if (hitPath != null) {
                    String hitClip = getHitAnimClipName(tracker);
                    hitAnims.add(new HitAnim(tracker.lastDrawX, tracker.lastDrawY, hitPath, hitClip));
                }
                iterator.remove();
            }
        }
        for (Projectile proj : currentProjs) {
            ProjectileHitTracker tracker = trackedProjectiles.computeIfAbsent(proj, p -> {
                ProjectileHitTracker t = new ProjectileHitTracker();
                t.startX = proj.getX();
                return t;
            });
            tracker.distanceTraveled = Math.abs(proj.getX() - tracker.startX);
            tracker.type = proj.getSourcePlantType();
            tracker.isButter = (proj instanceof ButterProjectile);
            tracker.isIgnited = proj.isIgnited();
            tracker.isBlueFire = proj.isIgnited() && proj.getDamage() >= 60;
            if (tracker.type == PlantType.CITRON) {
                tracker.isPlantFood = proj.getDamage() >= 4000;
            }else if (tracker.type == PlantType.GOO_PEASHOOTER) {
                tracker.isPlantFood = proj.getDamage() >= 600; // Giant Goo Boulder!
            } else {
                tracker.isPlantFood = Math.abs(proj.getXSpeed()) >= 5.0 || Math.abs(proj.getYSpeed()) >= 5.0;
            }

            float widthOffset = Constants.Game.TILE_WIDTH / 2.0f;
            float heightOffset = 0.20f;
            PlantType sourcePlant = proj.getSourcePlantType();

            // --- FIX 1: ZERO OUT OFFSETS FOR DUMMIES SO THEY ALIGN PERFECTLY! ---
            if (proj.getDamage() == 0 || sourcePlant == PlantType.SQUASH) {
                widthOffset = 0f;
                heightOffset = 0f;
            } else if (sourcePlant == PlantType.PUFF_SHROOM || sourcePlant == PlantType.SEA_SHROOM) {
                heightOffset = 0.037f;
            } else if (sourcePlant == PlantType.STARFRUIT || sourcePlant == PlantType.ROTOBAGA || sourcePlant == PlantType.SPLIT_PEA) {
                widthOffset = 0f;
                heightOffset = 0.05f;
            }

            float drawX = (float) ((proj.getX() + widthOffset) * Constants.UI.METER_TO_PIX);
            float lobAltitude = (proj instanceof LobbedProjectile) ? (float) ((LobbedProjectile) proj).altitude : 0f;
            float drawY = (float) ((proj.getY() + lobAltitude + (Constants.Game.TILE_HEIGHT * heightOffset)) * Constants.UI.METER_TO_PIX);

            // --- FIX 2: TRACK COORDINATES BEFORE NULL CHECK SO PULSES SPAWN CORRECTLY! ---
            tracker.lastDrawX = drawX;
            tracker.lastDrawY = drawY;

            String pamPath = getProjectilePamPath(proj, tracker);
            String clipName = getProjectileClipName(proj, tracker);
            if (pamPath == null) continue;

            if (proj instanceof PiercingProjectile) {
                int currentPierce = ((PiercingProjectile) proj).getPierceRemaining();
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
            } catch (Exception e) {}
        }
        var hitIter = hitAnims.iterator();
        while (hitIter.hasNext()) {
            HitAnim hit = hitIter.next();
            if (!isPaused) hit.animTime += delta;
            try {
                player.draw(batch, hit.pamPath, hit.clipName, hit.animTime, hit.x, hit.y, 0.67f, 0.67f, false);
            } catch (Exception e) {}

            // CRITICAL FIX: Increased from 1.0f to 3.0f so Tangle Kelp & Pulses don't vanish early!
            if (hit.animTime > 3.0f) {
                hitIter.remove();
            }
        }
    }
    private String getProjectilePamPath(Projectile proj, ProjectileHitTracker tracker) {
       // if (proj.getDamage() == 0) return null; // Hides all Dummy Projectiles instantly!
        PlantType source = proj.getSourcePlantType();
        // --- FIX: Restrict Giant Peas ONLY to the Pea Family! ---
        boolean isPeaPlant = source == PlantType.PEASHOOTER || source == PlantType.REPEATER || source == PlantType.THREEPEATER
            || source == PlantType.PEA_POD || source == PlantType.SPLIT_PEA || source == PlantType.MEGA_GATLING_PEA
            || source == PlantType.FIRE_PEASHOOTER || source == PlantType.SNOW_PEA;

        if (tracker.isPlantFood && source == PlantType.CITRON) return "768/FULL/EFFECTS/CITRON_PLANTFOOD_ORB/CITRON_PLANTFOOD_ORB.PAM";
        if (tracker.isPlantFood && source == PlantType.FUME_SHROOM) return "768/INITIAL/EFFECTS/FUMESHROOM_BUBBLES/FUMESHROOM_BUBBLES.PAM";
        if (isPeaPlant && proj.getDamage() >= 400) {
            if (source == PlantType.PEA_POD || source == PlantType.MEGA_GATLING_PEA) return "768/FULL/EFFECTS/PEAPOD_PLANTFOOD_GIANTPEA/PEAPOD_PLANTFOOD_GIANTPEA.PAM";
            return "768/INITIAL/EFFECTS/REPEATER_PLANTFOOD_GIANTPEA/REPEATER_PLANTFOOD_GIANTPEA.PAM";
        }
        if (tracker.isPlantFood && source == PlantType.STARFRUIT) {
            return "768/INITIAL/EFFECTS/STARFRUIT_PROJECTILE_PLANTFOOD/STARFRUIT_PROJECTILE_PLANTFOOD.PAM";
        }
        if (tracker.isPlantFood && source == PlantType.BOWLING_BULB) {
            return "768/FULL/EFFECTS/BOWLINGBULB_PLANTFOOD_PROJECTILE/BOWLINGBULB_PLANTFOOD_PROJECTILE.PAM";
        }
        if (proj.isIgnited()) {
            if (source == PlantType.SNOW_PEA) return "768/INITIAL/EFFECTS/T_PEA_PROJECTILE/T_PEA_PROJECTILE.PAM";
            return proj.getDamage() >= 60 ? "768/INITIAL/EFFECTS/T_FIRE_PEA_BLUE/T_FIRE_PEA_BLUE.PAM" : "768/INITIAL/EFFECTS/T_FIRE_PEA/T_FIRE_PEA.PAM";
        }
        if (source == PlantType.CACTUS) {
            return proj.getDamage() >= 200 ? "768/INITIAL/EFFECTS/CACTUS_PROJECTILE_PLANTFOOD/CACTUS_PROJECTILE_PLANTFOOD.PAM" : "768/INITIAL/EFFECTS/T_CACTUS_PROJECTILE/T_CACTUS_PROJECTILE.PAM";
        }
        if (source == null) return "768/INITIAL/EFFECTS/T_PEA_PROJECTILE/T_PEA_PROJECTILE.PAM";
        return switch (source) {
            case BOWLING_WALL_NUT -> "768/FULL/PLANT/TALLNUT/TALLNUT.PAM";
            case BOWLING_EXPLODE_O_NUT -> "768/INITIAL/PLANT/EXPLODEONUT/EXPLODEONUT.PAM";
            case GIANT_WALL_NUT -> "768/FULL/PLANT/PRIMAL_WALLNUT/PRIMAL_WALLNUT.PAM";
            case GARLIC -> "768/INITIAL/EFFECTS/GARLIC_PROJECTILE/GARLIC_PROJECTILE.PAM";
            case SQUASH -> "768/INITIAL/PLANT/SQUASH/SQUASH.PAM";
            case GRAPESHOT -> "768/INITIAL/EFFECTS/GRAPESHOT_PROJECTILE/GRAPESHOT_PROJECTILE.PAM";
            case SNOW_PEA -> "768/INITIAL/EFFECTS/T_SNOW_PEA/T_SNOW_PEA.PAM";
            case ROTOBAGA -> "768/FULL/EFFECTS/T_ROTORUTABAGA_PROJECTILE1/T_ROTORUTABAGA_PROJECTILE1.PAM";
            case CITRON ->  "768/FULL/EFFECTS/T_CITRON_CITRUS_ORB_HIT/T_CITRON_CITRUS_ORB_HIT.PAM";
            case CAULIPOWER -> "768/INITIAL/EFFECTS/CAULIPOWER_PROJECTILE/CAULIPOWER_PROJECTILE.PAM";
            case ELECTRIC_BLUEBERRY -> "768/INITIAL/EFFECTS/ELECTRICBLUEBERRY_CLOUD_PROJECTILE/ELECTRICBLUEBERRY_CLOUD_PROJECTILE.PAM";
            case CACTUS -> "768/INITIAL/EFFECTS/T_CACTUS_PROJECTILE/T_CACTUS_PROJECTILE.PAM";
            case FIRE_PEASHOOTER -> "768/INITIAL/EFFECTS/T_FIRE_PEA/T_FIRE_PEA.PAM";
            case STARFRUIT -> "768/INITIAL/EFFECTS/T_STARFRUIT_PROJECTILE/T_STARFRUIT_PROJECTILE.PAM";
            case GOO_PEASHOOTER -> tracker.isPlantFood ? "768/INITIAL/EFFECTS/GOOPEASHOOTER_PLANTFOOD/GOOPEASHOOTER_PLANTFOOD.PAM" : "768/INITIAL/EFFECTS/GOOPEASHOOTER_PROJECTILES/GOOPEASHOOTER_PROJECTILES.PAM";
            case MEGA_GATLING_PEA -> "768/INITIAL/EFFECTS/MEGAGATLING_PROJECTILE/MEGAGATLING_PROJECTILE.PAM";
            case SEA_SHROOM -> "768/FULL/EFFECTS/SEASHROOM_PROJECTILE/SEASHROOM_PROJECTILE.PAM";
            case PUFF_SHROOM -> "768/INITIAL/EFFECTS/T_PUFFSHROOM_PROJECTILE/T_PUFFSHROOM_PROJECTILE.PAM";
            case FUME_SHROOM -> "768/INITIAL/EFFECTS/FUMESHROOM_BUBBLES/FUMESHROOM_BUBBLES.PAM";
            case CABBAGE_PULT -> "768/INITIAL/EFFECTS/T_CABBAGEPULT_PROJECTILE/T_CABBAGEPULT_PROJECTILE.PAM";
            case KERNEL_PULT -> "768/INITIAL/EFFECTS/T_KERNALPULT_PROJECTILE/T_KERNALPULT_PROJECTILE.PAM";
            case MELON_PULT -> "768/INITIAL/EFFECTS/T_MELON_PROJECTILE/T_MELON_PROJECTILE.PAM";
            case WINTER_MELON -> "768/FULL/EFFECTS/T_WINTERMELON_PROJECTILE/T_WINTERMELON_PROJECTILE.PAM";
            case PEPPER_PULT -> "768/FULL/EFFECTS/T_PEPPERPULT_PROJECTILE/T_PEPPERPULT_PROJECTILE.PAM";
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
        if (tracker.isPlantFood && source == PlantType.CITRON) return "Plantfood_Citron_Plasma_Orb";
        if (tracker.isPlantFood && source == PlantType.FUME_SHROOM) return "plantfood";
        if (tracker.isPlantFood && source == PlantType.GOO_PEASHOOTER) return "animation";
        if (source == null) return "animation";
        double rawDist = tracker != null ? tracker.distanceTraveled : 0;
        double dist = rawDist / Constants.Game.TILE_WIDTH;
        if (source == PlantType.CABBAGE_PULT || source == PlantType.KERNEL_PULT || source == PlantType.MELON_PULT || source == PlantType.WINTER_MELON || source == PlantType.PEPPER_PULT) {
            double p = 0;
            if (proj instanceof LobbedProjectile) p = ((LobbedProjectile) proj).getProgress();
            if (p < 0.33) return "animation";
            if (p < 0.66) return "animation2";
            return "animation3";
        }
        if (source == PlantType.SQUASH && proj instanceof SquashProjectile) {
            SquashProjectile sq = (SquashProjectile) proj;
            boolean goingRight = sq.getSqTargetX() >= sq.getSqStartX();
            double p = sq.getSqProgress();
            if (goingRight) {
                return p < 0.5 ? "jump_up_right" : "jump_down_right";
            } else {
                if (p < 0.1) return "turn";
                return p < 0.5 ? "jump_up_left" : "jump_down_left";
            }
        }
        if (source == PlantType.BOWLING_WALL_NUT ||
            source == PlantType.BOWLING_EXPLODE_O_NUT ||
            source == PlantType.GIANT_WALL_NUT) {
            return "idle";
        }
        if (source == PlantType.CACTUS && proj.getDamage() >= 200) return "idle";
        if (source == PlantType.GARLIC) return "animation";
        if (source == PlantType.FUME_SHROOM) return "special";
        if (source == PlantType.PUFF_SHROOM) {
            if (dist < 2.0) return "animation";
            if (dist < 4.0) return "animation2";
            return "animation3";
        }
        if (source == PlantType.SEA_SHROOM) {
            if (dist < 3.0) return "animation";
            return "animation2";
        }
        if (source == PlantType.MEGA_GATLING_PEA) {
            if (dist < 6.0) return "animation";
            return "animation3";
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
        if (source == PlantType.PEASHOOTER || source == PlantType.REPEATER || source == PlantType.THREEPEATER || source == PlantType.PEA_POD || source == PlantType.SNOW_PEA || source == PlantType.FIRE_PEASHOOTER || source == PlantType.STARFRUIT) {
            if (dist < 3.5) return "animation";
            if (dist < 6.5) return "animation2";
            return "animation3";
        }
        if (source == PlantType.GRAPESHOT && proj instanceof GrapeProjectile) {
            GrapeProjectile gp = (GrapeProjectile) proj;
            double vx = gp.getGrapeXSpeed();
            double vy = gp.getGrapeYSpeed();
            if (Math.abs(vx) > Math.abs(vy)) return vx > 0 ? "animation_forward" : "animation_backward";
            else return vy > 0 ? "animation_verticle_up" : "animation_verticle_down";
        }
        return "animation";
    }
    private String getHitAnimPamPath(ProjectileHitTracker tracker) {
        PlantType source = tracker.type;
        if (source == null) return "768/INITIAL/EFFECTS/T_SPLAT_PEA/T_SPLAT_PEA.PAM";
        if (tracker.isIgnited) {
            if (source == PlantType.SNOW_PEA) return "768/INITIAL/EFFECTS/T_SPLAT_PEA/T_SPLAT_PEA.PAM";
            return tracker.isBlueFire ? "768/INITIAL/EFFECTS/SPLAT_FIRE_PEA_BLUE/SPLAT_FIRE_PEA_BLUE.PAM" : "768/INITIAL/EFFECTS/T_SPLAT_FIRE_PEA/T_SPLAT_FIRE_PEA.PAM";
        }
        return switch (source) {
            case BOWLING_BULB -> tracker.isPlantFood ? "768/FULL/EFFECTS/BOWLINGBULB_PLANTFOOD_PROJECTILE/BOWLINGBULB_PLANTFOOD_PROJECTILE.PAM" : null;
            case BOWLING_WALL_NUT, GIANT_WALL_NUT, CAULIPOWER, SQUASH -> null;
            case BOWLING_EXPLODE_O_NUT -> "768/FULL/EFFECTS/CHERRYBOMB_EXPLOSION_REAR/CHERRYBOMB_EXPLOSION_REAR.PAM";
            case GRAPESHOT -> "768/INITIAL/EFFECTS/GRAPESHOT_HIT/GRAPESHOT_HIT.PAM";
            case PUFF_SHROOM -> "768/INITIAL/EFFECTS/T_PUFFSHROOM_HIT/T_PUFFSHROOM_HIT.PAM";
            case SEA_SHROOM -> "768/FULL/EFFECTS/SEASHOOTER_FX/SEASHOOTER_FX.PAM";
            case SNOW_PEA -> "768/INITIAL/EFFECTS/T_SPLAT_SNOW_PEA/T_SPLAT_SNOW_PEA.PAM";
            case ROTOBAGA -> tracker.isPlantFood ? "768/FULL/EFFECTS/T_ROTORUTABAGA_MUZZLE_BURST/T_ROTORUTABAGA_MUZZLE_BURST.PAM" : "768/FULL/EFFECTS/T_ROTORUTABAGA_PROJECTILE_HIT/T_ROTORUTABAGA_PROJECTILE_HIT.PAM";
            case CITRON -> tracker.isPlantFood ? "768/FULL/EFFECTS/CITRON_PLANTFOOD_ORB_HIT/CITRON_PLANTFOOD_ORB_HIT.PAM" : "768/FULL/EFFECTS/T_CITRON_CITRUS_ORB_HIT/T_CITRON_CITRUS_ORB_HIT.PAM";
            case CACTUS -> "768/INITIAL/EFFECTS/CACTUS_PROJECTILE_HIT/CACTUS_PROJECTILE_HIT.PAM";
            case FIRE_PEASHOOTER -> "768/INITIAL/EFFECTS/T_SPLAT_FIRE_PEA/T_SPLAT_FIRE_PEA.PAM";
            case STARFRUIT -> "768/INITIAL/EFFECTS/T_STARFRUIT_PROJECTILE_HIT/T_STARFRUIT_PROJECTILE_HIT.PAM";
            case ELECTRIC_BLUEBERRY -> "768/INITIAL/EFFECTS/ELECTRICBLUEBERRY_CLOUD_PROJECTILE/ELECTRICBLUEBERRY_CLOUD_PROJECTILE.PAM";
            case GOO_PEASHOOTER -> "768/INITIAL/EFFECTS/GOOPEASHOOTER_PROJECTILES/GOOPEASHOOTER_PROJECTILES.PAM";
            case FUME_SHROOM -> "768/INITIAL/EFFECTS/FUMESHROOM_BUBBLES_HIT/FUMESHROOM_BUBBLES_HIT.PAM";
            case CABBAGE_PULT -> "768/INITIAL/EFFECTS/SPLAT_CABBAGEPULT/SPLAT_CABBAGEPULT.PAM";
            case MELON_PULT -> "768/INITIAL/EFFECTS/T_SPLAT_MELONPULT/T_SPLAT_MELONPULT.PAM";
            case WINTER_MELON -> "768/FULL/EFFECTS/T_SPLAT_WINTERMELON/T_SPLAT_WINTERMELON.PAM";
            case PEPPER_PULT -> tracker.isPlantFood ? "768/FULL/EFFECTS/T_PEPPERPULT_PROJECTILE_SPLAT/T_PEPPERPULT_PROJECTILE_SPLAT.PAM" : "768/FULL/EFFECTS/T_PEPPERPULT_PROJECTILE_SPLAT/T_PEPPERPULT_PROJECTILE_SPLAT.PAM";
            case KERNEL_PULT -> tracker.isButter ? "768/INITIAL/EFFECTS/SPLAT_KERNALPULT_BUTTER/SPLAT_KERNALPULT_BUTTER.PAM" : "768/INITIAL/EFFECTS/SPLAT_KERNALPULT_KERNAL/SPLAT_KERNALPULT_KERNAL.PAM";
            case SUN_BEAN -> tracker.isPlantFood ? "768/FULL/EFFECTS/SUNBEAN_PLANTFOOD_EFFECT_OVERLAY1/SUNBEAN_PLANTFOOD_EFFECT_OVERLAY1.PAM" : null;
            // --- NEW: Dummy Triggers routed perfectly to their animations! ---
            case PHAT_BEET -> tracker.isPlantFood ? "768/FULL/EFFECTS/PHATBEETS_PF_PULSE/PHATBEETS_PF_PULSE.PAM" : "768/FULL/EFFECTS/PHATBEETS_ATTACK_PULSE/PHATBEETS_ATTACK_PULSE.PAM";
            case KIWIBEAST -> tracker.isPlantFood ? "768/INITIAL/EFFECTS/KIWIBEAST_PF_PULSE/KIWIBEAST_PF_PULSE.PAM" : "768/INITIAL/EFFECTS/KIWIBEAST_ATTACK_PULSE/KIWIBEAST_ATTACK_PULSE.PAM";
            case TANGLE_KELP -> "768/FULL/PLANT/TANGLEKELP/TANGLEKELP.PAM";

            case PEASHOOTER, REPEATER, THREEPEATER, PEA_POD, SPLIT_PEA -> "768/INITIAL/EFFECTS/T_SPLAT_PEA/T_SPLAT_PEA.PAM";
            default -> "768/INITIAL/EFFECTS/T_SPLAT_PEA/T_SPLAT_PEA.PAM";
        };
    }
    private String getHitAnimClipName(ProjectileHitTracker tracker) {
        PlantType source = tracker.type;
        if (source == null) return "animation";
        double dist = tracker.distanceTraveled;
        if (source == PlantType.PUFF_SHROOM) {
            if (dist < 2.0) return "animation";
            if (dist < 4.0) return "animation2";
            return "animation3";
        }
        if (tracker.isPlantFood && source == PlantType.CITRON) return "animation";
        if (source == PlantType.TANGLE_KELP) return "attack";
        if (source == PlantType.PHAT_BEET || source == PlantType.KIWIBEAST) return "animation";
        if (source == PlantType.ELECTRIC_BLUEBERRY) return "attack";
        if (source == PlantType.ROTOBAGA) return tracker.isPlantFood ? "animation3" : "animation";
        if (source == PlantType.SPLIT_PEA || source == PlantType.SEA_SHROOM || source == PlantType.FUME_SHROOM || source == PlantType.CABBAGE_PULT || source == PlantType.KERNEL_PULT) return "animation";
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
        if (source == PlantType.SUN_BEAN) return "animation";
        return "animation3";
    }
}
