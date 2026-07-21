package com.compileordie.pvz2.models.entities.zombies;

import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.boss.GargantuarZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.capable.*;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.*;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.*;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.*;
import com.compileordie.pvz2.models.entities.zombies.variants.vehicle.*;

public class ZombieBuilder {
    private ZombieType type;
    private double x;
    private double y;
    private int row;

    public ZombieBuilder() {
    }

    /**
     * متد کارخانه‌ای استاتیک (Static Factory Method) برای دسترسی سریع‌تر
     */
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

    /**
     * متد اصلی ساخت شیء زامبی با شروط کامل و دقیق کانتستراکتورهای ۲۸ نوع زامبی
     */
    public Zombie build() {
        if (type == null) {
            throw new IllegalStateException();
        }

        double startX = this.x;

        switch (type) {
            // === ۱. زامبی‌های استاندارد (Standard Pack) ===
            case STANDARD:
                return new BasicZombie(190, 0.185, 100, row, startX, x, y, 0.185, 0);

            case CONEHEAD:
                return new ConeHeadZombie(190, 0.185, 100, row, startX, 370, x, y, 0.185, 0);

            case BUCKETHEAD:
                return new BucketHeadZombie(190, 0.185, 100, row, startX, 1100, x, y, 0.185, 0);

            case KNIGHT:
                return new KnightZombie(190, 0.185, 100, row, startX, 1600, 1600, x, y, 0.185, 0);

            case BLOCKHEAD:
                return new BlockheadZombie(190, 0.185, 100, row, startX, 2200, x, y, 0.185, 0);

            case GARGANTUAR:
                return new GargantuarZombie(3600, 0.24, 1500, row, startX, x, y, 0.24, 0);

            case IMP:
                return new ImpZombie(190, 0.22, 100, row, startX, x, y, 0.22, 0);

            case ALL_STAR:
                return new AllStarZombie(1100, 0.16, 150, row, startX, 2.5, x, y, 0.16, 0);

            case ARCADE_ZOMBIE:
                return new ArcadeZombie(490, 0.19, 100, row, startX, x, y, 0.19, 0, 0.05, 1100);

            case PARASOL_ZOMBIE:
                return new ParasolZombie(350, 0.25, 100, row, startX, x, y, 0.25, 0);

            case TURQUOISE_ZOMBIE:
                return new TurquoiseZombie(250, 0.185, 100, row, startX, x, y, 0.185, 0);

            case PROSPECTOR_ZOMBIE:
                return new ProspectorZombie(190, 0.16, 100, row, startX, x, y, 0.16, 0);

            case PIANIST_ZOMBIE:
                return new PianistZombie(840, 0.12, 4000, row, startX, x, y, 0.12, 0);

            case NEWSPAPER_ZOMBIE:
                return new NewspaperZombie(460, 0.15, 80, row, startX, 190, x, y, 0.22, 0);

            case BARREL_ROLLER:
                return new BarrelRollerZombie(190, 0.15, 100, row, startX, x, y, 0.15, 0, 800);

            // === ۲. زامبی‌های دنیای مصر باستان (Ancient Egypt) ===
            case RA_ZOMBIE:
                return new RaZombie(190, 0.2, 100, row, startX, x, y, 0.2, 0);

            case EXPLORER_ZOMBIE:
                return new ExplorerZombie(250, 0.25, 100, row, startX, x, y, 0.25, 0);

            case TOMBRAISER:
                return new TombraiserZombie(380, 0.185, 100, row, startX, x, y, 0.185, 0);

            // === ۳. زامبی‌های غارهای یخی (Frostbite Caves) ===
            case DODO_RIDER:
                return new DodoRiderZombie(490, 0.3, 100, row, startX, x, y, 0.3, 0);

            case HUNTER_ZOMBIE:
                return new HunterZombie(700, 0.12, 100, row, startX, x, y, 0.12, 0);

//            case TROGLOBITE:
//                return new TroglobiteZombie(470, 0.185, 100, row, startX, x, y, 0.185, 0, 0.05, 9999999);

            // === ۴. زامبی‌های ساحل (Big Wave Beach) ===
            case FISHERMAN_ZOMBIE:
                return new FishermanZombie(1000, 0.185, 100, row, startX, x, y, 0.185, 0);

            case SNORKEL_ZOMBIE:
                return new SnorkelZombie(350, 0.185, 100, row, startX, x, y, 0.185, 0);

            case OCTOPUS_ZOMBIE:
                return new OctopusZombie(910, 0.12, 100, row, startX, x, y, 0.12, 0);

            // === ۵. زامبی‌های قرون وسطی (Dark Ages) ===
            case JESTER_ZOMBIE:
                return new JesterZombie(420, 0.2, 100, row, startX, x, y, 0.2, 0);

            case WIZARD_ZOMBIE:
                return new WizardZombie(490, 0.12, 100, row, startX, x, y, 0.12, 0);

            case KING_ZOMBIE:
                return new KingZombie(1000, row, startX, x, y, 3, 1, 5.0);

            case IMP_DRAGON:
                return new ImpDragon(190, 0.185, 100, row, startX, x, y, 0.185, 0);

            default:
                throw new IllegalArgumentException();
        }
    }
}
