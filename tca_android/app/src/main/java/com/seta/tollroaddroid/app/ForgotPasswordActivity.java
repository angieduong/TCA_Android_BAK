package com.seta.tollroaddroid.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.json.CommonResponse;
import com.seta.tollroaddroid.app.json.ForgotPasswordResponse;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import java.io.UnsupportedEncodingException;

public class ForgotPasswordActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private TextView tvGoNext;
    private EditText etAccountNumberEmail;

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit Forgot_Password page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        FlurryAgent.logEvent("Enter Forgot_Password page.");

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvGoNext = (TextView) findViewById(R.id.tv_go_next);

        etAccountNumberEmail = (EditText)findViewById(R.id.et_account_number_email);

        setupListener();
    }

    private void setupListener()
    {
        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(getApplicationContext(), LoginActivity.class,
                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
        });

        tvGoNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etAccountNumberEmail.getText().toString().isEmpty()) {
                    showToastMessage(getString(R.string.account_no_email_empty_warning));
                } else {
                    String params = "privateVal="+etAccountNumberEmail.getText().toString();
                    forgotPasswordRequest(Resource.URL_RESET_PWD,params);

//                    gotoActivity(ForgotPasswordActivity.this, SecurityQuestionsActivity.class,
//                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                }
            }
        });

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            gotoActivity(getApplicationContext(), LoginActivity.class,
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            return true;
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

    private void forgotPasswordRequest(String url,String params)
    {
        showProgressDialog();
        Response.Listener listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                closeProgressDialog();
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, "response:" + response);
                        Gson gson = new GsonBuilder().serializeNulls().create();
                        CommonResponse commonResponse = gson.fromJson(response, CommonResponse.class);

                        if(checkResponse(response)) {
                            TollRoadsApp.getInstance().setToken(commonResponse.getTokenID());
                            ForgotPasswordResponse forgotPasswordResponse = gson.fromJson(response, ForgotPasswordResponse.class);
                            if(forgotPasswordResponse.isSent_code())
                            {
                                showToastMessage(getString(R.string.answer_sec_questions_successfully));
                                finish();
                                gotoActivity(getApplicationContext(), LandingPageActivity.class,
                                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            }
                            else
                            {
                                Bundle bundle = new Bundle();
                                bundle.putString("response",response);
                                gotoActivity(ForgotPasswordActivity.this, SecurityQuestionsActivity.class,
                                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT, bundle);
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
        ServerDelegate.forgotPasswordReq(url, params, listener, errorListener);
    }

}
