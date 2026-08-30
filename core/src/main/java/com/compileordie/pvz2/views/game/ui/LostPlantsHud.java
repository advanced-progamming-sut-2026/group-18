package com.compileordie.pvz2.views.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.compileordie.pvz2.models.AppModel;

public class LostPlantsHud extends Table {

    private final Label lostPlantsLabel;

    public LostPlantsHud(Skin skin) {
        setBackground(skin.getDrawable("image_ui_if_bundle_reward_multiplier_bg_10"));
        pad(8, 12, 8, 12);

        lostPlantsLabel = new Label("Lost Plants: 0", skin, "medium");
        lostPlantsLabel.setColor(Color.BROWN);
        add(lostPlantsLabel);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (AppModel.gameSession != null && AppModel.gameSession.gameBoard != null) {
            int lostCount = AppModel.gameSession.gameBoard.lostPlants;
            lostPlantsLabel.setText("Lost Plants: " + lostCount);
        }
    }
}
