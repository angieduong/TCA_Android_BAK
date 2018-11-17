package com.seta.tollroaddroid.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.utilities.Constants;

public class OTTPayResultActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();
    
    private TextView tvOK, tvConfirmationMessage, tvEmailReceiptHint;

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit OTT_5_Success page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ott_pay_result);
        FlurryAgent.logEvent("Enter OTT_5_Success page.");

        tvOK = (TextView)findViewById(R.id.tv_ok);
        tvConfirmationMessage = (TextView)findViewById(R.id.tv_confirmation_message);
        tvEmailReceiptHint = (TextView)findViewById(R.id.tv_email_receipt_hint);

        Intent intent = getIntent();
        if(intent != null)
        {
            String confMessage = intent.getStringExtra(Resource.KEY_CONF_MESSAGE);
            if(confMessage != null)
            {
                tvConfirmationMessage.setText(confMessage);
            }
        }
        setupListener();
    }

    private void setupListener()
    {
        tvOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(getApplicationContext(), LandingPageActivity.class,
                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
        });
        
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            gotoActivity(getApplicationContext(), LandingPageActivity.class,
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            return  true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initWidgetValue()
    {
        if(TollRoadsApp.getInstance().gVehicleFound == Constants.VEHICLE_FOUND_TYPE_RENTAL)
        {
            tvEmailReceiptHint.setText(getString(R.string.will_email_receipt_hint));
        }
        else
        {
            tvEmailReceiptHint.setText(getString(R.string.email_receipt_hint));

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initWidgetValue();
    }

    @Override
    public void onPause() {

        super.onPause();
    }

}
