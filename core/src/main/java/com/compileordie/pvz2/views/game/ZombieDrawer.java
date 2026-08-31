package com.compileordie.pvz2.views.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.zombies.ZombieBuilder;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.entities.zombies.variants.ZomBoss.DarkZomboss;
import com.compileordie.pvz2.models.entities.zombies.variants.ZomBoss.EgyptZomboss;
import com.compileordie.pvz2.models.entities.zombies.variants.boss.GargantuarZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.capable.HunterZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.capable.OctopusZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.capable.RaZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.mobility.ProspectorZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.AllStarZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.NewspaperZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.standard.StandardZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.summoner.TombraiserZombie;
import com.compileordie.pvz2.models.entities.zombies.variants.vehicle.BarrelRollerZombie;
import com.compileordie.pvz2.models.game.levels.ChapterType;
import pvz.libpvz.pam.PamPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * All live-zombie drawing (extracted from GameScreen, logic unchanged).
 */
final class ZombieDrawer {

    private static final String ICE_BLOCK_PAM =
        "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_ZOMBIE/FROSTBITE_ICE_BLOCK_ZOMBIE.PAM";
    private static final String ICE_BLOCK_CLIP = "idle";

    private final GameRenderStates states;
    private final Set<String> brokenAssets;
    private final DebrisDrawer debris;
    private final ZombieSpecialDrawer special;
    private float effectPulseTime = 0f;
    private boolean isPaused;

    // 🧪 تستی: وقتی true باشه، فشردن کلید "K" یه RaincoatZombie رندوم (ردیف و
    // ستون تصادفی، همون‌جوری که ZombieTestSpawner برای اسپاون وسط زمین انجام
    // می‌ده) اسپاون می‌کنه. فعلا true (طبق درخواست).
    boolean testSunZombie = false;
    private final Random testSpawnRandom = new Random();

    ZombieDrawer(GameRenderStates states, Set<String> brokenAssets, DebrisDrawer debris) {
        this.states = states;
        this.brokenAssets = brokenAssets;
        this.debris = debris;
        this.special = new ZombieSpecialDrawer(brokenAssets);
    }

    void setEffectPulseTime(float t) { this.effectPulseTime = t; }
    void setPaused(boolean p) { this.isPaused = p; }

    void drawZombies(SpriteBatch batch, PamPlayer player, float delta) {
        if (AppModel.gameSession == null || player == null) return;
        handleTestSunZombieSpawnKey();

        List<Zombie> currentZombies = new ArrayList<>(AppModel.gameSession.gameBoard.getAllZombies());
        Set<Zombie> aliveSet = new HashSet<>(currentZombies);
        handleDeaths(aliveSet);
        currentZombies.sort((z1, z2) -> Double.compare(z2.getY(), z1.getY()));
        special.logUnrenderableBoardSummary(delta, currentZombies);

        for (Zombie zombie : currentZombies) {
            GameRenderStates.ZombieRenderState state =
                states.zombieRenderStates.computeIfAbsent(zombie, z -> new GameRenderStates.ZombieRenderState());
            boolean isEatingNow = zombie.isEating()
                || (zombie instanceof GargantuarZombie && ((GargantuarZombie) zombie).isSmashSequenceActive());
            if (isEatingNow != state.wasEating) {
                state.animTime = 0f;
                state.wasEating = isEatingNow;
            }
            drawSingleZombie(batch, player, zombie, state, delta);
        }
    }

