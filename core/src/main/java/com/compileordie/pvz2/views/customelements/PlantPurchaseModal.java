package com.compileordie.pvz2.views.customelements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import pvz.libpvz.textures.TextureBank;

public class PlantPurchaseModal extends BaseModal {
    public PlantPurchaseModal(Skin skin,
                              TextureBank textureBank,
                              PlantType plantType,
                              int price,
                              Runnable onConfirmAction) {
        super("Unlock Plant", skin);

        Label promptLabel = new Label("Are you sure you want to unlock" + System.lineSeparator()
            + plantType.toString() + "?", skin, "medium_outline");
        promptLabel.setAlignment(Align.center);

        // Cost display using your coin icon asset
        Table costTable = new Table();
        Label costPrefix = new Label("Total Cost: ", skin, "medium_outline");
        Label costValue = new Label(String.valueOf(price), skin, "medium_outline");
        costValue.setColor(Color.GOLD);

        Image curIcon = new Image(textureBank.region("IMAGE_UI_QUESTS_COIN_ICON"));
        curIcon.setScaling(Scaling.fit);

        costTable.add(costPrefix);
        costTable.add(costValue).padRight(8);
        costTable.add(curIcon).size(28, 28);

        bodyTable.add(promptLabel).padBottom(20).row();
        bodyTable.add(costTable).padBottom(20).row();

        TextButton cancelBtn = new TextButton("Cancel", skin, "brown");
        TextButton buyBtn = new TextButton("Buy", skin, "green");

        buttonTable.add(cancelBtn).size(140, 50).padRight(20);
        buttonTable.add(buyBtn).size(140, 50);

        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
            }
        });

        buyBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onConfirmAction.run();
                hide();
            }
        });
    }
}
