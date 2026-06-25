package com.compileordie.pvz2.models.user.authentication;

import java.util.UUID;

public class UserRegistry {
    private String username;
    private String passwordHash;
    private String saveField;

    public UserRegistry() {
    }

    public UserRegistry(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.saveField = UUID.randomUUID().toString();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSaveField() {
        return saveField;
    }

    public void setSaveField(String saveField) {
        this.saveField = saveField;
    }
}
