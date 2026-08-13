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
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.ProspectorZombie;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * View لایه‌ی اصلی گیم‌پلی (MVC - View).
 *
 * این کلاس هیچ منطق بازی‌ای تولید نمی‌کند؛ فقط:
 * 1) هر فریم، گیم‌سشن مدل (AppModel.gameSession) را با گام زمانی ثابت tick می‌زند.
 * 2) وضعیت فعلی مدل (لیست زامبی‌های GameBoard) را می‌خواند و رسم می‌کند.
 * 3) پس‌زمینه و ظاهر زامبی‌ها را بر اساس AppModel.currentChapter انتخاب می‌کند.
 *
 * تمام مسیرهای asset و ثابت‌های رندر در {@link GameScreenConstants} و
 * {@link ZombieVisualRegistry} نگه‌داری می‌شوند تا این کلاس تمیز و خوانا بماند.
 */
public class GameScreen implements Screen {
    private final ZombieTestSpawner testSpawner = new ZombieTestSpawner();

    private SpriteBatch batch;
    private ScreenViewport viewport;
    private TextureBank textureBank;
    private PamPlayer player;

    private TextureRegion bgLeftRegion;
    private TextureRegion bgMainRegion;
    private TextureRegion bgRightRegion;

    // متغیر برای دریافت مختصات کلیک بدون ساختن شیء جدید در هر فریم
    private final Vector3 touchPoint = new Vector3();

    // انباشتگر زمان برای پیشبرد شبیه‌سازی مدل با گام ثابت (fixed timestep)،
    // مستقل از نوسانات فریم‌ریت رندر.
    private float simulationAccumulator = 0f;

    /**
     * وضعیت رندر مخصوص هر زامبی (تایمر انیمیشن و آخرین حالت eating/walking).
     * عمدا این اطلاعات داخل مدل (Zombie.java) نگه‌داری نمی‌شود، چون این‌ها صرفا
     * جزئیات نمایشی (View) هستند و مدل نباید چیزی درباره‌ی رندر بداند (MVC).
     */
    private static final class ZombieRenderState {
        float animTime = 0f;
        boolean wasEating = false;
        // آخرین جهت معتبر (بر اساس علامت xSpeed) - وقتی xSpeed دقیقا صفره (مثلا لحظه‌ی
        // توقف کامل)، جهت قبلی حفظ می‌شود تا زامبی یهو نچرخد.
        boolean flip = true;
    }

    private final Map<Zombie, ZombieRenderState> zombieRenderStates = new HashMap<>();

    @Override
    public void show() {
        batch = new SpriteBatch();
        viewport = new ScreenViewport();
        simulationAccumulator = 0f;
        zombieRenderStates.clear();

        loadAssets();
    }

    private void loadAssets() {
        try {
            textureBank = new TextureBank(GameScreenConstants.ASSET_RESOLUTION, Gdx.files.internal("pvz-assets"));
            player = new PamPlayer(textureBank, Gdx.files.internal("pvz-assets"));

            String chapterFolder = GameScreenConstants.chapterFolder(AppModel.currentChapter);

            bgLeftRegion = textureBank.region(GameScreenConstants.BG_REGION_PREFIX + chapterFolder + GameScreenConstants.BG_TEXTURE_LEFT_SUFFIX);
            bgMainRegion = textureBank.region(GameScreenConstants.BG_REGION_PREFIX + chapterFolder + GameScreenConstants.BG_TEXTURE_SUFFIX);
            bgRightRegion = textureBank.region(GameScreenConstants.BG_REGION_PREFIX + chapterFolder + GameScreenConstants.BG_TEXTURE_RIGHT_SUFFIX);

            if (bgMainRegion == null) {
                Gdx.app.error("PVZ-DEBUG", "❌ تصویر پس‌زمینه اصلی برای چپتر '" + chapterFolder + "' پیدا نشد!");
            } else {
                Gdx.app.log("PVZ-DEBUG", "✅ پس‌زمینه چپتر '" + chapterFolder + "' با موفقیت لود شد.");
            }
        } catch (Exception e) {
            Gdx.app.error("PVZ-DEBUG", "❌ خطا در بارگذاری asset های گیم‌اسکرین: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        if (textureBank != null) {
            textureBank.update();
        }

        // ===============================================
        // لاگ گرفتن مختصات کلیک ماوس / لمس صفحه
        // ===============================================
        if (Gdx.input.justTouched()) {
            touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchPoint); // تبدیل به مختصات دوربین/دنیای بازی

            // تبدیل پیکسل به متر برای هماهنگی با مدل
            float meterX = touchPoint.x / Constants.UI.MeterToPix;
            float meterY = touchPoint.y / Constants.UI.MeterToPix;

            Gdx.app.log("PVZ-CLICK", String.format(
                "🖱️ مختصات کلیک -> پیکسل: (X: %.1f, Y: %.1f) | متر-مدل: (X: %.2f, Y: %.2f)",
                touchPoint.x, touchPoint.y, meterX, meterY
            ));
        }

        advanceSimulation(delta);

        // خط زیر رو هر وقت خواستی زامبی‌ها اسپاون بشن از کامنت دربیار:
        // (قبلا این خط با وجود کامنت بالا، به‌اشتباه فعال بود و همیشه در حال اسپاون تستی بود)
        testSpawner.update(delta);

        viewport.getCamera().update();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        drawBackground();
        drawZombies(delta);
        batch.end();
    }

