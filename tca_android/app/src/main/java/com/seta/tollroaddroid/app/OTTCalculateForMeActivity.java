package com.seta.tollroaddroid.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.custom.MultiButtonsPopupDialog;
import com.seta.tollroaddroid.app.json.CalcTripInfoResponse;
import com.seta.tollroaddroid.app.json.CheckPlateResponse;
import com.seta.tollroaddroid.app.json.CommonResponse;
import com.seta.tollroaddroid.app.json.OTTUserInfoRequest;
import com.seta.tollroaddroid.app.json.OttTrip;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class OTTCalculateForMeActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private LinearLayout llNoToll, llToll;
    private TextView tvTotalDue;
    private ImageView ivGoBack;
    private TextView tvGoNext;

    private RelativeLayout layoutDatePicker;
    private DatePicker datePicker;
    private TextView tvDatePickerConfirm,tvDatePickerCancel;
    private TextView tvStartDate,tvEndDate;
    private boolean selectStartDate = true;

    private boolean gbNoToll = true;
    private MultiButtonsPopupDialog gMultiButtonsPopupDialog;
    private boolean gbHasError = false;
    private String errorMessage;
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
        setContentView(R.layout.activity_ott_calculate_for_me);
        if(savedInstanceState != null)
        {
            TollRoadsApp.getInstance().gOTTUserInfoRequest = (OTTUserInfoRequest) savedInstanceState.getSerializable("OTTUserInfoRequest");
        }

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvGoNext = (TextView)findViewById(R.id.tv_go_next);

        llNoToll = (LinearLayout)findViewById(R.id.ll_no_toll_alert) ;
        llToll = (LinearLayout)findViewById(R.id.ll_toll_alert);
        tvTotalDue = (TextView)findViewById(R.id.tv_total_due);

        layoutDatePicker = (RelativeLayout)findViewById(R.id.layout_datePicker);
        datePicker = (DatePicker)findViewById(R.id.datePicker);
        tvDatePickerConfirm = (TextView)findViewById(R.id.tv_datePicker_ok);
        tvDatePickerCancel = (TextView)findViewById(R.id.tv_datePicker_cancel);
        tvStartDate = (TextView)findViewById(R.id.tv_start_date);
        tvEndDate = (TextView)findViewById(R.id.tv_end_date);

        setupListener();
        String endDate, endDateStr, startDate, startDateStr;
        if(savedInstanceState == null)
        {
            Calendar c = Calendar.getInstance();

            endDate = TollRoadsApp.formatDateBySystem(c.getTimeInMillis());
            endDateStr = c.get(Calendar.YEAR) + "-" + String.format("%02d", c.get(Calendar.MONTH) + 1)
                    + "-" + String.format("%02d", c.get(Calendar.DAY_OF_MONTH));


            c.setTimeInMillis(c.getTimeInMillis() - Resource.ONE_DAY*6L);  //7 days include today
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

        retrieveTolls();
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

                //retrieve tolls request
                retrieveTolls();
            }
        });
        tvDatePickerCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutDatePicker.setVisibility(View.GONE);
            }
        });

    }

    private void showCalcResult(double amount)
    {
        if(amount > 0)
        {
            tvGoNext.setText(R.string.pay);
            llToll.setVisibility(View.VISIBLE);
            llNoToll.setVisibility(View.GONE);

            TollRoadsApp.getInstance().gOTTUserInfoRequest.setTotal_amount(amount);
            tvTotalDue.setText(getString(R.string.your_total_due, amount));
        }
        else
        {
            tvGoNext.setText(R.string.next);
            llToll.setVisibility(View.GONE);
            llNoToll.setVisibility(View.VISIBLE);
            TollRoadsApp.getInstance().gOTTUserInfoRequest.setTotal_amount(0.0);
        }
    }
    private void retrieveTolls()
    {
//        if(gbNoToll)
//        {
//            tvGoNext.setText(R.string.next);
//            llToll.setVisibility(View.GONE);
//            llNoToll.setVisibility(View.VISIBLE);
//            TollRoadsApp.getInstance().gOTTUserInfoRequest.setTotal_amount(0.0);
//        }
//        else
//        {
//            tvGoNext.setText(R.string.pay);
//            llToll.setVisibility(View.VISIBLE);
//            llNoToll.setVisibility(View.GONE);
//
//            TollRoadsApp.getInstance().gOTTUserInfoRequest.setTotal_amount(21.49);
//            tvTotalDue.setText(getString(R.string.your_total_due, "$21.49"));
//        }
//
//        gbNoToll = !gbNoToll;
        TollRoadsApp.getInstance().gOTTUserInfoRequest.setCalc_start_date((String) tvStartDate.getTag());
        TollRoadsApp.getInstance().gOTTUserInfoRequest.setCalc_end_date((String) tvEndDate.getTag());

        String params = TollRoadsApp.populateOTTParams();

        showProgressDialog();
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                closeProgressDialog();
                gbHasError = true;
                errorMessage = getString(R.string.network_error_retry);
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
                gbHasError = true;
                errorMessage = getString(R.string.network_error_retry);
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, "response:" + response);
                        if(checkResponse(response)) {
                            Gson gson = new GsonBuilder().serializeNulls().create();
                            CalcTripInfoResponse calcTripInfoResponse = gson.fromJson(response, CalcTripInfoResponse.class);
                            showCalcResult(calcTripInfoResponse.getAmount_due());
                            gbHasError = false;
                        }
                        else
                        {
                            Gson gson = new GsonBuilder().serializeNulls().create();
                            CommonResponse commonResponse = gson.fromJson(response, CommonResponse.class);

                            errorMessage = commonResponse.getMessage();
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

        ServerDelegate.calcTripInfo(Resource.URL_PAY_GO, params, listener, errorListener);
    }

    private void showUpPrivacyPopupDialog()
    {
        gMultiButtonsPopupDialog = new MultiButtonsPopupDialog(this,R.style.CustomProgressDialog);
        gMultiButtonsPopupDialog.createDialog(this);
        gMultiButtonsPopupDialog.CutomizeTitle(View.GONE, "");

        gMultiButtonsPopupDialog.CutomizeContent(View.VISIBLE, getString(R.string.privacy_popup),
                getResources().getColor(R.color.colorRed), Gravity.START);
        gMultiButtonsPopupDialog.CutomizePositiveButton(View.GONE, 0, "", null);
        gMultiButtonsPopupDialog.CutomizeExtraButton(View.VISIBLE, 0, getString(R.string.i_understand), getResources().getColor(R.color.colorDarkGray), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(OTTCalculateForMeActivity.this, OTTContactInfoActivity.class,
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
        });
        gMultiButtonsPopupDialog.CutomizeNeutralButton(View.GONE, 0, "", null);
        gMultiButtonsPopupDialog.CutomizeNegativeButton(View.GONE, 0, getString(R.string.cancel), null);

        gMultiButtonsPopupDialog.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            gotoActivity(getApplicationContext(), OTTCalculateTollActivity.class,
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
                gotoActivity(v.getContext(), OTTCalculateTollActivity.class,
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

            }
        });

        tvGoNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gbHasError)
                {
                    showDialog(getString(R.string.dialog_title_error),
                            errorMessage, getString(R.string.ok), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int position)
                                {
                                    dialog.dismiss();
                                }
                            }, false);
                }
                else {
                    showUpPrivacyPopupDialog();
                }

            }
        });
        setupDatePicker();
    }
}
