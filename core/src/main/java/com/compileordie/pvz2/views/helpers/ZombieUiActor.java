package com.compileordie.pvz2.views.helpers;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import pvz.libpvz.pam.PamPlayer;

import java.util.List;
import java.util.Map;

public class ZombieUiActor extends Actor {
    private final PamPlayer player;
    private final List<String> pamPaths;
    private final Map<String, Boolean> visibilityMap;
    private float stateTime = 0f;
    private final float scale;

    public ZombieUiActor(PamPlayer player, List<String> pamPaths, Map<String, Boolean> visibilityMap, float scale) {
        this.player = player;
        this.pamPaths = pamPaths;
        this.visibilityMap = visibilityMap;
        this.scale = scale;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float drawX = getX(Align.center);
        float drawY = getY(Align.bottom);

        com.badlogic.gdx.math.Matrix4 originalTransform = batch.getTransformMatrix().cpy();
        com.badlogic.gdx.math.Matrix4 scaledTransform = originalTransform.cpy()
            .translate(drawX, drawY, 0f)
            .scale(scale, scale, 1f)
            .translate(-drawX, -drawY, 0f);

        batch.setTransformMatrix(scaledTransform);

        if (batch instanceof SpriteBatch) {
            // Loop through all parts (e.g., both the piano AND the zombie)
            for (String pamPath : pamPaths) {
                player.draw(batch, pamPath, "idle", stateTime, drawX, drawY, true, visibilityMap);
            }
        }

        batch.setTransformMatrix(originalTransform);
    }
}
