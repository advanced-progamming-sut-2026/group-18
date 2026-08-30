package com.compileordie.pvz2.views.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.controllers.menus.game.GameScreenController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.LawnMower;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.Tomb;
import com.compileordie.pvz2.models.game.board.Tile;
import pvz.libpvz.pam.ClipRef;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

import java.util.HashSet;
import java.util.Set;

final class BoardEntityDrawer {

    private static final ZombieVisualRegistry.PamSpec MOWER_PAM_SPEC =
        new ZombieVisualRegistry.PamSpec("768/INITIAL/MOWERS/MOWER_{CH}/MOWER_{CH}.PAM");
    private static final ZombieVisualRegistry.PamSpec TOMB_PAM_SPEC =
        new ZombieVisualRegistry.PamSpec(
            ZombieVisualRegistry.getChapterTag().equals("EGYPT")
                ? "768/INITIAL/GRAVESTONES/EGYPT_HIEROGLYPH/EGYPT_HIEROGLYPH.PAM"
                : "768/FULL/GRAVESTONES/DARK_NOOP/DARK_NOOP.PAM");
    private static final String FIRE_TILE_PAM =
        "768/INITIAL/EFFECTS/FIREPEASHOOTER_FIRE/FIREPEASHOOTER_FIRE.PAM";
    private static final String FIRE_TILE_CLIP = "idle2";
    private static final String SUN_PAM = "768/INITIAL/EFFECTS/SUN/SUN.PAM";
    private static final String RADIOACTIVE_SUN_PAM = "768/FULL/EFFECTS/SUN_BOMB/SUN_BOMB.PAM";

    private final GameRenderStates states;
    private final Set<String> brokenAssets;
    private TextureRegion bgLeftRegion;
    private TextureRegion bgMainRegion;
    private TextureRegion bgRightRegion;
    private boolean isPaused;
    private NinePatch tileHighlightPatch;
    private TextureRegion shovelCursorRegion;
    private float cursorAnimTime = 0f;
    private TextureRegion plantFoodCursorRegion;
    private static final String CRATER_PAM = "768/FULL/EFFECTS/CRATER/CRATER.PAM";

    BoardEntityDrawer(GameRenderStates states, Set<String> brokenAssets) {
        this.states = states;
        this.brokenAssets = brokenAssets;
    }

    void setPaused(boolean p) { this.isPaused = p; }

    void setBackgroundRegions(TextureRegion left, TextureRegion main, TextureRegion right) {
        this.bgLeftRegion = left;
        this.bgMainRegion = main;
        this.bgRightRegion = right;
    }

    void drawBackground(SpriteBatch batch) {
        float screenW = Constants.UI.DEFAULT_WIDTH;
        float screenH = Constants.UI.DEFAULT_HEIGHT;
        if (bgLeftRegion != null && bgMainRegion != null && bgRightRegion != null) {
            float leftAspect = (float) bgLeftRegion.getRegionWidth() / bgLeftRegion.getRegionHeight();
            float leftWidth = (screenH * leftAspect) * GameScreenConstants.BG_SIDE_SCALE;
            batch.draw(bgLeftRegion, 0, 0, leftWidth, screenH);
            float rightAspect = (float) bgRightRegion.getRegionWidth() / bgRightRegion.getRegionHeight();
            float rightWidth = (screenH * rightAspect) * GameScreenConstants.BG_SIDE_SCALE;
            float visibleRightWidth = rightWidth * 0.25f;
            float rightX = screenW - visibleRightWidth;
            batch.draw(bgRightRegion, rightX, 0, rightWidth, screenH);
            float mainWidth = rightX - leftWidth;
            batch.draw(bgMainRegion, leftWidth, 0, mainWidth, screenH);
        } else if (bgMainRegion != null) {
            batch.draw(bgMainRegion, 0, 0, screenW, screenH);
        }
    }

    void drawMowers(SpriteBatch batch, PamPlayer player, float delta) {
        if (AppModel.gameSession == null || player == null) return;
        String resolvedPath = MOWER_PAM_SPEC.getResolvedPath();
        if (brokenAssets.contains(resolvedPath)) return;

        var lanes = AppModel.gameSession.gameBoard.lanes;
        for (int i = 0; i < lanes.size(); i++) {
            LawnMower mower = lanes.get(i).lawnMower;
            if (mower == null || !mower.isAlive()) continue;
            drawOneMower(batch, player, mower, resolvedPath, delta, i);
        }
    }

