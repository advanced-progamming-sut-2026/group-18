package com.compileordie.pvz2.controllers.menus.auth;

import com.compileordie.pvz2.controllers.AppController;
import com.compileordie.pvz2.models.AppModel;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;
import com.compileordie.pvz2.models.repositories.databases.AuthDatabase;
import com.compileordie.pvz2.models.user.Gender;
import com.compileordie.pvz2.models.user.UserValidator;
import com.compileordie.pvz2.models.user.authentication.AuthManager;
import com.compileordie.pvz2.views.helpers.Menu;

import java.util.ArrayList;
import java.util.Random;

public class SignupMenuController {

    private static PendingRegistration pendingUser = null;

    private SignupMenuController() {
    }

    public static String enterMenu(String name) {
        Menu menu = Menu.getByName(name);
        if (menu == null) {
            return "[ERROR] Wrong menu name.";
        }
        if (menu != Menu.LOGIN) {
            return "[ERROR] You can only enter Login Menu from here.";
        }

        pendingUser = null;
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

        return "Exiting the game..." + System.lineSeparator() + randomPhrase;
    }

    public static String registerUser(String username,
                                      String password, String passwordConfirm,
                                      String nickname, String email, String gender) {
        if (!UserValidator.isUsernameValid(username)) {
            return "[ERROR] Invalid username format.";
        }
        if (new AuthDatabase().loadOne(username) != null) {
            return "[ERROR] This username is already taken.";
        }
        String passwordStrength = UserValidator.isPasswordStrong(password);
        if (passwordStrength != null) {
            return passwordStrength;
        }
        if (!UserValidator.isNicknameValid(nickname)) {
            return "[ERROR] Nickname must be 3 to 30 characters.";
        }
        if (!UserValidator.isEmailValid(email)) {
            return "[ERROR] Invalid email format.";
        }
        Gender genderType = switch (gender.toLowerCase()) {
            case "male" -> Gender.MALE;
            case "female" -> Gender.FEMALE;
            default -> null;
        };
        if (genderType == null) {
            return "[ERROR] (Call be transphobic but) Invalid gender type.";
        }
        if (!password.equals(passwordConfirm)) {
            return "[ERROR] Passwords do not match.";
        }

        pendingUser = new PendingRegistration(username, password, nickname, email, genderType);
        int counter = 1;
        for (String securityQuestion : ConfigManager.securityQuestion().questions) {
            AppModel.addAfterPrompt(counter + " - " + securityQuestion);
            counter++;
        }
        AppModel.addAfterPrompt("Use the command bellow:");
        AppModel.addAfterPrompt("pick question -q <question_number> -a <answer> -c <answer_confirm>");
        return "Registration Step 1 complete!" + System.lineSeparator() + "Now pick and answer a security questions:";
    }

    public static String pickQuestionUser(String questionNumber, String answer, String answerConfirm) {
        if (pendingUser == null) {
            return "[ERROR] You must complete the 'register' command first!";
        }
        if (!answer.equalsIgnoreCase(answerConfirm)) {
            return "[ERROR] Answers do not match. Please try picking the question again.";
        }
        int number;
        try {
            number = Integer.parseInt(questionNumber);
        } catch (NumberFormatException e) {
            return "[ERROR] Invalid question number, please enter an integer.";
        }
        ArrayList<String> securityQuestions = ConfigManager.securityQuestion().questions;
        if (number < 1 || number > securityQuestions.size()) {
            return "[ERROR] Invalid question number, your number must be between 1 and "
                + securityQuestions.size() + ".";
        }

        String question = securityQuestions.get(number - 1);
        AuthManager.signupPlayer(pendingUser.username(),
            pendingUser.password().toCharArray(),
            pendingUser.nickname(),
            pendingUser.email(),
            pendingUser.gender(),
            question,
            answer);
        String username = pendingUser.username();
        pendingUser = null;
        AppModel.menu = Menu.LOGIN;
        return "Account created successfully for '" + username + "'."
            + System.lineSeparator() + AppController.showCurrentMenu();
    }

    private record PendingRegistration(
        String username,
        String password,
        String nickname,
        String email,
        Gender gender
    ) {
    }
}
