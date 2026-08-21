package com.compileordie.pvz2.controllers.menus.game;

import com.compileordie.pvz2.models.entities.plants.types.PlantType;
import com.compileordie.pvz2.models.game.levels.LevelID;

import java.util.ArrayList;
import java.util.Arrays;

public class GameMenuController {
    private GameMenuController() {
    }

    public static ArrayList<PlantType> getPlants(LevelID levelID) {
        if (levelID == LevelID.LOCKED_PLANTS) {
            return new ArrayList<>(Arrays.asList(
                PlantType.GOLD_BLOOM,
                PlantType.PRIMAL_SUNFLOWER,
                PlantType.PRIMAL_POTATO_MINE,
                PlantType.GRAVE_BUSTER,
                PlantType.MEGA_GATLING_PEA,
                PlantType.TORCHWOOD,
                PlantType.EXPLODE_O_NUT,
                PlantType.APPEASE_MINT
            ));
        } else if (levelID == LevelID.PLANT_WHAT_YOU_GET) {
            ArrayList<PlantType> plants = new ArrayList<>(Arrays.asList(PlantType.values()));
            plants.removeAll(Arrays.asList(
                PlantType.SUNFLOWER,
                PlantType.TWIN_SUNFLOWER,
                PlantType.SUN_SHROOM,
                PlantType.PRIMAL_SUNFLOWER,
                PlantType.GOLD_BLOOM,
                PlantType.SUN_BEAN,
                PlantType.ENLIGHTEN_MINT
            ));
            return plants;
        } else if (levelID == LevelID.WALNUT_BOWLING) {
            // TODO: Add Bowling Walnut and Giant Bowling Walnut
            return new ArrayList<>(Arrays.asList(PlantType.EXPLODE_O_NUT));
        } else {
            return new ArrayList<>(Arrays.asList(PlantType.values()));
        }
    }
}
