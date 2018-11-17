package com.seta.tollroaddroid.app;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class SettingsActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    Switch swFingerprintLogin;

        private View mProgressView;
        private UserLoginTask mAuthTask;

        private static final String KEY_ALIAS = "tcapwd";
        private static final String KEYSTORE = "AndroidKeyStore";
        private static final String PREFERENCES_KEY_EMAIL = "email";
        private static final String PREFERENCES_KEY_PASS = "pass";
        private static final String PREFERENCES_KEY_IV = "iv";

        private KeyStore keyStore;
        private KeyGenerator generator;
        private Cipher cipher;

        private SharedPreferences sharedPreferences;


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptEncryptPassword() {
        if (!TollRoadsApp.getInstance().testFingerPrintSettings())
            return;

        if (mAuthTask != null) {
            return;
        }

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        showProgress(true);
        mAuthTask = new UserLoginTask(TollRoadsApp.getInstance().loginEmail, TollRoadsApp.getInstance().loginPassword);
        mAuthTask.execute((Void) null);

    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
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
            editor.putString(PREFERENCES_KEY_PASS, encryptedText);
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
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
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

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PREFERENCES_KEY_EMAIL, mEmail);
            editor.commit();

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
                String textToEncrypt = TollRoadsApp.getInstance().loginPassword;
                encryptString(textToEncrypt);
                Toast.makeText(SettingsActivity.this, getString(R.string.fingerprint_login_enabled), Toast.LENGTH_LONG).show();

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
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit Menu_Settings page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        FlurryAgent.logEvent("Enter Menu_Settings page.");

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        swFingerprintLogin = (Switch)(findViewById(R.id.sw_fingerprint_login));
        swFingerprintLogin.setChecked(TollRoadsApp.getInstance().getFingerprintLogin());

        mProgressView = findViewById(R.id.progressbar);

        setupListener();
        if(!TollRoadsApp.getInstance().testFingerPrintSettings())
        {
            swFingerprintLogin.setEnabled(false);
        }
        else
        {
            swFingerprintLogin.setEnabled(true);
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void setupListener()
    {
        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        swFingerprintLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    attemptEncryptPassword();
                }
                else
                {

                }
                TollRoadsApp.getInstance().setFingerprintLogin(isChecked);
            }
        });
    }

}
