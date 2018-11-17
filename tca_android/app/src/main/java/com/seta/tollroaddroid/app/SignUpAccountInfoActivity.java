package com.seta.tollroaddroid.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seta.tollroaddroid.app.api.Convert;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.json.CommonResponse;
import com.seta.tollroaddroid.app.json.SignUpRequest;
import com.seta.tollroaddroid.app.utilities.Constants;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import java.io.UnsupportedEncodingException;

public class SignUpAccountInfoActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private TextView tvGoNext;
    private ImageView ivPinHint,ivPasswordHint, ivUserNameHint;
    private RelativeLayout rlFastrak, rlExpressCharge, rlExpressPrepaid, rlExpressInvoice;
    private ImageView ivFastrak, ivExpressCharge, ivExpressPrepaid, ivExpressInvoice;
    
    private int widgetWidth =0;
    private DisplayMetrics gDm;
    private EditText etUserName,etPassword, etRetypePassword,etPin;
    private Response.ErrorListener errorListener;
    private TextView tvCompareAccount;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("CurrentSignUpReq", TollRoadsApp.getInstance().gSignUpRequest);

        outState.putBoolean(Constants.FROM_OTT,TollRoadsApp.getInstance().gFromOTT);
    }

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit SignUp_1_account page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_account_info);
        if(savedInstanceState != null)
        {
            TollRoadsApp.getInstance().gSignUpRequest = (SignUpRequest) savedInstanceState.getSerializable("CurrentSignUpReq");
            TollRoadsApp.getInstance().gFromOTT = savedInstanceState.getBoolean(Constants.FROM_OTT,false);
        }
        else
        {
            TollRoadsApp.getInstance().gSignUpRequest = new SignUpRequest();
        }
        FlurryAgent.logEvent("Enter SignUp_1_account page.");

        Intent intent = getIntent();
        if(intent != null)
        {
            TollRoadsApp.getInstance().gFromOTT = intent.getBooleanExtra(Constants.FROM_OTT,false);

        }
        TollRoadsApp.getInstance().selectedAccountType = Constants.ACCOUNT_TYPE_FASTRAK_INDIVIDUAL;

        tvCompareAccount = (TextView)findViewById(R.id.tv_compare_account);

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvGoNext = (TextView) findViewById(R.id.tv_go_next);
        rlFastrak = (RelativeLayout)findViewById(R.id.rl_fastrak);
        rlExpressCharge = (RelativeLayout)findViewById(R.id.rl_express_charge);
        rlExpressPrepaid = (RelativeLayout)findViewById(R.id.rl_express_prepaid);
        rlExpressInvoice = (RelativeLayout)findViewById(R.id.rl_express_invoice);

        ivFastrak = (ImageView)findViewById(R.id.iv_fastrak);
        ivExpressCharge = (ImageView)findViewById(R.id.iv_express_charge);
        ivExpressPrepaid = (ImageView)findViewById(R.id.iv_express_prepaid);
        ivExpressInvoice = (ImageView)findViewById(R.id.iv_express_invoice);

        etUserName = (EditText)findViewById(R.id.et_user_name);
        etPassword = (EditText)findViewById(R.id.et_password);
        etRetypePassword = (EditText)findViewById(R.id.et_re_type);

        etPin = (EditText)findViewById(R.id.et_pin);

        ivUserNameHint = (ImageView)findViewById(R.id.iv_user_name_hint);
        ivPinHint = (ImageView)findViewById(R.id.iv_pin_hint);
        ivPasswordHint = (ImageView)findViewById(R.id.iv_password_hint);


        gDm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(gDm);
        Convert.mDensity = getResources().getDisplayMetrics().density;
        widgetWidth = (gDm.widthPixels - Convert.dpToPx(100))/2;

        initWidgetValue();
        setupListener();
    }

    private void refreshBackground()
    {
        if(TollRoadsApp.getInstance().selectedAccountType == Constants.ACCOUNT_TYPE_FASTRAK_INDIVIDUAL)
        {
            rlFastrak.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            ivFastrak.setImageResource(R.drawable.ic_tca_fastrak_white);
        }
        else
        {
            rlFastrak.setBackgroundResource(R.drawable.white_background_primary_frame_bg);
            ivFastrak.setImageResource(R.drawable.ic_tca_fastrak_colour);
        }

        if(TollRoadsApp.getInstance().selectedAccountType == Constants.ACCOUNT_TYPE_CHARGE_EXPRESS)
        {
            rlExpressCharge.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            ivExpressCharge.setImageResource(R.drawable.ic_tca_expresscharge_white);
        }
        else
        {
            rlExpressCharge.setBackgroundResource(R.drawable.white_background_primary_frame_bg);
            ivExpressCharge.setImageResource(R.drawable.ic_tca_expresscharge_colour);
        }

        if(TollRoadsApp.getInstance().selectedAccountType == Constants.ACCOUNT_TYPE_PREPAID_EXPRESS)
        {
            rlExpressPrepaid.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            ivExpressPrepaid.setImageResource(R.drawable.ic_tca_expressprepaid_white);
        }
        else
        {
            rlExpressPrepaid.setBackgroundResource(R.drawable.white_background_primary_frame_bg);
            ivExpressPrepaid.setImageResource(R.drawable.ic_tca_expressprepaid_colour);
        }

        if(TollRoadsApp.getInstance().selectedAccountType == Constants.ACCOUNT_TYPE_INVOICE_EXPRESS)
        {
            rlExpressInvoice.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            ivExpressInvoice.setImageResource(R.drawable.ic_tca_expressinvoice_white);
        }
        else
        {
            rlExpressInvoice.setBackgroundResource(R.drawable.white_background_primary_frame_bg);
            ivExpressInvoice.setImageResource(R.drawable.ic_tca_expressinvoice_colour);
        }
    }

    private void setupListener()
    {
        tvCompareAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //gotoActivity(v.getContext(),CompareAccountsActivity.class);

                Bundle bundle = new Bundle();
                bundle.putString(Resource.KEY_URL, Resource.COMPARE_ACCOUNT_URL);
                bundle.putString(Resource.KEY_TITLE, getString(R.string.compare_accounts));

                gotoActivity(v.getContext(),WebActivity.class, bundle);
            }
        });
        ivPinHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToastMessage(getString(R.string.pin_hint));
            }
        });
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
                gotoActivity(getApplicationContext(), LandingPageActivity.class,
                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
        });
        rlFastrak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TollRoadsApp.getInstance().selectedAccountType = Constants.ACCOUNT_TYPE_FASTRAK_INDIVIDUAL;
                refreshBackground();
            }
        });
        rlExpressCharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TollRoadsApp.getInstance().selectedAccountType = Constants.ACCOUNT_TYPE_CHARGE_EXPRESS;
                refreshBackground();
            }
        });

        rlExpressPrepaid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TollRoadsApp.getInstance().selectedAccountType = Constants.ACCOUNT_TYPE_PREPAID_EXPRESS;
                refreshBackground();
            }
        });
        rlExpressInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TollRoadsApp.getInstance().selectedAccountType = Constants.ACCOUNT_TYPE_INVOICE_EXPRESS;
                refreshBackground();
            }
        });

        tvGoNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkValidation())
                {
                    showProgressDialog();
                    setRequest();
                    String params = "account_username=" + Uri.encode(etUserName.getText().toString());
                    params = params + "&account_password=" + Uri.encode(etPassword.getText().toString());
                    params = params + "&confirm_password=" + Uri.encode(etRetypePassword.getText().toString());
                    params = params + "&account_pin=" + Uri.encode(etPin.getText().toString());
                    checkUserNameRequest(Resource.URL_ENROLLMENT, params);
               }

            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            gotoActivity(getApplicationContext(), LandingPageActivity.class,
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void checkUserNameRequest(String url,String params)
    {
        Response.Listener listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                closeProgressDialog();
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, "response:"+response);

                        Gson gson = new GsonBuilder().serializeNulls().create();
                        CommonResponse commonResponse = gson.fromJson(response, CommonResponse.class);

                        if(checkUserNameResponse(response)) {
                            if(commonResponse.getStatus() == 200) {
                                TollRoadsApp.getInstance().gSignUpRequest.setTokenID(commonResponse.getTokenID());
                                gotoActivity(SignUpAccountInfoActivity.this,
                                        SignUpContactInfoActivity.class,
                                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            }
                            else
                            {
                                showToastMessage(commonResponse.getMessage());
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

        ServerDelegate.checkUserRequest(url, params, listener, errorListener);
    }
    private void setRequest()
    {
        TollRoadsApp.getInstance().gSignUpRequest.setAccount_username(etUserName.getText().toString());
        TollRoadsApp.getInstance().gSignUpRequest.setAccount_password(etPassword.getText().toString());
        TollRoadsApp.getInstance().gSignUpRequest.setConfirm_password(etRetypePassword.getText().toString());

        TollRoadsApp.getInstance().gSignUpRequest.setAccount_type(TollRoadsApp.getInstance().selectedAccountType);

        if(!etPin.getText().toString().isEmpty())
        {
            TollRoadsApp.getInstance().gSignUpRequest.setAccount_pin(Uri.encode(etPin.getText().toString()));
        }
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
        else if(etPin.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.pin_empty_warning));
        }
        else if(etPin.getText().length() != 4)
        {
            ret = false;
            showToastMessage(getString(R.string.pin_length_error));
        }
        return ret;
    }

    private void initWidgetValue()
    {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) rlFastrak.getLayoutParams();
        layoutParams.width = widgetWidth;
        layoutParams.height = layoutParams.width;

        rlFastrak.setLayoutParams(layoutParams);
        rlExpressCharge.setLayoutParams(layoutParams);
        rlExpressPrepaid.setLayoutParams(layoutParams);
        rlExpressInvoice.setLayoutParams(layoutParams);
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
