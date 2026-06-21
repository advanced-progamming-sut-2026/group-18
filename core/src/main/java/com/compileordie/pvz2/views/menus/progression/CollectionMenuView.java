package com.compileordie.pvz2.views.menus.progression;

import com.compileordie.pvz2.controllers.menus.progression.CollectionMenuController;
import com.compileordie.pvz2.views.helpers.Command;
import com.compileordie.pvz2.views.helpers.MenuView;

public class CollectionMenuView implements MenuView {
    @Override
    public String handleCommandCore(String command) {
        if (Command.MENU_EXIT.matches(command)) {
            return CollectionMenuController.exitMenu();
        }
        if (Command.COLLECTION_SHOW_PLANTS.matches(command)) {
            return CollectionMenuController.showPlants();
        }
        if (Command.COLLECTION_SHOW_ALL_PLANTS.matches(command)) {
            return CollectionMenuController.showAllPlants();
        }
        if (Command.COLLECTION_SHOW_ZOMBIES.matches(command)) {
            return CollectionMenuController.showZombies();
        }
        if (Command.COLLECTION_SHOW_ALL_ZOMBIES.matches(command)) {
            return CollectionMenuController.showAllZombies();
        }
        if (Command.COLLECTION_SHOW_PLANT.matches(command)) {
            String name = Command.COLLECTION_SHOW_PLANT.getGroup(command, "name");
            return CollectionMenuController.showPlant(name);
        }
        if (Command.COLLECTION_SHOW_ZOMBIE.matches(command)) {
            String name = Command.COLLECTION_SHOW_ZOMBIE.getGroup(command, "name");
            return CollectionMenuController.showZombie(name);
        }
        if (Command.COLLECTION_UPGRADE_PLANT.matches(command)) {
            String name = Command.COLLECTION_UPGRADE_PLANT.getGroup(command, "name");
            return CollectionMenuController.upgradePlant(name);
        }
        if (Command.COLLECTION_PURCHASE_PLANT.matches(command)) {
            String name = Command.COLLECTION_PURCHASE_PLANT.getGroup(command, "name");
            return CollectionMenuController.purchasePlant(name);
        }
        return null;
    }
}
