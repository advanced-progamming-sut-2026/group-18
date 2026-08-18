package com.compileordie.pvz2.views.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.compileordie.pvz2.config.Constants;
import pvz.libpvz.pam.PamPlayer;

import java.util.HashSet;
import java.util.Set;

/**
 * Dead zombie animations + falling body parts (extracted, logic unchanged).
 */
final class DebrisDrawer {

    static final float ARM_DROP_HEALTH_RATIO = 0.5f;
    static final float DEBRIS_FALL_DISTANCE_HAND_M = 0.5f;
    static final float DEBRIS_FALL_DISTANCE_HEAD_AND_CONE_M = 1.1f;
    static final float DEBRIS_FALL_DURATION = 0.45f;
    static final float DEBRIS_HOLD_DURATION = 0.2f;
    static final float DEBRIS_FADE_DURATION = 0.4f;
    static final float DEBRIS_TOTAL_DURATION =
        DEBRIS_FALL_DURATION + DEBRIS_HOLD_DURATION + DEBRIS_FADE_DURATION;
    static final float DEBRIS_PARABOLA_ARC_HEIGHT_PX = 75f;
    static final float DEBRIS_PARABOLA_HORIZONTAL_PX = 105f;
    static final float HEAD_DEBRIS_DIE_FREEZE_TIME = 1.6f;

    private final GameRenderStates states;
    private final Set<String> brokenAssets;

    DebrisDrawer(GameRenderStates states, Set<String> brokenAssets) {
        this.states = states;
        this.brokenAssets = brokenAssets;
    }

    String chapterPartName(String suffix) {
        if ("EGYPT".equals(ZombieVisualRegistry.getChapterTag())) {
            return "zombie_egypt_" + suffix;
        }
        return "zombie_" + suffix;
    }

    void spawnFallingPart(String pam, String partName, String sourceClip, float freezeTime,
                          float startX, float startY, float scaleX, float scaleY,
                          boolean parabolic, float horizontalDir, float fallDistanceM) {
        GameRenderStates.FallingDebris d = new GameRenderStates.FallingDebris();
        d.pam = pam;
        d.partName = partName;
        d.sourceClip = sourceClip;
        d.freezeTime = freezeTime;
        d.startX = startX;
        d.startY = startY;
        d.scaleX = scaleX;
        d.scaleY = scaleY;
        d.parabolic = parabolic;
        d.horizontalDir = horizontalDir;
        d.fallDistanceM = fallDistanceM;
        states.fallingDebris.add(d);
    }

    void drawFallingDebris(SpriteBatch batch, PamPlayer player, float delta) {
        if (states.fallingDebris.isEmpty()) return;
        var iterator = states.fallingDebris.iterator();
        while (iterator.hasNext()) {
            GameRenderStates.FallingDebris d = iterator.next();
            d.animTime += delta;
            if (d.animTime >= DEBRIS_TOTAL_DURATION) {
                iterator.remove();
                continue;
            }
            if (brokenAssets.contains(d.pam)) continue;
            drawOneDebris(batch, player, d);
        }
    }

    private void drawOneDebris(SpriteBatch batch, PamPlayer player, GameRenderStates.FallingDebris d) {
        float fallT = Math.min(1f, d.animTime / DEBRIS_FALL_DURATION);
        float fallenPx = d.fallDistanceM * Constants.UI.METER_TO_PIX * fallT;
        float curX = d.startX;
        float curY = d.startY - fallenPx;
        if (d.parabolic) {
            curX += DEBRIS_PARABOLA_HORIZONTAL_PX * d.horizontalDir * fallT;
            curY += DEBRIS_PARABOLA_ARC_HEIGHT_PX * (float) Math.sin(Math.PI * fallT);
        }
        float alpha = 1f;
        if (d.animTime > DEBRIS_FALL_DURATION + DEBRIS_HOLD_DURATION) {
            float fadeT = (d.animTime - DEBRIS_FALL_DURATION - DEBRIS_HOLD_DURATION) / DEBRIS_FADE_DURATION;
            alpha = Math.max(0f, 1f - fadeT);
        }
        Color oldColor = batch.getColor().cpy();
        com.badlogic.gdx.math.Matrix4 originalTransform = batch.getTransformMatrix().cpy();
        try {
            batch.setColor(1f, 1f, 1f, alpha);
            com.badlogic.gdx.math.Matrix4 scaledTransform = originalTransform.cpy()
                .translate(curX, curY, 0f)
                .scale(d.scaleX, d.scaleY, 1f)
                .translate(-curX, -curY, 0f);
            batch.setTransformMatrix(scaledTransform);
            player.drawPart(batch, d.pam, d.sourceClip, d.freezeTime, curX, curY, d.partName);
        } catch (Throwable e) {
            brokenAssets.add(d.pam);
        } finally {
            batch.setTransformMatrix(originalTransform);
            batch.setColor(oldColor);
        }
    }

