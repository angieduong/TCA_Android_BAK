package com.seta.tollroaddroid.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.custom.MultiButtonsPopupDialog;
import com.seta.tollroaddroid.app.json.OTTUserInfoResponse;
import com.seta.tollroaddroid.app.json.PaySelectedViolationsRequest;
import com.seta.tollroaddroid.app.json.SelectedUnpaidViolationsResponse;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

public class ViolationContactInfoActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private TextView tvGoNext;
    private EditText etName, etPhone, etEmail,etRetypeEmail;
    

    private Response.ErrorListener errorListener;

    private MultiButtonsPopupDialog gMultiButtonsPopupDialog;

    private TextView tvTotalCharge;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("SelectedUnpaidViolationsResponse", TollRoadsApp.getInstance().selectedUnpaidViolationsResponse);
    }

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit violation_contact page.");
        super.onDestroy();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_violation_contact_info);
        FlurryAgent.logEvent("Enter violation_contact page.");

        if(savedInstanceState != null)
        {
            TollRoadsApp.getInstance().selectedUnpaidViolationsResponse = (SelectedUnpaidViolationsResponse)
                    savedInstanceState.getSerializable("SelectedUnpaidViolationsResponse");
        }

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvGoNext = (TextView) findViewById(R.id.tv_go_next);


        etName = (EditText)findViewById(R.id.et_name);
        etPhone = (EditText)findViewById(R.id.et_phone);
        etRetypeEmail = (EditText)findViewById(R.id.et_re_type_email);

        etEmail = (EditText)findViewById(R.id.et_email);


        tvTotalCharge = (TextView)findViewById(R.id.tv_total_charge);

        setupListener();

        debugModeInit(savedInstanceState);
    }

    private void debugModeInit(Bundle savedInstanceState)
    {
        if(savedInstanceState == null && BuildConfig.DEBUG)
        {
            etName.setText("t");
            etPhone.setText("1235678912");

            etEmail.setText("t@t.com");
            etRetypeEmail.setText("t@t.com");

        }
    }

    private void hideAllSoftKeyboard()
    {
        InputMethodManager gImm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        if(gImm != null) {
            gImm.hideSoftInputFromWindow(etRetypeEmail.getWindowToken(), 0);
            gImm.hideSoftInputFromWindow(etEmail.getWindowToken(), 0);
            gImm.hideSoftInputFromWindow(etName.getWindowToken(), 0);
            gImm.hideSoftInputFromWindow(etPhone.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            gotoActivity(this, ViolationDashboardActivity.class, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            return  true;
        }
        return super.onKeyDown(keyCode, event);
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

                gotoActivity(v.getContext(), ViolationDashboardActivity.class, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
        });

        tvGoNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation()) {
                    showProgressDialog();

                    String params = populateUserInfoParams();

                    params = params +"&"+ServerDelegate.getCommonUrlExtra();
                    params = params +"&"+Resource.KEY_ACTION+"="+ Resource.ACTION_CHECK_CONTACT;

                    CheckContactRequest(Resource.URL_VIOLATION_PAYMENT, params);
                }
            }
        });
    }


    private void showUpConfirmDialog()
    {
        gMultiButtonsPopupDialog = new MultiButtonsPopupDialog(this,R.style.CustomProgressDialog);
        gMultiButtonsPopupDialog.createDialog(this);
        gMultiButtonsPopupDialog.CutomizeTitle(View.GONE, "");

        gMultiButtonsPopupDialog.CutomizeContent(View.VISIBLE, getString(R.string.ott_missing_email_hint));
        gMultiButtonsPopupDialog.CutomizeExtraButton(View.GONE, 0, "", 0, null);
        gMultiButtonsPopupDialog.CutomizePositiveButton(View.VISIBLE, 0, getString(R.string.ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                String params = populateUserInfoParams();
                params = params +"&"+ServerDelegate.getCommonUrlExtra();
                params = params +"&"+Resource.KEY_ACTION+"="+ Resource.ACTION_CHECK_CONTACT;
                CheckContactRequest(Resource.URL_VIOLATION_PAYMENT, params);
            }
        });
        gMultiButtonsPopupDialog.CutomizeNeutralButton(View.GONE, 0, "", null);
        gMultiButtonsPopupDialog.CutomizeNegativeButton(View.VISIBLE, 0, getString(R.string.cancel), null);

        gMultiButtonsPopupDialog.show();
    }

    private String populateRequestParams()
    {
        String params = "";

        if(TollRoadsApp.getInstance().paySelectedViolationsRequest != null) {
            Gson gson = new GsonBuilder().serializeNulls().create();
            String jsonStr = gson.toJson(TollRoadsApp.getInstance().paySelectedViolationsRequest,
                    PaySelectedViolationsRequest.class);
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

    private void setUserInfoRequest()
    {
        PaySelectedViolationsRequest paySelectedViolationsRequest = TollRoadsApp.getInstance().paySelectedViolationsRequest;

        paySelectedViolationsRequest.setAddress_contact(etName.getText().toString());
        paySelectedViolationsRequest.setPrimary_phone(etPhone.getText().toString());
        
        paySelectedViolationsRequest.setEmail_address(etEmail.getText().toString());
        paySelectedViolationsRequest.setEmail_address2(etRetypeEmail.getText().toString());
    }
    
    private String populateUserInfoParams()
    {
        setUserInfoRequest();
        
        String params = populateRequestParams();
        
        return params;
    }

    private boolean checkContactInfoValidation()
    {
        boolean ret = true;
        if(etName.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.first_name_empty_warning));
        }
        else if(etPhone.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.phone_no_empty_warning));
        }
        else if(etEmail.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.email_empty_warning));
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString()).matches())
        {
            ret = false;
            showToastMessage(getString(R.string.email_invalid_warning));
        }
        else if(!etEmail.getText().toString().equals(etRetypeEmail.getText().toString())) {
            ret = false;
            showToastMessage(getString(R.string.email_not_match_warning));
        }
        return ret;
    }

    private long getTimeStamp(String dateString)
    {
        if(dateString == null || dateString.isEmpty())
            return 0;

        GregorianCalendar cal = new GregorianCalendar();
        String[] dateArray = dateString.split("-");
        int year = Integer.valueOf(dateArray[0]);
        int month = Integer.valueOf(dateArray[1]) - 1;
        int day = Integer.valueOf(dateArray[2]);
        cal.set(year, month, day);
        return cal.getTimeInMillis();
    }

    private long getTodayTimeStamp()
    {
        GregorianCalendar curCal = new GregorianCalendar();
        Calendar c = Calendar.getInstance();
        curCal.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        return curCal.getTimeInMillis();
    }

    private boolean checkValidation()
    {
        boolean ret = true;

        if(!checkContactInfoValidation())
        {
            ret = false;
        }

        return ret;
    }

    private void CheckContactRequest(String url,String params)
    {
        Response.Listener listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                closeProgressDialog();
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, "response:" + response);

                        if(checkResponse(response.toString())) {
                            gotoActivity(ViolationContactInfoActivity.this, ViolationSelectPaymentActivity.class,
                                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
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

        url = url+"?"+params;
        ServerDelegate.commonPostRequest(url, listener, errorListener);

    }
    

    private void setTotalCharge()
    {
        if(TollRoadsApp.getInstance().selectedUnpaidViolationsResponse != null &&
                TollRoadsApp.getInstance().selectedUnpaidViolationsResponse.getSel_unpaid_vios_total_amount_due() != null)
        {
            tvTotalCharge.setText(getString(R.string.total_amount_due,
                    TollRoadsApp.getInstance().selectedUnpaidViolationsResponse.getSel_unpaid_vios_total_amount_due()));

            tvTotalCharge.setVisibility(View.VISIBLE);
        }
        else
        {
            tvTotalCharge.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTotalCharge();
    }

    @Override
    public void onPause() {

        super.onPause();
    }

}
