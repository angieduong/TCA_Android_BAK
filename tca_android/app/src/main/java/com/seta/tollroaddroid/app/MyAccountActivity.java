package com.seta.tollroaddroid.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.seta.tollroaddroid.app.adapters.MenuItemAdapter;
import com.seta.tollroaddroid.app.api.Convert;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.custom.MultiButtonsPopupDialog;
import com.seta.tollroaddroid.app.fragments.AlertBannerFragment;
import com.seta.tollroaddroid.app.json.AccountInfo;
import com.seta.tollroaddroid.app.json.RecentToll;
import com.seta.tollroaddroid.app.json.RoadInfo;
import com.seta.tollroaddroid.app.json.ViolationInfo;
import com.seta.tollroaddroid.app.model.MenuItem;
import com.seta.tollroaddroid.app.utilities.Constants;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyAccountActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();
    private DrawerLayout drawer;
    private LinearLayout drawerLinear;
    private ListView lvMenu,lvRecentTolls;
    private ImageView ivMenu,ivCloseMenu;
    private ArrayList<MenuItem> menuItems;
    private ImageView ivOption;
    private LinearLayout llControlPanel;
    private TextView tvLogOut, tvRecentTollsEmpty,tvLegal;
    private MultiButtonsPopupDialog gMultiButtonsPopupDialog;
    private Response.ErrorListener errorListener;
    private List<RecentToll> recentTolls = new ArrayList<RecentToll>();
    private List<RecentToll> filteredRecentTolls = new ArrayList<RecentToll>();
    
    private RecentTollListAdapter recentTollListAdapter;
    private LinearLayout llTollCalculator, llVehicles,llPayments,llTransponders,llContactInformation,llLoginInformation;
    private TextView tvBalance;
    private AlertBannerFragment alertBannerFragment;
    private TextView tvVersion, tvStatement;
    private Spinner filterSpinner;
    private ArrayList<String> categories = new ArrayList<String>();
    private int sortByNewest = 1;
    private ImageView ivSortIndicator;
    private LinearLayout llSort;
    private RelativeLayout rlFilter;

    private Comparator<RecentToll> recentTollNewestComparator, recentTollOldestComparator;
    private int currentFilterIndex = 0;
    private boolean optionExpanded = false;
    @SuppressLint("NewApi")
    private boolean checkFingerPrintFunction() {
        Log.d(LOG_TAG, "Testing Fingerprint Settings");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            Log.d(LOG_TAG, "This Android version does not support fingerprint authentication.");
            return false;
        }

        FingerprintManagerCompat fingerprintManager = FingerprintManagerCompat.from(this);

        if (!fingerprintManager.isHardwareDetected()) {
            // Device doesn't support fingerprint authentication
            Log.d(LOG_TAG, "Device doesn't support fingerprint authentication");
            return false;
        }

        return true;
    }
    
    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit Account page.");
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("sortByNewest", sortByNewest);
        outState.putInt("currentFilterIndex", currentFilterIndex);
        outState.putStringArrayList("categories", categories);
        outState.putBoolean("optionExpanded", optionExpanded);

        Log.d(LOG_TAG, "save currentFilterIndex:"+currentFilterIndex);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);
        FlurryAgent.logEvent("Enter Account page.");

        if(savedInstanceState != null)
        {
            sortByNewest = savedInstanceState.getInt("sortByNewest", 1);
            currentFilterIndex = savedInstanceState.getInt("currentFilterIndex", 0);

            categories = savedInstanceState.getStringArrayList("categories");
            optionExpanded = savedInstanceState.getBoolean("optionExpanded", false);
            Log.d(LOG_TAG, "restore currentFilterIndex:"+currentFilterIndex);
        }

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLinear = (LinearLayout) findViewById(R.id.left_drawer);
        lvMenu = (ListView) findViewById(R.id.lv_menu);
        ivMenu = (ImageView)findViewById(R.id.iv_menu);
        ivCloseMenu = (ImageView)findViewById(R.id.iv_close_menu);
        ivOption = (ImageView)findViewById(R.id.iv_option);
        llControlPanel = (LinearLayout)findViewById(R.id.ll_control_pannel);
        tvLogOut = (TextView)findViewById(R.id.tv_log_out);
        tvLegal = (TextView)findViewById(R.id.tv_legal);
        lvRecentTolls = (ListView) findViewById(R.id.lv_recent_tolls);
        tvRecentTollsEmpty = (TextView)findViewById(R.id.tv_recent_tolls_empty);
        llTollCalculator = (LinearLayout)findViewById(R.id.ll_toll_calculator);
        llVehicles = (LinearLayout)findViewById(R.id.ll_vehicles);
        llPayments = (LinearLayout)findViewById(R.id.ll_payments);
        llTransponders = (LinearLayout)findViewById(R.id.ll_transponders);
        llContactInformation = (LinearLayout)findViewById(R.id.ll_contact_information);
        llLoginInformation = (LinearLayout)findViewById(R.id.ll_log_in_information);

        tvBalance = (TextView)findViewById(R.id.tv_balance);
        alertBannerFragment = (AlertBannerFragment)(getSupportFragmentManager().
                findFragmentById(R.id.fm_alert));
        tvVersion = (TextView)findViewById(R.id.tv_version);
        tvStatement = (TextView)findViewById(R.id.tv_statement);

        filterSpinner = (Spinner)findViewById(R.id.filter_spinner);
        rlFilter = (RelativeLayout)findViewById(R.id.rl_filter);
        llSort = (LinearLayout)findViewById(R.id.ll_sort);
        ivSortIndicator = (ImageView)findViewById(R.id.iv_sort_indicator);

        recentTollOldestComparator =  new Comparator<RecentToll>() {
            @Override
            public int compare(RecentToll lhs, RecentToll rhs) {
                if (lhs != null && rhs != null) {
                    int ret = lhs.compareTo(rhs);

                    //Log.d(LOG_TAG, "lhs datetime:" + lhs.getDatetime() + ", rhs datetime:" + rhs.getDatetime() + ",ret:" + ret);
                    return ret;
                }
                return 0;
            }
        };

        recentTollNewestComparator =  new Comparator<RecentToll>() {
            @Override
            public int compare(RecentToll lhs, RecentToll rhs) {
                if (lhs != null && rhs != null) {
                    int ret = (-1)*lhs.compareTo(rhs);

                    //Log.d(LOG_TAG, "lhs datetime:" + lhs.getDatetime() + ", rhs datetime:" + rhs.getDatetime() + ",ret:" + ret);
                    return ret;
                }
                return 0;
            }
        };
        refreshSortIndicator();

        setVersion();

        initMyAccount();
        refreshMyBalance();
        setupListener();
        setupMenuList();

        initOption();
    }

    private void refreshSortIndicator()
    {
        if(sortByNewest == 1)
        {
            ivSortIndicator.setImageResource(R.drawable.ic_down);
        }
        else
        {
            ivSortIndicator.setImageResource(R.drawable.ic_up);
        }
    }

    private void setVersion()
    {
        PackageInfo pInfo = null;
        String version = "";

        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(),0);
            version = getString(R.string.version)+" "+ pInfo.versionName +" "
                    +getString(R.string.build)+" "+ String.valueOf(pInfo.versionCode);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        tvVersion.setText(version);
    }

    private void initMyAccount()
    {
        if(!TollRoadsApp.getInstance().getMyAccount().isEmpty()) {
            Gson gson = new GsonBuilder().serializeNulls().create();

            TollRoadsApp.getInstance().accountInfo = gson.fromJson(TollRoadsApp.getInstance().getMyAccount(),
                    AccountInfo.class);
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

    private void refreshStatementText()
    {
        AccountInfo accountInfo = TollRoadsApp.getInstance().accountInfo;
        if(accountInfo != null)
        {
            int accountType = accountInfo.getAccount_type();

            if(accountType == Constants.ACCOUNT_TYPE_INVOICE_EXPRESS)
            {
                tvStatement.setText(getString(R.string.invoices));
            }
            else
            {
                tvStatement.setText(getString(R.string.statement));
            }
        }
    }

    private void initOption()
    {
        if (optionExpanded) {
            llControlPanel.setVisibility(View.GONE);
            tvStatement.setVisibility(View.VISIBLE);

            rlFilter.setVisibility(View.VISIBLE);
            llSort.setVisibility(View.VISIBLE);
        } else {
            llControlPanel.setVisibility(View.VISIBLE);
            tvStatement.setVisibility(View.GONE);

            rlFilter.setVisibility(View.GONE);
            llSort.setVisibility(View.GONE);
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
        ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(drawerLinear);
            }
        });
        ivCloseMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(drawerLinear);
            }
        });
        ivOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (llControlPanel.getVisibility() == View.GONE) {
                    llControlPanel.setVisibility(View.VISIBLE);
                    tvStatement.setVisibility(View.GONE);

                    rlFilter.setVisibility(View.GONE);
                    llSort.setVisibility(View.GONE);

                    optionExpanded = false;
                } else {
                    llControlPanel.setVisibility(View.GONE);
                    tvStatement.setVisibility(View.VISIBLE);

                    rlFilter.setVisibility(View.VISIBLE);
                    llSort.setVisibility(View.VISIBLE);
                    optionExpanded = true;
                }
            }
        });

        tvLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpLogoutConfirmDialog();
            }
        });

        llTollCalculator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(v.getContext(), TollCalculatorActivity.class);
            }
        });
        llVehicles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(v.getContext(), VehiclesActivity.class);
            }
        });
        llPayments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(v.getContext(), PaymentsActivity.class);
            }
        });
        if(TollRoadsApp.getInstance().accountInfo != null &&
                TollRoadsApp.getInstance().accountInfo.getAccount_type() != Constants.ACCOUNT_TYPE_FASTRAK_INDIVIDUAL)
        {
            llTransponders.setAlpha(0.5f);
            llTransponders.setOnClickListener(null);
        }
        else
        {
            llTransponders.setAlpha(1.0f);
            llTransponders.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                gotoActivity(v.getContext(), TranspondersActivity.class);
                }
            });
        }

        llContactInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            gotoActivity(v.getContext(), ContactInformationActivity.class);
            }
        });
        llLoginInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            gotoActivity(v.getContext(), LoginInformationActivity.class);
            }
        });
        tvLegal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Bundle bundle = new Bundle();
            bundle.putString(Resource.KEY_URL, Resource.LEGAL_URL);
            bundle.putString(Resource.KEY_TITLE, getString(R.string.legal));

            gotoActivity(v.getContext(),WebActivity.class, bundle);
            }
        });

        tvStatement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            AccountInfo accountInfo = TollRoadsApp.getInstance().accountInfo;

            if(accountInfo != null)
            {
                int accountType = accountInfo.getAccount_type();

                if(accountType == Constants.ACCOUNT_TYPE_INVOICE_EXPRESS)
                {
                    gotoActivity(v.getContext(), RecentInvoicesActivity.class);
                }
                else
                {
                    gotoActivity(v.getContext(), StatementActivity.class);
                }
            }
            }
        });
        refreshStatementText();

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(LOG_TAG, "onItemSelected enter currentFilterIndex:"
                        +currentFilterIndex+",position:"+position+ ",filterSpinner.getCount():"+filterSpinner.getCount());

                if(filterSpinner.getCount() > 1) {
                    if (currentFilterIndex != position) {
                        currentFilterIndex = position;
                        sortAndFilterRecentTolls();
                        loadRecentTollMenu();
                    }
                }
                Log.d(LOG_TAG, "onItemSelected currentFilterIndex:"+currentFilterIndex);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        llSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sortByNewest == 1)
                {
                    sortByNewest = 0;
                }
                else
                {
                    sortByNewest = 1;
                }
                refreshSortIndicator();
                if(filteredRecentTolls != null) {
                    sortRecentTolls();
                    loadRecentTollMenu();
                }
            }
        });
    }

    private void sortRecentTolls()
    {
        if(sortByNewest == 1)
        {
            Collections.sort(filteredRecentTolls, recentTollNewestComparator);
        }
        else
        {
            Collections.sort(filteredRecentTolls, recentTollOldestComparator);
        }
    }

    public class FilterAdapter extends ArrayAdapter<String>{

        public FilterAdapter(Context context, int textViewResourceId,
                            List<String> objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
            View mView = super.getView(position, convertView, parent);

            TextView myText= (TextView)mView.findViewById(android.R.id.text1);

            if(position == currentFilterIndex)
            {
                myText.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
            else
            {
                myText.setTextColor(Color.BLACK);
            }

            ViewGroup.LayoutParams params = mView.getLayoutParams();
            params.height = Convert.dpToPx(36);
            mView.setLayoutParams(params);

            return mView;

        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return super.getView(position, convertView, parent);
        }
    }

    private boolean isContained(List<String> strings, String checkingString)
    {
        if(strings != null && checkingString != null)
        {
            for(String stringItem: strings)
            {
                if(stringItem.contains(checkingString))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private void refreshFilterSpinner()
    {
        String currentFilter = null;
        Log.d(LOG_TAG, "currentFilterIndex:"+currentFilterIndex + ", categories.size():"+categories.size());

        if(currentFilterIndex < categories.size())
        {
            currentFilter = categories.get(currentFilterIndex);
            Log.d(LOG_TAG, "currentFilter:"+currentFilter);

        }
        categories.clear();
        categories.add(getString(R.string.show_all));

        List<String> plates = new ArrayList<String>();
        List<String> transponders = new ArrayList<String>();

        if(recentTolls != null) {
            for (RecentToll recentToll: recentTolls) {
                String plate = recentToll.getLicense_plate();
                String transponder = recentToll.getTransponder_code();

                if(!plate.isEmpty() && !isContained(plates, plate))
                {
                    plates.add(getString(R.string.plate) + " - "+ plate);
                }

                if(!transponder.isEmpty() && !isContained(transponders, transponder))
                {
                    transponders.add(getString(R.string.transponder_first_capital) + " - "+ transponder);
                }
            }

            //if (plates.size() > 1)
            {
                categories.addAll(plates);
            }

            //if (transponders.size() > 1)
            {
                categories.addAll(transponders);
            }
        }

        Log.d(LOG_TAG, "pre currentFilterIndex:"+currentFilterIndex);
        if(currentFilterIndex >= categories.size())
        {
            currentFilterIndex = 0;
        }
        else
        {
            boolean exist = false;
            for(int i =0; i< categories.size(); i++)
            {
                if(categories.get(i).equals(currentFilter))
                {
                    currentFilterIndex = i;
                    exist = true;
                    Log.d(LOG_TAG, "find currentFilter:"+currentFilter);
                    break;
                }
            }

            if(!exist)
            {
                currentFilterIndex = 0;
            }
        }
        Log.d(LOG_TAG, "after currentFilterIndex:"+currentFilterIndex);

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new FilterAdapter(this,
                R.layout.item_spinner, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        filterSpinner.setAdapter(dataAdapter);

        if(currentFilterIndex != 0)
        {
            filterSpinner.setSelection(currentFilterIndex);
        }
    }
    
    private void setupMenuList() {
        menuItems = new ArrayList<MenuItem>();
        
        MenuItem compareAccountsItem = new MenuItem();
        compareAccountsItem.setName(getString(R.string.compare_accounts));
        MenuItem helpItem = new MenuItem();
        helpItem.setName(getString(R.string.help));
        MenuItem feedbackItem = new MenuItem();
        feedbackItem.setName(getString(R.string.feedback));
        MenuItem privacyItem = new MenuItem();
        privacyItem.setName(getString(R.string.privacy));
        MenuItem termOfUseItem = new MenuItem();
        termOfUseItem.setName(getString(R.string.term_of_use));


        menuItems.add(compareAccountsItem);
        menuItems.add(helpItem);
        menuItems.add(feedbackItem);
        menuItems.add(privacyItem);
        menuItems.add(termOfUseItem);

        if(checkFingerPrintFunction()) {
            MenuItem settingsItem = new MenuItem();
            settingsItem.setName(getString(R.string.settings));
            menuItems.add(settingsItem);
        }

        lvMenu.setAdapter(new MenuItemAdapter(this, -1, menuItems));
        
        lvMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        showCompareAccountsActivity();
                        break;
                    case 1:
                        showHelpActivity();
                        break;
                    case 2:
                        showFeedbackActivity();
                        break;
                    case 3:
                        showPrivacyActivity();
                        break;
                    case 4:
                        showTermsActivity();
                        break;
                    case 5:
                        showSettingsActivity();
                        break;

                    default:
                        break;
                }
                drawer.closeDrawer(drawerLinear);
            }
        });
    }

    private void showCompareAccountsActivity() {
//        Intent intent = new Intent();
//        intent.setClass(this, CompareAccountsActivity.class);
//
//        startActivity(intent);

        Bundle bundle = new Bundle();
        bundle.putString(Resource.KEY_URL, Resource.COMPARE_ACCOUNT_URL);
        bundle.putString(Resource.KEY_TITLE, getString(R.string.compare_accounts));

        gotoActivity(this,WebActivity.class, bundle);
    }
    private void showHelpActivity() {
        Intent intent = new Intent();
        intent.setClass(this, HelpActivity.class);

        startActivity(intent);
    }
    private void showFeedbackActivity() {
        Intent intent = new Intent();
        intent.setClass(this, FeedbackActivity.class);

        startActivity(intent);
    }
    private void showPrivacyActivity() {
        Bundle bundle = new Bundle();
        bundle.putString(Resource.KEY_URL, Resource.PRIVACY_URL);
        bundle.putString(Resource.KEY_TITLE, getString(R.string.privacy));

        gotoActivity(this,WebActivity.class, bundle);
    }
    private void showTermsActivity() {
        Bundle bundle = new Bundle();
        bundle.putString(Resource.KEY_URL, Resource.TERMS_URL);
        bundle.putString(Resource.KEY_TITLE, getString(R.string.term_of_use));

        gotoActivity(this,WebActivity.class, bundle);
    }
    private void showSettingsActivity() {
        Intent intent = new Intent();
        intent.setClass(this, SettingsActivity.class);

        startActivity(intent);
    }

    private void showUpLogoutConfirmDialog()
    {
        gMultiButtonsPopupDialog = new MultiButtonsPopupDialog(this,R.style.CustomProgressDialog);
        gMultiButtonsPopupDialog.createDialog(this);
        gMultiButtonsPopupDialog.CutomizeTitle(View.GONE, "");

        gMultiButtonsPopupDialog.CutomizeContent(View.GONE, null);
        gMultiButtonsPopupDialog.CutomizeExtraButton(View.GONE, 0, "", 0, null);
        gMultiButtonsPopupDialog.CutomizePositiveButton(View.VISIBLE, 0, getString(R.string.log_out), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogoutAction();
            }
        });
        gMultiButtonsPopupDialog.CutomizeNeutralButton(View.GONE, 0, "", null);
        gMultiButtonsPopupDialog.CutomizeNegativeButton(View.VISIBLE, 0, getString(R.string.cancel), null);

        gMultiButtonsPopupDialog.show();
    }

    private void LogoutAction()
    {
        TollRoadsApp.getInstance().setToken("");
        TollRoadsApp.getInstance().accountInfo = null;
        TollRoadsApp.getInstance().setMyAccount("");

        ServerDelegate.logOutReq();
        gotoActivity(this, LandingPageActivity.class,
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    private RecentTollListAdapter getRecentTollMenuAdapter() {
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

        for (int i = 0; i < filteredRecentTolls.size(); i++) {
            RecentToll recentToll = filteredRecentTolls.get(i);
            if(recentToll != null) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                String dateTime = recentToll.getDatetime();
                int spaceIndex = dateTime.indexOf(" ");
                String date = dateTime.substring(0, spaceIndex);
                String time = dateTime.substring(spaceIndex+1);

                map.put("tv_date", date);
                map.put("tv_time", time);
                map.put("tv_description", recentToll.getDescription());
                map.put("tv_amount", recentToll.getAmount());
                if(!recentToll.getTransponder_code().isEmpty()) {
                    map.put("tv_transponder_number", recentToll.getTransponder_code());
                }
                else
                {
                    map.put("tv_transponder_number", recentToll.getLicense_plate());
                }

                data.add(map);
            }
        }

        RecentTollListAdapter recentTollListAdapter = new RecentTollListAdapter(
                this, data,
                R.layout.item_recent_tolls_menu,
                new String[] { "tv_date", "tv_time", "tv_description","tv_amount","tv_transponder_number"}, new int[] { R.id.tv_date,
                R.id.tv_time, R.id.tv_description, R.id.tv_amount,R.id.tv_transponder_number});

        return recentTollListAdapter;
    }

    class RecentTollListAdapter extends SimpleAdapter {
        Context mContext;
        private LayoutInflater l_Inflater;

        public RecentTollListAdapter(Context context,
                                    List<? extends Map<String, ?>> data, int resource,
                                    String[] from, int[] to) {
            super(context, data, resource, from, to);

            mContext = context;
            l_Inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            if(filteredRecentTolls != null) {
                return filteredRecentTolls.size();
            }
            else
            {
                return 0;
            }
        }

        @Override
        public View getView(int position, View convertView, final ViewGroup parent) {
            RecentToll recentToll = filteredRecentTolls.get(position);
            if (convertView == null) {
                convertView = l_Inflater.inflate(
                        R.layout.item_recent_tolls_menu, null);
            }

            TextView tvDate = (TextView)convertView.findViewById(R.id.tv_date);
            TextView tvTime = (TextView)convertView.findViewById(R.id.tv_time);
            TextView tvDescription = (TextView)convertView.findViewById(R.id.tv_description);
            TextView tvAmount = (TextView)convertView.findViewById(R.id.tv_amount);
            TextView tvTransponderNumber = (TextView)convertView.findViewById(R.id.tv_transponder_number);



            String dateTime = recentToll.getDatetime();
            int spaceIndex = dateTime.indexOf(" ");
            String date = dateTime.substring(0, spaceIndex);
            String time = dateTime.substring(spaceIndex+1);

            tvDate.setText(date);
            tvTime.setText(time);
            tvDescription.setText(recentToll.getDescription());
            tvAmount.setText(recentToll.getAmount());

            if(!recentToll.getTransponder_code().isEmpty()) {
                tvTransponderNumber.setText(recentToll.getTransponder_code());
            }
            else
            {
                tvTransponderNumber.setText(recentToll.getLicense_plate());
            }


            return convertView;
        }
    }
    
    private void loadRecentTollMenu()
    {
        if(filteredRecentTolls.size() > 0)
        {
            tvRecentTollsEmpty.setVisibility(View.GONE);
        }
        else
        {
            tvRecentTollsEmpty.setVisibility(View.VISIBLE);
        }

        if(recentTollListAdapter == null)
        {
            recentTollListAdapter = getRecentTollMenuAdapter();
            lvRecentTolls.setAdapter(recentTollListAdapter);
        }
        else
        {
            recentTollListAdapter.notifyDataSetInvalidated();
        }
    }

    private void filterRecentTolls()
    {
        if(recentTolls != null) {
            if(currentFilterIndex >= categories.size())
            {
                currentFilterIndex = 0;
            }

            filteredRecentTolls.clear();
            if (currentFilterIndex == 0) {
                filteredRecentTolls.addAll(recentTolls);
            }
            else
            {
                String filter = categories.get(currentFilterIndex);
                String plateType = getString(R.string.plate);
                String transponderType = getString(R.string.transponder_first_capital);

                if(filter.contains(plateType))
                {
                    filter = filter.replace(plateType, "");
                    filter = filter.replace(" - ", "");
                    for(RecentToll recentToll: recentTolls)
                    {
                        if(recentToll.getLicense_plate().equals(filter))
                        {
                            filteredRecentTolls.add(recentToll);
                        }
                    }
                }
                else if(filter.contains(transponderType))
                {
                    filter = filter.replace(transponderType, "");
                    filter = filter.replace(" - ", "");

                    for(RecentToll recentToll: recentTolls)
                    {
                        if(recentToll.getTransponder_code().equals(filter))
                        {
                            filteredRecentTolls.add(recentToll);
                        }
                    }
                }
            }
        }
    }

    private void sortAndFilterRecentTolls()
    {
        filterRecentTolls();
        sortRecentTolls();
    }

    private void getRecentTolls()
    {
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
                                Type listType = new TypeToken<ArrayList<RecentToll>>() {}.getType();

                                recentTolls = gson.fromJson(info, listType);

                                refreshFilterSpinner();
//                                if(recentTolls != null) {
//                                   Collections.sort(recentTolls, recentTollNewestComparator);
//                                }
                                sortAndFilterRecentTolls();
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

        ServerDelegate.getRecentTolls(Resource.URL_PAYMENT, listener, errorListener);
    }

    private void sendAccountRequest()
    {
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
                                refreshMyBalance();
                                refreshStatementText();
//                                AccountInfo accountInfo = TollRoadsApp.getInstance().accountInfo;
//                                if(accountInfo != null)
//                                {
//                                    if(accountInfo.getAlert_list() == null)
//                                    {
//                                        accountInfo.setAlert_list(new ArrayList<AlertBanner>());
//                                    }
//                                    ArrayList<AlertBanner> alertBanners = accountInfo.getAlert_list();
//
//                                    AlertBanner alertBanner = new AlertBanner();
//                                    alertBanner.setAlert_type("payment");
//                                    alertBanner.setAlert_action("fix");
//                                    alertBanner.setAlert_message("Payment method is expiring within 30 days");
//                                    alertBanners.add(alertBanner);
//
//                                    alertBanner = new AlertBanner();
//                                    alertBanner.setAlert_type("vehicle");
//                                    alertBanner.setAlert_action("fix");
//                                    alertBanner.setAlert_message("No associated vehicle");
//                                    alertBanners.add(alertBanner);
//                                }

                                alertBannerFragment.refresh();
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
        sendAccountRequest();
        getRecentTolls();
    }
}
