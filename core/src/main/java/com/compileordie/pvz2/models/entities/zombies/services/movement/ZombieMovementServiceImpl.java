package com.compileordie.pvz2.models.entities.zombies.services.movement;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.LawnMower;
import com.compileordie.pvz2.models.entities.plants.variants.Plant;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.SnorkelZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.ProspectorZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.DodoRiderZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.ImpZombie;
import com.compileordie.pvz2.models.entities.zombies.services.manager.ZombieTickContext;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.board.Lane;

import java.util.List;

/**
 * پیاده‌سازی کامل و هماهنگ موتور فیزیک و جابه‌جایی زامبی‌ها (ZombieMovementService)
 * منطبق با ساختارهای واقعی LawnMower، Zombie و فرزندان آنها بدون متدهای تعریف‌نشده.
 */
public class ZombieMovementServiceImpl implements ZombieMovementService {

    /**
     * پردازش حرکت تمام زامبی‌های زنده بر اساس وضعیت جاری، سرعت و جهت در هر فریم بازی.
     */
    @Override
    public void moveAll(ZombieTickContext context) {
        double dt = context.getDelta();

        // تبدیل دلتا تایم به تیک منطقی بازی جهت هماهنگی با متد move(ticks) زامبی‌ها
        int ticks = (int) Math.max(1, dt / Constants.Game.TIME_COEFFICIENT);

        for (Zombie zombie : context.getActiveZombies()) {

            // ۱. بررسی زنده بودن و امکان حرکت زامبی بر اساس کدهای کلاس Zombie
            if (zombie.isDead() || zombie.getCurrentSpeed() <= 0) {
                continue;
            }

            // ۲. پردازش رفتارهای حرکتی خاص هر نوع زامبی
            if (zombie instanceof SnorkelZombie) {
                handleSnorkelDivingLogic(zombie, context);
            } else if (zombie instanceof ProspectorZombie) {
                handleMinerReverseWalk(zombie, context);
            } else if (zombie instanceof DodoRiderZombie) {
                handleFlyerOverObstacles(zombie, context);
            } else if (zombie instanceof ImpZombie && isThrownImp(zombie)) {
                // اگر ایمپ در حال پرواز سهمی‌شکل است، جابه‌جایی آن توسط کلاس فیزیک کنترل می‌شود
                handleThrownImpTrajectory(zombie, context);
                continue; // جلوگیری از اجرای متد move() افقی زامبی در این فریم
            }

            // ۳. بررسی برخورد با گیاهان (توقف برای جویدن)
            boolean isCollidingWithPlant = checkPlantCollision(zombie, context.getActivePlants());

            // ۴. اگر در حال خوردن گیاه نباشد، حرکت فیزیکی افقی انجام می‌شود
            if (!isCollidingWithPlant) {
                zombie.move(ticks);
            }
        }

        // ۵. بررسی وضعیت نفوذ زامبی‌ها به انتهای زمین و فعال‌سازی یا نبود چمن‌زن‌ها
        checkLawnMowersAndBreach(context);
    }

    /**
     * مدیریت شیرجه رفتن و روی آب آمدن زامبی غواص (SnorkelZombie).
     */
    @Override
    public void handleSnorkelDivingLogic(Zombie snorkelZombie, ZombieTickContext context) {
        SnorkelZombie snorkel = (SnorkelZombie) snorkelZombie;

        // طبق قوانین: اگر گیاهی مستقیم جلویش باشد، روی آب می‌آید و در غیر این صورت زیر آب می‌رود.
        boolean hasPlantAhead = checkPlantCollision(snorkelZombie, context.getActivePlants());

        if (hasPlantAhead) {
            snorkel.surface();
        } else {
            snorkel.dive();
        }
    }

    /**
     * مدیریت وضعیت پرواز زامبی دودوسوار (DodoRiderZombie).
     */
    @Override
    public void handleFlyerOverObstacles(Zombie flyerZombie, ZombieTickContext context) {
        // منطق شمارش زمان پرواز و تغییر حالت در متد tick() داخلی کلاس DodoRiderZombie مدیریت می‌شود.
    }

