package com.seta.tollroaddroid.app.json;

/**
 * Created by thomashuang on 16-03-14.
 * "transponder_number": "133945755",
 "transponder_code": "001308060411",
 "assigned_date": "2009-10-30",
 "deposit_amount": 0.00
 */
public class Transponder {
    private String transponder_number;
    private String transponder_code;
    private String assigned_date;
    private double deposit_amount;

    private boolean checked = false;

    public String getTransponder_number() {
        return transponder_number;
    }

    public void setTransponder_number(String transponder_number) {
        this.transponder_number = transponder_number;
    }

    public String getTransponder_code() {
        return transponder_code;
    }

    public void setTransponder_code(String transponder_code) {
        this.transponder_code = transponder_code;
    }

    public String getAssigned_date() {
        return assigned_date;
    }

    public void setAssigned_date(String assigned_date) {
        this.assigned_date = assigned_date;
    }

    public double getDeposit_amount() {
        return deposit_amount;
    }

    public void setDeposit_amount(double deposit_amount) {
        this.deposit_amount = deposit_amount;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
