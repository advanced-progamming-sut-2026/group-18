package com.compileordie.pvz2.views.customelements;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.TimeUtils;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.views.helpers.ToastManager;

public class DebugGrowthModal extends BaseModal {
    public DebugGrowthModal(Skin skin, int potIndex, Runnable onConfirm) {
        super("Debug Cheat", skin);

        Label promptLabel = new Label("Fast-forward growth for free?", skin, "medium_outline");
        promptLabel.setAlignment(Align.center);

        bodyTable.add(promptLabel).padBottom(20).center().row();

        TextButton cancelBtn = new TextButton("Cancel", skin, "brown");
        TextButton confirmBtn = new TextButton("Confirm", skin, "green");

        buttonTable.add(cancelBtn).size(140, 50).padRight(20);
        buttonTable.add(confirmBtn).size(140, 50);

        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
            }
        });

        confirmBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AppModel.player.greenhousePots.get(potIndex).readyTimeMillis = TimeUtils.millis();
                new UserDatabase(AppModel.player.username).save(AppModel.player);

                ToastManager.showSuccess("Growth artificially accelerated!");
                onConfirm.run();
                hide();
            }
        });
    }
}
