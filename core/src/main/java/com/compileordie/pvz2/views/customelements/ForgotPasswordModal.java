package com.compileordie.pvz2.views.customelements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.compileordie.pvz2.controllers.menus.auth.LoginMenuController;
import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.views.helpers.ToastManager;

public class ForgotPasswordModal extends BaseModal {
    public ForgotPasswordModal(Skin skin) {
        super("Reset Password", skin);

        TextField userField = createTextField("Username", skin, false);
        TextField emailField = createTextField("Email Address", skin, false);
        TextField answerField = createTextField("Security Answer", skin, false);
        TextField newPassField = createTextField("New Password", skin, true);

        Label questionLabel = createQuestionLabel(skin);
        TextButton fetchBtn = new TextButton("Get Question", skin, "default");
        Table phaseTwoTable = buildPhaseTwoTable(answerField, newPassField);

        setupBodyTable(userField, emailField, questionLabel); // fetchBtn removed here

        TextButton cancelBtn = new TextButton("Cancel", skin, "brown");
        TextButton resetBtn = new TextButton("Reset", skin, "green");
        resetBtn.setVisible(false);

        // Create a Stack to hold both buttons in the exact same layout space
        Stack actionStack = new Stack();
        actionStack.add(resetBtn);
        actionStack.add(fetchBtn);

        buttonTable.add(cancelBtn).size(150, 50).padRight(20);
        buttonTable.add(actionStack).size(150, 50); // Add the Stack instead of just resetBtn

        attachListeners(userField, emailField, answerField, newPassField,
            questionLabel, fetchBtn, phaseTwoTable, cancelBtn, resetBtn);
    }

    private TextField createTextField(String message, Skin skin, boolean isPassword) {
        TextField field = new TextField("", skin);
        field.setMessageText(message);
        if (isPassword) {
            field.setPasswordMode(true);
            field.setPasswordCharacter('*');
        }
        return field;
    }

    private Label createQuestionLabel(Skin skin) {
        Label label = new Label("Enter username and email to fetch your security question.", skin, "medium");
        label.setColor(Color.BROWN);
        label.setFontScale(0.85f);
        label.setWrap(true);
        return label;
    }

    private Table buildPhaseTwoTable(TextField answerField, TextField newPassField) {
        Table table = new Table();
        table.setVisible(false);
        table.add(answerField).size(300, 45).padBottom(15).row();
        table.add(newPassField).size(300, 45).row();
        return table;
    }

    private void setupBodyTable(TextField userField, TextField emailField, Label questionLabel) {
        bodyTable.top();
        bodyTable.add(userField).size(300, 45).padBottom(15).row();
        bodyTable.add(emailField).size(300, 45).padBottom(15).row();
        bodyTable.add(questionLabel).width(300).padBottom(20).center().row();
    }

    private void attachListeners(TextField userField, TextField emailField, TextField answerField,
                                 TextField newPassField, Label questionLabel, TextButton fetchBtn,
                                 Table phaseTwoTable, TextButton cancelBtn, TextButton resetBtn) {
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide(); // Provided by BaseModal
            }
        });

        resetBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Result<Void> result = LoginMenuController.resetPassword(
                    userField.getText(), emailField.getText(),
                    answerField.getText(), newPassField.getText()
                );

                if (result.isSuccess) {
                    ToastManager.showSuccess("Password changed successfully!");
                    hide();
                } else {
                    ToastManager.showError(result.errorMessage);
                }
            }
        });

        fetchBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Pass fetchBtn into the handler
                handleFetchClick(userField, emailField, questionLabel, phaseTwoTable, resetBtn, fetchBtn);
            }
        });
    }

    private void handleFetchClick(TextField userField, TextField emailField, Label questionLabel,
                                  Table phaseTwoTable, TextButton resetBtn, TextButton fetchBtn) {
        Result<String> result = LoginMenuController.fetchSecurityQuestion(userField.getText(), emailField.getText());

        if (result.isSuccess) {
            questionLabel.setText("Type in your " + result.data.toLowerCase());
            questionLabel.setAlignment(Align.center);

            // 1. Clear the layout
            bodyTable.clearChildren();

            // 2. Rebuild the layout (leaving out the fetchBtn entirely)
            bodyTable.add(userField).size(300, 45).padBottom(15).row();
            bodyTable.add(emailField).size(300, 45).padBottom(15).row();
            bodyTable.add(questionLabel).width(300).padBottom(20).center().row();
            bodyTable.add(phaseTwoTable).row();

            // 3. Make the hidden elements visible
            phaseTwoTable.setVisible(true);
            resetBtn.setVisible(true);
            fetchBtn.setVisible(false); // Hide the fetch button so the reset button takes over!

            // 4. Lock Phase 1 inputs
            userField.setDisabled(true);
            emailField.setDisabled(true);
        } else {
            ToastManager.showError(result.errorMessage);
        }
    }
}
