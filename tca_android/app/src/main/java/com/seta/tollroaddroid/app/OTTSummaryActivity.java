package com.seta.tollroaddroid.app;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seta.tollroaddroid.app.api.Convert;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.json.AutoPopulatePaymentInfo;
import com.seta.tollroaddroid.app.json.AutoPopulateVehicleAndContactInfo;
import com.seta.tollroaddroid.app.json.OTTUserInfoRequest;
import com.seta.tollroaddroid.app.json.OttTrip;
import com.seta.tollroaddroid.app.utilities.Constants;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class OTTSummaryActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;

    private TextView tvTotalCharge,tvPay;
    
    private TextView tvPlate,tvCountry, tvState, tvRentalStartDate, tvRentalEndDate;
    private TextView tvCardNo, tvNameOnCard, tvExpirationDate, tvZipCode,tvCVV,
            tvContactAddress, tvCityAndState;
    private TextView tvRoutingNo, tvFirstName, tvLastName, tvAccountNo;
    private TextView tvContactName, tvEmail;

    private ScrollView svSummary;
    private LinearLayout llRentalTrip, llTrip;
    private LinearLayout llCreditCard, llECheck;
    private LinearLayout llTrailer;
    private TextView tvTrailerPlate,tvTrailerCountry, tvTrailerState;
    private ListView lvTripDetail;
    private SimpleAdapter tripDetailListAdapter;
    private DisplayMetrics gDm = new DisplayMetrics();
    private RelativeLayout rlStates, rlTrailerStates;
    private Switch swAutoPopulateVehicle, swAutoPopulatePayment;

    private EncryptPaymentTask mAuthTask;

    private static final String KEY_ALIAS = "tca_payment";
    private static final String KEYSTORE = "AndroidKeyStore";
    private static final String PREFERENCES_KEY_PAYMENT = "payment";
    private static final String PREFERENCES_KEY_IV = "payment_iv";

    private KeyStore keyStore;
    private KeyGenerator generator;
    private Cipher cipher;

    private SharedPreferences sharedPreferences;

    private void fillPaymentInfo(AutoPopulatePaymentInfo autoPopulatePaymentInfo)
    {
        OTTUserInfoRequest ottUserInfoRequest = TollRoadsApp.getInstance().gOTTUserInfoRequest;
        autoPopulatePaymentInfo.setPayment_method(ottUserInfoRequest.getPayment_method());
        autoPopulatePaymentInfo.setAccount_number(ottUserInfoRequest.getAccount_number());
        autoPopulatePaymentInfo.setCard_holder_name(ottUserInfoRequest.getCard_holder_name());
        autoPopulatePaymentInfo.setCard_number(ottUserInfoRequest.getCard_number());
        autoPopulatePaymentInfo.setCvv2(ottUserInfoRequest.getCvv2());
        autoPopulatePaymentInfo.setExpired_date(ottUserInfoRequest.getExpired_date());
        autoPopulatePaymentInfo.setFirst_name(ottUserInfoRequest.getFirst_name());
        autoPopulatePaymentInfo.setLast_name(ottUserInfoRequest.getLast_name());
        autoPopulatePaymentInfo.setRouting_number(ottUserInfoRequest.getRouting_number());
        autoPopulatePaymentInfo.setZip_code(ottUserInfoRequest.getZip_code());

    }
    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptEncryptPayment() {
        if (!TollRoadsApp.getInstance().testFingerPrintSettings())
            return;

        if (mAuthTask != null) {
            return;
        }

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        showProgress(true);

        AutoPopulatePaymentInfo autoPopulatePaymentInfo = new AutoPopulatePaymentInfo();
        fillPaymentInfo(autoPopulatePaymentInfo);

        Gson gson= new GsonBuilder().serializeNulls().create();
        String paymentInfo = gson.toJson(autoPopulatePaymentInfo,
                AutoPopulatePaymentInfo.class);

        mAuthTask = new EncryptPaymentTask(paymentInfo);
        mAuthTask.execute((Void) null);

    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        if(show)
        {
            showProgressDialog();
        }
        else
        {
            closeProgressDialog();
        }
    }

    public void encryptString(String initialText) {
        print("Encrypting...");
        try {
            Log.e("encrypt","Encrypting..");
            byte[] bytes = cipher.doFinal(initialText.getBytes());
            //String finalText = new String(bytes);
            Log.e("encrypt","initialText:"+initialText);
            String encryptedText = Base64.encodeToString(bytes, Base64.NO_WRAP);

            Log.e("encrypt","initialText:"+initialText+",encryptedText:"+encryptedText);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PREFERENCES_KEY_PAYMENT, encryptedText);
            editor.commit();

//            print(encryptedText);
        } catch (Exception e) {
            print(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class EncryptPaymentTask extends AsyncTask<Void, Void, Boolean> {

        private final String mInfo;


        EncryptPaymentTask(String info) {
            mInfo = info;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (!getKeyStore())
                return false;

            if (!createNewKey(true))
                return false;

            // Inside doInBackground
            if (!getCipher())
                return false;

            // Inside doInBackground

            if (!initCipher(Cipher.ENCRYPT_MODE))
                return false;

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            onCancelled();

            if (!success) {
                print("Fingerprint setup failed!");
            } else {

                encryptString(mInfo);
                Toast.makeText(OTTSummaryActivity.this, getString(R.string.fingerprint_payment_enabled), Toast.LENGTH_LONG).show();

                print("Authenticate using fingerprint!");
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public void print(String text) {
        if(text != null && !text.isEmpty()) {
            Log.d("settings", text);
        }
    }

    public void print(int id) {
        print(getString(id));
    }

    private boolean getKeyStore() {
        print("Getting keystore...");
        try {
            keyStore = KeyStore.getInstance(KEYSTORE);
            keyStore.load(null); // Create empty keystore
            return true;
        } catch (KeyStoreException e) {
            print(e.getMessage());
        } catch (CertificateException e) {
            print(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            print(e.getMessage());
        } catch (IOException e) {
            print(e.getMessage());
        }

        return false;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean createNewKey(boolean forceCreate) {
        print("Creating new key...");
        try {
            if (forceCreate)
                keyStore.deleteEntry(KEY_ALIAS);

            if (!keyStore.containsAlias(KEY_ALIAS)) {
                generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE);

                generator.init(new KeyGenParameterSpec.Builder (KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .setUserAuthenticationRequired(false)
                        .build()
                );

                generator.generateKey();
                print("Key created.");
            } else
                print("Key exists.");

            return true;
        } catch (Exception e) {
            print(e.getMessage());
        }

        return false;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean getCipher() {
        print("Getting cipher...");
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);

            return true;
        } catch (NoSuchAlgorithmException e) {
            print(e.getMessage());
        } catch (NoSuchPaddingException e) {
            print(e.getMessage());
        }

        return false;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean initCipher(int mode) {
        print("Initializing cipher...");
        try {
            keyStore.load(null);
            SecretKey keyspec = (SecretKey)keyStore.getKey(KEY_ALIAS, null);

            if (mode == Cipher.ENCRYPT_MODE) {
                cipher.init(mode, keyspec);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(PREFERENCES_KEY_IV, Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP));
                editor.commit();
            }
            else {
                byte[] iv = Base64.decode(sharedPreferences.getString(PREFERENCES_KEY_IV, ""), Base64.NO_WRAP);
                IvParameterSpec ivspec = new IvParameterSpec(iv);
                cipher.init(mode, keyspec, ivspec);
            }

            return true;
//        } catch (KeyPermanentlyInvalidatedException e) {
//            print(e.getMessage());
//            createNewKey(true); // Retry after clearing entry
        } catch (Exception e) {
            print(e.getMessage());
        }

        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble("OTTTotalAmount", TollRoadsApp.getInstance().ottTotalAmount);
        outState.putInt("VehicleFound", TollRoadsApp.getInstance().gVehicleFound);
        outState.putSerializable("OTTUserInfoRequest", TollRoadsApp.getInstance().gOTTUserInfoRequest);
        outState.putSerializable("OttTrips", (Serializable) TollRoadsApp.getInstance().ottTrips);
    }

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit OTT_4_Summary page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ott_summary);
        if(savedInstanceState != null)
        {
            TollRoadsApp.getInstance().ottTotalAmount =
                    savedInstanceState.getDouble("OTTTotalAmount",0.0);
            TollRoadsApp.getInstance().gVehicleFound = savedInstanceState.getInt("VehicleFound");
            TollRoadsApp.getInstance().gOTTUserInfoRequest =
                    (OTTUserInfoRequest) savedInstanceState.getSerializable("OTTUserInfoRequest");
            TollRoadsApp.getInstance().ottTrips=
                    (List<OttTrip>) savedInstanceState.getSerializable("OttTrips");
        }
        FlurryAgent.logEvent("Enter OTT_4_Summary page.");

        getWindowManager().getDefaultDisplay().getMetrics(gDm);

        svSummary = (ScrollView)findViewById(R.id.sv_summary);
        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvPay = (TextView)findViewById(R.id.tv_pay);
        tvTotalCharge = (TextView)findViewById(R.id.tv_total_charge);
        llCreditCard = (LinearLayout)findViewById(R.id.ll_credit_card);
        llECheck = (LinearLayout)findViewById(R.id.ll_echeck);

        llRentalTrip = (LinearLayout)findViewById(R.id.ll_rental_trip_detail);
        llTrip = (LinearLayout)findViewById(R.id.ll_trip_detail);

        tvPlate = (TextView)findViewById(R.id.tv_license_plate);
        tvCountry = (TextView)findViewById(R.id.tv_country);
        tvState = (TextView)findViewById(R.id.tv_state);
        tvRentalStartDate = (TextView)findViewById(R.id.tv_start_date);
        tvRentalEndDate = (TextView)findViewById(R.id.tv_end_date);

        tvCardNo = (TextView)findViewById(R.id.tv_card_number);
        tvNameOnCard = (TextView)findViewById(R.id.tv_name_on_card);
        tvExpirationDate = (TextView)findViewById(R.id.tv_exp_date);
        tvZipCode = (TextView)findViewById(R.id.tv_billing_zip_code);
        tvCVV = (TextView)findViewById(R.id.tv_cvv);
        tvCityAndState = (TextView)findViewById(R.id.tv_billing_city_state);

        tvRoutingNo = (TextView)findViewById(R.id.tv_routing_no);
        tvFirstName = (TextView)findViewById(R.id.tv_first_name);
        tvLastName = (TextView)findViewById(R.id.tv_last_name);
        tvAccountNo = (TextView)findViewById(R.id.tv_account_no);

        tvContactName = (TextView)findViewById(R.id.tv_contact_name);
        tvContactAddress = (TextView)findViewById(R.id.tv_contact_address);
        tvEmail = (TextView)findViewById(R.id.tv_email);

        lvTripDetail = (ListView)findViewById(R.id.lv_trip_detail);

        llTrailer = (LinearLayout)findViewById(R.id.ll_trailer);
        tvTrailerPlate = (TextView)findViewById(R.id.tv_trailer_license_plate);
        tvTrailerCountry = (TextView)findViewById(R.id.tv_trailer_country);
        tvTrailerState = (TextView)findViewById(R.id.tv_trailer_state);
        rlStates = (RelativeLayout)findViewById(R.id.rl_state);
        rlTrailerStates = (RelativeLayout)findViewById(R.id.rl_trailer_state);

        swAutoPopulateVehicle = (Switch)findViewById(R.id.sw_auto_populate_vehicle);

        swAutoPopulatePayment = (Switch)findViewById(R.id.sw_auto_populate_payment);

        LinearLayout llSaveCreditCard = (LinearLayout)findViewById(R.id.ll_save_credit_card);

        if(Resource.enableAutoPopulatePayment)
        {
            llSaveCreditCard.setVisibility(View.VISIBLE);
        }
        else
        {
            llSaveCreditCard.setVisibility(View.GONE);
        }

        if(!TollRoadsApp.getInstance().testFingerPrintSettings())
        {
            swAutoPopulatePayment.setEnabled(false);
        }
        else
        {
            swAutoPopulatePayment.setEnabled(true);
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        setupListener();
    }

    private void setupListener()
    {
        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(v.getContext(), OTTSaveCreditCardActivity.class, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
        });

        tvPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(swAutoPopulateVehicle.isChecked())
                {
                    OTTUserInfoRequest ottUserInfoRequest = TollRoadsApp.getInstance().gOTTUserInfoRequest;
                    AutoPopulateVehicleAndContactInfo autoPopulateVehicleAndContactInfo = new AutoPopulateVehicleAndContactInfo();
                    
                    TollRoadsApp.getInstance().setAutoPopulateVehicleAndContact(true);
                    
                    autoPopulateVehicleAndContactInfo.setVehicle_class(ottUserInfoRequest.getVehicle_class());
                    autoPopulateVehicleAndContactInfo.setCountry1(ottUserInfoRequest.getCountry1());
                    autoPopulateVehicleAndContactInfo.setPlate1(ottUserInfoRequest.getPlate1());
                    autoPopulateVehicleAndContactInfo.setState1(ottUserInfoRequest.getState1());

                    autoPopulateVehicleAndContactInfo.setCountry2(ottUserInfoRequest.getCountry2());
                    autoPopulateVehicleAndContactInfo.setPlate2(ottUserInfoRequest.getPlate2());
                    autoPopulateVehicleAndContactInfo.setState2(ottUserInfoRequest.getState2());

                    autoPopulateVehicleAndContactInfo.setAddress_first_name(ottUserInfoRequest.getAddress_first_name());
                    autoPopulateVehicleAndContactInfo.setAddress_last_name(ottUserInfoRequest.getAddress_last_name());
                    autoPopulateVehicleAndContactInfo.setAddress1(ottUserInfoRequest.getAddress1());
                    autoPopulateVehicleAndContactInfo.setAddress_city(ottUserInfoRequest.getAddress_city());
                    autoPopulateVehicleAndContactInfo.setEmail_address(ottUserInfoRequest.getEmail_address());
                    autoPopulateVehicleAndContactInfo.setAddress_zipcode(ottUserInfoRequest.getAddress_zipcode());
                    autoPopulateVehicleAndContactInfo.setAddress_country(ottUserInfoRequest.getAddress_country());
                    autoPopulateVehicleAndContactInfo.setAddress_state(ottUserInfoRequest.getAddress_state());

                    Gson gson= new GsonBuilder().serializeNulls().create();
                    String jsonStr = gson.toJson(autoPopulateVehicleAndContactInfo,
                            AutoPopulateVehicleAndContactInfo.class);
                    TollRoadsApp.getInstance().setAutoPopulateVehicleAndContactInfo(jsonStr);
                }
                ottPayReq();
            }
        });

        swAutoPopulatePayment.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    attemptEncryptPayment();
                }
                else
                {

                }
                TollRoadsApp.getInstance().setAutoPopulatePayment(isChecked);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            gotoActivity(getApplicationContext(), OTTSaveCreditCardActivity.class,
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            return  true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private String populateParams()
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

    private void ottPayReq()
    {
        String params = populateParams();

        showProgressDialog();
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
                            JSONObject jsonObject = new JSONObject(response);
                            String confMessage = jsonObject.optString(Resource.KEY_CONF_MESSAGE);
                            Bundle bundle = new Bundle();
                            bundle.putString(Resource.KEY_CONF_MESSAGE, confMessage);
                            Gson gson = new Gson();

                            TollRoadsApp.getInstance().setOTTRequest(gson.toJson(
                                    TollRoadsApp.getInstance().gOTTUserInfoRequest,
                                    OTTUserInfoRequest.class
                            ));
                            gotoActivity(getApplicationContext(), OTTPayResultActivity.class,
                                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK,
                                    bundle);
                        }
                        else
                        {
                            closeProgressDialog();
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

        if(TollRoadsApp.getInstance().gVehicleFound == Constants.VEHICLE_FOUND_TYPE_RENTAL)
        {
            ServerDelegate.ottPayReq(Resource.URL_PAY_GO, params, listener, errorListener,true);
        }
        else
        {
            ServerDelegate.ottPayReq(Resource.URL_PAY_GO, params, listener, errorListener,false);
        }

    }

    private void initVehicleWidget()
    {
        OTTUserInfoRequest ottUserInfoRequest = TollRoadsApp.getInstance().gOTTUserInfoRequest;

        tvPlate.setText(ottUserInfoRequest.getPlate1());
        tvCountry.setText(TollRoadsApp.getCountryFullString(this,
                ottUserInfoRequest.getCountry1(),
                ottUserInfoRequest.getState1()));
        if(ottUserInfoRequest.getCountry1().isEmpty()) {
            tvState.setText(ottUserInfoRequest.getState1());
            rlStates.setVisibility(View.VISIBLE);
        }
        else
        {
            rlStates.setVisibility(View.GONE);
        }

        if(ottUserInfoRequest.getPlate2() != null && !ottUserInfoRequest.getPlate2().isEmpty()) {
            tvTrailerPlate.setText(ottUserInfoRequest.getPlate2());
            tvTrailerCountry.setText(TollRoadsApp.getCountryFullString(this,
                    ottUserInfoRequest.getCountry2(),
                    ottUserInfoRequest.getState2()));
            if(ottUserInfoRequest.getCountry2().isEmpty()) {
                tvTrailerState.setText(ottUserInfoRequest.getState2());
                rlTrailerStates.setVisibility(View.VISIBLE);
            }
            else
            {
                rlTrailerStates.setVisibility(View.GONE);
            }

            llTrailer.setVisibility(View.VISIBLE);
        }
        else
        {
            llTrailer.setVisibility(View.GONE);
        }
    }

    private String maskNumber(String src, int num)
    {
        if(src == null || src.isEmpty() || num < 0)
        {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0; i< src.length() -num; i++)
        {
            stringBuilder.append("*");
        }
        if(src.length() > num) {
            stringBuilder.append(src.substring(src.length() - num));
        }
        return stringBuilder.toString();
    }

    private void initPaymentWidget()
    {
        OTTUserInfoRequest ottUserInfoRequest = TollRoadsApp.getInstance().gOTTUserInfoRequest;
        if(ottUserInfoRequest.getPayment_method() == 6)
        {
            llCreditCard.setVisibility(View.VISIBLE);
            llECheck.setVisibility(View.GONE);

            String maskCardNumber = maskNumber(ottUserInfoRequest.getCard_number(), 4);
            String cityAndState = ottUserInfoRequest.getAddress_city() + "," +
                    ottUserInfoRequest.getAddress_state();
            tvCardNo.setText(maskCardNumber);
            tvNameOnCard.setText(ottUserInfoRequest.getCard_holder_name());
            tvExpirationDate.setText(ottUserInfoRequest.getExpired_date());
            tvZipCode.setText(ottUserInfoRequest.getZip_code());
            tvCVV.setText(ottUserInfoRequest.getCvv2());

            tvCityAndState.setText(cityAndState);

        } else {
            llCreditCard.setVisibility(View.GONE);
            llECheck.setVisibility(View.VISIBLE);
            String maskRoutingNumber = maskNumber(ottUserInfoRequest.getRouting_number(), 3);
            String maskAccountNumber = maskNumber(ottUserInfoRequest.getAccount_number(), 3);

            tvRoutingNo.setText(maskRoutingNumber);
            tvFirstName.setText(ottUserInfoRequest.getFirst_name());
            tvLastName.setText(ottUserInfoRequest.getLast_name());
            tvAccountNo.setText(maskAccountNumber);
        }

        tvContactName.setText(ottUserInfoRequest.getAddress_first_name() + " " +
                ottUserInfoRequest.getAddress_last_name());
        tvContactAddress.setText(ottUserInfoRequest.getAddress1());
        tvEmail.setText(ottUserInfoRequest.getEmail_address());

        TextView tvPaymentHint = (TextView)findViewById(R.id.tv_payment_hint);
        int saveCardType = ottUserInfoRequest.getSave_credit_card_option();

        if(saveCardType == Resource.SAVE_CARD_SAVE_FOREVER)
        {
            tvPaymentHint.setText(getString(R.string.ott_payment_hint_forever, ottUserInfoRequest.getCash_amount()));
        }
        else if(saveCardType == Resource.SAVE_CARD_SAVE_30)
        {
            tvPaymentHint.setText(R.string.ott_payment_hint_30_days);
        }
        else
        {
            tvPaymentHint.setText(R.string.ott_payment_hint_no_save);
        }
    }

    private void initExpressChargeWidget()
    {
        OTTUserInfoRequest ottUserInfoRequest = TollRoadsApp.getInstance().gOTTUserInfoRequest;

        LinearLayout llExpressCharge = (LinearLayout)findViewById(R.id.ll_express_charge);

        if(ottUserInfoRequest != null && ottUserInfoRequest.getSave_credit_card_option() == Resource.SAVE_CARD_SAVE_FOREVER)
        {
            llExpressCharge.setVisibility(View.VISIBLE);

            TextView tvPhoneNumber = (TextView)findViewById(R.id.tv_phone_number);
            TextView tvUserName = (TextView)findViewById(R.id.tv_user_name);

            tvPhoneNumber.setText(ottUserInfoRequest.getPrimary_phone());
            tvUserName.setText(ottUserInfoRequest.getAccount_username());
        }
        else
        {
            llExpressCharge.setVisibility(View.GONE);
        }
    }

    private void initTripWidget()
    {
        OTTUserInfoRequest ottUserInfoRequest = TollRoadsApp.getInstance().gOTTUserInfoRequest;

        if(ottUserInfoRequest.getCalculate_toll_mode() == Resource.CALCULATE_TOLL_MYSELF) {
            if (TollRoadsApp.getInstance().gVehicleFound == Constants.VEHICLE_FOUND_TYPE_RENTAL) {
                tvTotalCharge.setVisibility(View.GONE);
                llRentalTrip.setVisibility(View.VISIBLE);
                llTrip.setVisibility(View.GONE);

                tvRentalStartDate.setText(ottUserInfoRequest.getRental_start_date());
                tvRentalEndDate.setText(ottUserInfoRequest.getRental_end_date());

                tvPay.setText(getString(R.string.done));
            } else {
                llTrip.setVisibility(View.VISIBLE);
                llRentalTrip.setVisibility(View.GONE);
                tvTotalCharge.setVisibility(View.VISIBLE);
                loadTripDetailMenu();
                tvTotalCharge.setText(getString(R.string.total_charge, TollRoadsApp.getInstance().ottTotalAmount));
                tvPay.setText(getString(R.string.pay));
            }
        }
        else
        {
            if(ottUserInfoRequest.getTotal_amount() > 0)
            {
                tvTotalCharge.setVisibility(View.VISIBLE);

                if(ottUserInfoRequest.getSave_credit_card_option() == Resource.SAVE_CARD_SAVE_FOREVER)
                {
                    tvTotalCharge.setText(getString(R.string.total_charge, ottUserInfoRequest.getCash_amount()));
                }
                else
                {
                    tvTotalCharge.setText(getString(R.string.total_charge, ottUserInfoRequest.getTotal_amount()));
                }
            }
            else
            {
                tvTotalCharge.setVisibility(View.GONE);
            }
            llRentalTrip.setVisibility(View.VISIBLE);
            llTrip.setVisibility(View.GONE);

            tvRentalStartDate.setText(ottUserInfoRequest.getCalc_start_date());
            tvRentalEndDate.setText(ottUserInfoRequest.getCalc_end_date());
        }
    }

    private void initWidgetValue()
    {
        initVehicleWidget();
        initPaymentWidget();
        initTripWidget();
        initExpressChargeWidget();

        svSummary.post(new Runnable() {
            @Override
            public void run() {
                svSummary.scrollTo(0,0);
            }
        });
    }

    private double calTotalCharge()
    {
        double ret = 0.0;
        List<OttTrip> ottTrips = TollRoadsApp.getInstance().ottTrips;
        for(int i=0; i< ottTrips.size();i++)
        {
            ret = ret + ottTrips.get(i).getTrip_amount();
        }
        return ret;
    }

//    class TripDetailListAdapter extends SimpleAdapter {
//        Context mContext;
//        private LayoutInflater l_Inflater;
//
//
//        public TripDetailListAdapter(Context context,
//                               List<? extends Map<String, ?>> data, int resource,
//                               String[] from, int[] to) {
//            super(context, data, resource, from, to);
//
//            mContext = context;
//            l_Inflater = LayoutInflater.from(context);
//        }
//
//        @Override
//        public int getCount() {
//            return TollRoadsApp.getInstance().ottTrips.size();
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//
//            View mView = super.getView(position, convertView, parent);
////            if (convertView == null) {
////                convertView = l_Inflater.inflate(
////                        R.layout.item_ott_trip_detail, null);
////
////            }
//
//            TextView tvStart = (TextView)mView.findViewById(R.id.tv_start);
//            TextView tvEnd = (TextView)mView.findViewById(R.id.tv_end);
//
//            RelativeLayout.LayoutParams startLayoutParams = (RelativeLayout.LayoutParams)tvStart.getLayoutParams();
//            RelativeLayout.LayoutParams endLayoutParams = (RelativeLayout.LayoutParams)tvEnd.getLayoutParams();
//            int width = gDm.widthPixels- Convert.dpToPx(200);
//            startLayoutParams.width = width;
//            endLayoutParams.width = width;
//            tvStart.measure(
//                    View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
//                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//            startLayoutParams.height = tvStart.getMeasuredHeight();
//            tvStart.setLayoutParams(startLayoutParams);
//
//            tvEnd.measure(
//                    View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
//                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//            endLayoutParams.height = tvEnd.getMeasuredHeight();
//            tvEnd.setLayoutParams(endLayoutParams);
//
//            return mView;//mView;//
//        }
//    }

    private SimpleAdapter getTripDetailMenuAdapter() {
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
        List<OttTrip> ottTrips = TollRoadsApp.getInstance().ottTrips;
        for (int i = 0; i < ottTrips.size(); i++) {
            OttTrip ottTrip = ottTrips.get(i);

            HashMap<String, Object> map = new HashMap<String, Object>();

            map.put("tv_trip_index", getString(R.string.trip_index,i+1));
            map.put("tv_date", ottTrip.getTrip_date());
            map.put("tv_road", ottTrip.getRoad());

            map.put("tv_start", ottTrip.getFrom_loc_name());
            map.put("tv_end", ottTrip.getTo_loc_name());
            map.put("tv_trip_total", getString(R.string.trip_amount, ottTrip.getTrip_amount()));
            data.add(map);
        }

        SimpleAdapter tripDetailListAdapter = new SimpleAdapter(
                this, data,
                R.layout.item_ott_trip_detail,
                new String[] { "tv_trip_index", "tv_date", "tv_road",
                        "tv_start", "tv_end", "tv_trip_total"},
                new int[] { R.id.tv_trip_index, R.id.tv_date, R.id.tv_road,
                        R.id.tv_start, R.id.tv_end, R.id.tv_trip_total});

        return tripDetailListAdapter;
    }

    private void setListViewTotalHeight(ListView listView,int width)
    {
        ListAdapter mAdapter = listView.getAdapter();
        int totalHeight = 0;

        for (int i = 0; i < mAdapter.getCount(); i++)
        {
            View mView = mAdapter.getView(i, null, listView);
            mView.measure(
                    View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            totalHeight += mView.getMeasuredHeight();
//	        Log.w("HEIGHT " + i, String.valueOf(totalHeight));
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (mAdapter.getCount() - 1));
        Log.e("base","params.height:"+params.height);
        listView.setLayoutParams(params);
        //listView.requestLayout();
    }

    private void loadTripDetailMenu()
    {
        tripDetailListAdapter = getTripDetailMenuAdapter();
        lvTripDetail.setAdapter(tripDetailListAdapter);
        setListViewTotalHeight(lvTripDetail,gDm.widthPixels- Convert.dpToPx(40));
    }

    @Override
    protected void onResume() {
        super.onResume();
        initWidgetValue();
    }

    @Override
    public void onPause() {

        super.onPause();
    }

}
