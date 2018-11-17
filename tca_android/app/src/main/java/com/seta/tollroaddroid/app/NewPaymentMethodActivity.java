package com.seta.tollroaddroid.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.json.PaymentMethodUpdateRequest;
import com.seta.tollroaddroid.app.utilities.Constants;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;

public class NewPaymentMethodActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private LinearLayout llCreditCard, llECheck;
    private Spinner spPayMethod,spPaymentOrder,spReplenishmentAmount;
    private EditText etCardNo, etNameOnCard, etExpirationDate, etZipCode;
    private EditText etRoutingNo, etFirstName, etLastName, etAccountNo;
    private TextView tvSave;
    private PaymentMethodUpdateRequest paymentMethodUpdateRequest = new PaymentMethodUpdateRequest();
    private Response.ErrorListener errorListener;
    private RelativeLayout rlPayMethodControl, rlCreditCardOnly;
    private TextView tvTitle;
    private boolean isOneTimePayment = false;
    private RelativeLayout rlPayOrder, rlAmount;
    private LinearLayout llCVV;
    private EditText etCVV,etAmount;
    private TextView tvAmount;

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit Account_Payment_Method_New page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_payment_method);
        FlurryAgent.logEvent("Enter Account_Payment_Method_New page.");

        Intent intent = getIntent();
        if(intent != null)
        {
            isOneTimePayment = intent.getBooleanExtra(Resource.KEY_ONE_TIME_PAYMENT, false);
        }

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvSave = (TextView)findViewById(R.id.tv_save);

        llCreditCard = (LinearLayout)findViewById(R.id.ll_credit_card);
        llECheck = (LinearLayout)findViewById(R.id.ll_echeck);
        spPayMethod = (Spinner)findViewById(R.id.pay_method_spinner);
        spPaymentOrder = (Spinner)findViewById(R.id.payment_order_spinner);
        spReplenishmentAmount = (Spinner)findViewById(R.id.replenishment_amount_spinner);
        etCardNo = (EditText)findViewById(R.id.et_card_no);
        etNameOnCard = (EditText)findViewById(R.id.et_name_on_card);
        etExpirationDate = (EditText)findViewById(R.id.et_expiration_date);
        etZipCode = (EditText)findViewById(R.id.et_billing_zip_code);

        etRoutingNo = (EditText)findViewById(R.id.et_routing_no);
        etFirstName = (EditText)findViewById(R.id.et_first_name);
        etLastName = (EditText)findViewById(R.id.et_last_name);
        etAccountNo = (EditText)findViewById(R.id.et_account_no);

        rlPayMethodControl = (RelativeLayout)findViewById(R.id.rl_pay_method_control);
        rlCreditCardOnly = (RelativeLayout)findViewById(R.id.rl_credit_card_only);

        tvTitle = (TextView)findViewById(R.id.tv_title);
        rlPayOrder = (RelativeLayout)findViewById(R.id.rl_pay_order);
        rlAmount = (RelativeLayout)findViewById(R.id.rl_amount);
        llCVV = (LinearLayout)findViewById(R.id.ll_cvv);
        etCVV = (EditText)findViewById(R.id.et_cvv);
        etAmount = (EditText)findViewById(R.id.et_amount);
        
        initWidgetValue();
        setupListener();

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
        spPayMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    llCreditCard.setVisibility(View.VISIBLE);
                    llECheck.setVisibility(View.GONE);

                    if (isOneTimePayment) {
                        llCVV.setVisibility(View.VISIBLE);
                    } else {
                        llCVV.setVisibility(View.GONE);
                    }
                } else {
                    llCreditCard.setVisibility(View.GONE);
                    llECheck.setVisibility(View.VISIBLE);
                    llCVV.setVisibility(View.GONE);
                }
                tvSave.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                tvSave.setVisibility(View.VISIBLE);
            }
        };

        etCardNo.addTextChangedListener(textWatcher);
        etNameOnCard.addTextChangedListener(textWatcher);
        etExpirationDate.addTextChangedListener(textWatcher);
        etZipCode.addTextChangedListener(textWatcher);
        etRoutingNo.addTextChangedListener(textWatcher);
        etFirstName.addTextChangedListener(textWatcher);
        etLastName.addTextChangedListener(textWatcher);
        etAccountNo.addTextChangedListener(textWatcher);

        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation()) {
                    setRequest();
                    showProgressDialog();
                    String params = populateParams();
                    if(isOneTimePayment) {
                        oneTimePaymentRequest(Resource.URL_PAYMENT, params);
                    }
                    else {
                        updatePaymentMethodRequest(Resource.URL_PAYMENT, params);
                    }
                }
            }
        });
    }

    private void initWidgetValue()
    {
        if(TollRoadsApp.getInstance().accountInfo != null &&
                (TollRoadsApp.getInstance().accountInfo.getAccount_type()
                        == Constants.ACCOUNT_TYPE_CHARGE_EXPRESS) ||
                (TollRoadsApp.getInstance().accountInfo.getAccount_type()
                    == Constants.ACCOUNT_TYPE_INVOICE_EXPRESS))
        {
            rlCreditCardOnly.setVisibility(View.VISIBLE);
            rlPayMethodControl.setVisibility(View.GONE);
        }
        else
        {
            rlCreditCardOnly.setVisibility(View.GONE);
            rlPayMethodControl.setVisibility(View.VISIBLE);
        }

        llCreditCard.setVisibility(View.VISIBLE);
        llECheck.setVisibility(View.GONE);

        if(isOneTimePayment)
        {
            tvTitle.setText(getString(R.string.other));
            tvSave.setText(getString(R.string.pay));
            rlPayOrder.setVisibility(View.GONE);
            rlAmount.setVisibility(View.VISIBLE);
            if(spPayMethod.getSelectedItemPosition() == 0)
            {
                llCVV.setVisibility(View.VISIBLE);
            }
            else
            {
                llCVV.setVisibility(View.GONE);
            }
            tvSave.setVisibility(View.VISIBLE);
        }
        else
        {
            tvTitle.setText(getString(R.string.new_payment_method));
            tvSave.setText(getString(R.string.save));
            rlPayOrder.setVisibility(View.VISIBLE);
            rlAmount.setVisibility(View.GONE);
            llCVV.setVisibility(View.GONE);
        }
    }
    private String formatDate(String dateString)
    {
        String ret = "";
        if(dateString != null && !dateString.isEmpty())
        {
            if(dateString.length() < 2) {
                ret = dateString;
            }
            else
            {
                ret = dateString.substring(0,2)+"/"+dateString.substring(2);
            }
        }
        return ret;
    }
    
    private void setRequest()
    {
        if(spReplenishmentAmount.getSelectedItemPosition() == 0) {
            paymentMethodUpdateRequest.setReplenishment_amt("30.00");
        }
        else if(spReplenishmentAmount.getSelectedItemPosition() == 1) {
            paymentMethodUpdateRequest.setReplenishment_amt("60.00");
        }
        else
        {
            paymentMethodUpdateRequest.setReplenishment_amt("90.00");
        }
        
        if(spPayMethod.getSelectedItemPosition() == 0)
        {
            paymentMethodUpdateRequest.setPayment_type(Constants.CREDIT_CARD_TYPE);
            paymentMethodUpdateRequest.setCard_number(etCardNo.getText().toString());
            paymentMethodUpdateRequest.setCard_holder_name(etNameOnCard.getText().toString());
            paymentMethodUpdateRequest.setExpired_date(formatDate(etExpirationDate.getText().toString()));
            paymentMethodUpdateRequest.setZip_code(etZipCode.getText().toString());
            //paymentMethodUpdateRequest.setCvv2(etCVV.getText().toString());
        }
        else
        {
            paymentMethodUpdateRequest.setPayment_type(Constants.ELECTRONIC_CHECK_TYPE);
            paymentMethodUpdateRequest.setRouting_number(etRoutingNo.getText().toString());
            paymentMethodUpdateRequest.setAccount_number(etAccountNo.getText().toString());
            paymentMethodUpdateRequest.setFirst_name(etFirstName.getText().toString());
            paymentMethodUpdateRequest.setLast_name(etLastName.getText().toString());
        }
        paymentMethodUpdateRequest.setPayment_order(spPaymentOrder.getSelectedItemPosition()+1);
        paymentMethodUpdateRequest.setPaymethod_id("");

        if(isOneTimePayment)
        {
            if(llCVV.getVisibility() == View.VISIBLE)
            {
                paymentMethodUpdateRequest.setCvv2(etCVV.getText().toString());
            }
            paymentMethodUpdateRequest.setAmount(tvAmount.getText().toString());
        }
    }

    private boolean checkValidation()
    {
        boolean ret = true;
        if(spPayMethod.getSelectedItemPosition() == 0) {
            if (etCardNo.getText().length() == 0) {
                ret = false;
                showToastMessage(getString(R.string.card_no_empty_warning));
//                } else if (llCVV.getVisibility() == View.VISIBLE
//                        && etCVV.getText().length() == 0) {
//                    ret = false;
//                    showToastMessage(getString(R.string.cvv_empty_warning));
            } else if (etNameOnCard.getText().length() == 0) {
                ret = false;
                showToastMessage(getString(R.string.name_on_card_empty_warning));
            } else if (etExpirationDate.getText().length() == 0) {
                ret = false;
                showToastMessage(getString(R.string.expiration_date_empty_warning));
            }
//            else if (etZipCode.getText().length() == 0) {
//                ret = false;
//                showToastMessage(getString(R.string.billing_zip_code_empty_warning));
//            }
        }
        else
        {
            if (etRoutingNo.getText().length() == 0) {
                ret = false;
                showToastMessage(getString(R.string.routing_no_empty_warning));
            } else if (etAccountNo.getText().length() == 0) {
                ret = false;
                showToastMessage(getString(R.string.account_no_empty_warning));
            } else if (etFirstName.getText().length() == 0) {
                ret = false;
                showToastMessage(getString(R.string.first_name_empty_warning));
            } else if (etLastName.getText().length() == 0) {
                ret = false;
                showToastMessage(getString(R.string.last_name_empty_warning));
            }
        }
        if(isOneTimePayment) {
            if (llCVV.getVisibility() == View.VISIBLE && etCVV.getText().length() == 0) {
                ret = false;
                showToastMessage(getString(R.string.cvv_empty_warning));
            } else if (etAmount.getText().length() == 0) {
                ret = false;
                showToastMessage(getString(R.string.amount_empty_warning));
            }
        }
        return ret;
    }

    private String populateParams()
    {
        String params = "";

        if(paymentMethodUpdateRequest != null) {
            Gson gson = new GsonBuilder().serializeNulls().create();
            String jsonStr = gson.toJson(paymentMethodUpdateRequest,
                    PaymentMethodUpdateRequest.class);
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

    private void updatePaymentMethodRequest(String url,String params)
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
                            showToastMessage(getString(R.string.successfully_saved));
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

        ServerDelegate.updateRequest(url, params, listener, errorListener);
    }

    private void oneTimePaymentRequest(String url,String params)
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

        ServerDelegate.oneTimePaymentRequest(url, params, listener, errorListener);
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
