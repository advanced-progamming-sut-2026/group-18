package com.compileordie.pvz2.views.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.controllers.menus.game.GameScreenController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.LawnMower;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.Tomb;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.TombType;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Tile;
import com.compileordie.pvz2.models.game.economy.PlantCard;
import com.compileordie.pvz2.models.game.economy.Sun;
import com.compileordie.pvz2.models.game.levels.ChapterType;
import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.models.missions.quests.QuestEvent;
import com.compileordie.pvz2.models.missions.quests.QuestManager;
import com.compileordie.pvz2.network.NetworkClient;
import com.compileordie.pvz2.network.protocol.Message;
import com.compileordie.pvz2.network.protocol.MessageType;
import com.compileordie.pvz2.views.game.ui.PlantFoodBank;
import com.compileordie.pvz2.views.helpers.ToastManager;
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

    public final GameRenderStates states;
    private final Set<String> brokenAssets;
    private TextureRegion bgLeftRegion;
    private TextureRegion bgMainRegion;
    private TextureRegion bgRightRegion;
    private boolean isPaused;
    private NinePatch tileHighlightPatch;
    private TextureRegion shovelCursorRegion;
    private float cursorAnimTime = 0f;
    private TextureRegion plantFoodCursorRegion;
    private TextureRegion plantFoodIcon;
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
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

        float mainY = 0f;
        float mainHeight = screenH;

        if (AppModel.currentChapter == ChapterType.FROSTBITE_CAVES) {
            mainHeight = screenH * 1.025f;
            float shiftDown = 25f; // Adjust pixel amount to shift down as needed
            mainY = -shiftDown;
        }

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
            batch.draw(bgMainRegion, leftWidth, mainY, mainWidth, mainHeight);
        } else if (bgMainRegion != null) {
            batch.draw(bgMainRegion, 0, mainY, screenW, mainHeight);
        }
    }

    public void drawGridLines(SpriteBatch batch) {
        if (AppModel.player == null || !AppModel.player.showGridBox) {
            return;
        }

        batch.end();

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);

        Gdx.gl.glLineWidth(8.0f);

        float tileW = Constants.Game.TILE_WIDTH * Constants.UI.METER_TO_PIX;
        float tileH = Constants.Game.TILE_HEIGHT * Constants.UI.METER_TO_PIX;
        float startX = Constants.Game.PADDING_X * Constants.UI.METER_TO_PIX;
        float startY = Constants.Game.PADDING_Y * Constants.UI.METER_TO_PIX;

        int rows = Constants.Game.BOARD_ROWS;
        int cols = Constants.Game.BOARD_COLS;

        float endX = startX + (cols * tileW);
        float endY = startY + (rows * tileH);

        // Vertical boundary lines
        for (int col = 0; col <= cols; col++) {
            float x = startX + (col * tileW);
            shapeRenderer.line(x, startY, x, endY);
        }

        // Horizontal boundary lines
        for (int row = 0; row <= rows; row++) {
            float y = startY + (row * tileH);
            shapeRenderer.line(startX, y, endX, y);
        }

        shapeRenderer.end();
        Gdx.gl.glLineWidth(1.0f); // Reset line thickness back to default
        batch.begin();
    }

    void drawMowers(SpriteBatch batch, PamPlayer player, float delta) {
        if (AppModel.gameSession == null || player == null || AppModel.currentLevel == LevelID.I_ZOMBIE) return;
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
            for (Tomb tomb : lane.getAllTombs()) {
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

        float drawX = (float) tomb.getPositionX() * Constants.UI.METER_TO_PIX;
        float drawY = (float) tomb.getPositionY() * Constants.UI.METER_TO_PIX;
        state.lastDrawX = drawX;
        state.lastDrawY = drawY;

        Color oldColor = null;
        try {
            if (state.damageAlphaTimer > 0) {
                oldColor = batch.getColor().cpy();
                batch.setColor(new Color(1f, 1f, 1f, 0.6f));
            }
            String myResolvedPath = getMyResolvedPath(tomb, resolvedPath);
            player.draw(batch, myResolvedPath, animName, 0f, drawX, drawY,
                GameScreenConstants.TOMB_SCALE, GameScreenConstants.TOMB_SCALE, true);
            if (oldColor != null) batch.setColor(oldColor);
        } catch (Throwable e) {
            brokenAssets.add(resolvedPath);
        } finally {
            if (state.damageAlphaTimer > 0) batch.setColor(Color.WHITE);
        }
    }

    private static String getMyResolvedPath(Tomb tomb, String resolvedPath) {
        String myResolvedPath;
        if (tomb.type == TombType.NORMAL) {
            myResolvedPath = resolvedPath;
        } else {
            if (tomb.type == TombType.SUN) {
                myResolvedPath = "768/FULL/GRAVESTONES/DARK_SUN/DARK_SUN.PAM";
            } else {
                if (tomb.type == TombType.PLANT_FOOD)
                    myResolvedPath = "768/FULL/GRAVESTONES/DARK_PLANTFOOD/DARK_PLANTFOOD.PAM";
                else {
                    myResolvedPath = "768/FULL/GRAVESTONES/DARK_NOOP/DARK_NOOP.PAM";
                }
            }
        }
        return myResolvedPath;
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
                    float craterX = ((Constants.Game.PADDING_X + (tile.column * Constants.Game.TILE_WIDTH)
                        + (Constants.Game.TILE_WIDTH / 2.0f)) * Constants.UI.METER_TO_PIX);
                    float craterY = ((Constants.Game.PADDING_Y + (tile.row * Constants.Game.TILE_HEIGHT)
                        + (Constants.Game.TILE_HEIGHT / 2.0f)) * Constants.UI.METER_TO_PIX);

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

        // I_ZOMBIE فقط: زامبی‌ساید (isReceiverClient=true) هیچ‌وقت authoritative نیست -
        // gameBoard اش هر ۱/۲۰ ثانیه کامل overwrite می‌شه، پس هر تغییری که خودش مستقیم روی
        // economyManager.sunAmount/suns بده، همون لحظه‌ی sync بعدی گم می‌شه (خورشید دوباره
        // "برمی‌گرده"). راه‌حل: به‌جای جمع‌آوری واقعی، فقط درخواست جمع‌آوری برای حریف
        // (پلنت‌ساید، simulator واقعی) می‌فرسته و همون لحظه (بدون صبر برای fade) محلی حذفش
        // می‌کنه تا تکراری نفرستد.
        boolean isReceiverSide = AppModel.currentLevel == LevelID.I_ZOMBIE && AppModel.isReceiverClient;

        var iterator = sunsList.iterator();
        while (iterator.hasNext()) {
            var sun = iterator.next();
            GameRenderStates.SunRenderState state =
                states.sunRenderStates.computeIfAbsent(sun, _ -> new GameRenderStates.SunRenderState());
            state.animTime += delta;
            // --- FIX: Removed Double-Padding! Sun coordinates are already world coordinates! ---
            float baseX = (float) (sun.getX() * Constants.UI.METER_TO_PIX);
            float baseY = (float) (sun.getY() * Constants.UI.METER_TO_PIX);
            boolean isHovered = false;
            if (!state.isFading) {
                float dx = baseX - mousePos.x;
                float dy = baseY - mousePos.y;
                if (dx * dx + dy * dy <= 2500) {
                    isHovered = true;
                    state.isFading = true;
                }
            }

            if (isReceiverSide) {
                if (isHovered) {
                    requestSunCollectFromOpponent(sun);
                    iterator.remove();
                    states.sunRenderStates.remove(sun);
                    continue;
                }
                // فرصت خیلی کوچیک بین اسپاون یه خورشید تازه (که هنوز state.isFading اش
                // false شده تو همین فریم) و ساینک بعدی: اگه از فریم قبل isFading مونده
                // (مثلا یه سری از کد قدیمی که هنوز نرسیده sync بشه)، بازم فوراً درخواست بفرست.
                if (state.isFading) {
                    requestSunCollectFromOpponent(sun);
                    iterator.remove();
                    states.sunRenderStates.remove(sun);
                    continue;
                }
            }

            float alpha = 1f;
            if (state.isFading) {
                state.fadeTimer += delta;
                alpha = Math.max(0f, 1f - (state.fadeTimer / 0.5f));
                if (state.fadeTimer >= 0.3f) {
                    collectSun(AppModel.gameSession.gameBoard, sun);
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

    /**
     * منطق واقعی «جمع‌کردن یک خورشید» (افزایش sunAmount، دیسپچ کوئست، و ترکاندن خورشید
     * رادیواکتیو اگه لازم بود). این متد باید فقط روی gameBoard ای صدا زده بشه که واقعاً
     * authoritative است - یعنی: خودِ پلنت‌ساید (isReceiverClient=false) وقتی خودش hover
     * کرده (پایین‌تر، داخل همین حلقه)، یا پلنت‌ساید وقتی یه IZOMBIE_SUN_COLLECT_REQUEST از
     * حریفش (زامبی‌ساید) می‌گیره (نگاه کنید GameScreen.processNetworkInput).
     */
    static void collectSun(GameBoard gameBoard, Sun sun) {
        if (sun.type.name().contains("RADIOACTIVE")) {
            GameScreenController.explodeSun(gameBoard, sun);
        }
        QuestManager.dispatch(QuestEvent.SUN_COLLECTED, sun.type.value, null);
        gameBoard.economyManager.sunAmount += sun.type.value;
        gameBoard.economyManager.totalSunsGenerated += sun.type.value;
    }

    /**
     * فقط زامبی‌ساید (isReceiverClient=true) این را صدا می‌زند: به‌جای جمع‌آوری محلی
     * (که چون gameBoard اش authoritative نیست بی‌فایده‌ست)، از حریفش (پلنت‌ساید، مالک
     * واقعی simulation) می‌خواهد خودش این خورشید را جمع کند. شناسایی خورشید روی سرور و
     * پلنت‌ساید صرفاً با مختصات x/y انجام می‌شود (Sun شناسه‌ی یکتا ندارد؛ چون suns هیچ‌وقت
     * روی هم نمی‌افتن، این کافی است).
     */
    private void requestSunCollectFromOpponent(Sun sun) {
        if (AppModel.opponentUsername == null || !NetworkClient.getInstance().isConnected()) return;
        NetworkClient.getInstance().send(new Message(MessageType.IZOMBIE_SUN_COLLECT_REQUEST)
            .put("target", AppModel.opponentUsername)
            .put("x", String.valueOf(sun.getX()))
            .put("y", String.valueOf(sun.getY())));
    }

    void initSelectionAssets(TextureBank textureBank) {
        TextureRegion selectRegion = textureBank.region("IMAGE_UI_PACKETS_SELECT");
        if (selectRegion != null) {
            this.tileHighlightPatch = new NinePatch(selectRegion, 8, 8, 8, 8);
        }
        this.shovelCursorRegion = textureBank.region("IMAGE_UI_HUD_INGAME_SHOVEL_ICON");
        this.plantFoodCursorRegion = textureBank.region("IMAGE_UI_HUD_INGAME_PLANTFOOD_BUTTON");
        this.plantFoodIcon = textureBank.region("IMAGE_UI_HUD_INGAME_PLANTFOOD_BUTTON");
    }

    void drawTileHighlight(SpriteBatch batch, Tile tile) {
        if (tile == null || tileHighlightPatch == null) return;
        if (GameScreenController.selectedPlantCard == null
            && GameScreenController.selectedZombieCard == null
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

        // 1. Plant Card Selected
        if (GameScreenController.selectedPlantCard != null && plantAssets != null) {
            PlantType plantType = GameScreenController.selectedPlantCard.plantType;
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
        // 4. Zombie Card Selected
        else if (GameScreenController.selectedZombieCard != null && plantAssets != null) {
            String nameStr = GameScreenController.selectedZombieCard.zombieType.name();
            ZombieType zombieType = GameScreenController.selectedZombieCard.zombieType;
            ChapterType previousChapter = AppModel.currentChapter;

            ZombieVisualRegistry.ZombieVisualDef def = ZombieVisualRegistry.get(nameStr);
            if (def != null && !def.pams.isEmpty()) {
                float drawX = mousePos.x;
                float drawY = mousePos.y;

                Matrix4 original = batch.getTransformMatrix().cpy();
                Matrix4 scaled = original.cpy()
                    .translate(drawX, drawY, 0)
                    .scale(0.55f, 0.55f, 1f)
                    .translate(-drawX, -drawY, 0);

                batch.setTransformMatrix(scaled);

                // Draw all PAM layers (e.g., base zombie + cone)
                for (ZombieVisualRegistry.PamSpec spec : def.pams) {
                    String path = spec.getResolvedPath();
                    try {
                        plantAssets.getRawPlayer().draw(batch, path, "", cursorAnimTime, drawX, drawY, true);
                    } catch (Throwable e) {
                        Gdx.app.error("PVZ-CURSOR", "Missing zombie asset: " + path);
                    }
                }
                batch.setTransformMatrix(original);
            }
            AppModel.currentChapter = previousChapter;
        }
    }

    void drawPlantFoods(SpriteBatch batch, float delta, Vector3 mousePos) {
        if (AppModel.gameSession == null || AppModel.gameSession.gameBoard == null || plantFoodIcon == null) return;
        var plantFoods = AppModel.gameSession.gameBoard.plantFoods;
        if (plantFoods == null || plantFoods.isEmpty()) return;

        float w = plantFoodIcon.getRegionWidth() * 0.8f;
        float h = plantFoodIcon.getRegionHeight() * 0.8f;

        var iterator = plantFoods.iterator();
        while (iterator.hasNext()) {
            var pf = iterator.next();
            GameRenderStates.PlantFoodRenderState state =
                states.plantFoodRenderStates.computeIfAbsent(pf, p -> new GameRenderStates.PlantFoodRenderState());

            float drawX = (float) pf.getX() * Constants.UI.METER_TO_PIX;
            float drawY = (float) pf.getY() * Constants.UI.METER_TO_PIX;

            // Mouse hover distance check (50px radius squared = 2500)
            if (!state.isFading) {
                float dx = drawX - mousePos.x;
                float dy = drawY - mousePos.y;
                if (dx * dx + dy * dy <= 2500) {
                    state.isFading = true;
                }
            }

            float alpha = 1f;
            if (state.isFading) {
                state.fadeTimer += delta;
                alpha = Math.max(0f, 1f - (state.fadeTimer / 0.5f));
                if (state.fadeTimer >= 0.3f) {
                    // Increment plant food count and collect item
                    if (AppModel.player.plantFoodCount < PlantFoodBank.MAX_PLANT_FOOD) {
                        AppModel.player.plantFoodCount++;
                    }
                    iterator.remove();
                    states.plantFoodRenderStates.remove(pf);
                    continue;
                }
            }

            Color oldColor = batch.getColor().cpy();
            batch.setColor(oldColor.r, oldColor.g, oldColor.b, alpha);
            batch.draw(plantFoodIcon, drawX - (w / 2f), drawY - (h / 2f), w, h);
            batch.setColor(oldColor);
        }
    }

    void drawSeedPackets(SpriteBatch batch, PlantAssetManager plantAssets, float delta, Vector3 mousePos) {
        if (AppModel.gameSession == null || AppModel.gameSession.gameBoard == null || plantAssets == null) return;
        var seedPackets = AppModel.gameSession.gameBoard.seedPackets;
        if (seedPackets == null || seedPackets.isEmpty()) return;
        var iterator = seedPackets.iterator();
        while (iterator.hasNext()) {
            var packet = iterator.next();
            GameRenderStates.SeedPacketRenderState state =
                states.seedPacketRenderStates.computeIfAbsent(packet, _ ->
                    new GameRenderStates.SeedPacketRenderState());
            state.animTime += delta;
            float drawX = (float) packet.getX() * Constants.UI.METER_TO_PIX;
            float drawY = (float) packet.getY() * Constants.UI.METER_TO_PIX;
            if (!state.isFading) {
                float dx = drawX - mousePos.x;
                float dy = drawY - mousePos.y;
                if (dx * dx + dy * dy <= 2500) { // Mouse hover distance check (50px radius squared = 2500)
                    state.isFading = true;
                }
            }
            float alpha = 1f;
            if (state.isFading) {
                state.fadeTimer += delta;
                alpha = Math.max(0f, 1f - (state.fadeTimer / 0.5f));
                if (state.fadeTimer >= 0.3f) {
                    PlantType plantType = packet.plantType;
                    AppModel.gameSession.gameBoard.economyManager.plantCards.add(new PlantCard(plantType));
                    ToastManager.showMessage("Obtained " + plantType + "!");
                    iterator.remove();
                    states.seedPacketRenderStates.remove(packet);
                    continue;
                }
            }
            PlantType plantType = packet.plantType;
            ClipRef clip = plantAssets.loadPlantClip(plantType);
            if (clip != null) {
                Color oldColor = batch.getColor().cpy();
                batch.setColor(oldColor.r, oldColor.g, oldColor.b, alpha);
                Matrix4 original = batch.getTransformMatrix().cpy();
                Matrix4 scaled = original.cpy()
                    .translate(drawX, drawY, 0)
                    .scale(0.55f, 0.55f, 1f)
                    .translate(-drawX, -drawY, 0);
                batch.setTransformMatrix(scaled);
                plantAssets.drawPlant(batch, clip, state.animTime, drawX, drawY, true);
                batch.setTransformMatrix(original);
                batch.setColor(oldColor);
            }
        }
    }
}
