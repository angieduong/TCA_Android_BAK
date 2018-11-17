package com.seta.tollroaddroid.app.json;

/**
 * Created by thomashuang on 16-03-23.
 * "
 */
public class CalcTripInfoResponse {
    //{"success":1,"status":200,"tokenID":"50XBHH4777ZoTbDU3PwPSSjXBsOZffXC6zAGacQR","uniqueID":"","amount_due":7.61,"previous_amount_due":0.00,"previous_due_message":"","info":[]}
    private int success;
    private int status;
    private String previous_due_message;
    private String tokenID;
    private String uniqueID;
    private float amount_due;
    private float previous_amount_due;

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

    public String getPrevious_due_message() {
        return previous_due_message;
    }

    public void setPrevious_due_message(String previous_due_message) {
        this.previous_due_message = previous_due_message;
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

    public float getAmount_due() {
        return amount_due;
    }

    public void setAmount_due(float amount_due) {
        this.amount_due = amount_due;
    }

    public float getPrevious_amount_due() {
        return previous_amount_due;
    }

    public void setPrevious_amount_due(float previous_amount_due) {
        this.previous_amount_due = previous_amount_due;
    }
}
