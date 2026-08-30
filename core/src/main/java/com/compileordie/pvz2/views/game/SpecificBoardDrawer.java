package com.compileordie.pvz2.views.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.game.board.Tile;
import com.compileordie.pvz2.models.game.board.TileType;
import com.compileordie.pvz2.models.game.levels.ChapterType;
import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.models.game.minigames.vasebreaker.Vase;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

import java.util.*;

final class SpecificBoardDrawer {

    private static final String ICE_SLIDER_DOWN_PAM =
        "768/FULL/EFFECTS/TILESLIDER_ICEAGE_DOWN/TILESLIDER_ICEAGE_DOWN.PAM";
    private static final String ICE_SLIDER_UP_PAM =
        "768/FULL/EFFECTS/TILESLIDER_ICEAGE_UP/TILESLIDER_ICEAGE_UP.PAM";
    private static final String ICE_SLIDER_ANIM_CLIP = "idle";
    private static final String SHALLOW_BEACH_PAM = "768/FULL/EFFECTS/SHALLOW_PUDDLE_TILE/SHALLOW_PUDDLE_TILE.PAM";
    private static final String SHALLOW_BEACH_ANIM_CLIP = null;
    private static final String PROTECT_TILE_PAM = "768/INITIAL/BACKGROUNDS/PROTECT_TILE/PROTECT_TILE.PAM";
    private static final String PROTECT_TILE_ANIM_CLIP = "animation";
    private static final String WATER_LEVEL_PAM = "768/FULL/BACKGROUNDS/WATER_UNDERLAYER/WATER_UNDERLAYER.PAM";
    private static final String WATER_LEVEL_ANIM_CLIP = "Water";
    private static final float WATER_LERP_SPEED = 4.0f; // Higher values move faster
    private float currentWaterX = -1f;
    private float waterAnimTime = 0f;
    private static final String MAX_TIDE_PAM = "768/FULL/BACKGROUNDS/WATER_TIDE_LINE/WATER_TIDE_LINE.PAM";
    private static final String MAX_TIDE_ANIM_CLIP = "idle";
    private static final String CHILL_WIND_PAM = "768/FULL/EFFECTS/FROSTBITE_CHILL_WIND/FROSTBITE_CHILL_WIND.PAM";
    private static final String CHILL_WIND_ANIM_CLIP = "animation";

    private final GameRenderStates states;
    private final Set<String> brokenAssets;
    private final Map<Tile, Float> sliderAnimTimes = new HashMap<>();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    SpecificBoardDrawer(GameRenderStates states, Set<String> brokenAssets) {
        this.states = states;
        this.brokenAssets = brokenAssets;
    }

    void drawSlipperyTiles(SpriteBatch batch, PamPlayer player, float delta) {
        if (AppModel.gameSession == null || player == null) return;

        Set<Tile> activeSlipperyTiles = new HashSet<>();

        for (var lane : AppModel.gameSession.gameBoard.lanes) {
            for (Tile tile : lane.tiles) {
                String pamPath = null;

                if (tile.type == TileType.SLIPPERY_DOWN) {
                    pamPath = ICE_SLIDER_DOWN_PAM;
                } else if (tile.type == TileType.SLIPPERY_UP) {
                    pamPath = ICE_SLIDER_UP_PAM;
                }

                if (pamPath == null || brokenAssets.contains(pamPath)) continue;

                activeSlipperyTiles.add(tile);
                float animTime = sliderAnimTimes.getOrDefault(tile, 0f) + delta;
                sliderAnimTimes.put(tile, animTime);

                float drawX = ((tile.column * Constants.Game.TILE_WIDTH
                    + Constants.Game.PADDING_X + (Constants.Game.TILE_WIDTH / 2f)) * Constants.UI.METER_TO_PIX) + 5f;
                float drawY = ((tile.row * Constants.Game.TILE_HEIGHT
                    + Constants.Game.PADDING_Y + (Constants.Game.TILE_HEIGHT / 2f)) * Constants.UI.METER_TO_PIX);

                try {
                    player.draw(batch, pamPath, ICE_SLIDER_ANIM_CLIP, animTime, drawX, drawY, true);
                } catch (Throwable e) {
                    brokenAssets.add(pamPath);
                    Gdx.app.error("PVZ-ASSET-MISSING",
                        "❌ [SpecificBoardDrawer] Failed rendering slider tile PAM: "
                            + pamPath + " | clip: " + ICE_SLIDER_ANIM_CLIP, e);
                }
            }
        }

        sliderAnimTimes.keySet().removeIf(tile -> !activeSlipperyTiles.contains(tile));
    }

