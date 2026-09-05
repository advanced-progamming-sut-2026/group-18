package com.compileordie.pvz2.views.customelements;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.compileordie.pvz2.controllers.menus.game.GameMenuController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.game.levels.ChapterType;
import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.network.NetworkClient;
import com.compileordie.pvz2.network.protocol.Message;
import com.compileordie.pvz2.network.protocol.MessageType;
import com.compileordie.pvz2.views.ScreenManager;
import com.compileordie.pvz2.views.ScreenType;
import com.compileordie.pvz2.views.helpers.ToastManager;
import pvz.libpvz.textures.TextureBank;

import java.util.ArrayList;

public class IZombieInviteModal extends BaseModal {
    private final Skin skin;
    private final String inviterName;
    private final String inviterRole;
    private final Stage menuStage;
    private final TextureBank textureBank;
    private boolean actionTaken = false;

    public IZombieInviteModal(Skin skin,
                              TextureBank textureBank,
                              Stage menuStage,
                              String inviterName,
                              String inviterRole) {
        super("Match Invitation", skin);
        this.skin = skin;
        this.inviterName = inviterName;
        this.inviterRole = inviterRole;
        this.menuStage = menuStage;
        this.textureBank = textureBank;

        buildLayout();
    }

    private void buildLayout() {
        Label messageLbl = new Label(
            inviterName + " has invited you to a game of I, Zombie as "
                + (inviterRole.equals("ZOMBIES") ? "plants" : "zombies") + "!",
            skin,
            "medium_outline"
        );
        messageLbl.setFontScale(0.7f);
        bodyTable.add(messageLbl).pad(20).row();

        TextButton acceptBtn = new TextButton("Accept", skin, "green");
        TextButton rejectBtn = new TextButton("Reject", skin, "brown");

        buttonTable.add(acceptBtn).size(150, 48).padRight(20);
        buttonTable.add(rejectBtn).size(150, 48);

        acceptBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                actionTaken = true;
                hide();

                AppModel.currentLevel = LevelID.I_ZOMBIE;
                AppModel.opponentUsername = inviterName;

                boolean inviterIsPlants = "PLANTS".equalsIgnoreCase(inviterRole);

                if (inviterIsPlants) {
                    // Receiver plays Zombies -> Accept immediately
                    sendAccept();
                    AppModel.currentChapter = ChapterType.MINIGAME;
                    AppModel.currentLevel = LevelID.I_ZOMBIE;
                    IZombieLoadingModal loadingModal = new IZombieLoadingModal(skin, true, inviterName);
                    loadingModal.show(menuStage);
                } else {
                    // Receiver plays Plants -> Open Plant Selection Modal first
                    ArrayList<PlantType> availablePlants = GameMenuController.getPlants(LevelID.I_ZOMBIE);

                    GamePlantSelectionModal plantModal = new GamePlantSelectionModal(
                        skin,
                        textureBank,
                        availablePlants,
                        () -> {
                            // Plant selection finished -> Send Accept & Start Loading
                            sendAccept();
                            AppModel.currentChapter = ChapterType.MINIGAME;
                            AppModel.currentLevel = LevelID.I_ZOMBIE;
                            AppModel.isReceiverClient = false;
                            ScreenManager.setMenuScreen(ScreenType.GAME_SESSION);
                        },
                        () -> {
                            // Plant selection canceled -> Send Reject
                            sendReject();
                            ToastManager.showError("Match invitation canceled.");
                        }
                    );
                    plantModal.show(menuStage);
                }
            }
        });

        rejectBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                actionTaken = true;
                hide();
                sendReject();
            }
        });
    }

    @Override
    public void hide() {
        super.hide();
        if (!actionTaken) {
            actionTaken = true;
            sendReject();
        }
    }

    private void sendAccept() {
        Message acceptMsg = new Message(MessageType.IZOMBIE_ACCEPT)
            .put("target", inviterName)
            .put("inviterRole", inviterRole);
        NetworkClient.getInstance().send(acceptMsg);
    }

    private void sendReject() {
        Message rejectMsg = new Message(MessageType.IZOMBIE_REJECT)
            .put("target", inviterName);
        NetworkClient.getInstance().send(rejectMsg);
    }
}
