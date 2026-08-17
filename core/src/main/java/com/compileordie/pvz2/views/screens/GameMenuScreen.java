package com.compileordie.pvz2.views.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.game.SessionBuilder;
import com.compileordie.pvz2.models.game.levels.ChapterType;
import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.views.ScreenManager;
import com.compileordie.pvz2.views.ScreenType;
import com.compileordie.pvz2.views.customelements.CurrencyHud;
import com.compileordie.pvz2.views.customelements.LeaderboardModal;

public class GameMenuScreen extends MenuScreen {
    private Image backgroundImage;
    private Table islandTable;
    private float currentPercent = 0.5f;

    @Override
    public void showCore() {
        float screenW = stage.getWidth();
        float screenH = stage.getHeight();

        islandTable = buildForegroundLayer(screenH);
        backgroundImage = buildBackgroundLayer(screenW, screenH);

        stage.addActor(backgroundImage);
        stage.addActor(buildShadeOverlays());
        stage.addActor(islandTable);
        stage.addActor(buildStaticHud());
        stage.addActor(buildBottomNav());
    }

    private Table buildForegroundLayer(float screenH) {
        Table table = new Table();
        table.left();
        table.setHeight(screenH);

        ChapterType[] chapters = ChapterType.values();
        for (int i = 0; i < chapters.length; i++) {
            ChapterType chapterType = chapters[i];
            float padTop = (i % 2 == 0) ? 100f : 0f;
            float padBottom = (i % 2 == 0) ? 0f : 100f;
            float padLeft = (i == 0) ? 200f : -30f;
            float padRight = (i == chapters.length - 1) ? 200f : -30f;

            table.add(createIslandWrapper(chapterType)).pad(padTop, padLeft, padBottom, padRight);
        }

        table.pack();
        table.setY(((screenH - table.getHeight()) / 2f) - 20f);
        return table;
    }

    private Table createIslandWrapper(ChapterType chapterType) {
        TextureRegionDrawable islandDrawable = new TextureRegionDrawable(textureBank.region(chapterType.image));
        ImageButton islandBtn = new ImageButton(islandDrawable);
        islandBtn.getImage().setScaling(Scaling.fit);
        islandBtn.setTransform(true);
        islandBtn.setOrigin(Align.center);

        boolean isUnlocked = chapterType == ChapterType.MINIGAME
            || AppModel.player.getUnlockedChapters().contains(chapterType);
        if (!isUnlocked) {
            islandBtn.getImage().setColor(new Color(0.1f, 0.1f, 0.1f, 1));
        }

        addIslandListeners(islandBtn, isUnlocked);

        Label nameLabel = new Label(chapterType.toString().replace("_", " "), skin, "medium_outline");
        nameLabel.setColor(isUnlocked ? Color.WHITE : new Color(0.3f, 0.3f, 0.3f, 1f));

        Table islandWrapper = new Table();
        islandWrapper.add(islandBtn).size(450, 450).row();
        islandWrapper.add(nameLabel).padTop(15);
        return islandWrapper;
    }

