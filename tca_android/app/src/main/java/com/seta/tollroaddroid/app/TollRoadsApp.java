package com.seta.tollroaddroid.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.custom.MyLifecycleHandler;
import com.seta.tollroaddroid.app.json.AccountInfo;
import com.seta.tollroaddroid.app.json.OTTUserInfoRequest;
import com.seta.tollroaddroid.app.json.OttTrip;
import com.seta.tollroaddroid.app.json.PaySelectedViolationsRequest;
import com.seta.tollroaddroid.app.json.PaymentMethod;
import com.seta.tollroaddroid.app.json.SecQuestion;
import com.seta.tollroaddroid.app.json.SelectedUnpaidViolationsResponse;
import com.seta.tollroaddroid.app.json.SignUpRequest;
import com.seta.tollroaddroid.app.json.Vehicle;
import com.seta.tollroaddroid.app.json.ViolationLoginResponse;
import com.seta.tollroaddroid.app.thirdparty.LruBitmapCache;
import com.seta.tollroaddroid.app.thirdparty.TLSSocketFactory;
import com.seta.tollroaddroid.app.utilities.Constants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import com.crashlytics.android.Crashlytics;
import com.seta.tollroaddroid.app.utilities.DeviceManager;

import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import io.fabric.sdk.android.Fabric;

/**
 * Created by thomashuang on 16-02-29.
 */
public class TollRoadsApp extends Application {
    public static final String TAG = TollRoadsApp.class.getSimpleName();
    private static TollRoadsApp mInstance;
    private static String standardUserDefaults = "TCAUserInfo";

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private LruBitmapCache gLruBitmapCache;

    public AccountInfo accountInfo;
    public Vehicle gVehicle;
    public PaymentMethod gPaymentMethod;

    public boolean gbSplashed = false;
    public static synchronized TollRoadsApp getInstance() {
        return mInstance;
    }
    public int selectedAccountType = Constants.ACCOUNT_TYPE_FASTRAK_INDIVIDUAL;
    public SignUpRequest gSignUpRequest = null;
    public OTTUserInfoRequest gOTTUserInfoRequest = null;
    public PaySelectedViolationsRequest paySelectedViolationsRequest = null;

    public List<OttTrip> ottTrips = new ArrayList<OttTrip>();
    public double ottTotalAmount = 0.0;

//    vehicle_found = 0 means the license plate does not belong to a patron or fleet account.
//    vehicle_found = 1 means that the license plate belongs to a patron account.
//    vehicle_found = 2 means that the license plate belongs to a fleet (rental) account that allows One-Time-Toll processing (going down the fleet rental flow).
    public int gVehicleFound = Constants.VEHICLE_FOUND_TYPE_NOT_EXIST;
    public List<SecQuestion> secQuestionList;
    public boolean gFromOTT = false;
    public boolean gShowOTTCaching = false;
    public OTTUserInfoRequest gCachingOTTRequest = null;
    private MyLifecycleHandler myLifecycleHandler = null;

    public String loginEmail;
    public String loginPassword;

    public ViolationLoginResponse violationLoginResponse;
    public SelectedUnpaidViolationsResponse selectedUnpaidViolationsResponse;
    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        mInstance = this;
        myLifecycleHandler = new MyLifecycleHandler();
        registerActivityLifecycleCallbacks(myLifecycleHandler);

        new FlurryAgent.Builder()
                .withLogEnabled(false)
                .build(this, getString(R.string.flurry_api_key));

