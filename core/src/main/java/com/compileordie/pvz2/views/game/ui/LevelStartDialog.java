package com.compileordie.pvz2.views.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.compileordie.pvz2.models.entities.npcs.MissionProvider;
import com.compileordie.pvz2.models.entities.npcs.NpcType;
import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.views.helpers.NpcPamActor;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

public class LevelStartDialog extends Table {
    private final NpcPamActor npcActor;
    private final Runnable onDismiss;
    private boolean isExiting = false;

    public LevelStartDialog(LevelID levelID, Skin skin, TextureBank textureBank, PamPlayer npcPam, Runnable onDismiss) {
        this.onDismiss = onDismiss;
        NpcType npcType = NpcType.selectForLevel(levelID);
        this.npcActor = new NpcPamActor(npcPam, npcType);

        setFillParent(true);
        buildLayout(levelID, npcType, skin, textureBank);
    }

    private void buildLayout(LevelID levelID, NpcType npc, Skin skin, TextureBank textureBank) {
        Image shadeOverlay = createShadeOverlay();
        addActor(shadeOverlay);

        Table modalContainer = new Table();
        modalContainer.center();

        Table dialogBox = createDialogBox(levelID, npc, skin);

        if (npc.isFacingRight()) {
            modalContainer.add(npcActor).size(300, 320).padRight(200);
            modalContainer.add(dialogBox).width(600).height(320);
        } else {
            modalContainer.add(dialogBox).width(600).height(320).padRight(200);
            modalContainer.add(npcActor).size(300, 320);
        }

        add(modalContainer).center();
    }

    private Image createShadeOverlay() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture whiteTexture = new Texture(pixmap);
        pixmap.dispose();

        Image shade = new Image(new TextureRegionDrawable(new TextureRegion(whiteTexture)));
        shade.setColor(new Color(0f, 0f, 0f, 0.65f));
        shade.setFillParent(true);
        return shade;
    }

    private Table createDialogBox(LevelID levelID, NpcType npc, Skin skin) {
        Table box = new Table(skin);
        box.setBackground(skin.getDrawable("image_ui_if_bundle_reward_multiplier_bg_10"));

        // 1. Separate Labels
        Label greetingLabel = new Label(npc.getGreeting(), skin, "big");
        greetingLabel.setColor(Color.BROWN);
        greetingLabel.setWrap(true);

        Label missionLabel = new Label(MissionProvider.getMissionsDescription(levelID), skin, "medium");
        missionLabel.setColor(Color.BROWN);
        missionLabel.setWrap(true);

        TextButton confirmButton = new TextButton("Roger, roger!", skin);
        confirmButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!isExiting) {
                    isExiting = true;
                    confirmButton.setDisabled(true);
                    npcActor.triggerExit();
                }
            }
        });

        // 2. Add elements with top-left cell alignment (.top().left())
        box.add(greetingLabel).expandX().fillX().top().left().pad(25, 25, 12, 25).row();
        box.add(missionLabel).expand().fillX().top().left().pad(0, 25, 10, 25).row();
        box.add(confirmButton).right().bottom().pad(0, 0, 20, 25);

        return box;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (isExiting && npcActor.getCurrentState() == NpcPamActor.State.FINISHED) {
            remove();
            if (onDismiss != null) {
                onDismiss.run();
            }
        }
    }

    public static void show(Stage stage,
                            LevelID levelID,
                            Skin skin,
                            TextureBank textureBank,
                            PamPlayer npcPam,
                            Runnable onDismiss) {
        LevelStartDialog dialog = new LevelStartDialog(levelID, skin, textureBank, npcPam, onDismiss);
        stage.addActor(dialog);
    }
}
