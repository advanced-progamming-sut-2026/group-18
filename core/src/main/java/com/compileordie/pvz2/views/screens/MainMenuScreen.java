package com.compileordie.pvz2.views.screens;

import com.badlogic.gdx.Gdx;
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
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.game.SessionBuilder;
import com.compileordie.pvz2.models.game.levels.ChapterType;
import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.views.ScreenManager;
import com.compileordie.pvz2.views.ScreenType;
import com.compileordie.pvz2.views.customelements.BadgeWrapper;
import com.compileordie.pvz2.views.helpers.ToastManager;

public class MainMenuScreen extends MenuScreen {

    @Override
    public void showCore() {
        Stack stack = new Stack();
        stack.setFillParent(true);
        stage.addActor(stack);

        setupBackground(stack);
        setupHud(stack);
        setupCenterTable(stack);
    }

    private void setupBackground(Stack stack) {
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
                getDrawable().draw(batch, drawX, drawY, drawWidth, drawHeight);
            }
        };
    }

    private void setupHud(Stack stack) {
        Table hudTable = new Table();
        hudTable.setFillParent(true);
        stack.add(hudTable);

        ImageButton settingsBtn = new ImageButton(skin, "default");
        ImageButton newsBtn = new ImageButton(skin, "almanac");
        BadgeWrapper badgedNewsBtn = new BadgeWrapper(newsBtn, skin);
        badgedNewsBtn.setBadgeCount(AppModel.player.getUnreadNewsCount());

        hudTable.top();
        hudTable.add(settingsBtn).pad(25).left();
        hudTable.add().expandX();
        hudTable.add(badgedNewsBtn).pad(25).right();

        hudTable.row().expandY().bottom();
        Label teamName = new Label("Compile or Die!", skin, "medium_outline");
        hudTable.add(teamName).pad(25).padBottom(20).left();
        hudTable.add().expandX();

        ImageButton closeBtn = new ImageButton(skin, "generic_close");
        hudTable.add(closeBtn).size(64, 64).padRight(35).padBottom(-2).right().bottom();

        attachHudListeners(settingsBtn, newsBtn, closeBtn);
    }

    private void attachHudListeners(ImageButton settingsBtn, ImageButton newsBtn, ImageButton closeBtn) {
        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.SETTINGS);
            }

        });
        newsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.NEWS);
            }

        });
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }

        });
    }

    private void setupCenterTable(Stack stack) {
        Table centerTable = new Table();
        centerTable.setFillParent(true);
        stack.add(centerTable);

        TextureRegion logoRegion = textureBank.region("IMAGE_UI_MAINMENU_PVZ2_LOGO_HORIZONTAL");
        if (logoRegion != null) {
            Image logo = new Image(new TextureRegionDrawable(logoRegion));
            logo.setScaling(Scaling.fit);
            centerTable.add(logo).width(450).padBottom(40).row();
        }

        TextButton playBtn = new TextButton("PLAY", skin, "green");
        Table subButtons = new Table();
        TextButton profileBtn = new TextButton("Profile", skin, "default");
        TextButton logoutBtn = new TextButton("Logout", skin, "brown");

        subButtons.add(profileBtn).size(140, 50).padRight(20);
        subButtons.add(logoutBtn).size(140, 50);

        centerTable.add(playBtn).size(300, 70).padBottom(10).row();
        centerTable.add(subButtons);

        attachCenterListeners(playBtn, profileBtn, logoutBtn);
    }

    private void attachCenterListeners(TextButton playBtn, TextButton profileBtn, TextButton logoutBtn) {
        playBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // TODO: Temporary, implement later
                /*ScreenManager.setMenuScreen(ScreenType.GAME);*/

                AppModel.currentChapter = ChapterType.ANCIENT_EGYPT;
                AppModel.currentLevel = LevelID.STANDARD_ANCIENT_EGYPT;
                AppModel.selectionDeck.clear();
                if (AppModel.player.unlockedPlants != null) {
                    for (PlantType type : AppModel.player.unlockedPlants) {
                        AppModel.selectionDeck.put(type, false);
                    }
                }
                AppModel.gameSession = SessionBuilder.create(AppModel.currentLevel, AppModel.selectionDeck);
                ScreenManager.setMenuScreen(ScreenType.GAME_SESSION);
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
