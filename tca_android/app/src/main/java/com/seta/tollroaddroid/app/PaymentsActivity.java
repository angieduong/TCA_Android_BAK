package com.seta.tollroaddroid.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.seta.tollroaddroid.app.GooglePay.PaymentsUtil;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.json.AccountInfo;
import com.seta.tollroaddroid.app.json.RecentToll;
import com.seta.tollroaddroid.app.utilities.Constants;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PaymentsActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private Response.ErrorListener errorListener;
    private List<RecentToll> recentPayments = new ArrayList<RecentToll>();
    private ListView lvMenu;
    private TextView tvPaymentMethod,tvMakeAPayment, tvMakePayment;
    private TextView tvBalance;
    private EditText etAmount;
    private TextView tvAmount;
    private LinearLayout llMakePaymentPopup, llSelectPaymentType;
    private RelativeLayout rlMakePaymentPopupCancel;
    private boolean selectPaymentTypeMode = false;
    private LinearLayout llCreditCard, llEcheck, llGooglePay, llSamsungPay;
    private PaymentsClient mPaymentsClient;

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit Account_Payment page.");
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            backButtonHandler();
            return  true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peyments);
        FlurryAgent.logEvent("Enter Account_Payment page.");

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        lvMenu = (ListView)findViewById(R.id.lv_menu);
        tvPaymentMethod = (TextView)findViewById(R.id.tv_payment_method);
        tvMakeAPayment = (TextView)findViewById(R.id.tv_make_a_payment);
        tvBalance = (TextView)findViewById(R.id.tv_balance);

        llMakePaymentPopup = (LinearLayout)findViewById(R.id.ll_make_payment_popup);
        llSelectPaymentType = (LinearLayout) findViewById(R.id.ll_select_payment_type);

        llCreditCard = (LinearLayout)findViewById(R.id.ll_credit_card);
        llEcheck = (LinearLayout)findViewById(R.id.ll_electronic_check);
        llGooglePay = (LinearLayout)findViewById(R.id.ll_google_pay);
        llSamsungPay = (LinearLayout)findViewById(R.id.ll_samsung_pay);

        tvMakePayment = (TextView)findViewById(R.id.tv_make_payment);
        rlMakePaymentPopupCancel = (RelativeLayout)findViewById(R.id.rl_make_payment_popup_cancel);
        etAmount = (EditText)findViewById(R.id.et_amount);
        etAmount.setCursorVisible(false);
        tvAmount = (TextView)findViewById(R.id.tv_amount);
        tvAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etAmount.requestFocus();
                InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(etAmount, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().isEmpty())
                {
                    tvAmount.setText(String.format("%.2f",(Float.valueOf(s.toString())/100.0f)));
                }
                else
                {
                    tvAmount.setText("0.00");
                }
            }
        });

        setupListener();
        initMyAccount();
        refreshMyBalance();

        getRecentPayments();
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

    private void initMyAccount()
    {
        if(!TollRoadsApp.getInstance().getMyAccount().isEmpty()) {
            Gson gson = new GsonBuilder().serializeNulls().create();

            TollRoadsApp.getInstance().accountInfo = gson.fromJson(TollRoadsApp.getInstance().getMyAccount(),
                    AccountInfo.class);
        }

        if(TollRoadsApp.getInstance().accountInfo != null &&
                (TollRoadsApp.getInstance().accountInfo.getAccount_type()
                        == Constants.ACCOUNT_TYPE_CHARGE_EXPRESS) ||
                (TollRoadsApp.getInstance().accountInfo.getAccount_type()
                        == Constants.ACCOUNT_TYPE_INVOICE_EXPRESS))
        {
            llEcheck.setVisibility(View.GONE);
        }
        else
        {
            llEcheck.setVisibility(View.VISIBLE);
        }
    }

    private void refreshMyBalance()
    {
        AccountInfo accountInfo = TollRoadsApp.getInstance().accountInfo;

        if(accountInfo != null)
        {
            int accountType = accountInfo.getAccount_type();

            if(accountType == Constants.ACCOUNT_TYPE_FASTRAK_INDIVIDUAL ||
                    accountType == Constants.ACCOUNT_TYPE_FASTRAK_COMMERCIAL)
            {
                tvBalance.setText(getString(R.string.current_balance, accountInfo.getBalance()));
            }
            else
            {
                if(accountInfo.getBalance() <= 0)
                {
                    tvBalance.setText(getString(R.string.balance_due, accountInfo.getBalance()));
                }
                else
                {
                    tvBalance.setText(getString(R.string.available_toll_credit, accountInfo.getBalance()));
                }
            }
            tvBalance.setVisibility(View.VISIBLE);
        }
    }

    private void backButtonHandler()
    {
        if(selectPaymentTypeMode) {
            selectPaymentTypeMode = false;
            llSelectPaymentType.setVisibility(View.GONE);
        }
        else {
            finish();
        }
    }

    private void setupListener()
    {
        rlMakePaymentPopupCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llMakePaymentPopup.setVisibility(View.GONE);
            }
        });
        tvMakePayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etAmount.getText().length() == 0 || tvAmount.getText().toString().equals("0.00"))
                {
                    showToastMessage(getString(R.string.amount_empty_warning));
                }
                else {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(etAmount.getWindowToken(), 0);
                    }

                    selectPaymentTypeMode = true;
                    llSelectPaymentType.setVisibility(View.VISIBLE);
                }
            }
        });

        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backButtonHandler();
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

        tvPaymentMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(v.getContext(), PaymentMethodsActivity.class);
            }
        });
        tvMakeAPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //gotoActivityForResult(v.getContext(), MakePaymentActivity.class, Constants.MAKE_PAYMENT_REQUEST_CODE);
                llMakePaymentPopup.setVisibility(View.VISIBLE);
            }
        });

        llCreditCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt(Resource.KEY_PAYMENT_TYPE, Constants.CREDIT_CARD_TYPE);
                bundle.putString(Resource.KEY_AMOUNT, tvAmount.getText().toString());

                gotoActivityForResult(v.getContext(), MakePaymentNewActivity.class, bundle,
                        Constants.MAKE_PAYMENT_REQUEST_CODE);
            }
        });

        llEcheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt(Resource.KEY_PAYMENT_TYPE, Constants.ELECTRONIC_CHECK_TYPE);
                bundle.putString(Resource.KEY_AMOUNT, tvAmount.getText().toString());

                gotoActivityForResult(v.getContext(), MakePaymentNewActivity.class, bundle,
                        Constants.MAKE_PAYMENT_REQUEST_CODE);
            }
        });

        llGooglePay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt(Resource.KEY_PAYMENT_TYPE, Constants.GOOGLE_PAY_TYPE);
                bundle.putString(Resource.KEY_AMOUNT, tvAmount.getText().toString());

                gotoActivityForResult(v.getContext(), MakePaymentNewActivity.class, bundle,
                        Constants.MAKE_PAYMENT_REQUEST_CODE);
            }
        });

        llSamsungPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Constants.MAKE_PAYMENT_REQUEST_CODE && resultCode == RESULT_OK)
        {
            selectPaymentTypeMode = false;
            llSelectPaymentType.setVisibility(View.GONE);
            llMakePaymentPopup.setVisibility(View.GONE);
            etAmount.setText("");
            tvAmount.setText("0.00");

            sendAccountRequest();
            getRecentPayments();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private SimpleAdapter recentPaymentListAdapter;
    private SimpleAdapter getRecentPaymentMenuAdapter() {
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

        for (int i = 0; i < recentPayments.size(); i++) {
            RecentToll recentPayment = recentPayments.get(i);

            HashMap<String, Object> map = new HashMap<String, Object>();
            String dateTime = recentPayment.getDatetime();
            int spaceIndex = dateTime.indexOf(" ");
            String date = dateTime.substring(0, spaceIndex);
            String time = dateTime.substring(spaceIndex+1);

            map.put("tv_date", date);
            map.put("tv_time", time);

            map.put("tv_amount", recentPayment.getAmount());
            map.put("tv_description", recentPayment.getDescription());
            data.add(map);
        }

        SimpleAdapter recentPaymentListAdapter = new SimpleAdapter(
                this, data,
                R.layout.item_recent_payment_menu,
                new String[] { "tv_date", "tv_time","tv_amount", "tv_description"}, new int[] { R.id.tv_date,R.id.tv_time,
                R.id.tv_amount, R.id.tv_description });

        return recentPaymentListAdapter;
    }

    private void loadRecentTollMenu()
    {
        recentPaymentListAdapter = getRecentPaymentMenuAdapter();
        lvMenu.setAdapter(recentPaymentListAdapter);

        setTotalHeightToListView(lvMenu);
    }
    
    private void getRecentPayments()
    {
        Response.Listener listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                closeProgressDialog();
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, response.toString());

                        if(checkResponse(response.toString())) {
                            if(response.has(Resource.KEY_INFO))
                            {
                                String info = response.optString(Resource.KEY_INFO);
                                Gson gson = new GsonBuilder().serializeNulls().create();
                                Type listType = new TypeToken<ArrayList<RecentToll>>() {}.getType();

                                recentPayments = gson.fromJson(info, listType);
                                loadRecentTollMenu();
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

        ServerDelegate.getRecentPayments(Resource.URL_PAYMENT, listener, errorListener);
    }

    private void sendAccountRequest()
    {
        showProgressDialog();
        Response.Listener listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                closeProgressDialog();
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, response.toString());

                        if(checkResponse(response.toString())) {
                            if(response.has(Resource.KEY_INFO))
                            {
                                String info = response.optString(Resource.KEY_INFO);
                                Gson gson = new GsonBuilder().serializeNulls().create();

                                TollRoadsApp.getInstance().accountInfo = gson.fromJson(info, AccountInfo.class);
                                TollRoadsApp.getInstance().setMyAccount(info);
                                refreshMyBalance();
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

        ServerDelegate.sendAccountRequest(Resource.URL_ACCOUNT, listener, errorListener);
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
