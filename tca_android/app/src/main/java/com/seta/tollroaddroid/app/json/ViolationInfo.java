package com.seta.tollroaddroid.app.json;

import java.io.Serializable;

/**
 * Created by thomashuang on 2018-03-27.
 */

public class ViolationInfo implements Serializable {
  //  "violation_number":"422862674","violation_date":"10/10/2017 8:28AM","toll_point":"SR73 Catalina View South","plate_state":"CA","plate_number":"4MFE580","amount_due":"$106.49","due_date":"02/08/2018"
    private String violation_number;
    private String violation_date;
    private String toll_point;
    private String plate_state;
    private String plate_number;
    private String amount_due;
    private String due_date;

    private boolean isSelected = false;

    public String getViolation_number() {
        return violation_number;
    }

    public void setViolation_number(String violation_number) {
        this.violation_number = violation_number;
    }

    public String getViolation_date() {
        return violation_date;
    }

    public void setViolation_date(String violation_date) {
        this.violation_date = violation_date;
    }

    public String getToll_point() {
        return toll_point;
    }

    public void setToll_point(String toll_point) {
        this.toll_point = toll_point;
    }

    public String getPlate_state() {
        return plate_state;
    }

    public void setPlate_state(String plate_state) {
        this.plate_state = plate_state;
    }

    public String getPlate_number() {
        return plate_number;
    }

    public void setPlate_number(String plate_number) {
        this.plate_number = plate_number;
    }

    public String getAmount_due() {
        return amount_due;
    }

    public void setAmount_due(String amount_due) {
        this.amount_due = amount_due;
    }

    public String getDue_date() {
        return due_date;
    }

    public void setDue_date(String due_date) {
        this.due_date = due_date;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
