package com.compileordie.pvz2.views.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.compileordie.pvz2.controllers.menus.game.GameScreenController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.game.SessionBuilder;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.views.ScreenManager;
import com.compileordie.pvz2.views.ScreenType;
import com.compileordie.pvz2.views.game.GameScreenConstants;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.BorderedTable;
import pvz.skin.PvzSkin;

/**
 * Pause menu, game-end panel, and sun HUD (extracted from GameScreen).
 */
public final class GameScreenUI {
    public Stage uiStage;
    public static boolean isPaused = false;
    public ImageButton pauseButton;
    public Table pauseOverlayContainer;
    public Texture pauseOverlayTexture;
    public Skin skin;
    public Table gameEndOverlayContainer;
    public Label sunAmountLabel;
    private final Runnable onResume;
    private final Runnable onRestart;
    private final Runnable onExitToMain;

    public GameScreenUI(Runnable onResume, Runnable onRestart, Runnable onExitToMain) {
        this.onResume = onResume;
        this.onRestart = onRestart;
        this.onExitToMain = onExitToMain;
    }

    @SuppressWarnings("checkstyle:LineLength")
    public void setupPauseUI() {
        uiStage = new Stage(new ScreenViewport());
        skin = PvzSkin.get();

        Table root = new Table();
        root.setFillParent(true);
        root.top().right();
        uiStage.addActor(root);

        ImageButton.ImageButtonStyle pauseStyle = new ImageButton.ImageButtonStyle(
            skin.get("ingame_pause", ImageButton.ImageButtonStyle.class)
        );
        if (pauseStyle.imageChecked != null) {
            pauseStyle.imageDown = pauseStyle.imageChecked;
        }
        pauseStyle.checked = null;
        pauseStyle.imageChecked = null;
        pauseStyle.checkedOver = null;
        pauseStyle.imageCheckedOver = null;
        pauseStyle.checkedDown = null;
        pauseStyle.imageCheckedDown = null;

        pauseButton = new ImageButton(pauseStyle);
        root.add(pauseButton).pad(GameScreenConstants.PAUSE_BUTTON_PAD);
        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setPaused(true);
            }
        });
    }


    /**
     * Full pause panel with fog decoration (same logic as original buildPauseMenuPanel).
     */
    public void buildPauseMenuPanelWithFog(TextureBank textureBank) {
        if (pauseOverlayContainer != null) {
            pauseOverlayContainer.remove();
        }
        if (pauseOverlayTexture == null) {
            pauseOverlayTexture = createSolidTexture(new Color(0f, 0f, 0f, 0.55f));
        }
        pauseOverlayContainer = new Table();
        pauseOverlayContainer.setFillParent(true);
        pauseOverlayContainer.setBackground(
            new TextureRegionDrawable(new TextureRegion(pauseOverlayTexture)));

        BorderedTable panel = new BorderedTable();
        addPauseButtons(panel);

        Stack panelStack = new Stack();
        TextureRegion fogRegion = (textureBank != null)
            ? textureBank.region(GameScreenConstants.PAUSE_FOG_DECORATION_REGION) : null;
        float fogWidth = 0f;
        float fogHeight = 0f;
        if (fogRegion != null) {
            fogWidth = GameScreenConstants.PAUSE_PANEL_WIDTH * GameScreenConstants.PAUSE_FOG_WIDTH_RATIO;
            float aspect = (float) fogRegion.getRegionWidth() / (float) fogRegion.getRegionHeight();
            fogHeight = fogWidth / aspect;
        } else {
            Gdx.app.error("PVZ-DEBUG",
                "❌ تصویر تزئینی مه ('" + GameScreenConstants.PAUSE_FOG_DECORATION_REGION + "') پیدا نشد!");
        }
        float overlapAmount = fogHeight * GameScreenConstants.PAUSE_FOG_OVERLAP_RATIO;
        Table panelWrapper = new Table();
        panelWrapper.top();
        if (overlapAmount > 0) {
            panelWrapper.add().height(overlapAmount).row();
        }
        panelWrapper.add(panel)
            .size(GameScreenConstants.PAUSE_PANEL_WIDTH, GameScreenConstants.PAUSE_PANEL_HEIGHT);
        panelStack.add(panelWrapper);
        if (fogRegion != null) {
            Table fogWrapper = new Table();
            fogWrapper.top();
            fogWrapper.add(new Image(fogRegion)).size(fogWidth, fogHeight);
            panelStack.add(fogWrapper);
        }
        pauseOverlayContainer.add(panelStack)
            .size(GameScreenConstants.PAUSE_PANEL_WIDTH,
                GameScreenConstants.PAUSE_PANEL_HEIGHT + overlapAmount);
        pauseOverlayContainer.setVisible(false);
        pauseOverlayContainer.setTouchable(Touchable.disabled);
        uiStage.addActor(pauseOverlayContainer);
    }

    private void addPauseButtons(BorderedTable panel) {
        TextButton resumeButton = new TextButton(GameScreenConstants.RESUME_BUTTON_TEXT, skin, "green");
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setPaused(false);
            }
        });
        TextButton restartButton = new TextButton(GameScreenConstants.RESTART_BUTTON_TEXT, skin, "brown");
        restartButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AppModel.gameSession = SessionBuilder.create(AppModel.currentLevel, AppModel.selectionDeck);
                setPaused(false);
            }
        });
        TextButton saveExitButton = new TextButton(GameScreenConstants.SAVE_EXIT_BUTTON_TEXT, skin, "brown");
        saveExitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AppModel.wonLastGame = null;
                AppModel.clearSessionData();
                new UserDatabase().save(AppModel.player);
                ScreenManager.setMenuScreen(ScreenType.GAME);
            }
        });
        panel.add(resumeButton)
            .size(GameScreenConstants.PAUSE_MENU_BUTTON_WIDTH, GameScreenConstants.PAUSE_MENU_BUTTON_HEIGHT)
            .pad(GameScreenConstants.PAUSE_MENU_BUTTON_PAD).row();
        panel.add(restartButton)
            .size(GameScreenConstants.PAUSE_MENU_BUTTON_WIDTH, GameScreenConstants.PAUSE_MENU_BUTTON_HEIGHT)
            .pad(GameScreenConstants.PAUSE_MENU_BUTTON_PAD).row();
        panel.add(saveExitButton)
            .size(GameScreenConstants.PAUSE_MENU_BUTTON_WIDTH, GameScreenConstants.PAUSE_MENU_BUTTON_HEIGHT)
            .pad(GameScreenConstants.PAUSE_MENU_BUTTON_PAD).row();
    }

    public void setupGameUI(TextureBank textureBank) {
        Table sunTable = new Table();
        sunTable.setFillParent(true);
        sunTable.top().left();

        // 1. Game Resources HUD & Shovel (Pinned Top-Left)
        Table leftUITable = new Table();
        leftUITable.setFillParent(true);
        leftUITable.top().left();

        GameCurrencyHud currencyHud = new GameCurrencyHud(skin, uiStage, textureBank);
        leftUITable.add(currencyHud).pad(15).left().row();

        Table toolsTable = new Table();
        toolsTable.left();

        ImageButton shovelButton = new ImageButton(skin, "ingame_shovel");
        shovelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameScreenController.toggleShovel();
            }
        });
        toolsTable.add(shovelButton).padRight(12);

        PlantFoodBank plantFoodBank = new PlantFoodBank(textureBank);
        toolsTable.add(plantFoodBank);

        leftUITable.add(toolsTable).padLeft(15).left();

        uiStage.addActor(leftUITable);

        Table cardBarTable = new Table();
        cardBarTable.setFillParent(true);
        cardBarTable.top();
        PlantCardBar cardBar = new PlantCardBar(skin, textureBank, AppModel.gameSession.gameBoard.economyManager);
        cardBarTable.add(cardBar).padTop(10);
        uiStage.addActor(cardBarTable);
    }

    public void showGameEndPanel(boolean isWin) {
        if (uiStage == null) return;
        gameEndOverlayContainer = new Table();
        gameEndOverlayContainer.setFillParent(true);
        gameEndOverlayContainer.setBackground(
            new TextureRegionDrawable(new TextureRegion(pauseOverlayTexture)));

        BorderedTable panel = new BorderedTable();
        String titleText = isWin ? "YOU WON!" : "GAME OVER!";
        Label titleLabel = new Label(titleText, skin, "medium_outline");
        titleLabel.setFontScale(1.8f);
        panel.add(titleLabel).padBottom(20).row();

        if (isWin) {
            TextButton exitButton = new TextButton("EXIT", skin, "green");
            exitButton.getLabel().setFontScale(1.5f);
            exitButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    AppModel.wonLastGame = null;
                    ScreenManager.setMenuScreen(ScreenType.GAME);
                }
            });
            panel.add(exitButton)
                .size(GameScreenConstants.PAUSE_MENU_BUTTON_WIDTH * 1.2f,
                    GameScreenConstants.PAUSE_MENU_BUTTON_HEIGHT * 1.2f)
                .pad(GameScreenConstants.PAUSE_MENU_BUTTON_PAD).row();
        } else {
            addLoseButtons(panel);
        }
        gameEndOverlayContainer.add(panel)
            .size(GameScreenConstants.PAUSE_PANEL_WIDTH + 100,
                GameScreenConstants.PAUSE_PANEL_HEIGHT + 100);
        uiStage.addActor(gameEndOverlayContainer);
        gameEndOverlayContainer.toFront();
    }

    private void addLoseButtons(BorderedTable panel) {
        TextButton tryAgainButton = new TextButton("TRY AGAIN", skin, "green");
        tryAgainButton.getLabel().setFontScale(1.5f);
        tryAgainButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AppModel.wonLastGame = null;
                AppModel.gameSession = SessionBuilder.create(AppModel.currentLevel, AppModel.selectionDeck);
                if (onRestart != null) onRestart.run();
                setPaused(false);
                if (gameEndOverlayContainer != null) {
                    gameEndOverlayContainer.remove();
                }
            }
        });
        TextButton exitButton = new TextButton("EXIT", skin, "brown");
        exitButton.getLabel().setFontScale(1.5f);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AppModel.wonLastGame = null;
                ScreenManager.setMenuScreen(ScreenType.GAME);
            }
        });
        panel.add(tryAgainButton)
            .size(GameScreenConstants.PAUSE_MENU_BUTTON_WIDTH * 1.2f,
                GameScreenConstants.PAUSE_MENU_BUTTON_HEIGHT * 1.2f)
            .pad(GameScreenConstants.PAUSE_MENU_BUTTON_PAD).row();
        panel.add(exitButton)
            .size(GameScreenConstants.PAUSE_MENU_BUTTON_WIDTH * 1.2f,
                GameScreenConstants.PAUSE_MENU_BUTTON_HEIGHT * 1.2f)
            .pad(GameScreenConstants.PAUSE_MENU_BUTTON_PAD).row();
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
        if (pauseOverlayContainer != null) {
            pauseOverlayContainer.setVisible(paused);
            pauseOverlayContainer.setTouchable(paused ? Touchable.enabled : Touchable.disabled);
            if (paused) {
                pauseOverlayContainer.toFront();
            }
        }
    }

    public void updateSunLabel(int amount) {
        if (sunAmountLabel != null) {
            sunAmountLabel.setText(String.format("%04d", amount));
        }
    }

    public void actAndDraw(float delta) {
        if (uiStage != null) {
            uiStage.act(delta);
            uiStage.draw();
        }
    }

    public void resize(int width, int height) {
        if (uiStage != null) {
            uiStage.getViewport().update(width, height, true);
        }
    }

    public Texture createSolidTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public void dispose() {
        if (uiStage != null) uiStage.dispose();
        if (pauseOverlayTexture != null) pauseOverlayTexture.dispose();
    }
}
