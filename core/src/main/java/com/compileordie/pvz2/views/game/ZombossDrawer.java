package com.compileordie.pvz2.views.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.ZomBoss.DarkZomboss;
import com.compileordie.pvz2.models.entities.zombies.variants.ZomBoss.EgyptZomboss;
import pvz.libpvz.pam.PamPlayer;

import java.util.Set;
import java.util.function.Function;

/**
 * Zomboss explosion, dark laser, health bar (extracted, logic unchanged).
 */
final class ZombossDrawer {

    private static final String ZOMBOSS_EXPLOSION_PAM =
        "768/INITIAL/EFFECTS/ZOMBOSS_MISSILE_EXPLOSION_EGYPT/ZOMBOSS_MISSILE_EXPLOSION_EGYPT.PAM";
    private static final String ZOMBOSS_EXPLOSION_CLIP = "missile_lock_reticle";
    private static final String ZOMBOSS_DARK_LASER_PAM =
        "768/FULL/EFFECTS/ZOMBOSS_DARK_FIREBALL/ZOMBOSS_DARK_FIREBALL.PAM";
    private static final String ZOMBOSS_DARK_LASER_CLIP = "fall";

    private static final float ZOMBOSS_BAR_WIDTH_PX = 400f;
    private static final float ZOMBOSS_BAR_ASPECT_RATIO = 10f;
    private static final float ZOMBOSS_BAR_HEIGHT_PX = ZOMBOSS_BAR_WIDTH_PX / ZOMBOSS_BAR_ASPECT_RATIO;
    private static final float ZOMBOSS_BAR_MARGIN_PX = 24f;
    private static final float ZOMBOSS_BAR_DIVIDER_WIDTH_PX = 3f;
    private static final Color ZOMBOSS_BAR_VIVID_RED = new Color(0.75f, 0.05f, 0.05f, 1f);
    private static final Color ZOMBOSS_BAR_FADED_RED = new Color(0.75f, 0.05f, 0.05f, 0.28f);
    private static final Color ZOMBOSS_BAR_DIVIDER_YELLOW = new Color(1f, 0.85f, 0.1f, 1f);
    private static final String ZOMBOSS_HEAD_PAM =
        "768/FULL/EFFECTS/PRIZE_PINATA_ZOMBOSS/PRIZE_PINATA_ZOMBOSS.PAM";
    private static final String ZOMBOSS_HEAD_CLIP = "idle";
    private static final float ZOMBOSS_HEAD_TARGET_HEIGHT_PX = ZOMBOSS_BAR_HEIGHT_PX * 3.2f;

    private final GameRenderStates states;
    private final Set<String> brokenAssets;
    private float zombossHeadAnimTime = 0f;
    private Texture zombossBarSolidTexture;
    private final Function<Color, Texture> solidTextureFactory;

    ZombossDrawer(GameRenderStates states, Set<String> brokenAssets,
                  Function<Color, Texture> solidTextureFactory) {
        this.states = states;
        this.brokenAssets = brokenAssets;
        this.solidTextureFactory = solidTextureFactory;
    }

