package com.seta.tollroaddroid.app;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.json.AccountInfo;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import java.io.UnsupportedEncodingException;

public class ContactInformationActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack,ivTcaHint;
    private EditText etAddress1, etAddress2, etCity, etZipCode, etEmail;
    private EditText etPrimaryNo, etSecondaryNo;
    private EditText etAddressContact;
    private AccountInfo accountInfo = TollRoadsApp.getInstance().accountInfo;
    private TextView tvName,tvSave;
    private Switch swPrimary,swSecondary, swReceivePromotion, swReceiveAlerts;
    private Response.ErrorListener errorListener;
    private Spinner spCountry,spStates;
    private int selectedStateIndex = 0;
    private int selectedCountryIndex = 0;

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit Account_Contact page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_information);
        FlurryAgent.logEvent("Enter Account_Contact page.");
        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        etAddress1 = (EditText)findViewById(R.id.et_address_1);
        etAddress2 = (EditText)findViewById(R.id.et_address_2);
        etCity = (EditText)findViewById(R.id.et_city);
        etZipCode = (EditText)findViewById(R.id.et_zip_code);

        etEmail = (EditText)findViewById(R.id.et_email);
        etPrimaryNo = (EditText)findViewById(R.id.et_primary_no);
        etSecondaryNo = (EditText)findViewById(R.id.et_secondary_no);
        tvName = (TextView)findViewById(R.id.tv_name);
        swPrimary = (Switch)findViewById(R.id.sw_primary_receive_sms);
        swSecondary = (Switch)findViewById(R.id.sw_secondary_receive_sms);
        swReceivePromotion = (Switch)findViewById(R.id.sw_receive_promotion);
        swReceiveAlerts = (Switch)findViewById(R.id.sw_receive_road_alerts);
        tvSave = (TextView)findViewById(R.id.tv_save);

        spCountry = (Spinner)findViewById(R.id.country_spinner);
        spStates = (Spinner)findViewById(R.id.state_spinner);

        ivTcaHint = (ImageView)findViewById(R.id.iv_tca_hint);
        etAddressContact = (EditText)findViewById(R.id.et_address_contact);
        initWidgetValue();
        setupListener();
    }

    private void controlSecondarySwitch(String secondaryNo)
    {
        if(secondaryNo.isEmpty())
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

    private void setupListener()
    {
        ivTcaHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToastMessage(getString(R.string.additional_access_hint));
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

                        }catch (UnsupportedEncodingException exceptionError) {

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

        CompoundButton.OnCheckedChangeListener onCheckedChangeListener =
                new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tvSave.setVisibility(View.VISIBLE);
            }
        };

        etAddress1.addTextChangedListener(textWatcher);
        etAddress2.addTextChangedListener(textWatcher);
        etCity.addTextChangedListener(textWatcher);
        etEmail.addTextChangedListener(textWatcher);
        etPrimaryNo.addTextChangedListener(textWatcher);
        etSecondaryNo.addTextChangedListener(textWatcher);
        etZipCode.addTextChangedListener(textWatcher);
        etAddressContact.addTextChangedListener(textWatcher);

        swPrimary.setOnCheckedChangeListener(onCheckedChangeListener);
        swSecondary.setOnCheckedChangeListener(onCheckedChangeListener);
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
                        dataAdapter = new ArrayAdapter<String>(ContactInformationActivity.this,
                                android.R.layout.simple_spinner_item,
                                getResources().getStringArray(R.array.united_states_arrays));
                    } else {
                        dataAdapter = new ArrayAdapter<String>(ContactInformationActivity.this,
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

                if(selectedCountryIndex != position) {
                    tvSave.setVisibility(View.VISIBLE);
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
        spStates.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (selectedStateIndex != position) {
                    tvSave.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        
        swReceivePromotion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(accountInfo.isReceive_promotion_material() != isChecked) {
                    tvSave.setVisibility(View.VISIBLE);
                }
            }
        });
        swReceiveAlerts.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(accountInfo.isReceive_road_alerts() != isChecked) {
                    tvSave.setVisibility(View.VISIBLE);
                }
            }
        });
        
        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation()) {
                    String params = populateParams();
                    showProgressDialog();
                    updateContactInfo(Resource.URL_ACCOUNT, params);
                }
            }
        });

        etSecondaryNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                controlSecondarySwitch(etSecondaryNo.getText().toString());
            }
        });
    }

    private String populateParams()
    {
        String params = "address1=" + Uri.encode(etAddress1.getText().toString());

        int countryIndex = spCountry.getSelectedItemPosition();
        String countryAbbr = TollRoadsApp.getCountryString(countryIndex);
        params = params + "&address_country="+ countryAbbr;

        if(spStates.getSelectedItem().toString().equals("US GOVT"))
        {
            params = params + "&address_state=US";
        }
        else {
            params = params + "&address_state=" + spStates.getSelectedItem().toString();
        }

        params = params + "&address_city="+ Uri.encode(etCity.getText().toString());
        params = params + "&zipcode="+ Uri.encode(etZipCode.getText().toString());
        params = params + "&primary_phone="+ Uri.encode(etPrimaryNo.getText().toString().replace(" ",""));
        params = params + "&address_contact="+ Uri.encode(etAddressContact.getText().toString());

        if(swPrimary.isChecked())
        {
            params = params + "&primary_receive_text_messages=true";
        }
        else
        {
            params = params + "&primary_receive_text_messages=false";
        }
        params = params + "&email_address="+ Uri.encode(etEmail.getText().toString());

        if(!etSecondaryNo.getText().toString().isEmpty()) {
            params = params + "&secondary_phone=" + Uri.encode(etSecondaryNo.getText().toString().replace(" ",""));

            if(swSecondary.isChecked())
            {
                params = params + "&secondary_receive_text_messages=true";
            }
            else
            {
                params = params + "&secondary_receive_text_messages=false";
            }
        }

        if(!etAddress2.getText().toString().isEmpty())
        {
            params = params + "&address2="+ Uri.encode(etAddress2.getText().toString());
        }

        if(swReceivePromotion.isChecked())
        {
            params = params + "&receive_promotion_material=true";
        }
        else
        {
            params = params + "&receive_promotion_material=false";
        }

        if(swReceiveAlerts.isChecked())
        {
            params = params + "&receive_road_alerts=true";
        }
        else
        {
            params = params + "&receive_road_alerts=false";
        }
        
        params = params + "&statement_delivery_method=1";

        return  params;
    }

    private void setAccountInfo()
    {
        if(accountInfo != null) {
            accountInfo.setAddress1(etAddress1.getText().toString());

            int countryIndex = spCountry.getSelectedItemPosition();
            String countryAbbr = TollRoadsApp.getCountryString(countryIndex);
            accountInfo.setAddress_country(countryAbbr);

            if(spStates.getSelectedItem().toString().equals("US GOVT"))
            {
                accountInfo.setAddress_state("US");
            }
            else {
                accountInfo.setAddress_state(spStates.getSelectedItem().toString());
            }

            accountInfo.setAddress_city(etCity.getText().toString());
            accountInfo.setZipcode(etZipCode.getText().toString());
            accountInfo.setPrimary_phone(etPrimaryNo.getText().toString());
            accountInfo.setPrimary_receive_text_messages(swPrimary.isChecked());
            accountInfo.setEmail_address(etEmail.getText().toString());

            if (!etSecondaryNo.getText().toString().isEmpty()) {
                accountInfo.setSecondary_phone(etSecondaryNo.getText().toString());
                accountInfo.setSecondary_receive_text_messages(swSecondary.isChecked());
            }

            if (!etAddress2.getText().toString().isEmpty()) {
                accountInfo.setAddress2(etAddress2.getText().toString());
            }
            accountInfo.setReceive_promotion_material(swReceivePromotion.isChecked());
            accountInfo.setReceive_road_alerts(swReceiveAlerts.isChecked());

            accountInfo.setAddress_contact(etAddressContact.getText().toString());
        }
    }

    private void updateContactInfo(String url,String params)
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
                            showToastMessage(getString(R.string.contact_information_saved));
                            setAccountInfo();
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

    private boolean checkValidation()
    {
        boolean ret = true;
        if(etAddress1.getText().length() == 0)
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
        else if(!Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString()).matches())
        {
            ret = false;
            showToastMessage(getString(R.string.email_invalid_warning));
        }

        return ret;
    }

    private void initWidgetValue()
    {
        if(accountInfo != null)
        {
            if(accountInfo.getAddress1() != null) {
                etAddress1.setText(accountInfo.getAddress1());
            }

            if(accountInfo.getAddress2() != null) {
                etAddress2.setText(accountInfo.getAddress2());
            }

            if(accountInfo.getAddress_city() != null) {
                etCity.setText(accountInfo.getAddress_city());
            }

            if(accountInfo.getZipcode() != null) {
                etZipCode.setText(accountInfo.getZipcode());
            }

            if(accountInfo.getEmail_address() != null) {
                etEmail.setText(accountInfo.getEmail_address());
            }

            if(accountInfo.getPrimary_phone() != null) {
                etPrimaryNo.setText(accountInfo.getPrimary_phone());
            }
            if(accountInfo.getSecondary_phone() != null) {
                etSecondaryNo.setText(accountInfo.getSecondary_phone());
            }

            if(accountInfo.getFull_name() != null)
            {
                tvName.setText(accountInfo.getFull_name());
            }
            swPrimary.setChecked(accountInfo.isPrimary_receive_text_messages());
            swSecondary.setChecked(accountInfo.isSecondary_receive_text_messages());

            controlSecondarySwitch(etSecondaryNo.getText().toString());

            if(accountInfo.getAddress_country() != null)
            {
                selectedCountryIndex = TollRoadsApp.getCountryIndex(this,
                        accountInfo.getAddress_country(),
                        accountInfo.getAddress_state());
                Log.e("selection", "country:" + selectedCountryIndex+",count:"+
                        spCountry.getAdapter().getCount());

                if(selectedCountryIndex >= spCountry.getAdapter().getCount())
                {
                    selectedCountryIndex = 0;
                }
                if(selectedCountryIndex < spCountry.getAdapter().getCount()) {
                    spCountry.setSelection(selectedCountryIndex, false);
                }
                RelativeLayout rlState = (RelativeLayout)findViewById(R.id.rl_state);
                if (selectedCountryIndex == 0|| selectedCountryIndex == 1) {
                    rlState.setBackgroundColor(Color.WHITE);
                    spStates.setEnabled(true);
                    ArrayAdapter<String> dataAdapter;
                    if (selectedCountryIndex == 0) {
                        dataAdapter = new ArrayAdapter<String>(ContactInformationActivity.this,
                                android.R.layout.simple_spinner_item,
                                getResources().getStringArray(R.array.united_states_arrays));
                    } else {
                        dataAdapter = new ArrayAdapter<String>(ContactInformationActivity.this,
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

                if(selectedCountryIndex == 0)
                {
                    etZipCode.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
                else
                {
                    etZipCode.setInputType(InputType.TYPE_CLASS_TEXT);
                }
            }

            if(accountInfo.getAddress_state() != null)
            {
                selectedStateIndex = TollRoadsApp.getStateIndex(this,
                        accountInfo.getAddress_country(),
                        accountInfo.getAddress_state());
                Log.e("selection", "state:" + selectedCountryIndex+",count:"+
                        spStates.getAdapter().getCount());
                if(selectedStateIndex >= spStates.getAdapter().getCount())
                {
                    selectedStateIndex = 0;
                }
                if(selectedStateIndex < spStates.getAdapter().getCount()) {
                    spStates.setSelection(selectedStateIndex, false);
                }

            }

            if(accountInfo.getAddress_contact() != null)
            {
                etAddressContact.setText(accountInfo.getAddress_contact());
            }
            swReceivePromotion.setChecked(accountInfo.isReceive_promotion_material());

            swReceiveAlerts.setChecked(accountInfo.isReceive_road_alerts());
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
