package com.compileordie.pvz2.views;

import com.badlogic.gdx.Gdx;
import com.compileordie.pvz2.controllers.AppController;
import com.compileordie.pvz2.models.AppModel;

import java.util.Scanner;

public class AppView {
    private AppView() {
    }

    private static void printIfPresent(String message) {
        if (message != null && !message.isEmpty()) {
            System.out.println(message);
        }
    }

    public static void run() {
        Scanner scanner = new Scanner(System.in);
        do {
            if (!scanner.hasNextLine()) {
                AppModel.stop();
                continue;
            }

            printIfPresent(AppController.getBeforePrompt());
            String prompt = scanner.nextLine().trim();
            printIfPresent(AppController.getAfterPrompt());
        } while (AppModel.isRunning);

        Gdx.app.exit();
    }
}
