package com.compileordie.pvz2.utils;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.user.authentication.SecurityQuestion;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class DatabaseSeeder {

    static void main(String[] args) {
        generateSecurityQuestions();
    }

    private static void generateSecurityQuestions() {
        ArrayList<SecurityQuestion> questions = new ArrayList<>();

        questions.add(new SecurityQuestion(1, "What was the name of your first pet?"));
        questions.add(new SecurityQuestion(2, "In what city were you born?"));
        questions.add(new SecurityQuestion(3, "What is the name of your favorite childhood teacher?"));
        questions.add(new SecurityQuestion(5, "What was the model of your first car?"));
        questions.add(new SecurityQuestion(6, "What was your childhood nickname?"));
        questions.add(new SecurityQuestion(7, "What is the name of the hospital where you were born?"));
        questions.add(new SecurityQuestion(8, "What was the first video game you ever completed?"));

        Json json = new Json();
        json.setOutputType(OutputType.json);
        try (FileWriter writer = new FileWriter("assets/data/" + Constants.Paths.SECURITY_QUESTIONS)) {
            writer.write(json.prettyPrint(questions, 0)
                .replace("\t", "  ")
                .replace("\n", "\n  ")
                .replace("\n  ]", "\n]"));
            System.out.println("Successfully created " + Constants.Paths.SECURITY_QUESTIONS);
        } catch (IOException e) {
            System.out.println("[ERROR] Something went while creating the " + Constants.Paths.SECURITY_QUESTIONS);
            e.printStackTrace();
        }
    }
}
