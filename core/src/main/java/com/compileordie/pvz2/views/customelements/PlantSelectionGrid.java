package com.compileordie.pvz2.views.customelements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.views.game.PlantAssetManager;
import com.compileordie.pvz2.views.screens.PamActor;
import pvz.libpvz.pam.ClipRef;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PlantSelectionGrid extends Table {
    private final List<Button> plantButtons = new ArrayList<>();
    private PlantType selectedPlant = null;

    public PlantSelectionGrid(List<PlantType> availablePlants,
                              Skin skin,
                              PlantAssetManager plantAssets, // Now takes PlantAssetManager
                              Consumer<PlantType> onSelect) {
        top().left();

        int columns = 5;
        int currentCol = 0;

        for (PlantType plantType : availablePlants) {
            Button btn = createPlantButton(plantType, skin, plantAssets, onSelect);

            add(btn).size(100, 140).pad(10);

            currentCol++;
            if (currentCol >= columns) {
                row();
                currentCol = 0;
            }
        }
    }

    private Button createPlantButton(PlantType plantType,
                                     Skin skin,
                                     PlantAssetManager plantAssets,
                                     Consumer<PlantType> onSelect) {
        Button.ButtonStyle style = skin.get("brown", TextButton.TextButtonStyle.class);
        Button btn = new Button(style);
        btn.setTransform(true);
        btn.setOrigin(Align.center);

        Label nameLbl = new Label(plantType.toString(), skin, "medium_outline");
        nameLbl.setFontScale(0.5f);
        nameLbl.setAlignment(Align.center);
        nameLbl.setWrap(true);

        ClipRef idleClip = plantAssets.loadPlantClip(plantType);
        Rectangle bounds = plantAssets.getBounds(plantType);
        PamActor animActor = new PamActor(plantAssets, idleClip, bounds);

        Stack contentStack = new Stack();

        // Wrap the animation in a UI Container to leverage built-in matrix scaling
        Container<PamActor> animContainer = new Container<>(animActor);
        animContainer.setTransform(true); // Enables scaling transformations
        animContainer.setOrigin(Align.center); // Scales perfectly from the middle
        animContainer.setScale(0.9f); // Makes the entire animation 10% smaller!
        animContainer.fill(); // Ensures the PamActor still receives the correct cell size

        contentStack.add(animContainer);

        Table textTable = new Table();
        textTable.bottom();
        textTable.add(nameLbl).width(90).padBottom(8);
        contentStack.add(textTable);

        btn.add(contentStack).expand().fill();
        btn.setUserObject(nameLbl);
        plantButtons.add(btn);

        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedPlant = plantType;
                updateSelectionVisuals(btn);
                if (onSelect != null) {
                    onSelect.accept(selectedPlant);
                }
            }
        });

        return btn;
    }

    private void updateSelectionVisuals(Button selectedBtn) {
        for (Button btn : plantButtons) {
            // Retrieve the label we attached earlier
            Label lbl = (Label) btn.getUserObject();

            if (btn == selectedBtn) {
                btn.setColor(Color.WHITE);
                lbl.setColor(Color.WHITE); // Brighten label
                btn.setScale(1.1f);
            } else {
                btn.setColor(Color.DARK_GRAY);
                lbl.setColor(Color.DARK_GRAY); // Darken label
                btn.setScale(1.0f);
            }
        }
    }

    public PlantType getSelectedPlant() {
        return selectedPlant;
    }
}
