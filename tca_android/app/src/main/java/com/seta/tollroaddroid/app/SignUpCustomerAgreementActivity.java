package com.seta.tollroaddroid.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.custom.MyWebChromeClient;
import com.seta.tollroaddroid.app.custom.MyWebViewClient;
import com.seta.tollroaddroid.app.json.AccountInfo;
import com.seta.tollroaddroid.app.json.SignUpRequest;
import com.seta.tollroaddroid.app.json.SignUpResponse;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;

public class SignUpCustomerAgreementActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();
    private WebView wvTerms;
    private ImageView ivGoBack;
    private TextView tvAgree;
    private Response.ErrorListener errorListener;
    private MyWebViewClient myWebViewClient = null;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("CurrentSignUpReq", TollRoadsApp.getInstance().gSignUpRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_customer_agreement);
        FlurryAgent.logEvent("Enter SignUp_5_Agreement_"+TollRoadsApp.getInstance().getSignUpAccountType()+" page.");

        if(savedInstanceState != null)
        {
            TollRoadsApp.getInstance().gSignUpRequest = (SignUpRequest) savedInstanceState.getSerializable("CurrentSignUpReq");
        }

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        wvTerms = (WebView)findViewById(R.id.wv_terms);
        tvAgree = (TextView)findViewById(R.id.tv_agree);

        initWidgetValue();
        setupListener();
    }

    private void setupListener()
    {
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

        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(getApplicationContext(), SignUpPaymentInfoActivity.class,
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
        });
        tvAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                String params = populateSignUpParams();

                signUpRequest(Resource.URL_ENROLLMENT, params);

            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            gotoActivity(getApplicationContext(), SignUpPaymentInfoActivity.class,
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private String populateSignUpParams()
    {
        String params = "";

        if(TollRoadsApp.getInstance().gSignUpRequest != null) {
            Gson gson = new GsonBuilder().serializeNulls().create();
            String jsonStr = gson.toJson(TollRoadsApp.getInstance().gSignUpRequest,
                    SignUpRequest.class);
            int index = 0;
            try {
                JSONObject jsonObject = new JSONObject(jsonStr);
                Iterator<String> iter = jsonObject.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    Object value = jsonObject.get(key);
                    if(value == null || String.valueOf(value).equals("null"))
                    {
                        value = "";
                    }
                    if (index == 0) {
                        params = key + "=" + Uri.encode(String.valueOf(value));
                        index++;
                    } else {
                        params = params + "&" + key + "=" + Uri.encode(String.valueOf(value));
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return params;
    }

    private void signUpRequest(String url,String params)
    {
        Response.Listener listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                closeProgressDialog();
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, "response:" + response);

                        Gson gson = new GsonBuilder().serializeNulls().create();
                        SignUpResponse signUpResponse = gson.fromJson(response, SignUpResponse.class);

                        if(checkResponse(response)) {
                            TollRoadsApp.getInstance().setToken(signUpResponse.getTokenID());
                            if(TollRoadsApp.getInstance().getRememberLogIn())
                            {
                                TollRoadsApp.getInstance().setUserName(
                                        TollRoadsApp.getInstance().gSignUpRequest.getAccount_username());
                            }

                            Bundle bundle = new Bundle();
                            bundle.putString("response",response);

                            gotoActivity(getApplicationContext(), SignUpResultActivity.class,
                                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK,
                                    bundle);
                        }
                        else
                        {
                            closeProgressDialog();
                        }
                    }
                    else
                    {
                        closeProgressDialog();
                        showToastMessage(getString(R.string.network_error_retry));
                    }
                }
                catch (Exception e)
                {
                    closeProgressDialog();
                    showToastMessage(getString(R.string.network_error_retry));
                }
            }
        };

        ServerDelegate.signUpRequest(url, params, listener, errorListener);
    }
    private void initWidgetValue()
    {
        String url = Resource.SIGN_UP_AGREEMENT_URL;
        initWebView();

        if (url.lastIndexOf(".pdf") == (url.length()-4))
        {
            String pdfUrl = "https://docs.google.com/gview?embedded=true&url="+ url;
            wvTerms.loadUrl(pdfUrl);
        }
        else
        {
            wvTerms.loadUrl(url);
        }
    }

    @SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })
    void initWebView()
    {
        WebSettings settings = wvTerms.getSettings();

        wvTerms.setScrollContainer(false);
        wvTerms.setScrollbarFadingEnabled(false);
        wvTerms.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        wvTerms.setWebChromeClient(new MyWebChromeClient());
        myWebViewClient = new MyWebViewClient(this);
        wvTerms.setWebViewClient(myWebViewClient);

        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(false);
        settings.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
        settings.setAllowFileAccess(true);
        settings.setJavaScriptEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
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
        showProgressDialog();

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
                            finish();
                            gotoActivity(getApplicationContext(), MyAccountActivity.class, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

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

    @Override
    protected void onDestroy() {
        if(myWebViewClient != null)
        {
            myWebViewClient.closeProgressDialog();
        }
        FlurryAgent.logEvent("Exit SignUp_5_Agreement_"+TollRoadsApp.getInstance().getSignUpAccountType()+" page.");
        super.onDestroy();
    }

}
