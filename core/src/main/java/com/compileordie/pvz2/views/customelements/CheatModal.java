package com.compileordie.pvz2.views.customelements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.views.helpers.ToastManager;
import pvz.libpvz.textures.TextureBank;

public class CheatModal extends BaseModal {
    private TextField diamondField;
    private TextField coinField;
    private Image diamondIcon;
    private Label diamondLabel;
    private ImageButton diamondDownBtn;
    private ImageButton diamondUpBtn;
    private Image coinIcon;
    private Label coinLabel;
    private ImageButton coinDownBtn;
    private ImageButton coinUpBtn;
    private TextButton cancelBtn;
    private TextButton applyBtn;

    public CheatModal(Skin skin, TextureBank textureBank) {
        super("Modify Resources", skin);

        initComponents(skin, textureBank);
        buildLayout();
        setupListeners();
        loadInitialStates();
    }

    private void initComponents(Skin skin, TextureBank textureBank) {
        // 1. Diamond Row Components
        diamondIcon = new Image(new TextureRegionDrawable(textureBank.region("IMAGE_UI_QUESTS_GEM_ICON")));
        diamondLabel = new Label("Diamonds", skin, "medium");
        diamondLabel.setColor(Color.BROWN);

        diamondDownBtn = new ImageButton(new TextureRegionDrawable(
            textureBank.region("IMAGE_UI_GENERIC_ARROW_DOWN_ORANGE")
        ));
        diamondUpBtn = new ImageButton(new TextureRegionDrawable(
            textureBank.region("IMAGE_UI_GENERIC_ARROW_UP_GREEN")
        ));

        diamondField = new TextField("0", skin);
        diamondField.setAlignment(Align.center);
        diamondField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());

        // 2. Coin Row Components
        coinIcon = new Image(new TextureRegionDrawable(textureBank.region("IMAGE_UI_QUESTS_COIN_ICON")));
        coinLabel = new Label("Coins", skin, "medium");
        coinLabel.setColor(Color.BROWN);

        coinDownBtn = new ImageButton(new TextureRegionDrawable(
            textureBank.region("IMAGE_UI_GENERIC_ARROW_DOWN_ORANGE")
        ));
        coinUpBtn = new ImageButton(new TextureRegionDrawable(
            textureBank.region("IMAGE_UI_GENERIC_ARROW_UP_GREEN")
        ));

        coinField = new TextField("0", skin);
        coinField.setAlignment(Align.center);
        coinField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());

        // 3. Action Buttons
        cancelBtn = new TextButton("Cancel", skin, "brown");
        applyBtn = new TextButton("Apply", skin, "green");
    }

    private void buildLayout() {
        Table diamondEditor = new Table();
        diamondEditor.add(diamondIcon).size(40, 40).padRight(10);
        diamondEditor.add(diamondLabel).width(120).left();
        diamondEditor.add(diamondDownBtn).size(40, 40).padRight(10);
        diamondEditor.add(diamondField).width(100).padRight(10);
        diamondEditor.add(diamondUpBtn).size(40, 40);

        Table coinEditor = new Table();
        coinEditor.add(coinIcon).size(40, 40).padRight(10);
        coinEditor.add(coinLabel).width(120).left();
        coinEditor.add(coinDownBtn).size(40, 40).padRight(10);
        coinEditor.add(coinField).width(100).padRight(10);
        coinEditor.add(coinUpBtn).size(40, 40);

        // Populating inherited bodyTable
        bodyTable.add(diamondEditor).padBottom(20).row();
        bodyTable.add(coinEditor).padBottom(20).row();

        // Populating inherited buttonTable
        buttonTable.add(cancelBtn).size(150, 50).padRight(20);
        buttonTable.add(applyBtn).size(150, 50);
    }

    private void setupListeners() {
        // Step modifiers for Diamonds
        diamondDownBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int val = parseField(diamondField);
                diamondField.setText(String.valueOf(Math.max(0, val - 5)));
            }
        });

        diamondUpBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int val = parseField(diamondField);
                diamondField.setText(String.valueOf(val + 5));
            }
        });

        // Step modifiers for Coins
        coinDownBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int val = parseField(coinField);
                coinField.setText(String.valueOf(Math.max(0, val - 100)));
            }
        });

        coinUpBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int val = parseField(coinField);
                coinField.setText(String.valueOf(val + 100));
            }
        });

        // Modal Action Listeners
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
                    int newDiamonds = Integer.parseInt(diamondField.getText());
                    int newCoins = Integer.parseInt(coinField.getText());

                    if (newDiamonds < 0 || newCoins < 0) throw new NumberFormatException();

                    AppModel.player.diamonds = newDiamonds;
                    AppModel.player.coins = newCoins;
                    new UserDatabase(AppModel.player.username).save(AppModel.player);

                    ToastManager.showSuccess("Resources updated!");
                    hide();
                } catch (NumberFormatException e) {
                    ToastManager.showError("Invalid resource amount!");
                }
            }
        });
    }

    private void loadInitialStates() {
        int currentDiamonds = AppModel.player != null ? AppModel.player.diamonds : 0;
        int currentCoins = AppModel.player != null ? AppModel.player.coins : 0;

        diamondField.setText(String.valueOf(currentDiamonds));
        coinField.setText(String.valueOf(currentCoins));
    }

    private int parseField(TextField field) {
        try {
            return Integer.parseInt(field.getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
