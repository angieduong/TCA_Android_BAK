package com.seta.tollroaddroid.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.json.OTTUserInfoRequest;
import com.seta.tollroaddroid.app.json.SignUpRequest;
import com.seta.tollroaddroid.app.utilities.Constants;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class OTTSaveCreditCardActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();
    private LinearLayout llSaveThirtyDays, llSaveForever;
    private TextView tvNotSaveHint;

    private RadioButton rbSaveThirtyDays, rbSaveForever, rbNotSave;
    private ImageView ivGoBack;
    private TextView tvGoNext, tvTermsPrivacy;

    private RelativeLayout layoutDatePicker;
    private DatePicker datePicker;
    private TextView tvDatePickerConfirm,tvDatePickerCancel;
    private TextView tvStartDate,tvEndDate;
    private boolean selectStartDate = true;

    private EditText etUserName,etPassword, etRetypePassword;
    private ImageView ivPasswordHint, ivUserNameHint;
    private Switch swReceivePromotion, swReceiveAlerts;
    private EditText etPhoneNo;
    private CheckBox cbTermsPrivacy;
    private TextView tvSaveForeverHint;
    private TextView tvTotalTolls;
    private LinearLayout llNotSave;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("start_date", (String)tvStartDate.getTag());
        outState.putString("end_date", (String)tvEndDate.getTag());
        outState.putSerializable("OTTUserInfoRequest", TollRoadsApp.getInstance().gOTTUserInfoRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ott_save_credit_card);
        if(savedInstanceState != null)
        {
            TollRoadsApp.getInstance().gOTTUserInfoRequest = (OTTUserInfoRequest) savedInstanceState.getSerializable("OTTUserInfoRequest");
        }

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvGoNext = (TextView)findViewById(R.id.tv_go_next);

        llSaveThirtyDays = (LinearLayout)findViewById(R.id.ll_save_30_days);
        llSaveForever = (LinearLayout)findViewById(R.id.ll_save_forever);
        tvNotSaveHint = (TextView)findViewById(R.id.tv_not_save_hint);

        rbSaveThirtyDays = (RadioButton)findViewById(R.id.rb_save_30_days);
        rbSaveForever = (RadioButton)findViewById(R.id.rb_save_forever);
        rbNotSave = (RadioButton)findViewById(R.id.rb_not_save);

        layoutDatePicker = (RelativeLayout)findViewById(R.id.layout_datePicker);
        datePicker = (DatePicker)findViewById(R.id.datePicker);
        tvDatePickerConfirm = (TextView)findViewById(R.id.tv_datePicker_ok);
        tvDatePickerCancel = (TextView)findViewById(R.id.tv_datePicker_cancel);
        tvStartDate = (TextView)findViewById(R.id.tv_start_date);
        tvEndDate = (TextView)findViewById(R.id.tv_end_date);

        tvSaveForeverHint = (TextView)findViewById(R.id.tv_save_forever_hint);

        tvTermsPrivacy = (TextView)findViewById(R.id.tv_terms_privacy);
        tvTermsPrivacy.setMovementMethod(LinkMovementMethod.getInstance());
        cbTermsPrivacy = (CheckBox)findViewById(R.id.cb_terms_privacy_agree);

        etUserName = (EditText)findViewById(R.id.et_user_name);
        etPassword = (EditText)findViewById(R.id.et_password);
        etRetypePassword = (EditText)findViewById(R.id.et_re_type);
        ivUserNameHint = (ImageView)findViewById(R.id.iv_user_name_hint);
        ivPasswordHint = (ImageView)findViewById(R.id.iv_password_hint);

        etPhoneNo = (EditText)findViewById(R.id.et_phone_number);
        swReceivePromotion = (Switch)findViewById(R.id.sw_receive_promotion);
        swReceiveAlerts = (Switch)findViewById(R.id.sw_receive_road_alerts);

        tvTotalTolls = (TextView)findViewById(R.id.tv_total_tolls);
        llNotSave = (LinearLayout)findViewById(R.id.ll_not_save);

        setupListener();
        setupRadioButtonControl();

        setupForSaveThirtyDays(savedInstanceState);
        setupForSaveForever();
    }

    private void refreshTotalTolls()
    {
        double total_amount;

        if(rbSaveForever.isChecked())
        {
            total_amount = TollRoadsApp.getInstance().gOTTUserInfoRequest.getCash_amount();
        }
        else
        {
            total_amount = TollRoadsApp.getInstance().gOTTUserInfoRequest.getTotal_amount();
        }
        tvTotalTolls.setText(getString(R.string.total_charge, total_amount));
        tvSaveForeverHint.setText(getString(R.string.save_forever_hint,
                TollRoadsApp.getInstance().gOTTUserInfoRequest.getCash_amount()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        double total_amount = TollRoadsApp.getInstance().gOTTUserInfoRequest.getTotal_amount();

        if(total_amount > 0) {
            llNotSave.setVisibility(View.VISIBLE);
            if(rbNotSave.isChecked())
            {
                tvNotSaveHint.setVisibility(View.VISIBLE);
            }
            else
            {
                tvNotSaveHint.setVisibility(View.GONE);
            }
        }
        else
        {
            llNotSave.setVisibility(View.GONE);
            tvNotSaveHint.setVisibility(View.GONE);
        }
        tvTotalTolls.setVisibility(View.VISIBLE);
        refreshTotalTolls();
    }

    private void setupForSaveThirtyDays(Bundle savedInstanceState)
    {
        initDates(savedInstanceState);
        setupDatePicker();
    }

    private void setupForSaveForever()
    {
        tvSaveForeverHint.setText(getString(R.string.save_forever_hint,
                TollRoadsApp.getInstance().gOTTUserInfoRequest.getCash_amount()));

        ivUserNameHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToastMessage(getString(R.string.user_name_hint));
            }
        });
        ivPasswordHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToastMessage(getString(R.string.password_hint));
            }
        });
    }

    private void initDates(Bundle savedInstanceState)
    {
        String endDate, endDateStr, startDate, startDateStr;
        if(savedInstanceState == null)
        {
            Calendar c = Calendar.getInstance();

            OTTUserInfoRequest oTTUserInfoRequest = TollRoadsApp.getInstance().gOTTUserInfoRequest;
            startDateStr = oTTUserInfoRequest.getCalc_start_date();
            endDateStr = oTTUserInfoRequest.getCalc_end_date();

            long startTimestamp = getTimestampFromDateString(startDateStr);
            long endTimestamp = getTimestampFromDateString(endDateStr);
            long currentTimestamp = System.currentTimeMillis();
            long thirtyDays = Resource.ONE_DAY * 30L;
            if(oTTUserInfoRequest.getCalculate_toll_mode() == Resource.CALCULATE_TOLL_FOR_ME) {
                if (startTimestamp > currentTimestamp) {
                    startTimestamp = currentTimestamp;
                }

                //end date should always be 30 days later
//                if (endTimestamp > (currentTimestamp + thirtyDays)) {
//                    endTimestamp = currentTimestamp + thirtyDays;
//                }
//                else if (endTimestamp < currentTimestamp)
                {
                    endTimestamp = currentTimestamp + thirtyDays;
                }
            }
            else
            {
                startTimestamp = currentTimestamp - Resource.ONE_DAY * 6L;  //7 days include today
                endTimestamp = currentTimestamp + thirtyDays;
            }

            c.setTimeInMillis(endTimestamp);
            endDate = TollRoadsApp.formatDateBySystem(c.getTimeInMillis());
            endDateStr = c.get(Calendar.YEAR) + "-" + String.format("%02d", c.get(Calendar.MONTH) + 1)
                    + "-" + String.format("%02d", c.get(Calendar.DAY_OF_MONTH));


            c.setTimeInMillis(startTimestamp);
            startDate = TollRoadsApp.formatDateBySystem(c.getTimeInMillis());
            startDateStr = c.get(Calendar.YEAR) + "-" + String.format("%02d", c.get(Calendar.MONTH) + 1)
                    + "-" + String.format("%02d", c.get(Calendar.DAY_OF_MONTH));
        }
        else
        {
            startDateStr = savedInstanceState.getString("start_date", "");
            startDate = parseDateString(startDateStr);

            endDateStr = savedInstanceState.getString("end_date", "");
            endDate = parseDateString(endDateStr);
        }

        tvStartDate.setTag(startDateStr);
        tvStartDate.setText(startDate);
        tvEndDate.setTag(endDateStr);
        tvEndDate.setText(endDate);
    }

    private String parseDateString(String date)
    {
        String ret = "";
        if(date != null && !date.isEmpty())
        {
            String[] dateArray = date.split("-");
            if(dateArray.length == 3) {
                int year = Integer.valueOf(dateArray[0]);
                int month = Integer.valueOf(dateArray[1]) - 1;
                int day = Integer.valueOf(dateArray[2]);
                Calendar c = Calendar.getInstance();
                c.set(year, month, day);

                ret = TollRoadsApp.formatDateBySystem(c.getTimeInMillis());
            }
        }
        return ret;
    }

    private long getTimestampFromDateString(String date)
    {
        long ret = 0;
        if(date != null && !date.isEmpty())
        {
            String[] dateArray = date.split("-");
            if(dateArray.length == 3) {
                int year = Integer.valueOf(dateArray[0]);
                int month = Integer.valueOf(dateArray[1]) - 1;
                int day = Integer.valueOf(dateArray[2]);
                Calendar c = Calendar.getInstance();
                c.set(year, month, day);

                ret = c.getTimeInMillis();
            }
        }
        return ret;
    }

    private void setupDatePicker()
    {
        tvStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStartDate = true;

                String dateStr = (String)tvStartDate.getTag();
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
        tvEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStartDate = false;
                String dateStr = (String)tvEndDate.getTag();
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

            }
        });
        tvDatePickerCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutDatePicker.setVisibility(View.GONE);
            }
        });

    }

    private void setupRadioButtonControl()
    {
        rbSaveThirtyDays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbSaveForever.setChecked(false);
                rbNotSave.setChecked(false);

                llSaveThirtyDays.setVisibility(View.VISIBLE);
                llSaveForever.setVisibility(View.GONE);
                tvNotSaveHint.setVisibility(View.GONE);

                tvGoNext.setVisibility(View.VISIBLE);
                refreshTotalTolls();
            }
        });

        rbSaveForever.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbSaveThirtyDays.setChecked(false);
                rbNotSave.setChecked(false);

                llSaveThirtyDays.setVisibility(View.GONE);
                llSaveForever.setVisibility(View.VISIBLE);
                tvNotSaveHint.setVisibility(View.GONE);

                tvGoNext.setVisibility(View.VISIBLE);

                refreshTotalTolls();
            }
        });

        rbNotSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbSaveForever.setChecked(false);
                rbSaveThirtyDays.setChecked(false);

                llSaveThirtyDays.setVisibility(View.GONE);
                llSaveForever.setVisibility(View.GONE);
                tvNotSaveHint.setVisibility(View.VISIBLE);

                tvGoNext.setVisibility(View.VISIBLE);
                refreshTotalTolls();
            }
        });
    }

    private boolean checkValidation()
    {
        boolean ret = true;
        if(etUserName.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.user_name_empty_warning));
        }
        else if(etPassword.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.password_empty_warning));
        }
        else if(!etPassword.getText().toString().equals(etRetypePassword.getText().toString()))
        {
            ret = false;
            showToastMessage(getString(R.string.password_not_match_warning));
        }
        else if(etPhoneNo.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.phone_no_empty_warning));
        }
        else if(!cbTermsPrivacy.isChecked())
        {
            ret = false;
            showToastMessage(getString(R.string.terms_privacy_not_checked_warning));
        }
        return ret;
    }

    private void setSignUpRequest()
    {
//        TollRoadsApp.getInstance().gSignUpRequest = new SignUpRequest();
//
//        SignUpRequest signUpRequest = TollRoadsApp.getInstance().gSignUpRequest;
//
//        signUpRequest.setAccount_type(Constants.ACCOUNT_TYPE_CHARGE_EXPRESS);
//
//        signUpRequest.setAccount_username(etUserName.getText().toString());
//        signUpRequest.setAccount_password(etPassword.getText().toString());
//        signUpRequest.setConfirm_password(etRetypePassword.getText().toString());
//        signUpRequest.setPrimary_phone(etPhoneNo.getText().toString().replace(" ",""));
//        signUpRequest.setReceive_promotion_material(swReceivePromotion.isChecked());
//        signUpRequest.setReceive_road_alerts(swReceiveAlerts.isChecked());

        OTTUserInfoRequest oTTUserInfoRequest = TollRoadsApp.getInstance().gOTTUserInfoRequest;
        oTTUserInfoRequest.setAccount_username(etUserName.getText().toString());
        oTTUserInfoRequest.setAccount_password(etPassword.getText().toString());
        oTTUserInfoRequest.setConfirm_password(etRetypePassword.getText().toString());
        oTTUserInfoRequest.setPrimary_phone(etPhoneNo.getText().toString().replace(" ",""));
        oTTUserInfoRequest.setReceive_promotion_material(swReceivePromotion.isChecked());
        oTTUserInfoRequest.setReceive_road_alerts(swReceiveAlerts.isChecked());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            gotoActivity(getApplicationContext(), OTTPaymentInfoActivity.class,
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            return  true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setupListener()
    {
        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(v.getContext(), OTTPaymentInfoActivity.class,
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
        });

        tvGoNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OTTUserInfoRequest oTTUserInfoRequest = TollRoadsApp.getInstance().gOTTUserInfoRequest;

                if(rbNotSave.isChecked())
                {
                    oTTUserInfoRequest.setSave_credit_card_option(Resource.SAVE_CARD_NOT_SAVE);
                    oTTUserInfoRequest.setSave_start_date("");
                    oTTUserInfoRequest.setSave_end_date("");
                }
                else if(rbSaveForever.isChecked())
                {
                    oTTUserInfoRequest.setSave_credit_card_option(Resource.SAVE_CARD_SAVE_FOREVER);

                    oTTUserInfoRequest.setSave_start_date("");
                    oTTUserInfoRequest.setSave_end_date("");

                    if(!checkValidation())
                    {
                        return;
                    }
                    else
                    {
                        //set the register request parameters
                        setSignUpRequest();
                    }
                }
                else if(rbSaveThirtyDays.isChecked())
                {
                    oTTUserInfoRequest.setSave_credit_card_option(Resource.SAVE_CARD_SAVE_30);

                    oTTUserInfoRequest.setSave_start_date((String) tvStartDate.getTag());
                    oTTUserInfoRequest.setSave_end_date((String) tvEndDate.getTag());
                }

                calcSaveOptionRequest();
            }
        });

    }

    private void calcSaveOptionRequest()
    {
        String params = TollRoadsApp.populateOTTParams();

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

                            if(rbSaveForever.isChecked())
                            {
                                TollRoadsApp.getInstance().ottTotalAmount = TollRoadsApp.getInstance().gOTTUserInfoRequest.getCash_amount();
                            }
                            else
                            {
                                TollRoadsApp.getInstance().ottTotalAmount = TollRoadsApp.getInstance().gOTTUserInfoRequest.getTotal_amount();
                            }

                            gotoActivity(OTTSaveCreditCardActivity.this, OTTSummaryActivity.class,
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

        ServerDelegate.calcSaveOptionRequest(Resource.URL_PAY_GO, params, listener, errorListener);
////test
//        gotoActivity(OTTPaymentInfoActivity.this, OTTSummaryActivity.class,//OTTSummaryActivity.class,
//                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    }
}
