package com.compileordie.pvz2.views.customelements;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.compileordie.pvz2.controllers.menus.auth.LoginMenuController;
import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.views.helpers.ToastManager;
import pvz.skin.PvzSkin;

public class ForgotPasswordModal extends BaseModal {
    public ForgotPasswordModal() {
        super("Reset Password");
        TextField userField = new TextField("", PvzSkin.get());
        userField.setMessageText("Username");
        TextField emailField = new TextField("", PvzSkin.get());
        emailField.setMessageText("Email Address");
        TextField answerField = new TextField("", PvzSkin.get());
        answerField.setMessageText("Security Answer");
        TextField newPassField = new TextField("", PvzSkin.get());
        newPassField.setMessageText("New Password");
        newPassField.setPasswordMode(true);
        newPassField.setPasswordCharacter('*');

        bodyTable.add(userField).size(300, 45).padBottom(15).row();
        bodyTable.add(emailField).size(300, 45).padBottom(15).row();
        bodyTable.add(answerField).size(300, 45).padBottom(15).row();
        bodyTable.add(newPassField).size(300, 45).padBottom(15).row();

        TextButton cancelBtn = new TextButton("Cancel", PvzSkin.get(), "brown");
        TextButton resetBtn = new TextButton("Reset", PvzSkin.get(), "green");

        buttonTable.add(cancelBtn).size(150, 50).padRight(20);
        buttonTable.add(resetBtn).size(150, 50);

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
                    userField.getText(),
                    emailField.getText(),
                    answerField.getText(),
                    newPassField.getText()
                );

                if (result.isSuccess) {
                    ToastManager.showSuccess("Password changed successfully!");
                    hide();
                } else {
                    ToastManager.showError(result.errorMessage);
                }
            }
        });
    }
}
