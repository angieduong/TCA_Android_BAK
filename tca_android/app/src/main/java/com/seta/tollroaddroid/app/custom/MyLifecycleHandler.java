package com.seta.tollroaddroid.app.custom;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by thomashuang on 16-04-26.
 */
public class MyLifecycleHandler implements Application.ActivityLifecycleCallbacks {
    // I use four separate variables here. You can, of course, just use two and
    // increment/decrement them instead of using four and incrementing them all.
    private int resumed = 0;
    private int paused = 0;
    private int started = 0;
    private int stopped = 0;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        ++resumed;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        ++paused;

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        ++started;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        ++stopped;

        //Log.i("background", "application is visible: " + isApplicationVisible());

        if (!isApplicationVisible()) {
            //Log.i("background", "===== END of session =====");
        }
    }

    public boolean isApplicationVisible() {
        return started > stopped;
    }

    public boolean isApplicationInForeground() {
        return resumed > paused;
    }
}
