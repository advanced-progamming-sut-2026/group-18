package com.compileordie.pvz2.views.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.compileordie.pvz2.controllers.menus.progression.ShopMenuController;
import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.missions.shop.CurrencyType;
import com.compileordie.pvz2.models.missions.shop.DailyOffer;
import com.compileordie.pvz2.models.missions.shop.ShopItem;
import com.compileordie.pvz2.views.ScreenManager;
import com.compileordie.pvz2.views.ScreenType;
import com.compileordie.pvz2.views.customelements.CurrencyHud;
import com.compileordie.pvz2.views.customelements.PlantSelectionModal;
import com.compileordie.pvz2.views.customelements.ShopConfirmationModal;
import com.compileordie.pvz2.views.game.PlantAssetManager;
import com.compileordie.pvz2.views.helpers.PamActor;
import com.compileordie.pvz2.views.helpers.ToastManager;
import pvz.libpvz.pam.ClipRef;
import pvz.skin.BorderedTable;

public class ShopMenuScreen extends MenuScreen {
    private Table catalogTable;
    private PlantAssetManager plantAssetManager;

    @Override
    public void showCore() {
        Stack stack = new Stack();
        stack.setFillParent(true);
        stage.addActor(stack);

        // MAIN BOARD
        BorderedTable board = new BorderedTable();
        stack.add(board);

        Label title = new Label("Crazy Dave's Shop", skin, "big_outline");
        board.add(title).padBottom(15).center().row();

        setupCatalogScroll(board);
        setupTopHud();
        setupBottomNav();

        // POPULATE
        plantAssetManager = new PlantAssetManager();
        refreshCatalog();
    }

    private void setupCatalogScroll(BorderedTable board) {
        // HORIZONTAL CATALOG LIST
        catalogTable = new Table();
        catalogTable.left(); // Align cards from the left

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle(
            skin.get(ScrollPane.ScrollPaneStyle.class)
        );

        // FIX: Only attempt to tint the horizontal knob if it actually exists in the skin!
        if (scrollStyle.hScrollKnob != null) {
            scrollStyle.hScrollKnob = skin.newDrawable(scrollStyle.hScrollKnob, Color.GOLDENROD);
        }

        ScrollPane scrollPane = new ScrollPane(catalogTable, scrollStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(false, true);

        board.add(scrollPane).width(900).height(420).pad(10).row();
    }

    private void setupTopHud() {
        // TOP HUD (Transparent & Matched Padding)
        Table hudTable = new Table();
        hudTable.setFillParent(true);
        hudTable.top().left();

        CurrencyHud currencyHud = new CurrencyHud(skin, stage, textureBank);
        currencyHud.setBackground((Drawable) null); // Remove the background
        hudTable.add(currencyHud).pad(30).left(); // Exact padding from GameMenuScreen
        stage.addActor(hudTable);
    }

    private void setupBottomNav() {
        // BOTTOM NAV
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

    private void refreshCatalog() {
        catalogTable.clearChildren();

        // Add Daily Offer (If Available)
        Result<DailyOffer> dailyRes = ShopMenuController.getDailyOffer();
        if (dailyRes.isSuccess && !dailyRes.data.purchased) {
            DailyOffer offer = dailyRes.data;
            String itemName = "10x " + offer.targetPlant + " Seeds";
            String seedIcon = offer.targetPlant.name();
            String timeLeftTag = getDailyOfferTimeLeft();

            Table dailyCard = buildItemCard(
                "Daily Offer",
                timeLeftTag,
                itemName,
                offer.price,
                "IMAGE_UI_QUESTS_COIN_ICON",
                seedIcon,
                /*"IMAGE_UI_SUNFLOWER",*/
                true,
                () -> promptDailyPurchase(itemName, offer.price, "IMAGE_UI_QUESTS_COIN_ICON")
            );
            catalogTable.add(dailyCard).size(220, 360).padRight(15);
        }

        // Add Permanent Items
        for (ShopItem item : ShopMenuController.getPermanentItems()) {
            String currencyIcon = item.currencyType == CurrencyType.COINS ?
                "IMAGE_UI_QUESTS_COIN_ICON" : "IMAGE_UI_QUESTS_GEM_ICON";
            String desc = item.itemId.equals("exchange") ?
                "Yield: " + item.quantity + " Coins" : "Yield: x" + item.quantity;
            String itemIcon = getIconForItemId(item.itemId);

            Table card = buildItemCard(
                item.displayName,
                null,
                desc,
                item.price,
                currencyIcon,
                itemIcon,
                false,
                () -> handleItemClick(item, currencyIcon)
            );
            catalogTable.add(card).size(220, 360).padRight(15);
        }
    }

    /**
     * Helper to map shop items to texture regions.
     * Update these strings to match your actual texture bank keys.
     */
    private String getIconForItemId(String itemId) {
        return switch (itemId) {
            case "pot" -> "IMAGE_UI_SPROUTS_STACK_5"; // Pot texture
            case "plant_food" -> "IMAGE_GRAVESTONES_DARK_PLANTFOOD_DARK_PLANTFOOD_132X160"; // Plant Food texture
            case "exchange" -> "IMAGE_UI_COINS_STACK_6";
            case "random_seed" -> "IMAGE_UI_PACKETS_ELECTRICCURRANT"; // Mystery Seed
            case "selected_seed" -> "IMAGE_UI_PACKETS_PRIMALPEASHOOTER"; // Blank Seed
            default -> "IMAGE_UI_SUNFLOWER";
        };
    }

    private Table buildItemCard(String title,
                                String tag,
                                String desc,
                                int price,
                                String currencyTex,
                                String icon,
                                boolean isDailyOffer,
                                Runnable onClick) {
        Table card = new Table();
        card.setBackground(new TextureRegionDrawable(textureBank.region("IMAGE_DANGERROOM_CARD_FACE")));
        card.pad(30);

        addCardTag(card, tag);
        addCardTitle(card, title);

        // --- SPRING 1: Pushes Title UP and Image DOWN ---
        card.add().expandY().row();

        addCardIcon(card, isDailyOffer, icon);

        // --- SPRING 2: Pushes Image UP and Description/Button DOWN ---
        card.add().expandY().row();

        addCardDescription(card, desc);
        addCardBuyButton(card, price, currencyTex, onClick);

        return card;
    }

    private void addCardTag(Table card, String tag) {
        if (tag != null) {
            Label tagLbl = new Label(tag, skin, "default");
            tagLbl.setColor(Color.DARK_GRAY);
            card.add(tagLbl).padBottom(5).row();
        } else {
            // Invisible spacer so cards without tags stay horizontally aligned
            card.add().height(20).padBottom(5).row();
        }
    }

    private void addCardTitle(Table card, String title) {
        Label titleLbl = new Label(title, skin, "medium_outline");
        titleLbl.setFontScale(0.85f);
        titleLbl.setAlignment(Align.center);
        titleLbl.setWrap(true);
        card.add(titleLbl).width(180).padBottom(5).center().row();
    }

    private void addCardIcon(Table card, boolean isDailyOffer, String icon) {
        if (isDailyOffer) {
            PlantType plantType = ShopMenuController.getDailyOffer().data.targetPlant;
            ClipRef dailyPlantClip = plantAssetManager.loadPlantClip(plantType);
            Rectangle plantBounds = plantAssetManager.getBounds(plantType);
            PamActor iconActor = new PamActor(plantAssetManager, dailyPlantClip, plantBounds);
            card.add(iconActor).size(140, 140).row();
        } else {
            Image img = new Image(textureBank.region(icon));
            img.setScaling(Scaling.fit);
            card.add(img).size(140, 140).row(); // Removed expandY() from the image itself
        }
    }

    private void addCardDescription(Table card, String desc) {
        Label descLbl = new Label(desc, skin, "default");
        descLbl.setFontScale(0.85f);
        descLbl.setWrap(true);
        descLbl.setAlignment(Align.center);
        descLbl.setColor(Color.DARK_GRAY); // Kept the dark gray for contrast!
        card.add(descLbl).width(180).padBottom(10).row();
    }

    private void addCardBuyButton(Table card, int price, String currencyTex, Runnable onClick) {
        Button.ButtonStyle style = skin.get("green", TextButton.TextButtonStyle.class);
        Button buyBtn = new Button(style);

        Label priceLbl = new Label(String.valueOf(price), skin, "medium_outline");
        Image curIcon = new Image(textureBank.region(currencyTex));
        curIcon.setScaling(Scaling.fit);

        buyBtn.add(priceLbl).padRight(8);
        buyBtn.add(curIcon).size(28, 28);

        buyBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onClick.run();
            }
        });

