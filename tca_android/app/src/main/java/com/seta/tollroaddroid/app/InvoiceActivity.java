package com.seta.tollroaddroid.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.custom.MyWebChromeClient;
import com.seta.tollroaddroid.app.custom.MyWebViewClient;
import com.seta.tollroaddroid.app.json.Invoice;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class InvoiceActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();
    private WebView wvTCA;
    private ImageView ivGoBack;
    private TextView tvTitle, tvEmail;
    private String url = "";
    private boolean needReload = false;
    private MyWebViewClient myWebViewClient = null;
    private String pageName = "";
    private String invoiceID = "";
    private String pdfUrl;
    private boolean loadedResource = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        wvTCA = (WebView)findViewById(R.id.wv_tca);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvEmail = (TextView) findViewById(R.id.tv_email);
        Intent intent = getIntent();
        if(intent != null)
        {
            String title = intent.getStringExtra(Resource.KEY_TITLE);
            url = intent.getStringExtra(Resource.KEY_URL);
            invoiceID = intent.getStringExtra(Resource.KEY_INVOICE_ID);

            if(title != null)
            {
                tvTitle.setText(title);
            }
            else
            {
                tvTitle.setText(getString(R.string.invoices));
            }
        }
        Log.d("Invoice Activity","url:"+url);
        initWidgetValue();
        setupListener();

        FlurryAgent.logEvent("Enter invoice_detail page.");
    }

    private void setupListener()
    {
        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tvEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmailRequest();
            }
        });
    }

    private void sendEmailRequest()
    {
        Response.Listener listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                closeProgressDialog();
                try
                {

                    if(response != null) {
                        Log.d(LOG_TAG, response.toString());

                        if(checkResponse(response.toString())) {
                            showToastMessage(getString(R.string.email_successfully_sent));
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

        showProgressDialog();
        ServerDelegate.sendEmailInvoiceRequest(Resource.URL_PAYMENT, listener, errorListener, invoiceID);
    }

    private void initWidgetValue()
    {
        initWebView();

        pdfUrl = "https://docs.google.com/gview?embedded=true&url="+ Uri.encode(url);
        wvTCA.loadUrl(pdfUrl);
    }

    @SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })
    void initWebView()
    {
        WebSettings settings = wvTCA.getSettings();

        wvTCA.setScrollContainer(false);
        wvTCA.setScrollbarFadingEnabled(false);
        wvTCA.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        wvTCA.setWebChromeClient(new MyWebChromeClient());
        myWebViewClient = new MyWebViewClient(this,true,wvTCA){
            @Override
            public
            void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                needReload = true;
                handler.cancel();
                super.onReceivedSslError(view,handler,error);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                needReload = true;
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                if(pdfUrl != null && !pdfUrl.equals(url)) {
                    loadedResource = true;
                }
                Log.e(LOG_TAG, "onLoadResource url:"+ url+",loadedResource:"+loadedResource);

                //ServerDelegate.appendLog("onLoadResource url:"+ url+",loadedResource:"+loadedResource);
                super.onLoadResource(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                loadedResource = false;
                Log.e(LOG_TAG, "onPageStarted url:"+ url);
                //ServerDelegate.appendLog("onPageStarted url:"+ url);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if(!loadedResource)
                {
                    wvTCA.reload();
                }
                Log.e(LOG_TAG, "onPageFinished url:"+ url);
                //ServerDelegate.appendLog("onPageFinished url:"+ url+",loadedResource:"+loadedResource);
                loadedResource = false;

                super.onPageFinished(view, url);
            }
        };

        wvTCA.setWebViewClient(myWebViewClient);

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
        if(needReload)
        {
            needReload = false;
            wvTCA.reload();
        }
    }

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(myWebViewClient != null)
        {
            myWebViewClient.closeProgressDialog();
        }
        FlurryAgent.logEvent("Exit "+pageName+" page.");
        super.onDestroy();
    }

}
