//package com.compileordie.pvz2.views.game;
//
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.Screen;
//import com.badlogic.gdx.graphics.g2d.SpriteBatch;
//import com.badlogic.gdx.graphics.g2d.TextureRegion;
//import com.badlogic.gdx.math.Vector3;
//import com.badlogic.gdx.utils.ScreenUtils;
//import com.badlogic.gdx.utils.viewport.ScreenViewport;
//import com.compileordie.pvz2.models.AppModel;
//import com.compileordie.pvz2.models.game.GameSession;
//import com.compileordie.pvz2.models.game.board.GameBoard;
//import pvz.libpvz.pam.PamPlayer;
//import pvz.libpvz.textures.TextureBank;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class GameScreen implements Screen {
//
//    private SpriteBatch batch;
//    private ScreenViewport viewport;
//    private TextureBank textureBank;
//    private PamPlayer player;
//
//    private TextureRegion bgLeftRegion;
//    private TextureRegion bgMainRegion;
//    private TextureRegion bgRightRegion;
//
//    // --- تایمر عمومی بازی (Public Game Timer) ---
//    public float gameTimer = 0f;
//
//    private final Map<String, Boolean> visibility = new HashMap<>();
//
//    // --- تنظیمات و ثابت‌های اصلی پروژه ---
//    public static final float SPAWN_X = 1910f;          // نقطه اسپاون زامبی‌ها
//    public static final float CAR_X = 614f;             // نقطه حضور ماشین‌های چمن‌زن
//    public static final float GAME_OVER_X = 566f;       // حداکثر پیشروی زامبی‌ها (خوردن خانه)
//    public static final float ZOMBIE_SPEED = 14f;       // سرعت حرکت زامبی
//    public static final float ZOMBIE_SCALE = 13f / 18f; // ضریب مقیاس (ارتفاع 130)
//
//    // مختصات Y جدید لاین‌های زامبی (۲۵ واحد کاهش یافته)
//    private final float[] ZOMBIE_DRAW_Y = {675f, 550f, 425f, 300f, 175f};
//
//    // زمان‌بندی دقیق اسپاون بر حسب ثانیه برای ۵ لاین
//    private final float[] ZOMBIE_SPAWN_DELAYS = {1.0f, 4.0f, 7.0f, 10.0f, 20.0f};
//
//    // لیست زامبی‌های فعال
//    private final List<Zombie> zombies = new ArrayList<>();
//
//    // متغیر کمکی برای چاپ مختصات کلیک
//    private final Vector3 touchPoint = new Vector3();
//
//    // --- کلاس داخلی مدیریت زامبی ---
//    private static class Zombie {
//        float x;
//        float y;
//        int lineIndex;
//        float spawnDelay;   // زمان دقیق اسپاون به ثانیه
//        boolean isSpawned = false;
//        boolean isEating = false;
//        float animTime = 0f; // تایمر اختصاصی انیمیشن هر زامبی
//
//        GameSession mySession = AppModel.gameSession;
//        GameBoard myBorad = AppModel.gameSession.gameBoard;
//        ArrayList<com.compileordie.pvz2.models.entities.zombies.variants.Zombie> myZombies = myBorad.getAllZombies();
//
//        public Zombie(int lineIndex, float y, float spawnDelay) {
//            this.lineIndex = lineIndex;
//            this.x = SPAWN_X;
//            this.y = y;
//            this.spawnDelay = spawnDelay;
//        }
//
//        public void update(float delta, float gameTimer) {
//            // ۱. بررسی رسیدن زمان اسپاون با تایمر عمومی
//            if (!isSpawned) {
//                if (gameTimer >= spawnDelay) {
//                    isSpawned = true;
//                    animTime = 0f; // شروع تایمر انیمیشن از zero
//                } else {
//                    return; // هنوز زمان اسپاون نرسیده
//                }
//            }
//
//            // ۲. آپدیت زمان اختصاصی انیمیشن این زامبی
//            animTime += delta;
//
//            // ۳. منطق حرکت و خوردن
//            if (!isEating) {
//                x -= ZOMBIE_SPEED * delta;
//
//                // رسیدن به خط ۵۶۶ و شروع به خوردن
//                if (x <= GAME_OVER_X) {
//                    x = GAME_OVER_X;
//                    isEating = true;
//                    animTime = 0f; // ریست تایمر انیمیشن جهت اجرای روان انیمیشن eat
//                }
//            }
//        }
//    }
//
//    @Override
//    public void show() {
//        batch = new SpriteBatch();
//        viewport = new ScreenViewport();
//        gameTimer = 0f; // ریست تایمر عمومی با شروع گیم اسکرین
//
//        try {
//            textureBank = new TextureBank("768", Gdx.files.internal("pvz-assets"));
//            player = new PamPlayer(textureBank, Gdx.files.internal("pvz-assets"));
//
//            bgLeftRegion = textureBank.region("IMAGE_BACKGROUNDS_EGYPT_TEXTURE_LEFT");
//            bgMainRegion = textureBank.region("IMAGE_BACKGROUNDS_EGYPT_TEXTURE");
//            bgRightRegion = textureBank.region("IMAGE_BACKGROUNDS_EGYPT_TEXTURE_RIGHT");
//
//            if (bgMainRegion == null) {
//                Gdx.app.error("PVZ-DEBUG", "❌ تصاویر پس‌زمینه پیدا نشدند!");
//            } else {
//                Gdx.app.log("PVZ-DEBUG", "✅ SUCCESS: All Egypt background segments loaded successfully!");
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        // تعریف ۵ زامبی با زمان‌های اسپاون دقیق (1s, 6s, 3s, 9s, 2s) و مختصات Y جدید
//        for (int i = 0; i < 5; i++) {
//            zombies.add(new Zombie(i, ZOMBIE_DRAW_Y[i], ZOMBIE_SPAWN_DELAYS[i]));
//        }
//
//        visibility.put("zombie_armor_brick_norm", true);
//    }
//
//    @Override
//    public void render(float delta) {
//        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
//
//        // آپدیت تایمر عمومی بازی
//        gameTimer += delta;
//
//        if (textureBank != null) {
//            textureBank.update();
//        }
//
//        // ۱. ثبت و چاپ مختصات کلیک ماوس
//        if (Gdx.input.justTouched()) {
//            touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
//            viewport.unproject(touchPoint);
//
//            int worldX = (int) touchPoint.x;
//            int worldY = (int) touchPoint.y;
//
//            String logMsg = String.format("📍 Click Position -> X: %d | Y: %d", worldX, worldY);
//            Gdx.app.log("COORDS", logMsg);
//            System.out.println(logMsg);
//        }
//
//        // ۲. به‌روزرسانی زامبی‌ها بر اساس تایمر عمومی بازی
//        for (Zombie zombie : zombies) {
//            zombie.update(delta, gameTimer);
//        }
//
//        viewport.getCamera().update();
//        batch.setProjectionMatrix(viewport.getCamera().combined);
//
//        batch.begin();
//
//        // ۳. رسم پس‌زمینه سه تکه
//        float screenW = Gdx.graphics.getWidth();
//        float screenH = Gdx.graphics.getHeight();
//
//        if (bgLeftRegion != null && bgMainRegion != null && bgRightRegion != null) {
//            float leftAspect = (float) bgLeftRegion.getRegionWidth() / bgLeftRegion.getRegionHeight();
//            float leftWidth = screenH * leftAspect;
//            batch.draw(bgLeftRegion, 0, 0, leftWidth, screenH);
//
//            float rightAspect = (float) bgRightRegion.getRegionWidth() / bgRightRegion.getRegionHeight();
//            float rightWidth = screenH * rightAspect;
//            float visibleRightWidth = rightWidth * 0.25f;
//            float rightX = screenW - visibleRightWidth;
//
//            batch.draw(bgRightRegion, rightX, 0, rightWidth, screenH);
//
//            float mainWidth = rightX - leftWidth;
//            batch.draw(bgMainRegion, leftWidth, 0, mainWidth, screenH);
//        } else if (bgMainRegion != null) {
//            batch.draw(bgMainRegion, 0, 0, screenW, screenH);
//        }
//
//        // ۴. رسم زامبی‌ها بعد از اسپاون
//        if (player != null) {
//            for (Zombie zombie : zombies) {
//                if (zombie.isSpawned) {
//                    String animName = zombie.isEating ? "eat" : "walk";
//
//                    player.draw(
//                        batch,
//                        "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_BASIC/ZOMBIE_EGYPT_BASIC.PAM",
//                        animName,
//                        zombie.animTime,
//                        zombie.x,
//                        zombie.y,
//                        ZOMBIE_SCALE,
//                        ZOMBIE_SCALE,
//                        true
//                    );
//                }
//            }
//        }
//
//        batch.end();
//    }
//
//    @Override
//    public void resize(int width, int height) {
//        if (width <= 0 || height <= 0) return;
//        viewport.update(width, height, true);
//    }
//
//    @Override public void pause() {}
//    @Override public void resume() {}
//    @Override public void hide() {}
//
//    @Override
//    public void dispose() {
//        if (batch != null) batch.dispose();
//    }
//}
