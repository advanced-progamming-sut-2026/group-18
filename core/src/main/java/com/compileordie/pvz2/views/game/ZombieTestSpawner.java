package com.compileordie.pvz2.views.game;

import com.badlogic.gdx.Gdx;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.zombies.ZombieBuilder;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.ZomBoss.DarkZomboss;
import com.compileordie.pvz2.models.entities.zombies.variants.ZomBoss.EgyptZomboss;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.levels.ChapterType; // 👈 این ایمپورت اضافه شد

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * کلاس کمکی جهت تست و اسپاون متوالی تمام زامبی‌های موجود در بازی.
 * این کلاس بدون دستکاری منطق اصلی GameScreen، هر ۳ ثانیه یک زامبی جدید
 * در یکی از ۵ ردیف اسپاون می‌کند.
 */
public class ZombieTestSpawner {

    private float timer = 0f;
    private static final float SPAWN_INTERVAL = 0.5f; // فاصله زمانی ۳ ثانیه

    private int currentZombieIndex = 0;
    private int currentLane = 1;

    private final List<ZombieType> zombieTypes = new ArrayList<>();
    private final Random random = new Random();

    // 👈 با احتمال ۲۰٪، زامبی به‌جای لبه‌ی راست، از وسط زمین اسپاون می‌شه
    // (مثلا برای تست زامبی‌هایی که باید وسط زمین ظاهر بشن، مثل ایمپ پرتاب‌شده
    // توسط غول یا هر تست دیگه‌ای که نیاز به اسپاون میانی داره).
    private static final float MIDDLE_SPAWN_CHANCE = 0.2f;

    // لبه‌ی راست نقشه (رفتار پیش‌فرض قبلی)
    private static final float EDGE_SPAWN_X = 18f;

    // وسط زمین: ۹ ستون داریم (0..8)، ستون وسط ۴ ام، مرکزش هم مثل بقیه‌ی
    // اسپاون‌های میانی موجود در پروژه (مثلاً ZombieManager) باید علاوه‌بر
    // آفست نیم‌کاشی، PADDING_X_REALITY رو هم داشته باشه؛ وگرنه با مدل
    // هماهنگ نیست و به‌جای وسط زمین، سمت چپ صفحه (قبل از شروع تخته) ظاهر می‌شه.
    private static final float MIDDLE_SPAWN_X =
        Constants.Game.PADDING_X_REALITY + (4 + 0.5f) * (float) Constants.Game.TILE_WIDTH;

    // 👑 حالت تست اختصاصی زامباس: وقتی true باشه، این کلاس هیچ زامبی معمولی‌ای
    // اسپاون نمی‌کنه؛ فقط یک‌بار (اولین فراخوانی update) یک EgyptZomboss می‌سازه.
    private final boolean zombossOnlyMode;
    private boolean zombossSpawned = false;

    /**
     * سه حالت ممکن برای این کلاس:
     *  - NORMAL         : همون رفتار قبلی (چرخیدن روی همه‌ی انواع زامبی).
     *  - EGYPT_ZOMBOSS   : فقط یک EgyptZomboss اسپاون می‌کنه (رفتار قبلیِ true).
     *  - DARK_ZOMBOSS    : فقط یک DarkZomboss اسپاون می‌کنه.
     */
    public enum TestMode {
        NORMAL,
        EGYPT_ZOMBOSS,
        DARK_ZOMBOSS
    }

    private final TestMode mode;

    public ZombieTestSpawner() {
        this(TestMode.NORMAL);
    }

    /**
     * @param zombossOnlyMode اگه true باشه، به‌جای تست‌اسپاون همه‌ی انواع
     *                        زامبی، فقط یک EgyptZomboss (برای تست انیمیشن‌ها/
     *                        افکت انفجارش) اسپاون می‌شه.
     */
    public ZombieTestSpawner(boolean zombossOnlyMode) {
        this(zombossOnlyMode ? TestMode.EGYPT_ZOMBOSS : TestMode.NORMAL);
    }

    /**
     * @param mode حالت تست: NORMAL (همه‌ی زامبی‌ها)، EGYPT_ZOMBOSS (فقط یک
     *             EgyptZomboss) یا DARK_ZOMBOSS (فقط یک DarkZomboss).
     */
    public ZombieTestSpawner(TestMode mode) {
        this.mode = mode;
        this.zombossOnlyMode = (mode == TestMode.EGYPT_ZOMBOSS); // برای سازگاری با کد قدیمی که بهش رجوع می‌کنه

        if (mode == TestMode.EGYPT_ZOMBOSS) {
            // زامباس مصر فقط برای مصر پیاده شده، پس چپترو رو مصر می‌ذاریم
            // (رفتار قبلی، دست‌نخورده).
            AppModel.currentChapter = ChapterType.ANCIENT_EGYPT;
            Gdx.app.log("PVZ-TEST-SPAWNER", "👑 حالت تست اختصاصی زامباس فعاله - فقط یک EgyptZomboss اسپاون می‌شه.");
            return;
        }

        if (mode == TestMode.DARK_ZOMBOSS) {
            // زامباس دارک مخصوص چپتر Dark Ages هست.
            AppModel.currentChapter = ChapterType.DARK_AGES;
            Gdx.app.log("PVZ-TEST-SPAWNER", "👑 حالت تست اختصاصی زامباس دارک فعاله - فقط یک DarkZomboss اسپاون می‌شه.");
            return;
        }

        // جمع‌آوری تمام انواع زامبی‌های تعریف‌شده در Enum
        for (ZombieType type : ZombieType.values()) {
            zombieTypes.add(type);
        }
        Gdx.app.log("PVZ-TEST-SPAWNER", " تعداد "
            + zombieTypes.size() + " نوع زامبی برای تست در حالت DARK شناسایی شد.");
    }

