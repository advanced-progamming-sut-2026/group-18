package com.compileordie.pvz2.views.menus.game;

import com.compileordie.pvz2.controllers.menus.game.PlantSelectionMenuController;
import com.compileordie.pvz2.views.helpers.Command;
import com.compileordie.pvz2.views.helpers.MenuView;

public class PlantSelectionMenuView implements MenuView {
    @Override
    public String handleCommandCore(String command) {
        if (Command.SELECTION_SHOW_ALL_PLANTS.matches(command)) {
            return PlantSelectionMenuController.showAllPlants();
        }
        if (Command.SELECTION_SHOW_AVAILABLE_PLANTS.matches(command)) {
            return PlantSelectionMenuController.showAvailablePlants();
        }
        if (Command.SELECTION_ADD_PLANT.matches(command)) {
            String type = Command.SELECTION_ADD_PLANT.getGroup(command, "type");
            return PlantSelectionMenuController.addPant(type);
        }
        if (Command.SELECTION_REMOVE_PLANT.matches(command)) {
            String type = Command.SELECTION_REMOVE_PLANT.getGroup(command, "type");
            return PlantSelectionMenuController.removePant(type);
        }
        if (Command.SELECTION_BOOST_PLANT.matches(command)) {
            String type = Command.SELECTION_BOOST_PLANT.getGroup(command, "type");
            return PlantSelectionMenuController.boostPant(type);
        }
        if (Command.SELECTION_START_GAME.matches(command)) {
            return PlantSelectionMenuController.startGame();
        }
        if (Command.SELECTION_CANCEL_GAME.matches(command)) {
            return PlantSelectionMenuController.cancelGame();
        }
        return null;
    }
}
