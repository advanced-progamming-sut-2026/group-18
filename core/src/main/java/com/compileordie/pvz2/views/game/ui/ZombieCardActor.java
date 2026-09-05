package com.compileordie.pvz2.views.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.compileordie.pvz2.controllers.menus.game.GameScreenController;
import com.compileordie.pvz2.controllers.menus.progression.CollectionMenuController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.game.economy.ZombieCard;
import com.compileordie.pvz2.views.helpers.ToastManager;
import pvz.libpvz.textures.TextureBank;

public class ZombieCardActor extends Stack {
    public static final float CARD_WIDTH = 75f;
    public static final float CARD_HEIGHT = 75f;

    private final ZombieCard zombieCard;
    private final Image portraitImage;
    private final Image cooldownDarkOverlay;
    private final Label cooldownTimerLabel;
    private final Label sunCostLabel;
    private final Image selectionOverlay;

    public ZombieCardActor(ZombieCard zombieCard, Skin skin, TextureBank textureBank) {
        this.zombieCard = zombieCard;
        setSize(CARD_WIDTH, CARD_HEIGHT);

        TextureRegion bgRegion = textureBank.region("IMAGE_UI_PACKETS_EMPTY_PACKET");
        createBackground(bgRegion);

        this.portraitImage = createPortrait(textureBank, bgRegion);
        this.cooldownDarkOverlay = createCooldownOverlay(bgRegion);
        this.cooldownTimerLabel = createCooldownTimerLabel(skin);
        this.sunCostLabel = createSunCostLabel(skin);

        setupOverlayTable();
        this.selectionOverlay = createSelectionOverlay(textureBank, bgRegion);
        setupClickListener();
    }

    private void createBackground(TextureRegion bgRegion) {
        Image image = new Image(bgRegion);
        image.setScaling(Scaling.stretch);
        add(image);
    }

    private Image createPortrait(TextureBank textureBank, TextureRegion bgRegion) {
        // Use the same asset path as CollectionMenuScreen
        String portraitKey = switch (zombieCard.zombieType) {
            case IMP -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_EGYPT_IMP";
            case STANDARD -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_MUMMY";
            case CONEHEAD -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_MUMMY_ARMOR1";
            case BUCKETHEAD -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_MUMMY_ARMOR2";
            case NEWSPAPER_ZOMBIE -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_MODERN_NEWSPAPER";
            default -> "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_EGYPT_IMP";
        };
        TextureRegion portraitRegion = textureBank.region(portraitKey);
        Image portrait = new Image(portraitRegion != null ? portraitRegion : bgRegion);
        portrait.setScaling(Scaling.fit);

        Container<Image> portraitContainer = new Container<>(portrait);
        portraitContainer.setTransform(true);
        portraitContainer.setOrigin(Align.center);
        portraitContainer.setScale(0.92f);
        add(portraitContainer);
        return portrait;
    }

    private Image createCooldownOverlay(TextureRegion bgRegion) {
        Image overlay = new Image(bgRegion);
        overlay.setColor(new Color(0f, 0f, 0f, 0.65f));
        overlay.setScaling(Scaling.stretch);
        overlay.setAlign(Align.top);
        add(overlay);
        return overlay;
    }

    private Label createCooldownTimerLabel(Skin skin) {
        Label label = new Label("", skin, "big_outline");
        label.setFontScale(0.7f);
        label.setAlignment(Align.center);
        label.setColor(Color.WHITE);
        return label;
    }

    private Label createSunCostLabel(Skin skin) {
        Label label = new Label(String.valueOf(zombieCard.cost), skin, "medium_outline");
        label.setFontScale(0.6f);
        label.setColor(Color.GOLD);
        return label;
    }

    private void setupOverlayTable() {
        Table overlayTable = new Table();
        overlayTable.setFillParent(true);

        Table topTable = new Table();
        topTable.add(cooldownTimerLabel).center().expand();

        Table bottomTable = new Table();
        bottomTable.bottom().right();
        bottomTable.add(sunCostLabel).padBottom(4).padRight(6);

        overlayTable.add(topTable).expand().fill().row();
        overlayTable.add(bottomTable).fillX().padBottom(2);
        add(overlayTable);
    }

    private Image createSelectionOverlay(TextureBank textureBank, TextureRegion bgRegion) {
        TextureRegion selectRegion = textureBank.region("IMAGE_UI_PACKETS_SELECT");
        Image overlay;
        if (selectRegion != null) {
            NinePatch selectPatch = new NinePatch(selectRegion, 8, 8, 8, 8);
            overlay = new Image(new NinePatchDrawable(selectPatch));
        } else {
            overlay = new Image(bgRegion);
            overlay.setColor(new Color(1f, 1f, 1f, 0.45f));
        }
        overlay.setFillParent(true);
        overlay.setVisible(false);
        add(overlay);
        return overlay;
    }

    private void setupClickListener() {
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!zombieCard.isReady()) return;

                int currentSun = (AppModel.gameSession != null && AppModel.gameSession.gameBoard != null)
                    ? AppModel.gameSession.gameBoard.economyManager.sunAmount : 0;

                if (currentSun < zombieCard.cost) {
                    ToastManager.showError("Not enough sun!");
                    return;
                }

                GameScreenController.selectedZombieCard = zombieCard;
                GameScreenController.selectedPlantCard = null; // Clear plant selection
            }
        });
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        boolean onCooldown = !zombieCard.isReady();
        int currentSun = (AppModel.gameSession != null && AppModel.gameSession.gameBoard != null)
            ? AppModel.gameSession.gameBoard.economyManager.sunAmount : 0;
        boolean hasEnoughSun = currentSun >= zombieCard.cost;

        if (onCooldown) {
            cooldownDarkOverlay.setVisible(true);
            cooldownTimerLabel.setVisible(true);
            cooldownTimerLabel.setText(String.format("%.1f", zombieCard.getRemainingCooldownSeconds()));

            float progress = (zombieCard.cooldownTicks > 0)
                ? zombieCard.remainingCooldownTicks / zombieCard.cooldownTicks
                : 0f;
            cooldownDarkOverlay.setSize(getWidth(), getHeight() * Math.min(1f, progress));
            cooldownDarkOverlay.setY(getHeight() - cooldownDarkOverlay.getHeight());

            portraitImage.setColor(Color.GRAY);
            sunCostLabel.setColor(Color.LIGHT_GRAY);
        } else {
            cooldownDarkOverlay.setVisible(false);
            cooldownTimerLabel.setVisible(false);

            if (!hasEnoughSun) {
                portraitImage.setColor(Color.GRAY);
                sunCostLabel.setColor(Color.CORAL);
            } else {
                portraitImage.setColor(Color.WHITE);
                sunCostLabel.setColor(Color.GOLD);
            }
        }

        boolean isSelected = GameScreenController.selectedZombieCard == zombieCard;
        selectionOverlay.setVisible(isSelected);
    }
}
