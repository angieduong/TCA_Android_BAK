package com.seta.tollroaddroid.app.json;

/**
 * Created by thomashuang on 16-03-29.
 */
public class RoadInfo {
    private int toll_road;
    private String road_name= "";

    public int getToll_road() {
        return toll_road;
    }

    public void setToll_road(int toll_road) {
        this.toll_road = toll_road;
    }

    public String getRoad_name() {
        return road_name;
    }

    public void setRoad_name(String road_name) {
        this.road_name = road_name;
    }
}
