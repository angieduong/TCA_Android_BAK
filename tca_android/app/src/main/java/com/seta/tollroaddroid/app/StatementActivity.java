package com.seta.tollroaddroid.app;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.custom.MyWebChromeClient;
import com.seta.tollroaddroid.app.custom.MyWebViewClient;
import com.seta.tollroaddroid.app.json.AccountInfo;
import com.seta.tollroaddroid.app.json.CommonResponse;
import com.seta.tollroaddroid.app.json.Invoice;
import com.seta.tollroaddroid.app.json.LocInfo;
import com.seta.tollroaddroid.app.json.OttTrip;
import com.seta.tollroaddroid.app.thirdparty.InputStreamVolleyRequest;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import static com.seta.tollroaddroid.app.R.id.map;

public class StatementActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();
    private WebView wvTCA;
    private ImageView ivGoBack;

    private String url = "";
    private boolean needReload = false;
    private MyWebViewClient myWebViewClient = null;

    private RelativeLayout layoutDatePicker, rlStartDate, rlEndDate;
    private DatePicker datePicker;
    private TextView tvDatePickerConfirm,tvDatePickerCancel;
    private TextView tvStartDate,tvEndDate;
    private boolean selectStartDate = true;
    private TextView tvEmail, tvDatesNotSelectedHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statement);

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        wvTCA = (WebView)findViewById(R.id.wv_tca);

        layoutDatePicker = (RelativeLayout)findViewById(R.id.layout_datePicker);
        datePicker = (DatePicker)findViewById(R.id.datePicker);
        tvDatePickerConfirm = (TextView)findViewById(R.id.tv_datePicker_ok);
        tvDatePickerCancel = (TextView)findViewById(R.id.tv_datePicker_cancel);
        tvStartDate = (TextView)findViewById(R.id.tv_start_date);
        tvEndDate = (TextView)findViewById(R.id.tv_end_date);

        rlStartDate = (RelativeLayout)findViewById(R.id.rl_start_date);
        rlEndDate = (RelativeLayout)findViewById(R.id.rl_end_date);

        tvEmail = (TextView)findViewById(R.id.tv_email);
        tvDatesNotSelectedHint = (TextView)findViewById(R.id.tv_dates_not_selected_hint);

        Log.d("WebActivity","url:"+url);
        initWidgetValue();
        setupListener();

        FlurryAgent.logEvent("Enter Statement page.");

        if(savedInstanceState == null)
        {
            Calendar c = Calendar.getInstance();

            String today = TollRoadsApp.getFormattedDateFromTimestamp(c.getTimeInMillis());
            String dateStr = c.get(Calendar.YEAR) + "-" + String.format("%02d", c.get(Calendar.MONTH) + 1)
                    + "-" + String.format("%02d", c.get(Calendar.DAY_OF_MONTH));

            tvEndDate.setTag(dateStr);
            tvEndDate.setText(today);
        }

        datePicker.setMaxDate((System.currentTimeMillis()/ONE_DAY+1)*ONE_DAY);
    }

    static final long ONE_DAY = 60*60*24*1000;
    private void setupListener()
    {
        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        rlStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStartDate = true;

                String dateStr = (String)tvStartDate.getTag();
                if(dateStr == null || dateStr.isEmpty()){
                    Calendar c = Calendar.getInstance();

                    // set current date into datepicker
                    datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), null);
                } else {
                    String[] dateArray = dateStr.split("-");
                    int year = Integer.valueOf(dateArray[0]);
                    int month = Integer.valueOf(dateArray[1]) - 1;
                    int day = Integer.valueOf(dateArray[2]);
                    datePicker.init(year, month, day, null);
                }
                layoutDatePicker.setVisibility(View.VISIBLE);
            }
        });
        rlEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStartDate = false;
                String dateStr = (String)tvEndDate.getTag();
                if(dateStr == null || dateStr.isEmpty()){
                    Calendar c = Calendar.getInstance();

                    // set current date into datepicker
                    datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), null);
                } else {
                    String[] dateArray = dateStr.split("-");
                    int year = Integer.valueOf(dateArray[0]);
                    int month = Integer.valueOf(dateArray[1]) - 1;
                    int day = Integer.valueOf(dateArray[2]);
                    datePicker.init(year, month, day, null);
                }
                layoutDatePicker.setVisibility(View.VISIBLE);
            }
        });
        layoutDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutDatePicker.setVisibility(View.GONE);
            }
        });
        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                return;
            }
        });
        tvDatePickerConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dateStr = datePicker.getYear() + "-" + String.format("%02d", datePicker.getMonth() + 1) + "-" + String.format("%02d", datePicker.getDayOfMonth());
                Calendar calendar = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());

                String systemFormat = TollRoadsApp.getFormattedDateFromTimestamp(calendar.getTimeInMillis());

                if (selectStartDate) {
                    tvStartDate.setText(systemFormat);
                    tvStartDate.setTag(dateStr);
                } else {
                    tvEndDate.setText(systemFormat);
                    tvEndDate.setTag(dateStr);
                }

                layoutDatePicker.setVisibility(View.GONE);

                if(!tvStartDate.getText().toString().isEmpty() &&
                        !tvEndDate.getText().toString().isEmpty())
                {
                    if(tvDatesNotSelectedHint.getVisibility() != View.GONE)
                    {
                        tvDatesNotSelectedHint.setVisibility(View.GONE);
                    }

                    //loadURL();
                    getStatement((String)tvStartDate.getTag(), (String)tvEndDate.getTag());
                }
            }
        });
        tvDatePickerCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutDatePicker.setVisibility(View.GONE);
            }
        });

        tvEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tvStartDate.getText().toString().isEmpty())
                {
                    Toast.makeText(v.getContext(),getString(R.string.start_date_empty_warning),Toast.LENGTH_LONG).show();
                }
                else if(tvEndDate.getText().toString().isEmpty())
                {
                    Toast.makeText(v.getContext(),getString(R.string.invoice_end_date_empty_warning),Toast.LENGTH_LONG).show();
                }
                else
                {
                    sendEmailRequest((String)tvStartDate.getTag(), (String)tvEndDate.getTag());
                }
