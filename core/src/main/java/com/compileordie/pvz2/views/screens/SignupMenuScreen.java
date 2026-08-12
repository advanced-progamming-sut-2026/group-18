package com.compileordie.pvz2.views.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.compileordie.pvz2.controllers.menus.auth.SignupMenuController;
import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.models.repositories.configs.ConfigManager;
import com.compileordie.pvz2.views.ScreenManager;
import com.compileordie.pvz2.views.ScreenType;
import com.compileordie.pvz2.views.helpers.ToastManager;
import pvz.skin.BorderedTable;

public class SignupMenuScreen extends MenuScreen {
    @Override
    public void showCore() {
        Stack stack = new Stack();
        stack.setFillParent(true);
        stage.addActor(stack);

        BorderedTable form = new BorderedTable();
        stack.add(form);

        Label title = new Label("Create Account", skin, "big_outline");
        form.add(title).colspan(2).padBottom(30).center().row();

        // Left Column setup
        TextField userField = createTextField("Username", false);
        TextField passField = createTextField("Password", true);
        TextField passConfirmField = createTextField("Confirm Password", true);
        TextField emailField = createTextField("Email Address", false);
        form.add(buildLeftColumn(userField, passField, passConfirmField, emailField)).padRight(20).top();

        // Right Column setup
        TextField nickField = createTextField("Nickname", false);
        SelectBox<String> genderBox = new SelectBox<>(skin);
        genderBox.setItems("Male", "Female");
        SelectBox<String> questionBox = new SelectBox<>(skin);
        questionBox.setItems(ConfigManager.securityQuestion().questions.toArray(new String[0]));
        TextField answerField = createTextField("Security Answer", false);
        form.add(buildRightColumn(nickField, genderBox, questionBox, answerField)).padLeft(20).top().row();

        // Action Buttons
        form.add(buildButtonTable(userField, passField, passConfirmField, nickField, emailField,
            genderBox, questionBox, answerField)).colspan(2).padTop(20).center();

        buildPeripheralNav(stack);
    }

    private TextField createTextField(String messageText, boolean isPassword) {
        TextField textField = new TextField("", skin);
        textField.setMessageText(messageText);
        if (isPassword) {
            textField.setPasswordMode(true);
            textField.setPasswordCharacter('*');
        }
        return textField;
    }

    private Table buildLeftColumn(TextField userField,
                                  TextField passField,
                                  TextField passConfirmField,
                                  TextField emailField) {
        Table leftCol = new Table();
        leftCol.add(userField).size(250, 45).padBottom(15).row();
        leftCol.add(passField).size(250, 45).padBottom(15).row();
        leftCol.add(passConfirmField).size(250, 45).padBottom(15).row();
        leftCol.add(emailField).size(250, 45).padBottom(15).row();
        return leftCol;
    }

    private Table buildRightColumn(TextField nickField,
                                   SelectBox<String> genderBox,
                                   SelectBox<String> questionBox,
                                   TextField answerField) {
        Table rightCol = new Table();
        rightCol.add(nickField).size(250, 45).padBottom(15).row();
        rightCol.add(genderBox).size(250, 45).padBottom(15).row();
        rightCol.add(questionBox).size(250, 45).padBottom(15).row();
        rightCol.add(answerField).size(250, 45).padBottom(15).row();
        return rightCol;
    }

    private Table buildButtonTable(TextField userField, TextField passField, TextField passConfirmField,
                                   TextField nickField, TextField emailField, SelectBox<String> genderBox,
                                   SelectBox<String> questionBox, TextField answerField) {
        Table buttonTable = new Table();
        TextButton loginBtn = new TextButton("Login", skin, "brown");
        TextButton registerBtn = new TextButton("Register", skin, "green");

        loginBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.LOGIN);
            }
        });

        registerBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Result<Void> result = SignupMenuController.registerUser(
                    userField.getText(), passField.getText(), passConfirmField.getText(),
                    nickField.getText(), emailField.getText(), genderBox.getSelected(),
                    questionBox.getSelected(), answerField.getText()
                );

                if (result.isSuccess) {
                    ToastManager.showSuccess("Account created successfully!");
                    ScreenManager.setMenuScreen(ScreenType.LOGIN);
                } else {
                    ToastManager.showError(result.errorMessage);
                }
            }
        });

        buttonTable.add(loginBtn).size(180, 60).padRight(20);
        buttonTable.add(registerBtn).size(180, 60);
        return buttonTable;
    }

    private void buildPeripheralNav(Stack stack) {
        Table peripheralNav = new Table();
        peripheralNav.setFillParent(true);
        stack.add(peripheralNav);

        ImageButton closeBtn = new ImageButton(skin, "generic_close_circle");
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        // Place the navigation buttons in the bottom-left corner
        peripheralNav.bottom().left().pad(35);
        peripheralNav.add(closeBtn).size(64, 64);
    }
}
