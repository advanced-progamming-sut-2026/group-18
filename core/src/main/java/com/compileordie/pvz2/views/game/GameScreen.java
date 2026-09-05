package com.compileordie.pvz2.views.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.controllers.menus.game.GameScreenController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.enums.ProjectileType;
import com.compileordie.pvz2.models.entities.zombies.ZombieBuilder;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.capable.HunterZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.capable.OctopusZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.Tomb;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Tile;
import com.compileordie.pvz2.models.game.economy.Sun;
import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.models.game.minigames.vasebreaker.Vase;
import com.compileordie.pvz2.models.game.waves.WaveType;
import com.compileordie.pvz2.network.KryoSerializer;
import com.compileordie.pvz2.network.NetworkClient;
import com.compileordie.pvz2.network.protocol.Message;
import com.compileordie.pvz2.network.protocol.MessageType;
import com.compileordie.pvz2.views.game.ui.GameScreenUI;
import com.compileordie.pvz2.views.game.ui.LevelStartDialog;
import com.compileordie.pvz2.views.helpers.LeftMessageManager;
import com.compileordie.pvz2.views.helpers.ToastManager;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.PvzSkin;

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
    /**
     * سوییچ اصلی همه‌ی قابلیت‌های تستیِ مربوط به زامبی: اسپاون زامبی‌های
     * تستی (testSpawner) و همچنین تست دمیج با کلیک روی زامبی/قبر/زامباس
     * (handleClickDamageTest). وقتی false باشه، همه‌ی این‌ها کاملاً خاموشن.
     */
    boolean testPastKommeh = false;
    private final ZombieTestSpawner testSpawner =
        new ZombieTestSpawner();

    private SpriteBatch batch;
    private FitViewport viewport;
    /*private ScreenViewport viewport;*/
    private TextureBank textureBank;
    private PamPlayer player;

    public boolean hasWeTestForClickForDamaging = true;
    public boolean overrideForceDamageClick = true;
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
    private SpecificBoardDrawer specificBoardDrawer;
    //TODO:
    private PlantAssetManager plantAssetManager;
    private PlantMatchManager plantMatchManager;
    @Override
    public void show() {
        if (testZombossOn) AppModel.gameSession.gameBoard.waveManager.type = WaveType.NO_WAVES;

        batch = new SpriteBatch();
        // This locks your world to exactly 1920x1080, scaling the grid and mouse perfectly!
        viewport = new FitViewport(Constants.UI.DEFAULT_WIDTH, Constants.UI.DEFAULT_HEIGHT);
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

        GameScreenUI.isPaused = true;
        LevelStartDialog.show(
            ui.uiStage,
            AppModel.currentLevel,
            PvzSkin.get(),
            textureBank,
            player,
            () -> {
                GameScreenUI.isPaused = false;
                ToastManager.showMessage("The match begins!");
            }
        );

        if (AppModel.gameSession != null && AppModel.gameSession.gameBoard != null) {
            AppModel.gameSession.gameBoard.waveManager.setWaveEventListener(
                (currentWave, totalWaves, isFinalWave, message) -> {
                    if (isFinalWave) {
                        ToastManager.showError(message);
                    } else {
                        ToastManager.showMessage(message);
                    }
                }
            );
        }
    }

    private void initHelpers() {
        ui = new GameScreenUI(
            () -> ui.setPaused(false),
            () -> { hasGameEnded = false; },
            () -> {}
        );
        //TODO:
        plantAssetManager = new PlantAssetManager();
        plantMatchManager = new PlantMatchManager(states, plantAssetManager);
        debrisDrawer = new DebrisDrawer(states, brokenAssets);
        zombieDrawer = new ZombieDrawer(states, brokenAssets, debrisDrawer);
        boardDrawer = new BoardEntityDrawer(states, brokenAssets);
        zombossDrawer = new ZombossDrawer(states, brokenAssets, color -> ui.createSolidTexture(color));
        specificBoardDrawer = new SpecificBoardDrawer(states, brokenAssets);
        GameScreenUI.isPaused = false;
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
            boardDrawer.initSelectionAssets(textureBank);
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
        delta *= AppModel.player.gameSpeedCoefficient;
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        boolean paused = ui != null && GameScreenUI.isPaused;

        if (!paused) {
            effectPulseTime += delta;
        }

        updateTextureBank();
        handleInput();
        advanceSimulation(delta);
        if (!paused && testPastKommeh) testSpawner.update(delta);
        cameraEffects.checkGiantZombieFootsteps(delta, paused);
        cameraEffects.updateCameraShake(paused ? 0f : delta);
        viewport.getCamera().update();
        batch.setProjectionMatrix(viewport.getCamera().combined);
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
        if (ui != null && GameScreenUI.isPaused) return;
        handleMessageKeys();

        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            GameScreenController.cancelSelection();
            return;
        }

        if (!Gdx.input.justTouched()) return;
        touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touchPoint);

        Vase clickedVase = GameScreenController.getVaseAt(Gdx.input.getX(), Gdx.input.getY(), viewport);
        if (clickedVase != null) {
            GameScreenController.handleVaseClick(clickedVase);
            return;
        }

        Tile clickedTile = GameScreenController.getTileAt(Gdx.input.getX(), Gdx.input.getY(), viewport);
        if (clickedTile != null) {
            GameScreenController.handleTileClick(clickedTile);
            return;
        }

        // TODO: For debug purposes. Remove later:
        if (!Gdx.input.justTouched()) return;
        touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(touchPoint);
        float meterX = touchPoint.x / Constants.UI.METER_TO_PIX;
        float meterY = touchPoint.y / Constants.UI.METER_TO_PIX;
        Gdx.app.log("&&&&--------PVZ-CLICK", String.format(
            "🖱️ مختصات کلیک -> پیکسل: (X: %.1f, Y: %.1f) | متر-مدل: (X: %.2f, Y: %.2f)",
            touchPoint.x, touchPoint.y, meterX, meterY));
        if ((testPastKommeh && hasWeTestForClickForDamaging) || overrideForceDamageClick) {
            handleClickDamageTest(touchPoint.x, touchPoint.y);
        }
    }

    public static void sendReaction(int code) {
        if (AppModel.opponentUsername != null && NetworkClient.getInstance().isConnected()) {
            Message message = new Message(MessageType.REACTION_SEND)
                .put("target", AppModel.opponentUsername)
                .put("number", String.valueOf(code));
            NetworkClient.getInstance().send(message);
        }
    }

    private void handleMessageKeys() {
        if (AppModel.currentLevel != LevelID.I_ZOMBIE) return;

        int[] numberKeys = {
            com.badlogic.gdx.Input.Keys.NUM_1, com.badlogic.gdx.Input.Keys.NUM_2,
            com.badlogic.gdx.Input.Keys.NUM_3, com.badlogic.gdx.Input.Keys.NUM_4,
            com.badlogic.gdx.Input.Keys.NUM_5, com.badlogic.gdx.Input.Keys.NUM_6,
            com.badlogic.gdx.Input.Keys.NUM_7, com.badlogic.gdx.Input.Keys.NUM_8,
            com.badlogic.gdx.Input.Keys.NUM_9
        };

        for (int i = 0; i < 9; i++) {
            if (Gdx.input.isKeyJustPressed(numberKeys[i])) {
                sendReaction(i + 1);
                break;
            }
        }
    }

    private void drawWorld(float delta) {
        boolean paused = ui != null && GameScreenUI.isPaused;
        float worldDelta = paused ? 0f : delta;

        zombieDrawer.setPaused(paused);
        zombieDrawer.setEffectPulseTime(effectPulseTime);
        boardDrawer.setPaused(paused);

        batch.begin();
        Matrix4 originalMatrix = batch.getTransformMatrix().cpy();
        if (cameraEffects.shakeOffsetX != 0 || cameraEffects.shakeOffsetY != 0) {
            batch.getTransformMatrix().translate(
                cameraEffects.shakeOffsetX, cameraEffects.shakeOffsetY, 0);
            batch.setTransformMatrix(batch.getTransformMatrix());
        }
        boardDrawer.drawBackground(batch);
        specificBoardDrawer.drawSlipperyTiles(batch, player, worldDelta);
        specificBoardDrawer.drawShallowBeaches(batch, player, worldDelta);
        specificBoardDrawer.drawProtectTiles(batch, player, worldDelta);
        boardDrawer.drawGridLines(batch);
        specificBoardDrawer.drawBowlingLine(batch);
        specificBoardDrawer.drawDeadline(batch);
        specificBoardDrawer.drawWaterLevel(batch, player, worldDelta);
        specificBoardDrawer.drawMaxTideLevel(batch, player, worldDelta);
        boardDrawer.drawMowers(batch, player, worldDelta);
        specificBoardDrawer.drawBrains(batch, player, worldDelta);
        boardDrawer.drawTombs(batch, player, worldDelta);
        boardDrawer.drawFireTiles(batch, player, worldDelta);
        specificBoardDrawer.drawVases(batch, textureBank);

        boardDrawer.drawCraters(batch, player, worldDelta);
        Tile hoveredTile = GameScreenController.getTileAt(Gdx.input.getX(), Gdx.input.getY(), viewport);
        boardDrawer.drawTileHighlight(batch, hoveredTile);

        plantAssetManager.update();
        plantMatchManager.setPaused(paused);
        plantMatchManager.draw(batch, player, worldDelta);
        debrisDrawer.drawDeadZombies(batch, player, worldDelta);

        // Zombies receive regular delta so their "idle" animation plays
        zombieDrawer.drawZombies(batch, player, delta);

        zombossDrawer.drawZombossExplosion(batch, player, worldDelta);
        zombossDrawer.drawDarkZombossLaserSquare(batch, player, worldDelta);
        debrisDrawer.drawFallingDebris(batch, player, worldDelta);

        if (AppModel.gameSession != null) {
            int chillRow = AppModel.gameSession.gameBoard.waveManager.chillWindRow;
            if (chillRow >= 0) {
                boardDrawer.states.chillWinds.add(new GameRenderStates.ChillWindAnim(chillRow));
                AppModel.gameSession.gameBoard.waveManager.chillWindRow = -1;
            }
        }
        specificBoardDrawer.drawChillWinds(batch, player, boardDrawer.states, worldDelta);

        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);
        boardDrawer.drawPlantFoods(batch, worldDelta, mousePos);
        boardDrawer.drawSeedPackets(batch, plantAssetManager, worldDelta, mousePos);
        boardDrawer.drawSuns(batch, player, worldDelta, mousePos);
        boardDrawer.drawCursorFollower(batch, plantAssetManager, mousePos, delta);

        batch.setTransformMatrix(originalMatrix);
        zombossDrawer.drawZombossHealthBar(batch, player, viewport, worldDelta);
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
        if (AppModel.gameSession == null || GameScreenUI.isPaused) return;

        // Drain network queue and replace GameBoard if state sync arrives
        processNetworkInput();

        // Only run local tick simulation if this client is NOT the receiver
        if (!AppModel.isReceiverClient) {
            simulationAccumulator += delta;
            boolean ticked = false;
            while (simulationAccumulator >= GameScreenConstants.SIMULATION_STEP_SECONDS) {
                if (AppModel.gameSession == null) break; // judge()/afterSession may have ended it below
                AppModel.gameSession.tick(1);
                simulationAccumulator -= GameScreenConstants.SIMULATION_STEP_SECONDS;
                ticked = true;
                if (AppModel.wonLastGame != null) {
                    // Game just ended for us (plant/host side, the only side that ever
                    // ticks/judges - see GameJudge.judge()). Tell the opponent so THEIR
                    // client shows the exact same end-game modal, from their perspective
                    // (their win/loss is the opposite of ours). I_ZOMBIE-only, per design.
                    notifyOpponentGameOver();
                    break;
                }
            }

            // Pack and send GameBoard state to opponent on every tick
            if (ticked && AppModel.gameSession != null && AppModel.opponentUsername != null
                && NetworkClient.getInstance().isConnected()) {
                String encodedBoard = KryoSerializer.serialize(AppModel.gameSession.gameBoard);
                Message syncMessage = new Message(MessageType.IZOMBIE_STATE_SYNC)
                    .put("target", AppModel.opponentUsername)
                    .put("board", encodedBoard);
                NetworkClient.getInstance().send(syncMessage);
            }
        }
    }

    /**
     * I_ZOMBIE only: called right after WE (the plant/host side, the only side whose
     * GameJudge actually runs - see GameJudge.judge()'s early isReceiverClient return)
     * just decided the match is over. The opponent (zombie/receiver side) never judges
     * anything itself, so without this it would just keep playing forever with no idea
     * the match ended. "One's win is the other's loss", so we send them the inverse of
     * our own AppModel.wonLastGame.
     */
    private void notifyOpponentGameOver() {
        if (AppModel.currentLevel != LevelID.I_ZOMBIE) return;
        if (AppModel.opponentUsername == null || !NetworkClient.getInstance().isConnected()) return;
        boolean opponentWon = !AppModel.wonLastGame;
        NetworkClient.getInstance().send(new Message(MessageType.IZOMBIE_GAME_OVER)
            .put("target", AppModel.opponentUsername)
            .put("won", String.valueOf(opponentWon)));
    }

    private void processNetworkInput() {
        if (!NetworkClient.getInstance().isConnected()) return;

        Message msg;
        while ((msg = NetworkClient.getInstance().poll()) != null) {
            if (msg.getType() == MessageType.IZOMBIE_STATE_SYNC) {
                String encodedBoard = msg.get("board");
                if (encodedBoard != null && !encodedBoard.isEmpty()) {
                    GameBoard newBoard = KryoSerializer.deserialize(encodedBoard, GameBoard.class);
                    if (newBoard != null) {
                        AppModel.gameSession.gameBoard = newBoard;
                    }
                }
            }
            if (msg.getType() == MessageType.IZOMBIE_GAME_OVER) {
                // Comes from the opponent (plant/host side) telling us (zombie/receiver
                // side) that the match just ended and what OUR outcome is. This reuses
                // the exact same GameScreen.advanceSimulation()/GameScreenUI.showGameEndPanel
                // path/listeners as the host - we just set the same flag it sets itself.
                AppModel.wonLastGame = Boolean.parseBoolean(msg.get("won"));
            }
            if (msg.getType() == MessageType.IZOMBIE_SUN_COLLECT_REQUEST) {
                // Only meaningful on the plant/host side (the only side with an
                // authoritative gameBoard); if we're the receiver we shouldn't even be
                // able to get here since we'd never send ourselves this message, but
                // guard anyway.
                if (!AppModel.isReceiverClient && AppModel.gameSession != null) {
                    applyOpponentSunCollectRequest(msg);
                }
            }
            if (msg.getType() == MessageType.IZOMBIE_SPAWN_REQUEST) {
                String zombieTypeStr = msg.get("zombieType");
                int row = Integer.parseInt(msg.get("row"));
                int col = Integer.parseInt(msg.get("col"));
                int cost = Integer.parseInt(msg.get("cost"));

                ZombieType zombieType = ZombieType.valueOf(zombieTypeStr);
                Tile targetTile = AppModel.gameSession.gameBoard.getTile(row, col);

                // Instantiate zombie on the receiving opponent's local board state
                float spawnX = targetTile.column * Constants.Game.TILE_WIDTH + Constants.Game.PADDING_X
                    + (Constants.Game.TILE_WIDTH / 2f);
                float spawnY = targetTile.row * Constants.Game.TILE_HEIGHT + Constants.Game.PADDING_Y
                    + (Constants.Game.TILE_HEIGHT / 2f);
                Zombie newZombie = ZombieBuilder.create(zombieType, spawnX, spawnY, targetTile.row);
                AppModel.gameSession.gameBoard.getLane(row).zombies.add(newZombie);
                AppModel.gameSession.gameBoard.economyManager.sunAmount -= cost;
            }
            if (msg.getType() == MessageType.REACTION_SEND) {
                int number = Integer.parseInt(msg.get("number"));
                LeftMessageManager.showMessage(number);
            }
        }
    }

    /**
     * زامبی‌ساید (حریف) یه خورشید رو hover کرده و به‌جای جمع‌آوری محلی (که چون gameBoard
     * اش authoritative نیست بی‌فایده‌ست)، از ما (پلنت‌ساید، simulator واقعی) خواسته که
     * خودمون این خورشید رو جمع کنیم. چون Sun شناسه‌ی یکتا نداره، با نزدیک‌ترین تطبیق
     * مختصات x/y پیداش می‌کنیم (suns هیچ‌وقت دقیقا روی هم نمی‌افتن، پس این کافیه).
     */
    private void applyOpponentSunCollectRequest(Message msg) {
        try {
            double targetX = Double.parseDouble(msg.get("x"));
            double targetY = Double.parseDouble(msg.get("y"));
            var suns = AppModel.gameSession.gameBoard.economyManager.suns;
            Sun closest = null;
            double closestDistSq = Double.MAX_VALUE;
            for (var sun : suns) {
                double dx = sun.getX() - targetX;
                double dy = sun.getY() - targetY;
                double distSq = dx * dx + dy * dy;
                if (distSq < closestDistSq) {
                    closestDistSq = distSq;
                    closest = sun;
                }
            }
            // 0.05 متر (~۵ سانتی‌متر) تلورانس - چون خورشید غیر از پرت‌شدن معمولا ثابته،
            // فاصله‌ی واقعی باید تقریبا صفر باشه؛ این فقط برای رفع خطای اعشاری/سریالایزه.
            if (closest != null && closestDistSq <= 0.05 * 0.05) {
                BoardEntityDrawer.collectSun(AppModel.gameSession.gameBoard, closest);
                suns.remove(closest);
                states.sunRenderStates.remove(closest);
            }
        } catch (Exception e) {
            Gdx.app.error("PVZ-SUN-NETWORK", "❌ پردازش درخواست جمع‌آوری خورشید حریف خطا داد: " + e);
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
