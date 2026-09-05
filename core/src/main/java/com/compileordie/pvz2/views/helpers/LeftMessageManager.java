package com.compileordie.pvz2.views.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.PvzSkin;

import static com.badlogic.gdx.math.Interpolation.pow2In;
import static com.badlogic.gdx.math.Interpolation.pow2Out;

public class LeftMessageManager {
    @SuppressWarnings("GDXJavaStaticResource")
    private static Stage globalStage;
    private static Table container;
    private static TextureBank textureBank;
    public static PamPlayer pamPlayer;

    public static void init() {
        ScreenViewport viewport = new ScreenViewport();
        globalStage = new Stage(viewport);

        container = new Table();
        container.left().pad(20);
        container.setFillParent(true);
        globalStage.addActor(container);

        // Initialize libPVZ dependencies
        textureBank = new TextureBank("768", Gdx.files.internal("pvz-assets"));
        pamPlayer = new PamPlayer(textureBank, Gdx.files.internal("pvz-assets"));
    }

    public static void resize(int width, int height) {
        if (globalStage != null && width > 0 && height > 0) {
            globalStage.getViewport().update(width, height, true);
        }
    }

    public static void render(float delta) {
        if (globalStage != null) {
            float clampedDelta = Math.min(delta, 0.033f);

            if (textureBank != null) {
                textureBank.update();
            }

            globalStage.act(clampedDelta);
            globalStage.draw();

            // TODO: Enable for debug:
            /*handleDebugInputs();*/
        }
    }

    // NOTE: Debug keybind
    private static void handleDebugInputs() {
        int[] numberKeys = {
            com.badlogic.gdx.Input.Keys.NUM_1, com.badlogic.gdx.Input.Keys.NUM_2,
            com.badlogic.gdx.Input.Keys.NUM_3, com.badlogic.gdx.Input.Keys.NUM_4,
            com.badlogic.gdx.Input.Keys.NUM_5, com.badlogic.gdx.Input.Keys.NUM_6,
            com.badlogic.gdx.Input.Keys.NUM_7, com.badlogic.gdx.Input.Keys.NUM_8,
            com.badlogic.gdx.Input.Keys.NUM_9
        };

        for (int i = 0; i < 9; i++) {
            if (Gdx.input.isKeyJustPressed(numberKeys[i])) {
                showMessage(i + 1);
                break;
            }
        }
    }

    public static void showMessage(int code) {
        if (globalStage == null) return;

        Actor contentActor = createContentForCode(code);
        if (contentActor == null) return;

        WidgetGroup wrapper = new WidgetGroup();
        wrapper.setSize(contentActor.getWidth(), contentActor.getHeight());
        wrapper.addActor(contentActor);
        wrapper.setTouchable(Touchable.disabled);

        float slideOffset = contentActor.getWidth() + 50f;

        // Start off-screen on the left
        contentActor.setPosition(-slideOffset, 0);

        container.add(wrapper).size(contentActor.getWidth(), contentActor.getHeight())
            .padBottom(10).align(Align.left).row();

        // Lerp in from the left edge, pause, and lerp back out to the left
        contentActor.addAction(Actions.sequence(
            Actions.moveTo(0, 0, 0.25f, pow2Out),
            Actions.delay(3.0f),
            Actions.moveBy(-slideOffset, 0, 0.5f, pow2In),
            Actions.run(() -> {
                Cell<?> cell = container.getCell(wrapper);
                if (cell != null) {
                    cell.setActor(null);
                    cell.size(0, 0);
                    cell.pad(0);
                }
                container.invalidateHierarchy();
                wrapper.remove();
            })
        ));
    }

    private static Actor createContentForCode(int code) {
        return switch (code) {
            // 1 to 3: Custom Text in Background
            case 1 -> createTextCard("Is your strategy \"hope they die of old age\"?");
            case 2 -> createTextCard("No amount of photosynthesis is gonna save this lawn");
            case 3 -> createTextCard("Sir, my zombies are complaining about the quality of the brains on this lawn");

            // 4 to 6: Custom PNG Image
            case 4 -> createImageCard("images/smiling_face_with_tear.png");
            case 5 -> createImageCard("images/sunglasses.png");
            case 6 -> createImageCard("images/expressionless.png");

            // 7 to 9: Custom Looping PAM Animation from libPVZ
            case 7 -> new PamActor(pamPlayer,
                "768/FULL/ZOMBIE/ZOMBIE_DINO_STEGOSAURUS/ZOMBIE_DINO_STEGOSAURUS.PAM",
                "idle_head",
                150f,
                150f);
            case 8 -> new PamActor(pamPlayer,
                "768/FULL/ZOMBIE/ZOMBIE_DINO_STEGOSAURUS/ZOMBIE_DINO_STEGOSAURUS.PAM",
                "annoyed",
                150f,
                150f);
            case 9 -> new PamActor(pamPlayer,
                "768/FULL/ZOMBIE/ZOMBIE_DINO_STEGOSAURUS/ZOMBIE_DINO_STEGOSAURUS.PAM",
                "head_idle_charmed",
                150f,
                150f);
            default -> null;
        };
    }

    private static Table createTextCard(String message) {
        Table box = new Table();
        box.setBackground(PvzSkin.get().getDrawable("image_ui_if_bundle_reward_multiplier_bg_10"));
        box.pad(15);

        Label label = new Label(message, PvzSkin.get(), "big");
        label.setFontScale(0.75f);
        label.setColor(Color.BROWN);
        label.setAlignment(Align.center);

        float prefWidth = label.getPrefWidth();
        label.setWrap(true);
        box.add(label).width(Math.min(prefWidth, 300f)).align(Align.center);
        box.pack();

        return box;
    }

    private static Image createImageCard(String internalPath) {
        Texture texture = new Texture(Gdx.files.internal(internalPath));
        Image image = new Image(texture);
        image.setSize(texture.getWidth(), texture.getHeight());
        return image;
    }

    /**
     * Custom Scene2D Actor for rendering libPVZ PAM animations
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

            // Asynchronously load/bake PAM animation data
            pamPlayer.loadAsync(pamPath, null);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            // Render the looping animation centered within the actor bounds
            pamPlayer.draw(
                batch,
                pamPath,
                clipName,
                stateTime,
                getX() + getWidth() + 40f,
                getY() + getHeight() / 2f,
                -1f,
                1f,
                true
            );
        }
    }
}
