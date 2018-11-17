package com.seta.tollroaddroid.app;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.fragments.FingerprintAuthenticationDialogFragment;
import com.seta.tollroaddroid.app.json.AccountInfo;
import com.seta.tollroaddroid.app.json.LoginResponse;
import com.seta.tollroaddroid.app.json.SecQuestion;
import com.seta.tollroaddroid.app.thirdparty.FingerprintHelper;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class LoginActivity extends BaseActivity implements
        FingerprintHelper.FingerprintHelperListener, FingerprintAuthenticationDialogFragment.MyExitListener{
    private final String LOG_TAG = this.getClass().getSimpleName();
    private TextView tvLogin, tvCancel, tvForgotPassword;
    private EditText etUserName, etPassword;
    private Response.ErrorListener errorListener;
    private String username;
    private Switch swRememberLogIn;
    private boolean needSec = false;

    private UserLoginTask mAuthTask;

    private static final String KEY_ALIAS = "tcapwd";
    private static final String KEYSTORE = "AndroidKeyStore";
    private static final String PREFERENCES_KEY_EMAIL = "email";
    private static final String PREFERENCES_KEY_PASS = "pass";
    private static final String PREFERENCES_KEY_IV = "iv";

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
            Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void authenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
        print("Authentication succeeded!");
        cipher = result.getCryptoObject().getCipher();

        String encryptedText = sharedPreferences.getString(PREFERENCES_KEY_PASS, "");
        decryptString(encryptedText);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void decryptString(String cipherText) {
        print("Decrypting...");
        try {
            byte[] bytes = Base64.decode(cipherText, Base64.NO_WRAP);
            String finalText = new String(cipher.doFinal(bytes));
            Log.e("decrypt","cipherText:"+cipherText+",finalText:"+finalText);

            showProgressDialog();
            username = sharedPreferences.getString(PREFERENCES_KEY_EMAIL, "");
            String password = finalText.trim();
            String loginParams = "&username="+ Uri.encode(username) +"&password="+Uri.encode(password);
            sendLoginRequest(Resource.URL_LOGIN, loginParams);

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

        if (!usersRegistered())
            return;

        mAuthTask = new UserLoginTask();
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
            fingerprintHelper = new FingerprintHelper(LoginActivity.this);
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

                fingerprintHelper.startAuth(LoginActivity.this.fingerprintManager, cryptoObject);

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
    private boolean usersRegistered() {
        if (sharedPreferences.getString(PREFERENCES_KEY_EMAIL, null) == null) {
            print("No user is registered");
            return false;
        }

        return true;
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
    protected void onResume() {
        super.onResume();
        isPaused = false;
        if(TollRoadsApp.getInstance().getFingerprintLogin() &&
                fingerprintAuthenticationDialogFragment != null &&fingerprintAuthenticationDialogFragment.isAdded())
        {
            attemptFingerprintLogin();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isPaused = true;
        if(fingerprintAuthenticationDialogFragment != null && fingerprintAuthenticationDialogFragment.isAdded()) {
            if (fingerprintHelper != null)
                fingerprintHelper.cancel();
            if (mAuthTask != null)
                mAuthTask.cancel(true);
        }
    }

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit Log_In page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FlurryAgent.logEvent("Enter Log_In page.");

        fingerprintManager = FingerprintManagerCompat.from(this);
        if(TollRoadsApp.getInstance().getFingerprintLogin() && TollRoadsApp.getInstance().testFingerPrintSettings())
        {
            fingerprintAuthenticationDialogFragment = new FingerprintAuthenticationDialogFragment();

            Bundle bundle = new Bundle();
            bundle.putString(Resource.KEY_TITLE, getString(R.string.fingerprint_for_tca));
            bundle.putString(Resource.KEY_DESCRIPTION, getString(R.string.fingerprint_description));

            fingerprintAuthenticationDialogFragment.setArguments(bundle);
            fingerprintAuthenticationDialogFragment.show(getFragmentManager(), "fingerprintAuthenticationDialogFragment");
            fingerprintAuthenticationDialogFragment.setCancelable(false);
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        tvLogin = (TextView)findViewById(R.id.tv_log_in);
        tvCancel = (TextView)findViewById(R.id.tv_cancel);
        tvForgotPassword = (TextView)findViewById(R.id.tv_forgot_password);
        etUserName = (EditText)findViewById(R.id.et_user_name);
        etPassword = (EditText)findViewById(R.id.et_password);

        swRememberLogIn = (Switch)findViewById(R.id.sw_remember_log_in);

        if(TollRoadsApp.getInstance().getRememberLogIn())
        {
            etUserName.setText(TollRoadsApp.getInstance().getUserName());
            swRememberLogIn.setChecked(true);
        }
        else
        {
            swRememberLogIn.setChecked(false);
        }
//        etUserName.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if(!s.toString().contains("."))
//                {
//                    int cursorPosition = etUserName.getSelectionEnd();
//                    etUserName.setText(s.toString()+".0");
//                    etUserName.setSelection(cursorPosition);
//                }
//            }
//        });
//        if(!etUserName.getText().toString().contains("."))
//        {
//            int cursorPosition = etUserName.getSelectionEnd();
//            etUserName.setText(etUserName.getText().toString()+".0");
//            etUserName.setSelection(cursorPosition);
//        }
        setupListener();

    }

    private void setupListener()
    {
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(getApplicationContext(), LandingPageActivity.class,
                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation()) {
                    doLoginAction();
                }
            }
        });
        swRememberLogIn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TollRoadsApp.getInstance().setRememberLogIn(isChecked);
                if(!isChecked)
                {
                    TollRoadsApp.getInstance().setUserName("");
                }
            }
        });

        errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                closeProgressDialog();

                if(error != null) {
                    Log.d(LOG_TAG, "Error: " + error.getMessage());
//                    showDialog(getString(R.string.dialog_title_error),
//                            "Message: " + error.getMessage()+"\r\nCaused by:"+error.getCause(), getString(R.string.ok),
//                            new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            }, false);
                    //ServerDelegate.appendLog("Message: " + error.getMessage()+"\r\nCaused by:"+error.getCause());

                    //ServerDelegate.sendLog(LoginActivity.this);

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
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(v.getContext(), ForgotPasswordActivity.class);
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

    private boolean checkUserNameValidation() {
        if (etUserName.getText().length() == 0) {
            showToastMessage(getString(R.string.user_name_empty_warning));
            etUserName.requestFocus();
            return false;
        }

        return true;
    }

    private boolean checkPasswordValidation() {
        if(etPassword.getText().length() == 0)
        {
            showToastMessage(getString(R.string.password_empty_warning));
            etPassword.requestFocus();
            return false;
        }
//        else if(etPassword.getText().length() < Constants.MIN_PASSWORD_LENGTH)
//        {
//            showToastMessage(getString(R.string.password_too_short_warning));
//            etPassword.requestFocus();
//            return false;
//        }
        return true;
    }

    private boolean checkValidation()
    {
        if(!checkUserNameValidation())
        {
            return false;
        }
        else if(!checkPasswordValidation())
        {
            return false;
        }

        return true;
    }

    private void hideAllSoftKeyboard()
    {
        hideKeyboard(etPassword);
        hideKeyboard(etUserName);
    }

    private void doLoginAction()
    {
        hideAllSoftKeyboard();
        showProgressDialog();

        username = etUserName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String loginParams = "&username="+ Uri.encode(username) +"&password="+Uri.encode(password);
        sendLoginRequest(Resource.URL_LOGIN, loginParams);
    }

    private void sendLoginRequest(String loginRequestUrl,String loginParams)
    {
        needSec = false;
        Response.Listener listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, "response:"+response);

                        Gson gson = new GsonBuilder().serializeNulls().create();
                        LoginResponse loginResponse = gson.fromJson(response, LoginResponse.class);

                        if(checkResponse(response)) {
                            TollRoadsApp.getInstance().setToken(loginResponse.getTokenID());

                            String oldUserName = TollRoadsApp.getInstance().getUserName();


                            if(!oldUserName.equals(username))
                            {
                                //clear touch id
                                TollRoadsApp.getInstance().setFingerprintLogin(false);
                            }

                            //if(TollRoadsApp.getInstance().getRememberLogIn())
                            {
                                TollRoadsApp.getInstance().setUserName(username);
                            }

                            if(loginResponse.getNeed_sec() == 1)
                            {
                                needSec = true;
                            }

                            //For fingerprint login
                            TollRoadsApp.getInstance().loginPassword = etPassword.getText().toString().trim();
                            TollRoadsApp.getInstance().loginEmail = etUserName.getText().toString().trim();
                            sendAccountRequest();
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

        ServerDelegate.loginRequest(loginRequestUrl, loginParams, listener, errorListener);
    }

    private void getSecQuestions()
    {
        showProgressDialog();
        Response.Listener listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                closeProgressDialog();
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, response);

                        response = response.replace(",}","}");
                        if(checkResponse(response))
                        {
                            JSONObject jsonObject = new JSONObject(response);
                            if(jsonObject.has(Resource.KEY_SECURITY_QUESTION_LIST))
                            {
                                Gson gson = new GsonBuilder().serializeNulls().create();

                                Type listType = new TypeToken<ArrayList<SecQuestion>>() {}.getType();
                                String info = jsonObject.getString(Resource.KEY_SECURITY_QUESTION_LIST);
                                TollRoadsApp.getInstance().secQuestionList = gson.fromJson(info, listType);
                                gotoActivity(getApplicationContext(), SecurityActivity.class,
                                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            }
                            else
                            {
                                closeProgressDialog();
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

        ServerDelegate.getSecQuestions(Resource.URL_ACCOUNT, listener, errorListener);
    }

    private void sendAccountRequest()
    {
        showProgressDialog();

        Response.Listener listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, response.toString());
                        closeProgressDialog();
                        if(checkResponse(response.toString())) {
                            if(response.has(Resource.KEY_INFO))
                            {
                                String info = response.optString(Resource.KEY_INFO);
                                Gson gson = new GsonBuilder().serializeNulls().create();

                                TollRoadsApp.getInstance().accountInfo = gson.fromJson(info, AccountInfo.class);
                                TollRoadsApp.getInstance().setMyAccount(info);
                            }
                            if(needSec) {
                                getSecQuestions();
                            }
                            else {
                                finish();
                                gotoActivity(getApplicationContext(), MyAccountActivity.class,
                                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
}
