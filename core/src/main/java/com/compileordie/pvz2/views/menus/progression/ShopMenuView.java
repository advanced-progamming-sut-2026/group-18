package com.compileordie.pvz2.views.menus.progression;

import com.compileordie.pvz2.controllers.menus.progression.ShopMenuController;
import com.compileordie.pvz2.views.helpers.Command;
import com.compileordie.pvz2.views.helpers.MenuView;

public class ShopMenuView implements MenuView {
    @Override
    public String handleCommandCore(String command) {
        if (Command.MENU_EXIT.matches(command)) {
            return ShopMenuController.exitMenu();
        }
        if (Command.SHOP_LIST.matches(command)) {
            return ShopMenuController.showList();
        }
        if (Command.SHOP_DAILY.matches(command)) {
            return ShopMenuController.showDaily();
        }
        if (Command.SHOP_BUY.matches(command)) {
            String id = Command.SHOP_BUY.getGroup(command, "id");
            String count = Command.SHOP_BUY.getGroup(command, "count");
            String type = Command.SHOP_BUY.getGroup(command, "type");
            return ShopMenuController.buy(id, count, type);
        }
        return null;
    }
}
