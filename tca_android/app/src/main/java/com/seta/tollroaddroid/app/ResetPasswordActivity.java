package com.seta.tollroaddroid.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import java.io.UnsupportedEncodingException;

public class ResetPasswordActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private EditText etNewPassword, etRetypePassword;
    private String token;
    private TextView tvDone;

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit Reset_Password page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        FlurryAgent.logEvent("Enter Reset_Password page.");

        Intent intent = getIntent();
        if(intent != null)
        {
            token = intent.getStringExtra("token");
            showToastMessage(token);
        }

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvDone = (TextView)findViewById(R.id.tv_done);

        etNewPassword = (EditText)findViewById(R.id.et_new_password);
        etRetypePassword = (EditText)findViewById(R.id.et_re_type_password);
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

        tvDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkValidation())
                {
                    String params = "new_password=" + etNewPassword.getText().toString();
                    if(token != null)
                    {
                        params = params + "&"+Resource.PARAM_TOKEN+"="+ token;
                    }
                    else
                    {
                        params = params + "&"+Resource.PARAM_TOKEN+"=";
                    }
                    resetPasswordRequest(Resource.URL_ACCOUNT, params);
                }
            }
        });

    }

    private boolean checkValidation()
    {
        boolean ret = true;
        if (etNewPassword.getText().toString().isEmpty()) {
            showToastMessage(getString(R.string.account_no_email_empty_warning));
        }
        else if(!etNewPassword.getText().toString().equals(etRetypePassword.getText().toString()))
        {
            ret = false;
            showToastMessage(getString(R.string.password_not_match_warning));
        }

        return ret;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {

        super.onPause();
    }

    private void resetPasswordRequest(String url,String params)
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

                        if(checkResponse(response)) {
                            showToastMessage(getString(R.string.successfully_reset));
                            finish();
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
        ServerDelegate.resetPasswordRequest(url, params, listener, errorListener);
    }

}
