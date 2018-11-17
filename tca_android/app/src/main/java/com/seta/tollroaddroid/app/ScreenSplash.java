package com.seta.tollroaddroid.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class ScreenSplash extends FragmentActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();
    private int nWelcomeScreenDisplay = 500;
    private Runnable gRunnable = null;
    private Handler gHandler = new Handler();

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        Log.i(LOG_TAG,"onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.i(LOG_TAG, "onresume");
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_splash);
        Log.i("ScreenSplash", "onCreate");

        gRunnable = new Runnable() {
            @Override
            public void run()
            {
                launchMainPage();
            }
        };

        gHandler.postDelayed(gRunnable, nWelcomeScreenDisplay);
    }

    private void launchMainPage()
    {
        Intent mainIntent = null;
        if(TollRoadsApp.getInstance().getToken().isEmpty()) {
            mainIntent = new Intent(this, LandingPageActivity.class);
        }
        else
        {
            mainIntent = new Intent(this, MyAccountActivity.class);
        }
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(mainIntent);

        this.finish();
    }

}
