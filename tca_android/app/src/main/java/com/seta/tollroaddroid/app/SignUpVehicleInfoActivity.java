package com.seta.tollroaddroid.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

public class SignUpVehicleInfoActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private TextView tvGoNext;
    private RelativeLayout layoutDatePicker;
    private DatePicker datePicker;
    
    private TextView tvDatePickerConfirm,tvDatePickerCancel;
    private LinearLayout llAddAnotherVehicle;

    private LinearLayout llVehicle1,llVehicle2,llVehicle3,llVehicle4,llVehicle5;
    private Spinner spCountry1,spStates1;
    private Spinner spCountry2,spStates2;
    private Spinner spCountry3,spStates3;
    private Spinner spCountry4,spStates4;
    private Spinner spCountry5,spStates5;
    private TextView tvVehicleIndex1,tvVehicleIndex2,tvVehicleIndex3,tvVehicleIndex4,tvVehicleIndex5;
    private TextView tvStartDate1,tvStartDate2,tvStartDate3,tvStartDate4,tvStartDate5;
    private TextView tvEndDate1,tvEndDate2,tvEndDate3,tvEndDate4,tvEndDate5;
    private EditText etPlate1,etPlate2,etPlate3,etPlate4,etPlate5;
    private EditText etPlateConfirm1,etPlateConfirm2,etPlateConfirm3,etPlateConfirm4,etPlateConfirm5;
    private EditText etYear1,etYear2,etYear3,etYear4,etYear5;
    private EditText etMake1,etMake2,etMake3,etMake4,etMake5;
    private EditText etModel1,etModel2,etModel3,etModel4,etModel5;
    private Switch swRental1,swRental2,swRental3,swRental4,swRental5;
    private ImageView ivRemove1, ivRemove2, ivRemove3, ivRemove4, ivRemove5;

    private boolean selectStartDate = true;
    private int selectDateIndex = 0;
    private int vehicleCount = 1;
    private SignUpRequest signUpRequest = TollRoadsApp.getInstance().gSignUpRequest;
    private TextView tvVehicleDescription;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("CurrentSignUpReq", TollRoadsApp.getInstance().gSignUpRequest);
        outState.putBoolean(Constants.FROM_OTT, TollRoadsApp.getInstance().gFromOTT);
    }

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit SignUp_3_Vehicle_"+TollRoadsApp.getInstance().getSignUpAccountType()+" page.");
        super.onDestroy();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_vehicle_info);
        if(savedInstanceState != null)
        {
            TollRoadsApp.getInstance().gSignUpRequest =
                    (SignUpRequest) savedInstanceState.getSerializable("CurrentSignUpReq");
            TollRoadsApp.getInstance().gFromOTT =
                    savedInstanceState.getBoolean(Constants.FROM_OTT,false);
        }
        FlurryAgent.logEvent("Enter SignUp_3_Vehicle_"+TollRoadsApp.getInstance().getSignUpAccountType()+" page.");

        signUpRequest = TollRoadsApp.getInstance().gSignUpRequest;

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvGoNext = (TextView) findViewById(R.id.tv_go_next);
        layoutDatePicker = (RelativeLayout)findViewById(R.id.layout_datePicker);
        datePicker = (DatePicker)findViewById(R.id.datePicker);
        llAddAnotherVehicle = (LinearLayout)findViewById(R.id.ll_add_another_vehicle);

        llVehicle1 = (LinearLayout)findViewById(R.id.ll_vehicle1);
        llVehicle2 = (LinearLayout)findViewById(R.id.ll_vehicle2);
        llVehicle3 = (LinearLayout)findViewById(R.id.ll_vehicle3);
        llVehicle4 = (LinearLayout)findViewById(R.id.ll_vehicle4);
        llVehicle5 = (LinearLayout)findViewById(R.id.ll_vehicle5);
        
        tvDatePickerConfirm = (TextView)findViewById(R.id.tv_datePicker_ok);
        tvDatePickerCancel = (TextView)findViewById(R.id.tv_datePicker_cancel);

        tvVehicleDescription = (TextView)findViewById(R.id.tv_vehicle_description);

        initWidgetValue();
        setupListener();

        if(savedInstanceState == null)
        {
            Calendar c = Calendar.getInstance();
            String today = TollRoadsApp.formatDateBySystem(c.getTimeInMillis());

            String dateStr = c.get(Calendar.YEAR) + "-" + String.format("%02d", c.get(Calendar.MONTH) + 1)
                    + "-" + String.format("%02d", c.get(Calendar.DAY_OF_MONTH));
            tvStartDate1.setTag(dateStr);
            tvStartDate2.setTag(dateStr);
            tvStartDate3.setTag(dateStr);
            tvStartDate4.setTag(dateStr);
            tvStartDate5.setTag(dateStr);
            if(!TollRoadsApp.getInstance().gFromOTT)
            {
                tvStartDate1.setText(today);
            }
            tvStartDate2.setText(today);
            tvStartDate3.setText(today);
            tvStartDate4.setText(today);
            tvStartDate5.setText(today);
        }
    }

    private void initVehicle1()
    {
        spCountry1 = (Spinner)llVehicle1.findViewById(R.id.country_spinner);
        spStates1 = (Spinner)llVehicle1.findViewById(R.id.state_spinner);
        tvVehicleIndex1 = (TextView)llVehicle1.findViewById(R.id.tv_vehicle_index);
        etPlate1 = (EditText)llVehicle1.findViewById(R.id.et_license_plate_no);
        etPlateConfirm1 = (EditText)llVehicle1.findViewById(R.id.et_confirm_plate_no);
        etYear1 = (EditText)llVehicle1.findViewById(R.id.et_year);
        etMake1 = (EditText)llVehicle1.findViewById(R.id.et_make);
        etModel1 = (EditText)llVehicle1.findViewById(R.id.et_model);
        swRental1 = (Switch)llVehicle1.findViewById(R.id.sw_rental);
        tvStartDate1 = (TextView)llVehicle1.findViewById(R.id.tv_start_date);
        tvEndDate1 = (TextView)llVehicle1.findViewById(R.id.tv_end_date);

        tvStartDate1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStartDate = true;
                selectDateIndex = 1;
                String dateStr = (String)tvStartDate1.getTag();
                if(dateStr == null || dateStr.isEmpty())
                {
                    Calendar c = Calendar.getInstance();

                    // set current date into datepicker
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
        tvEndDate1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStartDate = false;
                selectDateIndex = 1;
                String dateStr = (String)tvEndDate1.getTag();
                if(dateStr == null || dateStr.isEmpty()){
                    Calendar c = Calendar.getInstance();

                    // set current date into datepicker
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
        tvVehicleIndex1.setText(getString(R.string.vehicle_index, 1));
        
        ivRemove1 = (ImageView)llVehicle1.findViewById(R.id.iv_remove);
        ivRemove1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vehicleCount > 1) {
                    if (llVehicle2.getVisibility() == View.VISIBLE) {
                        copyLayout(llVehicle2, llVehicle1);
                    }
                    if (llVehicle3.getVisibility() == View.VISIBLE) {
                        copyLayout(llVehicle3, llVehicle2);
                    }
                    if (llVehicle4.getVisibility() == View.VISIBLE) {
                        copyLayout(llVehicle4, llVehicle3);
                    }
                    if (llVehicle5.getVisibility() == View.VISIBLE) {
                        copyLayout(llVehicle5, llVehicle4);
                    }

                    hideLastLayout();
                    vehicleCount--;
                }
                if (vehicleCount <= 1) {
                    hideRemoveImage();
                }
                llAddAnotherVehicle.setVisibility(View.VISIBLE);
            }
        });

        if(TollRoadsApp.getInstance().gFromOTT)
        {
            OTTUserInfoRequest ottUserInfoRequest = TollRoadsApp.getInstance().gOTTUserInfoRequest;
            etPlate1.setText(ottUserInfoRequest.getPlate1());
            etPlateConfirm1.setText(ottUserInfoRequest.getPlate1());

            int countryIndex = TollRoadsApp.getCountryIndex(this,
                    ottUserInfoRequest.getCountry1(),
                    ottUserInfoRequest.getState1());
            if(countryIndex >= spCountry1.getCount())
            {
                countryIndex = spCountry1.getCount() - 1;
            }
            spCountry1.setSelection(countryIndex);

            int stateIndex = TollRoadsApp.getStateIndex(this,
                    ottUserInfoRequest.getCountry1(),
                    ottUserInfoRequest.getState1());
            if(stateIndex >= spStates1.getCount())
            {
                stateIndex = spStates1.getCount() - 1;
            }
            spStates1.setSelection(stateIndex);
        }

    }
    
    private void copyLayout(LinearLayout src, LinearLayout dest)
    {
        Spinner spCountrySrc = (Spinner)src.findViewById(R.id.country_spinner);
        Spinner spStatesSrc = (Spinner)src.findViewById(R.id.state_spinner);
        EditText etPlateSrc = (EditText)src.findViewById(R.id.et_license_plate_no);
        EditText etPlateConfirmSrc = (EditText)src.findViewById(R.id.et_confirm_plate_no);
        EditText etYearSrc = (EditText)src.findViewById(R.id.et_year);
        EditText etMakeSrc = (EditText)src.findViewById(R.id.et_make);
        EditText etModelSrc = (EditText)src.findViewById(R.id.et_model);
        Switch swRentalSrc = (Switch)src.findViewById(R.id.sw_rental);
        TextView tvStartDateSrc = (TextView)src.findViewById(R.id.tv_start_date);
        TextView tvEndDateSrc = (TextView)src.findViewById(R.id.tv_end_date);

        Spinner spCountryDest = (Spinner)dest.findViewById(R.id.country_spinner);
        Spinner spStatesDest = (Spinner)dest.findViewById(R.id.state_spinner);
        EditText etPlateDest = (EditText)dest.findViewById(R.id.et_license_plate_no);
        EditText etPlateConfirmDest = (EditText)dest.findViewById(R.id.et_confirm_plate_no);
        EditText etYearDest = (EditText)dest.findViewById(R.id.et_year);
        EditText etMakeDest = (EditText)dest.findViewById(R.id.et_make);
        EditText etModelDest = (EditText)dest.findViewById(R.id.et_model);
        Switch swRentalDest = (Switch)dest.findViewById(R.id.sw_rental);
        TextView tvStartDateDest = (TextView)dest.findViewById(R.id.tv_start_date);
        TextView tvEndDateDest = (TextView)dest.findViewById(R.id.tv_end_date);

        spCountryDest.setSelection(spCountrySrc.getSelectedItemPosition());
        spStatesDest.setSelection(spStatesSrc.getSelectedItemPosition());

        etPlateDest.setText(etPlateSrc.getText());
        etPlateConfirmDest.setText(etPlateConfirmSrc.getText());
        etYearDest.setText(etYearSrc.getText());
        etMakeDest.setText(etMakeSrc.getText());
        etModelDest.setText(etModelSrc.getText());

        swRentalDest.setChecked(swRentalSrc.isChecked());
        tvStartDateDest.setText(tvStartDateSrc.getText());
        tvEndDateDest.setText(tvEndDateSrc.getText());
    }
    
    private void hideLastLayout()
    {
        if(llVehicle5.getVisibility() == View.VISIBLE)
        {
            reInitLayout(llVehicle5);
            llVehicle5.setVisibility(View.GONE);
        }
        else if(llVehicle4.getVisibility() == View.VISIBLE)
        {
            reInitLayout(llVehicle4);
            llVehicle4.setVisibility(View.GONE);
        }
        else if(llVehicle3.getVisibility() == View.VISIBLE)
        {
            reInitLayout(llVehicle3);
            llVehicle3.setVisibility(View.GONE);
        }
        else if(llVehicle2.getVisibility() == View.VISIBLE)
        {
            reInitLayout(llVehicle2);
            llVehicle2.setVisibility(View.GONE);
        }
    }

    private void reInitLayout(LinearLayout linearLayout)
    {
        Spinner spCountryDest = (Spinner)linearLayout.findViewById(R.id.country_spinner);
        Spinner spStatesDest = (Spinner)linearLayout.findViewById(R.id.state_spinner);
        EditText etPlateDest = (EditText)linearLayout.findViewById(R.id.et_license_plate_no);
        EditText etPlateConfirmDest = (EditText)linearLayout.findViewById(R.id.et_confirm_plate_no);
        EditText etYearDest = (EditText)linearLayout.findViewById(R.id.et_year);
        EditText etMakeDest = (EditText)linearLayout.findViewById(R.id.et_make);
        EditText etModelDest = (EditText)linearLayout.findViewById(R.id.et_model);
        Switch swRentalDest = (Switch)linearLayout.findViewById(R.id.sw_rental);
        TextView tvStartDateDest = (TextView)linearLayout.findViewById(R.id.tv_start_date);
        TextView tvEndDateDest = (TextView)linearLayout.findViewById(R.id.tv_end_date);

        spCountryDest.setSelection(0);
        spStatesDest.setSelection(0);

        etPlateDest.setText("");
        etPlateConfirmDest.setText("");
        etYearDest.setText("");
        etMakeDest.setText("");
        etModelDest.setText("");

        swRentalDest.setChecked(false);
        tvStartDateDest.setText("");
        tvEndDateDest.setText("");
    }
    private void hideRemoveImage()
    {
        ivRemove1.setVisibility(View.GONE);
        ivRemove2.setVisibility(View.GONE);
        ivRemove3.setVisibility(View.GONE);
        ivRemove4.setVisibility(View.GONE);
        ivRemove5.setVisibility(View.GONE);
    }
    private void initVehicle2()
    {
        spCountry2 = (Spinner)llVehicle2.findViewById(R.id.country_spinner);
        spStates2 = (Spinner)llVehicle2.findViewById(R.id.state_spinner);
        tvVehicleIndex2 = (TextView)llVehicle2.findViewById(R.id.tv_vehicle_index);
        etPlate2 = (EditText)llVehicle2.findViewById(R.id.et_license_plate_no);
        etPlateConfirm2 = (EditText)llVehicle2.findViewById(R.id.et_confirm_plate_no);
        etYear2 = (EditText)llVehicle2.findViewById(R.id.et_year);
        etMake2 = (EditText)llVehicle2.findViewById(R.id.et_make);
        etModel2 = (EditText)llVehicle2.findViewById(R.id.et_model);
        swRental2 = (Switch)llVehicle2.findViewById(R.id.sw_rental);
        tvStartDate2 = (TextView)llVehicle2.findViewById(R.id.tv_start_date);
        tvEndDate2 = (TextView)llVehicle2.findViewById(R.id.tv_end_date);
        tvStartDate2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStartDate = true;
                selectDateIndex = 2;
                String dateStr = (String)tvStartDate2.getTag();
                if(dateStr == null || dateStr.isEmpty())
                {
                    Calendar c = Calendar.getInstance();

                    // set current date into datepicker
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
        tvEndDate2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStartDate = false;
                selectDateIndex = 2;
                String dateStr = (String)tvEndDate2.getTag();
                if(dateStr == null || dateStr.isEmpty()){
                    Calendar c = Calendar.getInstance();

                    // set current date into datepicker
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
        tvVehicleIndex2.setText(getString(R.string.vehicle_index, 2));

        ivRemove2 = (ImageView)llVehicle2.findViewById(R.id.iv_remove);
        ivRemove2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vehicleCount > 1) {
                    if (llVehicle3.getVisibility() == View.VISIBLE) {
                        copyLayout(llVehicle3, llVehicle2);
                    }
                    if (llVehicle4.getVisibility() == View.VISIBLE) {
                        copyLayout(llVehicle4, llVehicle3);
                    }
                    if (llVehicle5.getVisibility() == View.VISIBLE) {
                        copyLayout(llVehicle5, llVehicle4);
                    }

                    hideLastLayout();
                    vehicleCount--;
                }
                if (vehicleCount <= 1) {
                    hideRemoveImage();
                }
                llAddAnotherVehicle.setVisibility(View.VISIBLE);
            }
        });

    }
    private void initVehicle3()
    {
        spCountry3 = (Spinner)llVehicle3.findViewById(R.id.country_spinner);
        spStates3 = (Spinner)llVehicle3.findViewById(R.id.state_spinner);
        tvVehicleIndex3 = (TextView)llVehicle3.findViewById(R.id.tv_vehicle_index);
        etPlate3 = (EditText)llVehicle3.findViewById(R.id.et_license_plate_no);
        etPlateConfirm3 = (EditText)llVehicle3.findViewById(R.id.et_confirm_plate_no);
        etYear3 = (EditText)llVehicle3.findViewById(R.id.et_year);
        etMake3 = (EditText)llVehicle3.findViewById(R.id.et_make);
        etModel3 = (EditText)llVehicle3.findViewById(R.id.et_model);
        swRental3 = (Switch)llVehicle3.findViewById(R.id.sw_rental);
        tvStartDate3 = (TextView)llVehicle3.findViewById(R.id.tv_start_date);
        tvEndDate3 = (TextView)llVehicle3.findViewById(R.id.tv_end_date);
        tvStartDate3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStartDate = true;
                selectDateIndex = 3;
                String dateStr = (String)tvStartDate3.getTag();
                if(dateStr == null || dateStr.isEmpty())
                {
                    Calendar c = Calendar.getInstance();

                    // set current date into datepicker
                    datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), null);
                } else {
                    String[] dateArray = dateStr.split("-");
                    int year = Integer.valueOf(dateArray[0]);
                    int month = Integer.valueOf(dateArray[1])-1;
                    int day = Integer.valueOf(dateArray[2]);
                    datePicker.init(year, month, day, null);
                }
                layoutDatePicker.setVisibility(View.VISIBLE);
            }
        });
        tvEndDate3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStartDate = false;
                selectDateIndex = 3;
                String dateStr = (String)tvEndDate3.getTag();
                if(dateStr == null || dateStr.isEmpty()){
                    Calendar c = Calendar.getInstance();

                    // set current date into datepicker
                    datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), null);
                } else {
                    String[] dateArray = dateStr.split("-");
                    int year = Integer.valueOf(dateArray[0]);
                    int month = Integer.valueOf(dateArray[1])-1;
                    int day = Integer.valueOf(dateArray[2]);
                    datePicker.init(year, month, day, null);
                }
                layoutDatePicker.setVisibility(View.VISIBLE);
            }
        });
        tvVehicleIndex3.setText(getString(R.string.vehicle_index, 3));

        ivRemove3 = (ImageView)llVehicle3.findViewById(R.id.iv_remove);
        ivRemove3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vehicleCount > 1) {
                    if (llVehicle4.getVisibility() == View.VISIBLE) {
                        copyLayout(llVehicle4, llVehicle3);
                    }
                    if (llVehicle5.getVisibility() == View.VISIBLE) {
                        copyLayout(llVehicle5, llVehicle4);
                    }

                    hideLastLayout();
                    vehicleCount--;
                }
                if (vehicleCount <= 1) {
                    hideRemoveImage();
                }
                llAddAnotherVehicle.setVisibility(View.VISIBLE);
            }
        });
    }
    private void initVehicle4()
    {
        spCountry4 = (Spinner)llVehicle4.findViewById(R.id.country_spinner);
        spStates4 = (Spinner)llVehicle4.findViewById(R.id.state_spinner);
        tvVehicleIndex4 = (TextView)llVehicle4.findViewById(R.id.tv_vehicle_index);
        etPlate4 = (EditText)llVehicle4.findViewById(R.id.et_license_plate_no);
        etPlateConfirm4 = (EditText)llVehicle4.findViewById(R.id.et_confirm_plate_no);
        etYear4 = (EditText)llVehicle4.findViewById(R.id.et_year);
        etMake4 = (EditText)llVehicle4.findViewById(R.id.et_make);
        etModel4 = (EditText)llVehicle4.findViewById(R.id.et_model);
        swRental4 = (Switch)llVehicle4.findViewById(R.id.sw_rental);
        tvStartDate4 = (TextView)llVehicle4.findViewById(R.id.tv_start_date);
        tvEndDate4 = (TextView)llVehicle4.findViewById(R.id.tv_end_date);
        tvStartDate4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStartDate = true;
                selectDateIndex = 4;
                String dateStr = (String)tvStartDate4.getTag();
                if(dateStr == null || dateStr.isEmpty())
                {
                    Calendar c = Calendar.getInstance();

                    // set current date into datepicker
                    datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), null);
                } else {
                    String[] dateArray = dateStr.split("-");
                    int year = Integer.valueOf(dateArray[0]);
                    int month = Integer.valueOf(dateArray[1])-1;
                    int day = Integer.valueOf(dateArray[2]);
                    datePicker.init(year, month, day, null);
                }
                layoutDatePicker.setVisibility(View.VISIBLE);
            }
        });
        tvEndDate4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStartDate = false;
                selectDateIndex = 4;
                String dateStr = (String)tvEndDate4.getTag();
                if(dateStr == null || dateStr.isEmpty()){
                    Calendar c = Calendar.getInstance();

                    // set current date into datepicker
                    datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), null);
                } else {
                    String[] dateArray = dateStr.split("-");
                    int year = Integer.valueOf(dateArray[0]);
                    int month = Integer.valueOf(dateArray[1])-1;
                    int day = Integer.valueOf(dateArray[2]);
                    datePicker.init(year, month, day, null);
                }
                layoutDatePicker.setVisibility(View.VISIBLE);
            }
        });
        tvVehicleIndex4.setText(getString(R.string.vehicle_index, 4));

        ivRemove4 = (ImageView)llVehicle4.findViewById(R.id.iv_remove);
        ivRemove4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vehicleCount > 1) {
                    if (llVehicle5.getVisibility() == View.VISIBLE) {
                        copyLayout(llVehicle5, llVehicle4);
                    }

                    hideLastLayout();
                    vehicleCount--;
                }
                if (vehicleCount <= 1) {
                    hideRemoveImage();
                }
                llAddAnotherVehicle.setVisibility(View.VISIBLE);
            }
        });
    }
    private void initVehicle5()
    {
        spCountry5 = (Spinner)llVehicle5.findViewById(R.id.country_spinner);
        spStates5 = (Spinner)llVehicle5.findViewById(R.id.state_spinner);
        tvVehicleIndex5 = (TextView)llVehicle5.findViewById(R.id.tv_vehicle_index);
        etPlate5 = (EditText)llVehicle5.findViewById(R.id.et_license_plate_no);
        etPlateConfirm5 = (EditText)llVehicle5.findViewById(R.id.et_confirm_plate_no);
        etYear5 = (EditText)llVehicle5.findViewById(R.id.et_year);
        etMake5 = (EditText)llVehicle5.findViewById(R.id.et_make);
        etModel5 = (EditText)llVehicle5.findViewById(R.id.et_model);
        swRental5 = (Switch)llVehicle5.findViewById(R.id.sw_rental);
        tvStartDate5 = (TextView)llVehicle5.findViewById(R.id.tv_start_date);
        tvEndDate5 = (TextView)llVehicle5.findViewById(R.id.tv_end_date);
        tvStartDate5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStartDate = true;
                selectDateIndex = 5;
                String dateStr = (String)tvStartDate5.getTag();
                if(dateStr == null || dateStr.isEmpty())
                {
                    Calendar c = Calendar.getInstance();

                    // set current date into datepicker
                    datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), null);
                } else {
                    String[] dateArray = dateStr.split("-");
                    int year = Integer.valueOf(dateArray[0]);
                    int month = Integer.valueOf(dateArray[1])-1;
                    int day = Integer.valueOf(dateArray[2]);
                    datePicker.init(year, month, day, null);
                }
                layoutDatePicker.setVisibility(View.VISIBLE);
            }
        });
        tvEndDate5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStartDate = false;
                selectDateIndex = 5;
                String dateStr = (String)tvEndDate5.getTag();
                if(dateStr == null || dateStr.isEmpty())
                {
                    Calendar c = Calendar.getInstance();

                    // set current date into datepicker
                    datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), null);
                } else {
                    String[] dateArray = dateStr.split("-");
                    int year = Integer.valueOf(dateArray[0]);
                    int month = Integer.valueOf(dateArray[1])-1;
                    int day = Integer.valueOf(dateArray[2]);
                    datePicker.init(year, month, day, null);
                }
                layoutDatePicker.setVisibility(View.VISIBLE);
            }
        });
        tvVehicleIndex5.setText(getString(R.string.vehicle_index, 5));

        ivRemove5 = (ImageView)llVehicle5.findViewById(R.id.iv_remove);
        ivRemove5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vehicleCount > 1) {
                    hideLastLayout();
                    vehicleCount--;
                }
                if (vehicleCount <= 1) {
                    hideRemoveImage();
                }
                llAddAnotherVehicle.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            gotoActivity(this, SignUpContactInfoActivity.class, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    private void hideVehicle1SoftKeyboard()
    {
        InputMethodManager gImm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        gImm.hideSoftInputFromWindow(etPlate1.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etPlateConfirm1.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etYear1.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etMake1.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etModel1.getWindowToken(), 0);
    }

    private void hideVehicle2SoftKeyboard()
    {
        InputMethodManager gImm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        gImm.hideSoftInputFromWindow(etPlate2.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etPlateConfirm2.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etYear2.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etMake2.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etModel2.getWindowToken(), 0);
    }
    private void hideVehicle3SoftKeyboard()
    {
        InputMethodManager gImm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        gImm.hideSoftInputFromWindow(etPlate3.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etPlateConfirm3.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etYear3.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etMake3.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etModel3.getWindowToken(), 0);
    }
    private void hideVehicle4SoftKeyboard()
    {
        InputMethodManager gImm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        gImm.hideSoftInputFromWindow(etPlate4.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etPlateConfirm4.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etYear4.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etMake4.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etModel4.getWindowToken(), 0);
    }
    private void hideVehicle5SoftKeyboard()
    {
        InputMethodManager gImm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        gImm.hideSoftInputFromWindow(etPlate5.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etPlateConfirm5.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etYear5.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etMake5.getWindowToken(), 0);
        gImm.hideSoftInputFromWindow(etModel5.getWindowToken(), 0);
    }

    private void hideAllSoftKeyboard()
    {
        hideVehicle1SoftKeyboard();
        hideVehicle2SoftKeyboard();
        hideVehicle3SoftKeyboard();
        hideVehicle4SoftKeyboard();
        hideVehicle5SoftKeyboard();
    }
    
    private void setupListener()
    {
        llAddAnotherVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vehicleCount < 5)
                {
                    if(llVehicle2.getVisibility() != View.VISIBLE)
                    {
                        llVehicle2.setVisibility(View.VISIBLE);
                    }
                    else if(llVehicle3.getVisibility() != View.VISIBLE)
                    {
                        llVehicle3.setVisibility(View.VISIBLE);
                    }
                    else if(llVehicle4.getVisibility() != View.VISIBLE)
                    {
                        llVehicle4.setVisibility(View.VISIBLE);
                    }
                    else if(llVehicle5.getVisibility() != View.VISIBLE)
                    {
                        llVehicle5.setVisibility(View.VISIBLE);
                        llAddAnotherVehicle.setVisibility(View.GONE);
                    }
                    vehicleCount++;
                }
                else
                {
                    llAddAnotherVehicle.setVisibility(View.GONE);
                }

                ivRemove1.setVisibility(View.VISIBLE);
                ivRemove2.setVisibility(View.VISIBLE);
                ivRemove3.setVisibility(View.VISIBLE);
                ivRemove4.setVisibility(View.VISIBLE);
                ivRemove5.setVisibility(View.VISIBLE);
            }
        });

        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(v.getContext(), SignUpContactInfoActivity.class, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
        });
        layoutDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutDatePicker.setVisibility(View.GONE);
            }
        });
        layoutDatePicker.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (layoutDatePicker.getVisibility() == View.VISIBLE) {
                    hideAllSoftKeyboard();
                }
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
                String dateStr = datePicker.getYear()+"-"+String.format("%02d", datePicker.getMonth()+1)+ "-" + String.format("%02d", datePicker.getDayOfMonth());
                Calendar calendar = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());

                String systemFormat = TollRoadsApp.formatDateBySystem(calendar.getTimeInMillis());

                if(selectStartDate)
                {
                    if(selectDateIndex == 1)
                    {
                        tvStartDate1.setTag(dateStr);

                        tvStartDate1.setText(systemFormat);
                        signUpRequest.setStart_date1(dateStr);
                    }
                    else if(selectDateIndex == 2)
                    {
                        tvStartDate2.setTag(dateStr);

                        tvStartDate2.setText(systemFormat);
                        signUpRequest.setStart_date2(dateStr);
                    }
                    else if(selectDateIndex == 3)
                    {
                        tvStartDate3.setTag(dateStr);

                        tvStartDate3.setText(systemFormat);
                        signUpRequest.setStart_date3(dateStr);
                    }
                    else if(selectDateIndex == 4)
                    {
                        tvStartDate4.setTag(dateStr);

                        tvStartDate4.setText(systemFormat);
                        signUpRequest.setStart_date4(dateStr);
                    }
                    else if(selectDateIndex == 5)
                    {
                        tvStartDate5.setTag(dateStr);

                        tvStartDate5.setText(systemFormat);
                        signUpRequest.setStart_date5(dateStr);
                    }
                }
                else
                {
                    if(selectDateIndex == 1)
                    {
                        tvEndDate1.setTag(dateStr);

                        tvEndDate1.setText(systemFormat);
                        signUpRequest.setEnd_date1(dateStr);
                    }
                    else if(selectDateIndex == 2)
                    {
                        tvEndDate2.setTag(dateStr);

                        tvEndDate2.setText(systemFormat);
                        signUpRequest.setEnd_date2(dateStr);
                    }
                    else if(selectDateIndex == 3)
                    {
                        tvEndDate3.setTag(dateStr);

                        tvEndDate3.setText(systemFormat);
                        signUpRequest.setEnd_date3(dateStr);
                    }
                    else if(selectDateIndex == 4)
                    {
                        tvEndDate4.setTag(dateStr);

                        tvEndDate4.setText(systemFormat);
                        signUpRequest.setEnd_date4(dateStr);
                    }
                    else if(selectDateIndex == 5)
                    {
                        tvEndDate5.setTag(dateStr);

                        tvEndDate5.setText(systemFormat);
                        signUpRequest.setEnd_date5(dateStr);
                    }
                }
                layoutDatePicker.setVisibility(View.GONE);
            }
        });
        tvDatePickerCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutDatePicker.setVisibility(View.GONE);
            }
        });

        spCountry1.setTag(1);
        spCountry2.setTag(2);
        spCountry3.setTag(3);
        spCountry4.setTag(4);
        spCountry5.setTag(5);

        AdapterView.OnItemSelectedListener spItemSelectedListener = new AdapterView.OnItemSelectedListener() {
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
//                int vehicleIndex = (Integer)parent.getTag();
//                // attaching data adapter to spinner
//                if(vehicleIndex == 1) {
//                    spStates1.setAdapter(dataAdapter);
//                }
//                else if(vehicleIndex == 2) {
//                    spStates2.setAdapter(dataAdapter);
//                }
//                else if(vehicleIndex == 3) {
//                    spStates3.setAdapter(dataAdapter);
//                }
//                else if(vehicleIndex == 4) {
//                    spStates4.setAdapter(dataAdapter);
//                }
//                else if(vehicleIndex == 5) {
//                    spStates5.setAdapter(dataAdapter);
//                }
                RelativeLayout rlState = null;
                int vehicleIndex = (Integer)parent.getTag();
                if (position == 0 || position == 1) {
                    ArrayAdapter<String> dataAdapter;
                    if (position == 0) {
                        dataAdapter = new ArrayAdapter<String>(SignUpVehicleInfoActivity.this,
                                android.R.layout.simple_spinner_item,
                                getResources().getStringArray(R.array.united_states_arrays));
                    } else {
                        dataAdapter = new ArrayAdapter<String>(SignUpVehicleInfoActivity.this,
                                android.R.layout.simple_spinner_item,
                                getResources().getStringArray(R.array.canada_provinces_arrays));
                    }
                    // Drop down layout style - list view with radio button
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    // attaching data adapter to spinner
                    if(vehicleIndex == 1) {
                        rlState = (RelativeLayout)llVehicle1.findViewById(R.id.rl_state);
                        spStates1.setEnabled(true);
                        spStates1.setAdapter(dataAdapter);
                    }
                    else if(vehicleIndex == 2) {
                        rlState = (RelativeLayout)llVehicle2.findViewById(R.id.rl_state);
                        spStates2.setEnabled(true);
                        spStates2.setAdapter(dataAdapter);
                    }
                    else if(vehicleIndex == 3) {
                        rlState = (RelativeLayout)llVehicle3.findViewById(R.id.rl_state);
                        spStates3.setEnabled(true);
                        spStates3.setAdapter(dataAdapter);
                    }
                    else if(vehicleIndex == 4) {
                        rlState = (RelativeLayout)llVehicle4.findViewById(R.id.rl_state);
                        spStates4.setEnabled(true);
                        spStates4.setAdapter(dataAdapter);
                    }
                    else if(vehicleIndex == 5) {
                        rlState = (RelativeLayout)llVehicle5.findViewById(R.id.rl_state);
                        spStates5.setEnabled(true);
                        spStates5.setAdapter(dataAdapter);
                    }
                    if(rlState != null) {
                        //rlState.setBackgroundColor(Color.WHITE);
                        rlState.setBackgroundResource(R.drawable.vehicle_state_spinner_active_bg);
                    }
                }
                else {
                    if(vehicleIndex == 1) {
                        rlState = (RelativeLayout)llVehicle1.findViewById(R.id.rl_state);
                        spStates1.setEnabled(false);
                    }
                    else if(vehicleIndex == 2) {
                        rlState = (RelativeLayout)llVehicle2.findViewById(R.id.rl_state);
                        spStates2.setEnabled(false);
                    }
                    else if(vehicleIndex == 3) {
                        rlState = (RelativeLayout)llVehicle3.findViewById(R.id.rl_state);
                        spStates3.setEnabled(false);
                    }
                    else if(vehicleIndex == 4) {
                        rlState = (RelativeLayout)llVehicle4.findViewById(R.id.rl_state);
                        spStates4.setEnabled(false);
                    }
                    else if(vehicleIndex == 5) {
                        rlState = (RelativeLayout)llVehicle5.findViewById(R.id.rl_state);
                        spStates5.setEnabled(false);
                    }
                    if(rlState != null) {
                        //rlState.setBackgroundColor(getResources().getColor(R.color.colorBgGrayLight));
                        rlState.setBackgroundResource(R.drawable.vehicle_state_spinner_inactive_bg);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        spCountry1.setOnItemSelectedListener(spItemSelectedListener);
        spCountry2.setOnItemSelectedListener(spItemSelectedListener);
        spCountry3.setOnItemSelectedListener(spItemSelectedListener);
        spCountry4.setOnItemSelectedListener(spItemSelectedListener);
        spCountry5.setOnItemSelectedListener(spItemSelectedListener);

        tvGoNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation()) {
                    setRequest();
                    checkVehicleRequest();
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

    private void checkVehicleRequest()
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
                            gotoActivity(SignUpVehicleInfoActivity.this, SignUpPaymentInfoActivity.class,
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

        ServerDelegate.checkVehicle(params, listener, errorListener);
    }
    private boolean checkVehicle1()
    {
        boolean ret = true;

        //License plate info not required for FasTrak.
        int index = TollRoadsApp.getInstance().selectedAccountType;

        if(etPlate1.getText().length() == 0 && index != Constants.ACCOUNT_TYPE_FASTRAK_INDIVIDUAL)
        {
            ret = false;
            showToastMessage(getString(R.string.license_plate_empty_warning));
        }
        else if(!etPlate1.getText().toString().equals(etPlateConfirm1.getText().toString())) {
            ret = false;
            showToastMessage(getString(R.string.license_plate_not_match_warning));
        }
        else if(etYear1.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.year_empty_warning));
        }
        else if(etMake1.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.make_empty_warning));
        }
        else if(etModel1.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.model_empty_warning));
        }
        else if(tvStartDate1.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.start_date_empty_warning));
        }
        else
        {
            if(swRental1.isChecked() && tvEndDate1.getText().length() == 0)
            {
                ret = false;
                showToastMessage(getString(R.string.end_date_empty_warning));
            }

//            if(tvEndDate1.getText().length() != 0) {
//                long startTimeStamp = TollRoadsApp.getTimeStampFromDateString(tvStartDate1.getText().toString());
//                long endTimeStamp = TollRoadsApp.getTimeStampFromDateString(tvEndDate1.getText().toString());
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
//                else if(swRental1.isChecked() && (endTimeStamp -startTimeStamp) > Constants.MAX_INTERVAL_FOR_RENTAL_VEHICLE)  //max 7 days include today
//                {
//                    ret = false;
//                    showToastMessage(getString(R.string.end_date_warning3));
//                }
//            }
        }

        return ret;
    }

    private boolean checkVehicle2()
    {
        boolean ret = true;
        //License plate info not required for FasTrak.
        int index = TollRoadsApp.getInstance().selectedAccountType;
        if(index == Constants.ACCOUNT_TYPE_FASTRAK_INDIVIDUAL)
        {
            if(etPlate2.getText().length() == 0 && etPlateConfirm2.getText().length() == 0
                    && etYear2.getText().length() == 0 && etMake2.getText().length() == 0
                    && etModel2.getText().length() == 0)
            {
                return true;
            }
        }
        
        if(etPlate2.getText().length() == 0 && index != Constants.ACCOUNT_TYPE_FASTRAK_INDIVIDUAL)
        {
            ret = false;
            showToastMessage(getString(R.string.license_plate_empty_warning));
        }
        else if(!etPlate2.getText().toString().equals(etPlateConfirm2.getText().toString())) {
            ret = false;
            showToastMessage(getString(R.string.license_plate_not_match_warning));
        }
        else if(etYear2.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.year_empty_warning));
        }
        else if(etMake2.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.make_empty_warning));
        }
        else if(etModel2.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.model_empty_warning));
        }
        else if(tvStartDate2.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.start_date_empty_warning));
        }
        else
        {
            if(swRental2.isChecked() && tvEndDate2.getText().length() == 0)
            {
                ret = false;
                showToastMessage(getString(R.string.end_date_empty_warning));
            }

//            if(tvEndDate2.getText().length() != 0) {
//                long startTimeStamp = TollRoadsApp.getTimeStampFromDateString(tvStartDate2.getText().toString());
//                long endTimeStamp = TollRoadsApp.getTimeStampFromDateString(tvEndDate2.getText().toString());
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
//                else if(swRental2.isChecked() && (endTimeStamp -startTimeStamp) > Constants.MAX_INTERVAL_FOR_RENTAL_VEHICLE)  //max 7 days include today
//                {
//                    ret = false;
//                    showToastMessage(getString(R.string.end_date_warning3));
//                }
//            }
        }

        return ret;
    }

    private boolean checkVehicle3()
    {
        boolean ret = true;
        //License plate info not required for FasTrak.
        int index = TollRoadsApp.getInstance().selectedAccountType;
        if(index == Constants.ACCOUNT_TYPE_FASTRAK_INDIVIDUAL)
        {
            if(etPlate3.getText().length() == 0 && etPlateConfirm3.getText().length() == 0
                    && etYear3.getText().length() == 0 && etMake3.getText().length() == 0
                    && etModel3.getText().length() == 0)
            {
                return true;
            }
        }
        
        if(etPlate3.getText().length() == 0 && index != Constants.ACCOUNT_TYPE_FASTRAK_INDIVIDUAL)
        {
            ret = false;
            showToastMessage(getString(R.string.license_plate_empty_warning));
        }
        else if(!etPlate3.getText().toString().equals(etPlateConfirm3.getText().toString())) {
            ret = false;
            showToastMessage(getString(R.string.license_plate_not_match_warning));
        }
        else if(etYear3.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.year_empty_warning));
        }
        else if(etMake3.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.make_empty_warning));
        }
        else if(etModel3.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.model_empty_warning));
        }
        else if(tvStartDate3.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.start_date_empty_warning));
        }
        else
        {
            if(swRental3.isChecked() && tvEndDate3.getText().length() == 0)
            {
                ret = false;
                showToastMessage(getString(R.string.end_date_empty_warning));
            }

//            if(tvEndDate3.getText().length() != 0) {
//                long startTimeStamp = TollRoadsApp.getTimeStampFromDateString(tvStartDate3.getText().toString());
//                long endTimeStamp = TollRoadsApp.getTimeStampFromDateString(tvEndDate3.getText().toString());
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
//                else if(swRental3.isChecked() && (endTimeStamp -startTimeStamp) > Constants.MAX_INTERVAL_FOR_RENTAL_VEHICLE)  //max 7 days include today
//                {
//                    ret = false;
//                    showToastMessage(getString(R.string.end_date_warning3));
//                }
//            }
        }

        return ret;
    }

    private boolean checkVehicle4()
    {
        boolean ret = true;
        //License plate info not required for FasTrak.
        int index = TollRoadsApp.getInstance().selectedAccountType;
        if(index == Constants.ACCOUNT_TYPE_FASTRAK_INDIVIDUAL)
        {
            if(etPlate4.getText().length() == 0 && etPlateConfirm4.getText().length() == 0
                    && etYear4.getText().length() == 0 && etMake4.getText().length() == 0
                    && etModel4.getText().length() == 0)
            {
                return true;
            }
        }
        if(etPlate4.getText().length() == 0 && index != Constants.ACCOUNT_TYPE_FASTRAK_INDIVIDUAL)
        {
            ret = false;
            showToastMessage(getString(R.string.license_plate_empty_warning));
        }
        else if(!etPlate4.getText().toString().equals(etPlateConfirm4.getText().toString())) {
            ret = false;
            showToastMessage(getString(R.string.license_plate_not_match_warning));
        }
        else if(etYear4.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.year_empty_warning));
        }
        else if(etMake4.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.make_empty_warning));
        }
        else if(etModel4.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.model_empty_warning));
        }
        else if(tvStartDate4.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.start_date_empty_warning));
        }
        else
        {
            if(swRental4.isChecked() && tvEndDate4.getText().length() == 0)
            {
                ret = false;
                showToastMessage(getString(R.string.end_date_empty_warning));
            }

//            if(tvEndDate4.getText().length() != 0) {
//                long startTimeStamp = TollRoadsApp.getTimeStampFromDateString(tvStartDate4.getText().toString());
//                long endTimeStamp = TollRoadsApp.getTimeStampFromDateString(tvEndDate4.getText().toString());
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
//                else if(swRental4.isChecked() && (endTimeStamp -startTimeStamp) > Constants.MAX_INTERVAL_FOR_RENTAL_VEHICLE)  //max 7 days include today
//                {
//                    ret = false;
//                    showToastMessage(getString(R.string.end_date_warning3));
//                }
//            }
        }

        return ret;
    }

    private boolean checkVehicle5()
    {
        boolean ret = true;
        //License plate info not required for FasTrak.
        int index = TollRoadsApp.getInstance().selectedAccountType;
        if(index == Constants.ACCOUNT_TYPE_FASTRAK_INDIVIDUAL)
        {
            if(etPlate5.getText().length() == 0 && etPlateConfirm5.getText().length() == 0
                    && etYear5.getText().length() == 0 && etMake5.getText().length() == 0
                    && etModel5.getText().length() == 0)
            {
                return true;
            }
        }
        if(etPlate5.getText().length() == 0 && index != Constants.ACCOUNT_TYPE_FASTRAK_INDIVIDUAL)
        {
            ret = false;
            showToastMessage(getString(R.string.license_plate_empty_warning));
        }
        else if(!etPlate5.getText().toString().equals(etPlateConfirm5.getText().toString())) {
            ret = false;
            showToastMessage(getString(R.string.license_plate_not_match_warning));
        }
        else if(etYear5.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.year_empty_warning));
        }
        else if(etMake5.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.make_empty_warning));
        }
        else if(etModel5.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.model_empty_warning));
        }
        else if(tvStartDate5.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.start_date_empty_warning));
        }
        else
        {
            if(swRental5.isChecked() && tvEndDate5.getText().length() == 0)
            {
                ret = false;
                showToastMessage(getString(R.string.end_date_empty_warning));
            }

//            if(tvEndDate5.getText().length() != 0) {
//                long startTimeStamp = TollRoadsApp.getTimeStampFromDateString(tvStartDate5.getText().toString());
//                long endTimeStamp = TollRoadsApp.getTimeStampFromDateString(tvEndDate5.getText().toString());
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
//                else if(swRental1.isChecked() && (endTimeStamp -startTimeStamp) > Constants.MAX_INTERVAL_FOR_RENTAL_VEHICLE)  //max 7 days include today
//                {
//                    ret = false;
//                    showToastMessage(getString(R.string.end_date_warning3));
//                }
//            }
        }

        return ret;
    }
    
    private void setVehicle1ToRequest()
    {
        if(swRental1.isChecked()) {
            signUpRequest.setVehicle_type1(Constants.VEHICLE_TYPE_RENTAL);
        }
        else {
            signUpRequest.setVehicle_type1(Constants.VEHICLE_TYPE_INDIVIDUAL);
        }
        signUpRequest.setPlate1(etPlate1.getText().toString());

        if(spStates1.getSelectedItem().toString().equals("US GOVT"))
        {
            signUpRequest.setState1("US");
        }
        else {
            signUpRequest.setState1(spStates1.getSelectedItem().toString());
        }

        int countryIndex = spCountry1.getSelectedItemPosition();
        String countryAbbr = TollRoadsApp.getCountryString(countryIndex);

        signUpRequest.setCountry1(countryAbbr);

        signUpRequest.setYear1(etYear1.getText().toString());
        signUpRequest.setMake1(etMake1.getText().toString());
        signUpRequest.setModel1(etModel1.getText().toString());
        if(tvStartDate1.getTag() == null)
        {
            signUpRequest.setStart_date1("");
        }
        else
        {
            signUpRequest.setStart_date1((String)tvStartDate1.getTag());
        }

        if(tvEndDate1.getTag() == null)
        {
            signUpRequest.setEnd_date1("");
        }
        else
        {
            signUpRequest.setEnd_date1((String)tvEndDate1.getTag());
        }
    }

    private void setVehicle2ToRequest()
    {
        if(swRental2.isChecked()) {
            signUpRequest.setVehicle_type2(Constants.VEHICLE_TYPE_RENTAL);
        }
        else {
            signUpRequest.setVehicle_type2(Constants.VEHICLE_TYPE_INDIVIDUAL);
        }
        signUpRequest.setPlate2(etPlate2.getText().toString());

        if(spStates2.getSelectedItem().toString().equals("US GOVT"))
        {
            signUpRequest.setState2("US");
        }
        else {
            signUpRequest.setState2(spStates2.getSelectedItem().toString());
        }

        int countryIndex = spCountry2.getSelectedItemPosition();
        String countryAbbr = TollRoadsApp.getCountryString(countryIndex);

        signUpRequest.setCountry2(countryAbbr);

        signUpRequest.setYear2(etYear2.getText().toString());
        signUpRequest.setMake2(etMake2.getText().toString());
        signUpRequest.setModel2(etModel2.getText().toString());

        if(tvStartDate2.getTag() == null)
        {
            signUpRequest.setStart_date2("");
        }
        else
        {
            signUpRequest.setStart_date2((String)tvStartDate2.getTag());
        }

        if(tvEndDate2.getTag() == null)
        {
            signUpRequest.setEnd_date2("");
        }
        else
        {
            signUpRequest.setEnd_date2((String)tvEndDate2.getTag());
        }
        
    }

    private void setVehicle3ToRequest()
    {
        if(swRental3.isChecked()) {
            signUpRequest.setVehicle_type3(Constants.VEHICLE_TYPE_RENTAL);
        }
        else {
            signUpRequest.setVehicle_type3(Constants.VEHICLE_TYPE_INDIVIDUAL);
        }
        signUpRequest.setPlate3(etPlate3.getText().toString());

        if(spStates3.getSelectedItem().toString().equals("US GOVT"))
        {
            signUpRequest.setState3("US");
        }
        else {
            signUpRequest.setState3(spStates3.getSelectedItem().toString());
        }

        int countryIndex = spCountry3.getSelectedItemPosition();
        String countryAbbr = TollRoadsApp.getCountryString(countryIndex);;

        signUpRequest.setCountry3(countryAbbr);

        signUpRequest.setYear3(etYear3.getText().toString());
        signUpRequest.setMake3(etMake3.getText().toString());
        signUpRequest.setModel3(etModel3.getText().toString());
        if(tvStartDate3.getTag() == null)
        {
            signUpRequest.setStart_date3("");
        }
        else
        {
            signUpRequest.setStart_date3((String)tvStartDate3.getTag());
        }

        if(tvEndDate3.getTag() == null)
        {
            signUpRequest.setEnd_date3("");
        }
        else
        {
            signUpRequest.setEnd_date3((String)tvEndDate3.getTag());
        }
    }

    private void setVehicle4ToRequest()
    {
        if(swRental4.isChecked()) {
            signUpRequest.setVehicle_type4(Constants.VEHICLE_TYPE_RENTAL);
        }
        else {
            signUpRequest.setVehicle_type4(Constants.VEHICLE_TYPE_INDIVIDUAL);
        }
        signUpRequest.setPlate4(etPlate4.getText().toString());

        if(spStates4.getSelectedItem().toString().equals("US GOVT"))
        {
            signUpRequest.setState4("US");
        }
        else {
            signUpRequest.setState4(spStates4.getSelectedItem().toString());
        }

        int countryIndex = spCountry4.getSelectedItemPosition();
        String countryAbbr =TollRoadsApp.getCountryString(countryIndex);;

        signUpRequest.setCountry4(countryAbbr);

        signUpRequest.setYear4(etYear4.getText().toString());
        signUpRequest.setMake4(etMake4.getText().toString());
        signUpRequest.setModel4(etModel4.getText().toString());
        if(tvStartDate4.getTag() == null)
        {
            signUpRequest.setStart_date4("");
        }
        else
        {
            signUpRequest.setStart_date4((String)tvStartDate4.getTag());
        }

        if(tvEndDate4.getTag() == null)
        {
            signUpRequest.setEnd_date4("");
        }
        else
        {
            signUpRequest.setEnd_date4((String)tvEndDate4.getTag());
        }
    }

    private void setVehicle5ToRequest()
    {
        if(swRental5.isChecked()) {
            signUpRequest.setVehicle_type5(Constants.VEHICLE_TYPE_RENTAL);
        }
        else {
            signUpRequest.setVehicle_type5(Constants.VEHICLE_TYPE_INDIVIDUAL);
        }
        signUpRequest.setPlate5(etPlate5.getText().toString());

        if(spStates5.getSelectedItem().toString().equals("US GOVT"))
        {
            signUpRequest.setState5("US");
        }
        else {
            signUpRequest.setState5(spStates5.getSelectedItem().toString());
        }

        int countryIndex = spCountry5.getSelectedItemPosition();
        String countryAbbr = TollRoadsApp.getCountryString(countryIndex);;

        signUpRequest.setCountry5(countryAbbr);

        signUpRequest.setYear5(etYear5.getText().toString());
        signUpRequest.setMake5(etMake5.getText().toString());
        signUpRequest.setModel5(etModel5.getText().toString());
        if(tvStartDate5.getTag() == null)
        {
            signUpRequest.setStart_date5("");
        }
        else
        {
            signUpRequest.setStart_date5((String)tvStartDate5.getTag());
        }

        if(tvEndDate5.getTag() == null)
        {
            signUpRequest.setEnd_date5("");
        }
        else
        {
            signUpRequest.setEnd_date5((String)tvEndDate5.getTag());
        }
    }

    private void clearVehicle1ToRequest()
    {
        signUpRequest.setVehicle_type1(null);
        signUpRequest.setPlate1("");
        signUpRequest.setState1("");
        signUpRequest.setCountry1("");
        signUpRequest.setYear1("");
        signUpRequest.setMake1("");
        signUpRequest.setModel1("");
        signUpRequest.setStart_date1("");
        signUpRequest.setEnd_date1("");
    }
    private void clearVehicle2ToRequest()
    {
        signUpRequest.setVehicle_type2(null);
        signUpRequest.setPlate2("");
        signUpRequest.setState2("");
        signUpRequest.setCountry2("");
        signUpRequest.setYear2("");
        signUpRequest.setMake2("");
        signUpRequest.setModel2("");
        signUpRequest.setStart_date2("");
        signUpRequest.setEnd_date2("");
    }
    private void clearVehicle3ToRequest()
    {
        signUpRequest.setVehicle_type3(null);
        signUpRequest.setPlate3("");
        signUpRequest.setState3("");
        signUpRequest.setCountry3("");
        signUpRequest.setYear3("");
        signUpRequest.setMake3("");
        signUpRequest.setModel3("");
        signUpRequest.setStart_date3("");
        signUpRequest.setEnd_date3("");
    }
    private void clearVehicle4ToRequest()
    {
        signUpRequest.setVehicle_type4(null);
        signUpRequest.setPlate4("");
        signUpRequest.setState4("");
        signUpRequest.setCountry4("");
        signUpRequest.setYear4("");
        signUpRequest.setMake4("");
        signUpRequest.setModel4("");
        signUpRequest.setStart_date4("");
        signUpRequest.setEnd_date4("");
    }
    private void clearVehicle5ToRequest()
    {
        signUpRequest.setVehicle_type5(null);
        signUpRequest.setPlate5("");
        signUpRequest.setState5("");
        signUpRequest.setCountry5("");
        signUpRequest.setYear5("");
        signUpRequest.setMake5("");
        signUpRequest.setModel5("");
        signUpRequest.setStart_date5("");
        signUpRequest.setEnd_date5("");
    }

    private void setRequest()
    {
        clearVehicle1ToRequest();
        clearVehicle2ToRequest();
        clearVehicle3ToRequest();
        clearVehicle4ToRequest();
        clearVehicle5ToRequest();

        if(vehicleCount >= 1)
        {
            setVehicle1ToRequest();
        }
        if(vehicleCount >= 2)
        {
            setVehicle2ToRequest();
        }
        if(vehicleCount >= 3)
        {
            setVehicle3ToRequest();
        }
        if(vehicleCount >= 4)
        {
            setVehicle4ToRequest();
        }
        if(vehicleCount >= 5)
        {
            setVehicle5ToRequest();
        }
    }
    
    private boolean checkValidation()
    {
        boolean ret = true;
        if(vehicleCount >= 1)
        {
            if(!checkVehicle1())
            {
                return false;
            }
        }
        if(vehicleCount >= 2)
        {
            if(!checkVehicle2())
            {
                return false;
            }
        }
        if(vehicleCount >= 3)
        {
            if(!checkVehicle3())
            {
                return false;
            }
        }
        if(vehicleCount >= 4)
        {
            if(!checkVehicle4())
            {
                return false;
            }
        }
        if(vehicleCount >= 5)
        {
            if(!checkVehicle5())
            {
                return false;
            }
        }
        return ret;
    }
    private void initWidgetValue()
    {
        initVehicle1();
        initVehicle2();
        initVehicle3();
        initVehicle4();
        initVehicle5();
    }

    private void setDescription()
    {
        int index = TollRoadsApp.getInstance().selectedAccountType;
        String description;
        if(index == Constants.ACCOUNT_TYPE_FASTRAK_INDIVIDUAL)
        {
            description = getString(R.string.all_account_sign_up_vehicle_description)+
                    " "+ getString(R.string.fastrak_sign_up_vehicle_description);
        }
        else
        {
            description = getString(R.string.all_account_sign_up_vehicle_description);
        }
        tvVehicleDescription.setText(description);
    }

    @Override
    protected void onResume() {
        super.onResume();

        setDescription();
    }

    @Override
    public void onPause() {

        super.onPause();
    }

}
