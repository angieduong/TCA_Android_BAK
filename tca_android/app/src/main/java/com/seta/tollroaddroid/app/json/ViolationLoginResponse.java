package com.seta.tollroaddroid.app.json;

import java.util.ArrayList;

/**
 * Created by thomashuang on 2018-03-27.
 */

public class ViolationLoginResponse {
    //{"success":1,"status":200,"message":"","has_pay_plan":0,"ott_allowed_num":0,"sts_allowed":0,"tokenID":"19503630XIOJZoM8zJKWgaSWssaTWVz5xmBknbxr","uniqueID":"42cb6485-66c5-4593-ad59-8cb2ef0c0200","alert_list":[],"unpaid_vios_total_amount_due":"$958.41","unpaid_vios_info":[]}
    private int success;
    private int status;
    private String message;
    private String tokenID;
    private String uniqueID;
    private String unpaid_vios_total_amount_due;
    private ArrayList<ViolationInfo> unpaid_vios_info;

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

    public String getUnpaid_vios_total_amount_due() {
        return unpaid_vios_total_amount_due;
    }

    public void setUnpaid_vios_total_amount_due(String unpaid_vios_total_amount_due) {
        this.unpaid_vios_total_amount_due = unpaid_vios_total_amount_due;
    }

    public ArrayList<ViolationInfo> getUnpaid_vios_info() {
        return unpaid_vios_info;
    }

    public void setUnpaid_vios_info(ArrayList<ViolationInfo> unpaid_vios_info) {
        this.unpaid_vios_info = unpaid_vios_info;
    }
}
