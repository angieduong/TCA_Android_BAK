package com.seta.tollroaddroid.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seta.tollroaddroid.app.api.Resource;

import java.io.UnsupportedEncodingException;

public class OTTCalculateTollActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private TextView tvGoNext;
    private RadioButton rbCalculateForMe, rbCalculateMyself;
    private TextView tvCalculateForMeHint, tvCalculateMyselfHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ott_calculate_toll);

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvGoNext = (TextView) findViewById(R.id.tv_go_next);

        rbCalculateForMe = (RadioButton)findViewById(R.id.rb_calculate_for_me);
        rbCalculateMyself = (RadioButton)findViewById(R.id.rb_calculate_myself);

        tvCalculateForMeHint = (TextView)findViewById(R.id.tv_calculate_for_me_hint);
        tvCalculateMyselfHint = (TextView)findViewById(R.id.tv_calculate_myself_hint);

        setupListener();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            gotoActivity(getApplicationContext(), OTTVehicleInfoActivity.class,
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            return  true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setupListener()
    {
        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(v.getContext(), OTTVehicleInfoActivity.class,
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
        });

        tvGoNext.setVisibility(View.GONE);
        tvGoNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(rbCalculateMyself.isChecked())
            {
                TollRoadsApp.getInstance().gOTTUserInfoRequest.setCalculate_toll_mode(Resource.CALCULATE_TOLL_MYSELF);
                gotoActivity(v.getContext(), OTTTripActivity.class,
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
            else if(rbCalculateForMe.isChecked())
            {
                TollRoadsApp.getInstance().gOTTUserInfoRequest.setCalculate_toll_mode(Resource.CALCULATE_TOLL_FOR_ME);
                gotoActivity(v.getContext(), OTTCalculateForMeActivity.class,
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
            }
        });

        rbCalculateForMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbCalculateMyself.setChecked(false);
                tvCalculateMyselfHint.setVisibility(View.GONE);

                tvCalculateForMeHint.setVisibility(View.VISIBLE);

                tvGoNext.setVisibility(View.VISIBLE);
            }
        });

        rbCalculateMyself.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbCalculateForMe.setChecked(false);
                tvCalculateForMeHint.setVisibility(View.GONE);

                tvCalculateMyselfHint.setVisibility(View.VISIBLE);

                tvGoNext.setVisibility(View.VISIBLE);
            }
        });
    }
}