    private void drawOneMower(SpriteBatch batch, PamPlayer player, LawnMower mower,
                              String resolvedPath, float delta, int laneIndex) {
        GameRenderStates.MowerRenderState state =
            states.mowerRenderStates.computeIfAbsent(mower, m -> new GameRenderStates.MowerRenderState());
        boolean isIdle = Math.abs(mower.getX() - Constants.Game.X_OF_MOWER) < 1e-4;
        if (isIdle != state.wasIdle) {
            state.animTime = 0f;
            state.wasIdle = isIdle;
        }
        String animName = isIdle ? "idle" : "transition";
        if (isPaused) animName = "idle";
        state.animTime += delta;
        float drawX = (float) mower.getX() * Constants.UI.METER_TO_PIX;
        float drawY = (float) mower.getY() * Constants.UI.METER_TO_PIX;
        try {
            player.draw(batch, resolvedPath, animName, state.animTime, drawX, drawY,
                GameScreenConstants.MOWER_SCALE, GameScreenConstants.MOWER_SCALE, true);
        } catch (Throwable e) {
            brokenAssets.add(resolvedPath);
            Gdx.app.error("PVZ-ASSET-MISSING",
                "❌ [جلوگیری از کرش] بارگذاری/رندر ماشین چمن‌زن ناموفق بود! ردیف: "
                    + laneIndex + " مسیر: " + resolvedPath + " دلیل: " + e, e);
        }
    }

    void drawTombs(SpriteBatch batch, PamPlayer player, float delta) {
        if (AppModel.gameSession == null || player == null) return;
        String resolvedPath = TOMB_PAM_SPEC.getResolvedPath();
        if (brokenAssets.contains(resolvedPath)) return;

        for (var lane : AppModel.gameSession.gameBoard.lanes) {
            for (Tomb tomb : lane.tombs) {
                if (tomb.isDestroyed()) continue;
                drawOneTomb(batch, player, tomb, resolvedPath, delta);
            }
        }
    }

    private void drawOneTomb(SpriteBatch batch, PamPlayer player, Tomb tomb,
                             String resolvedPath, float delta) {
        GameRenderStates.TombRenderState state =
            states.tombRenderStates.computeIfAbsent(tomb, t -> new GameRenderStates.TombRenderState());
        if (tomb.takedDamage) {
            state.damageAlphaTimer = 0.15f;
            tomb.takedDamage = false;
        }
        if (state.damageAlphaTimer > 0) state.damageAlphaTimer -= delta;

        double ratio = tomb.getHealthRatio();
        String animName;
        if (ratio >= 1.0) animName = "undamaged";
        else if (ratio > 0.75) animName = "damage1";
        else if (ratio > 0.5) animName = "damage2";
        else if (ratio > 0.25) animName = "damage3";
        else animName = "damage4";

        float drawX = (float) (tomb.getPositionX()) * Constants.UI.METER_TO_PIX;
        float drawY = (float) (tomb.getPositionY() + Constants.Game.PADDING_Y_REALITY)
            * Constants.UI.METER_TO_PIX;
        state.lastDrawX = drawX;
        state.lastDrawY = drawY;

        Color oldColor = null;
        try {
            if (state.damageAlphaTimer > 0) {
                oldColor = batch.getColor().cpy();
                batch.setColor(new Color(1f, 1f, 1f, 0.6f));
            }
            player.draw(batch, resolvedPath, animName, 0f, drawX, drawY,
                GameScreenConstants.TOMB_SCALE, GameScreenConstants.TOMB_SCALE, true);
            if (oldColor != null) batch.setColor(oldColor);
        } catch (Throwable e) {
            brokenAssets.add(resolvedPath);
        } finally {
            if (state.damageAlphaTimer > 0) batch.setColor(Color.WHITE);
        }
    }

