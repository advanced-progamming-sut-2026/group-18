package com.compileordie.pvz2.views.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.compileordie.pvz2.controllers.menus.home.MainMenuController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.views.ScreenManager;
import com.compileordie.pvz2.views.ScreenType;
import com.compileordie.pvz2.views.customelements.BadgeWrapper;
import com.compileordie.pvz2.views.customelements.CurrencyHud;
import com.compileordie.pvz2.views.customelements.NewsModal;
import com.compileordie.pvz2.views.customelements.SettingsModal;
import com.compileordie.pvz2.views.helpers.ToastManager;

public class MainMenuScreen extends MenuScreen {
    private Stack stack;
    private Table hudTable;
    private Table centerTable;
    private Table subButtons;
    private CurrencyHud currencyHud;
    private ImageButton settingsBtn;
    private ImageButton newsBtn;
    private BadgeWrapper badgedNewsBtn;
    private ImageButton closeBtn;
    private Label teamName;
    private TextButton playBtn;
    private TextButton profileBtn;
    private TextButton logoutBtn;

    @Override
    public void showCore() {
        initComponents();
        buildLayout();
        attachListeners();
    }

    private void initComponents() {
        stack = new Stack();
        stack.setFillParent(true);
        stage.addActor(stack);
        hudTable = new Table();
        hudTable.setFillParent(true);
        centerTable = new Table();
        centerTable.setFillParent(true);
        subButtons = new Table();
        // HUD Initialization
        currencyHud = new CurrencyHud(skin, stage, textureBank);
        settingsBtn = new ImageButton(skin, "default");
        newsBtn = new ImageButton(skin, "almanac");
        badgedNewsBtn = new BadgeWrapper(newsBtn, skin);
        badgedNewsBtn.setBadgeCount(AppModel.player.getUnreadNewsCount());

        teamName = new Label("Welcome " + AppModel.player.nickname, skin, "medium_outline");
        closeBtn = new ImageButton(skin, "generic_close");

        // Center Initialization
        playBtn = new TextButton("PLAY", skin, "green");
        profileBtn = new TextButton("Profile", skin, "default");
        logoutBtn = new TextButton("Logout", skin, "brown");
    }

    private void buildLayout() {
        setupBackground();
        setupHud();
        setupCenterTable();
    }

    private void setupBackground() {
        String[] backgrounds = {
            "IMAGE_UI_THYMED_EVENTS_LAWNBOWL_EVENT_BG", "IMAGE_UI_THYMED_EVENTS_GEM_SPREE_EVENT_BG",
            "IMAGE_UI_THYMED_EVENTS_FEASTIVUS_EVENT_BG", "IMAGE_UI_THYMED_EVENTS_BIRTHDAYZ_EVENT_BG",
            "IMAGE_UI_THYMED_EVENTS_VALENBRAINZ2025_EVENT_BG", "IMAGE_UI_THYMED_EVENTS_MGPWINTEREVENT",
            "IMAGE_UI_THYMED_EVENTS_LAWNOFDOOM_EVENT_BG", "IMAGE_UI_THYMED_EVENTS_HARVESTFESTIVAL_EVENT_BG",
            "IMAGE_UI_THYMED_EVENTS_FOODFIGHT_EVENT_BG", "IMAGE_UI_THYMED_EVENTS_COINS_SPREE_EVENT_BG",
            "IMAGE_UI_THYMED_EVENTS_BEACH_EVENT_BG", "IMAGE_UI_THYMED_EVENTS_THE_SPRINGENING_2023_EVENT_BG"
        };
        String selectedBg = backgrounds[MathUtils.random(backgrounds.length - 1)];
        TextureRegion bgRegion = textureBank.region(selectedBg);

        if (bgRegion != null) {
            Image bgImage = createParallaxImage(bgRegion);
            stack.add(bgImage);
        }
    }

    private Image createParallaxImage(TextureRegion bgRegion) {
        return new Image(new TextureRegionDrawable(bgRegion)) {
            private float currentPercent = 0.5f;

            @Override
            public void draw(Batch batch, float parentAlpha) {
                float imageAspect = getDrawable().getMinWidth() / getDrawable().getMinHeight();
                float drawHeight = getHeight();
                float drawWidth = drawHeight * imageAspect;
                if (drawWidth < getWidth()) {
                    drawWidth = getWidth();
                    drawHeight = drawWidth / imageAspect;
                }
                float screenWidth = Gdx.graphics.getWidth();
                float targetPercent = Math.clamp(Gdx.input.getX() / screenWidth, 0f, 1f);
                currentPercent = MathUtils.lerp(currentPercent,
                    targetPercent,
                    Math.min(1f, 3f * Gdx.graphics.getDeltaTime()));
                float excessWidth = drawWidth - getWidth();
                float drawX = getX() - (excessWidth * currentPercent);
                float drawY = getY() + (getHeight() - drawHeight) / 2f;

                // Set the batch color using the actor's color and the parent's alpha
                Color color = getColor();
                batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

                getDrawable().draw(batch, drawX, drawY, drawWidth, drawHeight);
            }
        };
    }

    private void setupHud() {
        stack.add(hudTable);
        hudTable.top();

        hudTable.add(currencyHud).pad(25).left();
        hudTable.add().expandX();
        hudTable.add(badgedNewsBtn);
        hudTable.add(settingsBtn).pad(30).right();

        hudTable.row().expandY().bottom();
        hudTable.add(teamName).pad(25).padBottom(20).left();
        hudTable.add().expandX();

        hudTable.add(closeBtn).colspan(2).size(64, 64).padRight(35).padBottom(-3).right().bottom();
    }

    private void setupCenterTable() {
        stack.add(centerTable);

        TextureRegion logoRegion = textureBank.region("IMAGE_UI_MAINMENU_PVZ2_LOGO_HORIZONTAL");
        if (logoRegion != null) {
            Image logo = new Image(new TextureRegionDrawable(logoRegion));
            logo.setScaling(Scaling.fit);
            centerTable.add(logo).width(450).padBottom(40).row();
        }

        subButtons.add(profileBtn).size(140, 50).padRight(20);
        subButtons.add(logoutBtn).size(140, 50);

        centerTable.add(playBtn).size(300, 70).padBottom(10).row();
        centerTable.add(subButtons);
    }

    private void attachListeners() {
        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SettingsModal settingsModal = new SettingsModal(skin);
                settingsModal.show(stage);
            }
        });

        newsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                NewsModal newsModal = new NewsModal(skin);
                newsModal.show(stage);
                badgedNewsBtn.setBadgeCount(0);
            }
        });

        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        playBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.GAME);
            }
        });

        profileBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.PROFILE);
            }
        });

        logoutBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                MainMenuController.logoutUser();
                ToastManager.showSuccess("Logged out successfully");
                ScreenManager.setMenuScreen(ScreenType.LOGIN);
            }
        });
    }
}
