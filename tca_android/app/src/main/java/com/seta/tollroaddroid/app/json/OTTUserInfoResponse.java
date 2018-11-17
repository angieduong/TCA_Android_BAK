package com.seta.tollroaddroid.app.json;

import com.google.gson.annotations.SerializedName;

import java.util.PriorityQueue;

/**
 * Created by Thomas on 2015-12-17.
 *
 "success": 1,
 "status": 200,
 "message": "",
 "tokenID": "5870658XwUyU8sOPDHzZyRTuy51HKPz3sVHNTAcY",
 "uniqueID": "84730561-8C06-4B6E-A561-D31B858DD1DC"
 */
public class OTTUserInfoResponse {
    private int success;
    private int status;
    private String message;
    private String tokenID;
    private String uniqueID;

    @SerializedName("previous_due_message")
    private String previousDueMessage;

    @SerializedName("amount_due")
    private double amountDue;

    private double cash_amount;

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

    public String getPreviousDueMessage() {
        return previousDueMessage;
    }

    public void setPreviousDueMessage(String previousDueMessage) {
        this.previousDueMessage = previousDueMessage;
    }

    public double getAmountDue() {
        return amountDue;
    }

    public void setAmountDue(double amountDue) {
        this.amountDue = amountDue;
    }

    public double getCash_amount() {
        return cash_amount;
    }

    public void setCash_amount(double cash_amount) {
        this.cash_amount = cash_amount;
    }
}
