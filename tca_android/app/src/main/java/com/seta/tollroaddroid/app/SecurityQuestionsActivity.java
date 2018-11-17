package com.seta.tollroaddroid.app;

import android.content.Intent;
import android.net.Uri;
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
import com.seta.tollroaddroid.app.json.ForgotPasswordResponse;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import java.io.UnsupportedEncodingException;

public class SecurityQuestionsActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private TextView tvDone;
    private TextView tvQuestion1,tvQuestion2,tvQuestion3;
    private EditText etAnswer1, etAnswer2, etAnswer3;
    private String response;
    private ForgotPasswordResponse forgotPasswordResponse;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("response", response);
    }

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit Forgot_Questions page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_questions);
        if(savedInstanceState != null)
        {
            response = savedInstanceState.getString("response");
        }
        else
        {
            Intent intent = getIntent();
            if(intent != null)
            {
                response = intent.getStringExtra("response");
            }
        }
        FlurryAgent.logEvent("Enter Forgot_Questions page.");

        if(response != null)
        {
            Gson gson = new GsonBuilder().serializeNulls().create();
            forgotPasswordResponse = gson.fromJson(response, ForgotPasswordResponse.class);
        }
        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvDone = (TextView)findViewById(R.id.tv_done);
        tvQuestion1 = (TextView)findViewById(R.id.tv_question_1);
        tvQuestion2 = (TextView)findViewById(R.id.tv_question_2);
        tvQuestion3 = (TextView)findViewById(R.id.tv_question_3);

        etAnswer1 = (EditText)findViewById(R.id.et_answer_1);
        etAnswer2 = (EditText)findViewById(R.id.et_answer_2);
        etAnswer3 = (EditText)findViewById(R.id.et_answer_3);

        initWidgetValue();
        setupListener();
    }

    private void setupListener()
    {
        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(v.getContext(), ForgotPasswordActivity.class,
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
        });

        tvDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation()) {
                    String params = populateParams();
                    answerSecQuestionsReq(Resource.URL_RESET_PWD, params);
                }
//            gotoActivity(getApplicationContext(), LandingPageActivity.class,
//                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
        });
    }

    private void initWidgetValue()
    {
        if(forgotPasswordResponse != null)
        {
            if(forgotPasswordResponse.getQuestion1_text() != null) {
                tvQuestion1.setText(forgotPasswordResponse.getQuestion1_text());
            }
            if(forgotPasswordResponse.getQuestion2_text() != null) {
                tvQuestion2.setText(forgotPasswordResponse.getQuestion2_text());
            }
            if(forgotPasswordResponse.getQuestion3_text() != null) {
                tvQuestion3.setText(forgotPasswordResponse.getQuestion3_text());
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            gotoActivity(SecurityQuestionsActivity.this, ForgotPasswordActivity.class,
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private String populateParams()
    {
        String params = "secAnswer1=" + Uri.encode(etAnswer1.getText().toString());
        params = params + "&secAnswer2=" + Uri.encode(etAnswer2.getText().toString());
        params = params + "&secAnswer3=" + Uri.encode(etAnswer3.getText().toString());

        if(forgotPasswordResponse != null)
        {
            params = params + "&secQuestion1ID=" + forgotPasswordResponse.getQuestion1ID();
            params = params + "&secQuestion2ID=" + forgotPasswordResponse.getQuestion2ID();
            params = params + "&secQuestion3ID=" + forgotPasswordResponse.getQuestion3ID();
        }
        return params;
    }

    private boolean checkValidation()
    {
        boolean ret = true;

        if (etAnswer1.getText().length() == 0 && etAnswer2.getText().length() == 0
                && etAnswer3.getText().length() == 0) {
            ret = false;
            showToastMessage(getString(R.string.all_answer_empty_warning));
        }

        return ret;
    }

    private void answerSecQuestionsReq(String url,String params)
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
                            showToastMessage(getString(R.string.answer_sec_questions_successfully));
                            finish();
                            gotoActivity(getApplicationContext(), LandingPageActivity.class,
                                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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

        ServerDelegate.answerSecQuestionsReq(url, params, listener, errorListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {

        super.onPause();
    }

}
