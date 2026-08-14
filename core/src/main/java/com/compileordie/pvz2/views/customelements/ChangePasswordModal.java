package com.compileordie.pvz2.views.customelements;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.compileordie.pvz2.controllers.menus.auth.ProfileMenuController;
import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.views.helpers.ToastManager;

public class ChangePasswordModal extends BaseModal {
    public ChangePasswordModal(Skin skin) {
        super("Change Password", skin);

        TextField oldPassField = new TextField("", skin);
        oldPassField.setMessageText("Current Password");
        oldPassField.setPasswordMode(true);
        oldPassField.setPasswordCharacter('*');

        TextField newPassField = new TextField("", skin);
        newPassField.setMessageText("New Password");
        newPassField.setPasswordMode(true);
        newPassField.setPasswordCharacter('*');

        bodyTable.top();
        bodyTable.add(oldPassField).size(300, 45).padBottom(15).row();
        bodyTable.add(newPassField).size(300, 45).padBottom(20).row();

        TextButton cancelBtn = new TextButton("Cancel", skin, "brown");
        TextButton saveBtn = new TextButton("Confirm", skin, "green");

        buttonTable.add(cancelBtn).size(140, 50).padRight(20);
        buttonTable.add(saveBtn).size(140, 50);

        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
            }
        });

        saveBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Result<Void> result = ProfileMenuController.changePassword(
                    oldPassField.getText(),
                    newPassField.getText()
                );

                if (result.isSuccess) {
                    ToastManager.showSuccess("Password changed securely!");
                    hide();
                } else {
                    ToastManager.showError(result.errorMessage);
                }
            }
        });
    }
}
