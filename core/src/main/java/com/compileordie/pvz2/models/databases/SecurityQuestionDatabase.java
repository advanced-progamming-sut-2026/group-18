package com.compileordie.pvz2.models.databases;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.compileordie.pvz2.config.Constants;
import com.compileordie.pvz2.models.databases.fundamentals.ImmutableDatabase;
import com.compileordie.pvz2.models.user.authentication.SecurityQuestion;

import java.util.ArrayList;

public class SecurityQuestionDatabase extends ImmutableDatabase<ArrayList<SecurityQuestion>> {
    public SecurityQuestionDatabase() {
        super(Constants.Paths.SECURITY_QUESTIONS);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<SecurityQuestion> load() {
        FileHandle file = Gdx.files.local(filePath);
        if (file.exists()) {
            return json.fromJson(ArrayList.class, SecurityQuestion.class, file);
        } else {
            return new ArrayList<>();
        }
    }

    public SecurityQuestion loadOne(int id) {
        ArrayList<SecurityQuestion> database = load();
        for (SecurityQuestion securityQuestion : database) {
            if (securityQuestion.getId() == id) return securityQuestion;
        }
        return null;
    }

    public int getNumberOfQuestions() {
        return load().size();
    }
}
