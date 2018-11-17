package com.seta.tollroaddroid.app.json;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by thomashuang on 2018-03-28.
 */

public class SelectedUnpaidViolationsResponse implements Serializable {
    //{"success":1,"status":0,"message":"","has_pay_plan":0,"ott_allowed_num":0,"sts_allowed":0,"tokenID":"19503630XIOJZoM8zJKWgaSWssaTWz0kbSegaxZf","uniqueID":"42cb6485-66c5-4593-ad59-8cb2ef0c0200","sel_unpaid_vios_total_amount_due":"$0.00","sel_unpaid_vios_info":[]}
    private int success;
    private int status;
    private String message;
    private String tokenID;
    private String uniqueID;
    private String sel_unpaid_vios_total_amount_due;
    private ArrayList<ViolationInfo> sel_unpaid_vios_info;

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

    public String getSel_unpaid_vios_total_amount_due() {
        return sel_unpaid_vios_total_amount_due;
    }

    public void setSel_unpaid_vios_total_amount_due(String sel_unpaid_vios_total_amount_due) {
        this.sel_unpaid_vios_total_amount_due = sel_unpaid_vios_total_amount_due;
    }

    public ArrayList<ViolationInfo> getSel_unpaid_vios_info() {
        return sel_unpaid_vios_info;
    }

    public void setSel_unpaid_vios_info(ArrayList<ViolationInfo> sel_unpaid_vios_info) {
        this.sel_unpaid_vios_info = sel_unpaid_vios_info;
    }
}