    public void drawShallowBeaches(SpriteBatch batch, PamPlayer player, float delta) {
        if (AppModel.gameSession == null || player == null) return;

        Set<Tile> activeShallowBeachTiles = new HashSet<>();

        for (var lane : AppModel.gameSession.gameBoard.lanes) {
            for (Tile tile : lane.tiles) {
                String pamPath = null;

                if (tile.type == TileType.SHALLOW_BEACH) {
                    pamPath = SHALLOW_BEACH_PAM;
                }

                if (pamPath == null || brokenAssets.contains(pamPath)) continue;

                activeShallowBeachTiles.add(tile);
                float animTime = sliderAnimTimes.getOrDefault(tile, 0f) + delta;
                sliderAnimTimes.put(tile, animTime);

                float drawX = (((tile.column + 0.5f) * Constants.Game.TILE_WIDTH + Constants.Game.PADDING_X)
                    * Constants.UI.METER_TO_PIX);
                float drawY = (((tile.row + 0.5f) * Constants.Game.TILE_HEIGHT + Constants.Game.PADDING_Y)
                    * Constants.UI.METER_TO_PIX);

                try {
                    player.draw(batch, pamPath, SHALLOW_BEACH_ANIM_CLIP, animTime, drawX, drawY, 0.8f, 0.8f, true);
                } catch (Throwable e) {
                    brokenAssets.add(pamPath);
                    Gdx.app.error("PVZ-ASSET-MISSING",
                        "❌ [SpecificBoardDrawer] Failed rendering slider tile PAM: "
                            + pamPath + " | clip: " + SHALLOW_BEACH_ANIM_CLIP, e);
                }
            }
        }

        sliderAnimTimes.keySet().removeIf(tile -> !activeShallowBeachTiles.contains(tile));
    }

    public void drawProtectTiles(SpriteBatch batch, PamPlayer player, float delta) {
        if (AppModel.gameSession == null || player == null) return;

        Set<Tile> activeProtectTiles = new HashSet<>();

        for (var lane : AppModel.gameSession.gameBoard.lanes) {
            for (Tile tile : lane.tiles) {
                String pamPath = null;

                if (tile.plant != null && tile.plant.isSpecial()) {
                    pamPath = PROTECT_TILE_PAM;
                }

                if (pamPath == null || brokenAssets.contains(pamPath)) continue;

                activeProtectTiles.add(tile);
                float animTime = sliderAnimTimes.getOrDefault(tile, 0f) + delta;
                sliderAnimTimes.put(tile, animTime);

                float drawX = (((tile.column + 0.5f) * Constants.Game.TILE_WIDTH + Constants.Game.PADDING_X)
                    * Constants.UI.METER_TO_PIX);
                float drawY = (((tile.row + 0.5f) * Constants.Game.TILE_HEIGHT + Constants.Game.PADDING_Y)
                    * Constants.UI.METER_TO_PIX);

                try {
                    player.draw(batch,
                        pamPath,
                        PROTECT_TILE_ANIM_CLIP,
                        animTime,
                        drawX + 4f,
                        drawY + 2f,
                        0.8f,
                        0.8f,
                        true);
                } catch (Throwable e) {
                    brokenAssets.add(pamPath);
                    Gdx.app.error("PVZ-ASSET-MISSING",
                        "❌ [SpecificBoardDrawer] Failed rendering slider tile PAM: "
                            + pamPath + " | clip: " + PROTECT_TILE_ANIM_CLIP, e);
                }
            }
        }

        sliderAnimTimes.keySet().removeIf(tile -> !activeProtectTiles.contains(tile));
    }

    public void drawWaterLevel(SpriteBatch batch, PamPlayer player, float delta) {
        if (AppModel.gameSession == null || AppModel.gameSession.gameBoard == null
            || AppModel.currentLevel == null
            || AppModel.currentLevel.chapterType != ChapterType.BIG_WAVE_BEACH
            || player == null) return;

        if (brokenAssets.contains(WATER_LEVEL_PAM)) return;

        float targetTideLevel = AppModel.gameSession.gameBoard.tideLevel;
        float targetWaterX = ((Constants.Game.BOARD_COLS - targetTideLevel) * Constants.Game.TILE_WIDTH
            + Constants.Game.PADDING_X) * Constants.UI.METER_TO_PIX + 85f;

        // Snap on initial load
        if (currentWaterX < 0f) {
            currentWaterX = targetWaterX;
        } else {
            // Frame-rate independent linear interpolation
            currentWaterX += (targetWaterX - currentWaterX) * Math.min(1f, WATER_LERP_SPEED * delta);
        }

        waterAnimTime += delta;
        float drawY = 150f;

        try {
            player.draw(batch, WATER_LEVEL_PAM, WATER_LEVEL_ANIM_CLIP, waterAnimTime, currentWaterX, drawY, true);
        } catch (Throwable e) {
            brokenAssets.add(WATER_LEVEL_PAM);
            Gdx.app.error("PVZ-ASSET-MISSING",
                "❌ [SpecificBoardDrawer] Failed rendering water level PAM: " + WATER_LEVEL_PAM, e);
        }
    }

