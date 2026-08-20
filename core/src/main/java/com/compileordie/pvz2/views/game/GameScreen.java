package com.compileordie.pvz2.views.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.enums.ProjectileType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.capable.HunterZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.capable.OctopusZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.Tomb;
import com.compileordie.pvz2.models.game.waves.WaveType;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * View لایه‌ی اصلی گیم‌پلی (MVC - View).
 * منطق رندر به کلاس‌های کمکی تقسیم شده؛ رفتار همان GameScreen قبلی است.
 */
public class GameScreen implements Screen {
    boolean testOn = false;
    boolean testZombossOn = false;
    private final ZombieTestSpawner testSpawner =
        new ZombieTestSpawner();

    private SpriteBatch batch;
    private ScreenViewport viewport;
    private TextureBank textureBank;
    private PamPlayer player;

    public boolean hasWeTestForClickForDamaging = true;
    private static final float CLICK_DAMAGE_TEST_RADIUS_PX = 50f;

    private float effectPulseTime = 0f;
    private boolean hasGameEnded = false;
    private final Set<String> brokenAssets = new HashSet<>();
    private final Vector3 touchPoint = new Vector3();
    private float simulationAccumulator = 0f;
    private boolean resourcesReleased = false;

    private final GameRenderStates states = new GameRenderStates();
    private final GameCameraEffects cameraEffects = new GameCameraEffects();
    private GameScreenUI ui;
    private DebrisDrawer debrisDrawer;
    private ZombieDrawer zombieDrawer;
    private BoardEntityDrawer boardDrawer;
    private ZombossDrawer zombossDrawer;

    @Override
    public void show() {
        if (testZombossOn) AppModel.gameSession.gameBoard.waveManager.type = WaveType.NO_WAVES;

        batch = new SpriteBatch();
        viewport = new ScreenViewport();
        simulationAccumulator = 0f;
        resourcesReleased = false;
        states.clearAll();
        brokenAssets.clear();
        ZombieVisualRegistry.clearAvailabilityCache();
        hasGameEnded = false;
        AppModel.wonLastGame = null;

        initHelpers();
        loadAssets();
        ui.setupPauseUI();
        ui.buildPauseMenuPanelWithFog(textureBank);
        ui.setupGameUI(textureBank);
        Gdx.input.setInputProcessor(ui.uiStage);
    }

    private void initHelpers() {
        ui = new GameScreenUI(
            () -> ui.setPaused(false),
            () -> { hasGameEnded = false; },
            () -> {}
        );
        debrisDrawer = new DebrisDrawer(states, brokenAssets);
        zombieDrawer = new ZombieDrawer(states, brokenAssets, debrisDrawer);
        boardDrawer = new BoardEntityDrawer(states, brokenAssets);
        zombossDrawer = new ZombossDrawer(states, brokenAssets, color -> ui.createSolidTexture(color));
        ui.isPaused = false;
    }

    private void loadAssets() {
        try {
            textureBank = new TextureBank(GameScreenConstants.ASSET_RESOLUTION,
                Gdx.files.internal("pvz-assets"));
            player = new PamPlayer(textureBank, Gdx.files.internal("pvz-assets"));
            String chapterFolder = GameScreenConstants.chapterFolder(AppModel.currentChapter);
            TextureRegion bgLeft = textureBank.region(
                GameScreenConstants.BG_REGION_PREFIX + chapterFolder
                    + GameScreenConstants.BG_TEXTURE_LEFT_SUFFIX);
            TextureRegion bgMain = textureBank.region(
                GameScreenConstants.BG_REGION_PREFIX + chapterFolder
                    + GameScreenConstants.BG_TEXTURE_SUFFIX);
            TextureRegion bgRight = textureBank.region(
                GameScreenConstants.BG_REGION_PREFIX + chapterFolder
                    + GameScreenConstants.BG_TEXTURE_RIGHT_SUFFIX);
            boardDrawer.setBackgroundRegions(bgLeft, bgMain, bgRight);
            if (bgMain == null) {
                Gdx.app.error("PVZ-DEBUG",
                    "❌ تصویر پس‌زمینه اصلی برای چپتر '" + chapterFolder + "' پیدا نشد!");
            } else {
                Gdx.app.log("PVZ-DEBUG",
                    "✅ پس‌زمینه چپتر '" + chapterFolder + "' با موفقیت لود شد.");
            }
        } catch (Throwable e) {
            Gdx.app.error("PVZ-DEBUG",
                "❌ خطا در بارگذاری اولیه asset های گیم‌اسکرین: " + e.toString(), e);
        }
    }

