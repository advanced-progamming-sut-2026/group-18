package com.compileordie.pvz2.views.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.compileordie.pvz2.controllers.menus.game.GameScreenController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.repositories.databases.UserDatabase;
import com.compileordie.pvz2.views.helpers.ToastManager;
import pvz.libpvz.textures.TextureBank;

public class PlantFoodBank extends Table {
    public static final int MAX_PLANT_FOOD = 5;
    private ImageButton buyButton;
    private final TextureRegion fillingSlotRegion;
    private final TextureRegion filledSlotRegion;
    private final Stack[] slotStacks = new Stack[MAX_PLANT_FOOD];

    public PlantFoodBank(TextureBank textureBank) {
        this.fillingSlotRegion = textureBank.region("IMAGE_UI_HUD_INGAME_PLANTFOOD_BANK_FILLING_SLOT");
        this.filledSlotRegion = textureBank.region("IMAGE_UI_HUD_INGAME_PLANTFOOD_BANK_FILLED_SLOT");

        left().center();
        buildUI(textureBank);
    }

    private void buildUI(TextureBank textureBank) {
        Image bankBg = createBankBackground(textureBank);
        ImageButton leafButton = createLeafButton(textureBank);
        Table slotsTable = createSlotsTable();

        Table contentOverlay = new Table();
        contentOverlay.setFillParent(true);
        contentOverlay.left().center();
        contentOverlay.add(leafButton).size(48, 48).padLeft(20);
        contentOverlay.add(slotsTable).padLeft(4).expandX().left();

        Stack bankStack = new Stack();
        bankStack.add(bankBg);
        bankStack.add(contentOverlay);
        add(bankStack).left();

        buyButton = createBuyButton(textureBank);
        add(buyButton).padLeft(-8);
    }

    private Image createBankBackground(TextureBank textureBank) {
        TextureRegion bankBgRegion = textureBank.region("IMAGE_UI_HUD_INGAME_PLANTFOOD_BANK");
        Image bankBg = new Image(bankBgRegion != null ? new TextureRegionDrawable(bankBgRegion) : null);
        bankBg.setScaling(Scaling.none);
        return bankBg;
    }

    private ImageButton createLeafButton(TextureBank textureBank) {
        TextureRegion leafUpRegion = textureBank.region("IMAGE_UI_HUD_INGAME_PLANTFOOD_BUTTON");
        TextureRegion leafDownRegion = textureBank.region("IMAGE_UI_HUD_INGAME_PLANTFOOD_BUTTON_DOWN");

        ImageButtonStyle leafStyle = new ImageButtonStyle();
        if (leafUpRegion != null) {
            leafStyle.imageUp = new TextureRegionDrawable(leafUpRegion);
            leafStyle.imageDown = new TextureRegionDrawable(leafDownRegion != null ? leafDownRegion : leafUpRegion);
        }

        ImageButton leafButton = new ImageButton(leafStyle);
        leafButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameScreenController.togglePlantFood();
            }
        });
        return leafButton;
    }

    private Table createSlotsTable() {
        Table slotsTable = new Table();
        slotsTable.left().center();
        for (int i = 0; i < MAX_PLANT_FOOD; i++) {
            slotStacks[i] = new Stack();

            Image fillingSlot;
            if (fillingSlotRegion != null) {
                fillingSlot = new Image(new TextureRegionDrawable(fillingSlotRegion));
            } else {
                fillingSlot = new Image((Drawable) null);
            }
            fillingSlot.setScaling(Scaling.none);
            slotStacks[i].add(fillingSlot);

            Image filledSlot;
            if (filledSlotRegion != null) {
                filledSlot = new Image(new TextureRegionDrawable(filledSlotRegion));
            } else {
                filledSlot = new Image((Drawable) null);
            }
            filledSlot.setScaling(Scaling.none);
            slotStacks[i].add(filledSlot);

            slotStacks[i].setVisible(false);
            slotsTable.add(slotStacks[i]).padRight(-6);
        }
        return slotsTable;
    }

    private ImageButton createBuyButton(TextureBank textureBank) {
        TextureRegion buyUpRegion = textureBank.region("IMAGE_UI_HUD_INGAME_COIN_BUY");
        TextureRegion buyDownRegion = textureBank.region("IMAGE_UI_HUD_INGAME_COIN_BUY_DOWN");

        ImageButtonStyle buyStyle = new ImageButtonStyle();
        if (buyUpRegion != null) {
            buyStyle.imageUp = new TextureRegionDrawable(buyUpRegion);
            buyStyle.imageDown = new TextureRegionDrawable(buyDownRegion != null ? buyDownRegion : buyUpRegion);
        }

        ImageButton button = new ImageButton(buyStyle);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (AppModel.player == null || !AppModel.player.debugMode) {
                    ToastManager.showError("Debug mode is disabled!");
                    return;
                }
                if (AppModel.player.plantFoodCount >= MAX_PLANT_FOOD) {
                    ToastManager.showError("Plant food is already full!");
                    return;
                }
                AppModel.player.plantFoodCount++;
                new UserDatabase(AppModel.player.username).save(AppModel.player);
                ToastManager.showSuccess("Plant food added!");
            }
        });
        return button;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        int currentFood = AppModel.player != null ? Math.min(MAX_PLANT_FOOD, AppModel.player.plantFoodCount) : 0;
        for (int i = 0; i < MAX_PLANT_FOOD; i++) {
            if (slotStacks[i] != null) {
                slotStacks[i].setVisible(i < currentFood);
            }
        }

        if (buyButton != null && buyButton.getImage() != null && AppModel.player != null) {
            buyButton.getImage().setColor(AppModel.player.debugMode ? Color.WHITE : Color.GRAY);
        }
    }
}
