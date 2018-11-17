package com.seta.tollroaddroid.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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
import com.seta.tollroaddroid.app.json.OTTUserInfoRequest;
import com.seta.tollroaddroid.app.json.SignUpRequest;
import com.seta.tollroaddroid.app.utilities.Constants;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;

public class SignUpPaymentInfoActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private LinearLayout llTransponders;
    private TextView tvAccountType, tvAccountDescription;
    private TextView tvDone;
    private LinearLayout llPayment1,llPayment2,llPayment3;
    private LinearLayout llAddAnotherPayment;
    private Spinner typeSpinner,numberSpinner;
    private int paymentCount = 1;
    private SignUpRequest signUpRequest = TollRoadsApp.getInstance().gSignUpRequest;

    //payment 1
    private LinearLayout llReplenishment1;
    private LinearLayout llPaymentMethod1,llCVV1;
    
    private LinearLayout llCreditCard1, llECheck1;
    private Spinner spPayMethod1,spPaymentOrder1,spReplenishmentAmount1;
    private EditText etCardNo1, etNameOnCard1, etExpirationDate1, etZipCode1,etCVV1;
    private EditText etRoutingNo1, etFirstName1, etLastName1, etAccountNo1;

    //payment 2
    private LinearLayout llReplenishment2;
    private LinearLayout llPaymentMethod2,llCVV2;

    private LinearLayout llCreditCard2, llECheck2;
    private Spinner spPayMethod2,spPaymentOrder2,spReplenishmentAmount2;
    private EditText etCardNo2, etNameOnCard2, etExpirationDate2, etZipCode2,etCVV2;
    private EditText etRoutingNo2, etFirstName2, etLastName2, etAccountNo2;

    //payment 3
    private LinearLayout llReplenishment3;
    private LinearLayout llPaymentMethod3,llCVV3;

    private LinearLayout llCreditCard3, llECheck3;
    private Spinner spPayMethod3,spPaymentOrder3,spReplenishmentAmount3;
    private EditText etCardNo3, etNameOnCard3, etExpirationDate3, etZipCode3,etCVV3;
    private EditText etRoutingNo3, etFirstName3, etLastName3, etAccountNo3;
    private ImageView ivRemove1, ivRemove2, ivRemove3;
    private TextView tvPaymentDescription;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("CurrentSignUpReq", TollRoadsApp.getInstance().gSignUpRequest);
        outState.putBoolean(Constants.FROM_OTT, TollRoadsApp.getInstance().gFromOTT);
    }

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit SignUp_4_Payment_"+TollRoadsApp.getInstance().getSignUpAccountType()+" page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_payment_info);
        if(savedInstanceState != null)
        {
            TollRoadsApp.getInstance().gSignUpRequest =
                    (SignUpRequest) savedInstanceState.getSerializable("CurrentSignUpReq");
            TollRoadsApp.getInstance().gFromOTT =
                    savedInstanceState.getBoolean(Constants.FROM_OTT,false);
        }
        FlurryAgent.logEvent("Enter SignUp_4_Payment_"+TollRoadsApp.getInstance().getSignUpAccountType()+" page.");

        signUpRequest = TollRoadsApp.getInstance().gSignUpRequest;

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvAccountType = (TextView)findViewById(R.id.tv_account_type);
        tvAccountDescription = (TextView)findViewById(R.id.tv_account_description);
        tvDone = (TextView)findViewById(R.id.tv_done);
        llAddAnotherPayment = (LinearLayout)findViewById(R.id.ll_add_another_payment_method);
        llTransponders = (LinearLayout)findViewById(R.id.ll_transponders);

        typeSpinner = (Spinner)findViewById(R.id.type_spinner);
        numberSpinner = (Spinner)findViewById(R.id.number_spinner);

        llPayment1 = (LinearLayout)findViewById(R.id.ll_payment1);
        llPayment2 = (LinearLayout)findViewById(R.id.ll_payment2);
        llPayment3 = (LinearLayout)findViewById(R.id.ll_payment3);

        tvPaymentDescription = (TextView)findViewById(R.id.tv_payment_description);
        initPayment1();
        initPayment2();
        initPayment3();

        setupListener();
    }

    private void setupListener()
    {
        llAddAnotherPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(paymentCount < 3) {
                    if (llPayment2.getVisibility() != View.VISIBLE) {
                        llPayment2.setVisibility(View.VISIBLE);
                    } else if (llPayment3.getVisibility() != View.VISIBLE) {
                        llPayment3.setVisibility(View.VISIBLE);
                        llAddAnotherPayment.setVisibility(View.GONE);
                    }
                    paymentCount++;
                }
                else
                {
                    llAddAnotherPayment.setVisibility(View.GONE);
                }
                ivRemove1.setVisibility(View.VISIBLE);
                ivRemove2.setVisibility(View.VISIBLE);
                ivRemove3.setVisibility(View.VISIBLE);
            }
        });
        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(v.getContext(), SignUpVehicleInfoActivity.class, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
        });
        
        tvDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation()) {
                    setRequest();
                    checkPayTagRequest();
                }
            }
        });
    }

    private String populateParams()
    {
        String params = "";

        if(TollRoadsApp.getInstance().gSignUpRequest != null) {
            Gson gson = new GsonBuilder().serializeNulls().create();
            String jsonStr = gson.toJson(TollRoadsApp.getInstance().gSignUpRequest,
                    SignUpRequest.class);
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

    private void checkPayTagRequest()
    {
        String params = populateParams();

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
                            gotoActivity(SignUpPaymentInfoActivity.this, SignUpCustomerAgreementActivity.class,
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

        ServerDelegate.checkPayTag(params, listener, errorListener);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            gotoActivity(this, SignUpVehicleInfoActivity.class, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setPayment1ToRequest()
    {
        if(llReplenishment1.getVisibility() == View.VISIBLE)
        {
            if(spReplenishmentAmount1.getSelectedItemPosition() == 0) {
                signUpRequest.setReplenishment_amt("30.00");
            }
            else if(spReplenishmentAmount1.getSelectedItemPosition() == 1) {
                signUpRequest.setReplenishment_amt("60.00");
            }
            else
            {
                signUpRequest.setReplenishment_amt("100.00");
            }
        }
        else
        {
            signUpRequest.setReplenishment_amt(null);
        }

        if(spPayMethod1.getSelectedItemPosition() == 0)
        {
            if(etCardNo1.getText().toString().isEmpty()
                &&TollRoadsApp.getInstance().selectedAccountType == Constants.ACCOUNT_TYPE_INVOICE_EXPRESS)
            {
                signUpRequest.setPayment_method(null);
            }
            else {
                signUpRequest.setPayment_method(Constants.CREDIT_CARD_TYPE);
                signUpRequest.setCard_number(etCardNo1.getText().toString());
                signUpRequest.setCard_holder_name(etNameOnCard1.getText().toString());
                signUpRequest.setExpired_date(formatDate(etExpirationDate1.getText().toString()));
                signUpRequest.setZip_code(etZipCode1.getText().toString());
                if (llCVV1.getVisibility() == View.VISIBLE) {
                    signUpRequest.setCvv2(etCVV1.getText().toString());
                }
            }
        }
        else
        {
            if(etRoutingNo1.getText().toString().isEmpty()
                &&TollRoadsApp.getInstance().selectedAccountType == Constants.ACCOUNT_TYPE_INVOICE_EXPRESS)
            {
                signUpRequest.setPayment_method(null);
            }
            else {
                signUpRequest.setPayment_method(Constants.ELECTRONIC_CHECK_TYPE);
                signUpRequest.setRouting_number(etRoutingNo1.getText().toString());
                signUpRequest.setAccount_number(etAccountNo1.getText().toString());
                signUpRequest.setFirst_name(etFirstName1.getText().toString());
                signUpRequest.setLast_name(etLastName1.getText().toString());
            }
        }
    }

    private void setPayment2ToRequest()
    {
        if(spPayMethod2.getSelectedItemPosition() == 0)
        {
            if(etCardNo2.getText().toString().isEmpty()
                    &&TollRoadsApp.getInstance().selectedAccountType == Constants.ACCOUNT_TYPE_INVOICE_EXPRESS)
            {
                signUpRequest.setPayment_method2(null);
            }
            else {
                signUpRequest.setPayment_method2(Constants.CREDIT_CARD_TYPE);
                signUpRequest.setCard_number2(etCardNo2.getText().toString());
                signUpRequest.setCard_holder_name2(etNameOnCard2.getText().toString());
                signUpRequest.setExpired_date2(formatDate(etExpirationDate2.getText().toString()));
                signUpRequest.setZip_code2(etZipCode2.getText().toString());
                if (llCVV2.getVisibility() == View.VISIBLE) {
                    signUpRequest.setCvv22(etCVV2.getText().toString());
                }
            }
        }
        else
        {
            if(etRoutingNo2.getText().toString().isEmpty()
                    &&TollRoadsApp.getInstance().selectedAccountType == Constants.ACCOUNT_TYPE_INVOICE_EXPRESS)
            {
                signUpRequest.setPayment_method2(null);
            }
            else {
                signUpRequest.setPayment_method2(Constants.ELECTRONIC_CHECK_TYPE);
                signUpRequest.setRouting_number2(etRoutingNo2.getText().toString());
                signUpRequest.setAccount_number2(etAccountNo2.getText().toString());
                signUpRequest.setFirst_name2(etFirstName2.getText().toString());
                signUpRequest.setLast_name2(etLastName2.getText().toString());
            }
        }
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

    private void setPayment3ToRequest()
    {
        if(spPayMethod3.getSelectedItemPosition() == 0)
        {
            if(etCardNo3.getText().toString().isEmpty()
                    &&TollRoadsApp.getInstance().selectedAccountType == Constants.ACCOUNT_TYPE_INVOICE_EXPRESS)
            {
                signUpRequest.setPayment_method3(null);
            }
            else {
                signUpRequest.setPayment_method3(Constants.CREDIT_CARD_TYPE);
                signUpRequest.setCard_number3(etCardNo3.getText().toString());
                signUpRequest.setCard_holder_name3(etNameOnCard3.getText().toString());
                signUpRequest.setExpired_date3(formatDate(etExpirationDate3.getText().toString()));
                signUpRequest.setZip_code3(etZipCode3.getText().toString());
                if (llCVV3.getVisibility() == View.VISIBLE) {
                    signUpRequest.setCvv23(etCVV3.getText().toString());
                }
            }
        }
        else
        {
            if(etRoutingNo3.getText().toString().isEmpty()
                    &&TollRoadsApp.getInstance().selectedAccountType == Constants.ACCOUNT_TYPE_INVOICE_EXPRESS)
            {
                signUpRequest.setPayment_method3(null);
            }
            else {
                signUpRequest.setPayment_method3(Constants.ELECTRONIC_CHECK_TYPE);
                signUpRequest.setRouting_number3(etRoutingNo3.getText().toString());
                signUpRequest.setAccount_number3(etAccountNo3.getText().toString());
                signUpRequest.setFirst_name3(etFirstName3.getText().toString());
                signUpRequest.setLast_name3(etLastName3.getText().toString());
            }
        }
    }

    private void clearRequest()
    {
        //payment 1
        signUpRequest.setPayment_method(null);
        signUpRequest.setCard_number("");
        signUpRequest.setCard_holder_name("");
        signUpRequest.setExpired_date("");
        signUpRequest.setZip_code("");
        signUpRequest.setCvv2("");

        signUpRequest.setRouting_number("");
        signUpRequest.setAccount_number("");
        signUpRequest.setFirst_name("");
        signUpRequest.setLast_name("");
        
        //payment 2
        signUpRequest.setPayment_method2(null);
        signUpRequest.setCard_number2("");
        signUpRequest.setCard_holder_name2("");
        signUpRequest.setExpired_date2("");
        signUpRequest.setZip_code2("");
        signUpRequest.setCvv22("");
    
        signUpRequest.setRouting_number2("");
        signUpRequest.setAccount_number2("");
        signUpRequest.setFirst_name2("");
        signUpRequest.setLast_name2("");

        //payment 3
        signUpRequest.setPayment_method3(null);
        signUpRequest.setCard_number3("");
        signUpRequest.setCard_holder_name3("");
        signUpRequest.setExpired_date3("");
        signUpRequest.setZip_code3("");
        signUpRequest.setCvv23("");

        signUpRequest.setRouting_number3("");
        signUpRequest.setAccount_number3("");
        signUpRequest.setFirst_name3("");
        signUpRequest.setLast_name3("");
    }
    
    private void setRequest()
    {
        clearRequest();
        
        if(llTransponders.getVisibility() == View.VISIBLE)
        {
            int type;
            if(typeSpinner.getSelectedItemPosition() < 2)
            {
                type = typeSpinner.getSelectedItemPosition();
            }
            else
            {
                type = 3;
            }
            signUpRequest.setTag_type(type);

            signUpRequest.setTag_request(numberSpinner.getSelectedItemPosition()+1);
        }
        else
        {
            signUpRequest.setTag_type(null);
            signUpRequest.setTag_request(null);
        }

        if(paymentCount >= 1)
        {
            setPayment1ToRequest();
        }

        if(paymentCount >= 2)
        {
            setPayment2ToRequest();
        }

        if(paymentCount >= 3)
        {
            setPayment3ToRequest();
        }
    }

    private boolean checkPayment1()
    {
        boolean ret = true;
        if(TollRoadsApp.getInstance().selectedAccountType != Constants.ACCOUNT_TYPE_INVOICE_EXPRESS) {
            if(spPayMethod1.getSelectedItemPosition() == 0) {
                if (etCardNo1.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.card_no_empty_warning));
                } else if (llCVV1.getVisibility() == View.VISIBLE
                        && etCVV1.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.cvv_empty_warning));
                } else if (etNameOnCard1.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.name_on_card_empty_warning));
                } else if (etExpirationDate1.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.expiration_date_empty_warning));
                }
//                else if (etZipCode1.getText().length() == 0) {
//                    ret = false;
//                    showToastMessage(getString(R.string.billing_zip_code_empty_warning));
//                }
            }
            else
            {
                if (etRoutingNo1.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.routing_no_empty_warning));
                } else if (etAccountNo1.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.account_no_empty_warning));
                } else if (etFirstName1.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.first_name_empty_warning));
                } else if (etLastName1.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.last_name_empty_warning));
                }
            }
        }
        return ret;
    }

    private boolean checkPayment2()
    {
        boolean ret = true;
        if(TollRoadsApp.getInstance().selectedAccountType != Constants.ACCOUNT_TYPE_INVOICE_EXPRESS) {
            if(spPayMethod2.getSelectedItemPosition() == 0) {
                if (etCardNo2.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.card_no_empty_warning));
                } else if (llCVV2.getVisibility() == View.VISIBLE
                        && etCVV2.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.cvv_empty_warning));
                } else if (etNameOnCard2.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.name_on_card_empty_warning));
                } else if (etExpirationDate2.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.expiration_date_empty_warning));
                }
