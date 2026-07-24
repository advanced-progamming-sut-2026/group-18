package com.compileordie.pvz2.models.entities.zombies;

import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.boss.GargantuarZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.capable.*;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.*;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.*;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.*;
import com.compileordie.pvz2.models.entities.zombies.variants.vehicle.*;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;
import com.compileordie.pvz2.models.repositories.configs.ZombieStatsConfig;

public class ZombieBuilder {
    private ZombieType type;
    private double x;
    private double y;
    private int row;

    public ZombieBuilder() {
    }

    public static Zombie create(ZombieType type, double x, double y, int row) {
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
        if (type == null) {
            throw new IllegalStateException("ZombieType cannot be null in ZombieBuilder!");
        }

        double startX = this.x;
        // گرفتن کانفیگ اختصاصی مربوط به زامبی از ConfigManager
        ZombieStatsConfig stats = ConfigManager.zombies().get(type);

        switch (type) {
            // === ۱. زامبی‌های استاندارد (Standard Pack) ===
            case STANDARD:
                return new BasicZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, 0);

            case CONEHEAD:
                return new ConeHeadZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, stats.armorHp, x, y, stats.speed, 0);

            case BUCKETHEAD:
                return new BucketHeadZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, stats.armorHp, x, y, stats.speed, 0);

            case KNIGHT:
                return new KnightZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, stats.armorHp, stats.armorHp, x, y, stats.speed, 0);

            case BLOCKHEAD:
                return new BlockheadZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, stats.armorHp, x, y, stats.speed, 0);

            case GARGANTUAR:
                return new GargantuarZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, 0);

            case IMP:
                return new ImpZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, 0);

            case ALL_STAR:
                return new AllStarZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, 0);

            case ARCADE_ZOMBIE:
                return new ArcadeZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, 0, 0.05, stats.armorHp);

            case PARASOL_ZOMBIE:
                return new ParasolZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, 0);

            case TURQUOISE_ZOMBIE:
                return new TurquoiseZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, 0);

            case PROSPECTOR_ZOMBIE:
                return new ProspectorZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, 0);

            case PIANIST_ZOMBIE:
                return new PianistZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, 0);

            case NEWSPAPER_ZOMBIE:
                return new NewspaperZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, stats.armorHp, x, y, stats.speed, 0, stats.chargeSpeed, stats.chargeAttack);

            case BARREL_ROLLER:
                return new BarrelRollerZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, 0, stats.armorHp);

            // === ۲. زامبی‌های دنیای مصر باستان (Ancient Egypt) ===
            case RA_ZOMBIE:
                return new RaZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, 0);

            case EXPLORER_ZOMBIE:
                return new ExplorerZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, 0);

            case TOMBRAISER:
                return new TombraiserZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, 0);

            // === ۳. زامبی‌های غارهای یخی (Frostbite Caves) ===
            case DODO_RIDER:
                return new DodoRiderZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, 0);

            case HUNTER_ZOMBIE:
                return new HunterZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, 0);

            // === ۴. زامبی‌های ساحل (Big Wave Beach) ===
            case FISHERMAN_ZOMBIE:
                return new FishermanZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, 0);

            case SNORKEL_ZOMBIE:
                return new SnorkelZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, 0);

            case OCTOPUS_ZOMBIE:
                return new OctopusZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, 0);

            // === ۵. زامبی‌های قرون وسطی (Dark Ages) ===
            case JESTER_ZOMBIE:
                return new JesterZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, 0);

            case WIZARD_ZOMBIE:
                return new WizardZombie(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, 0);

            case KING_ZOMBIE:
                return new KingZombie(stats.hitpoints, row, startX, x, y, 3, 1, 5.0);

            case IMP_DRAGON:
                return new ImpDragon(stats.hitpoints, stats.speed, stats.eatDps, row, startX, x, y, stats.speed, 0);

            default:
                throw new IllegalArgumentException("Unsupported ZombieType: " + type);
        }
    }
}
