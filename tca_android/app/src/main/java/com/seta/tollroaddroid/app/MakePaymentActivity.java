package com.seta.tollroaddroid.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.json.PaymentMethod;
import com.seta.tollroaddroid.app.utilities.Constants;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MakePaymentActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private List<PaymentMethod> paymentMethods = new ArrayList<PaymentMethod>();
    private Response.ErrorListener errorListener;
    private Spinner payMethodSpinner;
    private TextView tvPayNow;
    private EditText etCVV,etAmount;
    private LinearLayout llCVV;
    private TextView tvAmount;
    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit Account_Payment_Make page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_payment);
        FlurryAgent.logEvent("Enter Account_Payment_Make page.");

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        payMethodSpinner = (Spinner)findViewById(R.id.pay_method_spinner);
        tvPayNow = (TextView)findViewById(R.id.tv_pay_now);
        etCVV = (EditText)findViewById(R.id.et_cvv);
        etAmount = (EditText)findViewById(R.id.et_amount);
        llCVV = (LinearLayout)findViewById(R.id.ll_cvv);

        etAmount.setCursorVisible(false);
        tvAmount = (TextView)findViewById(R.id.tv_amount);
        tvAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etAmount.requestFocus();
                InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(etAmount, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
//                if(!s.toString().isEmpty() && !s.toString().contains("."))
//                {
//                    int cursorPosition = etAmount.getSelectionEnd();
//                    etAmount.setText(s.toString()+".0");
//                    etAmount.setSelection(cursorPosition);
//                }
                if(!s.toString().isEmpty())
                {
                    tvAmount.setText(String.format("%.2f",(Float.valueOf(s.toString())/100.0f)));
                }
                else
                {
                    tvAmount.setText("0.00");
                }
            }
        });

        initWidgetValue();
        setupListener();
        getPaymentMethods();
    }

    private void setupListener()
    {
        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        payMethodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == paymentMethods.size()) {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(Resource.KEY_ONE_TIME_PAYMENT, true);
                    gotoActivityForResult(view.getContext(), NewPaymentMethodActivity.class,
                            bundle, Constants.ONE_TIME_PAYMENT);
                } else {
                    int type = paymentMethods.get(position).getPayment_type();
                    if (type == Constants.CREDIT_CARD_TYPE) {
                        llCVV.setVisibility(View.VISIBLE);
                    } else {
                        llCVV.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
        tvPayNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation()) {
                    makePayment();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Constants.ONE_TIME_PAYMENT)
        {
            if(resultCode == RESULT_OK)
            {
                setResult(RESULT_OK);
                finish();
            }
            else
            {
                if(paymentMethods.size() > 0) {
                    payMethodSpinner.setSelection(0);
                }
                else {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initWidgetValue()
    {

    }

    private boolean checkValidation()
    {
        boolean ret = true;
        if(llCVV.getVisibility() == View.VISIBLE && etCVV.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.cvv_empty_warning));
        }
        else if(etAmount.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.amount_empty_warning));
        }

        return ret;
    }

    private void populatePaymentSpinner()
    {
        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();

        for(int i =0; i< paymentMethods.size(); i++)
        {
            PaymentMethod paymentMethod = paymentMethods.get(i);
            if(paymentMethod.getPayment_type() == Constants.CREDIT_CARD_TYPE)
            {
                categories.add(paymentMethod.getCard_number());
            }
            else
            {
                categories.add(paymentMethod.getAccount_number());
            }
        }

        categories.add(getString(R.string.other));
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String> (this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        payMethodSpinner.setAdapter(dataAdapter);
    }

    private void getPaymentMethods()
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
                            if(response.has(Resource.KEY_PAYMENT_METHOD_LIST))
                            {
                                String info = response.optString(Resource.KEY_PAYMENT_METHOD_LIST);
                                Gson gson = new GsonBuilder().serializeNulls().create();
                                Type listType = new TypeToken<ArrayList<PaymentMethod>>() {}.getType();

                                paymentMethods = gson.fromJson(info, listType);
                                populatePaymentSpinner();
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
        showProgressDialog();
        ServerDelegate.getPaymentMethods(Resource.URL_PAYMENT, listener, errorListener);
    }

    private String populateParams()
    {
        String params = "cvv=" + etCVV.getText().toString();
        if(payMethodSpinner.getSelectedItemPosition() < paymentMethods.size()) {
            params = params + "&paymethod_id=" +
                    paymentMethods.get(payMethodSpinner.getSelectedItemPosition()).getPaymethod_id();
        }
        params = params+ "&amount="+tvAmount.getText().toString();// etAmount.getText().toString();

        return params;
    }

    private void makePayment()
    {
        String params = populateParams();
        Response.Listener listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                closeProgressDialog();
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, response);

                        if(checkResponse(response)) {
                            showToastMessage(getString(R.string.payment_successful));
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
        showProgressDialog();
        ServerDelegate.makePaymentRequest(Resource.URL_PAYMENT, params, listener, errorListener);
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