//                else if (etZipCode2.getText().length() == 0) {
//                    ret = false;
//                    showToastMessage(getString(R.string.billing_zip_code_empty_warning));
//                }
            }
            else
            {
                if (etRoutingNo2.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.routing_no_empty_warning));
                } else if (etAccountNo2.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.account_no_empty_warning));
                } else if (etFirstName2.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.first_name_empty_warning));
                } else if (etLastName2.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.last_name_empty_warning));
                }
            }
        }
        return ret;
    }
    private boolean checkPayment3()
    {
        boolean ret = true;
        if(TollRoadsApp.getInstance().selectedAccountType != Constants.ACCOUNT_TYPE_INVOICE_EXPRESS) {
            if(spPayMethod3.getSelectedItemPosition() == 0) {
                if (etCardNo3.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.card_no_empty_warning));
                } else if (llCVV3.getVisibility() == View.VISIBLE
                        && etCVV3.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.cvv_empty_warning));
                } else if (etNameOnCard3.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.name_on_card_empty_warning));
                } else if (etExpirationDate3.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.expiration_date_empty_warning));
                }
//                else if (etZipCode3.getText().length() == 0) {
//                    ret = false;
//                    showToastMessage(getString(R.string.billing_zip_code_empty_warning));
//                }
            }
            else
            {
                if (etRoutingNo3.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.routing_no_empty_warning));
                } else if (etAccountNo3.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.account_no_empty_warning));
                } else if (etFirstName3.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.first_name_empty_warning));
                } else if (etLastName3.getText().length() == 0) {
                    ret = false;
                    showToastMessage(getString(R.string.last_name_empty_warning));
                }
            }
        }
        return ret;
    }
    private boolean checkValidation()
    {
        boolean ret = true;
        if(paymentCount >= 1)
        {
            if(!checkPayment1())
            {
                return false;
            }
        }
        if(paymentCount >= 2)
        {
            if(!checkPayment2())
            {
                return false;
            }
        }
        if(paymentCount >= 3)
        {
            if(!checkPayment3())
            {
                return false;
            }
        }
        return ret;
    }
    
    private void initPayment1()
    {
        TextView tvPaymentIndex = (TextView)llPayment1.findViewById(R.id.tv_payment_index);
        tvPaymentIndex.setText(getString(R.string.payment_index,1));

        llCreditCard1 = (LinearLayout)llPayment1.findViewById(R.id.ll_credit_card);
        llECheck1 = (LinearLayout)llPayment1.findViewById(R.id.ll_echeck);
        spPayMethod1 = (Spinner)llPayment1.findViewById(R.id.pay_method_spinner);
        spPaymentOrder1 = (Spinner)llPayment1.findViewById(R.id.payment_order_spinner);
        spReplenishmentAmount1 = (Spinner)llPayment1.findViewById(R.id.replenishment_amount_spinner);

        llCVV1 = (LinearLayout)llPayment1.findViewById(R.id.ll_cvv);
        llPaymentMethod1 = (LinearLayout)llPayment1.findViewById(R.id.ll_pay_method);
        llReplenishment1 = (LinearLayout)llPayment1.findViewById(R.id.ll_replenishment);

        etCardNo1 = (EditText)llPayment1.findViewById(R.id.et_card_no);
        etNameOnCard1 = (EditText)llPayment1.findViewById(R.id.et_name_on_card);
        etExpirationDate1 = (EditText)llPayment1.findViewById(R.id.et_expiration_date);
        etZipCode1 = (EditText)llPayment1.findViewById(R.id.et_billing_zip_code);
        etCVV1 = (EditText)llPayment1.findViewById(R.id.et_cvv);

        etRoutingNo1 = (EditText)llPayment1.findViewById(R.id.et_routing_no);
        etFirstName1 = (EditText)llPayment1.findViewById(R.id.et_first_name);
        etLastName1 = (EditText)llPayment1.findViewById(R.id.et_last_name);
        etAccountNo1 = (EditText)llPayment1.findViewById(R.id.et_account_no);
        
        spPayMethod1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    llCreditCard1.setVisibility(View.VISIBLE);
                    llECheck1.setVisibility(View.GONE);
                } else {
                    llCreditCard1.setVisibility(View.GONE);
                    llECheck1.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ivRemove1 = (ImageView)llPayment1.findViewById(R.id.iv_remove);
        ivRemove1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paymentCount > 1) {
                    if (llPayment2.getVisibility() == View.VISIBLE) {
                        copyLayout(llPayment2, llPayment1);
                    }
                    if (llPayment3.getVisibility() == View.VISIBLE) {
                        copyLayout(llPayment3, llPayment2);
                    }
                    
                    hideLastLayout();
                    paymentCount--;
                }
                if (paymentCount <= 1) {
                    hideRemoveImage();
                }
                llAddAnotherPayment.setVisibility(View.VISIBLE);
            }
        });

        if(TollRoadsApp.getInstance().gFromOTT)
        {
            OTTUserInfoRequest ottUserInfoRequest = TollRoadsApp.getInstance().gOTTUserInfoRequest;
            etCardNo1.setText(ottUserInfoRequest.getCard_number());
            etNameOnCard1.setText(ottUserInfoRequest.getCard_holder_name());
            etExpirationDate1.setText(ottUserInfoRequest.getExpired_date().replace("/",""));
            etZipCode1.setText(ottUserInfoRequest.getZip_code());
            etCVV1.setText(ottUserInfoRequest.getCvv2());
        }
    }

    private void copyLayout(LinearLayout src, LinearLayout dest)
    {
        LinearLayout llCreditCardSrc = (LinearLayout)src.findViewById(R.id.ll_credit_card);
        LinearLayout llECheckSrc = (LinearLayout)src.findViewById(R.id.ll_echeck);
        Spinner spPayMethodSrc = (Spinner)src.findViewById(R.id.pay_method_spinner);
        Spinner spPaymentOrderSrc = (Spinner)src.findViewById(R.id.payment_order_spinner);

        EditText etCardNoSrc = (EditText)src.findViewById(R.id.et_card_no);
        EditText etNameOnCardSrc = (EditText)src.findViewById(R.id.et_name_on_card);
        EditText etExpirationDateSrc = (EditText)src.findViewById(R.id.et_expiration_date);
        EditText etZipCodeSrc = (EditText)src.findViewById(R.id.et_billing_zip_code);
        EditText etCVVSrc = (EditText)src.findViewById(R.id.et_cvv);
        EditText etRoutingNoSrc = (EditText)src.findViewById(R.id.et_routing_no);
        EditText etFirstNameSrc = (EditText)src.findViewById(R.id.et_first_name);
        EditText etLastNameSrc = (EditText)src.findViewById(R.id.et_last_name);
        EditText etAccountNoSrc = (EditText)src.findViewById(R.id.et_account_no);

        LinearLayout llCreditCardDest = (LinearLayout)dest.findViewById(R.id.ll_credit_card);
        LinearLayout llECheckDest = (LinearLayout)dest.findViewById(R.id.ll_echeck);
        Spinner spPayMethodDest = (Spinner)dest.findViewById(R.id.pay_method_spinner);
        Spinner spPaymentOrderDest = (Spinner)dest.findViewById(R.id.payment_order_spinner);

        EditText etCardNoDest = (EditText)dest.findViewById(R.id.et_card_no);
        EditText etNameOnCardDest = (EditText)dest.findViewById(R.id.et_name_on_card);
        EditText etExpirationDateDest = (EditText)dest.findViewById(R.id.et_expiration_date);
        EditText etZipCodeDest = (EditText)dest.findViewById(R.id.et_billing_zip_code);
        EditText etCVVDest = (EditText)dest.findViewById(R.id.et_cvv);
        EditText etRoutingNoDest = (EditText)dest.findViewById(R.id.et_routing_no);
        EditText etFirstNameDest = (EditText)dest.findViewById(R.id.et_first_name);
        EditText etLastNameDest = (EditText)dest.findViewById(R.id.et_last_name);
        EditText etAccountNoDest = (EditText)dest.findViewById(R.id.et_account_no);

        llCreditCardDest.setVisibility(llCreditCardSrc.getVisibility());
        llECheckDest.setVisibility(llECheckSrc.getVisibility());
        spPayMethodDest.setSelection(spPayMethodSrc.getSelectedItemPosition());
        spPaymentOrderDest.setSelection(spPaymentOrderSrc.getSelectedItemPosition());
        
        etCardNoDest.setText(etCardNoSrc.getText());
        etNameOnCardDest.setText(etNameOnCardSrc.getText());
        etExpirationDateDest.setText(etExpirationDateSrc.getText());
        etZipCodeDest.setText(etZipCodeSrc.getText());
        etCVVDest.setText(etCVVSrc.getText());
        etRoutingNoDest.setText(etRoutingNoSrc.getText());
        etFirstNameDest.setText(etFirstNameSrc.getText());
        etLastNameDest.setText(etLastNameSrc.getText());
        etAccountNoDest.setText(etAccountNoSrc.getText());
    }

    private void hideLastLayout()
    {
        if(llPayment3.getVisibility() == View.VISIBLE)
        {
            reInitLayout(llPayment3);
            llPayment3.setVisibility(View.GONE);
        }
        else if(llPayment2.getVisibility() == View.VISIBLE)
        {
            reInitLayout(llPayment2);
            llPayment2.setVisibility(View.GONE);
        }
    }

    private void reInitLayout(LinearLayout linearLayout)
    {
        LinearLayout llCreditCardDest = (LinearLayout)linearLayout.findViewById(R.id.ll_credit_card);
        LinearLayout llECheckDest = (LinearLayout)linearLayout.findViewById(R.id.ll_echeck);
        Spinner spPayMethodDest = (Spinner)linearLayout.findViewById(R.id.pay_method_spinner);
        Spinner spPaymentOrderDest = (Spinner)linearLayout.findViewById(R.id.payment_order_spinner);

        EditText etCardNoDest = (EditText)linearLayout.findViewById(R.id.et_card_no);
        EditText etNameOnCardDest = (EditText)linearLayout.findViewById(R.id.et_name_on_card);
        EditText etExpirationDateDest = (EditText)linearLayout.findViewById(R.id.et_expiration_date);
        EditText etZipCodeDest = (EditText)linearLayout.findViewById(R.id.et_billing_zip_code);
        EditText etCVVDest = (EditText)linearLayout.findViewById(R.id.et_cvv);
        EditText etRoutingNoDest = (EditText)linearLayout.findViewById(R.id.et_routing_no);
        EditText etFirstNameDest = (EditText)linearLayout.findViewById(R.id.et_first_name);
        EditText etLastNameDest = (EditText)linearLayout.findViewById(R.id.et_last_name);
        EditText etAccountNoDest = (EditText)linearLayout.findViewById(R.id.et_account_no);

        llCreditCardDest.setVisibility(View.VISIBLE);
        llECheckDest.setVisibility(View.GONE);
        spPayMethodDest.setSelection(0);
        spPaymentOrderDest.setSelection(0);

        etCardNoDest.setText("");
        etNameOnCardDest.setText("");
        etExpirationDateDest.setText("");
        etZipCodeDest.setText("");
        etCVVDest.setText("");
        etRoutingNoDest.setText("");
        etFirstNameDest.setText("");
        etLastNameDest.setText("");
        etAccountNoDest.setText("");
    }
    private void hideRemoveImage()
    {
        ivRemove1.setVisibility(View.GONE);
        ivRemove2.setVisibility(View.GONE);
        ivRemove3.setVisibility(View.GONE);
    }
    
    private void initPayment2()
    {
        TextView tvPaymentIndex = (TextView)llPayment2.findViewById(R.id.tv_payment_index);
        tvPaymentIndex.setText(getString(R.string.payment_index,2));

        llCreditCard2 = (LinearLayout)llPayment2.findViewById(R.id.ll_credit_card);
        llECheck2 = (LinearLayout)llPayment2.findViewById(R.id.ll_echeck);
        spPayMethod2 = (Spinner)llPayment2.findViewById(R.id.pay_method_spinner);
        spPaymentOrder2 = (Spinner)llPayment2.findViewById(R.id.payment_order_spinner);
        spReplenishmentAmount2 = (Spinner)llPayment2.findViewById(R.id.replenishment_amount_spinner);

        llCVV2 = (LinearLayout)llPayment2.findViewById(R.id.ll_cvv);
        llPaymentMethod2 = (LinearLayout)llPayment2.findViewById(R.id.ll_pay_method);
        llReplenishment2 = (LinearLayout)llPayment2.findViewById(R.id.ll_replenishment);
        llReplenishment2.setVisibility(View.GONE);

        etCardNo2 = (EditText)llPayment2.findViewById(R.id.et_card_no);
        etNameOnCard2 = (EditText)llPayment2.findViewById(R.id.et_name_on_card);
        etExpirationDate2 = (EditText)llPayment2.findViewById(R.id.et_expiration_date);
        etZipCode2 = (EditText)llPayment2.findViewById(R.id.et_billing_zip_code);
        etCVV2 = (EditText)llPayment2.findViewById(R.id.et_cvv);

        etRoutingNo2 = (EditText)llPayment2.findViewById(R.id.et_routing_no);
        etFirstName2 = (EditText)llPayment2.findViewById(R.id.et_first_name);
        etLastName2 = (EditText)llPayment2.findViewById(R.id.et_last_name);
        etAccountNo2 = (EditText)llPayment2.findViewById(R.id.et_account_no);

        spPayMethod2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    llCreditCard2.setVisibility(View.VISIBLE);
                    llECheck2.setVisibility(View.GONE);
                } else {
                    llCreditCard2.setVisibility(View.GONE);
                    llECheck2.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ivRemove2 = (ImageView)llPayment2.findViewById(R.id.iv_remove);
        ivRemove2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paymentCount > 1) {
                    if (llPayment3.getVisibility() == View.VISIBLE) {
                        copyLayout(llPayment3, llPayment2);
                    }

                    hideLastLayout();
                    paymentCount--;
                }
                if (paymentCount <= 1) {
                    hideRemoveImage();
                }
                llAddAnotherPayment.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initPayment3()
    {
        TextView tvPaymentIndex = (TextView)llPayment3.findViewById(R.id.tv_payment_index);
        tvPaymentIndex.setText(getString(R.string.payment_index,3));

        llCreditCard3 = (LinearLayout)llPayment3.findViewById(R.id.ll_credit_card);
        llECheck3 = (LinearLayout)llPayment3.findViewById(R.id.ll_echeck);
        spPayMethod3 = (Spinner)llPayment3.findViewById(R.id.pay_method_spinner);
        spPaymentOrder3 = (Spinner)llPayment3.findViewById(R.id.payment_order_spinner);
        spReplenishmentAmount3 = (Spinner)llPayment3.findViewById(R.id.replenishment_amount_spinner);

        llCVV3 = (LinearLayout)llPayment3.findViewById(R.id.ll_cvv);
        llPaymentMethod3 = (LinearLayout)llPayment3.findViewById(R.id.ll_pay_method);
        llReplenishment3 = (LinearLayout)llPayment3.findViewById(R.id.ll_replenishment);
        llReplenishment3.setVisibility(View.GONE);

        etCardNo3 = (EditText)llPayment3.findViewById(R.id.et_card_no);
        etNameOnCard3 = (EditText)llPayment3.findViewById(R.id.et_name_on_card);
        etExpirationDate3 = (EditText)llPayment3.findViewById(R.id.et_expiration_date);
        etZipCode3 = (EditText)llPayment3.findViewById(R.id.et_billing_zip_code);
        etCVV3 = (EditText)llPayment3.findViewById(R.id.et_cvv);

        etRoutingNo3 = (EditText)llPayment3.findViewById(R.id.et_routing_no);
        etFirstName3 = (EditText)llPayment3.findViewById(R.id.et_first_name);
        etLastName3 = (EditText)llPayment3.findViewById(R.id.et_last_name);
        etAccountNo3 = (EditText)llPayment3.findViewById(R.id.et_account_no);

        spPayMethod3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    llCreditCard3.setVisibility(View.VISIBLE);
                    llECheck3.setVisibility(View.GONE);
                } else {
                    llCreditCard3.setVisibility(View.GONE);
                    llECheck3.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ivRemove3 = (ImageView)llPayment3.findViewById(R.id.iv_remove);
        ivRemove3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paymentCount > 1) {
                    hideLastLayout();
                    paymentCount--;
                }
                if (paymentCount <= 1) {
                    hideRemoveImage();
                }
                llAddAnotherPayment.setVisibility(View.VISIBLE);
            }
        });
    }
    
    private void initWidgetValue()
    {
        int index = TollRoadsApp.getInstance().selectedAccountType;
        String description = getString(R.string.new_payment_method_hint);
        if(index == Constants.ACCOUNT_TYPE_FASTRAK_INDIVIDUAL)
        {
            tvAccountType.setText(R.string.fastrak_account);
            tvAccountDescription.setText(R.string.fastrak_account_description);

            llTransponders.setVisibility(View.VISIBLE);

            //both
            llPaymentMethod1.setVisibility(View.VISIBLE);
            llPaymentMethod2.setVisibility(View.VISIBLE);
            llPaymentMethod3.setVisibility(View.VISIBLE);

            //need pay
            llCVV1.setVisibility(View.VISIBLE);
            llCVV2.setVisibility(View.GONE);
            llCVV3.setVisibility(View.GONE);
            llReplenishment1.setVisibility(View.VISIBLE);

        }
        else if(index == Constants.ACCOUNT_TYPE_CHARGE_EXPRESS)
        {
            tvAccountType.setText(R.string.express_charge_account);
            tvAccountDescription.setText(R.string.express_account_description);
            llTransponders.setVisibility(View.GONE);

            //Credit Card ONLY
            llPaymentMethod1.setVisibility(View.GONE);
            llPaymentMethod2.setVisibility(View.GONE);
            llPaymentMethod3.setVisibility(View.GONE);

            spPayMethod1.setSelection(0);
            spPayMethod2.setSelection(0);
            spPayMethod3.setSelection(0);

            //don't pay
            llCVV1.setVisibility(View.GONE);
            llCVV2.setVisibility(View.GONE);
            llCVV3.setVisibility(View.GONE);
            llReplenishment1.setVisibility(View.GONE);

            llCreditCard1.setVisibility(View.VISIBLE);
            llCreditCard2.setVisibility(View.VISIBLE);
            llCreditCard3.setVisibility(View.VISIBLE);
            llECheck1.setVisibility(View.GONE);
            llECheck2.setVisibility(View.GONE);
            llECheck3.setVisibility(View.GONE);
        }
        else if(index == Constants.ACCOUNT_TYPE_PREPAID_EXPRESS)
        {
            tvAccountType.setText(R.string.express_prepaid_account);
            tvAccountDescription.setText(R.string.express_account_description);
            llTransponders.setVisibility(View.GONE);

            //both
            llPaymentMethod1.setVisibility(View.VISIBLE);
            llPaymentMethod2.setVisibility(View.VISIBLE);
            llPaymentMethod3.setVisibility(View.VISIBLE);
            //need pay
            llCVV1.setVisibility(View.VISIBLE);
            llCVV2.setVisibility(View.GONE);
            llCVV3.setVisibility(View.GONE);
            llReplenishment1.setVisibility(View.VISIBLE);
        }
        else
        {
            tvAccountType.setText(R.string.express_invoice_account);
            tvAccountDescription.setText(R.string.express_account_description);
            llTransponders.setVisibility(View.GONE);

            //Credit Card ONLY
            llPaymentMethod1.setVisibility(View.GONE);
            llPaymentMethod2.setVisibility(View.GONE);
            llPaymentMethod3.setVisibility(View.GONE);

            spPayMethod1.setSelection(0);
            spPayMethod2.setSelection(0);
            spPayMethod3.setSelection(0);

            //don't pay
            llCVV1.setVisibility(View.GONE);
            llCVV2.setVisibility(View.GONE);
            llCVV3.setVisibility(View.GONE);
            llReplenishment1.setVisibility(View.GONE);

            llCreditCard1.setVisibility(View.VISIBLE);
            llCreditCard2.setVisibility(View.VISIBLE);
            llCreditCard3.setVisibility(View.VISIBLE);
            llECheck1.setVisibility(View.GONE);
            llECheck2.setVisibility(View.GONE);
            llECheck3.setVisibility(View.GONE);

            description = getString(R.string.optional_colon) + description;
        }

        tvPaymentDescription.setText(description);
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