    public void drawMaxTideLevel(SpriteBatch batch, PamPlayer player, float delta) {
        if (AppModel.gameSession == null || AppModel.gameSession.gameBoard == null
            || AppModel.currentLevel == null
            || AppModel.currentLevel.chapterType != ChapterType.BIG_WAVE_BEACH
            || player == null) return;

        if (brokenAssets.contains(MAX_TIDE_PAM)) return;
        float drawX = ((Constants.Game.BOARD_COLS - AppModel.gameSession.gameBoard.maxTideLevel)
            * Constants.Game.TILE_WIDTH + Constants.Game.PADDING_X) * Constants.UI.METER_TO_PIX + 50f;
        float drawY = 400f;

        try {
            player.draw(batch, MAX_TIDE_PAM, MAX_TIDE_ANIM_CLIP, delta, drawX, drawY, true);
        } catch (Throwable e) {
            brokenAssets.add(MAX_TIDE_PAM);
            Gdx.app.error("PVZ-ASSET-MISSING",
                "❌ [SpecificBoardDrawer] Failed rendering water level PAM: " + MAX_TIDE_PAM, e);
        }
    }

    public void drawChillWinds(SpriteBatch batch, PamPlayer player, GameRenderStates renderStates, float delta) {
        if (renderStates.chillWinds.isEmpty()) return;

        Iterator<GameRenderStates.ChillWindAnim> iterator = renderStates.chillWinds.iterator();
        while (iterator.hasNext()) {
            GameRenderStates.ChillWindAnim wind = iterator.next();
            wind.animTime += delta;

            if (wind.animTime >= GameRenderStates.ChillWindAnim.MAX_DURATION) {
                iterator.remove();
                continue;
            }

            float drawX = 0;
            float drawY = (wind.row * Constants.Game.TILE_HEIGHT + Constants.Game.PADDING_Y)
                * Constants.UI.METER_TO_PIX + 130f;

            try {
                player.draw(
                    batch,
                    CHILL_WIND_PAM,
                    CHILL_WIND_ANIM_CLIP,
                    wind.animTime,
                    drawX,
                    drawY,
                    false
                );
            } catch (Throwable e) {
                Gdx.app.error("PVZ-RENDER", "Failed to render Chill Wind PAM: " + e.getMessage());
            }
        }
    }

    public void drawBowlingLine(SpriteBatch batch) {
        if (AppModel.gameSession == null || AppModel.currentLevel != LevelID.WALNUT_BOWLING) {
            return;
        }

        batch.end();

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);

        Gdx.gl.glLineWidth(8.0f);

        float x = ((Constants.Game.BOARD_COLS - ConfigManager.gameplay().bowlingLine) * Constants.Game.TILE_WIDTH
            + Constants.Game.PADDING_X) * Constants.UI.METER_TO_PIX;
        float startY = Constants.Game.PADDING_Y * Constants.UI.METER_TO_PIX;
        float endY = ((Constants.Game.BOARD_ROWS * Constants.Game.TILE_HEIGHT) + Constants.Game.PADDING_Y)
            * Constants.UI.METER_TO_PIX;
        shapeRenderer.line(x, startY, x, endY);

        shapeRenderer.end();
        Gdx.gl.glLineWidth(1.0f); // Reset line thickness back to default
        batch.begin();
    }

    public void drawVases(SpriteBatch batch, TextureBank textureBank) {
        if (AppModel.gameSession == null || AppModel.gameSession.gameBoard == null || textureBank == null) return;

        for (var lane : AppModel.gameSession.gameBoard.lanes) {
            for (Tile tile : lane.tiles) {
                Vase vase = tile.vase;
                if (vase == null || vase.isBroken) continue;

                String textureKey = switch (vase.type) {
                    case GARGANTUAR -> "IMAGE_VASEBREAKER_VASE_GARGANTUAR_VASE_GARGANTUAR_115X150";
                    case PLANT -> "IMAGE_VASEBREAKER_VASE_GREEN_VASE_GREEN_115X150";
                    case NORMAL -> "IMAGE_VASEBREAKER_VASE_BROWN_VASE_BROWN_115X150";
                };

                TextureRegion region = textureBank.region(textureKey);
                if (region == null) continue;

                float drawX = (float) (vase.getX() * Constants.UI.METER_TO_PIX);
                float drawY = (float) (vase.getY() * Constants.UI.METER_TO_PIX);

                float width = region.getRegionWidth();
                float height = region.getRegionHeight();

                batch.draw(region, drawX - (width / 2f), drawY - (height / 2f), width, height);
            }
        }
    }

    public void drawDeadline(SpriteBatch batch) {
        if (AppModel.gameSession == null || AppModel.currentLevel != LevelID.DEAD_LINE) {
            return;
        }

        batch.end();

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);

        Gdx.gl.glLineWidth(8.0f);

        float x = Constants.Game.DEADLINE_X * Constants.UI.METER_TO_PIX;
        float startY = Constants.Game.PADDING_Y * Constants.UI.METER_TO_PIX;
        float endY = ((Constants.Game.BOARD_ROWS * Constants.Game.TILE_HEIGHT) + Constants.Game.PADDING_Y)
            * Constants.UI.METER_TO_PIX;
        shapeRenderer.line(x, startY, x, endY);

        shapeRenderer.end();
        Gdx.gl.glLineWidth(1.0f); // Reset line thickness back to default
        batch.begin();
    }
}
