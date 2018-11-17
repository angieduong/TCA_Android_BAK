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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.google.gson.reflect.TypeToken;
import com.seta.tollroaddroid.app.GooglePay.PaymentsUtil;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.json.PaymentMethod;
import com.seta.tollroaddroid.app.json.PaymentMethodUpdateRequest;
import com.seta.tollroaddroid.app.utilities.Constants;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MakePaymentNewActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private List<PaymentMethod> paymentMethods = new ArrayList<PaymentMethod>();
    private List<PaymentMethod> subPaymentMethods = new ArrayList<PaymentMethod>();
    private Response.ErrorListener errorListener;
    private Spinner payMethodSpinner;
    private TextView tvPayNow;
    private EditText etCVV;
    private LinearLayout llCVV;
    private TextView tvAmount, tvPaymentType;

    private LinearLayout llCreditCard, llECheck;
    private EditText etCardNo, etNameOnCard, etExpirationDate, etZipCode;
    private EditText etRoutingNo, etFirstName, etLastName, etAccountNo;

    private String amount;
    private int payType = Constants.CREDIT_CARD_TYPE;

    private PaymentMethodUpdateRequest paymentMethodUpdateRequest = new PaymentMethodUpdateRequest();

    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;

    private PaymentsClient mPaymentsClient;
    private String paymentToken;
    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit Account_Payment_Make page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_payment_new);
        FlurryAgent.logEvent("Enter Account_Payment_Make page.");

        Intent intent = getIntent();
        if(intent != null)
        {
            amount = intent.getStringExtra(Resource.KEY_AMOUNT);
            payType = intent.getIntExtra(Resource.KEY_PAYMENT_TYPE, Constants.CREDIT_CARD_TYPE);
        }

        if(amount == null)
        {
            amount = "0.0";
        }

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        payMethodSpinner = (Spinner)findViewById(R.id.pay_method_spinner);
        tvPayNow = (TextView)findViewById(R.id.tv_pay_now);
        tvPaymentType = (TextView)findViewById(R.id.tv_payment_type);

        etCVV = (EditText)findViewById(R.id.et_cvv);
        llCVV = (LinearLayout)findViewById(R.id.ll_cvv);

        tvAmount = (TextView)findViewById(R.id.tv_amount);

        llCreditCard = (LinearLayout)findViewById(R.id.ll_credit_card);
        llECheck = (LinearLayout)findViewById(R.id.ll_echeck);

        etCardNo = (EditText)findViewById(R.id.et_card_no);
        etNameOnCard = (EditText)findViewById(R.id.et_name_on_card);
        etExpirationDate = (EditText)findViewById(R.id.et_expiration_date);
        etZipCode = (EditText)findViewById(R.id.et_billing_zip_code);

        etRoutingNo = (EditText)findViewById(R.id.et_routing_no);
        etFirstName = (EditText)findViewById(R.id.et_first_name);
        etLastName = (EditText)findViewById(R.id.et_last_name);
        etAccountNo = (EditText)findViewById(R.id.et_account_no);

        initWidgetValue();
        setupListener();

        mPaymentsClient = PaymentsUtil.createPaymentsClient(this);

        if(payType == Constants.GOOGLE_PAY_TYPE)
        {
            requestPayment();
        }
        else
        {
            if (payType == Constants.CREDIT_CARD_TYPE) {
                llCVV.setVisibility(View.VISIBLE);
                tvPaymentType.setText(getString(R.string.credit_card));
            } else {
                llCVV.setVisibility(View.GONE);
                tvPaymentType.setText(getString(R.string.electronic_check));
            }
            getPaymentMethods();
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

            paymentToken = token.getToken();
            makePayment();
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

        TransactionInfo transaction = PaymentsUtil.createTransaction(amount);
        PaymentDataRequest request = PaymentsUtil.createPaymentDataRequest(transaction);
        Task<PaymentData> futurePaymentData = mPaymentsClient.loadPaymentData(request);

        // Since loadPaymentData may show the UI asking the user to select a payment method, we use
        // AutoResolveHelper to wait for the user interacting with it. Once completed,
        // onActivityResult will be called with the result.
        AutoResolveHelper.resolveTask(futurePaymentData, this, LOAD_PAYMENT_DATA_REQUEST_CODE);
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
                if (position == subPaymentMethods.size()) {
//                    Bundle bundle = new Bundle();
//                    bundle.putBoolean(Resource.KEY_ONE_TIME_PAYMENT, true);
//                    gotoActivityForResult(view.getContext(), NewPaymentMethodActivity.class,
//                            bundle, Constants.ONE_TIME_PAYMENT);
                    if(payType == Constants.CREDIT_CARD_TYPE)
                    {
                        llCreditCard.setVisibility(View.VISIBLE);
                        llECheck.setVisibility(View.GONE);
                    }
                    else
                    {
                        llCreditCard.setVisibility(View.GONE);
                        llECheck.setVisibility(View.VISIBLE);
                    }
                } else {
                    llCreditCard.setVisibility(View.GONE);
                    llECheck.setVisibility(View.GONE);
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

    private void initWidgetValue()
    {
        if(amount != null)
        {
            tvAmount.setText(getString(R.string.total_amount, amount));
        }
    }

    private boolean checkPaymentDetailValidation()
    {
        boolean ret = true;
        if(payMethodSpinner.getSelectedItemPosition() == subPaymentMethods.size()) {
            if (payType == Constants.CREDIT_CARD_TYPE) {
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
            } else {
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

    private boolean checkValidation()
    {
        if(payType == Constants.GOOGLE_PAY_TYPE)
        {
            return true;
        }
        else
        {
            boolean ret;
            if (llCVV.getVisibility() == View.VISIBLE && etCVV.getText().length() == 0) {
                ret = false;
                showToastMessage(getString(R.string.cvv_empty_warning));
            } else {
                ret = checkPaymentDetailValidation();
            }
            return ret;
        }
    }

    private void populatePaymentSpinner()
    {
        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();

        subPaymentMethods.clear();
        for(int i =0; i< paymentMethods.size(); i++)
        {
            PaymentMethod paymentMethod = paymentMethods.get(i);

            if(paymentMethod.getPayment_type() == payType) {
                if (paymentMethod.getPayment_type() == Constants.CREDIT_CARD_TYPE) {
                    categories.add(paymentMethod.getCard_number());
                } else {
                    categories.add(paymentMethod.getAccount_number());
                }

                subPaymentMethods.add(paymentMethod);
            }
        }

        categories.add(getString(R.string.other));
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String> (this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        payMethodSpinner.setAdapter(dataAdapter);

        if(subPaymentMethods.size() == 0)
        {
            //need to show extra info
            if(payType == Constants.CREDIT_CARD_TYPE)
            {
                llCreditCard.setVisibility(View.VISIBLE);
                llECheck.setVisibility(View.GONE);
            }
            else
            {
                llCreditCard.setVisibility(View.GONE);
                llECheck.setVisibility(View.VISIBLE);
            }
        }

    }

    private void getPaymentMethodsExceptionHandle()
    {
        paymentMethods.clear();
        populatePaymentSpinner();
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
                        else
                        {
                            getPaymentMethodsExceptionHandle();
                        }
                    }
                    else
                    {
                        showToastMessage(getString(R.string.network_error_retry));
                        getPaymentMethodsExceptionHandle();
                    }
                }
                catch (Exception e)
                {
                    showToastMessage(getString(R.string.network_error_retry));
                    getPaymentMethodsExceptionHandle();
                }
            }
        };
        showProgressDialog();
        ServerDelegate.getPaymentMethods(Resource.URL_PAYMENT, listener, errorListener);
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
        paymentMethodUpdateRequest.setReplenishment_amt("0.00");

        if(payType == Constants.CREDIT_CARD_TYPE)
        {
            paymentMethodUpdateRequest.setPayment_type(Constants.CREDIT_CARD_TYPE);
            paymentMethodUpdateRequest.setCard_number(etCardNo.getText().toString());
            paymentMethodUpdateRequest.setCard_holder_name(etNameOnCard.getText().toString());
            paymentMethodUpdateRequest.setExpired_date(formatDate(etExpirationDate.getText().toString()));
            paymentMethodUpdateRequest.setZip_code(etZipCode.getText().toString());
            //paymentMethodUpdateRequest.setCvv2(etCVV.getText().toString());
        }
        else if(payType == Constants.ELECTRONIC_CHECK_TYPE)
        {
            paymentMethodUpdateRequest.setPayment_type(Constants.ELECTRONIC_CHECK_TYPE);
            paymentMethodUpdateRequest.setRouting_number(etRoutingNo.getText().toString());
            paymentMethodUpdateRequest.setAccount_number(etAccountNo.getText().toString());
            paymentMethodUpdateRequest.setFirst_name(etFirstName.getText().toString());
            paymentMethodUpdateRequest.setLast_name(etLastName.getText().toString());
        }
        else
        {
            paymentMethodUpdateRequest.setPayment_type(Constants.GOOGLE_PAY_TYPE);
            paymentMethodUpdateRequest.setPayment_token(paymentToken);
        }
        paymentMethodUpdateRequest.setPayment_order(0);
        paymentMethodUpdateRequest.setPaymethod_id("");

        if(llCVV.getVisibility() == View.VISIBLE)
        {
            paymentMethodUpdateRequest.setCvv2(etCVV.getText().toString());
        }
        paymentMethodUpdateRequest.setAmount(amount);
    }

    private String populateOthersParams()
    {
        String params = "";

        setRequest();
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

    private String populateParams()
    {
        String params = "cvv=" + etCVV.getText().toString();
        if(payType != Constants.GOOGLE_PAY_TYPE && payMethodSpinner.getSelectedItemPosition() < subPaymentMethods.size()) {
            params = params + "&paymethod_id=" +
                    subPaymentMethods.get(payMethodSpinner.getSelectedItemPosition()).getPaymethod_id();
            params = params+ "&amount=" + amount;// etAmount.getText().toString();
        }
        else
        {
            //others
            params = populateOthersParams();
        }

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
        if(payType != Constants.GOOGLE_PAY_TYPE && payMethodSpinner.getSelectedItemPosition() < subPaymentMethods.size()) {
            ServerDelegate.makePaymentRequest(Resource.URL_PAYMENT, params, listener, errorListener);
        }
        else
        {
            //others
            ServerDelegate.oneTimePaymentRequest(Resource.URL_PAYMENT, params, listener, errorListener);
        }

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