    void drawFireTiles(SpriteBatch batch, PamPlayer player, float delta) {
        if (AppModel.gameSession == null || player == null) return;
        if (brokenAssets.contains(FIRE_TILE_PAM)) return;

        Set<Tile> stillOnFire = new HashSet<>();
        for (var lane : AppModel.gameSession.gameBoard.lanes) {
            for (Tile tile : lane.tiles) {
                if (!tile.isOnFire) continue;
                stillOnFire.add(tile);
                float animTime = states.fireTileAnimTimes.getOrDefault(tile, 0f) + delta;
                states.fireTileAnimTimes.put(tile, animTime);
                float fireX = (float) ((tile.column * Constants.Game.TILE_WIDTH
                    + Constants.Game.PADDING_X_REALITY + 0.5) * Constants.UI.METER_TO_PIX);
                float fireY = (float) ((tile.row * Constants.Game.TILE_HEIGHT
                    + Constants.Game.PADDING_Y_REALITY + 0.5) * Constants.UI.METER_TO_PIX);
                try {
                    player.draw(batch, FIRE_TILE_PAM, FIRE_TILE_CLIP, animTime, fireX, fireY, true);
                } catch (Throwable e) {
                    brokenAssets.add(FIRE_TILE_PAM);
                    Gdx.app.error("PVZ-ASSET-MISSING",
                        "❌ رندر افکت آتیشِ خونه ناموفق بود. مسیر: " + FIRE_TILE_PAM
                            + " | کلیپ: " + FIRE_TILE_CLIP + " دلیل: " + e, e);
                }
            }
        }
        states.fireTileAnimTimes.keySet().removeIf(tile -> !stillOnFire.contains(tile));
    }

