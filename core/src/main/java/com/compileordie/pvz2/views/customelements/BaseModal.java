package com.compileordie.pvz2.views.customelements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import pvz.skin.BorderedTable;

public abstract class BaseModal extends Table {
    protected final BorderedTable contentWindow;
    protected final Table bodyTable;
    protected final Table buttonTable;

    public BaseModal(String titleText, Skin skin) {
        setFillParent(true);
        setTouchable(Touchable.enabled); // Blocks clicks from falling through to the screen behind it

        // 1. Create a semi-transparent dark background
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0, 0, 0, 0.7f));
        pixmap.fill();
        setBackground(new TextureRegionDrawable(new Texture(pixmap)));
        pixmap.dispose();

        // 2. The main dialog window (takes up a chunk of the screen automatically)
        contentWindow = new BorderedTable();
        add(contentWindow).minWidth(600).minHeight(400); // Guarantees a good size

        // 3. Title
        Label title = new Label(titleText, skin, "medium_outline");
        contentWindow.add(title).padBottom(20).center().row();

        // 4. Containers for child classes to populate
        bodyTable = new Table();
        buttonTable = new Table();

        contentWindow.add(bodyTable).expand().fill().row();
        contentWindow.add(buttonTable).padTop(20).center();
    }

    public void show(Stage stage) {
        getColor().a = 0f;
        addAction(Actions.fadeIn(0.2f));
        stage.addActor(this);
    }

    public void hide() {
        addAction(Actions.sequence(
            Actions.fadeOut(0.2f),
            Actions.removeActor()
        ));
    }
}
