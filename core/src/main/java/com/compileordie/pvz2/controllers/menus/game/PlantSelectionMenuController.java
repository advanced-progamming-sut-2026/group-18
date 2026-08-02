package com.compileordie.pvz2.controllers.menus.game;

import com.compileordie.pvz2.controllers.AppController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.game.SessionBuilder;
import com.compileordie.pvz2.models.game.levels.LevelID;
import com.compileordie.pvz2.views.helpers.Menu;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;

public class PlantSelectionMenuController {
    private PlantSelectionMenuController() {
    }

    public static ArrayList<PlantType> getAvailablePlants(LevelID levelID) {
        ArrayList<PlantType> plantTypes = new ArrayList<>();
        if (levelID == LevelID.LOCKED_PLANTS) {
            for (PlantType plantType : PlantType.values()) {
                if (new Random().nextInt(100) < 75) {
                    plantTypes.add(plantType);
                }
            }
        } else if (levelID == LevelID.PLANT_WHAT_YOU_GET) {
            for (PlantType plantType : PlantType.values()) {
                if (!Set.of(PlantType.SUNFLOWER,
                    PlantType.TWIN_SUNFLOWER,
                    PlantType.SUN_SHROOM,
                    PlantType.PRIMAL_SUNFLOWER,
                    PlantType.GOLD_BLOOM,
                    PlantType.SUN_BEAN,
                    PlantType.ENLIGHTEN_MINT).contains(plantType)
                    && AppModel.player.unlockedPlants.contains(plantType)) {
                    plantTypes.add(plantType);
                }
            }
        } else if (levelID == LevelID.WALNUT_BOWLING) {
            // TODO: Add Bowling Wall-nut and Giant Wall-nut to the list
            plantTypes.addAll(Set.of(PlantType.EXPLODE_O_NUT));
        } else {
            plantTypes.addAll(AppModel.player.unlockedPlants);
        }

        return plantTypes;
    }

    public static String showAllPlants() {
        StringJoiner joiner = new StringJoiner(System.lineSeparator());
        joiner.add("All plants:");
        for (PlantType plantType : PlantType.values()) {
            joiner.add(plantType.toString());
        }
        return joiner.toString();
    }

    public static String showAvailablePlants() {
        StringJoiner joiner = new StringJoiner(System.lineSeparator());
        joiner.add("Available plants:");
        for (PlantType plantType : getAvailablePlants(AppModel.currentLevel)) {
            joiner.add(plantType.toString());
        }
        return joiner.toString();
    }

    public static String addPlant(String type) {
        PlantType plant = PlantType.getByName(type);
        if (plant == null) {
            return "[ERROR] Invalid plant name.";
        }
        if (!getAvailablePlants(AppModel.currentLevel).contains(plant)) {
            return "[ERROR] Invalid plant type for this level.";
        }
        if (AppModel.selectionDeck.size() >= 8) {
            return "[ERROR] Selection deck is already full (8/8).";
        }

        AppModel.selectionDeck.put(plant, false);
        return "Added plant " + plant + "to selection deck. (capacity " + AppModel.selectionDeck.size() + "/8)";
    }

    public static String removePant(String type) {
        PlantType plant = PlantType.getByName(type);
        if (plant == null) {
            return "[ERROR] Invalid plant name.";
        }
        if (AppModel.selectionDeck.isEmpty()) {
            return "[ERROR] Selection deck is already empty.";
        }
        if (!AppModel.selectionDeck.containsKey(plant)) {
            return "[ERROR] Plant type " + plant + " does not exist in the selection deck.";
        }

        AppModel.selectionDeck.remove(plant);
        return "Removed plant " + plant + "from selection deck. (capacity " + AppModel.selectionDeck.size() + "/8)";
    }

    public static String boostPant(String type) {
        PlantType plant = PlantType.getByName(type);
        if (plant == null) {
            return "[ERROR] Invalid plant name.";
        }
        if (AppModel.selectionDeck.isEmpty()) {
            return "[ERROR] Selection deck is already empty.";
        }
        if (!AppModel.selectionDeck.containsKey(plant)) {
            return "[ERROR] Plant type " + plant + " does not exist in the selection deck.";
        }
        if (AppModel.selectionDeck.get(plant)) {
            return "[ERROR] This plant is already boosted!";
        }
        if (!AppModel.player.plantBoosts.get(plant)) {
            return "[ERROR] You do not have a boost for this plant.";
        }

        AppModel.selectionDeck.put(plant, true);
        AppModel.player.plantBoosts.put(plant, false);
        return "Boosted plant " + plant + "from selection deck.";
    }

    public static String startGame() {
        if (AppModel.selectionDeck.size() < 2) {
            return "[ERROR] You must select at least 2 plants.";
        }
        AppModel.gameSession = SessionBuilder.create(AppModel.currentLevel, AppModel.selectionDeck);
        return "Starting level: "
            + AppModel.currentLevel + System.lineSeparator() + AppController.changeMenu(Menu.GAME);
    }

    public static String cancelGame() {
        AppModel.clearSessionData();
        return "Mission aborted!" + System.lineSeparator() + AppController.changeMenu(Menu.GAME);
    }
}
