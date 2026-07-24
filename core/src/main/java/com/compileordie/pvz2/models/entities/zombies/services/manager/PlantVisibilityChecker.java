package com.compileordie.pvz2.models.entities.zombies.services.manager;

import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.plants.variants.Plant;

import java.util.EnumSet;
import java.util.Set;

public class PlantVisibilityChecker {

    // ۱. لیست گیاهان غیرقابل دیدن (فوری‌ها و وابسته‌ها)
    private static final Set<PlantType> INVISIBLE_TYPES = EnumSet.of(
        PlantType.CHERRY_BOMB,
        PlantType.JALAPENO,
        PlantType.DOOM_SHROOM,
        PlantType.ICE_SHROOM,
        PlantType.GRAPESHOT,
        PlantType.HOT_POTATO,
        PlantType.GOLD_BLOOM,
        PlantType.GRAVE_BUSTER
    );

    // ۲. لیست گیاهان هم‌سطح زمین / زیرزمینی (زامبی از روی آن‌ها عبور می‌کند)
    private static final Set<PlantType> STEP_OVER_TYPES = EnumSet.of(
        PlantType.POTATO_MINE,
        PlantType.PRIMAL_POTATO_MINE,
        PlantType.ICEBERG_LETTUCE,
        PlantType.TANGLE_KELP
    );

    /**
     * بررسی اینکه آیا نوع گیاه اساساً روی زمین جثه تعاملی دارد یا خیر
     */
    public static boolean isVisibleOnBoard(PlantType type) {
        if (type == null) return false;

        // پاور مینت‌ها جثه فیزیکی روی زمین ندارند
        if (isPowerMint(type)) {
            return false;
        }

        // اگر جزء گیاهان فوری یا نامرئی باشد
        if (INVISIBLE_TYPES.contains(type)) {
            return false;
        }

        return true;
    }

    /**
     * بررسی اینکه آیا گیاه هم‌سطح زمین/زیرزمین است (زامبی از روی آن عبور می‌کند)
     */
    public static boolean isStepOverOrUnderground(PlantType type) {
        if (type == null) return false;
        return STEP_OVER_TYPES.contains(type);
    }

    /**
     * بررسی دیده‌شدن شیء واقعی گیاه در زمان اجرای بازی (با احتساب زنده بودن و نوع آن)
     */
    public static boolean isVisible(Plant plant) {
        if (plant == null || plant.isDead()) {
            return false; // گیاه مرده یا وجود نداشته دیده نمی‌شود
        }

        PlantType type = PlantType.getByName(plant.getName());
        return isVisibleOnBoard(type);
    }

    /**
     * تشخیص پاور مینت‌ها از روی نام انام
     */
    private static boolean isPowerMint(PlantType type) {
        return type.name().endsWith("_MINT");
    }
}