//                else if(pdfFileName != null) {
//                    AccountInfo accountInfo = TollRoadsApp.getInstance().accountInfo;
//                    String defaultEmail = accountInfo.getEmail_address();
//                    if(defaultEmail == null)
//                    {
//                        defaultEmail = "";
//                    }
//                    TollRoadsApp.sendFileViaEmail(StatementActivity.this, pdfFileName,
//                            new String[]{defaultEmail},
//                            "Statement",
//                            "The attached is the recent statement.");
//                }
            }
        });
    }

    private void sendEmailRequest(String startDate, String endDate)
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
        ServerDelegate.sendEmailStatementRequest(Resource.URL_PAYMENT, listener, errorListener, startDate, endDate);
    }

    private void initWidgetValue()
    {
        initWebView();
//        url = "https://www.dmv.ca.gov/portal/wcm/connect/5a40cbcc-a9a9-4a42-ac43-a8564fa552c0/reg256.pdf?MOD=AJPERES&amp;CONVERT_TO=url&amp;CACHEID=5a40cbcc-a9a9-4a42-ac43-a8564fa552c0";
//                //"https://secure.thetollroads.com/customer/pdfServlet?startMonth=12&startDay=06&startYear=2016&endMonth=01&endDay=05&endYear=2017&cmd=pdfStmt2";
//
//        if (url.lastIndexOf(".pdf") == (url.length()-4))
//        {
//            String pdfUrl = "https://docs.google.com/gview?embedded=true&url="+ url;
//            wvTCA.loadUrl(pdfUrl);
//        }
//        else
//        {
//            wvTCA.loadUrl(url);
//        }
    }

    private void getStatement(String startDate, String endDate)
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
                            if(response.has(Resource.KEY_URL))
                            {
                                url = response.optString(Resource.KEY_URL);

                                loadURL();
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
        showProgressDialog();
        ServerDelegate.getStatement(Resource.URL_PAYMENT, listener, errorListener, startDate, endDate);
    }
    private String pdfUrl;
    private void loadURL()
    {
        //url = "https://www.dmv.ca.gov/portal/wcm/connect/5a40cbcc-a9a9-4a42-ac43-a8564fa552c0/reg256.pdf?MOD=AJPERES&amp;CONVERT_TO=url&amp;CACHEID=5a40cbcc-a9a9-4a42-ac43-a8564fa552c0";
        //"https://secure.thetollroads.com/customer/pdfServlet?startMonth=12&startDay=06&startYear=2016&endMonth=01&endDay=05&endYear=2017&cmd=pdfStmt2";

        pdfUrl = "https://docs.google.com/gview?embedded=true&url="+ Uri.encode(url);
        Log.d(LOG_TAG, "pdfUrl: " + pdfUrl);
        wvTCA.loadUrl(pdfUrl);
        //downloadPDFFile();
    }

    InputStreamVolleyRequest request;
    String pdfFileName;
    private void writeToFile(byte[] response)
    {
        try {
            if (response!=null) {

                //Read file name from headers (We have configured API to send file name in "Content-Disposition" header in following format: "File-Name:File-Format" example "MyDoc:pdf"

//                String content =request.responseHeaders.get("Content-Disposition")
//                        .toString();
//                StringTokenizer st = new StringTokenizer(content, "=");
//                String[] arrTag = st.toArray();

                pdfFileName = "tca_statement_demo.pdf";//arrTag[1];
//                filename = filename.replace(":", ".");
//                Log.d("DEBUG::FILE NAME", filename);


                try{
                    //covert response to input stream
                    InputStream input = new ByteArrayInputStream(response);

                    //Create a file on desired path and write stream data to it
                    final String dir = "/Android/data/" + getPackageName();

                    File appDataDir = new File(Environment.getExternalStorageDirectory().getPath() + dir);
                    Log.i("logtag", "appDataDir: " + appDataDir.getAbsolutePath());
                    if (!appDataDir.exists()) {
                        appDataDir.mkdirs();
                    }

                    File myFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                            dir + File.separator + pdfFileName);
                    if(!myFile.exists())
                    {
                        myFile.createNewFile();
                    }
                    //map.put("resume_path", file.toString());
                    BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(myFile));
                    byte data[] = new byte[1024];

                    long total = 0;
                    int count = 0;

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        output.write(data, 0, count);
                    }

                    output.flush();

                    output.close();
                    input.close();
                }catch(IOException e){
                    e.printStackTrace();

                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE");
            e.printStackTrace();
        }
    }

    private void downloadPDFFile()
    {
        showProgressDialog();

        Response.Listener listener = new Response.Listener<byte[]>() {
            @Override
            public void onResponse(byte[] response) {
                closeProgressDialog();
                try
                {
                    writeToFile(response);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                closeProgressDialog();

                if(error != null) {
                    Log.d(LOG_TAG, "Error: " + error.getMessage());

                }
            }
        };

        url = "https://www.dmv.ca.gov/portal/wcm/connect/5a40cbcc-a9a9-4a42-ac43-a8564fa552c0/reg256.pdf?MOD=AJPERES&amp;CONVERT_TO=url&amp;CACHEID=5a40cbcc-a9a9-4a42-ac43-a8564fa552c0";
        //"https://secure.thetollroads.com/customer/pdfServlet?startMonth=12&startDay=06&startYear=2016&endMonth=01&endDay=05&endYear=2017&cmd=pdfStmt2"+
        //    "&"+ServerDelegate.getCommonUrlExtra();

        request = new InputStreamVolleyRequest(Request.Method.GET, url, listener, errorListener, null);
        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext(),
                new HurlStack());
        mRequestQueue.add(request);

//        InputStreamVolleyRequest req = new InputStreamVolleyRequest(Request.Method.GET, url,
//                new Response.Listener<byte[]>() {
//                    @Override
//                    public void onResponse(byte[] response) {
//                        // TODO handle the response
//                        try {
//                            if (response!=null) {
//                                final String dir = "/Android/data/" + getPackageName();
//
//                                File appDataDir = new File(Environment.getExternalStorageDirectory().getPath() + dir);
//                                Log.i("logtag", "appDataDir: " + appDataDir.getAbsolutePath());
//                                if (!appDataDir.exists()) {
//                                    appDataDir.mkdirs();
//                                }
//
//                                File myFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
//                                        dir + File.separator + "tca_statement_demo.pdf");
//                                //FileOutputStream out = TAContext.getAppContext().openFileOutput("statement.pdf", Context.MODE_PRIVATE);
//
//                                if(!myFile.exists())
//                                {
//                                    myFile.createNewFile();
//                                }
//                                FileOutputStream outputStream;
//                                outputStream = new FileOutputStream(myFile);//openFileOutput(myFile.getName(), Context.MODE_PRIVATE);
//                                outputStream.write(response);
//                                outputStream.close();
//
//                            }
//                        } catch (Exception e) {
//                            // TODO Auto-generated catch block
//                            Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE");
//                            e.printStackTrace();
//                        }
//                        closeProgressDialog();
//                    }
//                } ,new Response.ErrorListener() {
//
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                error.printStackTrace();
//                closeProgressDialog();
//            }
//        }, null);
//        req.setRetryPolicy(
//                new DefaultRetryPolicy(
//                        500000,
//                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
//                )
//        );
//        // Adding request to request queue
//        TollRoadsApp.getInstance().addToRequestQueue(req);


    }

    private boolean loadedResource = false, loadingResource = false;

    @SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })
    void initWebView()
    {
        WebSettings settings = wvTCA.getSettings();

        wvTCA.setScrollContainer(false);
        wvTCA.setScrollbarFadingEnabled(false);
        wvTCA.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        wvTCA.setWebChromeClient(new MyWebChromeClient(){

        });
        myWebViewClient = new MyWebViewClient(this,true,wvTCA){
            @Override
            public
            void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                needReload = true;
                handler.cancel();
                Log.e(LOG_TAG, "onReceivedSslError error:"+error.toString());
                super.onReceivedSslError(view,handler,error);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                needReload = true;
                Log.e(LOG_TAG, "onReceivedError errorCode:"+ errorCode);
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                Log.e(LOG_TAG, "onReceivedError error:"+ error);
                super.onReceivedError(view, request, error);
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                Log.e(LOG_TAG, "onReceivedHttpError errorResponse:"+ errorResponse);
                super.onReceivedHttpError(view, request, errorResponse);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                loadingResource = true;
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
                loadingResource = false;
                Log.e(LOG_TAG, "onPageStarted url:"+ url);
                //ServerDelegate.appendLog("onPageStarted url:"+ url);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if(!loadedResource && loadingResource)
                {
                    wvTCA.reload();
                }
                Log.e(LOG_TAG, "onPageFinished url:"+ url);
                //ServerDelegate.appendLog("onPageFinished url:"+ url+",loadedResource:"+loadedResource);

                super.onPageFinished(view, url);
            }
        };

        wvTCA.setWebViewClient(myWebViewClient);

//        wvTCA.setDownloadListener(new DownloadListener() {
//            @Override
//            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
//                DownloadManager.Request request = new DownloadManager.Request(
//                        Uri.parse(url));
//
//
//                request.setMimeType(mimeType);
//
//
//                String cookies = CookieManager.getInstance().getCookie(url);
//
//
//                request.addRequestHeader("cookie", cookies);
//
//
//                request.addRequestHeader("User-Agent", userAgent);
//
//
//                request.setDescription("Downloading file...");
//
//
//                request.setTitle(URLUtil.guessFileName(url, contentDisposition,
//                        mimeType));
//
//
//                request.allowScanningByMediaScanner();
//
//
//                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//                request.setDestinationInExternalPublicDir(
//                        Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(
//                                url, contentDisposition, mimeType));
//                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
//                dm.enqueue(request);
//                Toast.makeText(getApplicationContext(), "Downloading File",
//                        Toast.LENGTH_LONG).show();
//
//            }
//        });
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(false);
        settings.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
        settings.setAllowFileAccess(true);
        settings.setJavaScriptEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDomStorageEnabled(true);
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
        FlurryAgent.logEvent("Exit Statement page.");
        super.onDestroy();
    }

}
