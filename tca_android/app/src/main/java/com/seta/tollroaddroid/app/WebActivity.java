package com.seta.tollroaddroid.app;

import android.annotation.SuppressLint;
import android.content.Intent;
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

import com.flurry.android.FlurryAgent;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.custom.MyWebChromeClient;
import com.seta.tollroaddroid.app.custom.MyWebViewClient;

public class WebActivity extends FragmentActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();
    private WebView wvTCA;
    private ImageView ivGoBack;
    private TextView tvTitle;
    private String url = "";
    private boolean needReload = false;
    private MyWebViewClient myWebViewClient = null;
    private String pageName = "";

    private void setPageName()
    {
        if(!url.isEmpty())
        {
            if(url.equals(Resource.PRIVACY_URL))
            {
                pageName = "Privacy";
            }
            else if(url.equals(Resource.TERMS_URL))
            {
                pageName = "Terms";
            }
            else if(url.equals(Resource.LEGAL_URL))
            {
                pageName = "Legal";
            }
            else if(url.equals(Resource.COMPARE_ACCOUNT_URL))
            {
                pageName = "Compare_Account";
            }
            else if(url.equals(Resource.SIGN_UP_AGREEMENT_URL))
            {
                pageName = "Sign_Up_Agreement";
            }
            else if(url.equals(Resource.FAQ_URL))
            {
                pageName = "FAQ";
            }
            else if(url.equals(Resource.SERVICE_CENTERS__URL))
            {
                pageName = "Service_Centers";
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        wvTCA = (WebView)findViewById(R.id.wv_tca);
        tvTitle = (TextView) findViewById(R.id.tv_title);

        Intent intent = getIntent();
        if(intent != null)
        {
            String title = intent.getStringExtra(Resource.KEY_TITLE);
            url = intent.getStringExtra(Resource.KEY_URL);

            if(title != null)
            {
                tvTitle.setText(title);
            }
        }
        Log.d("WebActivity","url:"+url);
        initWidgetValue();
        setupListener();
        setPageName();
        FlurryAgent.logEvent("Enter "+pageName+" page.");
    }

    private void setupListener()
    {
        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void initWidgetValue()
    {
        initWebView();

        if (url.lastIndexOf(".pdf") == (url.length()-4))
        {
            String pdfUrl = "https://docs.google.com/gview?embedded=true&url="+ url;
            wvTCA.loadUrl(pdfUrl);
        }
        else
        {
            wvTCA.loadUrl(url);
        }
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
