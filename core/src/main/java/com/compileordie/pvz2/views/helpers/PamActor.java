package com.compileordie.pvz2.views.helpers;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import com.compileordie.pvz2.views.game.PlantAssetManager;
import pvz.libpvz.pam.ClipRef;

public class PamActor extends Actor {
    private final PlantAssetManager plantAssets;
    private final ClipRef clip;
    private final Rectangle bounds;
    private float stateTime = 0f;

    public PamActor(PlantAssetManager plantAssets, ClipRef clip, Rectangle bounds) {
        this.plantAssets = plantAssets;
        this.clip = clip;
        this.bounds = bounds;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // 1. Find the absolute center of this specific UI cell
        float centerX = getX() + (getWidth() / 2f);
        float centerY = getY() + (getHeight() / 2f);

        plantAssets.drawPlant(batch, clip, bounds, centerX, centerY, Align.top, stateTime, true);
    }
}
