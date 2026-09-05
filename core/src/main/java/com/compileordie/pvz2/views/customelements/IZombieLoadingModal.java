package com.compileordie.pvz2.views.customelements;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.game.SessionBuilder;
import com.compileordie.pvz2.network.NetworkClient;
import com.compileordie.pvz2.network.protocol.Message;
import com.compileordie.pvz2.network.protocol.MessageType;
import com.compileordie.pvz2.views.ScreenManager;
import com.compileordie.pvz2.views.ScreenType;
import com.compileordie.pvz2.views.game.PlantAssetManager;
import com.compileordie.pvz2.views.helpers.PamActor;
import com.compileordie.pvz2.views.helpers.ToastManager;
import pvz.libpvz.pam.ClipRef;

public class IZombieLoadingModal extends BaseModal {
    private final PlantAssetManager plantAssetManager;

    public IZombieLoadingModal(Skin skin, boolean isSpecific, String opponent) {
        super("Connecting...", skin);
        this.plantAssetManager = new PlantAssetManager();

        getCell(contentWindow).minWidth(500).minHeight(320);

        bodyTable.top().pad(20);
        String msg = isSpecific ? "Waiting for " + opponent + " to accept..." : "Searching for random opponent...";
        Label waitLbl = new Label(msg, skin, "medium_outline");
        bodyTable.add(waitLbl).padBottom(20).row();

        ClipRef sunflowerClip = plantAssetManager.loadPlantClip(PlantType.SUNFLOWER);
        PamActor sunflowerAnim = new PamActor(
            plantAssetManager, sunflowerClip, plantAssetManager.getBounds(PlantType.SUNFLOWER)
        );
        bodyTable.add(sunflowerAnim).size(100).padTop(40).padBottom(10).row();

        TextButton cancelBtn = new TextButton("Cancel", skin, "brown");
        buttonTable.add(cancelBtn).size(140, 48);
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                hide();
                // Wire cancel matchmaking packet to backend
                NetworkClient.getInstance().send(new Message(MessageType.IZOMBIE_CANCEL));
            }
        });
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // Poll network responses while waiting
        Message msg;
        while ((msg = NetworkClient.getInstance().poll()) != null) {
            if (msg.getType() == MessageType.IZOMBIE_MATCH_FOUND) {
                hide();
                AppModel.opponentUsername = msg.get("opponent");
                boolean isReceiver = Boolean.parseBoolean(msg.get("isReceiverClient"));

                AppModel.gameSession = SessionBuilder.create(AppModel.currentLevel, AppModel.selectionDeck);
                AppModel.isReceiverClient = isReceiver;

                ScreenManager.setMenuScreen(ScreenType.GAME_SESSION);
                return;
            } else if (msg.getType() == MessageType.IZOMBIE_MATCH_ERROR) {
                hide();
                ToastManager.showError(msg.get("reason"));
                return;
            }
        }
    }

    @Override
    public void hide() {
        super.hide();
        if (plantAssetManager != null) {
            plantAssetManager.dispose();
        }
    }
}
