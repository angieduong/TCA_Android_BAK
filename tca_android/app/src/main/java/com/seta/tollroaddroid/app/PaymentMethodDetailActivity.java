package com.seta.tollroaddroid.app;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.custom.MultiButtonsPopupDialog;
import com.seta.tollroaddroid.app.json.PaymentMethod;
import com.seta.tollroaddroid.app.utilities.Constants;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;

public class PaymentMethodDetailActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private PaymentMethod gPaymentMethod = TollRoadsApp.getInstance().gPaymentMethod;
    private TextView tvTitle;
    private LinearLayout llCreditCard, llECheck;
    private Spinner spPaymentOrder;
    private EditText etCardNo, etNameOnCard, etExpirationDate, etZipCode;
    private EditText etRoutingNo, etFirstName, etLastName, etAccountNo;
	private TextView tvSave, tvRemove;
    
    private Response.ErrorListener errorListener;
    private int curPaymentOrder = 0;
    private String paymentCreditCardNo;
    private MultiButtonsPopupDialog gMultiButtonsPopupDialog;
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("CurrentPaymentMethod", gPaymentMethod);
    }

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit Account_Payment_Method_Detail page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method_detail);
        if(savedInstanceState != null)
        {
            gPaymentMethod = (PaymentMethod) savedInstanceState.getSerializable("CurrentPaymentMethod");
        }
        FlurryAgent.logEvent("Enter Account_Payment_Method_Detail page.");

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvTitle = (TextView)findViewById(R.id.tv_title);
        tvSave = (TextView)findViewById(R.id.tv_save);
        llCreditCard = (LinearLayout)findViewById(R.id.ll_credit_card);
        llECheck = (LinearLayout)findViewById(R.id.ll_echeck);
        spPaymentOrder = (Spinner)findViewById(R.id.payment_order_spinner);
        etCardNo = (EditText)findViewById(R.id.et_card_no);
        etNameOnCard = (EditText)findViewById(R.id.et_name_on_card);
        etExpirationDate = (EditText)findViewById(R.id.et_expiration_date);
        etZipCode = (EditText)findViewById(R.id.et_billing_zip_code);

        etRoutingNo = (EditText)findViewById(R.id.et_routing_no);
        etFirstName = (EditText)findViewById(R.id.et_first_name);
        etLastName = (EditText)findViewById(R.id.et_last_name);
        etAccountNo = (EditText)findViewById(R.id.et_account_no);

        paymentCreditCardNo = gPaymentMethod.getCard_number();

        tvRemove = (TextView)findViewById(R.id.tv_remove_payment_method);
        initWidgetValue();
        setupListener();
    }

    @Override
    public void showToastMessage(String msg) {
        gPaymentMethod.setCard_number(paymentCreditCardNo);
        super.showToastMessage(msg);
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

                    updatePaymentMethodRequest(Resource.URL_PAYMENT, params);
                }
            }
        });

        spPaymentOrder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(curPaymentOrder != position) {
                    tvSave.setVisibility(View.VISIBLE);
                    curPaymentOrder = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        tvRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpDeleteConfirmDialog();
            }
        });
    }

    private void deletePaymentMethodRequest(String url,String params)
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
                            showToastMessage(getString(R.string.successfully_deleted));
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

        ServerDelegate.deleteRequest(url, params, listener, errorListener);
    }

    private void showUpDeleteConfirmDialog()
    {
        gMultiButtonsPopupDialog = new MultiButtonsPopupDialog(this,R.style.CustomProgressDialog);
        gMultiButtonsPopupDialog.createDialog(this);
        gMultiButtonsPopupDialog.CutomizeTitle(View.VISIBLE, getString(R.string.remove_payment_method_title));

        gMultiButtonsPopupDialog.CutomizeContent(View.VISIBLE, getString(R.string.remove_payment_method_content));
        gMultiButtonsPopupDialog.CutomizeExtraButton(View.VISIBLE, 0, getString(R.string.delete), Color.RED, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                String params = "paymethod_id="+gPaymentMethod.getPaymethod_id();

                deletePaymentMethodRequest(Resource.URL_PAYMENT, params);
            }
        });
        gMultiButtonsPopupDialog.CutomizePositiveButton(View.GONE, 0, "", null);
        gMultiButtonsPopupDialog.CutomizeNeutralButton(View.GONE, 0, "", null);
        gMultiButtonsPopupDialog.CutomizeNegativeButton(View.VISIBLE, 0, getString(R.string.cancel), null);

        gMultiButtonsPopupDialog.show();
    }

    private void initCreditCard()
    {
        llCreditCard.setVisibility(View.VISIBLE);
        llECheck.setVisibility(View.GONE);
        if(gPaymentMethod.getCard_number() != null)
        {
            etCardNo.setText(gPaymentMethod.getCard_number());
        }
        if(gPaymentMethod.getCard_holder_name() != null)
        {
            etNameOnCard.setText(gPaymentMethod.getCard_holder_name());
        }
        if(gPaymentMethod.getExpired_date() != null)
        {
            etExpirationDate.setText(gPaymentMethod.getExpired_date().replace("/", ""));
        }
        if(gPaymentMethod.getZip_code() != null)
        {
            etZipCode.setText(gPaymentMethod.getZip_code());
        }
    }

    private void initECheck()
    {
        llCreditCard.setVisibility(View.GONE);
        llECheck.setVisibility(View.VISIBLE);

        if(gPaymentMethod.getRouting_number() != null)
        {
            etRoutingNo.setText(gPaymentMethod.getRouting_number());
        }
        if(gPaymentMethod.getFirst_name() != null)
        {
            etFirstName.setText(gPaymentMethod.getFirst_name());
        }
        if(gPaymentMethod.getLast_name() != null)
        {
            etLastName.setText(gPaymentMethod.getLast_name());
        }
        if(gPaymentMethod.getAccount_number() != null)
        {
            etAccountNo.setText(gPaymentMethod.getAccount_number());
        }
    }

    private void initWidgetValue()
    {
        if(gPaymentMethod != null)
        {
            if(gPaymentMethod.getPayment_type() != 5)
            {
                number = gPaymentMethod.getCard_number();
                tvTitle.setText(gPaymentMethod.getCard_number());
                initCreditCard();
            }
            else
            {
                number = gPaymentMethod.getRouting_number();
                tvTitle.setText(gPaymentMethod.getRouting_number());
                initECheck();
            }
            Log.e("init", "order:" + gPaymentMethod.getPayment_order());
            if(gPaymentMethod.getPayment_order() <= 3) {
                spPaymentOrder.setSelection(gPaymentMethod.getPayment_order() - 1);
            }
            curPaymentOrder = spPaymentOrder.getSelectedItemPosition();
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
    private String number = "";

    private void setRequest()
    {
        if(gPaymentMethod.getPayment_type() == Constants.CREDIT_CARD_TYPE)
        {
            if(etCardNo.getText().toString().equals(number)) {
                gPaymentMethod.setCard_number("");
            }
            else {
                gPaymentMethod.setCard_number(etCardNo.getText().toString());
            }

            gPaymentMethod.setCard_holder_name(etNameOnCard.getText().toString());
            gPaymentMethod.setExpired_date(formatDate(etExpirationDate.getText().toString()));
            gPaymentMethod.setZip_code(etZipCode.getText().toString());
            //gPaymentMethod.setCvv2(etCVV.getText().toString());
        }
        else
        {
            if(etRoutingNo.getText().toString().equals(number)) {
                gPaymentMethod.setRouting_number("");
            } else {
                gPaymentMethod.setRouting_number(etRoutingNo.getText().toString());
            }

            gPaymentMethod.setAccount_number(etAccountNo.getText().toString());
            gPaymentMethod.setFirst_name(etFirstName.getText().toString());
            gPaymentMethod.setLast_name(etLastName.getText().toString());
        }
        gPaymentMethod.setPayment_order(spPaymentOrder.getSelectedItemPosition() + 1);
    }

    private boolean checkValidation()
    {
        boolean ret = true;
        if(gPaymentMethod.getPayment_type() == Constants.CREDIT_CARD_TYPE) {
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

        return ret;
    }

    private String populateParams()
    {
        String params = "";

        if(gPaymentMethod != null) {
            Gson gson = new GsonBuilder().serializeNulls().create();
            String jsonStr = gson.toJson(gPaymentMethod,
                    PaymentMethod.class);
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
    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {

        super.onPause();
    }

}
