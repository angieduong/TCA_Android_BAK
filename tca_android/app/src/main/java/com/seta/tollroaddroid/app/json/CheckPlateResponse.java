package com.seta.tollroaddroid.app.json;

/**
 * Created by thomashuang on 16-03-23.
 * "success":1,"status":200,"message":"","":2,"tokenID":"50X007FVJOZ1xty3B8geepiO3NEA3wscdDBOWs9E","uniqueID":""
 */
public class CheckPlateResponse {
    private int success;
    private int status;
    private String message;
    private String tokenID;
    private String uniqueID;
    private int vehicle_found;

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

    public int getVehicle_found() {
        return vehicle_found;
    }

    public void setVehicle_found(int vehicle_found) {
        this.vehicle_found = vehicle_found;
    }
}
