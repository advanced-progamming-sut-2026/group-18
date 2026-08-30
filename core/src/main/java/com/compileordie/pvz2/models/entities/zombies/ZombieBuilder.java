package com.compileordie.pvz2.models.entities.zombies;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.ZomBoss.EgyptZomboss;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.boss.GargantuarZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.capable.*;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.DodoRiderZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.ProspectorZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.SnorkelZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.*;
//import com.compileordie.pvz2.models.entities.zombies.variants.summoner.KingZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.TombraiserZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.vehicle.BarrelRollerZombie;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;
import com.compileordie.pvz2.models.repositories.configs.ZombieStatsConfig;
//
public class ZombieBuilder {
    private ZombieType type;
    private double x;
    private double y;
    private int row;
    public static boolean gargi = false;

    public ZombieBuilder() {
    }

    public static Zombie create(ZombieType type, double x, double y, int row) {
        return new ZombieBuilder()
            .type(type)
            .at(x, y)
            .row(row)
            .build();
    }

    public static Zombie create(ZombieType type, double x, double y, int row, boolean gargii) {
        gargi = gargii;
        return new ZombieBuilder()
            .type(type)
            .at(x, y)
            .row(row)
            .build();
    }

    public ZombieBuilder type(ZombieType type) {
        this.type = type;
        return this;
    }

    public ZombieBuilder at(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public ZombieBuilder row(int row) {
        this.row = row;
        return this;
    }

    public Zombie build() {
        Zombie zombie = construct();

        // =========================================================
        // 🌪️ خودکار، بدون نیاز به این‌که هیچ caller ای صریحا صداش بزنه:
        // اگه این زامبی همون لحظه‌ی ساخته‌شدن، داخل محدوده‌ی دیدنیِ زمین باشه
        // (نه تو ناحیه‌ی اسپاونِ بیرون از صفحه که موج‌های عادی ازش می‌آن، مثلا
        // x=18 که WaveType.placeZombiesRandomly استفاده می‌کنه)، یعنی این
        // زامبی "وسط زمین" ظاهر شده - چه از قبرِ Tombraiser، چه از تست‌اسپاونر،
        // چه از کنسول دیباگ، چه از شکستن یه گلدون تو vasebreaker. تو همه‌ی این
        // حالت‌ها، بدون این‌که هیچ‌کدوم از اون call site ها لازم باشه چیزی صدا
        // بزنن، همینجا ۱.۵ ثانیه گردباد (SANDSTORM_TOP) خودکار شروع می‌شه.
        //
        // ایمپ (IMP) عمدا از این قانون مستثناست: پرتاب ایمپ (توسط غول یا بشکه)
        // یه مکانیزم جدا و از قبل تعریف‌شده تو ZombieManager داره (fly-in +
        // پرچم تستی isImpProved) که نباید این‌جا بی‌سروصدا override بشه.
        // =========================================================
        boolean isWithinLawn = x < Constants.Game.TILE_WIDTH * 9 + Constants.Game.PADDING_X - 1;
        if (isWithinLawn && !gargi) {
            zombie.startSandstormSpawn();
        }

        return zombie;
    }

    private Zombie construct() {
        if (type == null) {
            throw new IllegalStateException("ZombieType cannot be null");
        }

        double startX = this.x;

        // دریافت مشخصات زامبی با استفاده از ساختار صحیح ConfigManager
        ZombieStatsConfig stats = ConfigManager.zombies().get(type);

        if (stats == null) {
            throw new IllegalArgumentException("Stats not found in JSON for type: " + type.name());
        }

        switch (type) {
            // === ۱. زامبی‌های استاندارد (Standard Pack) ===
            case STANDARD:
                return new BasicZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, stats.armorHp);

            case CONEHEAD:
                return new ConeHeadZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, stats.armorHp, x, y, stats.speed, 0);

            case BUCKETHEAD:
                return new BucketHeadZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, stats.armorHp, x, y, stats.speed, 0);

            case KNIGHT:
                return new KnightZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, stats.armorHp, stats.armorHp, x, y, stats.speed, 0);

            case BLOCKHEAD:
                return new BlockheadZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, stats.armorHp, x, y, stats.speed, 0);

            case GARGANTUAR:
                return new GargantuarZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, stats.armorHp);

            case IMP:
                return new ImpZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, stats.armorHp);

            case ALL_STAR:
                return new AllStarZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, 2.5, x, y, stats.speed, stats.armorHp);

            case PARASOL_ZOMBIE:
                return new ParasolZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, stats.armorHp);

            case TURQUOISE_ZOMBIE:
                return new TurquoiseZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, stats.armorHp);

            case PROSPECTOR_ZOMBIE:
                return new ProspectorZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, stats.armorHp);

            case PIANIST_ZOMBIE:
                return new PianistZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, stats.armorHp);

            case NEWSPAPER_ZOMBIE:
                return new NewspaperZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, stats.armorHp, x, y, stats.speed, 0);

            case BARREL_ROLLER:
                return new BarrelRollerZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, 0, stats.armorHp);

            // === ۲. زامبی‌های دنیای مصر باستان (Ancient Egypt) ===
            case RA_ZOMBIE:
                return new RaZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, stats.armorHp);

            case EXPLORER_ZOMBIE:
                return new ExplorerZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, stats.armorHp);

            case TOMBRAISER:
                return new TombraiserZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, stats.armorHp);

            // === ۳. زامبی‌های غارهای یخی (Frostbite Caves) ===
            case DODO_RIDER:
                return new DodoRiderZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, stats.armorHp);

            case HUNTER_ZOMBIE:
                return new HunterZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, stats.armorHp);

            // === ۴. زامبی‌های ساحل (Big Wave Beach) ===
//            case FISHERMAN_ZOMBIE:
//                return new FishermanZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, stats.armorHp);

            case SNORKEL_ZOMBIE:
                return new SnorkelZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, stats.armorHp);

            case OCTOPUS_ZOMBIE:
                return new OctopusZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, stats.armorHp);

            // === ۵. زامبی‌های قرون وسطی (Dark Ages) ===
//            case JESTER_ZOMBIE:
//                return new JesterZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, stats.armorHp);
//
//            case WIZARD_ZOMBIE:
//                return new WizardZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, stats.armorHp);
//
//            case KING_ZOMBIE:
//                return new KingZombie(stats.hitpoints, row, startX, x, y, 3, 1, 5.0);

            case IMP_DRAGON:
                return new ImpDragon(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, stats.armorHp);

            case ZOMBOSS_IN_EGYPT:
                return new EgyptZomboss();

            default:
                ZombieStatsConfig defaultStats = ConfigManager.zombies().get(ZombieType.STANDARD);
                return new BasicZombie(defaultStats.hitpoints, defaultStats.speed, defaultStats.eatDps, row, startX, x, y, defaultStats.speed, defaultStats.armorHp);
        }
    }
}
