package com.compileordie.pvz2.views.game.ui;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.game.levels.LevelID;

public final class LevelSpecificUI {

    private LevelSpecificUI() {
    }

    public static void setupLevelSpecificElements(Stage uiStage, Skin skin) {
        if (uiStage == null || skin == null || AppModel.currentLevel == null) return;

        // 1. PLANT_WHAT_YOU_GET Level Mode
        if (AppModel.currentLevel == LevelID.PLANT_WHAT_YOU_GET) {
            createStartWavesButton(uiStage, skin);
        }

        // 2. LOVE_YOUR_PLANTS Level Mode
        if (AppModel.currentLevel == LevelID.LOVE_YOUR_PLANTS) {

            LostPlantsHud lostPlantsHud = new LostPlantsHud(skin);

            lostPlantsHud.pack();
            lostPlantsHud.setPosition(25f, 25f);
            uiStage.addActor(lostPlantsHud);
        }

        // 3. TIMED_WAR Level Mode
        if (AppModel.currentLevel == LevelID.TIMED_WAR) {

            TimedWarHud timedWarHud = new TimedWarHud(skin);

            Table bottomTable = new Table();
            bottomTable.setFillParent(true);
            bottomTable.bottom().left();
            bottomTable.add(timedWarHud).padBottom(25f).padLeft(25f);

            uiStage.addActor(bottomTable);
        }
    }

    private static void createStartWavesButton(Stage uiStage, Skin skin) {
        TextButton startWavesButton = new TextButton("START WAVES", skin, "green");

        float buttonWidth = 180f;
        float buttonHeight = 52f;
        float paddingX = 25f;
        float paddingY = 25f;

        startWavesButton.setSize(buttonWidth, buttonHeight);
        startWavesButton.setPosition(paddingX, paddingY);

        startWavesButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (AppModel.gameSession != null
                    && AppModel.gameSession.gameBoard != null
                    && AppModel.gameSession.gameBoard.waveManager != null) {

                    AppModel.gameSession.gameBoard.waveManager.shouldStartWaves = true;
                }

                startWavesButton.setTouchable(Touchable.disabled);

                float exitX = -startWavesButton.getWidth() - 50f;
                float duration = 0.45f;

                startWavesButton.addAction(Actions.sequence(
                    Actions.moveTo(exitX, startWavesButton.getY(), duration, Interpolation.pow2In),
                    Actions.removeActor()
                ));
            }
        });

        uiStage.addActor(startWavesButton);
    }
}
