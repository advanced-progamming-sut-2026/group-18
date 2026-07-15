package com.compileordie.pvz2.models.entities.zombies.services.combat;

import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.plants.variants.Plant;
import com.compileordie.pvz2.models.entities.projectiles.Projectile;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.boss.GargantuarZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.SnorkelZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.MovementState;
import com.compileordie.pvz2.models.entities.zombies.types.DamageType;
import com.compileordie.pvz2.models.entities.zombies.services.manager.ZombieTickContext;

import java.util.ArrayList;
import java.util.List;

public class ZombieCombatServiceImpl implements ZombieCombatService {

    // برای کنترل نرخ آسیب زامبی‌ها (مثلاً اعمال دمیج جویدن در فواصل مشخص در ثانیه)
    private double attackCooldownTimer = 0.0;
    private static final double ATTACK_INTERVAL = 0.5; // جویدن هر نیم ثانیه یکبار

    /**
     * مرحله ۵ چرخه بازی: مکانیک ژانگولر برای بررسی و معکوس کردن مسیر تیرها قبل از محاسبات برخورد نهایی
     */
    @Override
    public void reflectProjectiles(Zombie zombie, ZombieTickContext context) {
        if (zombie.isDead()) return;

        // بررسی اینکه آیا زامبی ژانگولر (Jester / Juggler) است
        String name = zombie.getType().name();
        if (name.equalsIgnoreCase("JESTER") || name.equalsIgnoreCase("JUGGLER")) {

            for (Projectile proj : context.getActiveProjectiles()) {
                if (proj.isDead() || proj.isReversed() || proj.getRow() != zombie.getCurrentRow()) {
                    continue;
                }

                // فاصله تشخیص برای شروع چرخش و بازتاب (مثلاً در فاصله ۱.۵ کاشی)
                double distance = Math.abs(proj.getX() - zombie.getX());
                if (distance <= Constants.Game.TILE_SIZE * 1.5) {

                    // تیرهای مستقیم (NORMAL) یا یخی بازتاب داده می‌شوند
                    if (proj.getType() == DamageType.NORMAL || proj.getType() == DamageType.NORMAL || proj.getType() == DamageType.ICE) {
                        proj.setReversed(true);
                        proj.setXSpeed(-Math.abs(proj.getXSpeed())); // معکوس کردن جهت حرکت به سمت چپ (گیاهان)
                        zombie.startSpinning(); // تریگر کردن حالت انیمیشن چرخش درون کلاس زامبی
                    }
                }
            }
        }
    }

    /**
     * مرحله ۸ چرخه بازی: پردازش درگیری‌های نزدیک (جویدن گیاهان، ضربه پتک غول‌ها، آسیب دیدن زامبی از تیرها)
     */
    @Override
    public void processCombat(ZombieTickContext context) {
        double dt = context.getDelta();
        attackCooldownTimer += dt;

        boolean isAttackTick = false;
        if (attackCooldownTimer >= ATTACK_INTERVAL) {
            isAttackTick = true;
            attackCooldownTimer = 0.0;
        }

        // ۱. پردازش حمله زامبی‌ها به گیاهان
        for (Zombie zombie : context.getActiveZombies()) {
            if (zombie.isDead()) continue;

            // اگر زامبی در حال خوردن گیاه است (یا زامبی غول‌پیکر به گیاه رسیده است)
            if (zombie.isEating() || zombie instanceof GargantuarZombie) {
                if (isAttackTick || zombie instanceof GargantuarZombie) {
                    handleZombieAttacking(zombie, context);
                }
            }
        }

        // ۲. پردازش دمیج خوردن زامبی‌ها بر اثر برخورد فیزیکی پرتابه‌ها
        handleProjectileDamage(context);
    }

    /**
     * مدیریت دمیج گاز زدن یا کوبیدن گیاه جلوی زامبی
     */
    private void handleZombieAttacking(Zombie zombie, ZombieTickContext context) {
        Plant targetPlant = null;

        // پیدا کردن گیاه هم‌سطری که دقیقاً جلوی دهان زامبی قرار دارد
        for (Plant plant : context.getActivePlants()) {
            if (plant.getRow() == zombie.getCurrentRow() && plant.getCurrentHp() > 0) {
                double distance = zombie.getX() - plant.getX();
                if (distance > 0 && distance <= Constants.Game.TILE_SIZE * 0.5) {
                    targetPlant = plant;
                    break;
                }
            }
        }

        if (targetPlant == null) {
            zombie.stopEating();
            return;
        }

        // منطق ضربه کوبنده زامبی غول‌پیکر (Gargantuar): نابودی آنی گیاه
        if (zombie instanceof GargantuarZombie) {
            targetPlant.setCurrentHp(0);
            targetPlant.die();
            zombie.stopEating();
            return;
        }

        // منطق گاز زدن زامبی‌های معمولی: کسر دمیج از گیاه
        int damage = zombie.getAttackPower();
        int remainingHp = targetPlant.getCurrentHp() - damage;
        if (remainingHp <= 0) {
            targetPlant.setCurrentHp(0);
            targetPlant.die();
            zombie.stopEating(); // مسیر آزاد شد؛ زامبی دوباره راه می‌افتد
        } else {
            targetPlant.setCurrentHp(remainingHp);
        }
    }

    /**
     * پردازش برخورد فیزیکی پرتابه‌ها به زامبی‌ها و اعمال آسیب با توجه به ویژگی‌های خاص (مثل زامبی غواص)
     */
    private void handleProjectileDamage(ZombieTickContext context) {
        List<Projectile> projectiles = context.getActiveProjectiles();
        List<Zombie> zombies = context.getActiveZombies();
        List<Projectile> spentProjectiles = new ArrayList<>();

        for (Projectile proj : projectiles) {
            // اگر تیر قبلاً مرده یا برگشت داده شده باشد نادیده گرفته می‌شود
            if (proj.isDead() || proj.isReversed()) {
                continue;
            }

            for (Zombie zombie : zombies) {
                if (zombie.isDead() || proj.getRow() != zombie.getCurrentRow()) {
                    continue;
                }

                double distance = Math.abs(proj.getX() - zombie.getX());

                // آستانه برخورد معین (در محدوده نیم‌کاشی)
                if (distance <= Constants.Game.TILE_SIZE * 0.4) {

                    // منطق اختصاصی زامبی غواص (SnorkelZombie):
                    // وقتی زیر آب است فقط به دمیج لوببر (Lobber) و انفجاری (Explosive) پاسخ می‌دهد.
                    if (zombie instanceof SnorkelZombie) {
                        SnorkelZombie snorkel = (SnorkelZombie) zombie;
                        if (snorkel.getMovementState() == MovementState.UNDERWATER) {
                            if (proj.getType() != DamageType.LOBBER && proj.getType() != DamageType.EXPLOSIVE) {
                                continue; // نادیده گرفتن برخورد تیر مستقیم روی سر غواص
                            }
                        }
                    }

                    // اعمال آسیب فیزیکی به زامبی
                    zombie.takeDamage(proj.getDamage(), proj.getType());

                    // حذف فیزیکی تیر پس از اصابت
                    proj.destroy();
                    spentProjectiles.add(proj);
                    break; // هر تیر فقط به یک زامبی برخورد می‌کند
                }
            }
        }

        // پاکسازی تیرهای برخورد کرده از لیست اصلی
        projectiles.removeAll(spentProjectiles);
    }
}
