package com.seta.tollroaddroid.app.json;

/**
 * Created by Thomas on 2015-12-17.
 *
 "success": 1,
 "status": 200,
 "message": "",
 "tokenID": "5870658XwUyU8sOPDHzZyRTuy51HKPz3sVHNTAcY",
 "uniqueID": "84730561-8C06-4B6E-A561-D31B858DD1DC"
 */
public class LoginResponse {
    private int success;
    private int status;
    private String message;
    private String tokenID;
    private String uniqueID;
    private int need_sec;

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

    public int getNeed_sec() {
        return need_sec;
    }

    public void setNeed_sec(int need_sec) {
        this.need_sec = need_sec;
    }
}
