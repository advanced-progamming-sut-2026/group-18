package com.compileordie.pvz2.views.customelements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.compileordie.pvz2.controllers.menus.game.GameMenuController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.network.NetworkClient;
import com.compileordie.pvz2.network.protocol.Message;
import com.compileordie.pvz2.network.protocol.MessageType;
import com.compileordie.pvz2.views.helpers.ToastManager;
import pvz.libpvz.textures.TextureBank;

import java.util.ArrayList;

public class IZombieMatchmakingModal extends BaseModal {
    private final Skin skin;
    private final TextureBank textureBank;
    private final Stage menuStage;
    private CheckBox specificPlayerCheck;
    private TextField opponentField;
    private Table specificPlayerRow;

    public IZombieMatchmakingModal(Skin skin, TextureBank textureBank, Stage stage) {
        super("I, Zombie Multiplayer", skin);
        this.skin = skin;
        this.textureBank = textureBank;
        this.menuStage = stage;

        getCell(contentWindow).minWidth(600).minHeight(380);

        buildLayout();
        setupButtons();
    }

    private void buildLayout() {
        bodyTable.top().pad(20);

        Label subtitle = new Label("Choose your side and opponent pairing", skin, "medium_outline");
        subtitle.setAlignment(Align.center);
        bodyTable.add(subtitle).padBottom(25).row();

        specificPlayerCheck = new CheckBox(" Pair with specific player", skin);
        specificPlayerCheck.getLabel().setColor(Color.BROWN);
        bodyTable.add(specificPlayerCheck).left().padBottom(15).row();

        specificPlayerRow = new Table();
        Label vsLabel = new Label("Opponent Username:", skin, "medium_outline");
        vsLabel.setFontScale(0.6f);
        opponentField = new TextField("", skin);
        opponentField.setMessageText("Enter username...");

        specificPlayerRow.add(vsLabel).padRight(10).left();
        specificPlayerRow.add(opponentField).width(240).height(40).left();
        specificPlayerRow.setVisible(false);
        bodyTable.add(specificPlayerRow).left().padBottom(20).row();

        specificPlayerCheck.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                specificPlayerRow.setVisible(specificPlayerCheck.isChecked());
            }
        });
    }

    private void setupButtons() {
        TextButton plantsBtn = new TextButton("Play Plants", skin, "green");
        TextButton zombiesBtn = new TextButton("Play Zombies", skin, "green");
        TextButton backBtn = new TextButton("Back", skin, "brown");

        buttonTable.add(plantsBtn).size(160, 48).padRight(15);
        buttonTable.add(zombiesBtn).size(160, 48).padRight(15);
        buttonTable.add(backBtn).size(120, 48);

        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
            }
        });

        plantsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                proceed(true);
            }
        });

        zombiesBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                proceed(false);
            }
        });
    }

    private void proceed(boolean isPlants) {
        boolean specific = specificPlayerCheck.isChecked();
        String opponent = opponentField.getText().trim();

        if (specific && opponent.isEmpty()) {
            ToastManager.showError("Please enter an opponent username!");
            return;
        }

        hide();

        AppModel.currentLevel = LevelID.I_ZOMBIE;
        AppModel.selectionDeck.clear();
        AppModel.isReceiverClient = !isPlants;
        AppModel.opponentUsername = opponent;

        // Send network request to backend
        Message request = new Message(MessageType.IZOMBIE_MATCH_REQUEST)
            .put("specific", String.valueOf(specific))
            .put("target", opponent)
            .put("isPlants", String.valueOf(isPlants));

        if (isPlants) {
            ArrayList<PlantType> plants = GameMenuController.getPlants(AppModel.currentLevel);
            GamePlantSelectionModal plantSelectionModal = new GamePlantSelectionModal(skin,
                textureBank,
                plants,
                () -> {
                    NetworkClient.getInstance().send(request);
                    IZombieLoadingModal loadingModal = new IZombieLoadingModal(skin, specific, opponent);
                    loadingModal.show(menuStage);
                },
                () -> {
                    ToastManager.showError("Match invitation canceled.");
                }
            );
            plantSelectionModal.show(menuStage);
        } else {
            NetworkClient.getInstance().send(request);
            IZombieLoadingModal loadingModal = new IZombieLoadingModal(skin, specific, opponent);
            loadingModal.show(menuStage);
        }
    }
}
