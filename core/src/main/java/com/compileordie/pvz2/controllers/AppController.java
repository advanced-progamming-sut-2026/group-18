package com.compileordie.pvz2.controllers;

import com.compileordie.pvz2.models.AppModel;

import java.util.StringJoiner;

public class AppController {
    private AppController() {
    }

    public static String getBeforePrompt() {
        StringJoiner joiner = new StringJoiner(System.lineSeparator());

        while (AppModel.hasBeforePrompt()) {
            joiner.add(AppModel.getBeforePrompt());
        }

        return joiner.toString();
    }

    public static String getAfterPrompt() {
        StringJoiner joiner = new StringJoiner(System.lineSeparator());

        while (AppModel.hasAfterPrompt()) {
            joiner.add(AppModel.getAfterPrompt());
        }

        return joiner.toString();
    }
}
