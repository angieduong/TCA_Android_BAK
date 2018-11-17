package com.seta.tollroaddroid.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.PaymentsClient;
import com.seta.tollroaddroid.app.GooglePay.PaymentsUtil;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.custom.MultiButtonsPopupDialog;
import com.seta.tollroaddroid.app.json.PaySelectedViolationsRequest;
import com.seta.tollroaddroid.app.json.SelectedUnpaidViolationsResponse;
import com.seta.tollroaddroid.app.utilities.Constants;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import java.io.UnsupportedEncodingException;

public class ViolationSelectPaymentActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();
    private ImageView ivGoBack;
    private LinearLayout llCreditCard, llEcheck, llGooglePay, llSamsungPay;

    private TextView tvTotalCharge;
    private PaymentsClient mPaymentsClient;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("SelectedUnpaidViolationsResponse", TollRoadsApp.getInstance().selectedUnpaidViolationsResponse);
        outState.putSerializable("PaySelectedViolationsRequest", TollRoadsApp.getInstance().paySelectedViolationsRequest);
    }

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit violation_select_payment page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_violation_select_payment);
        FlurryAgent.logEvent("Enter violation_select_payment page.");
        if(savedInstanceState != null)
        {
            TollRoadsApp.getInstance().selectedUnpaidViolationsResponse = (SelectedUnpaidViolationsResponse)
                    savedInstanceState.getSerializable("SelectedUnpaidViolationsResponse");
            TollRoadsApp.getInstance().paySelectedViolationsRequest = (PaySelectedViolationsRequest)
                    savedInstanceState.getSerializable("PaySelectedViolationsRequest");
        }

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);

        llCreditCard = (LinearLayout)findViewById(R.id.ll_credit_card);
        llEcheck = (LinearLayout)findViewById(R.id.ll_electronic_check);
        llGooglePay = (LinearLayout)findViewById(R.id.ll_google_pay);
        llSamsungPay = (LinearLayout)findViewById(R.id.ll_samsung_pay);

        tvTotalCharge = (TextView)findViewById(R.id.tv_total_charge);

        setupListener();
        // It's recommended to create the PaymentsClient object inside of the onCreate method.
        mPaymentsClient = PaymentsUtil.createPaymentsClient(this);

        //TODO:temporarily remove the google pay for now
        //checkIsReadyToPay();
    }

    private void setPwgAvailable(boolean available) {
        // If isReadyToPay returned true, show the button and hide the "checking" text. Otherwise,
        // notify the user that Pay with Google is not available.
        // Please adjust to fit in with your current user flow. You are not required to explicitly
        // let the user know if isReadyToPay returns false.
        if (available) {
            llGooglePay.setVisibility(View.VISIBLE);
        } else {
            llGooglePay.setVisibility(View.GONE);
        }
    }

    private void checkIsReadyToPay() {
        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        PaymentsUtil.isReadyToPay(mPaymentsClient).addOnCompleteListener(
                new OnCompleteListener<Boolean>() {
                    public void onComplete(Task<Boolean> task) {
                        try {
                            boolean result = task.getResult(ApiException.class);
                            setPwgAvailable(result);
                        } catch (ApiException exception) {
                            // Process error
                            Log.w("isReadyToPay failed", exception);
                        }
                    }
                });
    }


    private void setupListener()
    {
        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                gotoActivity(v.getContext(), ViolationContactInfoActivity.class, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
        });

        llCreditCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TollRoadsApp.getInstance().paySelectedViolationsRequest.
                        setPayment_type(Constants.CREDIT_CARD_TYPE);
                gotoActivity(v.getContext(), ViolationPaymentInfoActivity.class, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
        });

        llEcheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TollRoadsApp.getInstance().paySelectedViolationsRequest.
                        setPayment_type(Constants.ELECTRONIC_CHECK_TYPE);
                gotoActivity(v.getContext(), ViolationPaymentInfoActivity.class, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
        });

        llGooglePay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TollRoadsApp.getInstance().paySelectedViolationsRequest.
                        setPayment_type(Constants.GOOGLE_PAY_TYPE);
                gotoActivity(v.getContext(), ViolationPaymentInfoActivity.class);
            }
        });

        llSamsungPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void setTotalCharge()
    {
        if(TollRoadsApp.getInstance().selectedUnpaidViolationsResponse != null &&
                TollRoadsApp.getInstance().selectedUnpaidViolationsResponse.getSel_unpaid_vios_total_amount_due() != null)
        {
            tvTotalCharge.setText(getString(R.string.total_amount_due,
                    TollRoadsApp.getInstance().selectedUnpaidViolationsResponse.getSel_unpaid_vios_total_amount_due()));

            tvTotalCharge.setVisibility(View.VISIBLE);
        }
        else
        {
            tvTotalCharge.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTotalCharge();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            gotoActivity(this, ViolationContactInfoActivity.class, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            return  true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
