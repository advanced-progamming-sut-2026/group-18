package com.compileordie.pvz2.views.customelements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.compileordie.pvz2.controllers.menus.home.SettingsMenuController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.views.helpers.ToastManager;

public class SettingsModal extends BaseModal {
    private Slider diffSlider;
    private Label diffValLabel;
    private Slider speedSlider;
    private Label speedValLabel;
    private CheckBox gridBox;
    private CheckBox debugBox;
    private TextButton cancelBtn;
    private TextButton applyBtn;

    public SettingsModal(Skin skin) {
        super("Settings", skin);

        initComponents(skin);
        buildLayout(skin);
        setupListeners();
        loadInitialStates();
    }

    private void initComponents(Skin skin) {
        // 1. Difficulty Config
        diffSlider = new Slider(1, 5, 1, false, skin, "default-horizontal");
        diffValLabel = new Label("3", skin, "medium");
        diffValLabel.setColor(Color.BROWN);
        diffValLabel.setAlignment(Align.left);

        diffSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                diffValLabel.setText((int) diffSlider.getValue());
            }
        });

        // 2. Game Speed Config
        speedSlider = new Slider(1, 3, 1, false, skin, "default-horizontal");
        speedValLabel = new Label("1x", skin, "medium");
        speedValLabel.setColor(Color.BROWN);
        speedValLabel.setAlignment(Align.left);

        speedSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                speedValLabel.setText((int) speedSlider.getValue() + "x");
            }
        });

        // 3. Toggle Configs
        gridBox = new CheckBox(" Show Grid Lines", skin, "default");
        gridBox.getLabel().setColor(Color.BROWN);

        debugBox = new CheckBox(" Enable Debug Mode", skin, "default");
        debugBox.getLabel().setColor(Color.BROWN);

        // Buttons
        cancelBtn = new TextButton("Cancel", skin, "brown");
        applyBtn = new TextButton("Apply", skin, "green");
    }

    private void buildLayout(Skin skin) {
        Label diffLabel = new Label("Difficulty:", skin, "medium");
        diffLabel.setColor(Color.FOREST);

        Label speedLabel = new Label("Game Speed:", skin, "medium");
        speedLabel.setColor(Color.FOREST);

        // Populating inherited bodyTable
        bodyTable.add(diffLabel).left().padRight(20).padBottom(15);
        bodyTable.add(diffSlider).width(200).padRight(20).padBottom(15);
        bodyTable.add(diffValLabel).width(40).left().padBottom(15).row();

        bodyTable.add(speedLabel).left().padRight(20).padBottom(15);
        bodyTable.add(speedSlider).width(200).padRight(20).padBottom(15);
        bodyTable.add(speedValLabel).width(40).left().padBottom(15).row();

        bodyTable.add(gridBox).colspan(3).left().padBottom(15).row();
        bodyTable.add(debugBox).colspan(3).left().padBottom(25).row();

        // Populating inherited buttonTable
        buttonTable.add(cancelBtn).size(150, 50).padRight(20);
        buttonTable.add(applyBtn).size(150, 50);
    }

    private void setupListeners() {
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide(); // Inherited from BaseModal
            }
        });

        applyBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SettingsMenuController.saveSettings(
                    (int) diffSlider.getValue(),
                    (int) speedSlider.getValue(),
                    gridBox.isChecked(),
                    debugBox.isChecked()
                );

                ToastManager.showSuccess("Settings updated!");
                hide(); // Close modal on save
            }
        });
    }

    private void loadInitialStates() {
        Player player = AppModel.player;
        diffSlider.setValue(player.difficultyLevel);
        speedSlider.setValue(player.gameSpeedCoefficient);
        gridBox.setChecked(player.showGridBox);
        debugBox.setChecked(player.debugMode);
    }
}
