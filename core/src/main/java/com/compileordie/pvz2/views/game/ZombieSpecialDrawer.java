package com.compileordie.pvz2.views.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import pvz.libpvz.pam.PamPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Sandstorm spawn, fly-in, and unrenderable logging (extracted from ZombieDrawer).
 */
final class ZombieSpecialDrawer {

    private static final String SANDSTORM_SPAWN_PAM =
        "768/INITIAL/EFFECTS/SANDSTORM_TOP/SANDSTORM_TOP.PAM";
    private static final String SANDSTORM_SPAWN_CLIP = "loop";
    private static final float IMP_THROW_ARC_HEIGHT_PX = 140f;

    private final Set<String> brokenAssets;
    private float unrenderableSummaryTimer = 0f;

    ZombieSpecialDrawer(Set<String> brokenAssets) {
        this.brokenAssets = brokenAssets;
    }

    void logUnrenderableBoardSummary(float delta, List<Zombie> currentZombies) {
        unrenderableSummaryTimer += delta;
        if (unrenderableSummaryTimer < 3f) return;
        unrenderableSummaryTimer = 0f;
        Map<String, Integer> missingCounts = new HashMap<>();
        for (Zombie zombie : currentZombies) {
            String typeKey = zombie.getType().name();
            if (!ZombieVisualRegistry.isAvailable(typeKey)) {
                missingCounts.merge(typeKey, 1, Integer::sum);
            }
        }
        if (missingCounts.isEmpty()) return;
        StringBuilder sb = new StringBuilder();
        sb.append("\n================ 📋 خلاصه‌ی زامبی‌های اسپاون‌شده-ولی-رندرنشده (چپتر: ")
            .append(ZombieVisualRegistry.getChapterTag()).append(") ================\n");
        for (Map.Entry<String, Integer> e : missingCounts.entrySet()) {
            sb.append("  - ").append(e.getKey()).append(" : ").append(e.getValue())
                .append(" عدد روی زمین (بدون رندر)\n");
        }
        sb.append("جزئیات هر مسیر asset مورد نیاز رو تو لاگ‌های 'PVZ-ZOMBIE-SPAWNED-NOT-RENDERED' بالاتر ببین.\n");
        sb.append("=================================================================");
        Gdx.app.error("PVZ-ZOMBIE-SPAWNED-NOT-RENDERED-SUMMARY", sb.toString());
    }