    /**
     * وقتی testSunZombie فعاله، با هر بار فشرده شدن کلید "K"، یک RaincoatZombie
     * توی یه ردیف/ستون تصادفیِ داخل خودِ زمین (نه لبه‌ی بیرون از صفحه، چون این
     * زامبی سرعتش صفره و هیچ‌وقت از لبه به داخل نمی‌رسه) اسپاون می‌کنه. مثل
     * بقیه‌ی اسپاون‌های وسط زمین، ZombieBuilder.build() خودش افکت گردباد رو
     * خودکار شروع می‌کنه.
     */
    private void handleTestSunZombieSpawnKey() {
        if (!testSunZombie) return;
        if (!Gdx.input.isKeyJustPressed(Input.Keys.K)) return;
        if (AppModel.gameSession == null || AppModel.gameSession.gameBoard == null) return;

        int row = testSpawnRandom.nextInt(5);
        int col = testSpawnRandom.nextInt(9);
        float spawnX = Constants.Game.PADDING_X_REALITY + (col + 0.5f) * (float) Constants.Game.TILE_WIDTH;
        float spawnY = (row * (float) Constants.Game.TILE_HEIGHT) + Constants.UI.BOTTOM_LINE_METER;

        try {
            Zombie zombie = ZombieBuilder.create(ZombieType.RAINCOAT_ZOMBIE, spawnX, spawnY, row);
            if (zombie != null) {
                AppModel.gameSession.gameBoard.lanes.get(row).zombies.add(zombie);
                Gdx.app.log("PVZ-TEST-SUN-ZOMBIE",
                    "☀️ یک RaincoatZombie تستی در ردیف " + row + "، ستون " + col + " اسپاون شد.");
            }
        } catch (Exception e) {
            Gdx.app.error("PVZ-TEST-SUN-ZOMBIE", "❌ خطا در اسپاون RaincoatZombie تستی: " + e.getMessage());
        }
    }