        DeviceManager.getInstance().setAppVersionName(BuildConfig.VERSION_NAME);
        DeviceManager.getInstance().setAppVersionCode(String.valueOf(BuildConfig.VERSION_CODE));
        DeviceManager.getInstance().setDeviceOS(System.getProperty("os.version"));
        DeviceManager.getInstance().setDeviceApiLevel(Build.VERSION.SDK_INT);
        DeviceManager.getInstance().setDeviceModel(Build.MODEL);

    }

    public boolean isApplicationVisible() {
        if(myLifecycleHandler == null)
        {
            myLifecycleHandler = new MyLifecycleHandler();
            registerActivityLifecycleCallbacks(myLifecycleHandler);
        }

        return myLifecycleHandler.isApplicationVisible();
    }

    public boolean isApplicationInForeground() {
        if(myLifecycleHandler == null)
        {
            myLifecycleHandler = new MyLifecycleHandler();
            registerActivityLifecycleCallbacks(myLifecycleHandler);
        }

        return myLifecycleHandler.isApplicationInForeground();
    }

    private SSLSocketFactory getSSLSocketFactory()
            throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream caInput = getResources().openRawResource(R.raw.thetollroads); // this cert file stored in \app\src\main\res\raw folder path

        Certificate ca = cf.generateCertificate(caInput);
        caInput.close();

        KeyStore keyStore = KeyStore.getInstance("BKS");
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");

        sslContext.init(null, tmf.getTrustManagers(), null);

        return sslContext.getSocketFactory();
    }

    public void updateAndroidSecurityProvider(Activity callingActivity) {
        try {
            ProviderInstaller.installIfNeeded(this);
        } catch (GooglePlayServicesRepairableException e) {
            // Thrown when Google Play Services is not installed, up-to-date, or enabled
            // Show dialog to allow users to install, update, or otherwise enable Google Play services.
            GooglePlayServicesUtil.getErrorDialog(e.getConnectionStatusCode(), callingActivity, 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e("SecurityException", "Google Play Services not available.");
        }
    }

    public RequestQueue getRequestQueue() {

        HurlStack hurlStack = new HurlStack(){
            @Override
            protected HttpURLConnection createConnection(URL url) throws IOException {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) super.createConnection(url);
                try {
                    SSLSocketFactory tlsSocketFactory = new TLSSocketFactory();

                    httpsURLConnection.setSSLSocketFactory(tlsSocketFactory);
                    //httpsURLConnection.setHostnameVerifier(hostnameVerifier);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return httpsURLConnection;

            }
        };

        if (mRequestQueue == null) {
            if(Resource.APP_DOMAIN.contains("https://")) {
                mRequestQueue = Volley.newRequestQueue(getApplicationContext(), hurlStack);
            }
            else
            {
                mRequestQueue = Volley.newRequestQueue(getApplicationContext());
            }
        }

        return mRequestQueue;
    }

    public LruBitmapCache getLruBitmapCache() {
        if (gLruBitmapCache == null) {
            gLruBitmapCache = new LruBitmapCache();
        }

        return gLruBitmapCache;
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        getLruBitmapCache();

        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this.mRequestQueue,
                    gLruBitmapCache);
        }

        return this.mImageLoader;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public String readTextFromLocalFile(String fileName)
    {
        AssetManager am = getAssets();
        InputStream inputStream = null;
        try {
            inputStream = am.open(fileName);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            int i;
            i = inputStream.read();
            while (i != -1)
            {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
            return byteArrayOutputStream.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    static public void GotoTheUrl(Context mContext, String mWebSite) {
        if (mWebSite == null || mWebSite.isEmpty()) {
            return;
        }

        Intent mIntent = null;
        String mWebSiteStr;
        int mHttpIndex = mWebSite.lastIndexOf("http");

        if (mHttpIndex != (-1)) {
            mWebSiteStr = mWebSite.substring(mHttpIndex);
        } else {
            mWebSiteStr = mWebSite.trim();
        }
        mIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mWebSiteStr));

        PackageManager pm = mContext.getPackageManager();
        ComponentName cn = mIntent.resolveActivity(pm);
        if (cn != null) {
            mContext.startActivity(mIntent);
        }
    }
    static public void makeACall(Context mContext, String callNum)
    {
        if(!callNum.isEmpty())
        {
            String lowerCallNum = callNum.toLowerCase(Locale.getDefault());
            StringBuffer mStringBuffer = new StringBuffer();

            Log.e("makeACall", "callNum: " + callNum);

            for(int i =0; i < lowerCallNum.length(); i++)
            {
                char mChar = lowerCallNum.charAt(i);
                if((mChar == '*') || (mChar == '#') || (mChar == '+')  || (mChar == 'p')|| (mChar == 'w')|| (mChar == 'h')|| ((mChar >= 0x30) &&(mChar <= 0x39)))
                {
                    mStringBuffer.append(mChar);
                }
            }
            Log.e("makeACall", "tel: " + mStringBuffer);
            Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mStringBuffer));//"android.intent.action.CALL"

            mContext.startActivity(phoneIntent);
        }
    }

    static public void sendAnEmail(Context mContext, String emailAddress)
    {
        Uri uri=Uri.parse("mailto:" + emailAddress);

        Intent emailIntent=new Intent(Intent.ACTION_SENDTO,uri);

        mContext.startActivity(emailIntent);
    }

    public String getUniqueID() {
        SharedPreferences preference = getSharedPreferences(standardUserDefaults, Context.MODE_PRIVATE);
        String uniqueID = preference.getString("UniqueID", "");

        if(uniqueID.isEmpty())
        {
            uniqueID = UUID.randomUUID().toString();
            setUniqueID(uniqueID);
        }
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        SharedPreferences preference = getSharedPreferences(standardUserDefaults, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putString("UniqueID", uniqueID);

        editor.commit();
    }

    public String getUserName() {
        SharedPreferences preference = getSharedPreferences(standardUserDefaults, Context.MODE_PRIVATE);
        String userName = preference.getString("UserName", "");

        return userName;
    }

    public void setUserName(String userName) {
        SharedPreferences preference = getSharedPreferences(standardUserDefaults, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putString("UserName", userName);

        editor.commit();
    }
    
    public String getAppVersion()
    {
        String versionName = "0.0";
        try
        {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        String appVersion = Resource.LOCAL_LANGUAGE + "_an_" + versionName ;

        return appVersion;

    }

    public String getMyAccount() {
        SharedPreferences preference = getSharedPreferences(standardUserDefaults, Context.MODE_PRIVATE);
        String myAccount = preference.getString("MyAccount", "");

        return myAccount;
    }

    public void setMyAccount(String myAccount) {
        SharedPreferences preference = getSharedPreferences(standardUserDefaults, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putString("MyAccount", myAccount);

        editor.commit();
    }

    public String getRoutes() {
        SharedPreferences preference = getSharedPreferences(standardUserDefaults, Context.MODE_PRIVATE);
        String routes = preference.getString("Routes", "");

        return routes;
    }

    public void setRoutes(String routes) {
        SharedPreferences preference = getSharedPreferences(standardUserDefaults, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putString("Routes", routes);

        editor.commit();
    }

    public String getToken() {
        SharedPreferences preference = getSharedPreferences(standardUserDefaults, Context.MODE_PRIVATE);
        String token = preference.getString("Token", "");

        return token;
    }

    public void setToken(String token) {
        SharedPreferences preference = getSharedPreferences(standardUserDefaults, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putString("Token", token);

        editor.commit();
    }

    public boolean getRememberLogIn() {
        SharedPreferences preference = getSharedPreferences(standardUserDefaults, Context.MODE_PRIVATE);
        boolean rememberLogIn = preference.getBoolean("RememberLogIn", false);

        return rememberLogIn;
    }

    public void setRememberLogIn(boolean rememberLogIn) {
        SharedPreferences preference = getSharedPreferences(standardUserDefaults, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putBoolean("RememberLogIn", rememberLogIn);

        editor.commit();
    }

    public String getOTTRequest() {
        SharedPreferences preference = getSharedPreferences(standardUserDefaults, Context.MODE_PRIVATE);
        String ottRequest = preference.getString("OTTRequest", "");

        return ottRequest;
    }

    public void setOTTRequest(String ottRequest) {
        SharedPreferences preference = getSharedPreferences(standardUserDefaults, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putString("OTTRequest", ottRequest);

        editor.commit();
    }

    public boolean getFingerprintLogin() {
        SharedPreferences preference = getSharedPreferences(standardUserDefaults, Context.MODE_PRIVATE);
        boolean fingerprintLogin = preference.getBoolean("FingerprintLogin", false);

        return fingerprintLogin;
    }

    public void setFingerprintLogin(boolean fingerprintLogin) {
        SharedPreferences preference = getSharedPreferences(standardUserDefaults, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putBoolean("FingerprintLogin", fingerprintLogin);

        editor.commit();
    }
    
    public static String getCountryString(int countryIndex)
    {
        String countryAbbr ="";
        if(countryIndex == 1)
        {
            countryAbbr ="";
        }
        else if(countryIndex == 2)
        {
            countryAbbr ="MX";
        }
        else if(countryIndex == 3)
        {
            countryAbbr ="XX";
        }
        return countryAbbr;
    }

    public static String getCountryFullString(Context context,String countryAbbr, String stateStr)
    {
        String countryFull = "United States";
        if(countryAbbr.equals(""))
        {
            if(getCanadaStateIndex(context,stateStr) >= 0)
            {
                countryFull = "Canada";
            }
        }
        else if(countryAbbr.equals("MX"))
        {
            countryFull ="Mexico";
        }
        else if(countryAbbr.equals("XX"))
        {
            countryFull ="International";
        }
        return countryFull;
    }

    public static int getCountryIndex(Context context, String countryStr, String stateStr)
    {
        if(countryStr == null || countryStr.isEmpty())
        {
            if(getCanadaStateIndex(context,stateStr) >= 0)
            {
                return 1;
            }
            else {
                return 0;
            }
        }
        else if(countryStr.equals("MX"))
        {
            return 2;
        }
        else if(countryStr.equals("XX"))
        {
            return 3;
        }
        else
        {
            return 0;
        }
    }

    private static int getUSStateIndex(Context context, String stateStr)
    {
        String[] statesArray = context.getResources().getStringArray(R.array.united_states_arrays);
        if (stateStr == null || stateStr.isEmpty())
        {
            return -1;
        }
        for(int i =0; i< statesArray.length; i++)
        {
            if(stateStr.equals(statesArray[i]))
            {
                return  i;
            }
        }

        return -1;
    }

    private static int getCanadaStateIndex(Context context, String stateStr)
    {
        String[] statesArray = context.getResources().getStringArray(R.array.canada_provinces_arrays);
        if (stateStr == null || stateStr.isEmpty())
        {
            return -1;
        }
        for(int i =0; i< statesArray.length; i++)
        {
            if(stateStr.equals(statesArray[i]))
            {
                return  i;
            }
        }

        return -1;
    }

    public static int getStateIndex(Context context, String countryStr, String stateStr)
    {
        String[] statesArray;
        if(countryStr == null || countryStr.isEmpty())
        {
            int stateIndex = getUSStateIndex(context, stateStr);
            if(stateIndex == -1)
            {
                stateIndex = getCanadaStateIndex(context, stateStr);
                if(stateIndex == -1)
                {
                    stateIndex = 0;
                }
                return stateIndex;
            }
            else
            {
                return stateIndex;
            }
        }
//        else if(countryStr.equals("CN"))
//        {
//            statesArray = context.getResources().getStringArray(R.array.canada_provinces_arrays);
//        }
        else if(countryStr.equals("MX"))
        {
            statesArray = context.getResources().getStringArray(R.array.mexico_states_arrays);
        }
        else if(countryStr.equals("XX"))
        {
            return 0;
        }
        else
        {
            return 0;
        }

        for(int i =0; i< statesArray.length; i++)
        {
            if(stateStr.equals(statesArray[i]))
            {
                return  i;
            }
        }

        return 0;
    }

    public static long getTimeStampFromDateString(String dateString)
    {
        if(dateString == null || dateString.isEmpty())
            return 0;

        GregorianCalendar cal = new GregorianCalendar();
        String[] dateArray = dateString.split("-");
        int year = Integer.valueOf(dateArray[0]);
        int month = Integer.valueOf(dateArray[1]) - 1;
        int day = Integer.valueOf(dateArray[2]);
        cal.set(year, month, day);
        return cal.getTimeInMillis();
    }

    public static long getTodayTimeStamp()
    {
        GregorianCalendar curCal = new GregorianCalendar();
        Calendar c = Calendar.getInstance();
        curCal.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        return curCal.getTimeInMillis();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        MultiDex.install(getBaseContext());
    }

    public boolean isFasTrak()
    {
        boolean isFasTrak = false;

        if(accountInfo != null) {
            int accountType = accountInfo.getAccount_type();

            if (accountType == Constants.ACCOUNT_TYPE_FASTRAK_INDIVIDUAL ||
                    accountType == Constants.ACCOUNT_TYPE_FASTRAK_COMMERCIAL) {
                isFasTrak = true;
            }
        }

        return isFasTrak;
    }

    public static String formatDateBySystem(long mTimestamp)
    {
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getInstance());//DateFormat.getDateInstance();


        Date mDate = new Date(mTimestamp);

        return dateFormat.format(mDate);
    }

    public static String formatDateBySystemFromServer(String dateStr)
    {
        if(dateStr == null || dateStr.isEmpty())
        {
            return "";
        }

        String[] dateArray = dateStr.split("-");
        int year = Integer.valueOf(dateArray[0]);
        int month = Integer.valueOf(dateArray[1]) - 1;
        int day = Integer.valueOf(dateArray[2]);

        Calendar calendar = new GregorianCalendar(year, month, day);

        return formatDateBySystem(calendar.getTimeInMillis());
    }

    public String getSignUpAccountType()
    {
        String accountType = "FasTrak";
        int accountIndex = selectedAccountType;

        if(accountIndex == Constants.ACCOUNT_TYPE_FASTRAK_INDIVIDUAL)
        {
            accountType = "FasTrak";
        }
        else if(accountIndex == Constants.ACCOUNT_TYPE_CHARGE_EXPRESS)
        {
            accountType = "Charge";
        }
        else if(accountIndex == Constants.ACCOUNT_TYPE_PREPAID_EXPRESS)
        {
            accountType = "Prepaid";
        }
        else
        {
            accountType = "Invoice";
        }
        return accountType;
    }

    public static String getFormattedDateFromTimestamp(long timestampInMilliSeconds) {
        Date date = new Date();
        date.setTime(timestampInMilliSeconds);
        String formattedDate = new SimpleDateFormat("MMMM dd, yyyy").format(date);
        return formattedDate;
    }

    public static void sendFileViaEmail(Context context, String fileName, String[] email, String subject, String text){
        final String dir = "/Android/data/" + context.getPackageName();

        File appDataDir = new File(Environment.getExternalStorageDirectory().getPath() + dir);
        Log.i("logtag", "appDataDir: " + appDataDir.getAbsolutePath());
        if (!appDataDir.exists()) {
            appDataDir.mkdirs();
        }

        File logFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ dir, fileName);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        Uri path = Uri.fromFile(logFile);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, email);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);

        // the attachment
        intent.putExtra(Intent.EXTRA_STREAM, path);

        context.startActivity(Intent.createChooser(intent, "Send Email"));
    }

    public void print(String text) {
        if(text != null && !text.isEmpty()) {
            Log.d("settings", text);
        }
    }

    public void print(int id) {
        print(getString(id));
    }

    @SuppressLint("NewApi")
    public boolean testFingerPrintSettings() {
        print("Testing Fingerprint Settings");
        FingerprintManagerCompat fingerprintManager;
        KeyguardManager keyguardManager;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            print("This Android version does not support fingerprint authentication.");
            return false;
        }

        keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        fingerprintManager = FingerprintManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (!keyguardManager.isKeyguardSecure()) {
                print("User hasn't enabled Lock Screen");
                return false;
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            print("User hasn't granted permission to use Fingerprint");
            return false;
        }

        if (!fingerprintManager.hasEnrolledFingerprints()) {
            print("User hasn't registered any fingerprints");
            return false;
        }

        print("Fingerprint authentication is set.\n");

        return true;
    }

    public boolean isAutoPopulateVehicleAndContact() {
        SharedPreferences preference = getSharedPreferences(standardUserDefaults, Context.MODE_PRIVATE);
        boolean rememberLogIn = preference.getBoolean("AutoPopulateVehicleAndContact", false);

        return rememberLogIn;
    }

    public void setAutoPopulateVehicleAndContact(boolean isAutoPopulateVehicleAndContact) {
        SharedPreferences preference = getSharedPreferences(standardUserDefaults, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putBoolean("AutoPopulateVehicleAndContact", isAutoPopulateVehicleAndContact);

        editor.commit();
    }

    public String getAutoPopulateVehicleAndContactInfo() {
        SharedPreferences preference = getSharedPreferences(standardUserDefaults, Context.MODE_PRIVATE);
        String ottRequest = preference.getString("AutoPopulateVehicleAndContactInfo", "");

        return ottRequest;
    }

    public void setAutoPopulateVehicleAndContactInfo(String autoPopulateVehicleAndContactInfo) {
        SharedPreferences preference = getSharedPreferences(standardUserDefaults, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putString("AutoPopulateVehicleAndContactInfo", autoPopulateVehicleAndContactInfo);

        editor.commit();
    }

    public boolean isAutoPopulatePayment() {
        SharedPreferences preference = getSharedPreferences(standardUserDefaults, Context.MODE_PRIVATE);
        boolean rememberLogIn = preference.getBoolean("AutoPopulatePayment", false);

        return rememberLogIn;
    }

    public void setAutoPopulatePayment(boolean isAutoPopulatePayment) {
        SharedPreferences preference = getSharedPreferences(standardUserDefaults, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putBoolean("AutoPopulatePayment", isAutoPopulatePayment);

        editor.commit();
    }

    public String getAutoPopulatePaymentInfo() {
        SharedPreferences preference = getSharedPreferences(standardUserDefaults, Context.MODE_PRIVATE);
        String ottRequest = preference.getString("AutoPopulatePaymentInfo", "");

        return ottRequest;
    }

    public void setAutoPopulatePaymentInfo(String autoPopulatePaymentInfo) {
        SharedPreferences preference = getSharedPreferences(standardUserDefaults, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putString("AutoPopulatePaymentInfo", autoPopulatePaymentInfo);

        editor.commit();
    }

    public static String populateOTTParams()
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
}
