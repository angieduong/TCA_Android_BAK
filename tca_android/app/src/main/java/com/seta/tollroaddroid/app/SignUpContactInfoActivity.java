package com.seta.tollroaddroid.app;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
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

public class SignUpContactInfoActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private TextView tvGoNext;
    private EditText etAddress1, etAddress2, etCity, etZipCode, etEmail, etRetypeEmail;
    private EditText etPrimaryNo, etSecondaryNo;

    private EditText etFirstName, etLastName;
    private EditText etPromoCode;
    private Switch swPrimary,swSecondary, swReceivePromotion, swReceiveAlerts;
    private Spinner spCountry,spStates;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("CurrentSignUpReq", TollRoadsApp.getInstance().gSignUpRequest);
        outState.putBoolean(Constants.FROM_OTT, TollRoadsApp.getInstance().gFromOTT);
    }

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit SignUp_2_Contact_"+TollRoadsApp.getInstance().getSignUpAccountType()+" page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_contact_info);
        if(savedInstanceState != null)
        {
            TollRoadsApp.getInstance().gSignUpRequest =
                    (SignUpRequest) savedInstanceState.getSerializable("CurrentSignUpReq");
            TollRoadsApp.getInstance().gFromOTT =
                    savedInstanceState.getBoolean(Constants.FROM_OTT,false);
        }

        FlurryAgent.logEvent("Enter SignUp_2_Contact_"+TollRoadsApp.getInstance().getSignUpAccountType()+" page.");

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvGoNext = (TextView) findViewById(R.id.tv_go_next);
        
        etAddress1 = (EditText)findViewById(R.id.et_address_1);
        etAddress2 = (EditText)findViewById(R.id.et_address_2);
        etCity = (EditText)findViewById(R.id.et_city);
        etZipCode = (EditText)findViewById(R.id.et_zip_code);
        etEmail = (EditText)findViewById(R.id.et_email);
        etRetypeEmail = (EditText)findViewById(R.id.et_re_type_email);
        etPrimaryNo = (EditText)findViewById(R.id.et_primary_no);
        etSecondaryNo = (EditText)findViewById(R.id.et_secondary_no);
        etFirstName = (EditText)findViewById(R.id.et_first_name);
        etLastName = (EditText)findViewById(R.id.et_last_name);

        swPrimary = (Switch)findViewById(R.id.sw_primary_receive_sms);
        swSecondary = (Switch)findViewById(R.id.sw_secondary_receive_sms);
        swReceivePromotion = (Switch)findViewById(R.id.sw_receive_promotion);
        swReceiveAlerts = (Switch)findViewById(R.id.sw_receive_road_alerts);

        spCountry = (Spinner)findViewById(R.id.country_spinner);
        spStates = (Spinner)findViewById(R.id.state_spinner);

        etPromoCode = (EditText)findViewById(R.id.et_promo_code);
        LinearLayout llPromoCode = (LinearLayout)findViewById(R.id.ll_promo_code);
        int index = TollRoadsApp.getInstance().selectedAccountType;

        if(index == Constants.ACCOUNT_TYPE_INVOICE_EXPRESS)
        {
            llPromoCode.setVisibility(View.GONE);
        }
        else
        {
            llPromoCode.setVisibility(View.VISIBLE);
        }
        swSecondary.setClickable(false);
        swSecondary.setEnabled(false);
        swSecondary.setAlpha(0.5f);
        initWidgetValue();
        setupListener();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            gotoActivity(this, SignUpAccountInfoActivity.class, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setupListener()
    {
        etSecondaryNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().isEmpty())
                {
                    swSecondary.setClickable(false);
                    swSecondary.setChecked(false);
                    swSecondary.setEnabled(false);

                    swSecondary.setAlpha(0.5f);
                }
                else
                {
                    swSecondary.setAlpha(1.0f);
                    swSecondary.setClickable(true);
                    swSecondary.setEnabled(true);
                }
            }
        });
        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(v.getContext(), SignUpAccountInfoActivity.class,
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
        });
        spCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                ArrayAdapter<String> dataAdapter;
