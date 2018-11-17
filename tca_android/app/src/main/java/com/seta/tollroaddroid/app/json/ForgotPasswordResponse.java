package com.seta.tollroaddroid.app.json;

/**
 * Created by thomashuang on 16-04-11.
 */
public class ForgotPasswordResponse {
    private int success;
    private int status;
    private String message;
    private String tokenID;
    private String uniqueID;

    private String question1ID;
    private String question1_text;
    private String question2ID;
    private String question2_text;
    private String question3ID;
    private String question3_text;
    private boolean sent_code;

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTokenID() {
        return tokenID;
    }

    public void setTokenID(String tokenID) {
        this.tokenID = tokenID;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public String getQuestion1ID() {
        return question1ID;
    }

    public void setQuestion1ID(String question1ID) {
        this.question1ID = question1ID;
    }

    public String getQuestion1_text() {
        return question1_text;
    }

    public void setQuestion1_text(String question1_text) {
        this.question1_text = question1_text;
    }

    public String getQuestion2ID() {
        return question2ID;
    }

    public void setQuestion2ID(String question2ID) {
        this.question2ID = question2ID;
    }

    public String getQuestion2_text() {
        return question2_text;
    }

    public void setQuestion2_text(String question2_text) {
        this.question2_text = question2_text;
    }

    public String getQuestion3ID() {
        return question3ID;
    }

    public void setQuestion3ID(String question3ID) {
        this.question3ID = question3ID;
    }

    public String getQuestion3_text() {
        return question3_text;
    }

    public void setQuestion3_text(String question3_text) {
        this.question3_text = question3_text;
    }

    public boolean isSent_code() {
        return sent_code;
    }

    public void setSent_code(boolean sent_code) {
        this.sent_code = sent_code;
    }
}
