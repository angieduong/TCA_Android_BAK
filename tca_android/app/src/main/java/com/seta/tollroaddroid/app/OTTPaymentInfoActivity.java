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
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.custom.MultiButtonsPopupDialog;
import com.seta.tollroaddroid.app.fragments.FingerprintAuthenticationDialogFragment;
import com.seta.tollroaddroid.app.json.AutoPopulatePaymentInfo;
import com.seta.tollroaddroid.app.json.OTTUserInfoRequest;
import com.seta.tollroaddroid.app.thirdparty.FingerprintHelper;
import com.seta.tollroaddroid.app.utilities.Constants;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Iterator;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class OTTPaymentInfoActivity extends BaseActivity implements
        FingerprintHelper.FingerprintHelperListener, FingerprintAuthenticationDialogFragment.MyExitListener{
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private TextView tvGoNext;
    private EditText etCardNo, etNameOnCard, etExpirationDate, etZipCode,etCVV;

    private TextView tvTotalCharge;
    private MultiButtonsPopupDialog gMultiButtonsPopupDialog;
    private Response.ErrorListener errorListener;

    private OTTPaymentInfoActivity.UserLoginTask mAuthTask;

    private static final String KEY_ALIAS = "tca_payment";
    private static final String KEYSTORE = "AndroidKeyStore";
    private static final String PREFERENCES_KEY_PAYMENT = "payment";
    private static final String PREFERENCES_KEY_IV = "payment_iv";

    private KeyStore keyStore;
    private KeyGenerator generator;
    private Cipher cipher;
    private FingerprintManagerCompat fingerprintManager;

    private FingerprintManagerCompat.CryptoObject cryptoObject;
    private SharedPreferences sharedPreferences;
    private FingerprintHelper fingerprintHelper;

    FingerprintAuthenticationDialogFragment fingerprintAuthenticationDialogFragment;
    private boolean isPaused = false;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void authenticationFailed(String error) {
        if(fingerprintAuthenticationDialogFragment != null &&
                fingerprintAuthenticationDialogFragment.isAdded() && !isPaused) {
            Toast.makeText(OTTPaymentInfoActivity.this, error, Toast.LENGTH_LONG).show();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void authenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
        print("Authentication succeeded!");
        cipher = result.getCryptoObject().getCipher();

        String encryptedText = sharedPreferences.getString(PREFERENCES_KEY_PAYMENT, "");
        decryptString(encryptedText);
    }

    private void prePopulateWidget(AutoPopulatePaymentInfo autoPopulatePaymentInfo)
    {
        if(autoPopulatePaymentInfo != null) {
            etCardNo.setText(autoPopulatePaymentInfo.getCard_number());
            etNameOnCard.setText(autoPopulatePaymentInfo.getCard_holder_name());
            etExpirationDate.setText(autoPopulatePaymentInfo.getExpired_date().replace("/",""));
            etZipCode.setText(autoPopulatePaymentInfo.getZip_code());
            etCVV.setText(autoPopulatePaymentInfo.getCvv2());
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void decryptString(String cipherText) {
        print("Decrypting...");
        try {
            byte[] bytes = Base64.decode(cipherText, Base64.NO_WRAP);
            String finalText = new String(cipher.doFinal(bytes));
            Log.e("decrypt","cipherText:"+cipherText+",finalText:"+finalText);

            String paymentInfo = finalText.trim();
            AutoPopulatePaymentInfo autoPopulatePaymentInfo;
            Gson gson= new GsonBuilder().serializeNulls().create();
            autoPopulatePaymentInfo = gson.fromJson(paymentInfo,
                    AutoPopulatePaymentInfo.class);
            prePopulateWidget(autoPopulatePaymentInfo);

            if(fingerprintAuthenticationDialogFragment != null) {
                fingerprintAuthenticationDialogFragment.dismiss();
            }
        } catch (Exception e) {
            print(e.getMessage());
            if(fingerprintAuthenticationDialogFragment != null) {
                fingerprintAuthenticationDialogFragment.dismiss();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void attemptFingerprintLogin() {
        if (!TollRoadsApp.getInstance().testFingerPrintSettings())
            return;

        mAuthTask = new OTTPaymentInfoActivity.UserLoginTask();
        mAuthTask.execute((Void) null);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void exit() {
        if (fingerprintHelper != null)
            fingerprintHelper.cancel();
        if (mAuthTask != null)
            mAuthTask.cancel(true);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    @TargetApi(Build.VERSION_CODES.M)
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        @TargetApi(Build.VERSION_CODES.M)
        UserLoginTask() {
            fingerprintHelper = new FingerprintHelper(OTTPaymentInfoActivity.this);
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        protected Boolean doInBackground(Void... params) {
            if (!getKeyStore())
                return false;

            if (!createNewKey(false))
                return false;

            // Inside doInBackground
            if (!getCipher())
                return false;

            // Inside doInBackground

            if (!initCipher(Cipher.DECRYPT_MODE))
                return false;


            if (!initCryptObject())
                return false;

            return true;
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        protected void onPostExecute(final Boolean success) {
            onCancelled();

            if (!success) {
                print("Fingerprint authenticate failed!");
            } else {

                fingerprintHelper.startAuth(OTTPaymentInfoActivity.this.fingerprintManager, cryptoObject);

                print("Authenticate using fingerprint!");
            }
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    public void print(String text) {
        if(text!= null && !text.isEmpty()) {
            Log.d(LOG_TAG, text);
        }
    }

    public void print(int id) {
        print(getString(id));
    }

    @TargetApi(Build.VERSION_CODES.M)
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
                        .setUserAuthenticationRequired(true)
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

    @TargetApi(Build.VERSION_CODES.M)
    private boolean initCryptObject() {
        print("Initializing crypt object...");
        try {
            cryptoObject = new FingerprintManagerCompat.CryptoObject(cipher);
            return true;
        } catch (Exception ex) {
            print(ex.getMessage());
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putDouble("OTTTotalAmount", TollRoadsApp.getInstance().ottTotalAmount);
        outState.putSerializable("OTTUserInfoRequest", TollRoadsApp.getInstance().gOTTUserInfoRequest);
    }

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit OTT_3_Payment page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ott_payment_info);
        if(savedInstanceState != null)
        {
            TollRoadsApp.getInstance().ottTotalAmount =
                    savedInstanceState.getDouble("OTTTotalAmount",0.0);
            TollRoadsApp.getInstance().gOTTUserInfoRequest = 
                    (OTTUserInfoRequest) savedInstanceState.getSerializable("OTTUserInfoRequest");
        }
        FlurryAgent.logEvent("Enter OTT_3_Payment page.");

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvGoNext = (TextView) findViewById(R.id.tv_go_next);

        etCardNo = (EditText)findViewById(R.id.et_card_no);
        etNameOnCard = (EditText)findViewById(R.id.et_name_on_card);
        etExpirationDate = (EditText)findViewById(R.id.et_expiration_date);
        etZipCode = (EditText)findViewById(R.id.et_billing_zip_code);
        etCVV = (EditText)findViewById(R.id.et_cvv);

        tvTotalCharge = (TextView)findViewById(R.id.tv_total_charge);

        setupListener();

        //debugModeInit(savedInstanceState);

        fingerprintManager = FingerprintManagerCompat.from(this);
        if(Resource.enableAutoPopulatePayment) {
            if (TollRoadsApp.getInstance().isAutoPopulatePayment() && TollRoadsApp.getInstance().testFingerPrintSettings()) {
                fingerprintAuthenticationDialogFragment = new FingerprintAuthenticationDialogFragment();

                Bundle bundle = new Bundle();
                bundle.putString(Resource.KEY_TITLE, getString(R.string.payment));
                bundle.putString(Resource.KEY_DESCRIPTION, getString(R.string.fingerprint_payment));

                fingerprintAuthenticationDialogFragment.setArguments(bundle);
                fingerprintAuthenticationDialogFragment.show(getFragmentManager(), "fingerprintAuthenticationDialogFragment");
                fingerprintAuthenticationDialogFragment.setCancelable(false);
            }
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    }

    private void debugModeInit(Bundle savedInstanceState)
    {
        if(savedInstanceState == null && BuildConfig.DEBUG)
        {
            etCardNo.setText("5111111111111191");
            etNameOnCard.setText("John");
            etExpirationDate.setText("0619");
            etZipCode.setText("96216");
            etCVV.setText("616");
        }
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
                gotoActivity(v.getContext(), OTTContactInfoActivity.class,
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
        });

        tvGoNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation()) {
                    setRequest();

                    //showUpConfirmDialog();
                    checkPayment();
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            gotoActivity(getApplicationContext(), OTTContactInfoActivity.class,
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            return  true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showUpConfirmDialog()
    {
        gMultiButtonsPopupDialog = new MultiButtonsPopupDialog(this,R.style.CustomProgressDialog);
        gMultiButtonsPopupDialog.createDialog(this);
        gMultiButtonsPopupDialog.CutomizeTitle(View.GONE, "");

        gMultiButtonsPopupDialog.CutomizeContent(View.VISIBLE, getString(R.string.go_sign_up_hint));
        gMultiButtonsPopupDialog.CutomizeExtraButton(View.GONE, 0, "", 0, null);
        gMultiButtonsPopupDialog.CutomizePositiveButton(View.VISIBLE, 0, getString(R.string.yes), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putBoolean(Constants.FROM_OTT, true);
                gotoActivity(getApplicationContext(), SignUpAccountInfoActivity.class,
                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK, bundle);
            }
        });
        gMultiButtonsPopupDialog.CutomizeNeutralButton(View.GONE, 0, "", null);

        String continueWord;
        if(TollRoadsApp.getInstance().gVehicleFound == Constants.VEHICLE_FOUND_TYPE_RENTAL) {
            continueWord = getString(R.string.capital_continue);
        }
        else
        {
            continueWord = getString(R.string.continue_ott);
        }

        gMultiButtonsPopupDialog.CutomizeNegativeButton(View.VISIBLE, 0, continueWord, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPayment();
            }
        });

        gMultiButtonsPopupDialog.show();
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

    private void checkPayment()
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
                            gotoActivity(OTTPaymentInfoActivity.this, OTTSaveCreditCardActivity.class,//OTTSummaryActivity.class,
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

        ServerDelegate.checkPayment(Resource.URL_PAY_GO, params, listener, errorListener);
////test
//        gotoActivity(OTTPaymentInfoActivity.this, OTTSaveCreditCardActivity.class,//OTTSummaryActivity.class,
//                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    }

    private String formatDate(String dateString)
    {
        String ret = "";
        if(dateString != null && !dateString.isEmpty())
        {
            if(dateString.length() <2) {
                ret = dateString;
            }
            else
            {
                ret = dateString.substring(0,2)+"/"+dateString.substring(2);
            }
        }
        return ret;
    }

    private void setRequest()
    {
        OTTUserInfoRequest ottUserInfoRequest = TollRoadsApp.getInstance().gOTTUserInfoRequest;

        ottUserInfoRequest.setPayment_method(Constants.CREDIT_CARD_TYPE);
        ottUserInfoRequest.setCard_number(etCardNo.getText().toString());
        ottUserInfoRequest.setCard_holder_name(etNameOnCard.getText().toString());
        ottUserInfoRequest.setExpired_date(formatDate(etExpirationDate.getText().toString()));
        ottUserInfoRequest.setZip_code(etZipCode.getText().toString());
        ottUserInfoRequest.setCvv2(etCVV.getText().toString());
    }
    
    private boolean checkValidation()
    {
        boolean ret = true;

        if (etCardNo.getText().length() == 0) {
            ret = false;
            showToastMessage(getString(R.string.card_no_empty_warning));
        } else if (etCVV.getText().length() == 0) {
            ret = false;
            showToastMessage(getString(R.string.cvv_empty_warning));
        } else if (etNameOnCard.getText().length() == 0) {
            ret = false;
            showToastMessage(getString(R.string.name_on_card_empty_warning));
        } else if (etExpirationDate.getText().length() == 0) {
            ret = false;
            showToastMessage(getString(R.string.expiration_date_empty_warning));
        }
//        else if (etZipCode.getText().length() == 0) {
//            ret = false;
//            showToastMessage(getString(R.string.billing_zip_code_empty_warning));
//        }

        return ret;
    }
    
    private void initWidgetValue()
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
        initWidgetValue();
        isPaused = false;
        if(Resource.enableAutoPopulatePayment) {
            if (TollRoadsApp.getInstance().isAutoPopulatePayment() &&
                    fingerprintAuthenticationDialogFragment != null && fingerprintAuthenticationDialogFragment.isAdded()) {
                attemptFingerprintLogin();
            }
        }
    }

    @Override
    public void onPause() {

        super.onPause();
        isPaused = true;
        if(Resource.enableAutoPopulatePayment) {
            if (fingerprintAuthenticationDialogFragment != null && fingerprintAuthenticationDialogFragment.isAdded()) {
                if (fingerprintHelper != null)
                    fingerprintHelper.cancel();
                if (mAuthTask != null)
                    mAuthTask.cancel(true);
            }
        }
    }

}
