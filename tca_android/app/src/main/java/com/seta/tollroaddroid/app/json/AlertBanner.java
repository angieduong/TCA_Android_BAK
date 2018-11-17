package com.seta.tollroaddroid.app.json;

/**
 * Created by thomashuang on 16-04-04.
 * example:"alert_type":"WARNING","alert_action":"DISPLAY",
 * "alert_message":"There are no active vehicles on your account."
 */
public class AlertBanner {
    private String alert_type;
    private String alert_action;
    private String alert_message;

    public String getAlert_type() {
        return alert_type;
    }

    public void setAlert_type(String alert_type) {
        this.alert_type = alert_type;
    }

    public String getAlert_action() {
        return alert_action;
    }

    public void setAlert_action(String alert_action) {
        this.alert_action = alert_action;
    }

    public String getAlert_message() {
        return alert_message;
    }

    public void setAlert_message(String alert_message) {
        this.alert_message = alert_message;
    }
}
