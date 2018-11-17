package com.seta.tollroaddroid.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.json.AutoPopulateVehicleAndContactInfo;
import com.seta.tollroaddroid.app.json.CheckPlateResponse;
import com.seta.tollroaddroid.app.json.OTTUserInfoRequest;
import com.seta.tollroaddroid.app.json.Vehicle;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import java.io.UnsupportedEncodingException;

public class OTTVehicleInfoActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private TextView tvGoNext;
    private Vehicle gVehicle = new Vehicle();
    private Spinner spCountry,spStates, spTrailerStates, spTrailerCountry, spVehicleType;
    private LinearLayout llTrailer;
    private EditText etPlate, etPlateConfirm, etTrailerPlate;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("OTTUserInfoRequest", TollRoadsApp.getInstance().gOTTUserInfoRequest);
        outState.putBoolean("ShowOTTCaching",TollRoadsApp.getInstance().gShowOTTCaching);
    }

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit OTT_1_Vehicle page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ott_vehicle_info);
        if(savedInstanceState != null)
        {
            TollRoadsApp.getInstance().gOTTUserInfoRequest = (OTTUserInfoRequest) savedInstanceState.getSerializable("OTTUserInfoRequest");
            TollRoadsApp.getInstance().gShowOTTCaching = savedInstanceState.getBoolean("ShowOTTCaching");
        }
        else
        {
            TollRoadsApp.getInstance().gOTTUserInfoRequest = new OTTUserInfoRequest();
        }
        FlurryAgent.logEvent("Enter OTT_1_Vehicle page.");

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvGoNext = (TextView) findViewById(R.id.tv_go_next);
        spCountry = (Spinner)findViewById(R.id.country_spinner);
        spStates = (Spinner)findViewById(R.id.state_spinner);
        spVehicleType = (Spinner)findViewById(R.id.vehicle_type_spinner);
        spTrailerStates = (Spinner)findViewById(R.id.trailer_state_spinner);
        spTrailerCountry = (Spinner)findViewById(R.id.trailer_country_spinner);

        llTrailer = (LinearLayout)findViewById(R.id.ll_trailer);
        etPlate = (EditText)findViewById(R.id.et_license_plate_no);
        etPlateConfirm = (EditText)findViewById(R.id.et_confirm_plate_no);
        etTrailerPlate = (EditText)findViewById(R.id.et_trailer_plate_no);
        loadCaching();

        initWidgetValue();
        setupListener();
        debugModeInit(savedInstanceState);

        autoPrePopulate(savedInstanceState);
    }

    private void populateVehicle1Info(AutoPopulateVehicleAndContactInfo autoPopulateVehicleAndContactInfo)
    {
        if(autoPopulateVehicleAndContactInfo != null) {
            etPlate.setText(autoPopulateVehicleAndContactInfo.getPlate1());
            etPlateConfirm.setText(autoPopulateVehicleAndContactInfo.getPlate1());
            int countryIndex = TollRoadsApp.getCountryIndex(this,
                    autoPopulateVehicleAndContactInfo.getCountry1(),
                    autoPopulateVehicleAndContactInfo.getState1());
            if (countryIndex >= spCountry.getCount()) {
                countryIndex = spCountry.getCount() - 1;
            }
            cachingStateIndex = TollRoadsApp.getStateIndex(this,
                    autoPopulateVehicleAndContactInfo.getCountry1(),
                    autoPopulateVehicleAndContactInfo.getState1());

            spCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    countrySelectedListener.onItemSelected(parent, view, position, id);
                    if (cachingStateIndex >= spStates.getCount()) {
                        cachingStateIndex = spStates.getCount() - 1;
                    }
                    spStates.setSelection(cachingStateIndex);

                    spCountry.setOnItemSelectedListener(countrySelectedListener);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            spCountry.setSelection(countryIndex);

        }
    }

    private void populateVehicle2Info(AutoPopulateVehicleAndContactInfo autoPopulateVehicleAndContactInfo)
    {
        if(autoPopulateVehicleAndContactInfo != null && autoPopulateVehicleAndContactInfo.getPlate2() != null) {
            etTrailerPlate.setText(autoPopulateVehicleAndContactInfo.getPlate2());
            int trailerCountryIndex = TollRoadsApp.getCountryIndex(this,
                    autoPopulateVehicleAndContactInfo.getCountry2(),
                    autoPopulateVehicleAndContactInfo.getState2());
            if(trailerCountryIndex >= spTrailerCountry.getCount())
            {
                trailerCountryIndex = spTrailerCountry.getCount() - 1;
            }
            cachingTrailerStateIndex = TollRoadsApp.getStateIndex(this,
                    autoPopulateVehicleAndContactInfo.getCountry2(),
                    autoPopulateVehicleAndContactInfo.getState2());

            spTrailerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    trailerCountrySelectedListener.onItemSelected(parent,view,position,id);
                    if (cachingTrailerStateIndex >= spTrailerStates.getCount()) {
                        cachingTrailerStateIndex = spTrailerStates.getCount() - 1;
                    }
                    spTrailerStates.setSelection(cachingTrailerStateIndex);

                    spTrailerCountry.setOnItemSelectedListener(trailerCountrySelectedListener);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            spTrailerCountry.setSelection(trailerCountryIndex);

        }
    }

    private void populateVehicleInfo(AutoPopulateVehicleAndContactInfo autoPopulateVehicleAndContactInfo)
    {
        if(autoPopulateVehicleAndContactInfo != null)
        {
            String vehicleClass = autoPopulateVehicleAndContactInfo.getVehicle_class();
            if(vehicleClass.equals("2"))
            {
                spVehicleType.setSelection(0);
            }
            else if(vehicleClass.equals("3to4"))
            {
                spVehicleType.setSelection(1);
            }
            else if(vehicleClass.equals("5to6"))
            {
                spVehicleType.setSelection(2);
            }

            populateVehicle1Info(autoPopulateVehicleAndContactInfo);
            populateVehicle2Info(autoPopulateVehicleAndContactInfo);

        }
    }

    private void autoPrePopulate(Bundle savedInstanceState)
    {
        if(savedInstanceState == null)
        {
            if(TollRoadsApp.getInstance().isAutoPopulateVehicleAndContact()
                    && !TollRoadsApp.getInstance().getAutoPopulateVehicleAndContactInfo().isEmpty())
            {
                AutoPopulateVehicleAndContactInfo autoPopulateVehicleAndContactInfo;
                Gson gson= new GsonBuilder().serializeNulls().create();
                autoPopulateVehicleAndContactInfo = gson.fromJson(TollRoadsApp.getInstance().getAutoPopulateVehicleAndContactInfo(),
                        AutoPopulateVehicleAndContactInfo.class);

                populateVehicleInfo(autoPopulateVehicleAndContactInfo);
            }
        }
    }

    private void debugModeInit(Bundle savedInstanceState)
    {
//        if(savedInstanceState == null && BuildConfig.DEBUG)
//        {
//            etPlate.setText("lptuioo");
//            etPlateConfirm.setText("lptuioo");
//        }
    }

    private void loadCaching()
    {
        String ottCaching = TollRoadsApp.getInstance().getOTTRequest();
        if(!ottCaching.isEmpty()) {
            Gson gson = new GsonBuilder().serializeNulls().create();
            TollRoadsApp.getInstance().gCachingOTTRequest = gson.fromJson(ottCaching,
                    OTTUserInfoRequest.class);
        }
    }

    private int cachingStateIndex = 0;
    private int cachingTrailerStateIndex = 0;
    private void initWithCaching()
    {
        OTTUserInfoRequest ottUserInfoRequest = TollRoadsApp.getInstance().gCachingOTTRequest;
        etPlateConfirm.setText(ottUserInfoRequest.getPlate1());

        int countryIndex = TollRoadsApp.getCountryIndex(this,
                ottUserInfoRequest.getCountry1(),
                ottUserInfoRequest.getState1());
        if(countryIndex >= spCountry.getCount())
        {
            countryIndex = spCountry.getCount() - 1;
        }
        cachingStateIndex = TollRoadsApp.getStateIndex(this,
                ottUserInfoRequest.getCountry1(),
                ottUserInfoRequest.getState1());


        if(countryIndex == spCountry.getSelectedItemPosition())
        {
            spCountry.setOnItemSelectedListener(countrySelectedListener);
            if(cachingStateIndex >= spStates.getCount())
            {
                cachingStateIndex = spStates.getCount() - 1;
            }
            spStates.setSelection(cachingStateIndex);
        }
        else {
            spCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    countrySelectedListener.onItemSelected(parent,view,position,id);
                    if(cachingStateIndex >= spStates.getCount())
                    {
                        cachingStateIndex = spStates.getCount() - 1;
                    }
                    spStates.setSelection(cachingStateIndex);

                    spCountry.setOnItemSelectedListener(countrySelectedListener);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            spCountry.setSelection(countryIndex);
        }

        String vehicleClass = ottUserInfoRequest.getVehicle_class();
        if(vehicleClass.equals("2"))
        {
            spVehicleType.setSelection(0);
        }
        else if(vehicleClass.equals("3to4"))
        {
            spVehicleType.setSelection(1);
        }
        else if(vehicleClass.equals("5to6"))
        {
            spVehicleType.setSelection(2);
        }

        if(ottUserInfoRequest.getPlate2() != null) {
            etTrailerPlate.setText(ottUserInfoRequest.getPlate2());
            int trailerCountryIndex = TollRoadsApp.getCountryIndex(this,
                    ottUserInfoRequest.getCountry2(),
                    ottUserInfoRequest.getState2());
            if(trailerCountryIndex >= spTrailerCountry.getCount())
            {
                trailerCountryIndex = spTrailerCountry.getCount() - 1;
            }
            cachingTrailerStateIndex = TollRoadsApp.getStateIndex(this,
                    ottUserInfoRequest.getCountry2(),
                    ottUserInfoRequest.getState2());

            if(trailerCountryIndex == spTrailerCountry.getSelectedItemPosition())
            {
                spTrailerCountry.setOnItemSelectedListener(trailerCountrySelectedListener);
                if (cachingTrailerStateIndex >= spTrailerStates.getCount()) {
                    cachingTrailerStateIndex = spTrailerStates.getCount() - 1;
                }
                spTrailerStates.setSelection(cachingTrailerStateIndex);
            }
            else {
                spTrailerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        trailerCountrySelectedListener.onItemSelected(parent,view,position,id);
                        if (cachingTrailerStateIndex >= spTrailerStates.getCount()) {
                            cachingTrailerStateIndex = spTrailerStates.getCount() - 1;
                        }
                        spTrailerStates.setSelection(cachingTrailerStateIndex);

                        spTrailerCountry.setOnItemSelectedListener(trailerCountrySelectedListener);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                spTrailerCountry.setSelection(trailerCountryIndex);
            }
        }
    }

    private void checkCaching()
    {
//        OTTUserInfoRequest ottUserInfoRequest = TollRoadsApp.getInstance().gCachingOTTRequest;
//        if(ottUserInfoRequest != null && ottUserInfoRequest.getPlate1() != null)
//        {
//            if(ottUserInfoRequest.getPlate1().equals(etPlate.getText().toString()))
//            {
//                TollRoadsApp.getInstance().gShowOTTCaching = true;
//                initWithCaching();
//            }
//            else
//            {
//                TollRoadsApp.getInstance().gShowOTTCaching = false;
//            }
//        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            gotoActivity(getApplicationContext(), LandingPageActivity.class,
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            return  true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private AdapterView.OnItemSelectedListener countrySelectedListener = null;
    private AdapterView.OnItemSelectedListener trailerCountrySelectedListener = null;
    private void setupListener()
    {
        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(getApplicationContext(), LandingPageActivity.class,
                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
        });

        etPlate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    checkCaching();
                }
            }
        });

        countrySelectedListener = new AdapterView.OnItemSelectedListener() {
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
                if (position == 0 || position == 1) {
                    rlState.setBackgroundColor(Color.WHITE);
                    spStates.setEnabled(true);
                    ArrayAdapter<String> dataAdapter;
                    if (position == 0) {
                        dataAdapter = new ArrayAdapter<String>(OTTVehicleInfoActivity.this,
                                android.R.layout.simple_spinner_item,
                                getResources().getStringArray(R.array.united_states_arrays));
                    } else {
                        dataAdapter = new ArrayAdapter<String>(OTTVehicleInfoActivity.this,
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };

        spCountry.setOnItemSelectedListener(countrySelectedListener);

        trailerCountrySelectedListener = new AdapterView.OnItemSelectedListener() {
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
//                spTrailerStates.setAdapter(dataAdapter);
                RelativeLayout rlState = (RelativeLayout)findViewById(R.id.rl_trailer_state);
                if (position == 0|| position == 1) {
                    rlState.setBackgroundColor(Color.WHITE);
                    spTrailerStates.setEnabled(true);
                    ArrayAdapter<String> dataAdapter;
                    if (position == 0) {
                        dataAdapter = new ArrayAdapter<String>(OTTVehicleInfoActivity.this,
                                android.R.layout.simple_spinner_item,
                                getResources().getStringArray(R.array.united_states_arrays));
                    } else {
                        dataAdapter = new ArrayAdapter<String>(OTTVehicleInfoActivity.this,
                                android.R.layout.simple_spinner_item,
                                getResources().getStringArray(R.array.canada_provinces_arrays));
                    }
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    // attaching data adapter to spinner
                    spTrailerStates.setAdapter(dataAdapter);
                }
                else {
                    rlState.setBackgroundColor(getResources().getColor(R.color.colorBgGrayLight));
                    spTrailerStates.setEnabled(false);
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        spTrailerCountry.setOnItemSelectedListener(trailerCountrySelectedListener);
        spVehicleType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayAdapter<String> dataAdapter;
                if (position == 0) {
                    llTrailer.setVisibility(View.GONE);
                } else {
                    llTrailer.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        tvGoNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation()) {
                    showProgressDialog();
                    String params = populateParams();

                    checkPlateRequest(Resource.URL_PAY_GO, params);
                }
                //gotoActivity(v.getContext(), OTTContactInfoActivity.class, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
        });
    }

    private void initWidgetValue()
    {

    }

    private String populateParams()
    {
        String params = "plate1="+ etPlate.getText().toString();
        int countryIndex = spCountry.getSelectedItemPosition();
        String countryAbbr = TollRoadsApp.getCountryString(countryIndex);
        params = params + "&country1=" + countryAbbr;
        if(countryAbbr.isEmpty())
        {
            if(spStates.getSelectedItem().toString().equals("US GOVT"))
            {
                params = params + "&state1=US";
            }
            else {
                params = params + "&state1=" + spStates.getSelectedItem().toString();
            }
        }
        else
        {
            params = params + "&state1=";
        }

        int vehicleType = spVehicleType.getSelectedItemPosition();
        if(vehicleType == 0)
        {
            params = params + "&vehicle_class="+"2";
        }
        else if(vehicleType == 1)
        {
            params = params + "&vehicle_class="+"3to4";
        }
        else if(vehicleType == 2)
        {
            params = params + "&vehicle_class="+"5to6";
        }

        if(llTrailer.getVisibility() == View.VISIBLE)
        {
            params = params +"&plate2="+ etTrailerPlate.getText().toString();

            int country2Index = spTrailerCountry.getSelectedItemPosition();
            String country2Abbr = TollRoadsApp.getCountryString(country2Index);
            params = params + "&country2=" + country2Abbr;
            if(country2Abbr.isEmpty())
            {
                params = params + "&state2=" + spTrailerStates.getSelectedItem().toString();
            }
            else
            {
                params = params + "&state2=";
            }
        }
        return params;
    }

    private boolean checkValidation()
    {
        boolean ret = true;

            if (etPlate.getText().length() == 0) {
                ret = false;
                showToastMessage(getString(R.string.license_plate_empty_warning));

            } else if (!etPlate.getText().toString().equals(etPlateConfirm.getText().toString())) {
                ret = false;
                showToastMessage(getString(R.string.license_plate_not_match_warning));
            }

//        if (llTrailer.getVisibility() == View.VISIBLE
//                        && etTrailerPlate.getText().length() == 0) {
//            ret = false;
//            showToastMessage(getString(R.string.trailer_plate_no_empty_warning));
//        }
        return ret;
    }

    private void saveCheckPlateResponse(CheckPlateResponse checkPlateResponse)
    {
        if(checkPlateResponse != null) {
            TollRoadsApp.getInstance().setToken(checkPlateResponse.getTokenID());
            TollRoadsApp.getInstance().gVehicleFound = checkPlateResponse.getVehicle_found();
        }

        int countryIndex = spCountry.getSelectedItemPosition();
        String countryAbbr = TollRoadsApp.getCountryString(countryIndex);
        TollRoadsApp.getInstance().gOTTUserInfoRequest.setCountry1(countryAbbr);

        TollRoadsApp.getInstance().gOTTUserInfoRequest.setPlate1(etPlate.getText().toString());

        if(spStates.getSelectedItem().toString().equals("US GOVT"))
        {
            TollRoadsApp.getInstance().gOTTUserInfoRequest.setState1("US");
        }
        else {
            TollRoadsApp.getInstance().gOTTUserInfoRequest.setState1(spStates.getSelectedItem().toString());
        }

        if(llTrailer.getVisibility() == View.VISIBLE)
        {
            TollRoadsApp.getInstance().gOTTUserInfoRequest.setPlate2(etTrailerPlate.getText().toString());
            TollRoadsApp.getInstance().gOTTUserInfoRequest.setState2(spTrailerStates.getSelectedItem().toString());

            int trailerCountryIndex = spTrailerCountry.getSelectedItemPosition();
            String trailerCountryAbbr = TollRoadsApp.getCountryString(trailerCountryIndex);
            TollRoadsApp.getInstance().gOTTUserInfoRequest.setCountry2(trailerCountryAbbr);
        }
        int vehicleType = spVehicleType.getSelectedItemPosition();
        if(vehicleType == 0)
        {
            TollRoadsApp.getInstance().gOTTUserInfoRequest.setVehicle_class("2");
        }
        else if(vehicleType == 1)
        {
            TollRoadsApp.getInstance().gOTTUserInfoRequest.setVehicle_class("3to4");
        }
        else if(vehicleType == 2)
        {
            TollRoadsApp.getInstance().gOTTUserInfoRequest.setVehicle_class("5to6");
        }
    }

    private void checkPlateRequest(String url,String params)
    {
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
        Response.Listener listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                closeProgressDialog();
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, "response:" + response);

                        if(checkResponse(response)) {
                            Gson gson = new GsonBuilder().serializeNulls().create();
                            CheckPlateResponse checkPlateResponse = gson.fromJson(response, CheckPlateResponse.class);
                            if(checkPlateResponse.getVehicle_found() != 1) {

                                saveCheckPlateResponse(checkPlateResponse);

                                gotoActivity(OTTVehicleInfoActivity.this, OTTCalculateTollActivity.class,//OTTContactInfoActivity.class,
                                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            }
                            else
                            {
                                showToastMessage(getString(R.string.ott_account_exist_error));
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

        ServerDelegate.checkPlateRequest(url, params, listener, errorListener);

//        //test
//        closeProgressDialog();
//        TollRoadsApp.getInstance().gVehicleFound = Constants.VEHICLE_FOUND_TYPE_NOT_EXIST;
//        saveCheckPlateResponse(null);
//        gotoActivity(OTTVehicleInfoActivity.this, OTTCalculateTollActivity.class,//OTTContactInfoActivity.class,
//                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
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
