package com.compileordie.pvz2.views.game.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;

public class TimedWarHud extends Table {

    private final ProgressBar sunProgressBar;
    private final Label sunLabel;
    private final ProgressBar timeProgressBar;
    private final Label timeLabel;

    public TimedWarHud(Skin skin) {
        sunProgressBar = new ProgressBar(0f, 1f, 0.001f, false, skin, "xp_green");
        sunLabel = new Label("0 / 0", skin, "medium");

        timeProgressBar = new ProgressBar(0f, 1f, 0.001f, false, skin, "xp_fuschia");
        timeLabel = new Label("0 / 0", skin, "medium");

        add(sunProgressBar).width(200f).height(20f).padBottom(6f).padRight(8f);
        add(sunLabel).padBottom(6f).row();

        add(timeProgressBar).width(200f).height(20f).padRight(8f);
        add(timeLabel);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (AppModel.gameSession == null
            || AppModel.gameSession.gameBoard == null
            || AppModel.gameSession.gameBoard.economyManager == null) {
            return;
        }

        var economy = AppModel.gameSession.gameBoard.economyManager;
        var gameplayConfig = ConfigManager.gameplay();

        if (gameplayConfig != null) {
            // 1. Sun Progress Bar & Label
            int targetSuns = gameplayConfig.timedWarSunTarget;
            if (targetSuns > 0) {
                float sunProgress = (float) economy.totalSunsGenerated / targetSuns;
                sunProgressBar.setValue(Math.clamp(sunProgress, 0f, 1f));
                sunLabel.setText(economy.totalSunsGenerated + " / " + targetSuns);
            }

            // 2. Time Progress Bar & Label
            float timedWarMax = gameplayConfig.timedWarMax;
            if (timedWarMax > 0) {
                float elapsedTime = Constants.Game.TIME_COEFFICIENT * economy.tickCounter;
                float timeProgress = elapsedTime / timedWarMax;
                timeProgressBar.setValue(Math.clamp(timeProgress, 0f, 1f));
                timeLabel.setText((int) elapsedTime + " / " + (int) timedWarMax);
            }
        }
    }
}
