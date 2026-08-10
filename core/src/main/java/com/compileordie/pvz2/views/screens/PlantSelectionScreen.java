package com.compileordie.pvz2.views.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.compileordie.pvz2.controllers.menus.game.PlantSelectionMenuController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;

import java.util.ArrayList;

public class PlantSelectionScreen extends MenuScreen {

    private Table deckTable;

    @Override
    public void showCore() {
        Table root = new Table();
        root.setFillParent(true);
        root.pad(20);
        stage.addActor(root);

        // Left Panel: Available Plants
        Table availableTable = new Table();
        availableTable.top().left();
        availableTable.add(new Label("Available Plants", skin, "medium")).padBottom(20).row();

        Table grid = new Table();
        ArrayList<PlantType> available = PlantSelectionMenuController.getAvailablePlants(AppModel.currentLevel);

        int cols = 0;
        for (PlantType type : available) {
            TextButton plantBtn = new TextButton(type.toString(), skin, "default");
            plantBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    PlantSelectionMenuController.addPlant(type.name());
                    refreshDeck();
                }
            });
            grid.add(plantBtn).size(120, 50).pad(5);
            if (++cols % 4 == 0) grid.row();
        }

        ScrollPane scrollPane = new ScrollPane(grid, skin);
        availableTable.add(scrollPane).width(550).height(400);
        root.add(availableTable).expand().left();

        // Right Panel: Selected Deck
        Table rightPanel = new Table();
        rightPanel.top().right();
        rightPanel.add(new Label("Your Deck (Max 8)", skin, "medium")).padBottom(20).row();

        deckTable = new Table();
        refreshDeck();
        rightPanel.add(deckTable).height(300).padBottom(30).row();

        TextButton startBtn = new TextButton("Let's Rock!", skin, "green");
        startBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                PlantSelectionMenuController.startGame();
            }
        });
        rightPanel.add(startBtn).size(200, 70);

        root.add(rightPanel).width(300).expandY().top();
    }

    private void refreshDeck() {
        deckTable.clear();
        for (PlantType type : AppModel.selectionDeck.keySet()) {
            TextButton removeBtn = new TextButton(type.toString() + " (X)", skin, "brown");
            removeBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    PlantSelectionMenuController.removePant(type.name());
                    refreshDeck();
                }
            });
            deckTable.add(removeBtn).size(150, 40).padBottom(5).row();
        }
    }
}
