package com.compileordie.pvz2.views.game.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.compileordie.pvz2.models.entities.zombies.variants.Zombie;
import com.compileordie.pvz2.models.game.board.GameBoard;
import com.compileordie.pvz2.models.game.waves.WaveManager;
import pvz.libpvz.textures.TextureBank;

import java.util.List;

public class WaveProgressBarUI extends Group {

    private final ProgressBar progressBar;
    private final GameBoard gameBoard;
    private final WaveManager waveManager;
    private final TextureBank textureBank;

    private final Group markerGroup;

    public WaveProgressBarUI(GameBoard gameBoard,
                             WaveManager waveManager,
                             Skin skin,
                             TextureBank textureBank) {
        this.gameBoard = gameBoard;
        this.waveManager = waveManager;
        this.textureBank = textureBank;

        this.progressBar = new ProgressBar(0f, 1f, 0.001f, false, skin, "ingame_progress");
        this.progressBar.setAnimateDuration(0.25f);
        this.progressBar.setValue(0f);
        this.addActor(progressBar);

        this.markerGroup = new Group();
        this.addActor(markerGroup);

        setupWaveMarkers();

        if (waveManager != null && waveManager.waveNumber <= 0) {
            this.setVisible(false);
        }
    }

    private void setupWaveMarkers() {
        if (waveManager == null || textureBank == null) return;

        markerGroup.clear();
        List<Float> markers = waveManager.getWaveMarkerPercentages();
        float barWidth = progressBar.getWidth();

        TextureRegion flagPoleRegion = textureBank.region("IMAGE_UI_HUD_INGAME_PROGRESS_METER_FLAG_POLE");
        TextureRegion flagDefaultRegion = textureBank.region("IMAGE_UI_HUD_INGAME_PROGRESS_METER_FLAG_DEFAULT");

        for (Float percentage : markers) {
            Group flagMarkerGroup = new Group();

            // 1. Draw flag pole base
            Image flagPole = new Image(flagPoleRegion);

            // 2. Draw flag banner on top of the pole (offset slightly up and to the right)
            Image flagDefault = new Image(flagDefaultRegion);

            float offsetX = flagPole.getWidth() * 0.1f;
            float offsetY = flagPole.getHeight() * 0.25f;
            flagDefault.setPosition(offsetX, offsetY);

            flagMarkerGroup.addActor(flagPole);
            flagMarkerGroup.addActor(flagDefault);

            float totalWidth = Math.max(flagPole.getWidth(), offsetX + flagDefault.getWidth());
            float totalHeight = Math.max(flagPole.getHeight(), offsetY + flagDefault.getHeight());
            flagMarkerGroup.setSize(totalWidth, totalHeight);

            float posX = barWidth * percentage - (totalWidth / 2f);
            float posY = (progressBar.getHeight() - totalHeight) / 2f;

            flagMarkerGroup.setPosition(posX, posY);
            markerGroup.addActor(flagMarkerGroup);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        updateWaveProgress();
    }

    private void updateWaveProgress() {
        if (waveManager == null) return;
        float unreversedProgress = 1.0f - waveManager.getRemainingProgressPercentage();
        progressBar.setValue(unreversedProgress);
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        progressBar.setSize(width, height);
        setupWaveMarkers();
    }
}