    public void triggerCameraShake(float duration, float intensity) {
        cameraEffects.triggerCameraShake(duration, intensity);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        effectPulseTime += delta;
        updateTextureBank();
        handleInput();
        advanceSimulation(delta);
        if (!ui.isPaused && testOn) testSpawner.update(delta);
        cameraEffects.checkGiantZombieFootsteps(delta, ui.isPaused);
        cameraEffects.updateCameraShake(delta);
        viewport.getCamera().update();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        updateSunHud();
        drawWorld(delta);
        if (ui != null) ui.actAndDraw(delta);
    }

    private void updateTextureBank() {
        if (textureBank == null) return;
        try {
            textureBank.update();
        } catch (Throwable t) {
            if (!brokenAssets.contains("TEXTURE_BANK_ASYNC")) {
                Gdx.app.error("PVZ-ASSET-ASYNC",
                    "❌ خطا در بارگذاری پس‌زمینه است‌ها (TextureBank): " + t.toString(), t);
                brokenAssets.add("TEXTURE_BANK_ASYNC");
            }
        }
    }

    private void handleInput() {
        if (!Gdx.input.justTouched()) return;
        touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touchPoint);
        float meterX = touchPoint.x / Constants.UI.METER_TO_PIX;
        float meterY = touchPoint.y / Constants.UI.METER_TO_PIX;
        Gdx.app.log("&&&&--------PVZ-CLICK", String.format(
            "🖱️ مختصات کلیک -> پیکسل: (X: %.1f, Y: %.1f) | متر-مدل: (X: %.2f, Y: %.2f)",
            touchPoint.x, touchPoint.y, meterX, meterY));
        if (hasWeTestForClickForDamaging) {
            handleClickDamageTest(touchPoint.x, touchPoint.y);
        }
    }

    private void updateSunHud() {
        if (ui != null && AppModel.gameSession != null) {
            int amount = AppModel.gameSession.gameBoard.economyManager.sunAmount;
            ui.updateSunLabel(amount);
        }
    }

    private void drawWorld(float delta) {
        boolean paused = ui != null && ui.isPaused;
        zombieDrawer.setPaused(paused);
        zombieDrawer.setEffectPulseTime(effectPulseTime);
        boardDrawer.setPaused(paused);

        batch.begin();
        com.badlogic.gdx.math.Matrix4 originalMatrix = batch.getTransformMatrix().cpy();
        if (cameraEffects.shakeOffsetX != 0 || cameraEffects.shakeOffsetY != 0) {
            batch.getTransformMatrix().translate(
                cameraEffects.shakeOffsetX, cameraEffects.shakeOffsetY, 0);
            batch.setTransformMatrix(batch.getTransformMatrix());
        }
        boardDrawer.drawBackground(batch);
        boardDrawer.drawMowers(batch, player, delta);
        boardDrawer.drawTombs(batch, player, delta);
        boardDrawer.drawFireTiles(batch, player, delta);
        debrisDrawer.drawDeadZombies(batch, player, delta);
        zombieDrawer.drawZombies(batch, player, delta);
        zombossDrawer.drawZombossExplosion(batch, player, delta);
        zombossDrawer.drawDarkZombossLaserSquare(batch, player, delta);
        debrisDrawer.drawFallingDebris(batch, player, delta);

        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);
        boardDrawer.drawSuns(batch, player, delta, mousePos);

        batch.setTransformMatrix(originalMatrix);
        zombossDrawer.drawZombossHealthBar(batch, player, viewport, delta);
        batch.end();
    }

    private void handleClickDamageTest(float clickX, float clickY) {
        Zombie closest = null;
        float closestDistSq = Float.MAX_VALUE;
        float radiusSq = CLICK_DAMAGE_TEST_RADIUS_PX * CLICK_DAMAGE_TEST_RADIUS_PX;

        for (Map.Entry<Zombie, GameRenderStates.ZombieRenderState> entry
            : states.zombieRenderStates.entrySet()) {
            GameRenderStates.ZombieRenderState state = entry.getValue();
            if (Float.isNaN(state.lastDrawX) || Float.isNaN(state.lastDrawY)) continue;
            float dx = state.lastDrawX - clickX;
            float dy = state.lastDrawY - clickY;
            float distSq = dx * dx + dy * dy;
            if (distSq <= radiusSq && distSq < closestDistSq) {
                closestDistSq = distSq;
                closest = entry.getKey();
            }
        }

        Tomb closestTomb = null;
        float closestTombDistSq = Float.MAX_VALUE;
        for (Map.Entry<Tomb, GameRenderStates.TombRenderState> entry
            : states.tombRenderStates.entrySet()) {
            GameRenderStates.TombRenderState state = entry.getValue();
            if (Float.isNaN(state.lastDrawX) || Float.isNaN(state.lastDrawY)) continue;
            float dx = state.lastDrawX - clickX;
            float dy = state.lastDrawY - clickY;
            float distSq = dx * dx + dy * dy;
            if (distSq <= radiusSq && distSq < closestTombDistSq) {
                closestTombDistSq = distSq;
                closestTomb = entry.getKey();
            }
        }
        if (closestTomb != null) {
            closestTomb.takeDamage(100, ProjectileType.NORMAL);
            Gdx.app.log("PVZ-CLICK-DAMAGE-TEST",
                "🎯 [تست] قبر با کلیک روی نقطه‌ی رندرش، ۱۰۰ دمیج خورد.");
        }
        applyZombieClickDamage(closest);
    }

    private void applyZombieClickDamage(Zombie closest) {
        if (closest == null) return;
        try {
            closest.takeDamage(100, DamageType.NORMAL, null);
            Gdx.app.log("PVZ-CLICK-DAMAGE-TEST",
                "🎯 [تست] زامبی '" + closest.getType()
                    + "' با کلیک روی نقطه‌ی رندرش، ۱۰۰ دمیج خورد.");
            if (closest.getType() == ZombieType.HUNTER_ZOMBIE) {
                ((HunterZombie) closest).startThrowAnimation();
            } else if (closest.getType() == ZombieType.OCTOPUS_ZOMBIE) {
                ((OctopusZombie) closest).startTossAnimation();
            }
        } catch (Throwable e) {
            Gdx.app.error("PVZ-CLICK-DAMAGE-TEST",
                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n"
                    + "❌ takeDamage روی زامبی '" + closest.getType()
                    + "' خطا داد (احتمالا باگ null-check\n"
                    + "توی takeDamage خودِ مدل، نه اینجا): " + e + "\n"
                    + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", e);
        }
    }

    private void advanceSimulation(float delta) {
        if (AppModel.wonLastGame != null && !hasGameEnded) {
            hasGameEnded = true;
            ui.setPaused(true);
            ui.showGameEndPanel(AppModel.wonLastGame);
            return;
        }
        if (AppModel.gameSession == null || ui.isPaused) return;
        simulationAccumulator += delta;
        while (simulationAccumulator >= GameScreenConstants.SIMULATION_STEP_SECONDS) {
            AppModel.gameSession.tick(1);
            simulationAccumulator -= GameScreenConstants.SIMULATION_STEP_SECONDS;
        }
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height, true);
        if (ui != null) ui.resize(width, height);
    }

    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void hide() {
        releaseGraphicsResources();
    }

    @Override
    public void dispose() {
        releaseGraphicsResources();
    }

    private void releaseGraphicsResources() {
        if (resourcesReleased) return;
        resourcesReleased = true;
        if (batch != null) batch.dispose();
        if (ui != null) ui.dispose();
        if (zombossDrawer != null) zombossDrawer.dispose();
        states.clearAll();
    }
}
