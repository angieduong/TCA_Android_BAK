package com.seta.tollroaddroid.app;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.json.Vehicle;
import com.seta.tollroaddroid.app.utilities.Constants;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

public class NewVehicleActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private Vehicle gVehicle = new Vehicle();
    private TextView tvTitle,tvSave;
    private RelativeLayout layoutDatePicker;
    private DatePicker datePicker;
    private TextView tvDatePickerConfirm,tvDatePickerCancel;
    private TextView tvStartDate,tvEndDate;
    private boolean selectStartDate = true;
    private Response.ErrorListener errorListener;

    private EditText etLicensePlateNo, etConfirmPlateNo;
    private EditText etYear, etMake, etModel;
    private Spinner spCountry,spStates;

	private Switch swRental;
    private TextView tvVehicleDescription;

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit Account_Vehicles_New page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_vehicle);
        FlurryAgent.logEvent("Enter Account_Vehicles_New page.");

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvTitle = (TextView)findViewById(R.id.tv_title);
        etLicensePlateNo = (EditText)findViewById(R.id.et_license_plate_no);
        etConfirmPlateNo = (EditText)findViewById(R.id.et_confirm_plate_no);
        tvSave = (TextView)findViewById(R.id.tv_save);

        tvStartDate = (TextView)findViewById(R.id.tv_start_date);
        tvEndDate = (TextView)findViewById(R.id.tv_end_date);

        etYear = (EditText)findViewById(R.id.et_year);
        etMake = (EditText)findViewById(R.id.et_make);
        etModel = (EditText)findViewById(R.id.et_model);
        spCountry = (Spinner)findViewById(R.id.country_spinner);
        spStates = (Spinner)findViewById(R.id.state_spinner);

        layoutDatePicker = (RelativeLayout)findViewById(R.id.layout_datePicker);
        datePicker = (DatePicker)findViewById(R.id.datePicker);
        tvDatePickerConfirm = (TextView)findViewById(R.id.tv_datePicker_ok);
        tvDatePickerCancel = (TextView)findViewById(R.id.tv_datePicker_cancel);
        swRental = (Switch)findViewById(R.id.sw_rental);

        initWidgetValue();
        setupListener();

        if(savedInstanceState == null)
        {
            Calendar c = Calendar.getInstance();
            String dateStr = c.get(Calendar.YEAR) + "-" + String.format("%02d", c.get(Calendar.MONTH) + 1)
                    + "-" + String.format("%02d", c.get(Calendar.DAY_OF_MONTH));
            tvStartDate.setTag(dateStr);
            tvStartDate.setText(TollRoadsApp.formatDateBySystem(c.getTimeInMillis()));
        }
    }

    private void hideAllSoftKeyboard()
    {
        InputMethodManager gImm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        gImm.hideSoftInputFromWindow(etConfirmPlateNo.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etLicensePlateNo.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etMake.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etModel.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etYear.getWindowToken(), 0);
    }

    private void setupListener()
    {
        layoutDatePicker.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (layoutDatePicker.getVisibility() == View.VISIBLE) {
                    hideAllSoftKeyboard();
                }
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
        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
                    //rlState.setBackgroundColor(Color.WHITE);
                    rlState.setBackgroundResource(R.drawable.vehicle_state_spinner_active_bg);
                    spStates.setEnabled(true);
                    ArrayAdapter<String> dataAdapter;
                    if (position == 0) {
                        dataAdapter = new ArrayAdapter<String>(NewVehicleActivity.this,
                                android.R.layout.simple_spinner_item,
                                getResources().getStringArray(R.array.united_states_arrays));
                    } else {
                        dataAdapter = new ArrayAdapter<String>(NewVehicleActivity.this,
                                android.R.layout.simple_spinner_item,
                                getResources().getStringArray(R.array.canada_provinces_arrays));
                    }
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    // attaching data adapter to spinner
                    spStates.setAdapter(dataAdapter);
                }
                else {
                    //rlState.setBackgroundColor(getResources().getColor(R.color.colorBgGrayLight));
                    rlState.setBackgroundResource(R.drawable.vehicle_state_spinner_inactive_bg);
                    spStates.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        tvStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStartDate = true;

                //String dateStr = tvStartDate.getText().toString();
                String dateStr = (String)tvStartDate.getTag();
                if(dateStr == null || dateStr.isEmpty())
                {
                    Calendar c = Calendar.getInstance();

                    // set current date into date picker
                    datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), null);
                }
                else
                {
                    String[] dateArray = dateStr.split("-");
                    int year = Integer.valueOf(dateArray[0]);
                    int month = Integer.valueOf(dateArray[1])-1;
                    int day = Integer.valueOf(dateArray[2]);
                    datePicker.init(year, month, day, null);
                }
                layoutDatePicker.setVisibility(View.VISIBLE);
            }
        });
        tvEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStartDate = false;

                //String dateStr = tvEndDate.getText().toString();
                String dateStr = (String)tvEndDate.getTag();
                if(dateStr == null || dateStr.isEmpty())
                {
                    Calendar c = Calendar.getInstance();

                    // set current date into date picker
                    datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), null);
                } else {
                    String[] dateArray = dateStr.split("-");
                    int year = Integer.valueOf(dateArray[0]);
                    int month = Integer.valueOf(dateArray[1]) - 1;
                    int day = Integer.valueOf(dateArray[2]);
                    datePicker.init(year, month, day, null);
                }
                layoutDatePicker.setVisibility(View.VISIBLE);
            }
        });
        layoutDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutDatePicker.setVisibility(View.GONE);
            }
        });
        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                return;
            }
        });
        tvDatePickerConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dateStr = datePicker.getYear() + "-" + String.format("%02d", datePicker.getMonth() + 1) + "-" + String.format("%02d", datePicker.getDayOfMonth());
                Calendar calendar = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());

                String systemFormat = TollRoadsApp.formatDateBySystem(calendar.getTimeInMillis());

                if (selectStartDate) {
                    tvStartDate.setText(systemFormat);
                    tvStartDate.setTag(dateStr);
                } else {
                    tvEndDate.setText(systemFormat);
                    tvEndDate.setTag(dateStr);
                }
                layoutDatePicker.setVisibility(View.GONE);
                tvSave.setVisibility(View.VISIBLE);
            }
        });
        tvDatePickerCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutDatePicker.setVisibility(View.GONE);
            }
        });

        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation()) {
                    setRequest();
                    showProgressDialog();
                    String params = populateParams();

                    addVehicleRequest(Resource.URL_VEHICLE, params);
                }
            }
        });
    }

    private void initWidgetValue()
    {
        setDescription();
    }

    private void setRequest()
    {
        gVehicle.setPlate(etLicensePlateNo.getText().toString());
        gVehicle.setYear(Integer.valueOf(etYear.getText().toString()));
        gVehicle.setMake(etMake.getText().toString());
        gVehicle.setModel(etModel.getText().toString());

        String startDate = (String)tvStartDate.getTag();
        if(startDate == null)
        {
            gVehicle.setStart_date("");
        }
        else
        {
            gVehicle.setStart_date(startDate);
        }

        String endDate = (String)tvEndDate.getTag();
        if(endDate == null)
        {
            gVehicle.setEnd_date("");
        }
        else
        {
            gVehicle.setEnd_date(endDate);
        }

        if(swRental.isChecked())
        {
            gVehicle.setVehicle_type(Constants.VEHICLE_TYPE_RENTAL);
        }
        else
        {
            gVehicle.setVehicle_type(Constants.VEHICLE_TYPE_INDIVIDUAL);
        }

        int countryIndex = spCountry.getSelectedItemPosition();
        String countryAbbr = TollRoadsApp.getCountryString(countryIndex);
        gVehicle.setCountry(countryAbbr);

        if(spStates.getSelectedItem().toString().equals("US GOVT"))
        {
            gVehicle.setState("US");
        }
        else {
            gVehicle.setState(spStates.getSelectedItem().toString());
        }

        gVehicle.setVehicle_id("");
    }

    private void setDescription()
    {
        String description;
        if(TollRoadsApp.getInstance().isFasTrak())
        {
            description = getString(R.string.all_account_sign_up_vehicle_description)+
                    " "+ getString(R.string.fastrak_sign_up_vehicle_description);
        }
        else
        {
            description = getString(R.string.all_account_sign_up_vehicle_description);
        }
        tvVehicleDescription = (TextView)findViewById(R.id.tv_vehicle_description);
        tvVehicleDescription.setText(description);
    }
    
    private boolean checkValidation()
    {
        boolean ret = true;

        if(etLicensePlateNo.getText().length() == 0 && !TollRoadsApp.getInstance().isFasTrak())
        {
            ret = false;
            showToastMessage(getString(R.string.license_plate_empty_warning));
        }
        else if(!etLicensePlateNo.getText().toString().equals(etConfirmPlateNo.getText().toString())) {
            ret = false;
            showToastMessage(getString(R.string.license_plate_not_match_warning));
        }
        else if(etYear.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.year_empty_warning));
        }
        else if(etMake.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.make_empty_warning));
        }
        else if(etModel.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.model_empty_warning));
        }
        else if(tvStartDate.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.start_date_empty_warning));
        }
        else
        {
            if(swRental.isChecked() && tvEndDate.getText().length() == 0)
            {
                ret = false;
                showToastMessage(getString(R.string.end_date_empty_warning));
            }

//            if(tvEndDate.getText().length() != 0) {
//                long startTimeStamp = TollRoadsApp.getTimeStampFromDateString(tvStartDate.getText().toString());
//                long endTimeStamp = TollRoadsApp.getTimeStampFromDateString(tvEndDate.getText().toString());
//                long todayTimeStamp = TollRoadsApp.getTodayTimeStamp();
//
//                if(endTimeStamp < startTimeStamp )
//                {
//                    ret = false;
//                    showToastMessage(getString(R.string.end_date_warning1));
//                }
//                else if(endTimeStamp < todayTimeStamp)
//                {
//                    ret = false;
//                    showToastMessage(getString(R.string.end_date_warning2));
//                }
//                else if(swRental.isChecked() && (endTimeStamp -startTimeStamp) > Constants.MAX_INTERVAL_FOR_RENTAL_VEHICLE)  //max 7 days include today
//                {
//                    ret = false;
//                    showToastMessage(getString(R.string.end_date_warning3));
//                }
//            }
        }

        return ret;
    }

    private String populateParams()
    {
        String params = "";

        if(gVehicle != null) {
            Gson gson = new GsonBuilder().serializeNulls().create();
            String jsonStr = gson.toJson(gVehicle,
                    Vehicle.class);
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

    private void addVehicleRequest(String url,String params)
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

        ServerDelegate.addRequest(url, params, listener, errorListener);
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
