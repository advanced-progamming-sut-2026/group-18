package com.compileordie.pvz2.views.game;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.game.levels.ChapterType;

import java.util.*;

public class ZombieVisualRegistry {

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

        public String getResolvedPath() {
            String chTag = getChapterTag();
            return pathTemplate.replace("{CH}", chTag);
        }
    }

    public static class ZombieVisualDef {
        public final List<PamSpec> pams;
        public final List<String> extraStateFilterTemplates;

        public ZombieVisualDef(List<PamSpec> pams, List<String> extraStateFilterTemplates) {
            this.pams = pams;
            this.extraStateFilterTemplates = extraStateFilterTemplates;
        }

        public List<String> getResolvedStateFilters() {
            String chTag = getChapterTag();
            List<String> resolved = new ArrayList<>();
            for (String tmpl : extraStateFilterTemplates) {
                resolved.add(tmpl.replace("{CH}", chTag).replace("{ch}", chTag.toLowerCase()));
            }
            return resolved;
        }
    }

    private static final Map<String, ZombieVisualDef> REGISTRY = new HashMap<>();

    public static void register(String zombieTypeName, ZombieVisualDef def) {
        REGISTRY.put(zombieTypeName.toUpperCase(), def);
    }

    public static ZombieVisualDef get(String zombieTypeName) {
        if (zombieTypeName == null) return null;
        String upper = zombieTypeName.toUpperCase();
        ZombieVisualDef def = REGISTRY.get(upper);

        // اگر کلمه _ZOMBIE در انتهای نام نبود یا بود، هر دو حالت را چک می‌کند
        if (def == null) {
            if (upper.endsWith("_ZOMBIE")) {
                // نکته: قبلا اینجا از String.replace("_ZOMBIE", "") استفاده می‌شد که همه‌ی
                // رخدادهای "_ZOMBIE" رو (نه فقط انتهای رشته) حذف می‌کرد. اگه یه نوع زامبی
                // اسمش وسط رشته هم شامل "_ZOMBIE" می‌بود (مثلا فرضی: "IMP_ZOMBIE_DRAGON")
                // اشتباه پاک می‌شد. الان فقط پسوند انتهایی substring می‌شه.
                String base = upper.substring(0, upper.length() - "_ZOMBIE".length());
                def = REGISTRY.get(base);
            } else {
                def = REGISTRY.get(upper + "_ZOMBIE");
            }
        }
        return def;
    }

    /**
     * تبدیل چپتر جاری به تگ متناسب برای آدرس‌دهی Assetها
     */
    public static String getChapterTag() {
        if (AppModel.currentChapter == null) {
            return "EGYPT"; // برای مینی‌گیم‌ها یا پیش‌فرض
        }
        return switch (AppModel.currentChapter) {
            case ANCIENT_EGYPT -> "EGYPT";
            case BIG_WAVE_BEACH -> "BEACH";
            case DARK_AGES -> "DARK";
            case FROSTBITE_CAVES -> "ICEAGE";
            default -> "EGYPT";
        };
    }

    static {
        // ۱. زامبی استاندارد / معمولی
        ZombieVisualDef standardDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/ZOMBIE/ZOMBIE_{CH}_BASIC/ZOMBIE_{CH}_BASIC.PAM")),
            List.of()
        );
        register("STANDARD", standardDef);
        register("BASIC", standardDef);
        register("STANDARD_ZOMBIE", standardDef);

        // ۲. سر مخروطی (Conehead)
        // نکته‌ی مهم (طبق README کتابخانه): فقط ست کردن state-toggle کافی نیست؛
        // چون آن فقط گروه armor را از حالت hidden پیش‌فرض خارج می‌کند، ولی خودِ
        // مشِ زره یک part جدا با اسم مخصوص خودش است که باید جداگانه true شود
        // (دقیقا مثل مثال buckethead در README: هم armor2_states و هم
        // zombie_armor_bucket_norm با هم لازم بودند). تا قبل از این تنها کلید
        // toggle ثبت شده بود و مش زره هیچ‌وقت واقعا نمایان نمی‌شد.
        ZombieVisualDef coneheadDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/ZOMBIE/ZOMBIE_{CH}_BASIC/ZOMBIE_{CH}_BASIC.PAM")),
            List.of("_zombie_{ch}_armor1_states", "zombie_armor_cone_norm")
        );
        register("CONEHEAD", coneheadDef);
        register("CONEHEAD_ZOMBIE", coneheadDef);

        // ۳. سر سطلی (Buckethead)
        ZombieVisualDef bucketheadDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/ZOMBIE/ZOMBIE_{CH}_BASIC/ZOMBIE_{CH}_BASIC.PAM")),
            List.of("_zombie_{ch}_armor2_states", "zombie_armor_bucket_norm")
        );
        register("BUCKETHEAD", bucketheadDef);
        register("BUCKETHEAD_ZOMBIE", bucketheadDef);

        // ۴. سر بلوکی (Blockhead)
        // این یکی از اول درست کار می‌کرد چون کلید ثبت‌شده‌اش، خودِ اسم واقعی
        // مشِ زره است (نه یک state-toggle واسط)؛ برای همین به کلید دومی نیاز نداشت.
        ZombieVisualDef blockheadDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/ZOMBIE/ZOMBIE_{CH}_BASIC/ZOMBIE_{CH}_BASIC.PAM")),
            List.of("zombie_armor_brick_norm")
        );
        register("BLOCKHEAD", blockheadDef);
        register("BLOCKHEAD_ZOMBIE", blockheadDef);

        // ۵. شوالیه (Knight)
        // به همین ترتیب، کلید toggle تاج (crown_states) به‌تنهایی کافی نیست؛
        // مشِ واقعی تاج هم باید جدا فعال شود. "zombie_shoulder_armor" خودش
        // احتمالا از قبل اسم مستقیم part شانه است (مثل blockhead) پس دست
        // نخورده باقی می‌ماند.
        ZombieVisualDef knightDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/ZOMBIE/ZOMBIE_{CH}_BASIC/ZOMBIE_{CH}_BASIC.PAM")),
            List.of("_zombie_armor_crown_states", "zombie_armor_crown_norm", "zombie_shoulder_armor")
        );
        register("KNIGHT", knightDef);
        register("KNIGHT_ZOMBIE", knightDef);

        // ۶. را زامبی (Ra Zombie)
        ZombieVisualDef raDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/ZOMBIE/ZOMBIE_{CH}_RA/ZOMBIE_{CH}_RA.PAM")),
            List.of()
        );
        register("RA", raDef);
        register("RA_ZOMBIE", raDef);

        // ۷. غول (Gargantuar)
        ZombieVisualDef gargDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/ZOMBIE/{CH}_GARGANTUAR/{CH}_GARGANTUAR.PAM")),
            List.of()
        );
        register("GARGANTUAR", gargDef);
        register("GARGANTUAR_ZOMBIE", gargDef);

        // ۸. ایمپ (Imp)
        ZombieVisualDef impDef = new ZombieVisualDef(
            List.of(new PamSpec("768/FULL/ZOMBIE/ZOMBIE_DARK_IMP_MONK/ZOMBIE_DARK_IMP_MONK.PAM")),
            List.of()
        );
        register("IMP", impDef);
        register("IMP_ZOMBIE", impDef);

        // ۹. آل استار (All-Star)
        ZombieVisualDef allstarDef = new ZombieVisualDef(
            List.of(new PamSpec("768/FULL/ZOMBIE/ZOMBIE_MODERN_ALLSTAR/ZOMBIE_MODERN_ALLSTAR.PAM")),
            List.of()
        );
        register("ALLSTAR", allstarDef);
        register("ALL_STAR", allstarDef);
        register("ALL_STAR_ZOMBIE", allstarDef);
        // ۱۰. پارازول (Parasol)
        ZombieVisualDef parasolDef = new ZombieVisualDef(
            List.of(new PamSpec("768/FULL/ZOMBIE/ZOMBIE_LOSTCITY_JANE/ZOMBIE_LOSTCITY_JANE.PAM")),
            List.of()
        );
        register("PARASOL", parasolDef);
        register("PARASOL_ZOMBIE", parasolDef);

        // ۱۱. تورکویز / جمجمه کریستالی (Turquoise)
        ZombieVisualDef turquoiseDef = new ZombieVisualDef(
            List.of(new PamSpec("768/FULL/ZOMBIE/ZOMBIE_LOSTCITY_CRYSTALSKULL/ZOMBIE_LOSTCITY_CRYSTALSKULL.PAM")),
            List.of()
        );
        register("TURQUOISE", turquoiseDef);
        register("TURQUOISE_ZOMBIE", turquoiseDef);

        // ۱۲. روزنامه (Newspaper)
        ZombieVisualDef newspaperDef = new ZombieVisualDef(
            List.of(new PamSpec("768/FULL/ZOMBIE/ZOMBIE_MODERN_NEWSPAPER/ZOMBIE_MODERN_NEWSPAPER.PAM")),
            List.of()
        );
        register("NEWSPAPER", newspaperDef);
        register("NEWSPAPER_ZOMBIE", newspaperDef);

        // ۱۳. اکتشاف‌گر / کاوشگر (Prospector)
        ZombieVisualDef prospectorDef = new ZombieVisualDef(
            List.of(new PamSpec("768/FULL/ZOMBIE/ZOMBIE_PROSPECTOR/ZOMBIE_PROSPECTOR.PAM")),
            List.of()
        );
        register("PROSPECTOR", prospectorDef);
        register("PROSPECTOR_ZOMBIE", prospectorDef);

        // ۱۴. پیانو زامبی (Pianist + Piano)
        ZombieVisualDef pianistDef = new ZombieVisualDef(
            List.of(
                new PamSpec("768/FULL/ZOMBIE/PIANO/PIANO.PAM"),
                new PamSpec("768/FULL/ZOMBIE/ZOMBIE_PIANO/ZOMBIE_PIANO.PAM")
            ),
            List.of()
        );
        register("PIANIST", pianistDef);
        register("PIANIST_ZOMBIE", pianistDef);
        register("PIANO", pianistDef);

        // ۱۵. بشکه‌ران (Barrel Roller / Pusher)
        ZombieVisualDef barrelDef = new ZombieVisualDef(
            List.of(new PamSpec("768/FULL/ZOMBIE/ZOMBIE_PIRATE_BARREL_PUSHER/ZOMBIE_PIRATE_BARREL_PUSHER.PAM")),
            List.of()
        );
        register("BARREL_PUSHER", barrelDef);
        register("BARREL_ROLLER", barrelDef);
        register("BARREL_ZOMBIE", barrelDef);

        // ۱۶. مشعل‌دار / اکتشافی (Explorer / Torchlight)
        ZombieVisualDef explorerDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/ZOMBIE/ZOMBIE_EXPLORER/ZOMBIE_EXPLORER.PAM")),
            List.of()
        );
        register("EXPLORER", explorerDef);
        register("EXPLORER_ZOMBIE", explorerDef);
        register("TORCHLIGHT", explorerDef);

        // ۱۷. قبرساز (Tombraiser)
        ZombieVisualDef tombraiserDef = new ZombieVisualDef(
            List.of(new PamSpec("768/INITIAL/ZOMBIE/ZOMBIE_{CH}_TOMBRAISER/ZOMBIE_{CH}_TOMBRAISER.PAM")),
            List.of()
        );
        register("TOMBRAISER", tombraiserDef);
        register("TOMB_RAISER", tombraiserDef);
        register("TOMBRAISER_ZOMBIE", tombraiserDef);

        // ۱۸. دودو رایدر (Dodo Rider)
        ZombieVisualDef dodoDef = new ZombieVisualDef(
            List.of(new PamSpec("768/FULL/ZOMBIE/ZOMBIE_ICEAGE_DODORIDER/ZOMBIE_ICEAGE_DODORIDER.PAM")),
            List.of()
        );
        register("DODORIDER", dodoDef);
        register("DODO_RIDER", dodoDef);
        register("DODORIDER_ZOMBIE", dodoDef);

        // ۱۹. شکارچی (Hunter)
        ZombieVisualDef hunterDef = new ZombieVisualDef(
            List.of(new PamSpec("768/FULL/ZOMBIE/ZOMBIE_ICEAGE_HUNTER/ZOMBIE_ICEAGE_HUNTER.PAM")),
            List.of()
        );
        register("HUNTER", hunterDef);
        register("HUNTER_ZOMBIE", hunterDef);

        // ۲۰. غواص (Snorkeler / Diver)
        ZombieVisualDef snorkelerDef = new ZombieVisualDef(
            List.of(new PamSpec("768/FULL/ZOMBIE/ZOMBIE_BEACH_SNORKELER/ZOMBIE_BEACH_SNORKELER.PAM")),
            List.of()
        );
        register("SNORKELER", snorkelerDef);
        register("SNORKEL_ZOMBIE", snorkelerDef);
        register("DIVER", snorkelerDef);

        // ۲۱. اختاپوس (Octopus)
        ZombieVisualDef octopusDef = new ZombieVisualDef(
            List.of(new PamSpec("768/FULL/ZOMBIE/ZOMBIE_BEACH_OCTOPUS/ZOMBIE_BEACH_OCTOPUS.PAM")),
            List.of()
        );
        register("OCTOPUS", octopusDef);
        register("OCTOPUS_ZOMBIE", octopusDef);

        // ۲۲. ایمپ اژدها (Imp Dragon)
        ZombieVisualDef impDragonDef = new ZombieVisualDef(
            List.of(new PamSpec("768/FULL/ZOMBIE/ZOMBIE_DARK_IMP_DRAGON/ZOMBIE_DARK_IMP_DRAGON.PAM")),
            List.of()
        );
        register("IMP_DRAGON", impDragonDef);
        register("IMP_DRAGON_ZOMBIE", impDragonDef);

        // ۲۳. ماهیگیر (Fisherman)
        ZombieVisualDef fishermanDef = new ZombieVisualDef(
            List.of(new PamSpec("768/FULL/ZOMBIE/ZOMBIE_BEACH_FISHERMAN/ZOMBIE_BEACH_FISHERMAN.PAM")),
            List.of()
        );
        register("FISHERMAN", fishermanDef);
        register("FISHERMAN_ZOMBIE", fishermanDef);
    }
}
