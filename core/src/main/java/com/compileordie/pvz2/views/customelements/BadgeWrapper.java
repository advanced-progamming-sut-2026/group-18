package com.compileordie.pvz2.views.customelements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;

public class BadgeWrapper extends Stack {
    private final Table badgeTable;
    private final Label badgeLabel;

    public BadgeWrapper(Actor mainActor, Skin skin) {
        this.add(mainActor);

        // 1. Create a Pixmap for the circle
        int badgeSize = 16;
        Pixmap pixmap = new Pixmap(badgeSize, badgeSize, Pixmap.Format.RGBA8888);

        int center = badgeSize / 2;

        // 2. Draw the Border (Dark Red)
        pixmap.setColor(new Color(0.4f, 0.05f, 0.05f, 1f)); // Dark red border
        pixmap.fillCircle(center, center, center - 1);

        // 3. Draw the Inner Area (Bright Red)
        pixmap.setColor(new Color(0.88f, 0.12f, 0.12f, 1f)); // Rich red
        pixmap.fillCircle(center, center, center - 3);

        // 4. Convert to a NinePatch
        // By slicing 10 pixels from every side, we lock the curves and leave a 2x2 center area to stretch
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        NinePatch patch = new NinePatch(texture,
            badgeSize / 2 - 1,
            badgeSize / 2 - 1,
            badgeSize / 2 - 1,
            badgeSize / 2 - 1);
        patch.setPadding(2, 2, 2, 2);
        NinePatchDrawable badgeDrawable = new NinePatchDrawable(patch);

        // 5. Set up the Label
        badgeLabel = new Label("", skin, "default");
        badgeLabel.setColor(Color.WHITE);
        badgeLabel.setAlignment(Align.center);

        // 6. Set up the Box
        Table badgeBox = new Table();
        badgeBox.setBackground(badgeDrawable);

        // Slightly tweaked padding to keep the text perfectly centered in the new border
        badgeBox.add(badgeLabel).center().padRight(4).padLeft(4);

        badgeTable = new Table();
        badgeTable.top().right();

        badgeTable.add(badgeBox).minWidth(badgeSize * 1.5f).minHeight(badgeSize * 1.5f).padTop(-5).padRight(-5);

        this.add(badgeTable);
        setBadgeCount(0);
    }

    public void setBadgeCount(int count) {
        if (count <= 0) {
            badgeTable.setVisible(false);
        } else {
            badgeTable.setVisible(true);
            badgeLabel.setText(count > 99 ? "99+" : String.valueOf(count));
        }
    }
}