        card.add(buyBtn).size(150, 50).padBottom(0).row();
    }

    private void promptDailyPurchase(String itemName, int price, String currencyTex) {
        ShopConfirmationModal modal = new ShopConfirmationModal(skin, textureBank, itemName, price, currencyTex, () -> {
            Result<String> result = ShopMenuController.buyDailyOffer();
            handlePurchaseResult(result);
        });
        modal.show(stage);
    }

    private void handleItemClick(ShopItem item, String currencyTex) {
        if (item.itemId.equals("selected_seed")) {
            PlantSelectionModal selector = new PlantSelectionModal(skin, plantAssetManager, plant -> {
                String itemName = item.quantity + "x " + plant.name() + " Seeds";
                ShopConfirmationModal modal = new ShopConfirmationModal(skin,
                    textureBank,
                    itemName,
                    item.price,
                    currencyTex,
                    () -> {
                        Result<String> result = ShopMenuController.buyCatalogItem(item.itemId, 1, plant);
                        handlePurchaseResult(result);
                    });
                modal.show(stage);
            });
            selector.show(stage);
        } else {
            ShopConfirmationModal modal = new ShopConfirmationModal(skin,
                textureBank,
                item.displayName,
                item.price,
                currencyTex,
                () -> {
                    Result<String> result = ShopMenuController.buyCatalogItem(item.itemId, 1, null);
                    handlePurchaseResult(result);
                });
            modal.show(stage);
        }
    }

    private void handlePurchaseResult(Result<String> result) {
        if (result.isSuccess) {
            ToastManager.showSuccess(result.data);
            refreshCatalog(); // Refresh to immediately hide sold-out daily offers
        } else {
            ToastManager.showError(result.errorMessage);
        }
    }

    private String getDailyOfferTimeLeft() {
        java.time.LocalTime now = java.time.LocalTime.now();
        int hours = 23 - now.getHour();
        int minutes = 59 - now.getMinute();
        return String.format("%02dh %02dm left", hours, minutes);
    }
}
