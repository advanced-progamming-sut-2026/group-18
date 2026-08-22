package com.compileordie.pvz2.views.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.controllers.PlantSpawner;
import com.compileordie.pvz2.controllers.menus.progression.CollectionMenuController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.models.entities.plants.Plant;
import com.compileordie.pvz2.models.entities.plants.PlantTemplate;
import com.compileordie.pvz2.models.entities.plants.enums.PlantCategory;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.entities.zombies.types.ZombieType;
import com.compileordie.pvz2.models.game.levels.ChapterType;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;
import com.compileordie.pvz2.models.repositories.configs.PlantConfigRepository;
import com.compileordie.pvz2.models.repositories.configs.ZombieStatsConfig;
import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.views.ScreenManager;
import com.compileordie.pvz2.views.ScreenType;
import com.compileordie.pvz2.views.customelements.CurrencyHud;
import com.compileordie.pvz2.views.customelements.PlantPurchaseModal;
import com.compileordie.pvz2.views.game.PlantAssetManager;
import com.compileordie.pvz2.views.game.ZombieVisualRegistry;
import com.compileordie.pvz2.views.helpers.PamActor;
import com.compileordie.pvz2.views.helpers.ToastManager;
import com.compileordie.pvz2.views.helpers.ZombieUiActor;
import pvz.skin.BorderedTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CollectionMenuScreen extends MenuScreen {
    private enum Tab {PLANTS, ZOMBIES}
    private BorderedTable mainBg;
    private PlantAssetManager plantAssetManager;
    private PlantConfigRepository plantConfigRepository;
    private Tab currentTab = Tab.PLANTS;
    private PlantType selectedPlant = PlantType.PEASHOOTER;
    private ZombieType selectedZombie = ZombieType.STANDARD;
    private String selectedCategory = "All";
    private boolean showUnlockedOnly = false;
    private boolean showUpgradableOnly = false;

    @Override
    public void showCore() {
        plantAssetManager = new PlantAssetManager();
        if (plantConfigRepository == null) {
            plantConfigRepository = new PlantConfigRepository();
            plantConfigRepository.loadFromCSV(Constants.Paths.Configs.PLANTS);
        }

        mainBg = new BorderedTable();
        mainBg.setFillParent(true);
        mainBg.pad(20);
        stage.addActor(mainBg);

        // Top Navigation (Currency HUD)
        Table topNav = new Table();
        topNav.setFillParent(true);
        topNav.top().left();

        CurrencyHud currencyHud = new CurrencyHud(skin, stage, textureBank);
        currencyHud.setBackground((Drawable) null);
        topNav.add(currencyHud).pad(15);
        stage.addActor(topNav);

        // Bottom Navigation (Back Button)
        Table bottomNav = new Table();
        bottomNav.setFillParent(true);
        bottomNav.bottom().left();

        ImageButton backBtn = new ImageButton(skin, "generic_close_circle");
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.GAME);
            }
        });
        bottomNav.add(backBtn).size(64).pad(40);
        stage.addActor(bottomNav);

        refreshScreen();
    }

    private void refreshScreen() {
        mainBg.clearChildren();

        // 1. Info Panel (Top 1/3)
        // Added padTop(50) to clear the top navigation bar
        mainBg.add(buildInfoPanel()).fillX().expandX().padTop(50).padBottom(10).row();

        // 2. Controls & Filters (Middle Strip)
        mainBg.add(buildControlsStrip()).fillX().pad(10).row();

        // 3. Collection Grid (Bottom 2/3)
        ScrollPane scrollPane = new ScrollPane(buildGridTable(), skin);
        scrollPane.setFadeScrollBars(false);
        mainBg.add(scrollPane).fill().expand().pad(20);
    }

    private Table buildInfoPanel() {
        Table infoTable = new Table();
        infoTable.pad(15);

        Table animCell = new Table();
        Table statsCell = new Table();
        statsCell.left();

        if (currentTab == Tab.PLANTS) {
            buildPlantInfo(animCell, statsCell);
            infoTable.padLeft(50); // Shifts the plant info table slightly to the right
        } else {
            buildZombieInfo(animCell, statsCell);
            infoTable.padLeft(120); // Shifts the zombie info table significantly further to the right
        }

        infoTable.add(animCell).width(200).center();
        infoTable.add(statsCell).expand().fill().padLeft(20);
        return infoTable;
    }

    private void buildPlantInfo(Table animCell, Table statsCell) {
        PamActor plantAnim = new PamActor(plantAssetManager,
            plantAssetManager.loadPlantClip(selectedPlant),
            plantAssetManager.getBounds(selectedPlant));
        animCell.add(plantAnim).size(120).padTop(30);
        Player player = AppModel.player;
        Plant plant = PlantSpawner.spawn(selectedPlant);
        String category = plant.getCategory() != null ? plant.getCategory().toString() : "UNKNOWN";
        String hp = String.valueOf(plant.getBaseHp());
        String cost = String.valueOf(plant.getCost());
        String tagsStr = "None";
        if (plant.getTags() != null && !plant.getTags().isEmpty()) {
            tagsStr = plant.getTags().stream()
                .map(Enum::toString)
                .collect(Collectors.joining(", "));
        }
        Label cardNameLbl = new Label(selectedPlant.toString(), skin, "big_outline");
        statsCell.add(cardNameLbl).colspan(2).left().row();
        Label cardCategoryLbl = new Label(
            "Family: " + category + "   |   Tags: " + tagsStr, skin, "medium_outline"
        );
        cardCategoryLbl.setFontScale(0.75f);
        statsCell.add(cardCategoryLbl).left().padTop(8).row();
        Label cardDetailsLbl = new Label("HP: " + hp + "   |   Cost: " + cost, skin, "medium_outline");
        cardDetailsLbl.setFontScale(0.75f);
        statsCell.add(cardDetailsLbl).left().padTop(4).row();
        Table actions = new Table();
        int currentLevel = player.plantLevels.getOrDefault(selectedPlant, 1);
        int coinCost = currentLevel * ConfigManager.economy().plantUpgradeCoinsPerLevel;
        int packetCost = currentLevel * ConfigManager.economy().plantUpgradeSeedsPerLevel;
        boolean canUpgrade = CollectionMenuController.canUpgrade(selectedPlant);
        boolean isMaxLevel = currentLevel >= 4;
        String btnText = isMaxLevel ? "Max Level" : "Upgrade (Lvl " + (currentLevel + 1) + ")";
        TextButton upgradeBtn = new TextButton(btnText, skin, canUpgrade ? "green" : "brown");
        upgradeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleResult(CollectionMenuController.upgradePlant(selectedPlant));
            }
        });
        Label costLbl = new Label(
            isMaxLevel ? "" : (coinCost + " Coins, " + packetCost + " Seeds"), skin, "medium_outline"
        );
        costLbl.setFontScale(0.65f);
        if (!canUpgrade) costLbl.setColor(Color.LIGHT_GRAY);
        actions.add(upgradeBtn).size(200, 48).left().pad(10, 0, 10, 5);
        actions.add(costLbl).left();
        statsCell.add(actions).expandY().bottom().left().padBottom(10);
    }

    private void buildZombieInfo(Table animCell, Table statsCell) {
        populateZombieAnimation(animCell, selectedZombie.name());
        populateZombieStats(statsCell);
    }

    private ChapterType resolveZombieChapter(String nameStr) {
        if (nameStr.contains("KNIGHT") || nameStr.contains("DARK") || nameStr.contains("JESTER") ||
            nameStr.contains("WIZARD") || nameStr.contains("KING") || nameStr.contains("GARGANTUAR")) {
            return ChapterType.DARK_AGES;
        } else if (nameStr.contains("DODO") || nameStr.contains("HUNTER") || nameStr.contains("TROGLOBITE")) {
            return ChapterType.FROSTBITE_CAVES;
        } else if (nameStr.contains("SNORKEL") || nameStr.contains("OCTOPUS") || nameStr.contains("FISHERMAN")) {
            return ChapterType.BIG_WAVE_BEACH;
        }
        return ChapterType.ANCIENT_EGYPT;
    }

    private void populateZombieAnimation(Table animCell, String nameStr) {
        ChapterType previousChapter = AppModel.currentChapter;
        AppModel.currentChapter = resolveZombieChapter(nameStr);

        ZombieVisualRegistry.ZombieVisualDef def = ZombieVisualRegistry.get(nameStr);
        if (def != null && !def.pams.isEmpty()) {
            java.util.List<String> pamPaths = new java.util.ArrayList<>();
            for (ZombieVisualRegistry.PamSpec spec : def.pams) {
                String path = spec.getResolvedPath();
                pamPaths.add(path);
                plantAssetManager.getRawPlayer().loadSync(path);
            }

            java.util.Map<String, Boolean> visMap = new java.util.HashMap<>();
            for (String filter : def.getResolvedStateFilters()) {
                visMap.put(filter, !filter.contains("damage"));
            }

            ZombieUiActor zombieAnim = new ZombieUiActor(plantAssetManager.getRawPlayer(), pamPaths, visMap, 1.0f);
            animCell.add(zombieAnim).size(120).padTop(80);
        } else {
            animCell.add(new Label("No Asset", skin, "default"));
        }

        AppModel.currentChapter = previousChapter;
    }

    private void populateZombieStats(Table statsCell) {
        ZombieStatsConfig stats = null;
        try {
            stats = ConfigManager.zombies().get(selectedZombie);
        } catch (Exception ignored) {}

        String hpStr;
        String speedStr;

        if (stats != null) {
            int baseHp = (int) stats.hitpoints;
            int armorHp = (int) stats.armorHp;
            hpStr = armorHp > 0 ? (baseHp + " (+" + armorHp + " Armor)") : String.valueOf(baseHp);
            speedStr = String.valueOf(stats.speed);
        } else if (selectedZombie.name().contains("ZOMBOSS")) {
            hpStr = "15000";
            speedStr = "3.0";
        } else {
            hpStr = "N/A";
            speedStr = "N/A";
        }

        statsCell.add(new Label(selectedZombie.toString(), skin, "big_outline")).left().row();

        Label cardSpeedLbl = new Label("Speed: " + speedStr, skin, "medium_outline");
        cardSpeedLbl.setFontScale(0.75f);
        statsCell.add(cardSpeedLbl).left().padTop(10).row();

        Label cardHealthLbl = new Label("HP: " + hpStr, skin, "medium_outline");
        cardHealthLbl.setFontScale(0.75f);
        statsCell.add(cardHealthLbl).left().padTop(5).row();
    }

    private Table buildControlsStrip() {
        Table controls = new Table();

        addNavigationTabs(controls);

        if (currentTab == Tab.PLANTS) {
            addPlantFilters(controls);
        }

        return controls;
    }

    private void addNavigationTabs(Table controls) {
        TextButton plantsTab = new TextButton("PLANTS", skin, currentTab == Tab.PLANTS ? "green" : "brown");
        TextButton zombiesTab = new TextButton("ZOMBIES", skin, currentTab == Tab.ZOMBIES ? "green" : "brown");

        plantsTab.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                currentTab = Tab.PLANTS;
                refreshScreen();
            }
        });

        zombiesTab.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                currentTab = Tab.ZOMBIES;
                refreshScreen();
            }
        });

        controls.add(plantsTab).size(150, 50).padRight(10);
        controls.add(zombiesTab).size(150, 50).padRight(30);
    }

    private void addPlantFilters(Table controls) {
        controls.add(createCategoryFilter()).width(150).padRight(15);
        controls.add(createUnlockedFilter()).padRight(15);
        controls.add(createUpgradableFilter());
    }

    private SelectBox<String> createCategoryFilter() {
        List<String> families = new ArrayList<>();
        families.add("All");
        families.addAll(Arrays.stream(PlantCategory.values()).map(Enum::toString).toList());

        SelectBox<String> categoryFilter = new SelectBox<>(skin);
        categoryFilter.setItems(families.toArray(new String[0]));
        categoryFilter.setSelected(selectedCategory);

        categoryFilter.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                selectedCategory = categoryFilter.getSelected();
                refreshScreen();
            }
        });
        return categoryFilter;
    }

    private CheckBox createUnlockedFilter() {
        CheckBox unlockedCheck = new CheckBox(" Unlocked Only", skin);
        unlockedCheck.setChecked(showUnlockedOnly);

        unlockedCheck.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                showUnlockedOnly = unlockedCheck.isChecked();
                refreshScreen();
            }
        });
        return unlockedCheck;
    }

    private CheckBox createUpgradableFilter() {
        CheckBox upgradableCheck = new CheckBox(" Upgradable Only", skin);
        upgradableCheck.setChecked(showUpgradableOnly);

        upgradableCheck.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                showUpgradableOnly = upgradableCheck.isChecked();
                refreshScreen();
            }
        });
        return upgradableCheck;
    }

    private Table buildGridTable() {
        Table grid = new Table();
        int columns = 6;
        int index = 0;

        if (currentTab == Tab.PLANTS) {
            for (PlantType pt : PlantType.values()) {
                if (passesPlantFilters(pt)) {
                    grid.add(buildPlantCard(pt)).pad(10);
                    if (++index % columns == 0) grid.row();
                }
            }
        } else {
            for (ZombieType zt : ZombieType.values()) {
                grid.add(buildZombieCard(zt)).pad(10);
                if (++index % columns == 0) grid.row();
            }
        }
        return grid;
    }

    private boolean passesPlantFilters(PlantType type) {
        Player player = AppModel.player;
        boolean isUnlocked = player.unlockedPlants != null && player.unlockedPlants.contains(type);

        if (showUnlockedOnly && !isUnlocked) return false;
        if (showUpgradableOnly && !CollectionMenuController.canUpgrade(type)) return false;

        if (!selectedCategory.equals("All")) {
            PlantTemplate t = plantConfigRepository.getTemplate(type);
            return t != null && t.getCategory().toString().equals(selectedCategory);
        }
        return true;
    }

    private Stack buildPlantCard(PlantType type) {
        Stack card = new Stack();
        Player player = AppModel.player;
        boolean isUnlocked = player.unlockedPlants != null && player.unlockedPlants.contains(type);

        addCardBackground(card, isUnlocked);
        addCardPortrait(card, type, isUnlocked);
        addCardOverlays(card, type, player);

        if (!isUnlocked) {
            addLockIcon(card);
        }

        addCardClickListener(card, type, isUnlocked);
        return card;
    }

    private void addCardBackground(Stack card, boolean isUnlocked) {
        Image bg = new Image(textureBank.region("IMAGE_UI_PACKETS_SELECTED"));
        if (!isUnlocked) {
            bg.setColor(Color.DARK_GRAY);
        }
        card.add(bg);
    }

    private void addCardPortrait(Stack card, PlantType type, boolean isUnlocked) {
        TextureRegion portraitRegion = textureBank.region(CollectionMenuController.getPlantCardAssetPath(type));

        // Fallback to background texture if portrait is missing
        Image portrait = portraitRegion != null ?
            new Image(portraitRegion) : new Image(textureBank.region("IMAGE_UI_PACKETS_SELECTED"));

        portrait.setScaling(Scaling.fit);
        if (!isUnlocked) {
            portrait.setColor(Color.DARK_GRAY);
        }

        Container<Image> portraitContainer = new Container<>(portrait);
        portraitContainer.setTransform(true);
        portraitContainer.setOrigin(Align.center);
        portraitContainer.setScale(0.95f);

        card.add(portraitContainer);
    }

    private void addCardOverlays(Stack card, PlantType type, Player player) {
        int currentLevel = player.plantLevels.get(type);
        int currentSeeds = player.seedPackets.get(type);
        int neededSeeds = currentLevel * ConfigManager.economy().plantUpgradeSeedsPerLevel;

        // Top Overlay (Level)
        Table topOverlay = new Table();
        topOverlay.top().right();
        Label levelLbl = new Label("Lvl " + currentLevel, skin, "medium_outline");
        levelLbl.setFontScale(0.5f);
        topOverlay.add(levelLbl).padTop(5).padRight(5);
        card.add(topOverlay);

        // Bottom Overlay (Seed Packets)
        Table bottomOverlay = new Table();
        bottomOverlay.bottom();
        Label seedLbl = new Label(currentSeeds + "/" + neededSeeds, skin, "medium_outline");
        seedLbl.setFontScale(0.5f);
        bottomOverlay.add(seedLbl).padBottom(5);
        card.add(bottomOverlay);
    }

    private void addLockIcon(Stack card) {
        Image lock = new Image(textureBank.region("IMAGE_UI_PERKS_PERK_ICON_LOCKED"));
        lock.setScaling(Scaling.none);
        card.add(lock);
    }

    private void addCardClickListener(Stack card, PlantType type, boolean isUnlocked) {
        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isUnlocked) {
                    selectedPlant = type;
                    refreshScreen();
                } else {
                    int price = ConfigManager.economy().plantPurchaseCoins;
                    PlantPurchaseModal modal = new PlantPurchaseModal(
                        skin,
                        textureBank,
                        type,
                        price,
                        () -> handleResult(CollectionMenuController.purchasePlant(type))
                    );
                    modal.show(stage);
                }
            }
        });
    }

    private Stack buildZombieCard(ZombieType type) {
        Stack card = new Stack();
        Player player = AppModel.player;
        boolean isDiscovered = player.unlockedZombies != null && player.unlockedZombies.contains(type);

        Image bg = new Image(textureBank.region("IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_READY"));
        card.add(bg);

        if (isDiscovered) {
            TextureRegion portraitRegion = textureBank.region(CollectionMenuController.getZombieCardAssetPath(type));
            if (portraitRegion != null) {
                Image portrait = new Image(portraitRegion);
                portrait.setScaling(Scaling.fit);

                Container<Image> portraitContainer = new Container<>(portrait);
                portraitContainer.setTransform(true);
                portraitContainer.setOrigin(Align.center);
                portraitContainer.setScale(0.95f); // Makes the image 5% smaller
                card.add(portraitContainer);
            }
        } else {
            bg.setColor(Color.DARK_GRAY);
        }

        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isDiscovered) {
                    selectedZombie = type;
                    refreshScreen();
                }
            }
        });
        return card;
    }

    private void handleResult(Result<String> result) {
        if (result.isSuccess) {
            ToastManager.showSuccess(result.data);
            refreshScreen();
        } else {
            ToastManager.showError(result.errorMessage);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (plantAssetManager != null) plantAssetManager.dispose();
    }
}