    private void handleDeaths(Set<Zombie> aliveSet) {
        var iterator = states.zombieRenderStates.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Zombie, GameRenderStates.ZombieRenderState> entry = iterator.next();
            Zombie zombie = entry.getKey();
            if (!aliveSet.contains(zombie)) {
                if (zombie.getHealth() <= 0) {
                    registerDeadZombie(zombie, entry.getValue());
                }
                iterator.remove();
            }
        }
    }

    private void registerDeadZombie(Zombie zombie, GameRenderStates.ZombieRenderState state) {
        GameRenderStates.DeadZombieAnim deadAnim = new GameRenderStates.DeadZombieAnim();
        deadAnim.typeKey = zombie.getType().name();
        if (zombie.killByExplosive) {
            deadAnim.typeKey = "EXPLOSIVE_DEATH";
            deadAnim.effectColor = null;
        }
        deadAnim.x = (float) zombie.getX();
        deadAnim.y = (float) zombie.getY();
        deadAnim.flip = state.flip;
        deadAnim.effectColor = ZombieVisualHelpers.computeZombieEffectColor(zombie, effectPulseTime);
        deadAnim.isReversedDirection =
            (zombie.getType() == ZombieType.PROSPECTOR_ZOMBIE
                && ((ProspectorZombie) zombie).isReversedDirection)
                || zombie.isHypnotized();
        if (zombie.getType()== ZombieType.BARREL_ROLLER && !((BarrelRollerZombie)zombie).isRoller){
            deadAnim.typeKey = "die2";
        }
        states.deadZombies.add(deadAnim);
        spawnHeadDebrisIfNeeded(zombie, deadAnim);
    }

    private void spawnHeadDebrisIfNeeded(Zombie zombie, GameRenderStates.DeadZombieAnim deadAnim) {
        if (zombie.getType() != ZombieType.STANDARD
            && zombie.getType() != ZombieType.CONEHEAD
            && zombie.getType() != ZombieType.BUCKETHEAD) return;

        ZombieVisualRegistry.ZombieVisualDef headDef =
            ZombieVisualRegistry.get(zombie.getType().name());
        if (headDef == null || headDef.pams.isEmpty()) return;

        String headPam = headDef.pams.get(0).getResolvedPath();
        int headFlag = deadAnim.isReversedDirection ? -1 : 1;
        float headScaleX = headFlag * GameScreenConstants.ZOMBIE_SCALE;
        float headScaleY = GameScreenConstants.ZOMBIE_SCALE;
        float headBaseX = (float) zombie.getX() * Constants.UI.METER_TO_PIX;
        float headBaseY = (float) zombie.getY() * Constants.UI.METER_TO_PIX;

        debris.spawnFallingPart(headPam, debris.chapterPartName("skull"), "idle",
            DebrisDrawer.HEAD_DEBRIS_DIE_FREEZE_TIME,
            headBaseX, headBaseY, headScaleX, headScaleY, true, headFlag,
            DebrisDrawer.DEBRIS_FALL_DISTANCE_HEAD_AND_CONE_M);
        debris.spawnFallingPart(headPam, debris.chapterPartName("jaw"), "idle",
            DebrisDrawer.HEAD_DEBRIS_DIE_FREEZE_TIME,
            headBaseX, headBaseY, headScaleX, headScaleY, true, headFlag,
            DebrisDrawer.DEBRIS_FALL_DISTANCE_HEAD_AND_CONE_M);
    }



    private void drawSingleZombie(SpriteBatch batch, PamPlayer player, Zombie zombie,
                                  GameRenderStates.ZombieRenderState state, float delta) {
        String typeKey = zombie.getType().name();
        ZombieVisualRegistry.ZombieVisualDef def = ZombieVisualRegistry.get(typeKey);
        if (def == null) {
            if (!brokenAssets.contains("DEF_MISSING_" + typeKey)) {
                brokenAssets.add("DEF_MISSING_" + typeKey);
                special.logZombieNotRenderable(typeKey, null);
            }
            return;
        }
        if (zombie.isSandstormSpawning() && AppModel.currentChapter== ChapterType.ANCIENT_EGYPT) {
            special.drawSandstormSpawningZombie(batch, player, zombie, state, delta);
            return;
        }
        if (zombie.isFlyingIn()) {
            special.drawFlyingInZombie(batch, player, zombie, state, delta, def);
            return;
        }
        if (!ZombieVisualRegistry.isAvailable(typeKey)) {
            String cacheTag = "ASSET_UNAVAILABLE_" + typeKey + "@" + ZombieVisualRegistry.getChapterTag();
            if (!brokenAssets.contains(cacheTag)) {
                brokenAssets.add(cacheTag);
                special.logZombieNotRenderable(typeKey, def);
            }
            return;
        }
        advanceAnimTime(zombie, state, delta);
        float baseX = (float) zombie.getX() * Constants.UI.METER_TO_PIX;
        float baseY = (float) zombie.getY() * Constants.UI.METER_TO_PIX;
        state.lastDrawX = baseX;
        state.lastDrawY = baseY;
        updateDamageFlash(zombie, state, delta);
        drawFoodedHaloIfNeeded(batch, zombie, baseX, baseY);
        drawSunZombieHaloIfNeeded(batch, zombie, baseX, baseY);
        AnimChoice choice = chooseAnim(zombie, state, typeKey);
        if (isPaused) {
            choice.animName = (zombie.getType() == ZombieType.NEWSPAPER_ZOMBIE)
                ? "idle_newspaper" : "idle";
        }
        Map<String, Boolean> visibilityMap = ZombieVisualHelpers.buildArmorVisibilityMap(zombie, def);
        visibilityMap = applyDebrisVisibility(zombie, state, def, choice, baseX, baseY, visibilityMap);
        drawZombieParts(batch, player, zombie, state, def, choice, baseX, baseY, visibilityMap);
        drawIceBlockIfNeeded(batch, player, zombie, state, delta, baseX, baseY);
    }

    /**
     * وقتی zombie.isFrozenByIce فعاله، بلوک یخ (FROSTBITE_ICE_BLOCK_ZOMBIE)
     * رو دقیقا زیر خود زامبی (یعنی قبل از drawZombieParts، چون در SpriteBatch
     * هر چی زودتر رسم بشه زیرتره) با کلیپ "idle" پخش می‌کنه. به محض false شدن
     * isFrozenByIce (چه با شکستن یخ توسط دمیج، چه هر دلیل دیگه‌ای)، این متد
     * دیگه چیزی رسم نمی‌کنه.
     */
    private void drawIceBlockIfNeeded(SpriteBatch batch, PamPlayer player, Zombie zombie,
                                      GameRenderStates.ZombieRenderState state, float delta,
                                      float baseX, float baseY) {
        if (!zombie.isFrozenByIce) return;
        if (brokenAssets.contains(ICE_BLOCK_PAM)) return;

        state.iceAnimTime += delta;

        com.badlogic.gdx.graphics.Color color = batch.getColor();
        float oldAlpha = color.a;

        try {
            color.a = oldAlpha * 0.4f;
            batch.setColor(color);

            player.draw(batch, ICE_BLOCK_PAM, ICE_BLOCK_CLIP, state.iceAnimTime,
                baseX, baseY, GameScreenConstants.ZOMBIE_SCALE, GameScreenConstants.ZOMBIE_SCALE,
                state.flip);
        } catch (Throwable e) {
            brokenAssets.add(ICE_BLOCK_PAM);
        } finally {
            color.a = oldAlpha;
            batch.setColor(color);
        }
    }

    /**
     * وقتی zombie.isFooded تروئه، یه هاله‌ی نرم زرد+سبز دور زامبی (پشت خودِ
     * اسپرایتش، چون قبل از drawZombieParts صدا زده می‌شه) رسم می‌کنه؛ فقط
     * برای اطلاع بازیکن که این زامبی موقع مرگ پلنت‌فود زمین می‌ندازه.
     * baseX/baseY دقیقا همون مختصاتیه که خودِ بدن زامبی هم باهاش رسم می‌شه
     * (zombie.getX()/getY() که قبلا پدینگ‌دار شده، ضرب در METER_TO_PIX)، پس
     * هیچ پدینگ اضافه‌ای اینجا لازم نیست.
     */
    private void drawFoodedHaloIfNeeded(SpriteBatch batch, Zombie zombie, float baseX, float baseY) {
        if (!zombie.isFooded) return;

        Texture halo = ZombieVisualHelpers.getHaloTexture();
        float pulse = 0.85f + 0.15f * (float) Math.sin(effectPulseTime * 3f);

        float greenSize = 110f * pulse;
        float yellowSize = 78f * pulse;

        Color oldColor = batch.getColor().cpy();
        try {
            batch.setColor(0.45f, 1f, 0.2f, 0.55f); // سبز
            batch.draw(halo, baseX - greenSize / 2f, baseY - greenSize / 2f, greenSize, greenSize);

            batch.setColor(1f, 0.9f, 0.15f, 0.55f); // زرد
            batch.draw(halo, baseX - yellowSize / 2f, baseY - yellowSize / 2f, yellowSize, yellowSize);
        } finally {
            batch.setColor(oldColor);
        }
    }

    /**
     * هاله‌ی زرد، نسبتا بزرگ و ثابت (بدون پالس) دور RaincoatZombie - برخلاف
     * drawFoodedHaloIfNeeded که شرطیه، این همیشه برای این تایپ زامبی رسم
     * می‌شه (یه نشونه‌ی بصری دائمیِ «تولیدکننده‌ی خورشید»).
     */
    private void drawSunZombieHaloIfNeeded(SpriteBatch batch, Zombie zombie, float baseX, float baseY) {
        if (zombie.getType() != ZombieType.RAINCOAT_ZOMBIE) return;

        Texture halo = ZombieVisualHelpers.getHaloTexture();
        float haloSize = 150f;

        Color oldColor = batch.getColor().cpy();
        try {
            batch.setColor(1f, 0.85f, 0.1f, 0.5f); // زرد
            batch.draw(halo, baseX - haloSize / 2f, baseY - haloSize / 2f, haloSize, haloSize);
        } finally {
            batch.setColor(oldColor);
        }
    }


    private void advanceAnimTime(Zombie zombie, GameRenderStates.ZombieRenderState state, float delta) {
        float animSpeedMultiplier;
        if (zombie.isEating()) {
            animSpeedMultiplier = 1.0f;
        } else if (!zombie.canMove()) {
            animSpeedMultiplier = 0f;
        } else {
            double normalSpeed = zombie.getStableSpeed();
            double currentSpeed = Math.abs(zombie.getXSpeed());
            animSpeedMultiplier = normalSpeed > 0 ? (float) (currentSpeed / normalSpeed) : 1f;
        }
        boolean checkAllstar = (zombie.getType() == ZombieType.ALL_STAR
            && ((AllStarZombie) zombie).isCharging());
        state.animTime += delta * (checkAllstar ? animSpeedMultiplier * 0.3 : animSpeedMultiplier);
    }

    private void updateDamageFlash(Zombie zombie, GameRenderStates.ZombieRenderState state, float delta) {
        if (zombie.takedDamage) {
            state.damageAlphaTimer = 0.15f;
            zombie.takedDamage = false;
        }
        if (state.damageAlphaTimer > 0) state.damageAlphaTimer -= delta;
    }

    private static final class AnimChoice {
        String animName;
        float renderAnimTime;
        AnimChoice(String n, float t) { animName = n; renderAnimTime = t; }
    }

    private AnimChoice chooseAnim(Zombie zombie, GameRenderStates.ZombieRenderState state, String typeKey) {
        float renderAnimTime = state.animTime;
        String animName;
        if (zombie.getType() == ZombieType.ZOMBOSS_IN_EGYPT) {
            return egyptZombossAnim(zombie, state);
        } else if (zombie.getType() == ZombieType.ZOMBOSS_IN_DARK) {
            return darkZombossAnim(zombie);
        } else if (typeKey.contains("GARGANTUAR") && ((GargantuarZombie) zombie).isFiringImp()) {
            return gargantuarFireAnim(zombie);
        } else if (zombie.getType() == ZombieType.RA_ZOMBIE && ((RaZombie) zombie).shouldWeSteal()) {
            animName = "power";
        } else if (zombie.getType() == ZombieType.TOMBRAISER
            && ((TombraiserZombie) zombie).isPoweringUp()) {
            animName = "power";
            renderAnimTime = (float) ((TombraiserZombie) zombie).getPowerAnimElapsed();
        } else if (zombie.getType() == ZombieType.HUNTER_ZOMBIE
            && ((HunterZombie) zombie).isThrowing()) {
            animName = "throw";
            renderAnimTime = (float) ((HunterZombie) zombie).getThrowAnimElapsed();
        } else if (zombie.getType() == ZombieType.OCTOPUS_ZOMBIE
            && ((OctopusZombie) zombie).isTossing()) {
            animName = "toss";
            renderAnimTime = (float) ((OctopusZombie) zombie).getTossAnimElapsed();
        } else if (zombie.getType() == ZombieType.ALL_STAR
            && ((AllStarZombie) zombie).isTackleImpacting()) {
            // 💥 لحظه‌ی برخورد تکل - قبلا همون تیکی که ضربه می‌خورد بلافاصله
            // میفتاد رو walk عادی؛ الان تا پایان این تایمر، انیمیشن ضربه رو
            // کامل (از صفر) نگه می‌داریم.
            animName = "tackle";
            renderAnimTime = (float) ((AllStarZombie) zombie).getTackleImpactElapsed();
        } else if (zombie.isEating()
            || (typeKey.contains("GARGANTUAR") && ((GargantuarZombie) zombie).isSmashSequenceActive())) {
            return eatingAnim(zombie, state, typeKey);
        } else if ("PIANIST_ZOMBIE".equals(typeKey)) {
            animName = "play";
        } else if (zombie.getType() == ZombieType.RAINCOAT_ZOMBIE) {
            animName = "idle";
        } else {
            animName = "walk";
            if (zombie.getType() == ZombieType.NEWSPAPER_ZOMBIE
                && !((NewspaperZombie) zombie).isEnraged()) {
                animName = "walk_newspaper";
            }
            if (zombie.getType() == ZombieType.ALL_STAR
                && ((AllStarZombie) zombie).isCharging()) {
                animName = "run";
            }
            if (zombie.getType()==ZombieType.BARREL_ROLLER && !((BarrelRollerZombie)zombie).isRoller){
                animName = "walk2";
            }
        }
        return new AnimChoice(animName, renderAnimTime);
    }

    private AnimChoice egyptZombossAnim(Zombie zombie, GameRenderStates.ZombieRenderState state) {
        EgyptZomboss zomboss = (EgyptZomboss) zombie;
        String animName;
        if (zomboss.boom) animName = "rocket_launch";
        else if (zomboss.stun) animName = "stun_loop";
        else if (zomboss.spawnZombies) animName = "zombie_portal_loop";
        else if (zombie.getXSpeed() > 0) animName = "walk_forward";
        else if (zombie.getXSpeed() < 0) animName = "walk_backwards";
        else animName = "idle";
        if (zomboss.idle) animName = "idle";
        return new AnimChoice(animName, state.animTime);
    }

    private AnimChoice darkZombossAnim(Zombie zombie) {
        DarkZomboss darkZomboss = (DarkZomboss) zombie;
        String animName;
        if (darkZomboss.stun) animName = "stun_loop";
        else if (darkZomboss.spawnZombies) animName = "vulnerable_loop";
        else if (darkZomboss.smash) animName = "fire_attack_idle";
        else if (darkZomboss.boom) animName = "fire_bomb_loop";
        else animName = "idle";
        if (darkZomboss.idle) animName = "idle";
        return new AnimChoice(animName, states.zombieRenderStates.get(zombie).animTime);
    }

    private AnimChoice gargantuarFireAnim(Zombie zombie) {
        final float FIRE_DURATION = 0.5f;
        float elapsed = (float) ((GargantuarZombie) zombie).getFireSequenceElapsed();
        if (elapsed < FIRE_DURATION) {
            return new AnimChoice("fire", elapsed);
        }
        return new AnimChoice("cannon_fire", elapsed - FIRE_DURATION);
    }

    private AnimChoice eatingAnim(Zombie zombie, GameRenderStates.ZombieRenderState state, String typeKey) {
        String animName = "eat";
        float renderAnimTime = state.animTime;
        if (zombie.getType() == ZombieType.NEWSPAPER_ZOMBIE
            && !((NewspaperZombie) zombie).isEnraged()) {
            animName = "eat_newspaper";
        }
        if (zombie.getType() == ZombieType.ALL_STAR && ((AllStarZombie) zombie).isCharging()) {
            animName = "tackle";
        } else if (typeKey.contains("GARGANTUAR")) {
            final float EAT_DUR = 1.3f;
            final float SMASH_DUR = 1.8f;
            final float TOTAL_CYCLE = EAT_DUR + SMASH_DUR;
            float currentCycleTime = state.animTime % TOTAL_CYCLE;
            if (currentCycleTime < EAT_DUR) {
                animName = "eat";
                renderAnimTime = currentCycleTime;
            } else {
                animName = "smash_left";
                renderAnimTime = currentCycleTime - EAT_DUR;
            }
        }
        return new AnimChoice(animName, renderAnimTime);
    }

    private Map<String, Boolean> applyDebrisVisibility(
        Zombie zombie, GameRenderStates.ZombieRenderState state,
        ZombieVisualRegistry.ZombieVisualDef def, AnimChoice choice,
        float baseX, float baseY, Map<String, Boolean> visibilityMap) {

        boolean isBasicOrCone = zombie.getType() == ZombieType.STANDARD
            || zombie.getType() == ZombieType.CONEHEAD
            || zombie.getType() == ZombieType.BUCKETHEAD;
        if (!isBasicOrCone) return visibilityMap;

        int debrisFlag = zombie.isHypnotized() ? -1 : 1;
        float debrisScaleX = debrisFlag * GameScreenConstants.ZOMBIE_SCALE;
        float debrisScaleY = GameScreenConstants.ZOMBIE_SCALE;
        String bodyPam = def.pams.get(0).getResolvedPath();

        if (!state.armDropped && zombie.getMaxHealth() > 0
            && zombie.getHealth() <= zombie.getMaxHealth() * DebrisDrawer.ARM_DROP_HEALTH_RATIO) {
            state.armDropped = true;
            debris.spawnFallingPart(bodyPam, debris.chapterPartName("hand_outer_01"),
                choice.animName, choice.renderAnimTime,
                baseX, baseY, debrisScaleX, debrisScaleY, false, 0f,
                DebrisDrawer.DEBRIS_FALL_DISTANCE_HAND_M);
            debris.spawnFallingPart(bodyPam, debris.chapterPartName("arm_outer_lower"),
                choice.animName, choice.renderAnimTime,
                baseX, baseY, debrisScaleX, debrisScaleY, false, 0f,
                DebrisDrawer.DEBRIS_FALL_DISTANCE_HAND_M);
        }
        if (state.armDropped) {
            if (visibilityMap == null) visibilityMap = new HashMap<>();
            visibilityMap.put(debris.chapterPartName("hand_outer_01"), false);
            visibilityMap.put(debris.chapterPartName("arm_outer_lower"), false);
        }
        if (zombie.getType() == ZombieType.CONEHEAD || zombie.getType() == ZombieType.BUCKETHEAD) {
            float currentArmor = (float) ((StandardZombie) zombie).getArmorHealth();
            if (!Float.isNaN(state.lastArmorHealth) && state.lastArmorHealth > 0 && currentArmor <= 0) {
                String part = zombie.getType() == ZombieType.CONEHEAD
                    ? "zombie_armor_cone_damage_02" : "zombie_armor_bucket_damage_02";
                debris.spawnFallingPart(bodyPam, part, choice.animName, choice.renderAnimTime,
                    baseX, baseY, debrisScaleX, debrisScaleY, false, 0f,
                    DebrisDrawer.DEBRIS_FALL_DISTANCE_HEAD_AND_CONE_M);
            }
            state.lastArmorHealth = currentArmor;
        }
        return visibilityMap;
    }

    private void drawZombieParts(SpriteBatch batch, PamPlayer player, Zombie zombie,
                                 GameRenderStates.ZombieRenderState state,
                                 ZombieVisualRegistry.ZombieVisualDef def, AnimChoice choice,
                                 float baseX, float baseY, Map<String, Boolean> visibilityMap) {
        for (ZombieVisualRegistry.PamSpec part : def.pams) {
            String resolvedPath = part.getResolvedPath();
            if (brokenAssets.contains(resolvedPath)) continue;
            drawOnePart(batch, player, zombie, state, part, resolvedPath, choice, baseX, baseY, visibilityMap);
        }
    }

    private void drawOnePart(SpriteBatch batch, PamPlayer player, Zombie zombie,
                             GameRenderStates.ZombieRenderState state,
                             ZombieVisualRegistry.PamSpec part, String resolvedPath,
                             AnimChoice choice, float baseX, float baseY,
                             Map<String, Boolean> visibilityMap) {
        Color effectColor = null;
        try {
            float drawX = baseX + part.offsetX * Constants.UI.METER_TO_PIX;
            float drawY = baseY + part.offsetY * Constants.UI.METER_TO_PIX;
            int flag = 1;
            if (zombie.getType() == ZombieType.PROSPECTOR_ZOMBIE) {
                if (((ProspectorZombie) zombie).isReversedDirection) flag = -1;
            }
            if (zombie.isHypnotized()) flag = -1;
            float scaleX = flag * GameScreenConstants.ZOMBIE_SCALE;
            float scaleY = GameScreenConstants.ZOMBIE_SCALE;

            Color oldColor = null;
            effectColor = ZombieVisualHelpers.computeZombieEffectColor(zombie, effectPulseTime);
            if (state.damageAlphaTimer > 0) {
                effectColor = new Color(1f, 1f, 1f, 0.6f);
            }
            if (effectColor != null) {
                oldColor = batch.getColor().cpy();
                batch.setColor(effectColor);
            }
            if (visibilityMap != null) {
                com.badlogic.gdx.math.Matrix4 originalTransform = batch.getTransformMatrix().cpy();
                com.badlogic.gdx.math.Matrix4 scaledTransform = originalTransform.cpy()
                    .translate(drawX, drawY, 0f)
                    .scale(scaleX, scaleY, 1f)
                    .translate(-drawX, -drawY, 0f);
                batch.setTransformMatrix(scaledTransform);
                player.draw(batch, resolvedPath, choice.animName, choice.renderAnimTime,
                    drawX, drawY, state.flip, visibilityMap);
                batch.setTransformMatrix(originalTransform);
            } else {
                player.draw(batch, resolvedPath, choice.animName, choice.renderAnimTime,
                    drawX, drawY, scaleX, scaleY,
                    choice.animName.equals("cannon_fire") ? false : state.flip);
            }
            if (oldColor != null) batch.setColor(oldColor);
        } catch (Throwable e) {
            brokenAssets.add(resolvedPath);
        } finally {
            if (effectColor != null) batch.setColor(Color.WHITE);
        }
    }


}
