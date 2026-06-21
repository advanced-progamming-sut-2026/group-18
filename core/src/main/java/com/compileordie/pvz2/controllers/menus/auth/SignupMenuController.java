package com.compileordie.pvz2.controllers.menus.auth;

import com.compileordie.pvz2.controllers.AppController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.views.helpers.Menu;

import java.util.Random;

public class SignupMenuController {
    public static String enterMenu(String name) {
        Menu menu = Menu.getByName(name);
        if (menu == null) {
            return "[ERROR] Wrong menu name.";
        }
        if (menu != Menu.LOGIN) {
            return "[ERROR] You can only enter Login Menu from here.";
        }

        return AppController.changeMenu(menu);
    }

    public static String exitMenu() {
        AppModel.stop();

        final String[] EXIT_PHRASES = {
            "See you around!",
            "Go touch some grass!",
            "Thanks for playing!",
            "Until next time!",
            "Catch you later!",
            "Time to face the final boss: Reality!",
            "Achievement unlocked: Going Outside!",
            "Peace out!",
            "Farewell, brave adventurer!",
            "Finally, I can get some sleep!",
            "Have a great day!",
            "May your frame rates be high and your ping be low!"
        };

        Random random = new Random();
        String randomPhrase = EXIT_PHRASES[random.nextInt(EXIT_PHRASES.length)];

        return "Exiting the game." + System.lineSeparator() + randomPhrase;
    }

    public static String registerUser(String username,
                                      String password, String passwordConfirm,
                                      String nickname, String email, String gender) {
        // TODO: To be implemented.
        return "[TODO] This command is not implemented yet.";
    }

    public static String pickQuestionUser(String questionNumber, String answer, String answerConfirm) {
        // TODO: To be implemented.
        return "[TODO] This command is not implemented yet.";
    }
}
