package com.seta.tollroaddroid.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.json.LoginResponse;
import com.seta.tollroaddroid.app.json.ViolationLoginResponse;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import java.io.UnsupportedEncodingException;

public class ViolationLogInActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();
    private TextView tvLogin;
    private EditText etReferenceNumber, etViolationNumber;
    private Response.ErrorListener errorListener;
    private ImageView ivGoBack;

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit violation login page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_violation_log_in);

        FlurryAgent.logEvent("Enter violation login page.");
        tvLogin = (TextView)findViewById(R.id.tv_vio_login);
        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);

        etReferenceNumber = (EditText)findViewById(R.id.et_reference_number);
        etViolationNumber = (EditText)findViewById(R.id.et_violation_number);

        setupListener();
    }

    private void setupListener()
    {
        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        etViolationNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE)
                {
                    if (checkValidation()) {
                        doLoginAction();
                    }
                    return true;
                }
                return false;
            }
        });
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation()) {
                    doLoginAction();
                }
            }
        });

        errorListener = new Response.ErrorListener() {
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
        
    }

    private boolean checkUserNameValidation() {
        if (etReferenceNumber.getText().length() == 0) {
            showToastMessage(getString(R.string.user_name_empty_warning));
            etReferenceNumber.requestFocus();
            return false;
        }

        return true;
    }

    private boolean checkPasswordValidation() {
        if(etViolationNumber.getText().length() == 0)
        {
            showToastMessage(getString(R.string.password_empty_warning));
            etViolationNumber.requestFocus();
            return false;
        }
        return true;
    }

    private boolean checkValidation()
    {
        if(!checkUserNameValidation())
        {
            return false;
        }
        else if(!checkPasswordValidation())
        {
            return false;
        }

        return true;
    }
    
    private void hideAllSoftKeyboard()
    {
        hideKeyboard(etViolationNumber);
        hideKeyboard(etReferenceNumber);
    }

    private void doLoginAction()
    {
        hideAllSoftKeyboard();
        showProgressDialog();

        String referenceNumber = etReferenceNumber.getText().toString().trim();
        String violationNumber = etViolationNumber.getText().toString().trim();
        String loginParams = "&username="+ Uri.encode(referenceNumber) +"&password="+Uri.encode(violationNumber);
        sendLoginRequest(Resource.URL_VIOLATION_LOGIN, loginParams);
    }

    private void sendLoginRequest(String loginRequestUrl,String loginParams)
    {
        Response.Listener listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                closeProgressDialog();
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, "response:"+response);

                        Gson gson = new GsonBuilder().serializeNulls().create();
                        TollRoadsApp.getInstance().violationLoginResponse = gson.fromJson(response, ViolationLoginResponse.class);

                        if(checkResponse(response)) {
                            TollRoadsApp.getInstance().setToken(TollRoadsApp.getInstance().violationLoginResponse.getTokenID());
                            finish();
                            gotoActivity(ViolationLogInActivity.this, ViolationDashboardActivity.class);
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

        ServerDelegate.loginRequest(loginRequestUrl, loginParams, listener, errorListener);
    }
    
}