    void drawZombossExplosion(SpriteBatch batch, PamPlayer player, float delta) {
        if (AppModel.gameSession == null || player == null) return;
        if (brokenAssets.contains(ZOMBOSS_EXPLOSION_PAM)) return;

        for (Zombie zombie : AppModel.gameSession.gameBoard.getAllZombies()) {
            if (zombie.getType() != ZombieType.ZOMBOSS_IN_EGYPT) continue;
            EgyptZomboss zomboss = (EgyptZomboss) zombie;
            GameRenderStates.ZombieRenderState state =
                states.zombieRenderStates.computeIfAbsent(
                    zombie, z -> new GameRenderStates.ZombieRenderState());
            if (!zomboss.boom) {
                state.wasBooming = false;
                continue;
            }
            if (!state.wasBooming) {
                state.bombAnimTime = 0f;
                state.wasBooming = true;
            }
            state.bombAnimTime += delta;
            if (zomboss.r < 0 || zomboss.c < 0) continue;

            float explosionX = (float) (((zomboss.c + 0.5) * Constants.Game.TILE_WIDTH
                + Constants.Game.PADDING_X_REALITY) * Constants.UI.METER_TO_PIX);
            float explosionY = (float) (((zomboss.r) * Constants.Game.TILE_HEIGHT
                + Constants.Game.PADDING_Y_REALITY) * Constants.UI.METER_TO_PIX);
            try {
                player.draw(batch, ZOMBOSS_EXPLOSION_PAM, ZOMBOSS_EXPLOSION_CLIP,
                    state.bombAnimTime, explosionX, explosionY, 0.6f, 0.6f, true);
            } catch (Throwable e) {
                brokenAssets.add(ZOMBOSS_EXPLOSION_PAM);
                Gdx.app.error("PVZ-ASSET-MISSING",
                    "❌ رندر افکت انفجار زامباس ناموفق بود. مسیر: " + ZOMBOSS_EXPLOSION_PAM
                        + " | کلیپ: " + ZOMBOSS_EXPLOSION_CLIP + " دلیل: " + e, e);
            }
        }
    }

    void drawDarkZombossLaserSquare(SpriteBatch batch, PamPlayer player, float delta) {
        if (AppModel.gameSession == null || player == null) return;
        if (brokenAssets.contains(ZOMBOSS_DARK_LASER_PAM)) return;

        for (Zombie zombie : AppModel.gameSession.gameBoard.getAllZombies()) {
            if (zombie.getType() != ZombieType.ZOMBOSS_IN_DARK) continue;
            DarkZomboss zomboss = (DarkZomboss) zombie;
            GameRenderStates.ZombieRenderState state =
                states.zombieRenderStates.computeIfAbsent(
                    zombie, z -> new GameRenderStates.ZombieRenderState());
            if (!zomboss.boom) {
                state.wasBooming = false;
                continue;
            }
            if (!state.wasBooming) {
                state.bombAnimTime = 0f;
                state.wasBooming = true;
            }
            state.bombAnimTime += delta;
            drawDarkZombossLaserTile(batch, player, zomboss.r1, zomboss.c1, state.bombAnimTime);
            drawDarkZombossLaserTile(batch, player, zomboss.r2, zomboss.c2, state.bombAnimTime);
        }
    }

    private void drawDarkZombossLaserTile(SpriteBatch batch, PamPlayer player,
                                          int r, int c, float animTime) {
        if (r < 0 || c < 0) return;
        float explosionX = (float) (((c + 0.5) * Constants.Game.TILE_WIDTH
            + Constants.Game.PADDING_X_REALITY) * Constants.UI.METER_TO_PIX);
        float explosionY = (float) ((r * Constants.Game.TILE_HEIGHT
            + Constants.Game.PADDING_Y_REALITY) * Constants.UI.METER_TO_PIX);
        try {
            player.draw(batch, ZOMBOSS_DARK_LASER_PAM, ZOMBOSS_DARK_LASER_CLIP,
                animTime, explosionX, explosionY, 0.6f, 0.6f, true);
        } catch (Throwable e) {
            brokenAssets.add(ZOMBOSS_DARK_LASER_PAM);
            Gdx.app.error("PVZ-ASSET-MISSING",
                "❌ رندر افکت لیزر زامباس دارک ناموفق بود. مسیر: " + ZOMBOSS_DARK_LASER_PAM
                    + " | کلیپ: " + ZOMBOSS_DARK_LASER_CLIP + " دلیل: " + e, e);
        }
    }

    private double getZombossCurrentHealth(Zombie zombie) {
        if (zombie instanceof EgyptZomboss egyptZomboss) return egyptZomboss.health;
        if (zombie instanceof DarkZomboss darkZomboss) return darkZomboss.health;
        return zombie.getMaxHealth();
    }

