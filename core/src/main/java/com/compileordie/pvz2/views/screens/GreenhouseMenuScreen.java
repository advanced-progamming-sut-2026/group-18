package com.compileordie.pvz2.views.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.TimeUtils;
import com.compileordie.pvz2.controllers.menus.progression.GreenhouseMenuController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.models.missions.Pot;
import com.compileordie.pvz2.views.ScreenManager;
import com.compileordie.pvz2.views.ScreenType;
import com.compileordie.pvz2.views.customelements.CurrencyHud;
import com.compileordie.pvz2.views.customelements.DebugGrowthModal;
import com.compileordie.pvz2.views.game.PlantAssetManager;
import com.compileordie.pvz2.views.helpers.ToastManager;
import pvz.libpvz.pam.ClipRef;

public class GreenhouseMenuScreen extends MenuScreen {
    private Table gridTable;
    private PlantAssetManager plantAssetManager;

    @Override
    public void showCore() {
        plantAssetManager = new PlantAssetManager();

        Stack stack = new Stack();
        stack.setFillParent(true);
        stage.addActor(stack);

        // 1. Static Background
        Image bg = new Image(new TextureRegionDrawable(textureBank.region("IMAGE_BACKGROUNDS_ZEN_GARDEN")));
        bg.setScaling(Scaling.fill);
        stack.add(bg);

        // 2. Main Grid
        gridTable = new Table();
        gridTable.padTop(120);
        stack.add(gridTable);

        // 3. Persistent Overlays
        setupTopHud();
        setupBottomNav();

        refreshGrid();
    }

    private void setupTopHud() {
        Table hudTable = new Table();
        hudTable.setFillParent(true);
        hudTable.top().left();

        CurrencyHud currencyHud = new CurrencyHud(skin, stage, textureBank);
        hudTable.add(currencyHud).pad(30).left();
        stage.addActor(hudTable);
    }