    void logZombieNotRenderable(String typeKey, ZombieVisualRegistry.ZombieVisualDef def) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n################################################################\n");
        sb.append("⛔⛔⛔ زامبی اسپاون شده ولی قابل رندر نیست! ⛔⛔⛔\n");
        sb.append("نوع زامبی (typeKey) : ").append(typeKey).append("\n");
        sb.append("چپتر فعلی           : ").append(ZombieVisualRegistry.getChapterTag()).append("\n");
        if (def == null) {
            sb.append("علت                 : این تایپ زامبی اصلا در ZombieVisualRegistry ثبت (register) نشده!\n");
            sb.append("راه‌حل               : یک ZombieVisualDef جدید برای '" + typeKey
                + "' به ZombieVisualRegistry اضافه کن.\n");
        } else if (def.pams.isEmpty()) {
            sb.append("علت                 : def ثبت شده ولی لیست pams خالیه (هیچ فایلی تعریف نشده).\n");
        } else {
            sb.append("فایل‌های PAM مورد نیاز این زامبی (نسبت به pvz-assets/IMAGES/):\n");
            int i = 1;
            for (ZombieVisualRegistry.PamSpec pam : def.pams) {
                String resolved = pam.getResolvedPath();
                boolean exists = pam.existsOnDisk();
                sb.append("  ").append(i++).append(") ")
                    .append(exists ? "✅ موجوده" : "❌ پیدا نشد")
                    .append("  ->  ").append(resolved).append("\n");
            }
            sb.append("راه‌حل               : فایل(های) '❌ پیدا نشد' رو یا به pvz-assets اضافه کن، یا مسیر\n");
            sb.append("                       ثبت‌شده تو ZombieVisualRegistry برای '" + typeKey
                + "' رو با اسم واقعی فایل تطبیق بده.\n");
        }
        sb.append("نتیجه فعلی          : این زامبی به‌طور کامل رد می‌شه (نه رندر می‌شه، نه کرش می‌کنه).\n");
        sb.append("################################################################");
        Gdx.app.error("PVZ-ZOMBIE-SPAWNED-NOT-RENDERED", sb.toString());
    }

    void drawSandstormSpawningZombie(SpriteBatch batch, PamPlayer player, Zombie zombie,
                                     GameRenderStates.ZombieRenderState state, float delta) {
        if (brokenAssets.contains(SANDSTORM_SPAWN_PAM)) return;
        boolean existsOnDisk = Gdx.files.internal("pvz-assets/IMAGES/" + SANDSTORM_SPAWN_PAM).exists();
        if (!existsOnDisk) {
            brokenAssets.add(SANDSTORM_SPAWN_PAM);
            Gdx.app.error("PVZ-SANDSTORM-ASSET-MISSING",
                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n"
                    + "⛔ فایل افکت گردباد (SANDSTORM_TOP) روی دیسک پیدا نشد.\n"
                    + "مسیر جستجوشده : pvz-assets/IMAGES/" + SANDSTORM_SPAWN_PAM + "\n"
                    + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            return;
        }
        state.animTime += delta;
        float baseX = ((float) zombie.getX() + 0.2f) * Constants.UI.METER_TO_PIX;
        float baseY = ((float) zombie.getY() + 0.2f) * Constants.UI.METER_TO_PIX;
        try {
            player.draw(batch, SANDSTORM_SPAWN_PAM, SANDSTORM_SPAWN_CLIP, state.animTime,
                baseX, baseY, 0.6f, 0.6f, true);
        } catch (Throwable e) {
            brokenAssets.add(SANDSTORM_SPAWN_PAM);
            Gdx.app.error("PVZ-ASSET-MISSING",
                "❌ رندر افکت SANDSTORM_TOP ناموفق بود. مسیر: " + SANDSTORM_SPAWN_PAM
                    + " | کلیپ: " + SANDSTORM_SPAWN_CLIP + " دلیل: " + e, e);
        }
    }

    void drawFlyingInZombie(SpriteBatch batch, PamPlayer player, Zombie zombie,
                            GameRenderStates.ZombieRenderState state, float delta,
                            ZombieVisualRegistry.ZombieVisualDef def) {
        String typeKey = zombie.getType().name();
        if (!ZombieVisualRegistry.isAvailable(typeKey)) return;

        float progress = zombie.getFlyInProgress();
        float originX = (float) zombie.getThrowOriginX() * Constants.UI.METER_TO_PIX;
        float originY = (float) zombie.getThrowOriginY() * Constants.UI.METER_TO_PIX;
        float targetX = (float) zombie.getX() * Constants.UI.METER_TO_PIX;
        float targetY = (float) zombie.getY() * Constants.UI.METER_TO_PIX;
        float linearX = originX + (targetX - originX) * progress;
        float linearY = originY + (targetY - originY) * progress;
        float arcOffset = 4f * IMP_THROW_ARC_HEIGHT_PX * progress * (1f - progress);
        float baseX = linearX;
        float baseY = linearY + arcOffset;
        state.animTime += delta;
        boolean flip = originX >= targetX;

        for (ZombieVisualRegistry.PamSpec part : def.pams) {
            String resolvedPath = part.getResolvedPath();
            if (brokenAssets.contains(resolvedPath)) continue;
            try {
                float drawX = baseX + part.offsetX * Constants.UI.METER_TO_PIX;
                float drawY = baseY + part.offsetY * Constants.UI.METER_TO_PIX;
                player.draw(batch, resolvedPath, "fly", state.animTime, drawX, drawY,
                    GameScreenConstants.ZOMBIE_SCALE, GameScreenConstants.ZOMBIE_SCALE, flip);
            } catch (Throwable e) {
                brokenAssets.add(resolvedPath);
            }
        }
    }
}
