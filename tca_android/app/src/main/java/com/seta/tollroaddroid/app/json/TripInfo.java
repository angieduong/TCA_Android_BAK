package com.seta.tollroaddroid.app.json;

/**
 * Created by thomashuang on 16-03-31.
 */
public class TripInfo {
    private int trip_num;
    private int from_loc_id;
    private int to_loc_id;
    private String trip_date;
    private double trip_amount;

    public double getTrip_amount() {
        return trip_amount;
    }

    public void setTrip_amount(double trip_amount) {
        this.trip_amount = trip_amount;
    }

    public int getTrip_num() {
        return trip_num;
    }

    public void setTrip_num(int trip_num) {
        this.trip_num = trip_num;
    }

    public int getFrom_loc_id() {
        return from_loc_id;
    }

    public void setFrom_loc_id(int from_loc_id) {
        this.from_loc_id = from_loc_id;
    }

    public int getTo_loc_id() {
        return to_loc_id;
    }

    public void setTo_loc_id(int to_loc_id) {
        this.to_loc_id = to_loc_id;
    }

    public String getTrip_date() {
        return trip_date;
    }

    public void setTrip_date(String trip_date) {
        this.trip_date = trip_date;
    }
}
