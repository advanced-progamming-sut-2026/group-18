package com.compileordie.pvz2.views.game.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.game.economy.EconomyManager;
import com.compileordie.pvz2.models.game.economy.EconomyType;
import com.compileordie.pvz2.models.game.economy.PlantCard;
import com.compileordie.pvz2.models.repositories.configs.PlantConfigRepository;
import pvz.libpvz.textures.TextureBank;

import java.util.ArrayList;
import java.util.List;

public class PlantCardBar extends Table {
    private final Skin skin;
    private final TextureBank textureBank;
    private final PlantConfigRepository configRepo;
    private final List<PlantCard> currentTrackedCards = new ArrayList<>();
    private final EconomyManager economyManager;

    public PlantCardBar(Skin skin, TextureBank textureBank, EconomyManager economyManager) {
        this.skin = skin;
        this.textureBank = textureBank;
        this.economyManager = economyManager;
        this.configRepo = new PlantConfigRepository();
        this.configRepo.loadFromCSV(Constants.Paths.Configs.PLANTS);
        top().center();
        syncBar();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        EconomyManager activeEconomy = getEconomyManager();
        if (activeEconomy == null) return;

        if (activeEconomy.type == EconomyType.CONVEYOR_BELT || activeEconomy.type == EconomyType.VASE_BREAKER) {
            if (getChildren().isEmpty() || !(getChildren().first() instanceof ConveyorBeltActor)) {
                syncBar();
            }
            return;
        }

        List<PlantCard> modelCards = activeEconomy.plantCards;
        if (!currentTrackedCards.equals(modelCards)) {
            syncBar();
        }
    }

    public void syncBar() {
        clearChildren();
        currentTrackedCards.clear();
        EconomyManager activeEconomy = getEconomyManager();
        if (activeEconomy == null) return;

        if (activeEconomy.type == EconomyType.CONVEYOR_BELT || activeEconomy.type == EconomyType.VASE_BREAKER) {
            ConveyorBeltActor conveyorBelt = new ConveyorBeltActor(skin, textureBank, configRepo);
            add(conveyorBelt).size(ConveyorBeltActor.BELT_WIDTH, ConveyorBeltActor.BELT_HEIGHT).center().top();
            return;
        }

        List<PlantCard> cards = activeEconomy.plantCards;
        if (cards != null && !cards.isEmpty()) {
            currentTrackedCards.addAll(cards);
            for (PlantCard card : cards) {
                PlantCardActor cardActor = new PlantCardActor(card, skin, textureBank, configRepo, false);
                add(cardActor).size(PlantCardActor.CARD_WIDTH, PlantCardActor.CARD_HEIGHT).padRight(4f);
            }
        }
    }

    private EconomyManager getEconomyManager() {
        if (AppModel.gameSession != null && AppModel.gameSession.gameBoard != null) {
            return AppModel.gameSession.gameBoard.economyManager;
        }
        return this.economyManager;
    }
}