    void drawCraters(SpriteBatch batch, PamPlayer player, float delta) {
        if (AppModel.gameSession == null || player == null) return;
        if (brokenAssets.contains(CRATER_PAM)) return;

        for (var lane : AppModel.gameSession.gameBoard.lanes) {
            for (Tile tile : lane.tiles) {
                if (tile.obstacle instanceof com.compileordie.pvz2.models.entities.obstacles.Crater) {

                    // --- POSITION FIX: Added TILE_HEIGHT / 2.0f so it centers vertically! ---
                    float craterX = (float) ((Constants.Game.PADDING_X + (tile.column * Constants.Game.TILE_WIDTH) + (Constants.Game.TILE_WIDTH / 2.0f)) * Constants.UI.METER_TO_PIX);
                    float craterY = (float) ((Constants.Game.PADDING_Y + (tile.row * Constants.Game.TILE_HEIGHT) + (Constants.Game.TILE_HEIGHT / 2.0f)) * Constants.UI.METER_TO_PIX);

                    // --- SCALE FIX: 0.5f means half size. (Tweak to 0.4f or 0.6f if needed!) ---
                    float scale = 0.8f;

                    try {
                        // --- NEW: Using the full draw method to pass the scale! ---
                        player.draw(batch, CRATER_PAM, "", 0f, craterX, craterY, scale, scale, true);
                    } catch (Throwable e) {
                        brokenAssets.add(CRATER_PAM);
                        Gdx.app.error("PVZ-ASSET-MISSING", "❌ رندر گودال (Crater) با خطا مواجه شد: " + e.getMessage());
                    }
                }
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    void drawSuns(SpriteBatch batch, PamPlayer player, float delta, Vector3 mousePos) {
        if (AppModel.gameSession == null || player == null) return;
        var sunsList = AppModel.gameSession.gameBoard.economyManager.suns;
        if (sunsList == null || sunsList.isEmpty()) return;

        var iterator = sunsList.iterator();
        while (iterator.hasNext()) {
            var sun = iterator.next();
            GameRenderStates.SunRenderState state =
                states.sunRenderStates.computeIfAbsent(sun, s -> new GameRenderStates.SunRenderState());
            state.animTime += delta;

            // --- FIX: Removed Double-Padding! Sun coordinates are already world coordinates! ---
            float baseX = (float) ((sun.getX() + (Constants.Game.TILE_WIDTH / 2.0f)) * Constants.UI.METER_TO_PIX);
            float baseY = (float) (sun.getY() * Constants.UI.METER_TO_PIX);

            if (!state.isFading) {
                float dx = baseX - mousePos.x;
                float dy = baseY - mousePos.y;
                if (dx * dx + dy * dy <= 2500) state.isFading = true;
            }

            float alpha = 1f;
            if (state.isFading) {
                state.fadeTimer += delta;
                alpha = Math.max(0f, 1f - (state.fadeTimer / 0.5f));
                if (state.fadeTimer >= 0.3f) {
                    AppModel.gameSession.gameBoard.economyManager.sunAmount += sun.type.value;
                    iterator.remove();
                    states.sunRenderStates.remove(sun);
                    continue;
                }
            }

            String typeKey = sun.type.name();
            String pamPath = SUN_PAM;
            float scale = 1.0f;
            if (typeKey.contains("RADIOACTIVE")) pamPath = RADIOACTIVE_SUN_PAM;
            else if (typeKey.contains("SPECIAL")) scale = 1.5f;
            if (brokenAssets.contains(pamPath)) continue;

            Color oldColor = batch.getColor().cpy();
            try {
                batch.setColor(oldColor.r, oldColor.g, oldColor.b, alpha);
                player.draw(batch, pamPath, "animation", state.animTime, baseX, baseY,
                    scale * 0.8f, scale * 0.8f, true);
            } catch (Throwable e) {
                brokenAssets.add(pamPath);
                Gdx.app.error("PVZ-SUN", "❌ رندر خورشید خطا داد: " + e.getMessage());
            } finally {
                batch.setColor(oldColor);
            }
        }
    }

    void initSelectionAssets(TextureBank textureBank) {
        TextureRegion selectRegion = textureBank.region("IMAGE_UI_PACKETS_SELECT");
        if (selectRegion != null) {
            this.tileHighlightPatch = new NinePatch(selectRegion, 8, 8, 8, 8);
        }
        this.shovelCursorRegion = textureBank.region("IMAGE_UI_HUD_INGAME_SHOVEL_ICON");
        this.plantFoodCursorRegion = textureBank.region("IMAGE_UI_HUD_INGAME_PLANTFOOD_BUTTON");
    }

    void drawTileHighlight(SpriteBatch batch, Tile tile) {
        if (tile == null || tileHighlightPatch == null) return;
        if (GameScreenController.selectedCard == null
            && !GameScreenController.isShovelSelected
            && !GameScreenController.isPlantFoodSelected) return;

        float tileX = (tile.column * Constants.Game.TILE_WIDTH + Constants.Game.PADDING_X) * Constants.UI.METER_TO_PIX;
        float tileY = (tile.row * Constants.Game.TILE_HEIGHT + Constants.Game.PADDING_Y) * Constants.UI.METER_TO_PIX;
        float tileW = Constants.Game.TILE_WIDTH * Constants.UI.METER_TO_PIX;
        float tileH = Constants.Game.TILE_HEIGHT * Constants.UI.METER_TO_PIX;

        tileHighlightPatch.draw(batch, tileX, tileY, tileW, tileH);
    }

    void drawCursorFollower(SpriteBatch batch, PlantAssetManager plantAssets, Vector3 mousePos, float delta) {
        if (!isPaused) {
            cursorAnimTime += delta;
        }

        // 1. Plant Card Selected: Render Idle Animation under cursor
        if (GameScreenController.selectedCard != null && plantAssets != null) {
            PlantType plantType = GameScreenController.selectedCard.plantType;
            ClipRef clip = plantAssets.loadPlantClip(plantType);
            if (clip != null) {
                float drawX = mousePos.x;
                float drawY = mousePos.y;

                Matrix4 original = batch.getTransformMatrix().cpy();
                Matrix4 scaled = original.cpy()
                    .translate(drawX, drawY, 0)
                    .scale(0.55f, 0.55f, 1f)
                    .translate(-drawX, -drawY, 0);

                batch.setTransformMatrix(scaled);
                plantAssets.drawPlant(batch, clip, cursorAnimTime, drawX, drawY, true);
                batch.setTransformMatrix(original);
            }
        }
        // 2. Shovel Selected: Render Shovel Icon at cursor
        else if (GameScreenController.isShovelSelected && shovelCursorRegion != null) {
            float w = shovelCursorRegion.getRegionWidth() * 0.8f;
            float h = shovelCursorRegion.getRegionHeight() * 0.8f;
            batch.draw(shovelCursorRegion, mousePos.x - (w / 2f), mousePos.y - (h / 2f), w, h);
        }
        // 3. Plant Food Selected: Render Plant Food Icon at cursor
        else if (GameScreenController.isPlantFoodSelected && plantFoodCursorRegion != null) {
            float w = plantFoodCursorRegion.getRegionWidth() * 0.8f;
            float h = plantFoodCursorRegion.getRegionHeight() * 0.8f;
            batch.draw(plantFoodCursorRegion, mousePos.x - (w / 2f), mousePos.y - (h / 2f), w, h);
        }
    }
}
