package com.seta.tollroaddroid.app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import java.io.UnsupportedEncodingException;

public class RequestTranspondersActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private TextView tvDone;
    private Response.ErrorListener errorListener;
    private Spinner spType, spQuantity;

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit Account_Transponders_Request page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_transponders);
        FlurryAgent.logEvent("Enter Account_Transponders_Request page.");

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvDone = (TextView)findViewById(R.id.tv_done);
        spType = (Spinner)findViewById(R.id.sp_type);
        spQuantity = (Spinner)findViewById(R.id.sp_quantity);

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
                finish();
            }
        });
        tvDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String params = populateParams();
                showProgressDialog();

                requestTransponders(Resource.URL_TRANSPONDER, params);
            }
        });
    }

    private String populateParams()
    {
        String type;

        if(spType.getSelectedItemPosition() < 2)
        {
            type = String.valueOf(spType.getSelectedItemPosition());
        }
        else
        {
            type = "3";
        }
        String params = "type=" + type;
        params = params + "&quantity=" + (spQuantity.getSelectedItemPosition()+1);
        return  params;
    }

    private void requestTransponders(String url,String params)
    {
        Response.Listener listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                closeProgressDialog();
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, "response:" + response);

                        if(checkResponse(response)) {
                            showToastMessage(getString(R.string.request_successful));
                            setResult(RESULT_OK);
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

        ServerDelegate.requestTransponders(url, params, listener, errorListener);
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
