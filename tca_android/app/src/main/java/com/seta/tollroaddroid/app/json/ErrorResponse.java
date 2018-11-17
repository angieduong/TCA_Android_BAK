package com.seta.tollroaddroid.app.json;

/**
 * Created by Thomas on 2015-12-17.
 * example:{"timestamp":1450304356856,"status":400,"error":"Bad Request","exception":null,"message":"username is already in use!","path":"/register"}
 */
public class ErrorResponse {
    private Long timestamp;
    private Integer status;
    private String error;
    private String exception;
    private String message;
    private String path;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
