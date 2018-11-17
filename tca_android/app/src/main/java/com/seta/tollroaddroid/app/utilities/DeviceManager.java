package com.seta.tollroaddroid.app.utilities;


/**
 * Created by kpangilinan on 2016-03-02.
 */
public class DeviceManager {
    private static DeviceManager mInstance = null;

    private String platform;
    private String deviceOS;
    private String deviceModel;
    private int deviceApiLevel;
    private String appVersionName;
    private String appVersionCode;

    private DeviceManager() {

    }

    public static DeviceManager getInstance() {
        if (mInstance == null) {
            mInstance = new DeviceManager();
        }
        return mInstance;
    }

    public String getDeviceOS() {
        return deviceOS;
    }

    public void setDeviceOS(String deviceOS) {
        this.deviceOS = deviceOS;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public int getDeviceApiLevel() {
        return deviceApiLevel;
    }

    public void setDeviceApiLevel(int deviceApiLevel) {
        this.deviceApiLevel = deviceApiLevel;
    }

    public String getAppVersionName() {
        return appVersionName;
    }

    public void setAppVersionName(String appVersionName) {
        this.appVersionName = appVersionName;
    }

    public String getAppVersionCode() {
        return appVersionCode;
    }

    public void setAppVersionCode(String appVersionCode) {
        this.appVersionCode = appVersionCode;
    }

    public String getDeviceInfo(boolean addNewLines) {
        return  "Platform: " + "Android" + (addNewLines? "\n" : ", ") +
                "Model: " + getDeviceModel() + (addNewLines? "\n" : ", ") +
                "OS: " + getDeviceOS() + (addNewLines? "\n" : ", ") +
                "API Level: " + getDeviceApiLevel() + (addNewLines? "\n" : ", ") +
                "App Version Name: " + getAppVersionName() + (addNewLines? "\n" : ", ") +
                "App Version Code: " + getAppVersionCode() + (addNewLines? "\n" : "");
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
