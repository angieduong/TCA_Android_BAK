package com.seta.tollroaddroid.app.json;

import com.google.gson.annotations.SerializedName;

/**
 * Created by thomashuang on 16-03-29.
 */
public class LocInfo {
    private int loc_id;
    private int toll_road;
    private String loc_name= "";
    private String road_name= "";

    @SerializedName("long")
    private double longitude = 0.0d;

    private double lat = 0.0;

    public int getLoc_id() {
        return loc_id;
    }

    public void setLoc_id(int loc_id) {
        this.loc_id = loc_id;
    }

    public int getToll_road() {
        return toll_road;
    }

    public void setToll_road(int toll_road) {
        this.toll_road = toll_road;
    }

    public String getLoc_name() {
        return loc_name;
    }

    public void setLoc_name(String loc_name) {
        this.loc_name = loc_name;
    }

    public String getRoad_name() {
        return road_name;
    }

    public void setRoad_name(String road_name) {
        this.road_name = road_name;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
}