    /**
     * گیم‌سشن را با گام زمانی ثابت جلو می‌برد (طبق Constants.Game.TIME_COEFFICIENT).
     * این‌طوری فیزیک/سرعت زامبی‌ها مستقل از فریم‌ریت واقعی دستگاه یکسان می‌ماند،
     * چون خود مدل (مثلا Zombie.move) از «تعداد تیک × TIME_COEFFICIENT» به عنوان دلتای
     * زمانی استفاده می‌کند.
     */
    private void advanceSimulation(float delta) {
        if (AppModel.gameSession == null) {
            return;
        }

        simulationAccumulator += delta;
        while (simulationAccumulator >= GameScreenConstants.SIMULATION_STEP_SECONDS) {
            AppModel.gameSession.tick(1);
            simulationAccumulator -= GameScreenConstants.SIMULATION_STEP_SECONDS;
        }
    }

    private void drawBackground() {
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

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

    /**
     * رندر تمام زامبی‌های فعلی موجود در مدل (GameBoard.getAllZombies()).
     * این متد صرفا از مدل می‌خواند و چیزی در آن تغییر نمی‌دهد (View خالص).
     */
    private void drawZombies(float delta) {
        if (AppModel.gameSession == null || player == null) {
            return;
        }

        List<Zombie> currentZombies = new ArrayList<>(AppModel.gameSession.gameBoard.getAllZombies());

        // پاکسازی وضعیت رندر زامبی‌هایی که دیگر روی نقشه نیستند (مرده/حذف‌شده) تا نشتی حافظه نداشته باشیم
        Set<Zombie> aliveSet = new HashSet<>(currentZombies);
        zombieRenderStates.keySet().removeIf(z -> !aliveSet.contains(z));

        // مرتب‌سازی بر اساس مختصات Y (یا شماره ردیف):
        // چون در سیستم مختصات بازی، معمولاً خطوط پایین‌تر مقدار Y کمتری دارند (یا برعکس، بسته به پیاده‌سازی مدل شما)،
        // برای اینکه زامبی‌های پایین‌تر روی زامبی‌های بالاتر قرار بگیرند، لیست را بر اساس مقادیر Y صعودی یا نزولی مرتب می‌کنیم.
        currentZombies.sort((z1, z2) -> Double.compare(z2.getY(), z1.getY()));

        for (Zombie zombie : currentZombies) {
            ZombieRenderState state = zombieRenderStates.computeIfAbsent(zombie, z -> new ZombieRenderState());

            boolean isEatingNow = zombie.isEating();
            if (isEatingNow != state.wasEating) {
                state.animTime = 0f; // تغییر حالت راه‌رفتن/خوردن -> ریست تایمر انیمیشن برای پخش روان
                state.wasEating = isEatingNow;
            }

            drawSingleZombie(zombie, state, delta);
        }
    }

    private void drawSingleZombie(Zombie zombie, ZombieRenderState state, float delta) {
        String typeKey = zombie.getType().name();

        ZombieVisualRegistry.ZombieVisualDef def = ZombieVisualRegistry.get(typeKey);

        if (def == null) {
            Gdx.app.error("PVZ-ASSET-MISSING",
                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n"
                    + "❌ [هیچ تعریف بصری‌ای ثبت نشده] برای نوع زامبی: '" + typeKey + "'\n"
                    + "این زامبی رندر نخواهد شد. ZombieVisualRegistry را بررسی کنید.\n"
                    + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            return;
        }

        // =========================================================
        // تنظیم دینامیک سرعت انیمیشن برای جلوگیری از لیز خوردن پاها
        // =========================================================
        float animSpeedMultiplier;
        if (zombie.isEating()) {
            animSpeedMultiplier = 1.0f; // انیمیشن eat مستقل از سرعت حرکت است
        } else if (!zombie.canMove()) {
            animSpeedMultiplier = 0f; // زامبی گیر کرده/متوقف شده؛ نباید پا بزند
        } else {
            double normalSpeed = zombie.getStableSpeed();
            double currentSpeed = Math.abs(zombie.getXSpeed());
            animSpeedMultiplier = normalSpeed > 0 ? (float) (currentSpeed / normalSpeed) : 1f;
        }

        state.animTime += delta * animSpeedMultiplier;
        // =========================================================

        float baseX = (float) zombie.getX() * Constants.UI.MeterToPix;
        float baseY = (float) zombie.getY() * Constants.UI.MeterToPix;

        // تعیین نام انیمیشن بر اساس نوع زامبی و وضعیت آن
        String animName;
        if (zombie.isEating()) {
            animName = "eat";
        } else if ("PIANIST_ZOMBIE".equals(typeKey)) {
            // پیانیست کلید walk ندارد، از play استفاده می‌کنیم
            animName = "play";
        } else {
            animName = "walk";
        }

        // جهت صورت زامبی بر اساس علامت واقعی سرعت افقی (xSpeed)
        double xSpeed = zombie.getXSpeed();
        if (xSpeed > 0) {
            state.flip = true;  // حرکت به چپ (حالت عادی) -> پیش‌فرض آرت رو به راسته پس flip=true
        } else if (xSpeed < 0) {
            state.flip = false; // حرکت به راست (مثلا هیپنوتایز)
        }
        boolean flip = state.flip;

        // -----------------------------------------------------------------
        // نکته‌ی اصلی این اصلاح:
        // libPVZ متدی به اسم setState(String, String, boolean) روی PamPlayer
        // ندارد. فعال‌سازی state های ظاهری (زره‌ها: Conehead/Buckethead/
        // Blockhead/Knight و ...) باید با ساخت یک Map<String, Boolean>
        // "visibility map" انجام شود.
        //
        // اما با نگاه به سورس واقعی PamPlayer، هیچ overloadی از draw(...)
        // وجود ندارد که هم‌زمان scaleX/scaleY و هم partsVisibility بگیرد:
        //   draw(batch, pam, clip, time, x, y, loop, partsVisibility)      -> بدون scale
        //   draw(batch, pam, clip, time, x, y, scaleX, scaleY, loop)       -> بدون visibility
        // پس وقتی زامبی زره دارد (visibilityMap != null)، به‌جای پارامتر
        // scale (که این overload اصلا نمی‌گیرد)، مقیاس را با transform
        // matrix خودِ Batch، دقیقا حول نقطه‌ی (drawX, drawY)، اعمال می‌کنیم.
        // این کار همان اثر بصری root.set(scaleX, ...) داخل drawInternal را
        // تولید می‌کند (از جمله فلیپ افقی با علامت منفی scaleX).
        // -----------------------------------------------------------------
        List<String> stateFilters = def.getResolvedStateFilters();
        Map<String, Boolean> visibilityMap = null;
        if (!stateFilters.isEmpty()) {
            visibilityMap = new HashMap<>();
            for (String state2 : stateFilters) {
                visibilityMap.put(state2, true);
            }
        }

        for (ZombieVisualRegistry.PamSpec part : def.pams) {
            String resolvedPath = part.getResolvedPath();

            try {
                float drawX = baseX + part.offsetX * Constants.UI.MeterToPix;
                float drawY = baseY + part.offsetY * Constants.UI.MeterToPix;
                int flag = 1;
                if (zombie.getType() == ZombieType.PROSPECTOR_ZOMBIE) {
                    if (((ProspectorZombie) (zombie)).isReversedDirection) flag = -1;
                }
                if (zombie.isHypnotized()) flag = -1;

                float scaleX = flag * GameScreenConstants.ZOMBIE_SCALE;
                float scaleY = GameScreenConstants.ZOMBIE_SCALE;

                if (visibilityMap != null) {
                    // مقیاس‌دهی دستی از طریق transform matrix چون overload
                    // مربوطه پارامتر scale ندارد.
                    com.badlogic.gdx.math.Matrix4 originalTransform = batch.getTransformMatrix().cpy();
                    com.badlogic.gdx.math.Matrix4 scaledTransform = originalTransform.cpy()
                        .translate(drawX, drawY, 0f)
                        .scale(scaleX, scaleY, 1f)
                        .translate(-drawX, -drawY, 0f);

                    batch.setTransformMatrix(scaledTransform);
                    player.draw(
                        batch,
                        resolvedPath,
                        animName,
                        state.animTime,
                        drawX,
                        drawY,
                        flip,
                        visibilityMap
                    );
                    batch.setTransformMatrix(originalTransform);
                } else {
                    player.draw(
                        batch,
                        resolvedPath,
                        animName,
                        state.animTime,
                        drawX,
                        drawY,
                        scaleX,
                        scaleY,
                        flip
                    );
                }
            } catch (Exception e) {
                Gdx.app.error("PVZ-ASSET-MISSING",
                    "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n"
                        + "❌ عدم موفقیت در رندر زامبی '" + typeKey + "'\n"
                        + "مسیر تلاش‌شده: " + resolvedPath + "\n"
                        + "علت: " + e.getMessage() + "\n"
                        + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        zombieRenderStates.clear();
    }
}
