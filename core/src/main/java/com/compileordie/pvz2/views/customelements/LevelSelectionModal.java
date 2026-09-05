package com.compileordie.pvz2.views.customelements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.compileordie.pvz2.controllers.menus.game.GameMenuController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.game.SessionBuilder;
import com.compileordie.pvz2.models.game.levels.ChapterType;
import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.models.game.levels.LevelType;
import com.compileordie.pvz2.views.ScreenManager;
import com.compileordie.pvz2.views.ScreenType;
import com.compileordie.pvz2.views.helpers.ToastManager;
import pvz.libpvz.textures.TextureBank;

import java.util.ArrayList;
import java.util.List;

public class LevelSelectionModal extends BaseModal {
    private final Skin skin;
    private final ChapterType chapterType;
    private final TextureBank textureBank;
    private final Stage menuStage;

    public LevelSelectionModal(Skin skin, ChapterType chapterType, TextureBank textureBank, Stage stage) {
        super(chapterType.toString() + " Levels", skin);
        this.skin = skin;
        this.chapterType = chapterType;
        this.textureBank = textureBank;
        this.menuStage = stage;

        getCell(contentWindow).minWidth(650).minHeight(420);

        buildLayout();
        setupBottomControls();
    }

    private void buildLayout() {
        bodyTable.top().padTop(10);

        List<LevelID> chapterLevels = getLevelsForChapter(chapterType);
        Table levelsGrid = new Table();

        int columns = chapterType == ChapterType.MINIGAME ? 3 : 2;
        int index = 0;

        for (LevelID level : chapterLevels) {
            Button levelCard = createLevelCard(level);
            levelsGrid.add(levelCard).size(230, 85).pad(8);

            index++;
            if (index % columns == 0) {
                levelsGrid.row();
            }
        }

        bodyTable.add(levelsGrid).center();
    }

    private Button createLevelCard(LevelID level) {
        boolean isUnlocked = AppModel.player != null &&
            AppModel.player.unlockedLevelIDs != null &&
            AppModel.player.unlockedLevelIDs.contains(level);

        Button.ButtonStyle style = skin.get(isUnlocked ? "green" : "brown", TextButton.TextButtonStyle.class);
        Button cardBtn = new Button(style);
        Table content = new Table();
        content.center();

        String displayName = getLevelDisplayName(level);
        Label titleLbl = new Label(displayName, skin, "medium_outline");
        titleLbl.setFontScale(0.7f);
        titleLbl.setAlignment(Align.center);
        content.add(titleLbl).padBottom(4).row();

        Label subLbl = getSubLbl(level, isUnlocked);
        content.add(subLbl).row();
        cardBtn.add(content).expand().fill();
        cardBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!isUnlocked) {
                    ToastManager.showError("You must unlock this level first!");
                    return;
                }
                hide();
                AppModel.currentLevel = level;
                AppModel.selectionDeck.clear();
                ArrayList<PlantType> plants = GameMenuController.getPlants(level);
                if (level == LevelID.I_ZOMBIE) {
                    IZombieMatchmakingModal iZombieMatchmakingModal = new IZombieMatchmakingModal(skin,
                        textureBank,
                        menuStage);
                    iZombieMatchmakingModal.show(menuStage);
                } else if (level.needsPlantSelection()) {
                    GamePlantSelectionModal plantSelectionModal = new GamePlantSelectionModal(
                        skin,
                        textureBank,
                        plants
                    );
                    plantSelectionModal.show(menuStage);
                } else {
                    for (PlantType plantType : plants) {
                        AppModel.selectionDeck.put(plantType, MathUtils.randomBoolean(0.1f));
                    }
                    AppModel.gameSession = SessionBuilder.create(AppModel.currentLevel, AppModel.selectionDeck);
                    ScreenManager.setMenuScreen(ScreenType.GAME_SESSION);
                }
            }
        });

        return cardBtn;
    }

    private Label getSubLbl(LevelID level, boolean isUnlocked) {
        String subtext;
        if (!isUnlocked) {
            subtext = "Locked";
        } else if (level.levelType == LevelType.ZOMBOSS) {
            subtext = "Boss Fight";
        } else if (level.waveNumber > 0) {
            subtext = level.waveNumber + " Waves";
        } else if (level.waveNumber == -1) {
            subtext = "Enjoy Unlimited Waves";
        } else {
            subtext = "Special Challenge";
        }
        Label subLbl = new Label(subtext, skin, "medium");
        subLbl.setFontScale(0.5f);
        subLbl.setColor(isUnlocked ? Color.GOLD : Color.LIGHT_GRAY);
        subLbl.setAlignment(Align.center);
        return subLbl;
    }

    private String getLevelDisplayName(LevelID level) {
        if (chapterType == ChapterType.MINIGAME) {
            return level.toString();
        }

        return switch (level.levelType) {
            case STANDARD -> "Standard";
            case ZOMBOSS -> "ZomBoss";
            case SPECIAL -> level.toString();
            case MINIGAME -> level.toString();
        };
    }

    private List<LevelID> getLevelsForChapter(ChapterType chapter) {
        List<LevelID> result = new ArrayList<>();
        for (LevelID level : LevelID.values()) {
            if (level.chapterType == chapter) {
                result.add(level);
            }
        }
        return result;
    }

    private void setupBottomControls() {
        TextButton closeBtn = new TextButton("Back", skin, "brown");
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
            }
        });

        buttonTable.add(closeBtn).size(150, 48);
    }
}
