package com.seta.tollroaddroid.app.custom;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.util.Log;
import android.util.Patterns;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.seta.tollroaddroid.app.TollRoadsApp;

import java.util.Locale;

/**
 * Created by admin on 2016-01-06.
 */
public class MyWebViewClient extends WebViewClient {
    private final String LOG_TAG = this.getClass().getSimpleName();
    private Context context;
    private boolean useInternalWebView = false;
    private MyProgressDialog myProgressDialog;
    public MyWebViewClient(Context context)
    {
        this.context = context;
    }
    private WebView webView = null;

    public MyWebViewClient(Context context, boolean useInternalWebView)
    {
        this.context = context;
        this.useInternalWebView = useInternalWebView;
    }

    public MyWebViewClient(Context context, boolean useInternalWebView,WebView webView)
    {
        this.context = context;
        this.useInternalWebView = useInternalWebView;
        this.webView = webView;
    }

    public void showProgressDialog(String text)
    {
        if (myProgressDialog == null)
        {
            myProgressDialog = MyProgressDialog.show(context, text);;
        }
    }

    public void showProgressDialog()
    {
        showProgressDialog("");
    }

    public void closeProgressDialog()
    {
        if (myProgressDialog != null)
        {
            if(myProgressDialog.isShowing()) {
                myProgressDialog.cancel();
                myProgressDialog.dismiss();
            }
            myProgressDialog = null;
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if(url != null)
        {
            String mUrl = url.toLowerCase(Locale.getDefault());
            int mLenth = mUrl.length();
            Log.e(LOG_TAG, "shouldOverrideUrlLoading enter, url:" + url);
            if(mUrl.indexOf("tel:")==0)
            {
                //make a call
                TollRoadsApp.makeACall(view.getContext(), mUrl.subSequence(4, mLenth).toString());
                return true;
            }
            else if(mUrl.indexOf("mailto:")==0)
            {
                //email
                TollRoadsApp.sendAnEmail(view.getContext(), mUrl.subSequence(7, mLenth).toString());
                return true;
            }
            else
            {
                if(useInternalWebView) {
                    if(webView != null) {
                        if (url.lastIndexOf(".pdf") == (url.length() - 4)) {
                            String pdfUrl = "https://docs.google.com/gview?embedded=true&url=" + url;
                            webView.loadUrl(pdfUrl);
                            return true;
                        }
                    }
                    return false;
                }
                else {
                    String mWebSite = url;
                    if (!url.contains("http")) {
                        mWebSite = "http://" + url;
                    }

                    if (Patterns.WEB_URL.matcher(mWebSite).matches()) {
                        Uri uri = Uri.parse(mWebSite);

                        Intent mIntent = new Intent(Intent.ACTION_VIEW, uri);
                        context.startActivity(mIntent);
                    }
                }
            }
        }
        else
        {
            Log.e(LOG_TAG, "shouldOverrideUrlLoading enter, url is null");
        }

        return true;
    }

    @Override
    public
    void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        handler.cancel();
        closeProgressDialog();
        Log.e(LOG_TAG, "onReceivedSslError error:"+error.toString());
        super.onReceivedSslError(view, handler, error);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        showProgressDialog();
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        closeProgressDialog();
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        Log.e(LOG_TAG, "onLoadResource url:"+ url);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view,
                                                      String url) {
        Log.e(LOG_TAG, "shouldInterceptRequest url:"+ url);
        return null;
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        Log.e(LOG_TAG, "onReceivedError error:"+ error);
        super.onReceivedError(view, request, error);
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        Log.e(LOG_TAG, "onReceivedHttpError errorResponse:"+ errorResponse.toString());
        super.onReceivedHttpError(view, request, errorResponse);
    }


}