    /**
     * این متد باید در هر فریم (مثلاً انتهای render یا advanceSimulation) فراخوانی شود.
     */
    public void update(float delta) {
        if (AppModel.gameSession == null || AppModel.gameSession.gameBoard == null) {
            return;
        }

        if (mode == TestMode.EGYPT_ZOMBOSS) {
            if (!zombossSpawned) {
                zombossSpawned = true;
                try {
                    // سازنده‌ی خودِ EgyptZomboss، خودش این آبجکت رو به لاین‌های
                    // rowDown و rowUp اضافه می‌کنه؛ چیز دیگه‌ای لازم نیست.
                    new EgyptZomboss();
                    Gdx.app.log("PVZ-TEST-SPAWNER", "👑 [ZOMBOSS TEST] یک EgyptZomboss اسپاون شد.");
                } catch (Exception e) {
                    Gdx.app.error("PVZ-TEST-SPAWNER", "❌ خطا در اسپاون زامباس: " + e.getMessage());
                }
            }
            return; // تو این حالت هیچ زامبی معمولی دیگه‌ای اسپاون نمی‌شه
        }

        if (mode == TestMode.DARK_ZOMBOSS) {
            if (!zombossSpawned) {
                zombossSpawned = true;
                try {
                    // سازنده‌ی خودِ DarkZomboss هم دقیقا مثل EgyptZomboss، خودش
                    // این آبجکت رو به لاین‌های rowDown و rowUp اضافه می‌کنه.
                    new DarkZomboss();
                    Gdx.app.log("PVZ-TEST-SPAWNER", "👑 [DARK ZOMBOSS TEST] یک DarkZomboss اسپاون شد.");
                } catch (Exception e) {
                    Gdx.app.error("PVZ-TEST-SPAWNER", "❌ خطا در اسپاون زامباس دارک: " + e.getMessage());
                }
            }
            return; // تو این حالت هیچ زامبی معمولی دیگه‌ای اسپاون نمی‌شه
        }

        if (zombieTypes.isEmpty()) return;

        timer += delta;
        if (timer >= SPAWN_INTERVAL) {
            timer = 0f;
            spawnNextZombie();
        }
    }

    private void spawnNextZombie() {
        ZombieType typeToSpawn = zombieTypes.get(currentZombieIndex);

        // 👈 با احتمال MIDDLE_SPAWN_CHANCE (۲۰٪) از وسط زمین اسپاون می‌شه،
        // وگرنه رفتار قبلی (لبه‌ی راست نقشه) حفظ می‌شه.
        boolean spawnFromMiddle = random.nextFloat() < MIDDLE_SPAWN_CHANCE;
        float spawnX = spawnFromMiddle ? MIDDLE_SPAWN_X : EDGE_SPAWN_X;

        // محاسبه ارتفاع ردیف (Y بر حسب متر یا پیکسل بر اساس متغیرهای ساختار بازی شما)
        float spawnY = (currentLane * (float) Constants.Game.TILE_HEIGHT) + Constants.UI.BOTTOM_LINE_METER;

        try {
            // ساخت زامبی با استفاده از ZombieBuilder مدل
            Zombie zombie = ZombieBuilder.create(typeToSpawn, spawnX, spawnY, currentLane);

            if (zombie != null) {
                // 🌪️ دیگه لازم نیست اینجا startSandstormSpawn صدا زده بشه: چون
                // spawnX (برای spawnFromMiddle) داخل محدوده‌ی زمینه، خودِ
                // ZombieBuilder.build() این تشخیص رو خودکار می‌ده و ۱.۵ ثانیه
                // گردباد رو خودش شروع می‌کنه - بدون اینکه هیچ‌جا لازم باشه
                // صراحتا صداش بزنیم.

                // افزودن زامبی به ردیف مربوطه در GameBoard
                AppModel.gameSession.gameBoard.lanes.get(currentLane).zombies.add(zombie);


                Gdx.app.log("PVZ-TEST-SPAWNER", String.format(
                    "🧟 [SPAWN TEST DARK]%s زامبی نوع <%s> در ردیف %d اسپاون شد. (X: %.1f, Y: %.1f)",
                    spawnFromMiddle ? " [از وسط زمین]" : "", typeToSpawn.name(), currentLane, spawnX, spawnY
                ));
            }
        } catch (Exception e) {
            Gdx.app.error("PVZ-TEST-SPAWNER", "❌ خطا در اسپاون زامبی " + typeToSpawn.name() + ": " + e.getMessage());
        }

        // رفتن به زامبی بعدی و چرخاندن ردیف بین ۰ تا ۴
        currentZombieIndex = (currentZombieIndex + 1) % zombieTypes.size();
        currentLane = ((currentLane + 1) % 5);
    }
}
