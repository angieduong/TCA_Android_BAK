package com.seta.tollroaddroid.app.custom;

import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * Created by admin on 2016-01-06.
 */
public class MyWebChromeClient extends WebChromeClient {
    @Override
    public void onProgressChanged(WebView view, int newProgress)
    {
        Log.e("onProgressChanged", "newProgress:" + newProgress);
        super.onProgressChanged(view, newProgress);
    }

}
