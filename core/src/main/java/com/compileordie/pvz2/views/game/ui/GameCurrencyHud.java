package com.compileordie.pvz2.views.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.compileordie.pvz2.models.AppModel;
import pvz.libpvz.textures.TextureBank;

public class GameCurrencyHud extends Table {
    private final Skin skin;
    private final Stage stage;
    private final TextureBank textureBank;

    private Image sunIcon;
    private Label sunLabel;
    private Image coinIcon;
    private Label coinLabel;
    private Image diamondIcon;
    private Label diamondLabel;
    private ImageButton plusBtn;

    public GameCurrencyHud(Skin skin, Stage stage, TextureBank textureBank) {
        this.skin = skin;
        this.stage = stage;
        this.textureBank = textureBank;

        initComponents();
        buildLayout();
        setupListeners();
    }

    private void initComponents() {
        setBackground(skin.getDrawable("image_ui_if_bundle_reward_multiplier_bg_10"));
        pad(8, 12, 8, 12);

        // 1. Sun
        var sunRegion = textureBank.region("IMAGE_UI_SEASONS_UNCOMPRESSED_PVZ2_SEASONS_UIASSET_ICON_SUN");
        sunIcon = new Image(new TextureRegionDrawable(sunRegion));
        sunLabel = new Label("0", skin, "medium");
        sunLabel.setColor(Color.BROWN);
        sunLabel.setAlignment(Align.left);

        // 2. Coins
        coinIcon = new Image(new TextureRegionDrawable(textureBank.region("IMAGE_UI_QUESTS_COIN_ICON")));
        coinLabel = new Label("0", skin, "medium");
        coinLabel.setColor(Color.BROWN);
        coinLabel.setAlignment(Align.left);

        // 3. Diamonds
        diamondIcon = new Image(new TextureRegionDrawable(textureBank.region("IMAGE_UI_QUESTS_GEM_ICON")));
        diamondLabel = new Label("0", skin, "medium");
        diamondLabel.setColor(Color.BROWN);
        diamondLabel.setAlignment(Align.left);

        // 4. Plus Cheat Button
        plusBtn = new ImageButton(new TextureRegionDrawable(
            textureBank.region("IMAGE_UI_CURRENCY_LUCKOTHEZOMBIE_STACK_0")
        ));
    }

    private void buildLayout() {
        add(sunIcon).size(32, 32).padRight(4);
        add(sunLabel).padRight(20);

        add(coinIcon).size(32, 32).padRight(4);
        add(coinLabel).padRight(20);

        add(diamondIcon).size(30, 30).padRight(4);
        add(diamondLabel).padRight(20);

        add(plusBtn).size(28, 28);
    }

    private void setupListeners() {
        plusBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (AppModel.player != null && AppModel.player.debugMode) {
                    GameCheatModal modal = new GameCheatModal(skin, textureBank);
                    modal.show(stage);
                }
            }
        });
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (AppModel.gameSession != null && AppModel.gameSession.gameBoard != null) {
            sunLabel.setText(String.valueOf(AppModel.gameSession.gameBoard.economyManager.sunAmount));
        }

        if (AppModel.player != null) {
            coinLabel.setText(String.valueOf(AppModel.player.coins));
            diamondLabel.setText(String.valueOf(AppModel.player.diamonds));
            plusBtn.getImage().setColor(AppModel.player.debugMode ? Color.WHITE : Color.GRAY);
        }
    }
}
