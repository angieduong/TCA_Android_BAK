package com.seta.tollroaddroid.app.json;

/**
 * Created by thomashuang on 16-03-12.
 * "id": "74360387",
 "plate": "7GXM940",
 "state": "CA",
 "year": 2014,
 "make": "INFINITI",
 "model": "Q60",
 "color": "GREY",
 "start_date": "2014-11-01",
 "end_date": ""
 */
public class Vehicle implements java.io.Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String id;
    private String plate;
    private String state;
    private int year;
    private String make;
    private String model;
    private String color;
    private String start_date;
    private String end_date;

    private String country;
    private boolean rental =false;
    private String vehicle_id;
    private int vehicle_type;
    private int type;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isRental() {
        return rental;
    }

    public void setRental(boolean rental) {
        this.rental = rental;
    }

    public String getVehicle_id() {
        return vehicle_id;
    }

    public void setVehicle_id(String vehicle_id) {
        this.vehicle_id = vehicle_id;
    }

    public int getVehicle_type() {
        return vehicle_type;
    }

    public void setVehicle_type(int vehicle_type) {
        this.vehicle_type = vehicle_type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
