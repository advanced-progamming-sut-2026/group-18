package com.compileordie.pvz2.views.game;

import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;

/**
 * Camera shake + giant footstep logic (extracted from GameScreen, logic unchanged).
 */
final class GameCameraEffects {

    float shakeTimer = 0f;
    float shakeIntensity = 0f;
    float shakeOffsetX = 0f;
    float shakeOffsetY = 0f;
    float giantStepTimer = 0f;

    void triggerCameraShake(float duration, float intensity) {
        this.shakeTimer = duration;
        this.shakeIntensity = intensity;
    }

    void updateCameraShake(float delta) {
        if (shakeTimer > 0) {
            shakeTimer -= delta;
            shakeOffsetX = com.badlogic.gdx.math.MathUtils.random(-shakeIntensity, shakeIntensity);
            shakeOffsetY = com.badlogic.gdx.math.MathUtils.random(-shakeIntensity, shakeIntensity);
            if (shakeTimer <= 0) {
                shakeOffsetX = 0f;
                shakeOffsetY = 0f;
            }
        } else {
            shakeOffsetX = 0f;
            shakeOffsetY = 0f;
        }
    }

    void checkGiantZombieFootsteps(float delta, boolean isPaused) {
        if (AppModel.gameSession == null || isPaused) {
            giantStepTimer = 0f;
            return;
        }
        boolean isGiantActive = false;
        for (var lane : AppModel.gameSession.gameBoard.lanes) {
            for (var zombie : lane.zombies) {
                if (zombie.isAlive() && isGargantuar(zombie)) {
                    isGiantActive = true;
                    break;
                }
            }
            if (isGiantActive) break;
        }
        if (isGiantActive) {
            giantStepTimer += delta;
            if (giantStepTimer >= 1.22f) {
                triggerCameraShake(0.12f, 5.0f);
                giantStepTimer = 0f;
            }
        } else {
            giantStepTimer = 0f;
        }
    }

    private boolean isGargantuar(Object zombie) {
        String className = zombie.getClass().getSimpleName().toUpperCase();
        return className.contains("GARGANTUAR");
    }
}