    /**
     * مدیریت تایمر انفجار و حرکت معکوس زامبی معدن‌چی (ProspectorZombie).
     */
    @Override
    public void handleMinerReverseWalk(Zombie minerZombie, ZombieTickContext context) {
        // شمارش معکوس دینامیت (۱۰ ثانیه)، پرتاب و تغییر جهت به راست
        // در متدهای tick() و move() داخلی خود کلاس ProspectorZombie پیاده‌سازی شده است.
    }

    /**
     * جابه‌جایی نرم و عمودی زامبی‌ها بین لاین‌ها تحت تأثیر زامبی پیانیست.
     */
    @Override
    public void handleLaneSwitchTransition(Zombie zombie, int targetRow, ZombieTickContext context) {
        if (zombie.getCurrentRow() == targetRow) return;

        GameBoard board = context.getGameMap();
        double currentY = zombie.getY();
        double targetY = targetRow * Constants.Game.TILE_SIZE;
        double speedY = 50.0; // سرعت انتقال نرم عمودی

        if (Math.abs(currentY - targetY) > 2.0) {
            if (currentY < targetY) {
                zombie.setY(currentY + speedY * context.getDelta());
            } else {
                zombie.setY(currentY - speedY * context.getDelta());
            }
        } else {
            // آپدیت کردن لیست‌های منطقی GameBoard پس از جابه‌جایی کامل
            int oldRow = zombie.getCurrentRow();
            if (oldRow >= 0 && oldRow < board.totalRows && targetRow >= 0 && targetRow < board.totalRows) {
                board.getLane(oldRow).zombies.remove(zombie);
                zombie.setCurrentRow(targetRow);
                board.getLane(targetRow).zombies.add(zombie);
            }
        }
    }

    /**
     * اعمال محاسبات فیزیکی پرواز سهمی‌شکل زامبی ایمپ.
     */
    @Override
    public void handleThrownImpTrajectory(Zombie impZombie, ZombieTickContext context) {
        ImpZombie imp = (ImpZombie) impZombie;
        ImpTrajectoryPhysics.updateTrajectory(imp, context.getDelta());
    }

    /**
     * اعمال نیروی Knockback (عقب‌رانی) ناشی از باد کلاور یا انفجارها به سمت راست نقشه.
     */
    @Override
    public void applyKnockbackForce(Zombie zombie, double distance, ZombieTickContext context) {
        double newX = zombie.getX() + distance;

        double maxWidth = context.getGameMap().totalCols * Constants.Game.TILE_SIZE;
        if (newX > maxWidth) {
            newX = maxWidth;
        }
        zombie.setX(newX);
    }

    /**
     * مدیریت برخورد با چمن‌زن و اعلام باخت بازی در صورت عبور زامبی.
     */
    @Override
    public void checkLawnMowersAndBreach(ZombieTickContext context) {
        GameBoard board = context.getGameMap();

        for (Lane lane : board.lanes) {
            LawnMower mower = lane.lawnMower;

            // اگر چمن‌زن وجود نداشته باشد (یا استفاده و نابود شده باشد)،
            // عبور زامبی‌ها از ستون صفر به معنی باخت بازی است.
            if (mower == null) {
                for (Zombie zombie : lane.zombies) {
                    if (!zombie.isDead() && zombie.getX() <= 0) {
                        context.triggerGameOver();
                        return;
                    }
                }
            }
        }
    }

    /**
     * بررسی برخورد زامبی با گیاهان هم‌ردیف جهت تریگر کردن گاز زدن.
     */
    private boolean checkPlantCollision(Zombie zombie, List<Plant> activePlants) {
        for (Plant plant : activePlants) {
            // هماهنگی ۱۰۰٪ با متد getCurrentHp() در کلاس اصلی Plant شما
            if (plant.getRow() == zombie.getCurrentRow() && plant.getCurrentHp() > 0) {
                double distance = zombie.getX() - plant.getX();
                if (distance > 0 && distance <= Constants.Game.TILE_SIZE * 0.45) {
                    zombie.startEating();
                    return true;
                }
            }
        }
        zombie.stopEating();
        return false;
    }

    /**
     * بررسی اینکه آیا ایمپ در وضعیت پرواز فضایی قرار دارد یا خیر.
     */
    private boolean isThrownImp(Zombie zombie) {
        if (zombie instanceof ImpZombie) {
            return ((ImpZombie) zombie).isCurrentlyAirborne();
        }
        return false;
    }
}
