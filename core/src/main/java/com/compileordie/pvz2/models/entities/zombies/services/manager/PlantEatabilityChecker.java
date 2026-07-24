package com.compileordie.pvz2.models.entities.zombies.services.manager;

import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.plants.Plant;

import java.util.EnumSet;
import java.util.Set;

public class PlantEatabilityChecker {

    // مجموعه گیاهانی که ذاتاً قابل خوردن نیستند (انفجاری، زیرزمینی یا خاص)
    private static final Set<PlantType> NON_EATABLE_TYPES = EnumSet.of(
        // مین‌های زمینی / زیرزمینی
        PlantType.POTATO_MINE,
        PlantType.PRIMAL_POTATO_MINE,

        // گیاهان یک‌بار مصرف و فوری (Instant)
        PlantType.CHERRY_BOMB,
        PlantType.JALAPENO,
        PlantType.DOOM_SHROOM,
        PlantType.ICE_SHROOM,
        PlantType.SQUASH,
        PlantType.GRAPESHOT,
        PlantType.HOT_POTATO,
        PlantType.GOLD_BLOOM,

        // گیاهان زیرآبی و روی قبر
        PlantType.TANGLE_KELP,
        PlantType.GRAVE_BUSTER
    );

    /**
     * بررسی اینکه آیا یک شیء گیاه مشخص در حالت جاری قابل خوردن توسط زامبی هست یا خیر.
     *
     * @param plant شیء گیاه مورد نظر
     * @return true اگر زامبی بتواند آن را بخورد، false در غیر این صورت.
     */
    public static boolean isEatable(Plant plant) {
        // ۱. اگر شیء وجود نداشته باشد یا گیاه مرده باشد
        if (plant == null || plant.isDead()) {
            return false;
        }

        // ۲. بررسی افکت‌های زمان اجرا (Runtime Statuses طبق کلاس Plant شما):
        // الف) اگر توسط زامبی شکارچی (Hunter Zombie) کاملاً یخ زده باشد
        if (plant.isFreezedByHunter()) {
            return false;
        }

        // ب) اگر توسط اختاپوس (Octopus Zombie) قفل شده باشد
        if (plant.shouldBeFreezedByOcto()) {
            return false;
        }

        // ۳. دریافت انام نوع گیاه از روی نام آن (استفاده از متد getByName انام شما)
        PlantType type = PlantType.getByName(plant.getName());
        if (type == null) {
            // اگر نام تطبیق پیدا نکرد، فرض بر قابل خوردن بودن گیاه زنده عادی است
            return true;
        }

        // ۴. بررسی پاور مینت‌ها (Power Mints): هیچ‌کدام قابل خوردن نیستند
        if (isPowerMint(type)) {
            return false;
        }

        // ۵. بررسی لیست غیرقابل خوردن‌ها (انفجاری / زمینی)
        if (NON_EATABLE_TYPES.contains(type)) {
            return false;
        }

        // در غیر این صورت گیاه قابل خوردن است
        return true;
    }

    /**
     * تشخیص پاور مینت‌ها بر اساس پسوند _MINT در انام PlantType
     */
    private static boolean isPowerMint(PlantType type) {
        return type.name().endsWith("_MINT");
    }
}