    void drawZombossHealthBar(SpriteBatch batch, PamPlayer player, ScreenViewport viewport, float delta) {
        if (AppModel.gameSession == null || player == null) return;

        Zombie zomboss = null;
        for (Zombie zombie : AppModel.gameSession.gameBoard.getAllZombies()) {
            if (zombie.getType().name().contains("ZOMBOSS")) {
                zomboss = zombie;
                break;
            }
        }
        if (zomboss == null) return;

        if (zombossBarSolidTexture == null) {
            zombossBarSolidTexture = solidTextureFactory.apply(Color.WHITE);
        }
        zombossHeadAnimTime += delta;

        float barX = viewport.getWorldWidth() - ZOMBOSS_BAR_WIDTH_PX - ZOMBOSS_BAR_MARGIN_PX;
        float barY = ZOMBOSS_BAR_MARGIN_PX + 60;
        double maxHealth = zomboss.getMaxHealth() > 0 ? zomboss.getMaxHealth() : 1.0;
        float healthFraction = (float) Math.max(0.0,
            Math.min(1.0, getZombossCurrentHealth(zomboss) / maxHealth));
        float progress = 1f - healthFraction;
        float headX = barX + progress * ZOMBOSS_BAR_WIDTH_PX;

        Color prevColor = batch.getColor().cpy();
        if (headX > barX) {
            batch.setColor(ZOMBOSS_BAR_FADED_RED);
            batch.draw(zombossBarSolidTexture, barX, barY, headX - barX, ZOMBOSS_BAR_HEIGHT_PX);
        }
        if (headX < barX + ZOMBOSS_BAR_WIDTH_PX) {
            batch.setColor(ZOMBOSS_BAR_VIVID_RED);
            batch.draw(zombossBarSolidTexture, headX, barY,
                (barX + ZOMBOSS_BAR_WIDTH_PX) - headX, ZOMBOSS_BAR_HEIGHT_PX);
        }
        batch.setColor(ZOMBOSS_BAR_DIVIDER_YELLOW);
        float third = ZOMBOSS_BAR_WIDTH_PX / 3f;
        batch.draw(zombossBarSolidTexture,
            barX + third - ZOMBOSS_BAR_DIVIDER_WIDTH_PX / 2f, barY,
            ZOMBOSS_BAR_DIVIDER_WIDTH_PX, ZOMBOSS_BAR_HEIGHT_PX);
        batch.draw(zombossBarSolidTexture,
            barX + 2 * third - ZOMBOSS_BAR_DIVIDER_WIDTH_PX / 2f, barY,
            ZOMBOSS_BAR_DIVIDER_WIDTH_PX, ZOMBOSS_BAR_HEIGHT_PX);
        batch.setColor(prevColor);

        drawZombossHead(batch, player, headX, barY);
    }

    private void drawZombossHead(SpriteBatch batch, PamPlayer player, float headX, float barY) {
        if (brokenAssets.contains(ZOMBOSS_HEAD_PAM)) return;
        try {
            Rectangle nativeBounds = player.bounds(ZOMBOSS_HEAD_PAM, ZOMBOSS_HEAD_CLIP);
            float headScale = 1f;
            if (nativeBounds != null && nativeBounds.height > 0) {
                headScale = ZOMBOSS_HEAD_TARGET_HEIGHT_PX / nativeBounds.height;
            }
            float headCenterY = barY + ZOMBOSS_BAR_HEIGHT_PX / 2f;
            player.draw(batch, ZOMBOSS_HEAD_PAM, ZOMBOSS_HEAD_CLIP, zombossHeadAnimTime,
                headX, headCenterY, headScale, headScale, true);
        } catch (Throwable e) {
            brokenAssets.add(ZOMBOSS_HEAD_PAM);
            Gdx.app.error("PVZ-ASSET-MISSING",
                "❌ رندر کله‌ی زامباس (نوار سلامتی) ناموفق بود. مسیر: " + ZOMBOSS_HEAD_PAM
                    + " | کلیپ: " + ZOMBOSS_HEAD_CLIP + " دلیل: " + e, e);
        }
    }

    void dispose() {
        if (zombossBarSolidTexture != null) zombossBarSolidTexture.dispose();
        zombossBarSolidTexture = null;
    }
}