    private void addIslandListeners(ImageButton islandBtn, boolean isUnlocked) {
        islandBtn.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && isUnlocked) {
                    islandBtn.clearActions();
                    islandBtn.addAction(Actions.scaleTo(1.1f, 1.1f, 0.25f, Interpolation.smoother));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    islandBtn.clearActions();
                    islandBtn.addAction(Actions.scaleTo(1.0f, 1.0f, 0.25f, Interpolation.smoother));
                }
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (isUnlocked) {
                    islandBtn.getImage().setColor(Color.LIGHT_GRAY);
                    islandBtn.clearActions();
                    islandBtn.addAction(Actions.scaleTo(0.99f, 0.99f, 0.05f));
                }
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (isUnlocked) {
                    islandBtn.getImage().setColor(Color.WHITE);
                    islandBtn.clearActions();
                    islandBtn.addAction(Actions.scaleTo(1.1f, 1.1f, 0.1f, Interpolation.smooth));
                }
                super.touchUp(event, x, y, pointer, button);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isUnlocked) {
                    startLevel();
                }
            }
        });
    }

    private void startLevel() {
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

    private Image buildBackgroundLayer(float screenW, float screenH) {
        Image bg = new Image(textureBank.region("IMAGE_MAINMENU_BACKGROUND"));
        float bgAspect = bg.getDrawable().getMinWidth() / bg.getDrawable().getMinHeight();
        float bgWidth = Math.max(screenW * 1.5f, islandTable.getWidth() * 1.3f);
        float bgHeight = bgWidth / bgAspect;

        if (bgHeight < screenH) {
            bgHeight = screenH;
            bgWidth = bgHeight * bgAspect;
        }

        bg.setSize(bgWidth, bgHeight);
        bg.setPosition(0, (screenH - bgHeight) / 2f);
        return bg;
    }

    private Table buildShadeOverlays() {
        Table shadeTable = new Table();
        shadeTable.setFillParent(true);
        shadeTable.add(new Image(createGradient(true))).growX().height(150).top().row();
        shadeTable.add().expand().fill().row();
        shadeTable.add(new Image(createGradient(false))).growX().height(150).bottom();
        return shadeTable;
    }

    private Table buildStaticHud() {
        Table hudTable = new Table();
        hudTable.setFillParent(true);
        hudTable.top();

        CurrencyHud currencyHud = new CurrencyHud(skin, stage, textureBank);
        ImageButton leaderboardBtn = createLeaderboardButton();
        ImageButton travelLogBtn = new ImageButton(skin, "hud_quests");
        ImageButton greenhouseBtn = new ImageButton(skin, "hud_zg");

        hudTable.add(currencyHud).pad(25).left();
        hudTable.add().expandX();
        hudTable.add(leaderboardBtn).size(80, 80).pad(20);
        hudTable.add(travelLogBtn).pad(10);
        hudTable.add(greenhouseBtn).pad(30).right();
        return hudTable;
    }

    private ImageButton createLeaderboardButton() {
        ImageButton btn = new ImageButton(new TextureRegionDrawable(
            textureBank.region("IMAGE_UI_JOUST_MATCH_RESULTS_CROWN_COLLECT_ANIM_CROWN_COLLECT_ANIM_135X94")
        ));
        btn.getImage().setScaling(Scaling.fit);
        btn.setTransform(true);
        btn.setOrigin(Align.center);
        addLeaderboardListeners(btn);
        return btn;
    }

    private void addLeaderboardListeners(ImageButton btn) {
        btn.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    btn.clearActions();
                    btn.addAction(Actions.scaleTo(1.1f, 1.1f, 0.15f, Interpolation.smooth));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    btn.clearActions();
                    btn.addAction(Actions.scaleTo(1.0f, 1.0f, 0.15f, Interpolation.smooth));
                }
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                btn.getImage().setColor(Color.LIGHT_GRAY);
                btn.clearActions();
                btn.addAction(Actions.scaleTo(0.9f, 0.9f, 0.05f));
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                btn.getImage().setColor(Color.WHITE);
                btn.clearActions();
                btn.addAction(Actions.scaleTo(1.1f, 1.1f, 0.1f, Interpolation.smooth));
                super.touchUp(event, x, y, pointer, button);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                LeaderboardModal modal = new LeaderboardModal(skin);
                modal.show(stage);
            }
        });
    }

    private Table buildBottomNav() {
        Table bottomNavTable = new Table();
        bottomNavTable.setFillParent(true);
        bottomNavTable.bottom(); // Removed .left() so we can split the layout

        // Back Button
        ImageButton backBtn = new ImageButton(skin, "generic_close_circle");
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.MAIN);
            }
        });

        // Collection Button (using PvZ Skin)
        ImageButton collectionBtn = new ImageButton(skin, "hud_minigames");
        collectionBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.COLLECTION);
            }
        });

        // Shop Button (Custom textures for Normal and Pressed states)
        TextureRegionDrawable shopUp = new TextureRegionDrawable(
            textureBank.region("IMAGE_UI_HUD_EVENTSHOP_BUTTONS_HUD_EVENT_SHOP_NORMAL")
        );
        TextureRegionDrawable shopDown = new TextureRegionDrawable(
            textureBank.region("IMAGE_UI_HUD_EVENTSHOP_BUTTONS_HUD_EVENT_SHOP_SELECTED")
        );
        ImageButton shopBtn = new ImageButton(shopUp, shopDown);
        shopBtn.getImage().setScaling(Scaling.fit);

        shopBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.SHOP);
            }
        });

        // Layout: Left align back button, expand the middle, right align the new buttons
        bottomNavTable.add(backBtn).size(64, 64).pad(35).left();
        bottomNavTable.add().expandX(); // This invisible cell pushes everything else to the edges
        bottomNavTable.add(collectionBtn).pad(10);
        bottomNavTable.add(shopBtn).size(75, 75).pad(30).right();

        return bottomNavTable;
    }

    private TextureRegionDrawable createGradient(boolean topToBottom) {
        Pixmap pixmap = new Pixmap(1, 2, Pixmap.Format.RGBA8888);
        if (topToBottom) {
            pixmap.setColor(new Color(0, 0, 0, 0.85f));
            pixmap.drawPixel(0, 0);
            pixmap.setColor(new Color(0, 0, 0, 0.0f));
            pixmap.drawPixel(0, 1);
        } else {
            pixmap.setColor(new Color(0, 0, 0, 0.0f));
            pixmap.drawPixel(0, 0);
            pixmap.setColor(new Color(0, 0, 0, 0.85f));
            pixmap.drawPixel(0, 1);
        }
        Texture tex = new Texture(pixmap);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return new TextureRegionDrawable(tex);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        float screenW = stage.getWidth();

        float targetPercent = MathUtils.clamp(Gdx.input.getX() / screenW, 0f, 1f);
        currentPercent = MathUtils.lerp(currentPercent, targetPercent, Math.min(1f, 3f * delta));

        float excessBgWidth = Math.max(0, backgroundImage.getWidth() - screenW);
        float excessIslandWidth = Math.max(0, islandTable.getWidth() - screenW);

        backgroundImage.setX(-(excessBgWidth * currentPercent));
        islandTable.setX(-(excessIslandWidth * currentPercent));
    }
}
