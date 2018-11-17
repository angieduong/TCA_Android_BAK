package com.seta.tollroaddroid.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.json.AccountInfo;
import com.seta.tollroaddroid.app.json.SignUpResponse;
import com.seta.tollroaddroid.app.utilities.Constants;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class SignUpResultActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();
    
    private TextView tvOK, tvAccountMessage, tvAccountNumber, tvTitle;
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("account_type", TollRoadsApp.getInstance().selectedAccountType);
    }

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit SignUp_6_Confirmation_"+TollRoadsApp.getInstance().getSignUpAccountType()+" page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_result);
        FlurryAgent.logEvent("Enter SignUp_6_Confirmation_"+TollRoadsApp.getInstance().getSignUpAccountType()+" page.");

        tvTitle = (TextView)findViewById(R.id.tv_title);
        tvOK = (TextView)findViewById(R.id.tv_ok);
        tvAccountNumber = (TextView)findViewById(R.id.tv_account_no);
        tvAccountMessage = (TextView)findViewById(R.id.tv_account_message);

        Intent intent = getIntent();
        if(intent != null)
        {
            String response = intent.getStringExtra("response");
            Gson gson = new GsonBuilder().serializeNulls().create();
            SignUpResponse signUpResponse = gson.fromJson(response, SignUpResponse.class);
            if(signUpResponse.getAccount_message() != null)
            {
                tvAccountMessage.setText(signUpResponse.getAccount_message());
            }

            if(signUpResponse.getAccount_number() != null)
            {
                tvAccountNumber.setText(getString(R.string.sign_up_result_hint,
                        signUpResponse.getAccount_number()));
            }
        }
        if(savedInstanceState != null)
        {
            TollRoadsApp.getInstance().selectedAccountType = savedInstanceState.getInt("account_type");
        }

        int index = TollRoadsApp.getInstance().selectedAccountType;

        if(index == Constants.ACCOUNT_TYPE_FASTRAK_INDIVIDUAL)
        {
            tvTitle.setText(R.string.fastrak_account);
        }
        else if(index == Constants.ACCOUNT_TYPE_CHARGE_EXPRESS)
        {
            tvTitle.setText(R.string.express_charge_account);
        }
        else if(index == Constants.ACCOUNT_TYPE_PREPAID_EXPRESS)
        {
            tvTitle.setText(R.string.express_prepaid_account);
        }
        else
        {
            tvTitle.setText(R.string.express_invoice_account);
        }
        setupListener();
        sendAccountRequest();
    }

    private void setupListener()
    {
        tvOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAccountRequest();
                finish();
                gotoActivity(getApplicationContext(), MyAccountActivity.class,
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

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {

        super.onPause();
    }

    private void sendAccountRequest()
    {
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                closeProgressDialog();

                if(error != null) {
                    Log.d(LOG_TAG, "Error: " + error.getMessage());
                    if(error.networkResponse != null) {
                        try {

                            String responseBody = new String(error.networkResponse.data, "utf-8");

                            showToastMessage(responseBody);

                        }catch (UnsupportedEncodingException Exceptionerror) {

                        }
                    }
                    else
                    {
                        showToastMessage(getString(R.string.network_error_retry));
                    }
                }
            }
        };
        Response.Listener listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                closeProgressDialog();
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, response.toString());
                        closeProgressDialog();
                        if(checkResponse(response.toString())) {
                            if(response.has(Resource.KEY_INFO))
                            {
                                String info = response.optString(Resource.KEY_INFO);
                                Gson gson = new GsonBuilder().serializeNulls().create();

                                TollRoadsApp.getInstance().accountInfo = gson.fromJson(info, AccountInfo.class);
                                TollRoadsApp.getInstance().setMyAccount(info);
                            }
                        }
                    }
                    else
                    {
                        showToastMessage(getString(R.string.network_error_retry));
                    }
                }
                catch (Exception e)
                {
                    showToastMessage(getString(R.string.network_error_retry));
                }
            }
        };

        ServerDelegate.sendAccountRequest(Resource.URL_ACCOUNT, listener, errorListener);
    }
}
