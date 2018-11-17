package com.seta.tollroaddroid.app.json;

/**
 * Created by thomashuang on 16-04-01.
 */
public class CalRatesResponse {
    private int success;
    private int status;
    private String message;
    private String tokenID;
    private String uniqueID;

    private String peak_rate;
    private String offpeak_rate;
    private String weekend_rate;
    private String ott_rate;

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

    public String getPeak_rate() {
        return peak_rate;
    }

    public void setPeak_rate(String peak_rate) {
        this.peak_rate = peak_rate;
    }

    public String getOffpeak_rate() {
        return offpeak_rate;
    }

    public void setOffpeak_rate(String offpeak_rate) {
        this.offpeak_rate = offpeak_rate;
    }

    public String getWeekend_rate() {
        return weekend_rate;
    }

    public void setWeekend_rate(String weekend_rate) {
        this.weekend_rate = weekend_rate;
    }

    public String getOtt_rate() {
        return ott_rate;
    }

    public void setOtt_rate(String ott_rate) {
        this.ott_rate = ott_rate;
    }
}
