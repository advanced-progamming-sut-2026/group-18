package com.compileordie.pvz2.views.game;

import com.badlogic.gdx.graphics.Color;
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
    public static final float MOWER_SCALE = 0.8f;
    public static final float TOMB_SCALE = 0.8f;
    public static final float BG_SIDE_SCALE = 0.9f;

    // ---------------------------------------------------------------
    // منوی پاز (Pause Menu)
    // ---------------------------------------------------------------
    // نکته: آیکون دکمه‌ی پاز و پس‌زمینه‌ی دکمه‌های resume/restart/save-exit دیگه
    // اینجا به‌عنوان مسیر asset خام نگه‌داری نمی‌شن، چون به‌جاشون از استایل‌های
    // واقعی pvz-skin استفاده می‌کنیم ("ingame_pause" برای دکمه‌ی پاز، "green"/"brown"
    // برای دکمه‌های متنی) - دقیقا هم‌راستا با الگوی MainMenuScreen.
    public static final String PAUSE_PANEL_BG_REGION = "IMAGE_UI_GENERIC_PURPLEBUTTON_DOWN";
    public static final String PAUSE_FOG_DECORATION_REGION = "IMAGE_UI_PAUSEMENU_ZOMBOSS_FOG";

    public static final String RESUME_BUTTON_TEXT = "RESUME";
    public static final String RESTART_BUTTON_TEXT = "RESTART";
    public static final String SAVE_EXIT_BUTTON_TEXT = "SAVE AND EXIT";

    public static final float PAUSE_BUTTON_PAD = 20f;

    // ابعاد صفحه‌ی کوچک پاز (طبق درخواست: کل صفحه رو نگیره، ولی به‌اندازه‌ی کافی
    // بزرگ باشه که بک‌گراند کوچیکش کش‌اومده و خوشگل دیده بشه، نه تکه‌تکه/تکرارشونده)
    public static final float PAUSE_PANEL_WIDTH = 650f;
    public static final float PAUSE_PANEL_HEIGHT = 450f;
    public static final float PAUSE_MENU_BUTTON_WIDTH = 320f;
    public static final float PAUSE_MENU_BUTTON_HEIGHT = 90f;
    public static final float PAUSE_MENU_BUTTON_PAD = 12f;

    public static final Color PAUSE_PANEL_FILL_COLOR = new Color(1f, 0.45f, 0.75f, 1f); // صورتی
    public static final Color PAUSE_PANEL_BORDER_COLOR = Color.WHITE;
    public static final int PAUSE_PANEL_BORDER_PX = 6; // ضخامت بردر سفید، هر عددی خواستی عوض کن

    // تزئین مه (fog) بالای پنل - عرضش نسبتی از عرض پنله (تا با تغییر PAUSE_PANEL_WIDTH
    // هم متناسب بمونه)، ارتفاعش از روی نسبت واقعی تصویر (aspect ratio) در GameScreen
    // محاسبه می‌شه تا کش/کوتاه نشه. OVERLAP_RATIO میگه چقدرش روی لبه‌ی بالایی پنل بیفته.
    public static final float PAUSE_FOG_WIDTH_RATIO = 1.0f;
    public static final float PAUSE_FOG_OVERLAP_RATIO = 0.62f;

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
