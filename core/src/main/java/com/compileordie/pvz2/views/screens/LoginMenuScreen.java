package com.compileordie.pvz2.views.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.compileordie.pvz2.controllers.menus.auth.LoginMenuController;
import pvz.skin.BorderedTable;

public class LoginMenuScreen extends MenuScreen {

    @Override
    public void showCore() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        BorderedTable loginPanel = new BorderedTable();
        root.add(loginPanel).expand().center();

        loginPanel.add(new Label("Player Login", skin, "big")).colspan(2).padBottom(30).row();

        // Username Input
        loginPanel.add(new Label("Username:", skin, "medium")).right().padRight(15);
        TextField usernameField = new TextField("", skin);
        loginPanel.add(usernameField).width(250).padBottom(15).row();

        // Password Input
        loginPanel.add(new Label("Password:", skin, "medium")).right().padRight(15);
        TextField passwordField = new TextField("", skin);
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        loginPanel.add(passwordField).width(250).padBottom(20).row();

        // Stay Logged In Toggle
        CheckBox stayLoggedInBox = new CheckBox(" Stay logged in", skin, "default");
        loginPanel.add(stayLoggedInBox).colspan(2).padBottom(30).row();

        // Action Buttons
        Table buttonTable = new Table();
        TextButton loginBtn = new TextButton("Login", skin, "green");
        TextButton backBtn = new TextButton("Back", skin, "brown");

        buttonTable.add(backBtn).size(120, 50).padRight(20);
        buttonTable.add(loginBtn).size(120, 50);
        loginPanel.add(buttonTable).colspan(2);

        loginBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String response = LoginMenuController.loginUser(
                    usernameField.getText(),
                    passwordField.getText(),
                    stayLoggedInBox.isChecked()
                );

                System.out.println(response);
                if (!response.contains("[ERROR]")) {
                    // Transition to MainMenuScreen upon success
                    LoginMenuController.enterMenu("Main");
                }
            }
        });

        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                LoginMenuController.exitMenu();
            }
        });
    }
}