//                if (position == 0) {
//                    dataAdapter = new ArrayAdapter<String>(view.getContext(),
//                            android.R.layout.simple_spinner_item,
//                            getResources().getStringArray(R.array.united_states_arrays));
//                } else if (position == 1) {
//                    dataAdapter = new ArrayAdapter<String>(view.getContext(),
//                            android.R.layout.simple_spinner_item,
//                            getResources().getStringArray(R.array.canada_provinces_arrays));
//                } else {
//                    dataAdapter = new ArrayAdapter<String>(view.getContext(),
//                            android.R.layout.simple_spinner_item,
//                            getResources().getStringArray(R.array.mexico_states_arrays));
//                }
//                // Drop down layout style - list view with radio button
//                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                // attaching data adapter to spinner
//                spStates.setAdapter(dataAdapter);
                RelativeLayout rlState = (RelativeLayout)findViewById(R.id.rl_state);
                if (position == 0|| position == 1) {
                    rlState.setBackgroundColor(Color.WHITE);
                    spStates.setEnabled(true);
                    ArrayAdapter<String> dataAdapter;
                    if (position == 0) {
                        dataAdapter = new ArrayAdapter<String>(SignUpContactInfoActivity.this,
                                android.R.layout.simple_spinner_item,
                                getResources().getStringArray(R.array.united_states_arrays));
                    } else {
                        dataAdapter = new ArrayAdapter<String>(SignUpContactInfoActivity.this,
                                android.R.layout.simple_spinner_item,
                                getResources().getStringArray(R.array.canada_provinces_arrays));
                    }
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    // attaching data adapter to spinner
                    spStates.setAdapter(dataAdapter);
                }
                else {
                    rlState.setBackgroundColor(getResources().getColor(R.color.colorBgGrayLight));
                    spStates.setEnabled(false);
                }

                if(position == 0)
                {
                    etZipCode.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
                else
                {
                    etZipCode.setInputType(InputType.TYPE_CLASS_TEXT);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        tvGoNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation())
                {
                    setRequest();
                    checkContact();
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

    private void checkContact()
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
                            gotoActivity(SignUpContactInfoActivity.this, SignUpVehicleInfoActivity.class,
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

        ServerDelegate.checkContact(params, listener, errorListener);
    }

    private void setRequest()
    {
        SignUpRequest signUpRequest = TollRoadsApp.getInstance().gSignUpRequest;

        signUpRequest.setAddress_first_name(etFirstName.getText().toString());
        signUpRequest.setAddress_last_name(etLastName.getText().toString());
        signUpRequest.setAddress1(etAddress1.getText().toString());
        signUpRequest.setAddress2(etAddress2.getText().toString());

        signUpRequest.setAddress_zipcode(etZipCode.getText().toString());
        int countryIndex = spCountry.getSelectedItemPosition();
        String countryAbbr = TollRoadsApp.getCountryString(countryIndex);

        signUpRequest.setAddress_country(countryAbbr);

        if(spStates.getSelectedItem().toString().equals("US GOVT"))
        {
            signUpRequest.setAddress_state("US");
        }
        else {
            signUpRequest.setAddress_state(spStates.getSelectedItem().toString());
        }

        signUpRequest.setAddress_city(etCity.getText().toString());
        signUpRequest.setEmail_address(etEmail.getText().toString());
        signUpRequest.setPrimary_phone(etPrimaryNo.getText().toString().replace(" ",""));
        signUpRequest.setSecondary_phone(etSecondaryNo.getText().toString().replace(" ",""));
        signUpRequest.setPrimary_receive_text_messages(swPrimary.isChecked());
        signUpRequest.setSecondary_receive_text_messages(swSecondary.isChecked());
        signUpRequest.setReceive_promotion_material(swReceivePromotion.isChecked());
        signUpRequest.setReceive_road_alerts(swReceiveAlerts.isChecked());

        signUpRequest.setPromotion_code(etPromoCode.getText().toString());
    }

    private boolean checkValidation()
    {
        boolean ret = true;
        if(etFirstName.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.first_name_empty_warning));
        }
        else if(etLastName.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.last_name_empty_warning));
        }
        else if(etAddress1.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.address_1_empty_warning));
        }
        else if(etCity.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.city_empty_warning));
        }
//        else if(etZipCode.getText().length() == 0)
//        {
//            ret = false;
//            showToastMessage(getString(R.string.zip_code_empty_warning));
//        }
        else if(etPrimaryNo.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.primary_no_empty_warning));
        }
        else if(etEmail.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.email_empty_warning));
        }
        else if(!etEmail.getText().toString().equals(etRetypeEmail.getText().toString())) {
            ret = false;
            showToastMessage(getString(R.string.email_not_match_warning));
        }
        return ret;
    }

    private void initWidgetValue()
    {
        if(TollRoadsApp.getInstance().gFromOTT)
        {
            OTTUserInfoRequest ottUserInfoRequest = TollRoadsApp.getInstance().gOTTUserInfoRequest;
            etFirstName.setText(ottUserInfoRequest.getAddress_first_name());
            etLastName.setText(ottUserInfoRequest.getAddress_last_name());
            etAddress1.setText(ottUserInfoRequest.getAddress1());
            etZipCode.setText(ottUserInfoRequest.getAddress_zipcode());
            etCity.setText(ottUserInfoRequest.getAddress_city());
            if(ottUserInfoRequest.getEmail_address() != null) {
                etEmail.setText(ottUserInfoRequest.getEmail_address());
                etRetypeEmail.setText(ottUserInfoRequest.getEmail_address());
            }
            int countryIndex = TollRoadsApp.getCountryIndex(this,
                    ottUserInfoRequest.getAddress_country(),
                    ottUserInfoRequest.getAddress_state());
            if(countryIndex >= spCountry.getCount())
            {
                countryIndex = spCountry.getCount() - 1;
            }
            spCountry.setSelection(countryIndex);

            int stateIndex = TollRoadsApp.getStateIndex(this,
                    ottUserInfoRequest.getAddress_country(),
                    ottUserInfoRequest.getAddress_state());
            if(stateIndex >= spStates.getCount())
            {
                stateIndex = spStates.getCount() - 1;
            }
            spStates.setSelection(stateIndex);
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
