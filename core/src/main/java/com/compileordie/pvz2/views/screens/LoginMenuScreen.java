package com.compileordie.pvz2.views.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.compileordie.pvz2.controllers.menus.auth.LoginMenuController;
import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.views.ScreenManager;
import com.compileordie.pvz2.views.ScreenType;
import com.compileordie.pvz2.views.customelements.ForgotPasswordModal;
import com.compileordie.pvz2.views.helpers.ToastManager;
import pvz.skin.BorderedTable;

public class LoginMenuScreen extends MenuScreen {
    @Override
    public void showCore() {
        Stack stack = new Stack();
        stack.setFillParent(true);
        stage.addActor(stack);

        // BorderedTable natively applies the PvZ dialog background and a pad(50).
        BorderedTable form = new BorderedTable();
        stack.add(form);

        Label title = new Label("Login", skin, "big_outline");
        form.add(title).padBottom(30).center().row();

        // Credentials inputs
        TextField userField = createTextField("Username", false);
        TextField passField = createTextField("Password", true);
        CheckBox stayLoggedInBox = new CheckBox(" Stay logged in", skin, "default");
        stayLoggedInBox.getLabel().setColor(Color.BROWN);

        // Add layout blocks to main form
        form.add(buildCenterColumn(userField, passField, stayLoggedInBox)).top().row();
        form.add(buildButtonTable(userField, passField, stayLoggedInBox)).padTop(20).center();
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

    private Table buildCenterColumn(TextField userField, TextField passField, CheckBox stayLoggedInBox) {
        Table centerCol = new Table();
        centerCol.add(userField).size(320, 45).padBottom(15).row();
        centerCol.add(passField).size(320, 45).padBottom(15).row();
        centerCol.add(stayLoggedInBox).left().padBottom(15).row();
        return centerCol;
    }

    private Table buildButtonTable(TextField userField, TextField passField, CheckBox stayLoggedInBox) {
        Table buttonTable = new Table();
        TextButton loginBtn = new TextButton("Login", skin, "green");
        TextButton signupBtn = new TextButton("SignUp", skin, "brown");
        TextButton forgotBtn = new TextButton("Forgot Password?", skin, "default");

        buttonTable.add(signupBtn).size(150, 60).padRight(15);
        buttonTable.add(loginBtn).size(150, 60).row();
        buttonTable.add(forgotBtn).colspan(2).size(315, 50).padTop(15).center();

        attachButtonListeners(loginBtn, signupBtn, forgotBtn, userField, passField, stayLoggedInBox);

        return buttonTable;
    }

    private void attachButtonListeners(TextButton loginBtn, TextButton signupBtn, TextButton forgotBtn,
                                       TextField userField, TextField passField, CheckBox stayLoggedInBox) {
        loginBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String username = userField.getText();
                String password = passField.getText();
                boolean stayLoggedIn = stayLoggedInBox.isChecked();

                Result<Void> result = LoginMenuController.loginUser(username, password, stayLoggedIn);

                if (result.isSuccess) {
                    ToastManager.showSuccess("Logged in successfully!");
                    ScreenManager.setMenuScreen(ScreenType.MAIN);
                } else {
                    ToastManager.showError(result.errorMessage);
                }
            }
        });

        signupBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.setMenuScreen(ScreenType.SIGNUP);
            }
        });

        forgotBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ForgotPasswordModal modal = new ForgotPasswordModal();
                modal.show(stage);
            }
        });
    }
}
