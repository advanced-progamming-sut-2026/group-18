package com.compileordie.pvz2.views.customelements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.compileordie.pvz2.models.AppModel;
import pvz.libpvz.textures.TextureBank;

public class CurrencyHud extends Table {
    private Image diamondIcon;
    private Label diamondLabel;
    private Image coinIcon;
    private Label coinLabel;
    private ImageButton plusBtn;
    private final Skin skin;
    private final Stage stage;
    private final TextureBank textureBank;

    public CurrencyHud(Skin skin, Stage stage, TextureBank textureBank) {
        this.skin = skin;
        this.stage = stage;
        this.textureBank = textureBank;

        initComponents();
        buildLayout();
        setupListeners();
    }

    private void initComponents() {
        // Set the background and padding
        this.setBackground(skin.getDrawable("image_ui_if_bundle_reward_multiplier_bg_10"));
        this.pad(10, 15, 10, 15);

        diamondIcon = new Image(new TextureRegionDrawable(textureBank.region("IMAGE_UI_QUESTS_GEM_ICON")));
        diamondLabel = new Label("0", skin, "medium");
        diamondLabel.setColor(Color.BROWN);
        diamondLabel.setAlignment(Align.left);

        coinIcon = new Image(new TextureRegionDrawable(textureBank.region("IMAGE_UI_QUESTS_COIN_ICON")));
        coinLabel = new Label("0", skin, "medium");
        coinLabel.setColor(Color.BROWN);
        coinLabel.setAlignment(Align.left);

        plusBtn = new ImageButton(new TextureRegionDrawable(
            textureBank.region("IMAGE_UI_CURRENCY_LUCKOTHEZOMBIE_STACK_0")
        ));
    }

    private void buildLayout() {
        this.add(diamondIcon).size(36, 36).padRight(5);
        this.add(diamondLabel).padRight(15);

        this.add(coinIcon).size(40, 40).padRight(5);
        this.add(coinLabel).padRight(10);

        this.add(plusBtn).size(32, 32);
    }

    private void setupListeners() {
        plusBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CheatModal modal = new CheatModal(skin, textureBank);
                modal.show(stage);
            }
        });
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        // Continuously sync the HUD values and button visibility every frame
        if (AppModel.player != null) {
            diamondLabel.setText(String.valueOf(AppModel.player.diamonds));
            coinLabel.setText(String.valueOf(AppModel.player.coins));
        }
    }
}
