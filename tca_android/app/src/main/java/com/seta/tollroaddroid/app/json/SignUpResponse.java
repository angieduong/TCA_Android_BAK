package com.seta.tollroaddroid.app.json;

/**
 * Created by thomashuang on 16-03-21.
 */
public class SignUpResponse {
    private int success;
    private int status;
    private String message;
    private String tokenID;
    private String uniqueID;
    private String account_number;
    private String account_message;

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

    public String getAccount_number() {
        return account_number;
    }

    public void setAccount_number(String account_number) {
        this.account_number = account_number;
    }

    public String getAccount_message() {
        return account_message;
    }

    public void setAccount_message(String account_message) {
        this.account_message = account_message;
    }
}
