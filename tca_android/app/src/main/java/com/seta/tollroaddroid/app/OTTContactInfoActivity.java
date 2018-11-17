package com.seta.tollroaddroid.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.custom.MultiButtonsPopupDialog;
import com.seta.tollroaddroid.app.json.AccountInfo;
import com.seta.tollroaddroid.app.json.AutoPopulateVehicleAndContactInfo;
import com.seta.tollroaddroid.app.json.LocInfo;
import com.seta.tollroaddroid.app.json.OTTUserInfoRequest;
import com.seta.tollroaddroid.app.json.OTTUserInfoResponse;
import com.seta.tollroaddroid.app.json.OttTrip;
import com.seta.tollroaddroid.app.json.TripInfo;
import com.seta.tollroaddroid.app.utilities.Constants;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

public class OTTContactInfoActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private TextView tvGoNext;
    private EditText etFirstName, etLastName, etAddress, etCity, etZipCode, etEmail,etRetypeEmail;
    private AccountInfo accountInfo = new AccountInfo();

    private Spinner spCountry,spStates;

    private Response.ErrorListener errorListener;

    private MultiButtonsPopupDialog gMultiButtonsPopupDialog;

    private TextView tvTotalCharge;

    class ViewHolder
    {
        int index;
        Spinner spStart;
        Spinner spEnd;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("VehicleFound", TollRoadsApp.getInstance().gVehicleFound);
        outState.putSerializable("OTTUserInfoRequest", TollRoadsApp.getInstance().gOTTUserInfoRequest);
        outState.putBoolean("ShowOTTCaching", TollRoadsApp.getInstance().gShowOTTCaching);
    }

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit OTT_2_Contact page.");
        super.onDestroy();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ott_contact_info);
        FlurryAgent.logEvent("Enter OTT_2_Contact page.");

        if(savedInstanceState != null)
        {
            TollRoadsApp.getInstance().gOTTUserInfoRequest = (OTTUserInfoRequest) savedInstanceState.getSerializable("OTTUserInfoRequest");
            TollRoadsApp.getInstance().gVehicleFound = savedInstanceState.getInt("VehicleFound");
            TollRoadsApp.getInstance().gShowOTTCaching = savedInstanceState.getBoolean("ShowOTTCaching");
        }

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvGoNext = (TextView) findViewById(R.id.tv_go_next);
        etAddress = (EditText)findViewById(R.id.et_address);

        etFirstName = (EditText)findViewById(R.id.et_first_name);
        etLastName = (EditText)findViewById(R.id.et_last_name);
        etRetypeEmail = (EditText)findViewById(R.id.et_re_type_email);
        etCity = (EditText)findViewById(R.id.et_city);
        etZipCode = (EditText)findViewById(R.id.et_zip_code);
        etEmail = (EditText)findViewById(R.id.et_email);

        spCountry = (Spinner)findViewById(R.id.country_spinner);
        spStates = (Spinner)findViewById(R.id.state_spinner);

        tvTotalCharge = (TextView)findViewById(R.id.tv_total_charge);

        setupListener();

        if(savedInstanceState == null)
        {
            if(TollRoadsApp.getInstance().gShowOTTCaching) {
                initWithCaching();
            }
        }

        autoPrePopulate(savedInstanceState);
        //debugModeInit(savedInstanceState);
    }

    private void populateContactInfo(AutoPopulateVehicleAndContactInfo autoPopulateVehicleAndContactInfo)
    {
        etFirstName.setText(autoPopulateVehicleAndContactInfo.getAddress_first_name());
        etLastName.setText(autoPopulateVehicleAndContactInfo.getAddress_last_name());
        etAddress.setText(autoPopulateVehicleAndContactInfo.getAddress1());
        etCity.setText(autoPopulateVehicleAndContactInfo.getAddress_city());
        etEmail.setText(autoPopulateVehicleAndContactInfo.getEmail_address());
        etRetypeEmail.setText(autoPopulateVehicleAndContactInfo.getEmail_address());
        etZipCode.setText(autoPopulateVehicleAndContactInfo.getAddress_zipcode());

        int countryIndex = TollRoadsApp.getCountryIndex(this,
                autoPopulateVehicleAndContactInfo.getAddress_country(),
                autoPopulateVehicleAndContactInfo.getAddress_state());
        if(countryIndex >= spCountry.getCount())
        {
            countryIndex = spCountry.getCount() - 1;
        }
        spCountry.setSelection(countryIndex);

        int stateIndex = TollRoadsApp.getStateIndex(this,
                autoPopulateVehicleAndContactInfo.getAddress_country(),
                autoPopulateVehicleAndContactInfo.getAddress_state());
        if(stateIndex >= spStates.getCount())
        {
            stateIndex = spStates.getCount() - 1;
        }
        spStates.setSelection(stateIndex);
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

                populateContactInfo(autoPopulateVehicleAndContactInfo);
            }
        }
    }
    
    private void debugModeInit(Bundle savedInstanceState)
    {
        if(savedInstanceState == null && BuildConfig.DEBUG)
        {
            etFirstName.setText("t");
            etLastName.setText("h");
            etAddress.setText("g");
            etCity.setText("m");
            etEmail.setText("t@t.com");
            etRetypeEmail.setText("t@t.com");
            etZipCode.setText("96216");
        }
    }

    private void initWithCaching()
    {
        OTTUserInfoRequest ottUserInfoRequest = TollRoadsApp.getInstance().gCachingOTTRequest;
        etFirstName.setText(ottUserInfoRequest.getAddress_first_name());
        etLastName.setText(ottUserInfoRequest.getAddress_last_name());
        etAddress.setText(ottUserInfoRequest.getAddress1());
        etCity.setText(ottUserInfoRequest.getAddress_city());
        etEmail.setText(ottUserInfoRequest.getEmail_address());
        etRetypeEmail.setText(ottUserInfoRequest.getEmail_address());
        etZipCode.setText(ottUserInfoRequest.getAddress_zipcode());

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

    private void hideAllSoftKeyboard()
    {
        InputMethodManager gImm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        gImm.hideSoftInputFromWindow(etZipCode.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etRetypeEmail.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etEmail.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etCity.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etAddress.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etFirstName.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etLastName.getWindowToken(), 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            if(TollRoadsApp.getInstance().gOTTUserInfoRequest.getCalculate_toll_mode() == Resource.CALCULATE_TOLL_MYSELF) {
                gotoActivity(getApplicationContext(), OTTTripActivity.class,
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
            else
            {
                gotoActivity(getApplicationContext(), OTTCalculateForMeActivity.class,
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
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
                if(TollRoadsApp.getInstance().gOTTUserInfoRequest.getCalculate_toll_mode() == Resource.CALCULATE_TOLL_MYSELF) {
                    gotoActivity(getApplicationContext(), OTTTripActivity.class,
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                }
                else
                {
                    gotoActivity(getApplicationContext(), OTTCalculateForMeActivity.class,
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                }
                //gotoActivity(v.getContext(), OTTVehicleInfoActivity.class, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
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
                        dataAdapter = new ArrayAdapter<String>(OTTContactInfoActivity.this,
                                android.R.layout.simple_spinner_item,
                                getResources().getStringArray(R.array.united_states_arrays));
                    } else {
                        dataAdapter = new ArrayAdapter<String>(OTTContactInfoActivity.this,
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
                if (checkValidation()) {
                    showProgressDialog();

                    String params = populateUserInfoParams();
                    OTTUserInfoRequest(Resource.URL_PAY_GO, params);
                }
//                else
//                {
//                    if(etEmail.getText().length() == 0)
//                    {
//                        showUpConfirmDialog();
//                    }
//                }
                //populateAddRentalParams();
                //gotoActivity(v.getContext(), OTTPaymentInfoActivity.class, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
        });
    }

    private void showUp91FreewayWarning()
    {
        showDialog(getString(R.string.attention), getString(R.string.freeway_warning),
                getString(R.string.ok), null,
                getString(R.string.sign_up_for_fastrak), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        gotoActivity(getApplicationContext(), SignUpAccountInfoActivity.class,
                                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    }
                }, false);
    }

    private void control91FreewayWarning(int locId)
    {
        if(locId == 110)
        {
            showUp91FreewayWarning();
        }
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
                OTTUserInfoRequest(Resource.URL_PAY_GO, params);
            }
        });
        gMultiButtonsPopupDialog.CutomizeNeutralButton(View.GONE, 0, "", null);
        gMultiButtonsPopupDialog.CutomizeNegativeButton(View.VISIBLE, 0, getString(R.string.cancel), null);

        gMultiButtonsPopupDialog.show();
    }

    private String populateRequestParams()
    {
        String params = "";

        if(TollRoadsApp.getInstance().gOTTUserInfoRequest != null) {
            Gson gson = new GsonBuilder().serializeNulls().create();
            String jsonStr = gson.toJson(TollRoadsApp.getInstance().gOTTUserInfoRequest,
                    OTTUserInfoRequest.class);
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
        OTTUserInfoRequest ottUserInfoRequest = TollRoadsApp.getInstance().gOTTUserInfoRequest;

        ottUserInfoRequest.setAddress_first_name(etFirstName.getText().toString());
        ottUserInfoRequest.setAddress_last_name(etLastName.getText().toString());
        ottUserInfoRequest.setAddress1(etAddress.getText().toString());

        ottUserInfoRequest.setAddress_zipcode(etZipCode.getText().toString());
        int countryIndex = spCountry.getSelectedItemPosition();
        String countryAbbr = TollRoadsApp.getCountryString(countryIndex);

        ottUserInfoRequest.setAddress_country(countryAbbr);

        if(spStates.getSelectedItem().toString().equals("US GOVT"))
        {
            ottUserInfoRequest.setAddress_state("US");
        }
        else {
            ottUserInfoRequest.setAddress_state(spStates.getSelectedItem().toString());
        }

        ottUserInfoRequest.setAddress_city(etCity.getText().toString());
        if(etEmail.getText().toString().isEmpty())
        {
            ottUserInfoRequest.setEmail_receipt(false);
        }
        else
        {
            ottUserInfoRequest.setEmail_receipt(true);
        }
        ottUserInfoRequest.setEmail_address(etEmail.getText().toString());
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
        else if(etAddress.getText().length() == 0)
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

    private void gotoTripActivity(double amountDue)
    {
        Bundle b = new Bundle();
        b.putDouble(Resource.KEY_AMOUNT_DUE,  amountDue);
        b.putBoolean("re-add-trips",true);
        gotoActivity(OTTContactInfoActivity.this, OTTTripActivity.class, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT, b);
    }

    private void showUpPreAmountDueDialog(String previousDueMessage, final double amountDue)
    {
        gMultiButtonsPopupDialog = new MultiButtonsPopupDialog(this,R.style.CustomProgressDialog);
        gMultiButtonsPopupDialog.createDialog(this);
        gMultiButtonsPopupDialog.CutomizeTitle(View.GONE, "");

        gMultiButtonsPopupDialog.CutomizeContent(View.VISIBLE, previousDueMessage);
        gMultiButtonsPopupDialog.CutomizeExtraButton(View.GONE, 0, "", 0, null);
        gMultiButtonsPopupDialog.CutomizePositiveButton(View.VISIBLE, 0, getString(R.string.ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoTripActivity(amountDue);
            }
        });
        gMultiButtonsPopupDialog.CutomizeNeutralButton(View.GONE, 0, "", null);
        gMultiButtonsPopupDialog.CutomizeNegativeButton(View.GONE, 0, "", null);

        gMultiButtonsPopupDialog.show();
    }

    private void OTTUserInfoRequest(String url,String params)
    {
        Response.Listener listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, "response:" + response);

                        if(checkResponse(response)) {
                            closeProgressDialog();
                            Gson gson = new GsonBuilder().serializeNulls().create();
                            OTTUserInfoResponse ottUserInfoResponse = gson.fromJson(response, OTTUserInfoResponse.class);

                            if(ottUserInfoResponse != null)
                            {
//                                if(ottUserInfoResponse.getPreviousDueMessage() == null || ottUserInfoResponse.getPreviousDueMessage().isEmpty())
//                                {
//                                    gotoTripActivity(ottUserInfoResponse.getAmountDue());
//                                }
//                                else
//                                {
//                                    showUpPreAmountDueDialog(ottUserInfoResponse.getPreviousDueMessage(),
//                                            ottUserInfoResponse.getAmountDue());
//                                }
                                TollRoadsApp.getInstance().gOTTUserInfoRequest.setCash_amount(ottUserInfoResponse.getCash_amount());
                                gotoActivity(OTTContactInfoActivity.this, OTTPaymentInfoActivity.class,
                                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            }
                        }
                        else
                        {
                            closeProgressDialog();
                        }
                    }
                    else
                    {
                        closeProgressDialog();
                        showToastMessage(getString(R.string.network_error_retry));
                    }
                }
                catch (Exception e)
                {
                    closeProgressDialog();
                    showToastMessage(getString(R.string.network_error_retry));
                }
            }
        };

        ServerDelegate.OTTUserInfoRequest(url, params, listener, errorListener);
////test
//        closeProgressDialog();
//        gotoActivity(OTTContactInfoActivity.this, OTTPaymentInfoActivity.class,
//                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    }

    private void addRentalDatesReq(String url,String params)
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
                            gotoActivity(OTTContactInfoActivity.this, OTTPaymentInfoActivity.class, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
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

        ServerDelegate.addRentalDatesReq(url, params, listener, errorListener);
    }

    private void setTotalCharge()
    {
        if(TollRoadsApp.getInstance().gVehicleFound == Constants.VEHICLE_FOUND_TYPE_RENTAL)
        {
            tvTotalCharge.setVisibility(View.GONE);
        }
        else
        {
            if(TollRoadsApp.getInstance().gOTTUserInfoRequest.getCalculate_toll_mode() == Resource.CALCULATE_TOLL_MYSELF)
            {
                tvTotalCharge.setText(getString(R.string.total_charge, TollRoadsApp.getInstance().ottTotalAmount));
            }
            else
            {
                tvTotalCharge.setText(getString(R.string.total_charge, TollRoadsApp.getInstance().gOTTUserInfoRequest.getTotal_amount()));
            }
            tvTotalCharge.setVisibility(View.VISIBLE);
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
    private void sortLocList(List<LocInfo> mLocList)
    {
        Comparator<LocInfo> comp = new Comparator<LocInfo>() {
            public int compare(LocInfo p1, LocInfo p2) {
                try {
                    if ((p1 == null) || (p2 == null)) {
                        return 0;
                    }
                    else
                    {
                        return p1.getLoc_id() - p2.getLoc_id();
                    }
                }
                catch (Exception exception)
                {
                    return 0;
                }
            }

        };
        Collections.sort(mLocList, comp);
    }

    private DialogInterface.OnClickListener exitListener;

    private boolean isSameDate(String tripInfoDate, String ottTripDate)
    {
        boolean ret = false;
        if(tripInfoDate.equals(ottTripDate))
        {
            ret = true;
        }
        else
        {
            String[] dateSet1 = tripInfoDate.split("/");
            String[] dateSet2 = ottTripDate.split("-");

            if(dateSet1.length == dateSet2.length && dateSet1.length == 3)
            {
                if(dateSet1[2].equals(dateSet2[0]) && dateSet1[0].equals(dateSet2[1])
                        && dateSet1[1].equals(dateSet2[2]))
                {
                    ret = true;
                }
            }
        }
        return ret;
    }

    private boolean isSameTrip(TripInfo tripInfo, OttTrip ottTrip)
    {
        boolean ret = false;
        if(isSameDate(tripInfo.getTrip_date(),ottTrip.getTrip_date())
                && tripInfo.getFrom_loc_id() == ottTrip.getFrom_loc_id()
                && tripInfo.getTo_loc_id() == ottTrip.getTo_loc_id())
        {
            ret = true;
        }
        return ret;
    }
}