    void drawDeadZombies(SpriteBatch batch, PamPlayer player, float delta) {
        final float DIE_ANIM_SPEED = 1.0f;
        final float TOTAL_DEATH_DURATION = 2.2f;
        var iterator = states.deadZombies.iterator();
        while (iterator.hasNext()) {
            GameRenderStates.DeadZombieAnim deadAnim = iterator.next();
            deadAnim.animTime += delta * DIE_ANIM_SPEED;
            if (deadAnim.animTime >= TOTAL_DEATH_DURATION) {
                iterator.remove();
                continue;
            }
            drawOneDeadZombie(batch, player, deadAnim);
        }
    }

    private void drawOneDeadZombie(SpriteBatch batch, PamPlayer player,
                                   GameRenderStates.DeadZombieAnim deadAnim) {
        ZombieVisualRegistry.ZombieVisualDef def = ZombieVisualRegistry.get(deadAnim.typeKey);
        if (def == null || !ZombieVisualRegistry.isAvailable(deadAnim.typeKey)) return;

        float baseX = deadAnim.x * Constants.UI.METER_TO_PIX;
        float baseY = deadAnim.y * Constants.UI.METER_TO_PIX;
        String animName = deadAnim.typeKey.equals("EXPLOSIVE_DEATH") ? "animation" : "die";

        for (ZombieVisualRegistry.PamSpec part : def.pams) {
            String resolvedPath = part.getResolvedPath();
            if (brokenAssets.contains(resolvedPath)) continue;
            drawDeadPart(batch, player, deadAnim, part, resolvedPath, animName, baseX, baseY);
        }
    }

    private void drawDeadPart(SpriteBatch batch, PamPlayer player,
                              GameRenderStates.DeadZombieAnim deadAnim,
                              ZombieVisualRegistry.PamSpec part, String resolvedPath,
                              String animName, float baseX, float baseY) {
        Color oldColor = null;
        try {
            float drawX = baseX + part.offsetX * Constants.UI.METER_TO_PIX;
            float drawY = baseY + part.offsetY * Constants.UI.METER_TO_PIX;
            int flag = deadAnim.isReversedDirection ? -1 : 1;
            float scaleX = flag * GameScreenConstants.ZOMBIE_SCALE;
            float scaleY = GameScreenConstants.ZOMBIE_SCALE;
            if (deadAnim.effectColor != null) {
                oldColor = batch.getColor().cpy();
                batch.setColor(deadAnim.effectColor);
            }
            float animDuration = (resolvedPath.toLowerCase().contains("gargantuar") ? 1.9f
                : resolvedPath.toLowerCase().contains("imp") ? 1f : 1.6f);
            float renderTime = Math.min(deadAnim.animTime, animDuration);
            player.draw(batch, resolvedPath, animName, renderTime, drawX, drawY, scaleX, scaleY, deadAnim.flip);
            if (oldColor != null) batch.setColor(oldColor);
        } catch (Throwable e) {
            brokenAssets.add(resolvedPath);
        } finally {
            if (deadAnim.effectColor != null) batch.setColor(Color.WHITE);
        }
    }
}
