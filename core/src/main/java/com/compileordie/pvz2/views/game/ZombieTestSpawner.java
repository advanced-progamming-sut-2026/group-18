package com.compileordie.pvz2.views.game;

import com.badlogic.gdx.Gdx;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.zombies.ZombieBuilder;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

import java.util.ArrayList;
import java.util.List;

/**
 * کلاس کمکی جهت تست و اسپاون متوالی تمام زامبی‌های موجود در بازی.
 * این کلاس بدون دستکاری منطق اصلی GameScreen، هر ۳ ثانیه یک زامبی جدید
 * در یکی از ۵ ردیف اسپاون می‌کند.
 */
public class ZombieTestSpawner {

    private float timer = 0f;
    private static final float SPAWN_INTERVAL = 3.0f; // فاصله زمانی ۳ ثانیه

    private int currentZombieIndex = 0;
    private int currentLane = 1;

    private final List<ZombieType> zombieTypes = new ArrayList<>();

    public ZombieTestSpawner() {
        // جمع‌آوری تمام انواع زامبی‌های تعریف‌شده در Enum
        for (ZombieType type : ZombieType.values()) {
            zombieTypes.add(type);
        }
        Gdx.app.log("PVZ-TEST-SPAWNER", " تعداد " + zombieTypes.size() + " نوع زامبی برای تست شناسایی شد.");
    }

    /**
     * این متد باید در هر فریم (مثلاً انتهای render یا advanceSimulation) فراخوانی شود.
     */
    public void update(float delta) {
        if (AppModel.gameSession == null || AppModel.gameSession.gameBoard == null) {
            return;
        }

        if (zombieTypes.isEmpty()) return;

        timer += delta;
        if (timer >= SPAWN_INTERVAL) {
            timer = 0f;
            spawnNextZombie();
        }
    }

    private void spawnNextZombie() {
        ZombieType typeToSpawn = zombieTypes.get(currentZombieIndex);

        // نقطه اسپاون سمت راست نقشه بر حسب متر (مثلاً حدود ۱۰ تا ۱۱ متر)
        float spawnX = 18f;

        // محاسبه ارتفاع ردیف (Y بر حسب متر یا پیکسل بر اساس متغیرهای ساختار بازی شما)
        float spawnY = (currentLane * (float) Constants.Game.TILE_HEIGHT) + Constants.UI.bottomLineMeter;

        try {
            // ساخت زامبی با استفاده از ZombieBuilder مدل
            Zombie zombie = ZombieBuilder.create(typeToSpawn, spawnX, spawnY, currentLane);

            if (zombie != null) {
                // افزودن زامبی به ردیف مربوطه در GameBoard
                AppModel.gameSession.gameBoard.lanes.get(currentLane).zombies.add(zombie);

                Gdx.app.log("PVZ-TEST-SPAWNER", String.format(
                    "🧟 [SPAWN TEST] زامبی نوع <%s> در ردیف %d اسپاون شد. (X: %.1f, Y: %.1f)",
                    typeToSpawn.name(), currentLane, spawnX, spawnY
                ));
            }
        } catch (Exception e) {
            Gdx.app.error("PVZ-TEST-SPAWNER", "❌ خطا در اسپاون زامبی " + typeToSpawn.name() + ": " + e.getMessage());
        }

        // رفتن به زامبی بعدی و چرخاندن ردیف بین ۰ تا ۴
        currentZombieIndex = (currentZombieIndex + 1) % zombieTypes.size();
        currentLane = ((currentLane + 1) % 5);
    }
}
