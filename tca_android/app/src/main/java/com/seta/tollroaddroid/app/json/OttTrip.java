package com.seta.tollroaddroid.app.json;

/**
 * Created by thomashuang on 16-03-29.
 */
public class OttTrip implements java.io.Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String trip_date = "";
    private int from_loc_id = -1;
    private int to_loc_id = -1;
    private int toll_road;
    private String road= "";
    private String from_loc_name= "";
    private String to_loc_name= "";

    private int roadIndex = 0;
    private int fromIndex = 0;
    private int toIndex = 0;

    private int trip_num = 0;
    private double trip_amount;
    private boolean error = true;

    public String getTrip_date() {
        return trip_date;
    }

    public void setTrip_date(String trip_date) {
        this.trip_date = trip_date;
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

    public int getToll_road() {
        return toll_road;
    }

    public void setToll_road(int toll_road) {
        this.toll_road = toll_road;
    }

    public String getRoad() {
        return road;
    }

    public void setRoad(String road) {
        this.road = road;
    }

    public String getFrom_loc_name() {
        return from_loc_name;
    }

    public void setFrom_loc_name(String from_loc_name) {
        this.from_loc_name = from_loc_name;
    }

    public String getTo_loc_name() {
        return to_loc_name;
    }

    public void setTo_loc_name(String to_loc_name) {
        this.to_loc_name = to_loc_name;
    }

    public int getRoadIndex() {
        return roadIndex;
    }

    public void setRoadIndex(int roadIndex) {
        this.roadIndex = roadIndex;
    }

    public int getFromIndex() {
        return fromIndex;
    }

    public void setFromIndex(int fromIndex) {
        this.fromIndex = fromIndex;
    }

    public int getToIndex() {
        return toIndex;
    }

    public void setToIndex(int toIndex) {
        this.toIndex = toIndex;
    }

    public int getTrip_num() {
        return trip_num;
    }

    public void setTrip_num(int trip_num) {
        this.trip_num = trip_num;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public double getTrip_amount() {
        return trip_amount;
    }

    public void setTrip_amount(double trip_amount) {
        this.trip_amount = trip_amount;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }
}
