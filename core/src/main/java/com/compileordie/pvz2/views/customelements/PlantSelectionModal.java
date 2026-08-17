package com.compileordie.pvz2.views.customelements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.views.game.PlantAssetManager;

import java.util.function.Consumer;

public class PlantSelectionModal extends BaseModal {
    public PlantSelectionModal(Skin skin, PlantAssetManager plantAssets, Consumer<PlantType> onConfirm) {
        super("Select a Plant", skin);

        // 1. Create the reusable grid
        PlantSelectionGrid grid = new PlantSelectionGrid(
            AppModel.player.unlockedPlants,
            skin,
            plantAssets, // Pass the manager here instead of textureBank!
            _ -> {
            } // We handle confirmation via the bottom button instead
        );

        // 2. Wrap it in a scroll pane
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle(
            skin.get(ScrollPane.ScrollPaneStyle.class)
        );
        scrollStyle.vScrollKnob = skin.newDrawable(scrollStyle.vScrollKnob, Color.GOLDENROD);

        ScrollPane scrollPane = new ScrollPane(grid, scrollStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        bodyTable.add(scrollPane).width(640).height(350).padTop(10);

        // 3. Action Buttons
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
                PlantType selected = grid.getSelectedPlant();
                if (selected != null) {
                    onConfirm.accept(selected);
                    hide();
                }
            }
        });
    }
}
