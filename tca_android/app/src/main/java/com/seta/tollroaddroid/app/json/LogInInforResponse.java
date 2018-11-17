package com.seta.tollroaddroid.app.json;

import java.util.ArrayList;

/**
 * Created by thomashuang on 16-03-28.
 */
public class LogInInforResponse {

    private String username;
    private String question1ID;
    private String question2ID;
    private String question3ID;
    private String pin;
    private ArrayList<SecQuestion> security_question_list;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getQuestion1ID() {
        return question1ID;
    }

    public void setQuestion1ID(String question1ID) {
        this.question1ID = question1ID;
    }

    public String getQuestion2ID() {
        return question2ID;
    }

    public void setQuestion2ID(String question2ID) {
        this.question2ID = question2ID;
    }

    public String getQuestion3ID() {
        return question3ID;
    }

    public void setQuestion3ID(String question3ID) {
        this.question3ID = question3ID;
    }

    public ArrayList<SecQuestion> getSecurity_question_list() {
        return security_question_list;
    }

    public void setSecurity_question_list(ArrayList<SecQuestion> security_question_list) {
        this.security_question_list = security_question_list;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
