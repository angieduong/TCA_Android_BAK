package com.seta.tollroaddroid.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentMethodToken;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seta.tollroaddroid.app.GooglePay.PaymentsUtil;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.custom.MultiButtonsPopupDialog;
import com.seta.tollroaddroid.app.json.AutoPopulatePaymentInfo;
import com.seta.tollroaddroid.app.json.PaySelectedViolationsRequest;
import com.seta.tollroaddroid.app.json.SelectedUnpaidViolationsResponse;
import com.seta.tollroaddroid.app.utilities.Constants;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;

public class ViolationPaymentInfoActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private TextView tvGoNext;
    private EditText etCardNo, etNameOnCard, etExpirationDate, etZipCode,etCVV;
    private EditText etRoutingNo, etFirstName, etLastName, etAccountNo;
    
    private TextView tvTotalCharge;
    private MultiButtonsPopupDialog gMultiButtonsPopupDialog;
    private Response.ErrorListener errorListener;
    private LinearLayout llCreditCard, llEcheck;
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;

    private PaymentsClient mPaymentsClient;

    private void prePopulateWidget(AutoPopulatePaymentInfo autoPopulatePaymentInfo)
    {
        if(autoPopulatePaymentInfo != null) {
            etCardNo.setText(autoPopulatePaymentInfo.getCard_number());
            etNameOnCard.setText(autoPopulatePaymentInfo.getCard_holder_name());
            etExpirationDate.setText(autoPopulatePaymentInfo.getExpired_date().replace("/",""));
            etZipCode.setText(autoPopulatePaymentInfo.getZip_code());
            etCVV.setText(autoPopulatePaymentInfo.getCvv2());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("SelectedUnpaidViolationsResponse", TollRoadsApp.getInstance().selectedUnpaidViolationsResponse);
        outState.putSerializable("PaySelectedViolationsRequest", TollRoadsApp.getInstance().paySelectedViolationsRequest);

    }

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit violation_payment page.");
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(TollRoadsApp.getInstance().paySelectedViolationsRequest.
                getPayment_type() == Constants.GOOGLE_PAY_TYPE)
        {
            requestPayment();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_violation_payment_info);
        if(savedInstanceState != null)
        {
            TollRoadsApp.getInstance().selectedUnpaidViolationsResponse = (SelectedUnpaidViolationsResponse)
                    savedInstanceState.getSerializable("SelectedUnpaidViolationsResponse");

            TollRoadsApp.getInstance().paySelectedViolationsRequest = (PaySelectedViolationsRequest)
                    savedInstanceState.getSerializable("PaySelectedViolationsRequest");
        }
        FlurryAgent.logEvent("Enter violation_payment page.");

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvGoNext = (TextView) findViewById(R.id.tv_go_next);

        llCreditCard = (LinearLayout)findViewById(R.id.ll_credit_card);
        llEcheck = (LinearLayout)findViewById(R.id.ll_echeck);

        etCardNo = (EditText)findViewById(R.id.et_card_no);
        etNameOnCard = (EditText)findViewById(R.id.et_name_on_card);
        etExpirationDate = (EditText)findViewById(R.id.et_expiration_date);
        etZipCode = (EditText)findViewById(R.id.et_billing_zip_code);
        etCVV = (EditText)findViewById(R.id.et_cvv);

        etRoutingNo = (EditText)findViewById(R.id.et_routing_no);
        etFirstName = (EditText)findViewById(R.id.et_first_name);
        etLastName = (EditText)findViewById(R.id.et_last_name);
        etAccountNo = (EditText)findViewById(R.id.et_account_no);
        
        tvTotalCharge = (TextView)findViewById(R.id.tv_total_charge);

        setupListener();

        debugModeInit(savedInstanceState);
        mPaymentsClient = PaymentsUtil.createPaymentsClient(this);

        if(TollRoadsApp.getInstance().paySelectedViolationsRequest.
                getPayment_type() == Constants.GOOGLE_PAY_TYPE)
        {
            requestPayment();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LOAD_PAYMENT_DATA_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        handlePaymentSuccess(paymentData);
                        break;
                    case Activity.RESULT_CANCELED:
                        // Nothing to here normally - the user simply cancelled without selecting a
                        // payment method.
                        finish();
                        break;
                    case AutoResolveHelper.RESULT_ERROR:
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        if(status != null) {
                            handleError(status.getStatusCode());
                        }
                        finish();
                        break;
                }

                break;
        }
    }

    private void handlePaymentSuccess(PaymentData paymentData) {
        // PaymentMethodToken contains the payment information, as well as any additional
        // requested information, such as billing and shipping address.
        //
        // Refer to your processor's documentation on how to proceed from here.
        PaymentMethodToken token = paymentData.getPaymentMethodToken();

        // getPaymentMethodToken will only return null if PaymentMethodTokenizationParameters was
        // not set in the PaymentRequest.
        if (token != null) {
            String billingName = paymentData.getCardInfo().getBillingAddress().getName();
            Toast.makeText(this, getString(R.string.payments_show_name, billingName), Toast.LENGTH_LONG).show();

            // Use token.getToken() to get the token string.
            Log.d("PaymentData", "PaymentMethodToken received");

            PaySelectedViolationsRequest paySelectedViolationsRequest = TollRoadsApp.getInstance().paySelectedViolationsRequest;
            paySelectedViolationsRequest.setPayment_token(token.getToken());
            showProgressDialog();

            String params = populateParams();

            params = params +"&"+ServerDelegate.getCommonUrlExtra();
            params = params +"&"+Resource.KEY_ACTION+"="+ Resource.ACTION_CHECK_PAY;

            checkPayment(Resource.URL_VIOLATION_PAYMENT, params);
        }
        else
        {
            Toast.makeText(this, getString(R.string.pwg_status_error), Toast.LENGTH_LONG).show();
        }
    }

    private void handleError(int statusCode) {
        // At this stage, the user has already seen a popup informing them an error occurred.
        // Normally, only logging is required.
        // statusCode will hold the value of any constant from CommonStatusCode or one of the
        // WalletConstants.ERROR_CODE_* constants.
        Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode));
    }

    // This method is called when the Pay with Google button is clicked.
    public void requestPayment() {
        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.

        String amount = TollRoadsApp.getInstance().selectedUnpaidViolationsResponse.
                getSel_unpaid_vios_total_amount_due().replace("$","");
        TransactionInfo transaction = PaymentsUtil.createTransaction(amount);
        PaymentDataRequest request = PaymentsUtil.createPaymentDataRequest(transaction);
        Task<PaymentData> futurePaymentData = mPaymentsClient.loadPaymentData(request);

        // Since loadPaymentData may show the UI asking the user to select a payment method, we use
        // AutoResolveHelper to wait for the user interacting with it. Once completed,
        // onActivityResult will be called with the result.
        AutoResolveHelper.resolveTask(futurePaymentData, this, LOAD_PAYMENT_DATA_REQUEST_CODE);
    }

    private void initPaymentMethod()
    {
        if(TollRoadsApp.getInstance().paySelectedViolationsRequest != null) {
            PaySelectedViolationsRequest paySelectedViolationsRequest = TollRoadsApp.getInstance().paySelectedViolationsRequest;

            if(paySelectedViolationsRequest.getPayment_type() == Constants.CREDIT_CARD_TYPE) {
                llCreditCard.setVisibility(View.VISIBLE);
                llEcheck.setVisibility(View.GONE);
            }
            else if(paySelectedViolationsRequest.getPayment_type() == Constants.ELECTRONIC_CHECK_TYPE) {
                llCreditCard.setVisibility(View.GONE);
                llEcheck.setVisibility(View.VISIBLE);
            }
        }
    }

    private void debugModeInit(Bundle savedInstanceState)
    {
        if(savedInstanceState == null && BuildConfig.DEBUG
                && TollRoadsApp.getInstance().paySelectedViolationsRequest != null)
        {
            PaySelectedViolationsRequest paySelectedViolationsRequest = TollRoadsApp.getInstance().paySelectedViolationsRequest;

            if(paySelectedViolationsRequest.getPayment_type() == Constants.CREDIT_CARD_TYPE) {
                etCardNo.setText("5111111111111191");
                etNameOnCard.setText("John");
                etExpirationDate.setText("0619");
                etZipCode.setText("96216");
                etCVV.setText("616");
            }
            else if(paySelectedViolationsRequest.getPayment_type() == Constants.ELECTRONIC_CHECK_TYPE) {
                etRoutingNo.setText("123456789");
                etFirstName.setText("John");
                etLastName.setText("Joe");
                etAccountNo.setText("123456789012");
            }
        }

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
//                gotoActivity(v.getContext(), ViolationSelectPaymentActivity.class,
//                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                finish();
            }
        });

        tvGoNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation()) {
                    setRequest();

                    showProgressDialog();

                    String params = populateParams();

                    params = params +"&"+ServerDelegate.getCommonUrlExtra();
                    params = params +"&"+Resource.KEY_ACTION+"="+ Resource.ACTION_CHECK_PAY;

                    checkPayment(Resource.URL_VIOLATION_PAYMENT, params);
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
//            gotoActivity(getApplicationContext(), ViolationSelectPaymentActivity.class,
//                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            finish();
            return  true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private String populateParams()
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

    private void checkPayment(String url,String params)
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
                            if(TollRoadsApp.getInstance().paySelectedViolationsRequest.
                                    getPayment_type() == Constants.GOOGLE_PAY_TYPE)
                            {
                                finish();
                            }

                            gotoActivity(ViolationPaymentInfoActivity.this, ViolationSummaryActivity.class,
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

    private String formatDate(String dateString)
    {
        String ret = "";
        if(dateString != null && !dateString.isEmpty())
        {
            if(dateString.length() <2) {
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
        if(TollRoadsApp.getInstance().paySelectedViolationsRequest != null) {
            PaySelectedViolationsRequest paySelectedViolationsRequest = TollRoadsApp.getInstance().paySelectedViolationsRequest;

            if (paySelectedViolationsRequest.getPayment_type() == Constants.CREDIT_CARD_TYPE) {
                paySelectedViolationsRequest.setCard_number(etCardNo.getText().toString());
                paySelectedViolationsRequest.setCard_holder_name(etNameOnCard.getText().toString());
                paySelectedViolationsRequest.setExpired_date(formatDate(etExpirationDate.getText().toString()));
                paySelectedViolationsRequest.setZip_code(etZipCode.getText().toString());
                paySelectedViolationsRequest.setCvv2(Integer.parseInt(etCVV.getText().toString()));
            } else if (paySelectedViolationsRequest.getPayment_type() == Constants.ELECTRONIC_CHECK_TYPE) {
                paySelectedViolationsRequest.setRouting_number(etRoutingNo.getText().toString());
                paySelectedViolationsRequest.setAccount_number(etAccountNo.getText().toString());
                paySelectedViolationsRequest.setFirst_name(etFirstName.getText().toString());
                paySelectedViolationsRequest.setLast_name(etLastName.getText().toString());
            }
        }
    }
    
    private boolean checkValidation()
    {
        boolean ret = true;
        if(TollRoadsApp.getInstance().paySelectedViolationsRequest != null) {
            PaySelectedViolationsRequest paySelectedViolationsRequest = TollRoadsApp.getInstance().paySelectedViolationsRequest;

            if (paySelectedViolationsRequest.getPayment_type() == Constants.CREDIT_CARD_TYPE) {
                if (etCardNo.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.card_no_empty_warning));
                } else if (etCVV.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.cvv_empty_warning));
                } else if (etNameOnCard.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.name_on_card_empty_warning));
                } else if (etExpirationDate.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.expiration_date_empty_warning));
                }
                //        else if (etZipCode.getText().length() == 0) {
                //            ret = false;
                //            showToastMessage(getString(R.string.billing_zip_code_empty_warning));
                //        }
            }
            else if (paySelectedViolationsRequest.getPayment_type() == Constants.ELECTRONIC_CHECK_TYPE) {
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
        }
        return ret;
    }
    
    private void initWidgetValue()
    {
        initPaymentMethod();
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
        initWidgetValue();

    }

    @Override
    public void onPause() {

        super.onPause();
    }

}
