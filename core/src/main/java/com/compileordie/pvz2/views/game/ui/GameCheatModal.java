package com.compileordie.pvz2.views.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.views.customelements.BaseModal;
import com.compileordie.pvz2.views.helpers.ToastManager;
import pvz.libpvz.textures.TextureBank;

public class GameCheatModal extends BaseModal {
    private TextureBank textureBank;
    private Skin skin;
    private TextField sunField;
    private TextField plantFoodField;
    private TextField coinField;
    private TextField diamondField;
    private TextButton cancelBtn;
    private TextButton applyBtn;

    public GameCheatModal(Skin skin, TextureBank textureBank) {
        super("Modify Game Resources", skin);

        initComponents(skin, textureBank);
        buildLayout();
        setupListeners();
        loadInitialStates();
    }

    private void initComponents(Skin skin, TextureBank textureBank) {
        this.textureBank = textureBank;
        this.skin = skin;
        sunField = createDigitsField(skin);
        plantFoodField = createDigitsField(skin);
        coinField = createDigitsField(skin);
        diamondField = createDigitsField(skin);

        cancelBtn = new TextButton("Cancel", skin, "brown");
        applyBtn = new TextButton("Apply", skin, "green");
    }

    private TextField createDigitsField(Skin skin) {
        TextField field = new TextField("0", skin);
        field.setAlignment(Align.center);
        field.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        return field;
    }

    private Table createResourceRow(String iconKey, String labelText, TextField field, int step,
                                    Skin skin, TextureBank textureBank) {
        Table row = new Table();
        var region = textureBank.region(iconKey);
        Image icon = new Image(new TextureRegionDrawable(region));
        Label label = new Label(labelText, skin, "medium");
        label.setColor(Color.BROWN);

        ImageButton downBtn = new ImageButton(new TextureRegionDrawable(
            textureBank.region("IMAGE_UI_GENERIC_ARROW_DOWN_ORANGE")
        ));
        ImageButton upBtn = new ImageButton(new TextureRegionDrawable(
            textureBank.region("IMAGE_UI_GENERIC_ARROW_UP_GREEN")
        ));

        downBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int val = parseField(field);
                field.setText(String.valueOf(Math.max(0, val - step)));
            }
        });

        upBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int val = parseField(field);
                field.setText(String.valueOf(val + step));
            }
        });

        row.add(icon).size(34, 34).padRight(8);
        row.add(label).width(110).left();
        row.add(downBtn).size(34, 34).padRight(8);
        row.add(field).width(90).padRight(8);
        row.add(upBtn).size(34, 34);
        return row;
    }

    private void buildLayout() {
        bodyTable.add(createResourceRow("IMAGE_UI_SEASONS_UNCOMPRESSED_PVZ2_SEASONS_UIASSET_ICON_SUN",
            "Sun", sunField, 25, skin, textureBank)).padBottom(10).row();
        bodyTable.add(createResourceRow("IMAGE_UI_DANGERROOM_PLANTFOOD_ICON",
            "Plant Food", plantFoodField, 1, skin, textureBank)).padBottom(10).row();
        bodyTable.add(createResourceRow("IMAGE_UI_QUESTS_COIN_ICON",
            "Coins", coinField, 100, skin, textureBank)).padBottom(10).row();
        bodyTable.add(createResourceRow("IMAGE_UI_QUESTS_GEM_ICON",
            "Diamonds", diamondField, 5, skin, textureBank)).padBottom(15).row();

        buttonTable.add(cancelBtn).size(140, 45).padRight(15);
        buttonTable.add(applyBtn).size(140, 45);
    }

    private void setupListeners() {
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
            }
        });

        applyBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    int newSun = Integer.parseInt(sunField.getText());
                    int newPf = Integer.parseInt(plantFoodField.getText());
                    int newCoins = Integer.parseInt(coinField.getText());
                    int newDiamonds = Integer.parseInt(diamondField.getText());

                    if (newSun < 0 || newPf < 0 || newCoins < 0 || newDiamonds < 0) {
                        throw new NumberFormatException();
                    }

                    if (newPf > 3) {
                        throw new NumberFormatException();
                    }

                    if (AppModel.gameSession != null && AppModel.gameSession.gameBoard != null) {
                        AppModel.gameSession.gameBoard.economyManager.sunAmount = newSun;
                    }

                    if (AppModel.player != null) {
                        AppModel.player.coins = newCoins;
                        AppModel.player.diamonds = newDiamonds;
                        AppModel.player.plantFoodCount = newPf;
                        new UserDatabase(AppModel.player.username).save(AppModel.player);
                    }

                    ToastManager.showSuccess("Resources updated!");
                    hide();
                } catch (NumberFormatException e) {
                    ToastManager.showError("Invalid resource amount!");
                }
            }
        });
    }

    private void loadInitialStates() {
        if (AppModel.gameSession != null && AppModel.gameSession.gameBoard != null) {
            sunField.setText(String.valueOf(AppModel.gameSession.gameBoard.economyManager.sunAmount));
        }
        if (AppModel.player != null) {
            coinField.setText(String.valueOf(AppModel.player.coins));
            diamondField.setText(String.valueOf(AppModel.player.diamonds));
            plantFoodField.setText(String.valueOf(AppModel.player.plantFoodCount));
        }
    }

    private int parseField(TextField field) {
        try {
            return Integer.parseInt(field.getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