    private void setupBottomNav() {
        Table bottomNavTable = new Table();
        bottomNavTable.setFillParent(true);
        bottomNavTable.bottom().left();

        ImageButton backBtn = new ImageButton(skin, "generic_close_circle");
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.GAME);
            }
        });

        bottomNavTable.add(backBtn).size(64, 64).pad(35);
        stage.addActor(bottomNavTable);
    }

    private void refreshGrid() {
        gridTable.clearChildren();

        int columns = 4;
        for (int i = 0; i < 12; i++) {
            gridTable.add(buildPotSlot(i)).size(138, 120).pad(15, 10, 15, 10);
            if ((i + 1) % columns == 0) {
                gridTable.row();
            }
        }
    }

    private Stack buildPotSlot(int index) {
        Stack slotStack = new Stack();
        boolean isLocked = index >= AppModel.player.greenhousePots.size();

        if (isLocked) {
            return buildLockedSlot(slotStack);
        }

        Pot pot = AppModel.player.greenhousePots.get(index);
        long now = TimeUtils.millis();
        boolean isReady = !pot.isEmpty && now >= pot.readyTimeMillis;

        // Base Pot Image
        String potTex = isReady ?
            "IMAGE_ZEN_GARDEN_GROWING_PLANT_SLOT_GROWING_PLANT_SLOT_184X161_2" :
            "IMAGE_ZEN_GARDEN_GROWING_PLANT_SLOT_GROWING_PLANT_SLOT_184X161";
        slotStack.add(new Image(textureBank.region(potTex)));

        if (pot.isEmpty) {
            slotStack.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    handleResult(GreenhouseMenuController.plantPlot(index));
                }
            });
        } else {
            buildOccupiedSlot(index, pot, isReady, slotStack);
            addDebugListener(index, isReady, slotStack);
        }

        return slotStack;
    }

    private Stack buildLockedSlot(Stack slotStack) {
        Image emptyPot = new Image(
            textureBank.region("IMAGE_ZEN_GARDEN_GROWING_PLANT_SLOT_GROWING_PLANT_SLOT_184X161")
        );
        emptyPot.setColor(Color.DARK_GRAY);
        slotStack.add(emptyPot);

        Image lockIcon = new Image(textureBank.region("IMAGE_UI_PERKS_PERK_ICON_LOCKED"));
        lockIcon.setScaling(Scaling.none);
        Table lockTable = new Table();
        lockTable.add(lockIcon).padBottom(30);
        slotStack.add(lockTable);

        slotStack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ToastManager.showMessage("This pot is locked. Visit the shop!");
                ScreenManager.setMenuScreen(ScreenType.SHOP);
            }
        });
        return slotStack;
    }

    private void buildOccupiedSlot(int index, Pot pot, boolean isReady, Stack slotStack) {
        // Plant Animation
        ClipRef clip = pot.isMarigold ?
            plantAssetManager.loadMarigoldClip() : plantAssetManager.loadPlantClip(pot.targetPlant);
        Rectangle bounds = pot.isMarigold ?
            plantAssetManager.getMarigoldBounds() : plantAssetManager.getBounds(pot.targetPlant);
        PamActor animActor = new PamActor(plantAssetManager, clip, bounds);

        Container<PamActor> animContainer = new Container<>(animActor);
        animContainer.setTransform(true);
        animContainer.setOrigin(Align.center);
        animContainer.setScale(isReady ? 1.0f : 0.85f);
        animContainer.padBottom(80);
        slotStack.add(animContainer);

        // Name Label
        String plantName = pot.isMarigold ? "Marigold" : pot.targetPlant.toString();
        Label nameLbl = new Label(plantName, skin, "medium_outline");
        nameLbl.setAlignment(Align.center);
        nameLbl.setFontScale(0.5f);

        Table nameTable = new Table();
        nameTable.bottom(); // Anchors the table to the bottom of the Stack
        nameTable.add(nameLbl).padBottom(isReady ? 15 : 50);
        slotStack.add(nameTable);

        if (isReady) {
            slotStack.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    handleResult(GreenhouseMenuController.collectPot(index));
                }
            });
        } else {
            // Growing State Overlay (Timer & Speed up)
            slotStack.add(buildGrowingOverlay(index, pot));
        }
    }

    private void addDebugListener(int index, boolean isReady, Stack slotStack) {
        slotStack.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button == Input.Buttons.RIGHT || button == Input.Buttons.MIDDLE) {
                    if (!AppModel.player.debugMode) {
                        ToastManager.showError("Debug mode is disabled! Enable in Settings!");
                    } else if (!isReady) {
                        DebugGrowthModal modal = new DebugGrowthModal(skin, index, () -> refreshGrid());
                        modal.show(stage);
                    }
                    return true;
                }
                return super.touchDown(event, x, y, pointer, button);
            }
        });
    }

    private Table buildGrowingOverlay(int index, Pot pot) {
        Table overlay = new Table();
        overlay.bottom();

        // Semi-transparent background
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(new Color(0, 0, 0, 0.7f));
        pix.fill();
        Table timeTable = new Table();
        timeTable.setBackground(new TextureRegionDrawable(new Texture(pix)));
        pix.dispose();

        Label timeLbl = new Label("", skin, "default");

        // Timer Action
        timeLbl.addAction(Actions.forever(Actions.sequence(
            Actions.run(() -> {
                long now = TimeUtils.millis();
                if (now >= pot.readyTimeMillis) {
                    refreshGrid();
                } else {
                    long diff = pot.readyTimeMillis - now;
                    long hours = diff / (60 * 60 * 1000);
                    long minutes = (diff % (60 * 60 * 1000)) / (60 * 1000);
                    timeLbl.setText(String.format("%02dh %02dm", hours, minutes));
                }
            }),
            Actions.delay(1f)
        )));

        ImageButton diamondBtn = new ImageButton(
            new TextureRegionDrawable(textureBank.region("IMAGE_UI_QUESTS_GEM_ICON"))
        );
        diamondBtn.getImage().setScaling(Scaling.fit);
        diamondBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleResult(GreenhouseMenuController.growPot(index));
            }
        });

        timeTable.add(timeLbl).padLeft(5).padRight(10);
        timeTable.add(diamondBtn).size(24, 24).padRight(5);

        overlay.add(timeTable).fillX().expandX().height(35).padBottom(15);
        return overlay;
    }

    private void handleResult(Result<String> result) {
        if (result.isSuccess) {
            ToastManager.showSuccess(result.data);
            refreshGrid();
        } else {
            ToastManager.showError(result.errorMessage);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (plantAssetManager != null) {
            plantAssetManager.dispose();
        }
    }
}
