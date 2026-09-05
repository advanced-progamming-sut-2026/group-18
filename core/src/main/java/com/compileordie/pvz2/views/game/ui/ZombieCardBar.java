package com.compileordie.pvz2.views.game.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.game.economy.ZombieCard;
import com.compileordie.pvz2.models.game.levels.LevelID;
import pvz.libpvz.textures.TextureBank;

import java.util.Arrays;
import java.util.List;

public class ZombieCardBar extends Table {
    public final List<ZombieCard> cards;
    private final Skin skin;
    private final TextureBank textureBank;

    public ZombieCardBar(Skin skin, TextureBank textureBank) {
        this.skin = skin;
        this.textureBank = textureBank;
        this.cards = Arrays.asList(
            new ZombieCard(ZombieType.IMP, 50, 3f),
            new ZombieCard(ZombieType.STANDARD, 100, 5f),
            new ZombieCard(ZombieType.CONEHEAD, 175, 7f),
            new ZombieCard(ZombieType.BUCKETHEAD, 250, 10f),
            new ZombieCard(ZombieType.NEWSPAPER_ZOMBIE, 300, 12f)
        );
        top().center();
        syncBar();
    }

    private void syncBar() {
        clearChildren();
        for (ZombieCard card : cards) {
            ZombieCardActor cardActor = new ZombieCardActor(card, skin, textureBank);
            add(cardActor).size(ZombieCardActor.CARD_WIDTH, ZombieCardActor.CARD_HEIGHT).padRight(4f);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // Tick the cooldown for all zombie cards every frame
        if (cards != null) {
            for (ZombieCard card : cards) {
                card.update(delta);
            }
        }
    }
}
