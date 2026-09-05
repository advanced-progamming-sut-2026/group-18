package com.compileordie.pvz2.views.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.views.helpers.LeftMessageManager;
import pvz.libpvz.pam.PamPlayer;

public final class LevelSpecificUI {

    private LevelSpecificUI() {
    }

    public static void setupLevelSpecificElements(Stage uiStage, Skin skin) {
        if (uiStage == null || skin == null || AppModel.currentLevel == null) return;

        // 1. PLANT_WHAT_YOU_GET Level Mode
        if (AppModel.currentLevel == LevelID.PLANT_WHAT_YOU_GET) {
            createStartWavesButton(uiStage, skin);
        }

        // 2. LOVE_YOUR_PLANTS Level Mode
        if (AppModel.currentLevel == LevelID.LOVE_YOUR_PLANTS) {
            LostPlantsHud lostPlantsHud = new LostPlantsHud(skin);
            lostPlantsHud.pack();
            lostPlantsHud.setPosition(25f, 25f);
            uiStage.addActor(lostPlantsHud);
        }

        // 3. TIMED_WAR Level Mode
        if (AppModel.currentLevel == LevelID.TIMED_WAR) {
            TimedWarHud timedWarHud = new TimedWarHud(skin);

            Table bottomTable = new Table();
            bottomTable.setFillParent(true);
            bottomTable.bottom().left();
            bottomTable.add(timedWarHud).padBottom(25f).padLeft(25f);

            uiStage.addActor(bottomTable);
        }

        // 4. I_ZOMBIE Level Mode (Messenger Guide Panel)
        if (AppModel.currentLevel == LevelID.I_ZOMBIE) {
            Table messengerGuide = createIZombieMessengerGuide(skin);

            Table bottomTable = new Table();
            bottomTable.setFillParent(true);
            bottomTable.bottom().left();
            bottomTable.add(messengerGuide).padBottom(20f).padLeft(20f);

            uiStage.addActor(bottomTable);
        }
    }

    private static Table createIZombieMessengerGuide(Skin skin) {
        Table panel = new Table();
        panel.setBackground(skin.getDrawable("image_ui_if_bundle_reward_multiplier_bg_10"));
        // Increased outer padding for a taller, roomier legend box
        panel.pad(40, 18, 40, 18);

        Label title = new Label("MESSENGER GUIDE", skin, "medium");
        title.setColor(Color.BROWN);
        panel.add(title).colspan(3).padBottom(12).align(Align.center).row();

        // 1 - 3: Text Message Previews (added bottom padding between rows)
        panel.add(createGuideTextItem(skin, "1-", "\"Old age?\"")).padRight(14).padBottom(30).left();
        panel.add(createGuideTextItem(skin, "2-", "\"Photosynthesis\"")).padRight(14).padBottom(30).left();
        panel.add(createGuideTextItem(skin, "3-", "\"Brain quality\"")).padBottom(30).left().row();

        // 4 - 6: Emoji Previews (added vertical padding)
        panel.add(createGuideImageItem(skin, "4-", "images/smiling_face_with_tear.png")).padRight(14).padBottom(30)
            .left();
        panel.add(createGuideImageItem(skin, "5-", "images/sunglasses.png")).padRight(14).padBottom(30).left();
        panel.add(createGuideImageItem(skin, "6-", "images/expressionless.png")).padBottom(30).left().row();

        // 7 - 9: Animated PAM Previews
        String dinoPam = "768/FULL/ZOMBIE/ZOMBIE_DINO_STEGOSAURUS/ZOMBIE_DINO_STEGOSAURUS.PAM";
        panel.add(createGuidePamItem(skin, "7-", dinoPam, "idle_head")).padRight(14).left();
        panel.add(createGuidePamItem(skin, "8-", dinoPam, "annoyed")).padRight(14).left();
        panel.add(createGuidePamItem(skin, "9-", dinoPam, "head_idle_charmed")).left().row();

        panel.pack();
        return panel;
    }

    private static Table createGuideTextItem(Skin skin, String key, String text) {
        Table item = new Table();
        Label keyLabel = new Label(key, skin, "medium");
        keyLabel.setColor(Color.BROWN);

        Label textLabel = new Label(text, skin, "medium");
        textLabel.setColor(Color.DARK_GRAY);
        textLabel.setFontScale(0.85f);

        item.add(keyLabel).padRight(4);
        item.add(textLabel);
        return item;
    }

    private static Table createGuideImageItem(Skin skin, String key, String imagePath) {
        Table item = new Table();
        Label keyLabel = new Label(key, skin, "medium");
        keyLabel.setColor(Color.BROWN);

        Texture texture = new Texture(Gdx.files.internal(imagePath));
        Image img = new Image(texture);

        item.add(keyLabel).padRight(15);
        item.add(img).size(24, 24);
        return item;
    }

    private static Table createGuidePamItem(Skin skin, String key, String pamPath, String clipName) {
        Table item = new Table();
        Label keyLabel = new Label(key, skin, "medium");
        keyLabel.setColor(Color.BROWN);

        PamActor pamActor = new PamActor(
            LeftMessageManager.pamPlayer,
            pamPath,
            clipName,
            28f,
            28f
        );

        item.add(keyLabel).padRight(6);
        item.add(pamActor).size(28, 28);
        return item;
    }

    private static void createStartWavesButton(Stage uiStage, Skin skin) {
        TextButton startWavesButton = new TextButton("START WAVES", skin, "green");

        float buttonWidth = 180f;
        float buttonHeight = 52f;
        float paddingX = 25f;
        float paddingY = 25f;

        startWavesButton.setSize(buttonWidth, buttonHeight);
        startWavesButton.setPosition(paddingX, paddingY);

        startWavesButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (AppModel.gameSession != null
                    && AppModel.gameSession.gameBoard != null
                    && AppModel.gameSession.gameBoard.waveManager != null) {

                    AppModel.gameSession.gameBoard.waveManager.shouldStartWaves = true;
                }

                startWavesButton.setTouchable(Touchable.disabled);

                float exitX = -startWavesButton.getWidth() - 50f;
                float duration = 0.45f;

                startWavesButton.addAction(Actions.sequence(
                    Actions.moveTo(exitX, startWavesButton.getY(), duration, Interpolation.pow2In),
                    Actions.removeActor()
                ));
            }
        });

        uiStage.addActor(startWavesButton);
    }

    /**
     * Actor for rendering mini libPVZ PAM animations in the guide panel.
     */
    private static class PamActor extends Actor {
        private final PamPlayer pamPlayer;
        private final String pamPath;
        private final String clipName;
        private float stateTime = 0f;

        public PamActor(PamPlayer pamPlayer, String pamPath, String clipName, float width, float height) {
            this.pamPlayer = pamPlayer;
            this.pamPath = pamPath;
            this.clipName = clipName;
            setSize(width, height);

            if (pamPlayer != null) {
                pamPlayer.loadAsync(pamPath, null);
            }
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (pamPlayer == null) return;
            pamPlayer.draw(
                batch,
                pamPath,
                clipName,
                stateTime,
                getX() + 60f,
                getY(),
                -0.18f,
                0.18f,
                true
            );
        }
    }
}
