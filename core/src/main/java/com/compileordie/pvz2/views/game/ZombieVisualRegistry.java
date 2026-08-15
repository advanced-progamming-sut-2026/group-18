package com.compileordie.pvz2.views.game;

import com.badlogic.gdx.Gdx;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.game.levels.ChapterType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ZombieVisualRegistry {

    /**
     * ریشه‌ی واقعی‌ای که PamPlayer/TextureBank هنگام لودکردن فایل از آن استفاده می‌کند.
     * این مقدار برای جلوگیری از خطای NoSuchFileException و بررسی دقیق دیسک ضروری است.
     */
    private static final String REAL_ASSET_ROOT = "pvz-assets/IMAGES/";

    public static class PamSpec {
        public final String pathTemplate;
        public final float offsetX;
        public final float offsetY;

        public PamSpec(String pathTemplate, float offsetX, float offsetY) {
            this.pathTemplate = pathTemplate;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        public PamSpec(String pathTemplate) {
            this(pathTemplate, 0f, 0f);
        }

        /**
         * 🚨 ریشه‌ی اصلی باگ «زامبی اسپاون شده ولی رندر نشده»:
         * getResolvedPath() همیشه، برای *هر* PamSpec، بر اساس چپتر فعلی
         * FULL<->INITIAL رو جابه‌جا می‌کنه (خط پایین‌تر). این برای زامبی‌های
         * "بدون چپتر" (allstar, parasol, prospector, explorer, dodo rider،
         * imp عادی و ده‌ها مورد دیگه) فاجعه‌ست: مسیر واقعی‌شون ثابته (مثلا
         * همیشه FULL)، ولی اگه چپتر فعلی EGYPT باشه (یا هر جای دیگه که این
         * زامبی رو تست‌اسپاون کنی)، این تابع بی‌سروصدا "/FULL/" رو به
         * "/INITIAL/" تبدیل می‌کنه -> یه مسیر ناموجود -> existsOnDisk()==false
         * -> isAvailable()==false -> GameScreen کاملا رد می‌کنه (نه کرش، فقط
         * نامرئی). از fixed(...) استفاده کن تا این جایگزینی خودکار خنثی بشه.
         */
        public static PamSpec fixed(String literalPath) {
            return new PamSpec(literalPath) {
                @Override
                public String getResolvedPath() {
                    return this.pathTemplate; // مسیر واقعی، بدون هیچ جایگزینی FULL/INITIAL یا {CH}
                }
            };
        }

        /**
         * جایگزینی تگ چپتر و تنظیم خودکار مسیر (INITIAL برای مصر، FULL برای سایر).
         */
        public String getResolvedPath() {
            if (pathTemplate == null) return "";

            String chTag = getChapterTag();
            String path = pathTemplate;

            if ("EGYPT".equals(chTag)) {
                path = path.replace("/FULL/", "/INITIAL/");
            } else {
                path = path.replace("/INITIAL/", "/FULL/");
            }

            return path.replace("{CH}", chTag);
        }

        /**
         * بررسی ایمن وجود فیزیکی فایل روی دیسک با استفاده از پیشوند واقعی.
         */
        public boolean existsOnDisk() {
            try {
                String realPath = REAL_ASSET_ROOT + getResolvedPath();
                return Gdx.files.internal(realPath).exists();
            } catch (Throwable t) {
                Gdx.app.error("PVZ-REGISTRY-ERROR", "❌ خطا در بررسی دیسک برای فایل Asset: " + t.getMessage());
                return false;
            }
        }
    }

    public static class ZombieVisualDef {
        public final List<PamSpec> pams;
        public final List<String> extraStateFilterTemplates;

        public ZombieVisualDef(List<PamSpec> pams, List<String> extraStateFilterTemplates) {
            this.pams = pams != null ? pams : Collections.emptyList();
            this.extraStateFilterTemplates = extraStateFilterTemplates != null ? extraStateFilterTemplates : Collections.emptyList();
        }

        public List<String> getResolvedStateFilters() {
            String chTag = getChapterTag();
            List<String> resolved = new ArrayList<>(extraStateFilterTemplates.size());
            for (String tmpl : extraStateFilterTemplates) {
                resolved.add(tmpl.replace("{CH}", chTag).replace("{ch}", chTag.toLowerCase()));
            }
            return resolved;
        }

        /**
         * بررسی اینکه آیا تمام فایل‌های Asset این زامبی برای چپتر فعلی روی دیسک موجودند یا خیر.
         */
        public boolean isAvailableForCurrentChapter() {
            if (pams.isEmpty()) return false;
            for (PamSpec pam : pams) {
                if (!pam.existsOnDisk()) {
                    return false;
                }
            }
            return true;
        }
    }

    private static final Map<String, ZombieVisualDef> REGISTRY = new HashMap<>();

    // استفاده از ConcurrentHashMap برای جلوگیری از خطاهای همزمانی (Concurrency) در ترد رندر
    private static final Map<String, Boolean> AVAILABILITY_CACHE = new ConcurrentHashMap<>();

    public static void register(String zombieTypeName, ZombieVisualDef def) {
        if (zombieTypeName != null && def != null) {
            REGISTRY.put(zombieTypeName.toUpperCase(), def);
        }
    }

    public static ZombieVisualDef get(String zombieTypeName) {
        if (zombieTypeName == null || zombieTypeName.trim().isEmpty()) return null;
        String upper = zombieTypeName.trim().toUpperCase();

        ZombieVisualDef def = REGISTRY.get(upper);
        if (def != null) return def;

        String noUnderscore = upper.replace("_", "");
        def = REGISTRY.get(noUnderscore);
        if (def != null) return def;

        if (upper.endsWith("_ZOMBIE")) {
            String base = upper.substring(0, upper.length() - "_ZOMBIE".length());
            def = REGISTRY.get(base);
            if (def == null) def = REGISTRY.get(base.replace("_", ""));
        } else {
            String withZombie = upper + "_ZOMBIE";
            def = REGISTRY.get(withZombie);
            if (def == null) def = REGISTRY.get(withZombie.replace("_", ""));
        }

        return def;
    }

    /**
     * بررسی وجود فایل‌های زامبی با استفاده از کش برای جلوگیری از افت فریم (I/O Bottleneck).
     */
    public static boolean isAvailable(String zombieTypeName) {
        if (zombieTypeName == null) return false;
        ZombieVisualDef def = get(zombieTypeName);
        if (def == null) return false;

        String cacheKey = zombieTypeName.toUpperCase() + "@" + getChapterTag();

        // بررسی کش
        Boolean cached = AVAILABILITY_CACHE.get(cacheKey);
        if (cached != null) return cached;

        // محاسبه و ذخیره در کش
        boolean available = def.isAvailableForCurrentChapter();
        AVAILABILITY_CACHE.put(cacheKey, available);
        return available;
    }

    /**
     * پاک‌سازی کش. در متدهای show() یا loadAssets() کلاس GameScreen حتماً فراخوانی شود.
     */
    public static void clearAvailabilityCache() {
        AVAILABILITY_CACHE.clear();
    }

    public static String getChapterTag() {
        if (AppModel.currentChapter == null) {
            return "EGYPT";
        }
        // 🕹️ طبق درخواست: مینی‌گیم‌ها (و هر چپتر دیگه‌ای که این switch نمی‌شناستش)
        // باید دقیقا مثل EGYPT رفتار کنن؛ همین default از قبل این کار رو می‌کنه.
        return switch (AppModel.currentChapter) {
            case ANCIENT_EGYPT -> "EGYPT";
            case BIG_WAVE_BEACH -> "BEACH";
            case DARK_AGES -> "DARK";
            case FROSTBITE_CAVES -> "ICEAGE";
            default -> "EGYPT";
        };
    }

    /**
     * 🧾 برای هر PamSpec این تعریف، مسیر resolve‌شده‌اش رو برمی‌گردونه به همراه
     * اینکه رو دیسک هست یا نه - صرفا برای ساختن لاگ‌های دقیق (GameScreen ازش
     * استفاده می‌کنه تا بگه *دقیقا* کدوم فایل گمه، نه فقط «یه چیزی گم شده»).
     */
    public static List<String> describeMissingAssets(String zombieTypeName) {
        List<String> missing = new ArrayList<>();
        ZombieVisualDef def = get(zombieTypeName);
        if (def == null) return missing;
        for (PamSpec pam : def.pams) {
            if (!pam.existsOnDisk()) {
                missing.add(pam.getResolvedPath());
            }
        }
        return missing;
    }

    // مقداردهی اولیه‌ی رجیستری
    static {
        // ۱. زامبی استاندارد
        ZombieVisualDef standardDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/ZOMBIE/ZOMBIE_{CH}_BASIC/ZOMBIE_{CH}_BASIC.PAM")),
            Collections.emptyList()
        );
        register("STANDARD", standardDef);
        register("BASIC", standardDef);
        register("STANDARD_ZOMBIE", standardDef);

        // ۲. سر مخروطی
        // 🛡️ مراتب دمیج تدریجی (از سالم به شکسته): norm -> damage_01 -> damage_02
        // -> (وقتی armorHealth<=0) هیچ‌کدوم. مقدار true/false هر کلید دیگه به‌صورت
        // ثابت اینجا ست نمی‌شه؛ GameScreen.drawSingleZombie هر فریم بر اساس
        // armorHealth/maxArmorHealth واقعیِ زامبی، دقیقا یکی از این‌ها رو انتخاب
        // می‌کنه (نگاه کن به GameScreen#buildArmorVisibilityMap).
        ZombieVisualDef coneheadDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/ZOMBIE/ZOMBIE_{CH}_BASIC/ZOMBIE_{CH}_BASIC.PAM")),
            List.of(
                "_zombie_{ch}_armor1_states",
                "zombie_armor_cone_norm",
                "zombie_armor_cone_damage_01",
                "zombie_armor_cone_damage_02"
            )
        );
        register("CONEHEAD", coneheadDef);
        register("CONEHEAD_ZOMBIE", coneheadDef);

        // ۳. سر سطلی (همون منطق بالا، برای bucket)
        ZombieVisualDef bucketheadDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/ZOMBIE/ZOMBIE_{CH}_BASIC/ZOMBIE_{CH}_BASIC.PAM")),
            List.of(
                "_zombie_{ch}_armor2_states",
                "zombie_armor_bucket_norm",
                "zombie_armor_bucket_damage_01",
                "zombie_armor_bucket_damage_02"
            )
        );
        register("BUCKETHEAD", bucketheadDef);
        register("BUCKETHEAD_ZOMBIE", bucketheadDef);

        // ۴. سر بلوکی (بدون کلید master جدا - طبق مشخصات، فقط ۳ مرحله‌ی خود brick)
        ZombieVisualDef blockheadDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/ZOMBIE/ZOMBIE_{CH}_BASIC/ZOMBIE_{CH}_BASIC.PAM")),
            List.of(
                "zombie_armor_brick_norm",
                "zombie_armor_brick_damage_01",
                "zombie_armor_brick_damage_02"
            )
        );
        register("BLOCKHEAD", blockheadDef);
        register("BLOCKHEAD_ZOMBIE", blockheadDef);

        // ۵. شوالیه: دو تکه‌ی زره‌ی مستقل از هم - هلمت (crown) اول دمیج می‌بینه،
        // بعد شولدر (shoulder)، بعد خودِ زامبی. هر تکه مراتب norm/damage_01/damage_02
        // خودش رو داره و مستقل از اون یکی محاسبه می‌شه (نگاه کن به KnightZombie
        // #helmetArmorHealth/#shoulderArmorHealth و GameScreen#buildArmorVisibilityMap).
        ZombieVisualDef knightDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/ZOMBIE/ZOMBIE_{CH}_BASIC/ZOMBIE_{CH}_BASIC.PAM")),
            List.of(
                "_zombie_armor_crown_states",
                "zombie_armor_crown_norm",
                "zombie_armor_crown_damage_01",
                "zombie_armor_crown_damage_02",
                "zombie_shoulder_armor",
                "zombie_shoulder_armor_norm",
                "zombie_shoulder_armor_damage_01",
                "zombie_shoulder_armor_damage_02"
            )
        );
        register("KNIGHT", knightDef);
        register("KNIGHT_ZOMBIE", knightDef);

        // ۶. را زامبی
        ZombieVisualDef raDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/ZOMBIE/ZOMBIE_{CH}_RA/ZOMBIE_{CH}_RA.PAM")),
            Collections.emptyList()
        );
        register("RA", raDef);
        register("RA_ZOMBIE", raDef);

        // ۷. غول (Gargantuar - با در نظر گرفتن استثنای فصل ICEAGE)
        ZombieVisualDef gargDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/ZOMBIE/{CH}_GARGANTUAR/{CH}_GARGANTUAR.PAM") {
                @Override
                public String getResolvedPath() {
                    String chTag = getChapterTag();
                    if ("ICEAGE".equals(chTag)) {
                        return "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_GARGANTUAR/ZOMBIE_ICEAGE_GARGANTUAR.PAM";
                    }
                    return super.getResolvedPath();
                }
            }),
            Collections.emptyList()
        );
        register("GARGANTUAR", gargDef);
        register("GARGANTUAR_ZOMBIE", gargDef);

        // ۸. ایمپ - مسیر ثابت و تایید شده، مستقل از چپتر. (⚠️ قبلا این‌جا یه
        // switch حدسی بر اساس چپتر بود که مسیرهای تایید‌نشده مثل ZOMBIE_EGYPT_IMP
        // و ZOMBIE_BEACH_IMP_MERMAID می‌ساخت؛ اگه اون پوشه‌ها واقعا رو دیسک وجود
        // نداشته باشن، دقیقا همون باگ «اسپاون شده ولی رندر نشده» رخ می‌ده. طبق
        // تایید نهایی، ایمپ همیشه همین یک مسیره.)
        ZombieVisualDef impDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_DARK_IMP_MONK/ZOMBIE_DARK_IMP_MONK.PAM")),
            Collections.emptyList()
        );
        register("IMP", impDef);
        register("IMP_ZOMBIE", impDef);

        // ۹. آل استار
        ZombieVisualDef allstarDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_MODERN_ALLSTAR/ZOMBIE_MODERN_ALLSTAR.PAM")),
            Collections.emptyList()
        );
        register("ALLSTAR", allstarDef);
        register("ALL_STAR", allstarDef);
        register("ALL_STAR_ZOMBIE", allstarDef);

        // ۱۰. پارازول
        ZombieVisualDef parasolDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_LOSTCITY_JANE/ZOMBIE_LOSTCITY_JANE.PAM")),
            Collections.emptyList()
        );
        register("PARASOL", parasolDef);
        register("PARASOL_ZOMBIE", parasolDef);

        // ۱۱. جمجمه کریستالی
        ZombieVisualDef turquoiseDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_LOSTCITY_CRYSTALSKULL/ZOMBIE_LOSTCITY_CRYSTALSKULL.PAM")),
            Collections.emptyList()
        );
        register("TURQUOISE", turquoiseDef);
        register("TURQUOISE_ZOMBIE", turquoiseDef);

        // ۱۲. روزنامه
        ZombieVisualDef newspaperDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_MODERN_NEWSPAPER/ZOMBIE_MODERN_NEWSPAPER.PAM")),
            Collections.emptyList()
        );
        register("NEWSPAPER", newspaperDef);
        register("NEWSPAPER_ZOMBIE", newspaperDef);

        // ۱۳. کاوشگر
        ZombieVisualDef prospectorDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_PROSPECTOR/ZOMBIE_PROSPECTOR.PAM")),
            Collections.emptyList()
        );
        register("PROSPECTOR", prospectorDef);
        register("PROSPECTOR_ZOMBIE", prospectorDef);

        // ۱۴. پیانو
        ZombieVisualDef pianistDef = new ZombieVisualDef(
            List.of(
                PamSpec.fixed("768/FULL/ZOMBIE/PIANO/PIANO.PAM"),
                PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_PIANO/ZOMBIE_PIANO.PAM")
            ),
            Collections.emptyList()
        );
        register("PIANIST", pianistDef);
        register("PIANIST_ZOMBIE", pianistDef);
        register("PIANO", pianistDef);

        // ۱۵. بشکه‌ران
        ZombieVisualDef barrelDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_PIRATE_BARREL_PUSHER/ZOMBIE_PIRATE_BARREL_PUSHER.PAM")),
            Collections.emptyList()
        );
        register("BARREL_PUSHER", barrelDef);
        register("BARREL_ROLLER", barrelDef);
        register("BARREL_ZOMBIE", barrelDef);

        // ۱۶. مشعل‌دار
        ZombieVisualDef explorerDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/INITIAL/ZOMBIE/ZOMBIE_EXPLORER/ZOMBIE_EXPLORER.PAM")),
            Collections.emptyList()
        );
        register("EXPLORER", explorerDef);
        register("EXPLORER_ZOMBIE", explorerDef);
        register("TORCHLIGHT", explorerDef);

        // ۱۷. قبرساز
        ZombieVisualDef tombraiserDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/ZOMBIE/ZOMBIE_{CH}_TOMBRAISER/ZOMBIE_{CH}_TOMBRAISER.PAM")),
            Collections.emptyList()
        );
        register("TOMBRAISER", tombraiserDef);
        register("TOMB_RAISER", tombraiserDef);
        register("TOMBRAISER_ZOMBIE", tombraiserDef);

        // ۱۸. دودو رایدر
        ZombieVisualDef dodoDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_ICEAGE_DODORIDER/ZOMBIE_ICEAGE_DODORIDER.PAM")),
            Collections.emptyList()
        );
        register("DODORIDER", dodoDef);
        register("DODO_RIDER", dodoDef);
        register("DODORIDER_ZOMBIE", dodoDef);

        // ۱۹. شکارچی
        ZombieVisualDef hunterDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_ICEAGE_HUNTER/ZOMBIE_ICEAGE_HUNTER.PAM")),
            Collections.emptyList()
        );
        register("HUNTER", hunterDef);
        register("HUNTER_ZOMBIE", hunterDef);

        // ۲۰. غواص
        ZombieVisualDef snorkelerDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_BEACH_SNORKELER/ZOMBIE_BEACH_SNORKELER.PAM")),
            Collections.emptyList()
        );
        register("SNORKELER", snorkelerDef);
        register("SNORKEL_ZOMBIE", snorkelerDef);
        register("DIVER", snorkelerDef);

        // ۲۱. اختاپوس
        ZombieVisualDef octopusDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_BEACH_OCTOPUS/ZOMBIE_BEACH_OCTOPUS.PAM")),
            Collections.emptyList()
        );
        register("OCTOPUS", octopusDef);
        register("OCTOPUS_ZOMBIE", octopusDef);

        // ۲۲. ایمپ اژدها
        ZombieVisualDef impDragonDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_DARK_IMP_DRAGON/ZOMBIE_DARK_IMP_DRAGON.PAM")),
            Collections.emptyList()
        );
        register("IMP_DRAGON", impDragonDef);
        register("IMP_DRAGON_ZOMBIE", impDragonDef);

        // ۲۳. ماهیگیر
        ZombieVisualDef fishermanDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_BEACH_FISHERMAN/ZOMBIE_BEACH_FISHERMAN.PAM")),
            Collections.emptyList()
        );
        register("FISHERMAN", fishermanDef);
        register("FISHERMAN_ZOMBIE", fishermanDef);

        // ۲۴. انیمیشن خاکستر (برای مرگ با مواد منفجره)
        ZombieVisualDef explosiveDeathDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/EFFECTS/ZOMBIE_ASH/ZOMBIE_ASH.PAM") {
                @Override
                public String getResolvedPath() {
                    // 👈 این اورراید باعث می‌شه سیستم، مسیر INITIAL رو الکی به FULL تبدیل نکنه
                    return this.pathTemplate;
                }
            }),
            Collections.emptyList()
        );
        register("EXPLOSIVE_DEATH", explosiveDeathDef);
        register("ASH_EFFECT", explosiveDeathDef);

        // ==========================================================
        // ⚠️ موارد زیر (۲۵-۲۹) قبلا اصلا در این رجیستری تعریف نشده بودن - همین
        // باعث می‌شد ZombieVisualRegistry.get(...) براشون null برگردونه و
        // GameScreen.drawSingleZombie() کاملا رد بشون کنه (نه کرش می‌کرد نه رندر
        // می‌کرد؛ فقط لاگ DEF_MISSING می‌زد) با اینکه WaveManager طبق چپترشون
        // (ZombieType.chapter) داشت درست اسپاونشون می‌کرد. یعنی "زامبی اسپاون شده
        // ولی رندر نشده" دقیقا همینجا اتفاق می‌افتاد.
        // 🚨 توجه: چون پوشه‌ی pvz-assets داخل پروژه‌ای که برام فرستاده شده وجود
        // نداشت، نتونستم این مسیرها رو مثل بقیه روی دیسک واقعی تایید کنم - قبل
        // از اعتماد کامل، اسم واقعی پوشه‌ها رو زیر pvz-assets/IMAGES/768/FULL/ZOMBIE/
        // چک/اصلاح کن. تا وقتی مسیر درست نشه، existsOnDisk() خودش false برمی‌گردونه
        // و باز هم امن (بدون کرش، فقط رد/لاگ) می‌مونه.
        // ==========================================================

        // ۲۵. دلقک (Jester) - قرون وسطی
        ZombieVisualDef jesterDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_DARK_JESTER/ZOMBIE_DARK_JESTER.PAM")),
            Collections.emptyList()
        );
        register("JESTER", jesterDef);
        register("JESTER_ZOMBIE", jesterDef);

        // ۲۶. جادوگر (Wizard) - قرون وسطی
        ZombieVisualDef wizardDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_DARK_WIZARD/ZOMBIE_DARK_WIZARD.PAM")),
            Collections.emptyList()
        );
        register("WIZARD", wizardDef);
        register("WIZARD_ZOMBIE", wizardDef);

        // ۲۷. پادشاه (King) - قرون وسطی
        ZombieVisualDef kingDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_DARK_KING/ZOMBIE_DARK_KING.PAM")),
            Collections.emptyList()
        );
        register("KING", kingDef);
        register("KING_ZOMBIE", kingDef);

        // ۲۸. تروگلوبایت - غارهای یخی
        ZombieVisualDef troglobiteDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_ICEAGE_TROGLOBITE/ZOMBIE_ICEAGE_TROGLOBITE.PAM")),
            Collections.emptyList()
        );
        register("TROGLOBITE", troglobiteDef);
        register("TROGLOBITE_ZOMBIE", troglobiteDef);

        // ۲۹. آرکید (مینی‌گیم/مدرن، بدون چپتر خاص)
        ZombieVisualDef arcadeDef = new ZombieVisualDef(
            List.of(PamSpec.fixed("768/FULL/ZOMBIE/ZOMBIE_MODERN_ARCADE/ZOMBIE_MODERN_ARCADE.PAM")),
            Collections.emptyList()
        );
        register("ARCADE", arcadeDef);
        register("ARCADE_ZOMBIE", arcadeDef);
    }
}
