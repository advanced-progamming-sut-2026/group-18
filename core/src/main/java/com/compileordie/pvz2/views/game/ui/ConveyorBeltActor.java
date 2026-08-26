package com.compileordie.pvz2.views.game.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.game.economy.PlantCard;
import com.compileordie.pvz2.models.repositories.configs.PlantConfigRepository;
import pvz.libpvz.textures.TextureBank;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConveyorBeltActor extends Table {
    public static final int MAX_CONVEYOR_CAPACITY = 8;
    public static final float CARD_SPACING = 6f;
    public static final float RAIL_HEIGHT = 14f;
    public static final float BELT_WIDTH = (PlantCardActor.CARD_WIDTH + CARD_SPACING) * MAX_CONVEYOR_CAPACITY + 16f;
    public static final float BELT_HEIGHT = PlantCardActor.CARD_HEIGHT + (RAIL_HEIGHT * 2);

    private static final float BELT_SPEED = 70f; // Pixels per second

    private final Skin skin;
    private final TextureBank textureBank;
    private final PlantConfigRepository configRepo;
    private final TextureRegion topRailRegion;
    private final TextureRegion beltBandRegion;

    private final List<ConveyorCardSlot> cardSlots = new ArrayList<>();
    private float scrollOffset = 0f;

    public ConveyorBeltActor(Skin skin, TextureBank textureBank, PlantConfigRepository configRepo) {
        this.skin = skin;
        this.textureBank = textureBank;
        this.configRepo = configRepo;

        this.topRailRegion = textureBank.region("IMAGE_UI_CONVEYOR_CONVEYOR_TOP");
        this.beltBandRegion = textureBank.region("IMAGE_UI_CONVEYOR_CONVEYOR_BELT");

        setSize(BELT_WIDTH, BELT_HEIGHT);
    }

    @Override
    public void act(float delta) {
        if (GameScreenUI.isPaused) {
            super.act(0f);
            return;
        }

        super.act(delta);

        // 1. Calculate belt texture segment width to loop scrolling
        float bandHeight = (getHeight() - (RAIL_HEIGHT * 2)) / 5f;
        float aspect = (beltBandRegion != null && beltBandRegion.getRegionHeight() > 0)
            ? (float) beltBandRegion.getRegionWidth() / beltBandRegion.getRegionHeight() : 1f;
        float segmentWidth = Math.max(1f, bandHeight * aspect);

        scrollOffset = (scrollOffset + delta * BELT_SPEED) % segmentWidth;

        syncWithModelCards();
        updateCardPositions(delta);
    }

    private void syncWithModelCards() {
        if (AppModel.gameSession == null || AppModel.gameSession.gameBoard == null) return;
        List<PlantCard> modelCards = AppModel.gameSession.gameBoard.economyManager.plantCards;

        // 1. Remove cards that were planted or discarded
        Iterator<ConveyorCardSlot> it = cardSlots.iterator();
        while (it.hasNext()) {
            ConveyorCardSlot slot = it.next();
            if (!modelCards.contains(slot.card)) {
                slot.actor.remove();
                it.remove();
            }
        }

        // 2. Add newly spawned cards from the right edge
        for (int i = 0; i < modelCards.size() && cardSlots.size() < MAX_CONVEYOR_CAPACITY; i++) {
            PlantCard card = modelCards.get(i);
            if (!containsCard(card)) {
                PlantCardActor actor = new PlantCardActor(card, skin, textureBank, configRepo, true);
                float spawnX = getWidth(); // Spawn from the right edge
                ConveyorCardSlot newSlot = new ConveyorCardSlot(card, actor, spawnX);
                cardSlots.add(newSlot);
                addActor(actor);
            }
        }
    }

    private boolean containsCard(PlantCard card) {
        for (ConveyorCardSlot slot : cardSlots) {
            if (slot.card == card) return true;
        }
        return false;
    }

    private void updateCardPositions(float delta) {
        float startX = 8f;
        float cardY = RAIL_HEIGHT;

        for (int i = 0; i < cardSlots.size(); i++) {
            ConveyorCardSlot slot = cardSlots.get(i);
            slot.targetX = startX + i * (PlantCardActor.CARD_WIDTH + CARD_SPACING);

            // Glide smoothly from right to left until target slot is reached
            if (slot.currentX > slot.targetX) {
                slot.currentX = Math.max(slot.targetX, slot.currentX - (delta * BELT_SPEED));
            } else if (slot.currentX < slot.targetX) {
                slot.currentX = Math.min(slot.targetX, slot.currentX + (delta * BELT_SPEED));
            }

            slot.actor.setPosition(slot.currentX, cardY);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        validate();
        batch.flush();

        // Clip child actors and belt bands within the conveyor bounds
        if (clipBegin(getX(), getY(), getWidth(), getHeight())) {
            // 1. Draw 5 Horizontal Belt Bands (Tiled & Moving Left)
            if (beltBandRegion != null) {
                float bandHeight = (getHeight() - (RAIL_HEIGHT * 2)) / 5f;
                float aspect = (float) beltBandRegion.getRegionWidth() / Math.max(1, beltBandRegion.getRegionHeight());
                float segmentWidth = Math.max(1f, bandHeight * aspect);

                for (int i = 0; i < 5; i++) {
                    float bandY = getY() + RAIL_HEIGHT + (i * bandHeight);
                    float curX = getX() - scrollOffset;
                    while (curX < getX() + getWidth()) {
                        batch.draw(beltBandRegion, curX, bandY, segmentWidth, bandHeight);
                        curX += segmentWidth;
                    }
                }
            }

            // 2. Draw Moving Plant Cards
            super.draw(batch, parentAlpha);

            batch.flush();
            clipEnd();
        }

        float x = getX();
        float y = getY();
        float width = getWidth();
        float height = getHeight();

        // 3. Draw Top Rail
        if (topRailRegion != null) {
            batch.draw(topRailRegion, x, y + height - RAIL_HEIGHT, width, RAIL_HEIGHT);
        }

        // 4. Draw Bottom Rail (Flipped Vertically)
        if (topRailRegion != null) {
            batch.draw(topRailRegion.getTexture(), x, y, width, RAIL_HEIGHT,
                topRailRegion.getRegionX(), topRailRegion.getRegionY(),
                topRailRegion.getRegionWidth(), topRailRegion.getRegionHeight(),
                false, true);
        }
    }

    private static class ConveyorCardSlot {
        PlantCard card;
        PlantCardActor actor;
        float currentX;
        float targetX;

        ConveyorCardSlot(PlantCard card, PlantCardActor actor, float startX) {
            this.card = card;
            this.actor = actor;
            this.currentX = startX;
            this.targetX = startX;
        }
    }

    @Override
    public float getPrefWidth() {
        return BELT_WIDTH;
    }

    @Override
    public float getPrefHeight() {
        return BELT_HEIGHT;
    }
}
