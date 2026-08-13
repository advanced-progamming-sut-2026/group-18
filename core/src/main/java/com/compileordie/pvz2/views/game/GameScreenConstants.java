package com.compileordie.pvz2.views.game;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.game.levels.ChapterType;

/**
 * ثابت‌های اختصاصی View لایه GameScreen.
 * هدف این کلاس این است که GameScreen صرفا منطق رندر داشته باشد و اعداد/مسیرهای
 * جادویی (magic numbers/strings) داخل خودش پخش نشوند.
 *
 * طبق اصل MVC: این کلاس فقط به مدل (ChapterType, Constants) وابسته است و
 * هیچ وابستگی معکوسی از مدل به سمت View وجود ندارد.
 */
public final class GameScreenConstants {

    private GameScreenConstants() {
    }

    // ---------------------------------------------------------------
    // ریشه‌ی مسیرهای asset زامبی‌ها
    // ---------------------------------------------------------------
    public static final String ASSET_RESOLUTION = "768";
    public static final String INITIAL_ZOMBIE_ROOT = ASSET_RESOLUTION + "/INITIAL/ZOMBIE/";
    public static final String FULL_ZOMBIE_ROOT = ASSET_RESOLUTION + "/FULL/ZOMBIE/";

    // ---------------------------------------------------------------
    // پس‌زمینه
    // ---------------------------------------------------------------
    public static final String BG_REGION_PREFIX = "IMAGE_BACKGROUNDS_";
    public static final String BG_TEXTURE_SUFFIX = "_TEXTURE";
    public static final String BG_TEXTURE_LEFT_SUFFIX = "_TEXTURE_LEFT";
    public static final String BG_TEXTURE_RIGHT_SUFFIX = "_TEXTURE_RIGHT";

    // ---------------------------------------------------------------
    // تنظیمات نمایش/رندر
    // ---------------------------------------------------------------
    public static final float ZOMBIE_SCALE = 0.8f;
    public static final float BG_SIDE_SCALE = 0.9f;

    /**
     * گام زمانی ثابت شبیه‌سازی (بر حسب ثانیه) که هر بار سپری شدنش یک تیک
     * (AppModel.gameSession.tick(1)) به مدل ارسال می‌شود.
     * این مقدار عمدا برابر Constants.Game.TIME_COEFFICIENT گذاشته شده چون همان
     * ضریبی است که مدل (مثلا Zombie.move) برای تبدیل «تعداد تیک» به «دلتای زمانی واقعی» استفاده می‌کند.
     */
    public static final float SIMULATION_STEP_SECONDS = Constants.Game.TIME_COEFFICIENT;

    /**
     * تبدیل چپتر فعلی مدل (AppModel.currentChapter) به نام پوشه asset مربوطه.
     * طبق درخواست:
     *  مصر -> EGYPT ، بیچ -> BEACH ، دارک -> DARK ، آیس‌ایج -> ICEAGE
     * اگر چپتر مشخص نباشد (مثلا مینی‌گیم‌هایی مثل Vase Breaker که چپتر خاصی روی
     * AppModel.currentChapter ست نشده)، طبق دستور شما ("مینی‌گیم رو هم مثل egypt در نظر بگیر")
     * پیش‌فرض EGYPT در نظر گرفته می‌شود.
     */
    public static String chapterFolder(ChapterType chapter) {
        if (chapter == null) {
            return "EGYPT";
        }
        return switch (chapter) {
            case ANCIENT_EGYPT -> "EGYPT";
            case BIG_WAVE_BEACH -> "BEACH";
            case DARK_AGES -> "DARK";
            case FROSTBITE_CAVES -> "ICEAGE";
            default -> "EGYPT";
        };
    }
}
