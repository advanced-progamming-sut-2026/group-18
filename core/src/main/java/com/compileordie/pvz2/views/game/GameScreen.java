package com.compileordie.pvz2.views.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.compileordie.pvz2.models.entities.zombies.types.EffectType;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.NewspaperZombie;
import com.compileordie.pvz2.models.game.SessionBuilder;
import com.compileordie.pvz2.views.ScreenManager;
import com.compileordie.pvz2.views.ScreenType;
import pvz.skin.PvzSkin;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.LawnMower;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.boss.GargantuarZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.ProspectorZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.AllStarZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.KnightZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.StandardZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.Tomb;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.TombraiserZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.capable.RaZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.capable.HunterZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.capable.OctopusZombie;
import com.compileordie.pvz2.models.entities.plants.enums.ProjectileType;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import pvz.skin.BorderedTable;

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

    /**
     * نکته: قبلا اینجا یه فیلد کلاسی damageAlphaTimer بود که بین همه‌ی زامبی‌ها
     * مشترک بود - یعنی دمیج خوردن هر زامبی، تایمر فلش رو برای همه‌ی زامبی‌های
     * روی صفحه ریست می‌کرد و همه‌شون با هم کمرنگ می‌شدن. الان این تایمر داخل
     * ZombieRenderState (مخصوص هر زامبی) نگه‌داری می‌شه.
     */

    /**
     * 🧪 قابلیت تستی: وقتی true باشه، کلیک روی نقطه‌ای که یک زامبی رندر شده
     * (نه لزوما زیر مختصات x/y مدلش، بلکه دقیقا همون نقطه‌ی رندر روی صفحه -
     * baseX/baseY که در drawSingleZombie محاسبه می‌شه) با شعاع تلورانس
     * CLICK_DAMAGE_TEST_RADIUS_PX پیکسل، باعث می‌شه takeDamage(100, NORMAL, null)
     * روی همون زامبی صدا زده بشه. برای خاموش کردن کامل این قابلیت، فقط کافیه
     * این متغیر رو false کنید (چه از همینجا چه از بیرون کلاس).
     */
    public boolean hasWeTestForClickForDamaging = true;

    private static final float CLICK_DAMAGE_TEST_RADIUS_PX = 50f;


    private float shakeTimer = 0f;       // زمان باقی‌مانده از لرزش
    private float shakeIntensity = 0f;   // شدت لرزش
    private float shakeOffsetX = 0f;     // مقدار جابه‌جایی افقی
    private float shakeOffsetY = 0f;     // مقدار جابه‌جایی عمودی
    private float giantStepTimer = 0f;   // تایمر ۱ ثانیه‌ای برای ردپای غول

    private float effectPulseTime = 0f; // ساعت مستقل برای چشمک‌زدن افکت‌ها، ربطی به انیمیشن راه‌رفتن نداره
    // در کلاس AppModel (یا GameScreen)

    // کانتینر تمام‌صفحه برای overlay و پنل پایان بازی
    private Table gameEndOverlayContainer;
    // متغیری برای اینکه مطمئن شویم پنل پایان بازی فقط یک‌بار ساخته و نشان داده می‌شود
    private boolean hasGameEnded = false;

    // لیست سیاه برای فایل‌هایی که لود نمی‌شوند (جلوگیری از اسپم شدن ارور، کرش و افت فریم)
    private final Set<String> brokenAssets = new HashSet<>();

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
        // مختصات واقعیِ پیکسلی‌ای که این زامبی آخرین بار روش رندر شده (baseX/baseY
        // در drawSingleZombie) - برای تست کلیک-برای-دمیج استفاده می‌شه، نه مستقیم
        // zombie.getX()/getY() مدل، چون خواسته دقیقا «جایی که رندر شده» بود.
        float lastDrawX = Float.NaN;
        float lastDrawY = Float.NaN;
        // تایمر فلش دمیج مخصوص همین زامبی (نه سراسری) - وقتی >0 است، این زامبی
        // با آلفای کم رندر می‌شود، مستقل از این‌که زامبی‌های دیگر دمیج بخورند یا نه.
        float damageAlphaTimer = 0f;

        // 🦴 آیا بازو (مچ+ساعد) این زامبی از قبل کنده و به لیست fallingDebris
        // اضافه شده؟ (فقط برای BASIC/CONEHEAD) - یک‌بار true می‌شه و دیگه هیچ‌وقت
        // false نمی‌شه تا دوباره تریگر نشه.
        boolean armDropped = false;

        // 🛡️ آخرین armorHealth دیده‌شده برای این زامبی (فقط CONEHEAD) - برای
        // تشخیص لحظه‌ی «همین الان زره تموم شد» (گذر از >0 به <=0). NaN یعنی
        // هنوز حتی یک فریم هم برای این زامبی نخوندیمش.
        float lastArmorHealth = Float.NaN;
    }



    /**
     * کلاس موقت برای نگه‌داری اطلاعات زامبی‌هایی که مرده‌اند تا انیمیشن مرگشان تمام شود.
     */
    private static final class DeadZombieAnim {
        String typeKey;
        float x, y;
        boolean flip;
        float animTime = 0f;
        public Color effectColor;          // رنگ افکتی که زامبی موقع مرگ داشت (اگر داشت)
        public boolean isReversedDirection; // آیا جهت حرکتش معکوس بود؟ (مثل پراسپکتور یا هیپنوتیزم)
        // طول زمان پخش انیمیشن مرگ (بر حسب ثانیه). بعد از این زمان، از صفحه پاک می‌شود.
        static final float MAX_DURATION = 1.5f;
    }

    // لیستی از انیمیشن‌های مرگ در حال پخش
    private final List<DeadZombieAnim> deadZombies = new ArrayList<>();

    // =====================================================================
    // 🦴 تیکه‌های کنده‌شده‌ی بدن که روی زمین می‌افتند (دست/ساعد وقتی جون به نصف
    // می‌رسه، زره‌ی نصفه وقتی تموم می‌شه، سر وقتی زامبی می‌میره). کاملا View-only
    // و مستقل از مدل - دقیقا با همون الگوی DeadZombieAnim.
    // =====================================================================
    private static final class FallingDebris {
        String pam;              // مسیر resolve‌شده‌ی فایل PAM منبع (همون اسکلت بدن)
        String partName;         // اسم دقیق پارتی که باید تنها همون از drawPart بیرون کشیده بشه
        String sourceClip;       // کلیپی که فریم منجمد ازش گرفته می‌شه (walk/eat/die/...)
        float freezeTime;        // زمان داخل همون کلیپ برای فریز کردن ژست پارت
        float startX, startY;    // مختصات پیکسلی لحظه‌ی کنده شدن (همون‌جایی که زامبی رندر می‌شد)
        float scaleX, scaleY;    // جهت (فلیپ) و مقیاس زامبی در لحظه‌ی کنده شدن
        boolean parabolic;       // false = خطی و عمودی به سمت پایین (دست/زره) - true = سهمی (سر)
        float horizontalDir;     // فقط برای parabolic: جهت جابه‌جایی افقی حین پرتاب (-1..1)
        float fallDistanceM;     // 🎯 ارتفاع سقوط (متر) - مخصوص همین تیکه (دست=0.5، سر/مخروط=0.8)
        float animTime = 0f;
    }

    private final List<FallingDebris> fallingDebris = new ArrayList<>();

    // نسبت جونی که به محض رسیدنش، بازو کنده می‌شه (طبق درخواست: نصف جون خودِ
    // زامبی، بعد از زره - یعنی روی Zombie.health/Zombie.maxHealth، نه armorHealth)
    private static final float ARM_DROP_HEALTH_RATIO = 0.5f;

    // 🎯 طبق تایید صریح: ارتفاع افتادن دست (مچ+ساعد) روی زمین = ۰.۵ متر،
    // ارتفاع افتادن سر (فک+جمجمه) و مخروط نصفه‌ی سرمخروطی = ۰.۸ متر.
    private static final float DEBRIS_FALL_DISTANCE_HAND_M = 0.5f;
    private static final float DEBRIS_FALL_DISTANCE_HEAD_AND_CONE_M = 1.1f;

    // فیزیک افتادن روی زمین: با سرعت معقول (۰.۴۵ ثانیه)، ۲ ثانیه دقیقا همونجا
    // می‌مونه، بعد محو می‌شه.
    private static final float DEBRIS_FALL_DURATION = 0.45f;
    private static final float DEBRIS_HOLD_DURATION = 0.2f;
    private static final float DEBRIS_FADE_DURATION = 0.4f;
    private static final float DEBRIS_TOTAL_DURATION =
        DEBRIS_FALL_DURATION + DEBRIS_HOLD_DURATION + DEBRIS_FADE_DURATION;

    // فقط برای حرکت سهمی سر: کمی قوس رو به بالا قبل از سقوط + یه جابه‌جایی افقی کوچیک
    private static final float DEBRIS_PARABOLA_ARC_HEIGHT_PX = 75f;
    private static final float DEBRIS_PARABOLA_HORIZONTAL_PX = 105f;

    // زمان فریز روی کلیپ "die" برای گرفتن ژست سر جدا شده - دقیقا هم‌ارز با
    // animDuration=1.6f که drawDeadZombies برای زامبی‌های عادی (غیر غول/ایمپ) استفاده می‌کنه.
    private static final float HEAD_DEBRIS_DIE_FREEZE_TIME = 1.6f;

    private final Map<Zombie, ZombieRenderState> zombieRenderStates = new HashMap<>();

    /**
     * وضعیت رندر مخصوص هر ماشین چمن‌زن (تایمر انیمیشن + آخرین حالت idle/transition)،
     * دقیقا با همان منطق ZombieRenderState برای زامبی‌ها (View-only، مدل چیزی از این
     * اطلاعات نمی‌داند).
     */
    private static final class MowerRenderState {
        float animTime = 0f;
        boolean wasIdle = true;
    }

    private final Map<LawnMower, MowerRenderState> mowerRenderStates = new HashMap<>();

    /**
     * مسیر asset ماشین چمن‌زن. طبق درخواست:
     * 768/INITIAL/MOWERS/MOWER_{CH}/MOWER_{CH}.PAM
     * و دقیقا مثل زامبی‌ها: اگر چپتر EGYPT باشد INITIAL، وگرنه FULL.
     * خوشبختانه PamSpec.getResolvedPath() از قبل همین منطق (تعویض INITIAL/FULL بر
     * اساس چپتر + جایگزینی {CH}) را پیاده‌سازی کرده، پس همان را بازاستفاده می‌کنیم.
     */
    private static final ZombieVisualRegistry.PamSpec MOWER_PAM_SPEC =
        new ZombieVisualRegistry.PamSpec("768/INITIAL/MOWERS/MOWER_{CH}/MOWER_{CH}.PAM");

    /**
     * وضعیت رندر مخصوص هر قبر (Tomb) - دقیقا هم‌الگو با ZombieRenderState/MowerRenderState.
     * lastDrawX/Y برای تست کلیک-برای-دمیج (دقیقا مثل زامبی‌ها) استفاده می‌شه.
     */
    private static final class TombRenderState {
        float lastDrawX = Float.NaN;
        float lastDrawY = Float.NaN;
        float damageAlphaTimer = 0f;
    }

    private final Map<Tomb, TombRenderState> tombRenderStates = new HashMap<>();

    /**
     * مسیر asset قبر. ⚠️ چون پوشه‌ی pvz-assets داخل پروژه‌ای که برای من فرستاده
     * شده وجود نداشت، این مسیر را نتوانستم مثل بقیه‌ی PamSpec ها روی دیسک واقعی
     * تست/تایید کنم - قبل از استفاده حتما با اسم واقعی فولدر Tombstone در
     * pvz-assets چک/اصلاحش کن (existsOnDisk() هم به‌صورت خودکار جلوی کرش رو
     * می‌گیره اگه مسیر اشتباه باشه، ولی رندر هم انجام نمی‌شه).
     */
    private static final ZombieVisualRegistry.PamSpec TOMB_PAM_SPEC =
        new ZombieVisualRegistry.PamSpec(ZombieVisualRegistry.getChapterTag().equals("EGYPT")? "768/INITIAL/GRAVESTONES/EGYPT_HIEROGLYPH/EGYPT_HIEROGLYPH.PAM" : "768/FULL/GRAVESTONES/DARK_NOOP/DARK_NOOP.PAM");

    // ==========================================================================
    // منوی پاز (Pause Menu) - یک Stage جدا و مستقل از دنیای بازی، دقیقا مثل
    // الگویی که در MainMenuScreen برای UI استفاده شده (Stage/Table/Button) ولی
    // اینجا خودمان می‌سازیمش چون GameScreen برخلاف MainMenuScreen از MenuScreen
    // (که Stage/Skin آماده می‌دهد) ارث‌بری نمی‌کند.
    // ==========================================================================
    private Stage uiStage;
    private boolean isPaused = false;
    private ImageButton pauseButton;
    // کانتینر تمام‌صفحه‌ای که پس‌زمینه‌ی تیره (dim) داره و فقط پنل کوچیک پاز رو
    // وسط صفحه نگه می‌داره - وقتی paused نباشیم مخفی و غیرقابل‌کلیک‌ـه.
    private Table pauseOverlayContainer;
    // بافت ساخته‌شده به‌صورت دستی (رنگ تخت نیمه‌شفاف) فقط برای overlay تیره‌ی پشت
    // پنل پاز، چون اسکین/asset های پروژه drawable آماده‌ای برای یه dim ساده ندارن.
    // باید موقع dispose() آزاد بشه وگرنه memory leak می‌ده.
    private Texture pauseOverlayTexture;
    //    private Texture pausePanelBgTexture;
    // اسکین واقعی پروژه (همون pvz-skin که MainMenuScreen استفاده می‌کنه) - مستقیم
    // از PvzSkin.get() گرفته می‌شه، دقیقا مثل کاری که MenuScreen در سازنده‌اش می‌کنه.
    private Skin skin;

    @Override
    public void show() {
        batch = new SpriteBatch();
        viewport = new ScreenViewport();
        simulationAccumulator = 0f;
        isPaused = false;
        resourcesReleased = false;
        zombieRenderStates.clear();
        mowerRenderStates.clear();
        tombRenderStates.clear();
        brokenAssets.clear();
        ZombieVisualRegistry.clearAvailabilityCache();

        hasGameEnded = false;
        AppModel.wonLastGame = null; // جهت اطمینان

        loadAssets();
        setupPauseUI();

        Gdx.input.setInputProcessor(uiStage);
    }

    /**
     * ساخت و نمایش دیالوگ پایان بازی (برد / باخت)
     */
    private void showGameEndPanel(boolean isWin) {
        if (uiStage == null) return;

        // ۱. ساخت لایه‌ی تاریک پس‌زمینه (دیستراکشن / Overlay)
        gameEndOverlayContainer = new Table();
        gameEndOverlayContainer.setFillParent(true);
        gameEndOverlayContainer.setBackground(new TextureRegionDrawable(new TextureRegion(pauseOverlayTexture)));

        // ۲. ساخت پنل اصلی با BorderedTable (دقیقاً مثل منوی پاز بدون بخش تزئینی مه)
        BorderedTable panel = new BorderedTable();

        // ۳. عنوان پیام (YOU WON! یا GAME OVER!)
        String titleText = isWin ? "YOU WON!" : "GAME OVER!";
        Label titleLabel = new Label(titleText, skin, "medium_outline");
        titleLabel.setFontScale(1.8f); // 👈 دو برابر کردن سایز فونت عنوان
        panel.add(titleLabel).padBottom(20).row();

        if (isWin) {
            // ==================== حالت برد ====================
            TextButton exitButton = new TextButton("EXIT", skin, "green");
            exitButton.getLabel().setFontScale(1.5f); // 👈 دو برابر کردن سایز فونت دکمه
            exitButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    AppModel.wonLastGame = null; // ریست کردن وضعیت
                    ScreenManager.setMenuScreen(ScreenType.MAIN);
                }
            });

            panel.add(exitButton)
                // 👈 ۱.۵ برابر کردن طول و عرض دکمه
                .size(GameScreenConstants.PAUSE_MENU_BUTTON_WIDTH * 1.2f, GameScreenConstants.PAUSE_MENU_BUTTON_HEIGHT * 1.2f)
                .pad(GameScreenConstants.PAUSE_MENU_BUTTON_PAD)
                .row();

        } else {
            // ==================== حالت باخت ====================
            // دکمه TRY AGAIN (دقیقاً با منطق Restart)
            TextButton tryAgainButton = new TextButton("TRY AGAIN", skin, "green");
            tryAgainButton.getLabel().setFontScale(1.5f); // 👈 دو برابر کردن سایز فونت دکمه
            tryAgainButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    AppModel.wonLastGame = null; // ریست کردن وضعیت
                    AppModel.gameSession = SessionBuilder.create(AppModel.currentLevel, AppModel.selectionDeck);
                    hasGameEnded = false;
                    setPaused(false);
                    if (gameEndOverlayContainer != null) {
                        gameEndOverlayContainer.remove();
                    }
                }
            });

            // دکمه EXIT
            TextButton exitButton = new TextButton("EXIT", skin, "brown");
            exitButton.getLabel().setFontScale(1.5f); // 👈 دو برابر کردن سایز فونت دکمه
            exitButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    AppModel.wonLastGame = null; // ریست کردن وضعیت
                    ScreenManager.setMenuScreen(ScreenType.MAIN);
                }
            });

            panel.add(tryAgainButton)
                // 👈 ۱.۵ برابر کردن طول و عرض دکمه
                .size(GameScreenConstants.PAUSE_MENU_BUTTON_WIDTH * 1.2f, GameScreenConstants.PAUSE_MENU_BUTTON_HEIGHT * 1.2f)
                .pad(GameScreenConstants.PAUSE_MENU_BUTTON_PAD)
                .row();

            panel.add(exitButton)
                // 👈 ۱.۵ برابر کردن طول و عرض دکمه
                .size(GameScreenConstants.PAUSE_MENU_BUTTON_WIDTH * 1.2f, GameScreenConstants.PAUSE_MENU_BUTTON_HEIGHT * 1.2f)
                .pad(GameScreenConstants.PAUSE_MENU_BUTTON_PAD)
                .row();
        }

        gameEndOverlayContainer.add(panel)
            .size(GameScreenConstants.PAUSE_PANEL_WIDTH + 100, GameScreenConstants.PAUSE_PANEL_HEIGHT + 100);

        uiStage.addActor(gameEndOverlayContainer);
    }

    /**
     * فعال‌سازی لرزش
     */
    public void triggerCameraShake(float duration, float intensity) {
        this.shakeTimer = duration;
        this.shakeIntensity = intensity;
    }

    /**
     * آپدیت مقادیر لرزش در هر فریم
     */
    private void updateCameraShake(float delta) {
        if (shakeTimer > 0) {
            shakeTimer -= delta;
            // تولید آفست تصادفی
            shakeOffsetX = com.badlogic.gdx.math.MathUtils.random(-shakeIntensity, shakeIntensity);
            shakeOffsetY = com.badlogic.gdx.math.MathUtils.random(-shakeIntensity, shakeIntensity);

            if (shakeTimer <= 0) {
                shakeOffsetX = 0f;
                shakeOffsetY = 0f;
            }
        } else {
            shakeOffsetX = 0f;
            shakeOffsetY = 0f;
        }
    }

    /**
     * بررسی حضور زامبی غول در حال راه رفتن یا خوردن
     */
    private void checkGiantZombieFootsteps(float delta) {
        if (AppModel.gameSession == null || isPaused) {
            giantStepTimer = 0f;
            return;
        }

        boolean isGiantActive = false;

        // پیمایش لاین‌ها و زامبی‌ها
        for (var lane : AppModel.gameSession.gameBoard.lanes) {
            for (var zombie : lane.zombies) {

                // شرط ۱: زامبی زنده باشد
                // شرط ۲: زامبی از نوع غول (Gargantuar) باشد
                if (zombie.isAlive() && isGargantuar(zombie)) {
                    isGiantActive = true;
                    break;
                }
            }
            if (isGiantActive) break;
        }

        if (isGiantActive) {
            giantStepTimer += delta;
            if (giantStepTimer >= 1.22f) { // هر ۱ ثانیه یک‌بار
                triggerCameraShake(0.12f, 5.0f); // ۰.۲ ثانیه لرزش با شدت ۶ پیکسل
                giantStepTimer = 0f;
            }
        } else {
            giantStepTimer = 0f;
        }
    }

    /**
     * متد کمکی برای تشخیص غول بودن زامبی بر اساس نام کلاس یا Type
     */
    private boolean isGargantuar(Object zombie) {
        // بررسی نام کلاس یا enum مربوط به غول
        String className = zombie.getClass().getSimpleName().toUpperCase();
        if (className.contains("GARGANTUAR")) {
            return true;
        }

        // یا اگر متد getType() روی زامبی دارید:
        // return zombie.getType().toString().toUpperCase().contains("GARGANTUAR");

        return false;
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
        } catch (Throwable e) {
            Gdx.app.error("PVZ-DEBUG", "❌ خطا در بارگذاری اولیه asset های گیم‌اسکرین: " + e.toString(), e);
        }
    }

    // ==========================================================================
    // ساخت UI منوی پاز
    // ==========================================================================

    private void setupPauseUI() {
        uiStage = new Stage(new ScreenViewport());
        skin = PvzSkin.get();

        Table root = new Table();
        root.setFillParent(true);
        root.top().right();
        uiStage.addActor(root);

        // دکمه‌ی پاز - گوشه‌ی بالا سمت راست. طبق README پروژه‌ی pvz-skin، استایل
        // "ingame_pause" دقیقا برای همین دکمه ساخته شده (imagebutton_ingame_pause)،
        // پس به‌جای ساختن دستی Drawable از یک عکس خام، از خودِ اسکین استفاده می‌کنیم -
        // دقیقا مثل الگوی MainMenuScreen (new ImageButton(skin, "default") و ...).
        pauseButton = new ImageButton(skin, "ingame_pause");
        root.add(pauseButton).pad(GameScreenConstants.PAUSE_BUTTON_PAD);

        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setPaused(true);
            }
        });

        buildPauseMenuPanel();
    }

    /**
     * صفحه‌ی کوچک پاز: یک overlay تیره‌ی تمام‌صفحه (فقط برای محو کردن پس‌زمینه‌ی
     * بازی) که وسطش یک پنل کوچیک (نه تمام‌صفحه) با سه دکمه‌ی resume/restart/save-exit
     * قرار داره. پیش‌فرض مخفی و غیرقابل‌کلیکه (تا وقتی paused نشدیم جلوی کلیک روی
     * صحنه‌ی بازی رو نگیره).
     */
    private void buildPauseMenuPanel() {
        pauseOverlayTexture = createSolidTexture(new Color(0f, 0f, 0f, 0.55f));

        pauseOverlayContainer = new Table();
        pauseOverlayContainer.setFillParent(true);
        pauseOverlayContainer.setBackground(new TextureRegionDrawable(new TextureRegion(pauseOverlayTexture)));

        // ==========================================================
        // تغییر جدید: استفاده از BorderedTable به جای ساخت تکسچر دستی
        // ==========================================================
        BorderedTable panel = new BorderedTable();

        // ۱. دکمه‌ی Resume
        TextButton resumeButton = new TextButton(GameScreenConstants.RESUME_BUTTON_TEXT, skin, "green");
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setPaused(false);
            }
        });

        // ۲. دکمه‌ی Restart
        TextButton restartButton = new TextButton(GameScreenConstants.RESTART_BUTTON_TEXT, skin, "brown");
        restartButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                AppModel.gameSession = SessionBuilder.create(AppModel.currentLevel, AppModel.selectionDeck);
                setPaused(false);
            }
        });

        // ۳. دکمه‌ی Save & Exit
        TextButton saveExitButton = new TextButton(GameScreenConstants.SAVE_EXIT_BUTTON_TEXT, skin, "brown");
        saveExitButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                AppModel.wonLastGame = null; // ریست کردن وضعیت
                ScreenManager.setMenuScreen(ScreenType.MAIN);
            }
        });
        // ادامه کد بدون تغییر (اضافه کردن دکمه‌ها به پنل و ساخت Stack)...// TODO: منطق ذخیره‌کردن و خروج از گیم‌سشن فعلی رو اینجا پیاده‌سازی کن.

        panel.add(resumeButton)
            .size(GameScreenConstants.PAUSE_MENU_BUTTON_WIDTH, GameScreenConstants.PAUSE_MENU_BUTTON_HEIGHT)
            .pad(GameScreenConstants.PAUSE_MENU_BUTTON_PAD)
            .row();
        panel.add(restartButton)
            .size(GameScreenConstants.PAUSE_MENU_BUTTON_WIDTH, GameScreenConstants.PAUSE_MENU_BUTTON_HEIGHT)
            .pad(GameScreenConstants.PAUSE_MENU_BUTTON_PAD)
            .row();
        panel.add(saveExitButton)
            .size(GameScreenConstants.PAUSE_MENU_BUTTON_WIDTH, GameScreenConstants.PAUSE_MENU_BUTTON_HEIGHT)
            .pad(GameScreenConstants.PAUSE_MENU_BUTTON_PAD)
            .row();

        // ==================================================================
        // تزئین مه (ZOMBOSS_FOG) بالای سرِ پنل بک‌گراند.
        // با یک Stack پیاده می‌شه: اول panelWrapper اضافه می‌شه (لایه‌ی پایین‌تر)،
        // بعد fogWrapper (لایه‌ی بالاتر - چون در Group/Stack هر actor که دیرتر اضافه
        // بشه، روی actor های قبلی رسم می‌شه). یعنی طبق درخواست، لایه‌ی مه همیشه روی
        // لایه‌ی بک‌گراند پنل قرار می‌گیره، حتی جایی که روی هم می‌افتن.
        // ==================================================================
        Stack panelStack = new Stack();

        // خودِ پنل رو داخل یه wrapper با کمی فاصله‌ی خالی از بالا می‌ذاریم تا جا برای
        // نیمه‌ی پایینیِ تصویر مه باز بمونه (یعنی مه کمی روی لبه‌ی بالایی پنل می‌افته).
        TextureRegion fogRegion = (textureBank != null)
            ? textureBank.region(GameScreenConstants.PAUSE_FOG_DECORATION_REGION)
            : null;

        float fogWidth = 0f;
        float fogHeight = 0f;
        if (fogRegion != null) {
            fogWidth = GameScreenConstants.PAUSE_PANEL_WIDTH * GameScreenConstants.PAUSE_FOG_WIDTH_RATIO;
            // ارتفاع از روی نسبت واقعی تصویر محاسبه می‌شه تا کش/فشرده نشه
            float aspect = (float) fogRegion.getRegionWidth() / (float) fogRegion.getRegionHeight();
            fogHeight = fogWidth / aspect;
        } else {
            Gdx.app.error("PVZ-DEBUG",
                "❌ تصویر تزئینی مه ('" + GameScreenConstants.PAUSE_FOG_DECORATION_REGION + "') پیدا نشد!");
        }

        float overlapAmount = fogHeight * GameScreenConstants.PAUSE_FOG_OVERLAP_RATIO;

        Table panelWrapper = new Table();
        panelWrapper.top();
        if (overlapAmount > 0) {
            panelWrapper.add().height(overlapAmount).row(); // فضای خالی بالای پنل، جا برای مه
        }
        panelWrapper.add(panel).size(GameScreenConstants.PAUSE_PANEL_WIDTH, GameScreenConstants.PAUSE_PANEL_HEIGHT);
        panelStack.add(panelWrapper);

        if (fogRegion != null) {
            Table fogWrapper = new Table();
            fogWrapper.top();
            fogWrapper.add(new Image(fogRegion)).size(fogWidth, fogHeight);
            panelStack.add(fogWrapper); // اضافه‌شده بعد از panelWrapper => لایه‌ی بالاتر
        }

        pauseOverlayContainer.add(panelStack)
            .size(GameScreenConstants.PAUSE_PANEL_WIDTH, GameScreenConstants.PAUSE_PANEL_HEIGHT + overlapAmount);

        pauseOverlayContainer.setVisible(false);
        pauseOverlayContainer.setTouchable(Touchable.disabled);

        uiStage.addActor(pauseOverlayContainer);
    }

    /**
     * یک بافت تک‌رنگ (solid color) ۱x۱ پیکسلی می‌سازه - فقط برای overlay تیره‌ی
     * پشت پنل پاز، چون اسکین پروژه drawable آماده‌ای برای یه dim-overlay ساده نداره.
     */
    private Texture createSolidTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * یک بافت کوچیک می‌سازه که قراره منبع یک NinePatch باشه: یک مربع کوچیک با
     * رنگ بردر (borderColor) به ضخامت borderPx در چهار طرف، و رنگ پرکننده
     * (fillColor) وسطش. چون از Pixmap خودمون ساخته می‌شه (نه از Skin/TextureBank
     * مشترک)، هیچ‌جای دیگه‌ی UI (مثلا دکمه‌های منوی اصلی) رو تحت تاثیر قرار
     * نمی‌ده - دقیقاً همون درسی که از باگ قبلی گرفتیم.
     */


    /**
     * تغییر وضعیت پاز. این تنها نقطه‌ای است که باید برای pause/resume کردن بازی
     * صدا زده بشه؛ چون advanceSimulation() به isPaused نگاه می‌کنه، صدا زدن این
     * متد با paused=true دقیقا باعث می‌شه AppModel.gameSession.tick(...) دیگه صدا
     * زده نشه (طبق درخواست شما).
     */
    private void setPaused(boolean paused) {
        this.isPaused = paused;
        if (pauseOverlayContainer != null) {
            pauseOverlayContainer.setVisible(paused);
            pauseOverlayContainer.setTouchable(paused ? Touchable.enabled : Touchable.disabled);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        effectPulseTime += delta;

        if (textureBank != null) {
            try {
                textureBank.update();
            } catch (Throwable t) {
                // اگر کرش در سیستم آسنکرون پس‌زمینه لود است‌ها رخ داد
                if (!brokenAssets.contains("TEXTURE_BANK_ASYNC")) {
                    Gdx.app.error("PVZ-ASSET-ASYNC", "❌ خطا در بارگذاری پس‌زمینه است‌ها (TextureBank): " + t.toString(), t);
                    brokenAssets.add("TEXTURE_BANK_ASYNC");
                }
            }
        }

        // ===============================================
        // لاگ گرفتن مختصات کلیک ماوس / لمس صفحه
        // ===============================================
        if (Gdx.input.justTouched()) {
            touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchPoint); // تبدیل به مختصات دوربین/دنیای بازی

            // تبدیل پیکسل به متر برای هماهنگی با مدل
            float meterX = touchPoint.x / Constants.UI.METER_TO_PIX;
            float meterY = touchPoint.y / Constants.UI.METER_TO_PIX;

            Gdx.app.log("&&&&--------PVZ-CLICK", String.format(
                "🖱️ مختصات کلیک -> پیکسل: (X: %.1f, Y: %.1f) | متر-مدل: (X: %.2f, Y: %.2f)",
                touchPoint.x, touchPoint.y, meterX, meterY
            ));

            // 🧪 قابلیت تستی: کلیک روی نقطه‌ای که یک زامبی رندر شده = ۱۰۰ دمیج به همون زامبی.
            // برای خاموش کردنش کافیه hasWeTestForClickForDamaging رو false کنید.
            if (hasWeTestForClickForDamaging) {
                handleClickDamageTest(touchPoint.x, touchPoint.y);
            }
        }

        advanceSimulation(delta);

        if (!isPaused) {
            testSpawner.update(delta);
        }

        // 👈 ۱. بررسی گام‌های غول و به‌روزرسانی تایمر لرزش
        checkGiantZombieFootsteps(delta);
        updateCameraShake(delta);

        viewport.getCamera().update();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();

        // 👈 ۲. ذخیره ماتریس اصلی و اعمال آفست لرزش روی SpriteBatch
        com.badlogic.gdx.math.Matrix4 originalMatrix = batch.getTransformMatrix().cpy();
        if (shakeOffsetX != 0 || shakeOffsetY != 0) {
            batch.getTransformMatrix().translate(shakeOffsetX, shakeOffsetY, 0);
            batch.setTransformMatrix(batch.getTransformMatrix());
        }

        // رسم عناصر دنیای بازی
        drawBackground();
        drawMowers(delta); // رندر چمن‌زن‌ها جابه‌جا شد تا زیر زامبی‌ها قرار بگیرند
        drawTombs(delta);
        drawDeadZombies(delta);
        drawZombies(delta);
        drawFallingDebris(delta);

        // 👈 ۳. بازگرداندن ماتریس به حالت اول (تا لرزش روی UI تاثیر نگذارد)
        batch.setTransformMatrix(originalMatrix);

        batch.end();

        // UI منوی پاز (دکمه‌ی پاز + پنل، در صورت باز بودن) - همیشه بعد از دنیای
        // بازی رسم می‌شه تا روی همه‌چیز باشه.
        if (uiStage != null) {
            uiStage.act(delta);
            uiStage.draw();
        }
    }

    /**
     * 🧪 تست: نزدیک‌ترین زامبی‌ای که در شعاع CLICK_DAMAGE_TEST_RADIUS_PX از نقطه‌ی
     * کلیک واقعا رندر شده را پیدا می‌کند (بر اساس ZombieRenderState.lastDrawX/Y - همون
     * جایی که در drawSingleZombie واقعا رسم شده، نه مستقیم مختصات مدل) و ۱۰۰ دمیج
     * بهش می‌زند. اگه چند زامبی همزمان تو شعاع باشن، فقط نزدیک‌ترین‌شون دمیج می‌خوره.
     */
    private void handleClickDamageTest(float clickX, float clickY) {
        Zombie closest = null;
        float closestDistSq = Float.MAX_VALUE;
        float radiusSq = CLICK_DAMAGE_TEST_RADIUS_PX * CLICK_DAMAGE_TEST_RADIUS_PX;

        for (Map.Entry<Zombie, ZombieRenderState> entry : zombieRenderStates.entrySet()) {
            ZombieRenderState state = entry.getValue();
            if (Float.isNaN(state.lastDrawX) || Float.isNaN(state.lastDrawY)) {
                continue; // هنوز حتی یک بار هم رندر نشده
            }

            float dx = state.lastDrawX - clickX;
            float dy = state.lastDrawY - clickY;
            float distSq = dx * dx + dy * dy;

            if (distSq <= radiusSq && distSq < closestDistSq) {
                closestDistSq = distSq;
                closest = entry.getKey();
            }
        }

        // 🧪 همون تست، ولی برای قبرها (Tomb) - طبق درخواست: «قبر هم مثل زامبی باید
        // کلیک دمیج بخوره». نزدیک‌ترین قبر در شعاع مجاز رو مستقل از زامبی‌ها پیدا
        // و دمیج می‌زنیم (هر دو می‌تونن هم‌زمان دمیج بخورن اگه هر دو تو شعاع باشن).
        Tomb closestTomb = null;
        float closestTombDistSq = Float.MAX_VALUE;
        for (Map.Entry<Tomb, TombRenderState> entry : tombRenderStates.entrySet()) {
            TombRenderState state = entry.getValue();
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

        if (closest != null) {
            try {
                closest.takeDamage(100, DamageType.NORMAL, null);
                Gdx.app.log("PVZ-CLICK-DAMAGE-TEST",
                    "🎯 [تست] زامبی '" + closest.getType() + "' با کلیک روی نقطه‌ی رندرش، ۱۰۰ دمیج خورد.");

                // 🧪 تست: علاوه بر دمیج، اگه هانتر/اختاپوسه، همون متد واقعی منطق
                // (نه دستکاری مستقیم گرافیک) رو صدا می‌زنیم تا مطمئن بشیم اتصال
                // «منطق -> گرافیک» درست کار می‌کنه. با غیرفعال‌شدن
                // hasWeTestForClickForDamaging این تیکه هم باید حذف بشه.
                if (closest.getType() == ZombieType.HUNTER_ZOMBIE) {
                    ((HunterZombie) closest).startThrowAnimation();
                } else if (closest.getType() == ZombieType.OCTOPUS_ZOMBIE) {
                    ((OctopusZombie) closest).startTossAnimation();
                }
            } catch (Throwable e) {
                // نکته‌ی مهم: این try/catch به‌خاطر یه باگ توی خودِ مدل اضافه شد، نه
                // چیزی که اینجا ساخته باشیم. StandardZombie.takeDamage(...) پارامتر
                // سوم رو به‌عنوان PlantType واقعی می‌خونه و بدون چک null مستقیم
                // .name() روش صدا می‌زنه؛ چون این قابلیت تستیه و گیاه واقعی‌ای در
                // کار نیست، null پاس می‌دیم. بدون این try/catch، این باگِ مدل کل
                // بازی رو کرش می‌داد. فیکس اصلی (null-check تو خودِ StandardZombie)
                // نیاز به فایل StandardZombie.java داره.
                Gdx.app.error("PVZ-CLICK-DAMAGE-TEST",
                    "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n"
                        + "❌ takeDamage روی زامبی '" + closest.getType() + "' خطا داد (احتمالا باگ null-check\n"
                        + "توی takeDamage خودِ مدل، نه اینجا): " + e + "\n"
                        + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", e);
            }
        }
    }

    /**
     * گیم‌سشن را با گام زمانی ثابت جلو می‌برد (طبق Constants.Game.TIME_COEFFICIENT).
     * این‌طوری فیزیک/سرعت زامبی‌ها مستقل از فریم‌ریت واقعی دستگاه یکسان می‌ماند،
     * چون خود مدل (مثلا Zombie.move) از «تعداد تیک × TIME_COEFFICIENT» به عنوان دلتای
     * زمانی استفاده می‌کند.
     */
    private void advanceSimulation(float delta) {
        // بررسی پایان بازی
        if (AppModel.wonLastGame != null && !hasGameEnded) {
            hasGameEnded = true;
            setPaused(true); // متوقف کردن تیک بازی
            showGameEndPanel(AppModel.wonLastGame);
            return;
        }

        if (AppModel.gameSession == null || isPaused) {
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
        Set<Zombie> aliveSet = new HashSet<>(currentZombies);

        // ====================================================================
        // تشخیص مرگ: زامبی‌هایی که در فریم قبل بودند اما الان در aliveSet نیستند
        // ====================================================================
        var iterator = zombieRenderStates.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Zombie, ZombieRenderState> entry = iterator.next();
            Zombie zombie = entry.getKey();

            if (!aliveSet.contains(zombie)) {
                // اگر جون زامبی صفر یا کمتر شده بود، یعنی واقعا مرده (نه اینکه مثلا بخاطر باگ غیب شده باشه)
                if (zombie.getHealth() <= 0) {
                    DeadZombieAnim deadAnim = new DeadZombieAnim();
                    deadAnim.typeKey = zombie.getType().name();
                    if (zombie.killByExplosive) {
                        deadAnim.typeKey = "EXPLOSIVE_DEATH";
                        deadAnim.effectColor = null;
                    }
                    deadAnim.x = (float) zombie.getX();
                    deadAnim.y = (float) zombie.getY();
                    deadAnim.flip = entry.getValue().flip;
                    deadAnim.effectColor = computeZombieEffectColor(zombie);
                    deadAnim.isReversedDirection = (zombie.getType() == ZombieType.PROSPECTOR_ZOMBIE && ((ProspectorZombie)zombie).isReversedDirection) || zombie.isHypnotized();
                    deadZombies.add(deadAnim);

                    // 💀 سر (فک + جمجمه) با حرکت سهمی می‌افته - فقط برای بیسیک و سرمخروطی
                    if (zombie.getType() == ZombieType.STANDARD || zombie.getType() == ZombieType.CONEHEAD || zombie.getType()==ZombieType.BUCKETHEAD) {
                        ZombieVisualRegistry.ZombieVisualDef headDef = ZombieVisualRegistry.get(zombie.getType().name());
                        if (headDef != null && !headDef.pams.isEmpty()) {
                            String headPam = headDef.pams.get(0).getResolvedPath();
                            int headFlag = deadAnim.isReversedDirection ? -1 : 1;
                            float headScaleX = headFlag * GameScreenConstants.ZOMBIE_SCALE;
                            float headScaleY = GameScreenConstants.ZOMBIE_SCALE;
                            float headBaseX = (float) zombie.getX() * Constants.UI.METER_TO_PIX;
                            float headBaseY = (float) zombie.getY() * Constants.UI.METER_TO_PIX;

                            spawnFallingPart(headPam, chapterPartName("skull"), "idle", HEAD_DEBRIS_DIE_FREEZE_TIME,
                                headBaseX, headBaseY, headScaleX, headScaleY, true, headFlag, DEBRIS_FALL_DISTANCE_HEAD_AND_CONE_M);
                            spawnFallingPart(headPam, chapterPartName("jaw"), "idle", HEAD_DEBRIS_DIE_FREEZE_TIME,
                                headBaseX, headBaseY, headScaleX, headScaleY, true, headFlag, DEBRIS_FALL_DISTANCE_HEAD_AND_CONE_M);
                        }
                    }
                }
                // حالا از لیست رندرهای زنده پاکش می‌کنیم
                iterator.remove();
            }
        }

        // مرتب‌سازی بر اساس مختصات Y
        currentZombies.sort((z1, z2) -> Double.compare(z2.getY(), z1.getY()));

        logUnrenderableBoardSummary(delta, currentZombies);

        for (Zombie zombie : currentZombies) {
            ZombieRenderState state = zombieRenderStates.computeIfAbsent(zombie, z -> new ZombieRenderState());

            boolean isEatingNow = zombie.isEating();
            if (isEatingNow != state.wasEating) {
                state.animTime = 0f;
                state.wasEating = isEatingNow;
            }

            drawSingleZombie(zombie, state, delta);
        }
    }

    private float unrenderableSummaryTimer = 0f;

    /**
     * 📋 لاگ خلاصه‌ی دوره‌ای (هر ۳ ثانیه): تمام زامبی‌های "زنده در مدل" (توی
     * lane.zombies) که الان روی صفحه هستن ولی asset ندارن رو با تعدادشون لیست
     * می‌کنه - حتی اگه به هر دلیلی لاگ تک‌تک‌شون (logZombieNotRenderable، که فقط
     * یک‌بار به‌ازای هر typeKey+چپتر پرینت می‌شه) رو از دست داده باشی، این
     * خلاصه هر چند ثانیه یک‌بار دوباره چاپ می‌شه تا گم نشه.
     */
    private void logUnrenderableBoardSummary(float delta, List<Zombie> currentZombies) {
        unrenderableSummaryTimer += delta;
        if (unrenderableSummaryTimer < 3f) return;
        unrenderableSummaryTimer = 0f;

        Map<String, Integer> missingCounts = new HashMap<>();
        for (Zombie zombie : currentZombies) {
            String typeKey = zombie.getType().name();
            if (!ZombieVisualRegistry.isAvailable(typeKey)) {
                missingCounts.merge(typeKey, 1, Integer::sum);
            }
        }

        if (missingCounts.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        sb.append("\n================ 📋 خلاصه‌ی زامبی‌های اسپاون‌شده-ولی-رندرنشده (چپتر: ")
            .append(ZombieVisualRegistry.getChapterTag()).append(") ================\n");
        for (Map.Entry<String, Integer> e : missingCounts.entrySet()) {
            sb.append("  - ").append(e.getKey()).append(" : ").append(e.getValue()).append(" عدد روی زمین (بدون رندر)\n");
        }
        sb.append("جزئیات هر مسیر asset مورد نیاز رو تو لاگ‌های 'PVZ-ZOMBIE-SPAWNED-NOT-RENDERED' بالاتر ببین.\n");
        sb.append("=================================================================");
        Gdx.app.error("PVZ-ZOMBIE-SPAWNED-NOT-RENDERED-SUMMARY", sb.toString());
    }

    private void drawDeadZombies(float delta) {        // ضریب سرعت انیمیشن مرگ (اگر می‌خواهی دقیقاً ۲.۲ ثانیه طول بکشد، بکنش 1.0f)
        final float DIE_ANIM_SPEED = 1.0f;
        final float TOTAL_DEATH_DURATION = 2.2f; // مدت زمان کل نمایش جسد روی زمین

        var iterator = deadZombies.iterator();
        while (iterator.hasNext()) {
            DeadZombieAnim deadAnim = iterator.next();

            // افزایش زمان انیمیشن
            deadAnim.animTime += delta * DIE_ANIM_SPEED;

            // اگر زمان کل (۲.۲ ثانیه) تمام شد، جسد را از لیست حذف کن
            if (deadAnim.animTime >= TOTAL_DEATH_DURATION) {
                iterator.remove();
                continue;
            }

            ZombieVisualRegistry.ZombieVisualDef def = ZombieVisualRegistry.get(deadAnim.typeKey);
            if (def == null || !ZombieVisualRegistry.isAvailable(deadAnim.typeKey)) {
                continue;
            }

            float baseX = deadAnim.x * Constants.UI.METER_TO_PIX;
            float baseY = deadAnim.y * Constants.UI.METER_TO_PIX;
            String animName = "die";
            if (deadAnim.typeKey.equals("EXPLOSIVE_DEATH")) {
                animName = "animation";
            }

            for (ZombieVisualRegistry.PamSpec part : def.pams) {
                String resolvedPath = part.getResolvedPath();
                if (brokenAssets.contains(resolvedPath)) continue;

                Color oldColor = null;
                try {
                    float drawX = baseX + part.offsetX * Constants.UI.METER_TO_PIX;
                    float drawY = baseY + part.offsetY * Constants.UI.METER_TO_PIX;

                    // ۱. تنظیم مقیاس و جهت بر اساس اطلاعات موقع مرگ
                    int flag = deadAnim.isReversedDirection ? -1 : 1;
                    float scaleX = flag * GameScreenConstants.ZOMBIE_SCALE;
                    float scaleY = GameScreenConstants.ZOMBIE_SCALE;

                    // ۲. اعمال رنگ افکت ذخیره‌شده
                    if (deadAnim.effectColor != null) {
                        oldColor = batch.getColor().cpy();
                        batch.setColor(deadAnim.effectColor);
                    }

                    // ۳. 💡 محاسبه زمان رندر برای فریز شدن روی فریم آخر
                    // طول واقعی انیمیشن را از پلیر می‌گیریم (اگر متدش نام دیگری دارد، جایگزین کن)
                    float animDuration = (resolvedPath.toLowerCase().contains("gargantuar")?1.9f : resolvedPath.toLowerCase().contains("imp")?1f : 1.6f);

                    // زمان را محدود می‌کنیم تا از طول انیمیشن تجاوز نکند (روی آخرین فریم قفل می‌شود)
                    float renderTime = Math.min(deadAnim.animTime, animDuration);

                    // رسم انیمیشن مرگ با زمان فریز شده در انتها
                    player.draw(
                        batch,
                        resolvedPath,
                        animName,
                        renderTime, // به جای deadAnim.animTime از renderTime استفاده می‌کنیم
                        drawX,
                        drawY,
                        scaleX,
                        scaleY,
                        deadAnim.flip
                    );

                    // بازگرداندن رنگ بچ به حالت عادی
                    if (oldColor != null) {
                        batch.setColor(oldColor);
                    }

                } catch (Throwable e) {
                    brokenAssets.add(resolvedPath);
                } finally {
                    // لایه محافظ برای جلوگیری از ماندگاری رنگ در صورت بروز خطا
                    if (deadAnim.effectColor != null) {
                        batch.setColor(Color.WHITE);
                    }
                }
            }
        }
    }

    /**
     * اسم واقعی پارت PAM رو بر اساس چپتر فعلی می‌سازه - طبق تایید صریح:
     *  - EGYPT: پیشوند "zombie_egypt_" داره (مثلا zombie_egypt_jaw، zombie_egypt_skull،
     *    zombie_egypt_hand_outer_01، zombie_egypt_arm_outer_lower)
     *  - بقیه‌ی چپترها (dark/beach/iceage): اسم عمومی بدون پیشوند چپتر
     *    (مثلا zombie_jaw، zombie_skull، zombie_hand_outer_01، zombie_arm_outer_lower)
     * ⚠️ قبلا اینجا فرض شده بود همه‌ی چپترها پیشوند مخصوص خودشون رو دارن
     * (zombie_beach_jaw و...)؛ طبق تایید، این فرض غلط بود و فقط EGYPT همچین
     * پیشوندی داره.
     */
    private String chapterPartName(String suffix) {
        if ("EGYPT".equals(ZombieVisualRegistry.getChapterTag())) {
            return "zombie_egypt_" + suffix;
        }
        return "zombie_" + suffix;
    }

    private void spawnFallingPart(String pam, String partName, String sourceClip, float freezeTime,
                                  float startX, float startY, float scaleX, float scaleY,
                                  boolean parabolic, float horizontalDir, float fallDistanceM) {
        FallingDebris d = new FallingDebris();
        d.pam = pam;
        d.partName = partName;
        d.sourceClip = sourceClip;
        d.freezeTime = freezeTime;
        d.startX = startX;
        d.startY = startY;
        d.scaleX = scaleX;
        d.scaleY = scaleY;
        d.parabolic = parabolic;
        d.horizontalDir = horizontalDir;
        d.fallDistanceM = fallDistanceM;
        fallingDebris.add(d);
    }

    /**
     * آپدیت و رندر تیکه‌های کنده‌شده‌ی روی زمین (دست/زره/سر). هر تیکه دقیقا از
     * روی همون فایل PAM بدن، با drawPart، فقط پارت مورد نظر رو (با فریمِ
     * فریزشده‌ی لحظه‌ی کنده شدن) می‌کشه؛ فلیپ/مقیاس با ترنسفورم batch (دقیقا
     * همون ترفندی که برای visibilityMap در drawSingleZombie استفاده شده) اعمال
     * می‌شه چون drawPart خودش پارامتر مقیاس نداره.
     */
    private void drawFallingDebris(float delta) {
        if (fallingDebris.isEmpty()) return;

        var iterator = fallingDebris.iterator();
        while (iterator.hasNext()) {
            FallingDebris d = iterator.next();
            d.animTime += delta;

            if (d.animTime >= DEBRIS_TOTAL_DURATION) {
                iterator.remove();
                continue;
            }

            if (brokenAssets.contains(d.pam)) {
                continue; // فایل خرابه ولی تایمر رو نگه می‌داریم تا خودش عادی تموم بشه
            }

            float fallT = Math.min(1f, d.animTime / DEBRIS_FALL_DURATION);
            float fallenPx = d.fallDistanceM * Constants.UI.METER_TO_PIX * fallT;

            float curX = d.startX;
            float curY = d.startY - fallenPx;

            if (d.parabolic) {
                curX += DEBRIS_PARABOLA_HORIZONTAL_PX * d.horizontalDir * fallT;
                curY += DEBRIS_PARABOLA_ARC_HEIGHT_PX * (float) Math.sin(Math.PI * fallT);
            }

            float alpha = 1f;
            if (d.animTime > DEBRIS_FALL_DURATION + DEBRIS_HOLD_DURATION) {
                float fadeT = (d.animTime - DEBRIS_FALL_DURATION - DEBRIS_HOLD_DURATION) / DEBRIS_FADE_DURATION;
                alpha = Math.max(0f, 1f - fadeT);
            }

            Color oldColor = batch.getColor().cpy();
            com.badlogic.gdx.math.Matrix4 originalTransform = batch.getTransformMatrix().cpy();
            try {
                batch.setColor(1f, 1f, 1f, alpha);

                com.badlogic.gdx.math.Matrix4 scaledTransform = originalTransform.cpy()
                    .translate(curX, curY, 0f)
                    .scale(d.scaleX, d.scaleY, 1f)
                    .translate(-curX, -curY, 0f);
                batch.setTransformMatrix(scaledTransform);

                player.drawPart(batch, d.pam, d.sourceClip, d.freezeTime, curX, curY, d.partName);
            } catch (Throwable e) {
                brokenAssets.add(d.pam);
            } finally {
                batch.setTransformMatrix(originalTransform);
                batch.setColor(oldColor);
            }
        }
    }

    private static final float BLINK_SPEED_HYPNOTIZED = 6f;
    private static final float BLINK_SPEED_STUNNED = 8f;
    private static final float BLINK_SPEED_POISON = 5f;

    /**
     * اگه زامبی یکی از افکت‌های بصری فعال داره، رنگ متناظرش رو برمی‌گردونه؛
     * وگرنه null (یعنی رنگ عادی، بدون تینت).
     * ترتیب چک: هر افکتی که زودتر match بشه استفاده می‌شه - اگه اولویت متفاوتی
     * می‌خوای (مثلا FROZEN همیشه روی بقیه ارجحیت داشته باشه) ترتیب if ها رو عوض کن.
     */
    private Color computeZombieEffectColor(Zombie zombie) {
        // 👈 اگه اسم واقعی متد/enum شما فرق داره (مثلا zombie.getActiveEffect()==EffectType.X)
        // فقط همین شرط‌ها رو با API واقعی خودتون جایگزین کنید.
        if (zombie.hasEffect(EffectType.FROZEN)) {
            return new Color(0.15f, 0.35f, 0.75f, 0.95f); // آبی تیره و تقریبا کدر (یخ‌زده)
        }
        if (zombie.hasEffect(EffectType.CHILLED)) {
            return new Color(0.25f, 0.55f, 0.85f, 0.80f); // آبی روشن و شفاف
        }
        if (zombie.hasEffect(EffectType.HYPNOTIZED)) {
            float blink = 0.5f + 0.5f * (float) Math.sin(effectPulseTime * BLINK_SPEED_HYPNOTIZED);
            return new Color(1.0f, 0.35f, 0.75f, 0.6f + 0.7f * blink); // صورتی چشمک‌زن
        }
        if (zombie.hasEffect(EffectType.STUNNED)) {
            float blink = 0.5f + 0.5f * (float) Math.sin(effectPulseTime * BLINK_SPEED_STUNNED);
            return new Color(1.0f, 0.9f, 0.15f, 0.9f); // زرد چشمک‌زن
        }
        if (zombie.hasEffect(EffectType.POISON)) {
            float blink = 0.5f + 0.5f * (float) Math.sin(effectPulseTime * BLINK_SPEED_POISON);
            return new Color(0.05f, 0.35f, 0.1f, 0.6f + 0.7f * blink); // سبز تیره چشمک‌زن
        }

        // 👈 هشدار نزدیک شدن به خانه: اگه هیچ افکت دیگه‌ای فعال نباشه و زامبی از
        // ۶ متر (X) کمتر شده باشه، رنگش کم‌کم به سمت قرمز جیغ می‌ره - هرچی به صفر
        // نزدیک‌تر بشه، شدت قرمزی بیشتر می‌شه (X=6 یعنی هنوز اصلا قرمز نیست).
        final float RED_WARNING_X_THRESHOLD = 10f;
        double zombieX = zombie.getX();
        if (zombieX < RED_WARNING_X_THRESHOLD) {
            float intensity = (float) Math.min(1.0, (RED_WARNING_X_THRESHOLD - zombieX) / RED_WARNING_X_THRESHOLD);
            // ترکیب رنگ سفید (عادی) با قرمز جیغ (1,0,0) بر اساس شدت
            float r = 1f;
            float g = 1f - intensity;
            float b = 1f - intensity;
            return new Color(r, g, b, 1f);
        }

        return null;
    }

    /**
     * 📋 لاگ جامع و شفاف برای وقتی یک زامبی «اسپاون شده ولی رندر نمی‌شه» - یعنی
     * تو لیست lane.zombies مدل هست، ولی چون ZombieVisualRegistry براش asset
     * موجودی برای چپتر فعلی نداره، drawSingleZombie کاملا ردش می‌کنه. این لاگ
     * تک‌تک مسیرهای PAM مورد نیاز این نوع زامبی رو با نتیجه‌ی existsOnDisk()
     * لیست می‌کنه تا دقیقا مشخص بشه کدوم فایل رو باید توی pvz-assets پیدا/اضافه
     * کرد - اگه def اصلا null بود (یعنی این تایپ زامبی هیچ‌وقت register نشده)
     * هم همینو مشخص می‌کنه.
     */
    private void logZombieNotRenderable(String typeKey, ZombieVisualRegistry.ZombieVisualDef def) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("################################################################\n");
        sb.append("⛔⛔⛔ زامبی اسپاون شده ولی قابل رندر نیست! ⛔⛔⛔\n");
        sb.append("نوع زامبی (typeKey) : ").append(typeKey).append("\n");
        sb.append("چپتر فعلی           : ").append(ZombieVisualRegistry.getChapterTag()).append("\n");

        if (def == null) {
            sb.append("علت                 : این تایپ زامبی اصلا در ZombieVisualRegistry ثبت (register) نشده!\n");
            sb.append("راه‌حل               : یک ZombieVisualDef جدید برای '" + typeKey + "' به ZombieVisualRegistry اضافه کن.\n");
        } else if (def.pams.isEmpty()) {
            sb.append("علت                 : def ثبت شده ولی لیست pams خالیه (هیچ فایلی تعریف نشده).\n");
        } else {
            sb.append("فایل‌های PAM مورد نیاز این زامبی (نسبت به pvz-assets/IMAGES/):\n");
            int i = 1;
            for (ZombieVisualRegistry.PamSpec pam : def.pams) {
                String resolved = pam.getResolvedPath();
                boolean exists = pam.existsOnDisk();
                sb.append("  ").append(i++).append(") ")
                    .append(exists ? "✅ موجوده" : "❌ پیدا نشد")
                    .append("  ->  ").append(resolved).append("\n");
            }
            sb.append("راه‌حل               : فایل(های) '❌ پیدا نشد' رو یا به pvz-assets اضافه کن، یا مسیر\n");
            sb.append("                       ثبت‌شده تو ZombieVisualRegistry برای '" + typeKey + "' رو با اسم واقعی فایل تطبیق بده.\n");
        }
        sb.append("نتیجه فعلی          : این زامبی به‌طور کامل رد می‌شه (نه رندر می‌شه، نه کرش می‌کنه).\n");
        sb.append("################################################################");

        Gdx.app.error("PVZ-ZOMBIE-SPAWNED-NOT-RENDERED", sb.toString());
    }

    private void drawSingleZombie(Zombie zombie, ZombieRenderState state, float delta) {
        String typeKey = zombie.getType().name();

        ZombieVisualRegistry.ZombieVisualDef def = ZombieVisualRegistry.get(typeKey);

        if (def == null) {
            if (!brokenAssets.contains("DEF_MISSING_" + typeKey)) {
                brokenAssets.add("DEF_MISSING_" + typeKey);
                logZombieNotRenderable(typeKey, null);
            }
            return;
        }

        // 🚀 اگه این زامبی (مثلا ایمپ تازه‌پرتاب‌شده از غول) در حال پخش انیمیشن
        // پرتاب/فرود (fly-in) هست، رندرِ عادی (راه‌رفتن/خوردن و...) رو کامل رد
        // می‌کنیم و به‌جاش مسیر سهمی رو رسم می‌کنیم. همین که مدل خودش این حالت رو
        // تمام کنه (بعد از ۱ ثانیه، در Zombie.tick())، از فریم بعد خودکار می‌افته
        // تو همین متد و طبق روال عادی (walk/eat و...) رندر می‌شه - بدون هیچ کد
        // اضافه‌ای برای "لینک کردن"، چون از اول تا آخر همون یک آبجکت zombie‌ست.
        if (zombie.isFlyingIn()) {
            drawFlyingInZombie(zombie, state, delta, def);
            return;
        }

        if (!ZombieVisualRegistry.isAvailable(typeKey)) {
            String cacheTag = "ASSET_UNAVAILABLE_" + typeKey + "@" + ZombieVisualRegistry.getChapterTag();
            if (!brokenAssets.contains(cacheTag)) {
                brokenAssets.add(cacheTag);
                logZombieNotRenderable(typeKey, def);
            }
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
        boolean checkAllstar = (zombie.getType() == ZombieType.ALL_STAR && ((AllStarZombie) zombie).isCharging());
        state.animTime += delta * (checkAllstar ? animSpeedMultiplier * 0.3 : animSpeedMultiplier);
        // =========================================================

        float baseX = (float) zombie.getX() * Constants.UI.METER_TO_PIX;
        float baseY = (float) zombie.getY() * Constants.UI.METER_TO_PIX;

        // برای تست کلیک-برای-دمیج: دقیقا همین نقطه (baseX/baseY) جایی است که این
        // زامبی روی صفحه رندر می‌شود، پس همینو ذخیره می‌کنیم.
        state.lastDrawX = baseX;
        state.lastDrawY = baseY;

        if (zombie.takedDamage) {
            state.damageAlphaTimer = 0.15f;
            zombie.takedDamage = false; // فلگ رو خاموش می‌کنیم که تو فریم‌های بعدی دوباره تریگر نشه
        }
        if (state.damageAlphaTimer > 0) {
            state.damageAlphaTimer -= delta;
        }

        // تعیین نام انیمیشن و زمان محلی رندر بر اساس نوع زامبی و وضعیت آن
        String animName;
        float renderAnimTime = state.animTime; // زمان پیش‌فرضی که به draw فرستاده می‌شود

        // ==========================================
        // 💡 توالی fire -> cannon_fire موقع پرتاب ایمپ توسط غول (Gargantuar)
        // این شرط عمدا قبل از eat/walk چک می‌شه چون باید حالت عادی (خوردن/راه
        // رفتن) رو موقتا کامل override کنه؛ به محض تموم‌شدن (isFiringImp() که
        // false بشه)، خودکار به شرط‌های پایین (eat/walk عادی) برمی‌گرده - بدون
        // نیاز به هیچ ریست دستی‌ای، چون منبع حقیقت (fireSequenceRemaining) کامل
        // داخل مدل (GargantuarZombie) نگه‌داری می‌شه.
        // ==========================================
        if (typeKey.contains("GARGANTUAR") && ((GargantuarZombie) zombie).isFiringImp()) {
            final float FIRE_DURATION = 0.5f;
            final float CANNON_FIRE_DURATION = 0.3f;
            float elapsed = (float) ((GargantuarZombie) zombie).getFireSequenceElapsed();

            if (elapsed < FIRE_DURATION) {
                animName = "fire";
                renderAnimTime = elapsed;
            } else {
                animName = "cannon_fire";
                renderAnimTime = elapsed - FIRE_DURATION;
            }
        }
        // ==========================================
        // 💡 حالت "power" را زامبی حین دزدیدن خورشید - فلگ shouldWeSteal() خودش
        // به همون مدتی که واقعا در حال دزدیدنه true می‌مونه (مدل خودش کنترلش
        // می‌کنه)، پس نیازی به تایمر جدا نیست: فقط تا وقتی true هست "power" پخش
        // می‌شه، به محض false شدن خودکار برمی‌گرده به walk/eat عادی.
        // ==========================================
        else if (zombie.getType() == ZombieType.RA_ZOMBIE && ((RaZombie) zombie).shouldWeSteal()) {
            animName = "power";
        }
        // ==========================================
        // 💡 حالت "power" زامبی قبرساز حین ساختن قبر (۳ ثانیه، طبق تایمر داخل
        // TombraiserZombie که دقیقا همون لحظه‌ی summon() واقعی شروع می‌شه)
        // ==========================================
        else if (zombie.getType() == ZombieType.TOMBRAISER && ((TombraiserZombie) zombie).isPoweringUp()) {
            animName = "power";
            renderAnimTime = (float) ((TombraiserZombie) zombie).getPowerAnimElapsed();
        }
        // ==========================================
        // 💡 حالت "throw" هانتر حین پرتاب یخ (۲ ثانیه، از لحظه‌ی شلیک واقعی)
        // ==========================================
        else if (zombie.getType() == ZombieType.HUNTER_ZOMBIE && ((HunterZombie) zombie).isThrowing()) {
            animName = "throw";
            renderAnimTime = (float) ((HunterZombie) zombie).getThrowAnimElapsed();
        }
        // ==========================================
        // 💡 حالت "toss" اختاپوس‌پرت‌کن حین پرتاب اختاپوس (۳ ثانیه، از لحظه‌ی
        // پرتاب واقعی)
        // ==========================================
        else if (zombie.getType() == ZombieType.OCTOPUS_ZOMBIE && ((OctopusZombie) zombie).isTossing()) {
            animName = "toss";
            renderAnimTime = (float) ((OctopusZombie) zombie).getTossAnimElapsed();
        }
        else if (zombie.isEating()) {
            animName = "eat";

            if (zombie.getType()==ZombieType.NEWSPAPER_ZOMBIE && !((NewspaperZombie)zombie).isEnraged()) {
                animName = "eat_newspaper";
            }

            if (zombie.getType() == ZombieType.ALL_STAR && ((AllStarZombie) zombie).isCharging()) {
                animName = "tackle";
            }
            // ==========================================
            // 💡 منطق لوپ دوگانه برای غول (Gargantuar)
            // ==========================================
            else if (typeKey.contains("GARGANTUAR")) {
                final float EAT_DUR = 1.3f;
                final float SMASH_DUR = 1.8f;
                final float TOTAL_CYCLE = EAT_DUR + SMASH_DUR; // 3.1f

                // پیدا کردن موقعیت فعلی در لوپ 3.1 ثانیه‌ای
                float currentCycleTime = state.animTime % TOTAL_CYCLE;

                if (currentCycleTime < EAT_DUR) {
                    // تو فاز اول هستیم (1.3 ثانیه اول)
                    animName = "eat";
                    renderAnimTime = currentCycleTime; // انیمیشن از 0 تا 1.3 پخش میشه
                } else {
                    // تو فاز دوم هستیم (1.8 ثانیه دوم)
                    animName = "smash_left";
                    renderAnimTime = currentCycleTime - EAT_DUR; // زمان رو صفر می‌کنیم تا از فریم اول smash شروع بشه
                }
            }
        } else if ("PIANIST_ZOMBIE".equals(typeKey)) {
            animName = "play";
        } else {
            animName = "walk";
            if (zombie.getType()==ZombieType.NEWSPAPER_ZOMBIE && !((NewspaperZombie)zombie).isEnraged()) {
                animName = "walk_newspaper";
            }
            if (zombie.getType() == ZombieType.ALL_STAR && ((AllStarZombie) zombie).isCharging()) {
                animName = "run";
            }
        }



        if (isPaused) animName = (zombie.getType()==ZombieType.NEWSPAPER_ZOMBIE)?"idle_newspaper":"idle";

        // جهت صورت زامبی بر اساس علامت واقعی سرعت افقی (xSpeed)
        double xSpeed = zombie.getXSpeed();
        if (xSpeed > 0) {
            state.flip = true;  // حرکت به چپ (حالت عادی) -> پیش‌فرض آرت رو به راسته پس flip=true
        } else if (xSpeed < 0) {
            state.flip = false; // حرکت به راست (مثلا هیپنوتایز)
        }
        boolean flip = state.flip;

        Map<String, Boolean> visibilityMap = buildArmorVisibilityMap(zombie, def);

        // =====================================================================
        // 🦴🛡️ تیکه‌های کنده‌شده‌ی بدن (فقط بیسیک و سرمخروطی)
        // =====================================================================
        boolean isBasicOrConeForDebris = zombie.getType() == ZombieType.STANDARD || zombie.getType() == ZombieType.CONEHEAD || zombie.getType()==ZombieType.BUCKETHEAD;
        if (isBasicOrConeForDebris) {
            int debrisFlag = zombie.isHypnotized() ? -1 : 1;
            float debrisScaleX = debrisFlag * GameScreenConstants.ZOMBIE_SCALE;
            float debrisScaleY = GameScreenConstants.ZOMBIE_SCALE;
            String bodyPam = def.pams.get(0).getResolvedPath();

            // --- بازو (مچ+ساعد): وقتی جونِ خودِ زامبی (بعد از زره) به نصف رسید ---
            if (!state.armDropped && zombie.getMaxHealth() > 0
                && zombie.getHealth() <= zombie.getMaxHealth() * ARM_DROP_HEALTH_RATIO) {
                state.armDropped = true;
                spawnFallingPart(bodyPam, chapterPartName("hand_outer_01"), animName, renderAnimTime,
                    baseX, baseY, debrisScaleX, debrisScaleY, false, 0f, DEBRIS_FALL_DISTANCE_HAND_M);
                spawnFallingPart(bodyPam, chapterPartName("arm_outer_lower"), animName, renderAnimTime,
                    baseX, baseY, debrisScaleX, debrisScaleY, false, 0f, DEBRIS_FALL_DISTANCE_HAND_M);
            }
            if (state.armDropped) {
                // بازو دیگه روی بدنِ در حال راه رفتن نمایش داده نشه - چون تیکه‌ی
                // جداش الان مستقلا داره روی زمین می‌افته/نشسته
                if (visibilityMap == null) visibilityMap = new HashMap<>();
                visibilityMap.put(chapterPartName("hand_outer_01"), false);
                visibilityMap.put(chapterPartName("arm_outer_lower"), false);
            }

            // --- زره‌ی سرمخروطی: دقیقا لحظه‌ای که armorHealth از >0 به <=0 می‌ره ---
            if (zombie.getType() == ZombieType.CONEHEAD || zombie.getType()==ZombieType.BUCKETHEAD) {
                float currentArmor = (float)( (StandardZombie)zombie).getArmorHealth();
                if (!Float.isNaN(state.lastArmorHealth) && state.lastArmorHealth > 0 && currentArmor <= 0) {
                    spawnFallingPart(bodyPam, zombie.getType()==ZombieType.CONEHEAD?"zombie_armor_cone_damage_02":"zombie_armor_bucket_damage_02", animName, renderAnimTime,
                        baseX, baseY, debrisScaleX, debrisScaleY, false, 0f, DEBRIS_FALL_DISTANCE_HEAD_AND_CONE_M);
                }
                state.lastArmorHealth = currentArmor;
            }
        }

        // -------------------------------------------------------------------------
        // 💡 بررسی شرط افکت شیشه‌ای/آبی روی زامبی
        // شرط زیر را بر اساس متد/وضعیت واقعی زامبی خود (مثلاً zombie.isFrozen()) تغییر دهید
        // -------------------------------------------------------------------------
        boolean applyGlassEffect = zombie.getType()==ZombieType.STANDARD; // یا هر شرطی مثل zombie.hasEffect()

        for (ZombieVisualRegistry.PamSpec part : def.pams) {
            String resolvedPath = part.getResolvedPath();

            // اگر این فایل قبلاً خطا داده است، لود و رسم آن را نادیده بگیر
            if (brokenAssets.contains(resolvedPath)) {
                continue;
            }

            Color effectColor = null;
            try {
                float drawX = baseX + part.offsetX * Constants.UI.METER_TO_PIX;
                float drawY = baseY + part.offsetY * Constants.UI.METER_TO_PIX;
                int flag = 1;
                if (zombie.getType() == ZombieType.PROSPECTOR_ZOMBIE) {
                    if (((ProspectorZombie) (zombie)).isReversedDirection) flag = -1;
                }
                if (zombie.isHypnotized()) flag = -1;

                float scaleX = flag * GameScreenConstants.ZOMBIE_SCALE;
                float scaleY = GameScreenConstants.ZOMBIE_SCALE;

                // -----------------------------------------------------------------
                // اعمال تینت مخصوص افکت فعلی زامبی (اگه افکتی روش نیست، effectColor
                // همون null می‌مونه و رنگ اصلا دست‌کاری نمی‌شه)
                // -----------------------------------------------------------------
                Color oldColor = null;
                effectColor = computeZombieEffectColor(zombie);

                // 🔥 فلش کوتاه آلفا وقتی دمیج می‌خوره (۰.۲ ثانیه، آلفا=۰.۲) - مخصوص همین زامبی.
                if (state.damageAlphaTimer > 0) {
                    effectColor = new Color(1f, 1f, 1f, 0.6f); // رنگ سفید (بدون تینت) + فقط آلفا کم
                }

                if (effectColor != null) {
                    oldColor = batch.getColor().cpy(); // 👈 .cpy() حیاتیه
                    batch.setColor(effectColor);
                }

                if (visibilityMap != null) {
                    com.badlogic.gdx.math.Matrix4 originalTransform = batch.getTransformMatrix().cpy();
                    com.badlogic.gdx.math.Matrix4 scaledTransform = originalTransform.cpy()
                        .translate(drawX, drawY, 0f)
                        .scale(scaleX, scaleY, 1f)
                        .translate(-drawX, -drawY, 0f);

                    batch.setTransformMatrix(scaledTransform);
                    player.draw(batch, resolvedPath, animName, renderAnimTime, drawX, drawY, flip, visibilityMap);
                    batch.setTransformMatrix(originalTransform);
                } else {
                    player.draw(batch, resolvedPath, animName, renderAnimTime, drawX, drawY, scaleX, scaleY, flip);
                }

                if (oldColor != null) {
                    batch.setColor(oldColor);
                }
            } catch (Throwable e) {
                brokenAssets.add(resolvedPath);
            } finally {
                // اگه exception خورد و oldColor هنوز داخل try بازگردونده نشده بود،
                // این یه لایه‌ی محافظ اضافه‌ست: مطمئن می‌شه batch هیچ‌وقت رنگ‌آلود نمی‌مونه
                if (effectColor != null) {
                    batch.setColor(Color.WHITE);
                }
            }
        }
    }

    // =====================================================================
    // 🛡️ دمیج تدریجی زره (Cone/Bucket/Block head + Knight)
    // =====================================================================
    // هر تکه زره (armorHealth فعلی نسبت به maxArmorHealth اولیه‌اش) به یکی از
    // ۳ مرحله‌ی بصری تقسیم می‌شه: بالای ۲/۳ سالم -> norm ، بین ۱/۳ تا ۲/۳ ->
    // damage_01 ، زیر ۱/۳ (ولی هنوز >0) -> damage_02 . به محض این‌که آرمورHealth
    // به صفر برسه، اون تکه زره کاملا از نمایش حذف می‌شه (نه norm نه damage_XX).

    /** 0=norm, 1=damage_01, 2=damage_02, -1=زره تموم شده/نداره -> اصلا نمایش داده نشه */
    private int armorStage(double current, double max) {
        if (max <= 0 || current <= 0) return -1;
        double ratio = current / max;
        if (ratio > 2.0 / 3.0) return 0;
        if (ratio > 1.0 / 3.0) return 1;
        return 2;
    }

    /** اولین کلید از لیست کلیدهای شناخته‌شده که شامل needle باشه رو برمی‌گردونه (یا null). */
    private String findStateKey(List<String> knownKeys, String needle) {
        for (String key : knownKeys) {
            if (key.contains(needle)) return key;
        }
        return null;
    }

    /**
     * برای یک تکه‌ی زره مشخص (مثلا cone، bucket، brick، crown یا shoulder)، بر
     * اساس current/max فعلی‌اش، دقیقا یکی از کلیدهای «{prefix}norm / damage_01 /
     * damage_02» رو true می‌کنه (و در صورت وجود، کلید master رو هم true می‌کنه)؛
     * اگه زره تموم شده باشه، هیچی تغییر نمی‌ده (همه از قبل روی false هستن).
     */
    private void applyArmorPieceStage(Map<String, Boolean> map, String masterKey, String prefix,
                                      double current, double max) {
        int stage = armorStage(current, max);
        if (stage == -1) return; // زره این تکه تموم شده -> کلا مخفی بمونه

        if (masterKey != null) map.put(masterKey, true);
        String activeKey = switch (stage) {
            case 0 -> prefix + "norm";
            case 1 -> prefix + "damage_01";
            default -> prefix + "damage_02";
        };
        map.put(activeKey, true);
    }

    /**
     * جدول ویزیبل نهایی این فریم برای این زامبی رو می‌سازه: برای زامبی‌های
     * زره‌دار (cone/bucket/block/knight)، دقیقا مرحله‌ی درست هر تکه زره رو طبق
     * armorHealth واقعی مدل true و بقیه‌ی مراحل رو false نگه می‌داره. برای بقیه‌ی
     * زامبی‌ها (که extraStateFilterTemplates‌شون خالیه یا کلید ثابت/بدون-مرحله
     * دارن)، رفتار قبلی حفظ می‌شه: هر کلید تعریف‌شده true.
     */
    private Map<String, Boolean> buildArmorVisibilityMap(Zombie zombie, ZombieVisualRegistry.ZombieVisualDef def) {
        List<String> stateFilters = def.getResolvedStateFilters();
        if (stateFilters.isEmpty()) return null;

        Map<String, Boolean> map = new HashMap<>();
        for (String key : stateFilters) {
            map.put(key, false); // پیش‌فرض: همه چیز مخفی، بعدا دقیقا کلید(های) درست true می‌شن
        }

        ZombieType type = zombie.getType();
        if (type == ZombieType.KNIGHT && zombie instanceof KnightZombie knight) {
            applyArmorPieceStage(map,
                findStateKey(stateFilters, "armor_crown_states"), "zombie_armor_crown_",
                knight.helmetArmorHealth, knight.maxHelmetArmorHealth);
            applyArmorPieceStage(map,
                findStateKey(stateFilters, "zombie_shoulder_armor"), "zombie_shoulder_armor_",
                knight.shoulderArmorHealth, knight.maxShoulderArmorHealth);
        } else if ((type == ZombieType.CONEHEAD || type == ZombieType.BUCKETHEAD || type == ZombieType.BLOCKHEAD)
            && zombie instanceof StandardZombie sz) {
            String prefix = switch (type) {
                case CONEHEAD -> "zombie_armor_cone_";
                case BUCKETHEAD -> "zombie_armor_bucket_";
                default -> "zombie_armor_brick_"; // BLOCKHEAD
            };
            String masterNeedle = switch (type) {
                case CONEHEAD -> "armor1_states";
                case BUCKETHEAD -> "armor2_states";
                default -> null; // سر بلوکی کلید master جدا نداره
            };
            String masterKey = masterNeedle != null ? findStateKey(stateFilters, masterNeedle) : null;
            applyArmorPieceStage(map, masterKey, prefix, sz.getArmorHealth(), sz.getMaxArmorHealth());
        } else {
            // زامبی‌های دیگه‌ای که extraStateFilterTemplates ثابت (بدون مرحله) دارن؛
            // رفتار قبلی رو حفظ می‌کنیم تا چیزی نشکنه.
            for (String key : stateFilters) {
                map.put(key, true);
            }
        }

        return map;
    }

    // ارتفاع اوج قوس پرتاب (پیکسل) - وسط مسیر بیشترین ارتفاع رو می‌گیره، اول و
    // آخر مسیر صفره. اگه خواستی قوس بلندتر/کوتاه‌تر باشه فقط همین عدد رو عوض کن.
    private static final float IMP_THROW_ARC_HEIGHT_PX = 140f;

    /**
     * رسم زامبی‌ای که در حال پخش انیمیشن پرتاب/فرود (fly-in) هست - مثلا ایمپی
     * که غول تازه پرتابش کرده. به‌جای انیمیشن عادی (walk/eat/...)، حالت "fly"
     * پخش می‌شه و موقعیتش رو خودمون (نه مدل) روی یک مسیر سهمی از مبدا پرتاب تا
     * مقصد نهایی (که همون X/Y فعلی زامبی‌ست، چون در مدل فریز شده) درون‌یابی
     * می‌کنیم. بعد از این‌که مدل خودش freeze رو تمام کنه، این متد دیگه صدا زده
     * نمی‌شه و drawSingleZombie طبق روال عادی ادامه می‌ده - یعنی «لینک‌شدن» به
     * ایمپ واقعی خودکاره، چون از اول همون یک Zombie بوده.
     */
    private void drawFlyingInZombie(Zombie zombie, ZombieRenderState state, float delta, ZombieVisualRegistry.ZombieVisualDef def) {
        String typeKey = zombie.getType().name();
        if (!ZombieVisualRegistry.isAvailable(typeKey)) {
            return;
        }

        float progress = zombie.getFlyInProgress(); // 0 (شروع پرتاب) .. 1 (لحظه‌ی فرود)

        float originX = (float) zombie.getThrowOriginX() * Constants.UI.METER_TO_PIX;
        float originY = (float) zombie.getThrowOriginY() * Constants.UI.METER_TO_PIX;
        float targetX = (float) zombie.getX() * Constants.UI.METER_TO_PIX;
        float targetY = (float) zombie.getY() * Constants.UI.METER_TO_PIX;

        // مسیر خطی بین مبدا و مقصد روی هر دو محور...
        float linearX = originX + (targetX - originX) * progress;
        float linearY = originY + (targetY - originY) * progress;
        // ...به‌علاوه‌ی یک افست سهمی استاندارد (4*h*t*(1-t)) روی Y که در t=0 و t=1
        // صفره و در t=0.5 به بیشترین مقدار (h) می‌رسه - همون حس «پرتاب و فرود».
        float arcOffset = 4f * IMP_THROW_ARC_HEIGHT_PX * progress * (1f - progress);

        float baseX = linearX;
        float baseY = linearY + arcOffset;

        // انیمیشن fly مستقل و پیوسته پخش می‌شه (نه وابسته به eating/walking)
        state.animTime += delta;

        // جهت: اگه مبدا سمت راستِ مقصده (حالت معمول، چون غول جلوتر از ایمپه)
        // یعنی ایمپ داره به چپ پرت می‌شه -> flip=true (جهت پیش‌فرض/عادی راه رفتن)
        boolean flip = originX >= targetX;

        for (ZombieVisualRegistry.PamSpec part : def.pams) {
            String resolvedPath = part.getResolvedPath();
            if (brokenAssets.contains(resolvedPath)) {
                continue;
            }
            try {
                float drawX = baseX + part.offsetX * Constants.UI.METER_TO_PIX;
                float drawY = baseY + part.offsetY * Constants.UI.METER_TO_PIX;
                player.draw(
                    batch,
                    resolvedPath,
                    "fly",
                    state.animTime,
                    drawX,
                    drawY,
                    GameScreenConstants.ZOMBIE_SCALE,
                    GameScreenConstants.ZOMBIE_SCALE,
                    flip
                );
            } catch (Throwable e) {
                brokenAssets.add(resolvedPath);
            }
        }
    }

    /**
     * رندر تمام ماشین‌های چمن‌زن (یکی به‌ازای هر ردیف/لاین).
     * دسترسی طبق درخواست: AppModel.gameSession.gameBoard.lanes.get(i).lawnMower
     */
    private void drawMowers(float delta) {
        if (AppModel.gameSession == null || player == null) {
            return;
        }

        String resolvedPath = MOWER_PAM_SPEC.getResolvedPath();
        if (brokenAssets.contains(resolvedPath)) {
            return;
        }

        var lanes = AppModel.gameSession.gameBoard.lanes;
        for (int i = 0; i < lanes.size(); i++) {
            LawnMower mower = lanes.get(i).lawnMower;
            if (mower == null || !mower.isAlive()) {
                continue;
            }

            MowerRenderState state = mowerRenderStates.computeIfAbsent(mower, m -> new MowerRenderState());

            // طبق درخواست: اگر x برابر Constants.Game.X_OF_MOWER باشد -> idle، وگرنه transition.
            // مقایسه‌ی مستقیم == روی float/double به‌خاطر خطای محاسباتی خطرناکه، پس
            // با یک تلورانس خیلی کوچک مقایسه می‌کنیم.
            boolean isIdle = Math.abs(mower.getX() - Constants.Game.X_OF_MOWER) < 1e-4;
            if (isIdle != state.wasIdle) {
                state.animTime = 0f; // تغییر حالت -> ریست تایمر انیمیشن برای پخش روان از ابتدا
                state.wasIdle = isIdle;
            }
            String animName = isIdle ? "idle" : "transition";
            if (isPaused) animName = "idle";

            state.animTime += delta;

            float drawX = (float) mower.getX() * Constants.UI.METER_TO_PIX;
            float drawY = (float) mower.getY() * Constants.UI.METER_TO_PIX;

            try {
                player.draw(
                    batch,
                    resolvedPath,
                    animName,
                    state.animTime,
                    drawX,
                    drawY,
                    GameScreenConstants.MOWER_SCALE,
                    GameScreenConstants.MOWER_SCALE,
                    true
                );
            } catch (Throwable e) {
                brokenAssets.add(resolvedPath);
                Gdx.app.error("PVZ-ASSET-MISSING",
                    "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n"
                        + "❌ [جلوگیری از کرش] بارگذاری/رندر ماشین چمن‌زن ناموفق بود!\n"
                        + "ردیف: " + i + "\n"
                        + "مسیر است درخواست شده: " + resolvedPath + "\n"
                        + "دلیل دقیق خطا: " + e.toString() + "\n"
                        + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", e);
            }
        }
    }

    /**
     * رندر تمام قبرهای (Tomb) روی زمین - دقیقا هم‌الگو با drawMowers، با این
     * تفاوت که به‌جای idle/transition، حالت گرافیکی بر اساس درصد جونِ باقی‌مانده
     * انتخاب می‌شه (undamaged -> damage1..4) و وقتی isDestroyed() شد، دیگه اصلا
     * پیمایش/رندر نمی‌شه (خودِ Lane.tick() از لیست حذفش کرده).
     */
    private void drawTombs(float delta) {
        if (AppModel.gameSession == null || player == null) {
            return;
        }

        String resolvedPath = TOMB_PAM_SPEC.getResolvedPath();
        if (brokenAssets.contains(resolvedPath)) {
            return;
        }

        var lanes = AppModel.gameSession.gameBoard.lanes;
        for (var lane : lanes) {
            for (Tomb tomb : lane.tombs) {
                if (tomb.isDestroyed()) continue; // احتیاط اضافه؛ Lane.tick() خودش حذفشون می‌کنه

                TombRenderState state = tombRenderStates.computeIfAbsent(tomb, t -> new TombRenderState());

                if (tomb.takedDamage) {
                    state.damageAlphaTimer = 0.15f;
                    tomb.takedDamage = false;
                }
                if (state.damageAlphaTimer > 0) {
                    state.damageAlphaTimer -= delta;
                }

                // انتخاب حالت گرافیکی بر اساس نسبت جون باقی‌مانده
                double ratio = tomb.getHealthRatio();
                String animName;
                if (ratio >= 1.0) animName = "undamaged";
                else if (ratio > 0.75) animName = "damage1";
                else if (ratio > 0.5) animName = "damage2";
                else if (ratio > 0.25) animName = "damage3";
                else animName = "damage4";

                // 👈 طبق درخواست: اول x,y در METER_TO_PIX ضرب می‌شن، بعد دو پدینگ
                // (PADDING_X_REALITY/PADDING_Y_REALITY - همون‌هایی که برای مووِر و
                // مدل هم استفاده می‌شن) روش اعمال می‌شه.
                float drawX = (float) (tomb.getPositionX()) * Constants.UI.METER_TO_PIX;
                float drawY = (float) (tomb.getPositionY() + Constants.Game.PADDING_Y_REALITY) * Constants.UI.METER_TO_PIX;

                state.lastDrawX = drawX;
                state.lastDrawY = drawY;

                Color oldColor = null;
                try {
                    // فلش دمیج مثل زامبی: رنگ سفید + فقط آلفا کم می‌شه (کم‌رنگ/محو
                    // می‌شه)، نه روشن‌تر - قبلا اینجا اشتباهی رنگ رو *روشن‌تر*
                    // (1.4,1.4,1.4) می‌کرد که دقیقا برعکس افکت زامبی بود.
                    if (state.damageAlphaTimer > 0) {
                        oldColor = batch.getColor().cpy();
                        batch.setColor(new Color(1f, 1f, 1f, 0.6f));
                    }

                    player.draw(
                        batch,
                        resolvedPath,
                        animName,
                        0f, // قبر ثابته، نیازی به تایمر انیمیشن مستمر نداره
                        drawX,
                        drawY,
                        GameScreenConstants.TOMB_SCALE,
                        GameScreenConstants.TOMB_SCALE,
                        true
                    );

                    if (oldColor != null) {
                        batch.setColor(oldColor);
                    }
                } catch (Throwable e) {
                    brokenAssets.add(resolvedPath);
                } finally {
                    if (state.damageAlphaTimer > 0) {
                        batch.setColor(Color.WHITE);
                    }
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height, true);
        if (uiStage != null) {
            uiStage.getViewport().update(width, height, true);
        }
    }

    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void hide() {
        // نکته‌ی مهم (فیکس نشتی حافظه/GPU که باعث خراب‌شدن رندر صفحات دیگه مثل
        // MainMenuScreen می‌شد): در LibGDX وقتی با Game.setScreen(...) صفحه عوض
        // می‌شه، فقط hide() صدا زده می‌شه، نه dispose() - پس آزادسازی منابع سنگین
        // (خصوصا textureBank که قبلا اصلا dispose نمی‌شد) باید همینجا هم انجام بشه،
        // وگرنه هر بار ورود به GameScreen یه دسته texture جدید لود می‌شه که هیچ‌وقت
        // آزاد نمی‌شه و در نهایت حافظه‌ی GPU رو با texture های یتیم پر می‌کنه.
        releaseGraphicsResources();
    }

    @Override
    public void dispose() {
        // اینجا هم صدا می‌زنیم چون dispose() ممکنه علاوه بر hide() هم اجرا بشه
        // (مثلا موقع بستن کامل اپ)؛ releaseGraphicsResources خودش idempotent هست.
        releaseGraphicsResources();
    }

    /**
     * تمام منابع گرافیکی سنگینی که در show()/loadAssets()/setupPauseUI() ساخته
     * شدن رو آزاد می‌کنه. هم از hide() و هم از dispose() صدا زده می‌شه (چون در
     * LibGDX فقط hide() تضمینیه که هر بار خروج از این صفحه صدا زده بشه)، پس با
     * یک فلگ جلوی dispose دوباره‌ی همون شیء (که خودش می‌تونه کرش بده) رو می‌گیریم.
     */
    private boolean resourcesReleased = false;

    private void releaseGraphicsResources() {
        if (resourcesReleased) {
            return;
        }
        resourcesReleased = true;

        if (batch != null) batch.dispose();
//        if (textureBank != null) textureBank.dispose();
        if (uiStage != null) uiStage.dispose();
        if (pauseOverlayTexture != null) pauseOverlayTexture.dispose();

        // این خط را پاک کنید:
        // if (pausePanelBgTexture != null) pausePanelBgTexture.dispose();

        zombieRenderStates.clear();
        mowerRenderStates.clear();
        tombRenderStates.clear();
    }
}
