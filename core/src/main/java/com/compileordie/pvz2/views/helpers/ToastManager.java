package com.compileordie.pvz2.views.helpers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.compileordie.pvz2.config.Constants;
import pvz.skin.PvzSkin;

import static com.badlogic.gdx.math.Interpolation.pow2In;
import static com.badlogic.gdx.math.Interpolation.pow2Out;

public class ToastManager {
    @SuppressWarnings("GDXJavaStaticResource")
    private static Stage globalStage;
    private static Table toastContainer;

    public static void init() {
        ScreenViewport viewport = new ScreenViewport();
        viewport.setUnitsPerPixel(1f / Constants.UI.UPP);
        globalStage = new Stage(viewport);

        toastContainer = new Table();
        toastContainer.top().right().pad(20);
        toastContainer.setFillParent(true);
        globalStage.addActor(toastContainer);
    }

    public static void resize(int width, int height) {
        if (globalStage != null && width > 0 && height > 0) {
            globalStage.getViewport().update(width, height, true);
        }
    }

    public static void render(float delta) {
        if (globalStage != null) {
            float clampedDelta = Math.min(delta, 0.033f);
            globalStage.act(clampedDelta);
            globalStage.draw();
        }
    }

    public static void show(String message, Color color) {
        if (globalStage == null) return;

        Table toastBox = new Table();
        toastBox.setBackground(PvzSkin.get().getDrawable("image_ui_if_bundle_reward_multiplier_bg_10"));
        toastBox.pad(15);

        Label toastLabel = new Label(message, PvzSkin.get(), "medium");
        if (color != null) {
            toastLabel.setColor(color);
        }
        toastLabel.setAlignment(Align.center);

        float prefWidth = toastLabel.getPrefWidth();
        toastLabel.setWrap(true);
        toastBox.add(toastLabel).width(Math.min(prefWidth, 300f)).align(Align.center);
        toastBox.pack();

        WidgetGroup wrapper = new WidgetGroup();
        wrapper.setSize(toastBox.getWidth(), toastBox.getHeight());
        wrapper.addActor(toastBox);
        wrapper.setTouchable(Touchable.disabled);

        toastBox.setColor(1, 1, 1, 0);
        float slideOffset = toastBox.getWidth() + 50f;
        toastBox.setPosition(slideOffset, 0);

        toastContainer.add(wrapper).size(toastBox.getWidth(), toastBox.getHeight())
            .padBottom(10).align(Align.right).row();
        toastBox.addAction(Actions.sequence(
            Actions.parallel(Actions.alpha(0.8f, 0.25f), Actions.moveTo(0, 0, 0.25f, pow2Out)),
            Actions.delay(2.5f),
            Actions.parallel(Actions.fadeOut(0.5f), Actions.moveBy(slideOffset, 0, 0.5f, pow2In)),
            Actions.run(() -> {
                Cell<?> cell = toastContainer.getCell(wrapper);
                if (cell != null) {
                    cell.setActor(null);
                    cell.size(0, 0);
                    cell.pad(0);
                }
                toastContainer.invalidateHierarchy();
                wrapper.remove();
            })
        ));
    }

    public static void showMessage(String message) {
        show(message, Color.BROWN);
    }

    public static void showSuccess(String message) {
        show(message, Color.FOREST);
    }

    public static void showError(String message) {
        show(message, Color.FIREBRICK);
    }
}
