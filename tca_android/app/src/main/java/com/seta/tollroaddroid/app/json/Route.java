package com.seta.tollroaddroid.app.json;

/**
 * Created by thomashuang on 16-03-03.
 * "route_id": "2",
 "route_name": "91 Freeway (Anaheim)",
 "route_lat": "33.860190",
 "route_long": "-117.717337",
 "route_type": "point",
 "route_point_type": "entry\/exit",
 "route_fwy": "241",
 "route_order": "3"
 */
public class Route {
    private String route_id;
    private String route_name;
    private String route_lat;
    private String route_long;
    private String route_type;
    private String route_point_type;
    private String route_fwy;
    private String route_order;

    public String getRoute_id() {
        return route_id;
    }

    public void setRoute_id(String route_id) {
        this.route_id = route_id;
    }

    public String getRoute_name() {
        return route_name;
    }

    public void setRoute_name(String route_name) {
        this.route_name = route_name;
    }

    public String getRoute_lat() {
        return route_lat;
    }

    public void setRoute_lat(String route_lat) {
        this.route_lat = route_lat;
    }

    public String getRoute_long() {
        return route_long;
    }

    public void setRoute_long(String route_long) {
        this.route_long = route_long;
    }

    public String getRoute_type() {
        return route_type;
    }

    public void setRoute_type(String route_type) {
        this.route_type = route_type;
    }

    public String getRoute_point_type() {
        return route_point_type;
    }

    public void setRoute_point_type(String route_point_type) {
        this.route_point_type = route_point_type;
    }

    public String getRoute_fwy() {
        return route_fwy;
    }

    public void setRoute_fwy(String route_fwy) {
        this.route_fwy = route_fwy;
    }

    public String getRoute_order() {
        return route_order;
    }

    public void setRoute_order(String route_order) {
        this.route_order = route_order;
    }
}
