package com.compileordie.pvz2.views.customelements;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.compileordie.pvz2.controllers.menus.auth.ProfileMenuController;
import com.compileordie.pvz2.models.components.Result;
import com.compileordie.pvz2.models.user.Player;
import com.compileordie.pvz2.views.helpers.ToastManager;

public class EditProfileModal extends BaseModal {
    public EditProfileModal(Skin skin, Player currentUser, Runnable onSuccessUpdate) {
        super("Edit Profile", skin);

        TextField userField = new TextField(currentUser.username, skin);
        userField.setMessageText("Username");

        TextField nickField = new TextField(currentUser.nickname, skin);
        nickField.setMessageText("Nickname");

        TextField emailField = new TextField(currentUser.email, skin);
        emailField.setMessageText("Email Address");

        bodyTable.top();
        bodyTable.add(userField).size(300, 45).padBottom(15).row();
        bodyTable.add(nickField).size(300, 45).padBottom(15).row();
        bodyTable.add(emailField).size(300, 45).padBottom(20).row();

        TextButton cancelBtn = new TextButton("Cancel", skin, "brown");
        TextButton saveBtn = new TextButton("Save", skin, "green");

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
                Result<Void> result = ProfileMenuController.updateProfile(
                    userField.getText(),
                    nickField.getText(),
                    emailField.getText()
                );

                if (result.isSuccess) {
                    ToastManager.showSuccess("Profile updated successfully!");
                    onSuccessUpdate.run(); // Refreshes the parent screen labels
                    hide();
                } else {
                    ToastManager.showError(result.errorMessage);
                }
            }
        });
    }
}
