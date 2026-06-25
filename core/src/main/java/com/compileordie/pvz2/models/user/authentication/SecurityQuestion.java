package com.compileordie.pvz2.models.user.authentication;

public class SecurityQuestion {
    private int id;
    private String text;

    public SecurityQuestion() {
    }

    public SecurityQuestion(int id, String text) {
        this.id = id;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
