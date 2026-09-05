package com.compileordie.pvz2.views.customelements;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.controllers.menus.game.PlantSelectionMenuController;
import com.compileordie.pvz2.controllers.menus.progression.CollectionMenuController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.game.economy.PlantCard;
import com.compileordie.pvz2.views.ScreenManager;
import com.compileordie.pvz2.views.ScreenType;
import com.compileordie.pvz2.views.helpers.ToastManager;
import pvz.libpvz.textures.TextureBank;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GamePlantSelectionModal extends BaseModal {
    private final Skin skin;
    private final TextureBank textureBank;
    private final ArrayList<PlantType> availablePlants;
    private final List<PlantCard> selectedCards = new ArrayList<>();
    private final Runnable onConfirm;
    private final Runnable onCancel;
    private boolean isConfirmed = false;
    private Table topDeckTable;
    private Table gridTable;

    public GamePlantSelectionModal(Skin skin,
                                   TextureBank textureBank,
                                   ArrayList<PlantType> availablePlants) {
        this(skin, textureBank, availablePlants, null, null);
    }

    public GamePlantSelectionModal(Skin skin,
                                   TextureBank textureBank,
                                   ArrayList<PlantType> availablePlants,
                                   Runnable onConfirm,
                                   Runnable onCancel) {
        super("Choose Your Seeds", skin);
        this.skin = skin;
        this.textureBank = textureBank;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
        this.availablePlants = availablePlants.stream()
            .filter(plantType -> AppModel.player.unlockedPlants.contains(plantType))
            .collect(Collectors.toCollection(ArrayList::new));

        getCell(contentWindow).minWidth(740).minHeight(520);

        initComponents();
        buildLayout();
        refreshUI();
    }

    private void initComponents() {
        topDeckTable = new Table();
        gridTable = new Table();

        TextButton cancelBtn = new TextButton("Cancel", skin, "brown");
        TextButton startBtn = new TextButton("Let's Rock!", skin, "green");

        buttonTable.add(cancelBtn).size(150, 50).padRight(20);
        buttonTable.add(startBtn).size(160, 50);

        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
            }
        });

        startBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectedCards.size() < Constants.Game.MIN_SELECTION_SIZE) {
                    ToastManager.showError("You must select at least " +
                        Constants.Game.MIN_SELECTION_SIZE + " plants!");
                    return;
                }

                Result<Void> result = PlantSelectionMenuController.initializeGame(selectedCards);
                if (result.isSuccess) {
                    isConfirmed = true;
                    hide();
                    if (onConfirm != null) {
                        onConfirm.run();
                    } else {
                        ScreenManager.setMenuScreen(ScreenType.GAME_SESSION);
                    }
                } else {
                    ToastManager.showError(result.errorMessage);
                }
            }
        });
    }

    @Override
    public void hide() {
        super.hide();
        if (!isConfirmed && onCancel != null) {
            onCancel.run();
        }
    }

    private void buildLayout() {
        bodyTable.top();

        Label deckTitle = new Label("Selected Plants", skin, "medium_outline");
        deckTitle.setFontScale(0.75f);
        bodyTable.add(deckTitle).left().padBottom(5).row();
        bodyTable.add(topDeckTable).padBottom(15).row();

        Label gridTitle = new Label(
            "Available Plants (Left Click: Add, Right Click: Boost, Middle Click: Upgrade)", skin, "medium_outline"
        );
        gridTitle.setFontScale(0.65f);
        bodyTable.add(gridTitle).left().padBottom(5).row();

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle(
            skin.get(ScrollPane.ScrollPaneStyle.class)
        );
        scrollStyle.vScrollKnob = skin.newDrawable(scrollStyle.vScrollKnob, Color.GOLDENROD);

        ScrollPane scrollPane = new ScrollPane(gridTable, scrollStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        bodyTable.add(scrollPane).width(800).height(240).row();
    }

    private void refreshUI() {
        refreshTopDeck();
        refreshGrid();
    }

    private void refreshTopDeck() {
        topDeckTable.clearChildren();

        for (int i = 0; i < Constants.Game.MAX_SELECTION_SIZE; i++) {
            if (i < selectedCards.size()) {
                PlantCard card = selectedCards.get(i);
                int level = AppModel.player != null ? AppModel.player.plantLevels.getOrDefault(card.plantType, 1) : 1;
                Stack cardStack = createCardView(card.plantType, level, card.isBoosted, false);

                cardStack.addListener(new ClickListener(-1) {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        int button = event.getButton();
                        if (button == Input.Buttons.LEFT) {
                            selectedCards.remove(card);
                            refreshUI();
                        } else if (button == Input.Buttons.RIGHT) {
                            toggleCardBoost(card);
                        }
                    }
                });
                topDeckTable.add(cardStack).size(100, 100).pad(4);
            } else {
                Stack emptySlot = new Stack();
                Image emptyBg = new Image(textureBank.region("IMAGE_UI_PACKETS_EMPTY_PACKET"));
                emptySlot.add(emptyBg);
                topDeckTable.add(emptySlot).size(100, 100).pad(4);
            }
        }
    }

    private void refreshGrid() {
        gridTable.clearChildren();
        int columns = 7;
        int index = 0;

        for (PlantType type : availablePlants) {
            boolean isSelected = isCardSelected(type);
            boolean hasBoost = AppModel.player != null &&
                Boolean.TRUE.equals(AppModel.player.plantBoosts.getOrDefault(type, false));
            int level = AppModel.player != null ? AppModel.player.plantLevels.getOrDefault(type, 1) : 1;

            Stack cardStack = createCardView(type, level, hasBoost, isSelected);

            cardStack.addListener(new ClickListener(-1) {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    int button = event.getButton();
                    if (button == Input.Buttons.LEFT) {
                        handleLeftClick(type);
                    } else if (button == Input.Buttons.RIGHT) {
                        handleRightClick(type, hasBoost);
                    } else if (button == Input.Buttons.MIDDLE) {
                        handleMiddleClick(type);
                    }
                }
            });

            gridTable.add(cardStack).size(100, 100).pad(6);
            if (++index % columns == 0) {
                gridTable.row();
            }
        }
    }

    private void handleLeftClick(PlantType type) {
        if (isCardSelected(type)) {
            ToastManager.showError(type + " is already in your deck!");
            return;
        }
        if (selectedCards.size() >= Constants.Game.MAX_SELECTION_SIZE) {
            ToastManager.showError("Deck is full (Max " + Constants.Game.MAX_SELECTION_SIZE + ")!");
            return;
        }

        selectedCards.add(new PlantCard(type, 0, false));
        refreshUI();
    }

    private void handleRightClick(PlantType type, boolean hasBoost) {
        if (!hasBoost) {
            ToastManager.showError("You do not have a boost for " + type + "!");
            return;
        }
        if (isCardSelected(type)) {
            ToastManager.showError(type + " is already in your deck!");
            return;
        }
        if (selectedCards.size() >= Constants.Game.MAX_SELECTION_SIZE) {
            ToastManager.showError("Deck is full (Max " + Constants.Game.MAX_SELECTION_SIZE + ")!");
            return;
        }

        selectedCards.add(new PlantCard(type, 0, true));
        refreshUI();
    }

    private void handleMiddleClick(PlantType type) {
        Result<String> result = PlantSelectionMenuController.upgradePlant(type);
        if (result.isSuccess) {
            ToastManager.showSuccess(result.data);
            refreshUI();
        } else {
            ToastManager.showError(result.errorMessage);
        }
    }

    private boolean isCardSelected(PlantType type) {
        for (PlantCard card : selectedCards) {
            if (card.plantType == type) return true;
        }
        return false;
    }

    private Stack createCardView(PlantType type, int level, boolean isBoosted, boolean isDimmed) {
        Stack card = new Stack();

        String bgKey = isBoosted ? "IMAGE_UI_PACKETS_BOOST" : "IMAGE_UI_PACKETS_SELECTED";
        Image bg = new Image(textureBank.region(bgKey));
        if (isDimmed) bg.setColor(Color.DARK_GRAY);
        card.add(bg);

        TextureRegion portraitRegion = textureBank.region(CollectionMenuController.getPlantCardAssetPath(type));
        Image portrait = portraitRegion != null ? new Image(portraitRegion) : new Image(bg.getDrawable());
        portrait.setScaling(Scaling.fit);
        if (isDimmed) portrait.setColor(Color.DARK_GRAY);

        Container<Image> portraitContainer = new Container<>(portrait);
        portraitContainer.setTransform(true);
        portraitContainer.setOrigin(Align.center);
        portraitContainer.setScale(0.95f);
        card.add(portraitContainer);

        Table topOverlay = new Table();
        topOverlay.top().right();
        Label levelLbl = new Label("Lvl " + level, skin, "medium_outline");
        levelLbl.setFontScale(0.45f);
        if (isDimmed) levelLbl.setColor(Color.GRAY);
        topOverlay.add(levelLbl).padTop(3).padRight(3);
        card.add(topOverlay);

        return card;
    }

    private void toggleCardBoost(PlantCard card) {
        if (card.isBoosted) {
            card.isBoosted = false;
            refreshUI();
        } else {
            boolean hasBoost = AppModel.player != null &&
                Boolean.TRUE.equals(AppModel.player.plantBoosts.getOrDefault(card.plantType, false));

            if (!hasBoost) {
                ToastManager.showError("You do not have a boost for " + card.plantType + "!");
                return;
            }

            card.isBoosted = true;
            refreshUI();
        }
    }
}
