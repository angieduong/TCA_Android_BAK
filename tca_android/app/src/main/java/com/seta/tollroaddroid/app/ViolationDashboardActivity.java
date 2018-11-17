package com.seta.tollroaddroid.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.seta.tollroaddroid.app.adapters.MenuItemAdapter;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.custom.MultiButtonsPopupDialog;
import com.seta.tollroaddroid.app.fragments.AlertBannerFragment;
import com.seta.tollroaddroid.app.json.AccountInfo;

import com.seta.tollroaddroid.app.json.PaySelectedViolationsRequest;
import com.seta.tollroaddroid.app.json.SelectedUnpaidViolationsResponse;
import com.seta.tollroaddroid.app.json.Transponder;
import com.seta.tollroaddroid.app.json.ViolationInfo;
import com.seta.tollroaddroid.app.json.ViolationLoginResponse;
import com.seta.tollroaddroid.app.model.MenuItem;
import com.seta.tollroaddroid.app.utilities.Constants;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViolationDashboardActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();
    private DrawerLayout drawer;
    private LinearLayout drawerLinear;
    private ListView lvMenu,lvViolations;
    private ImageView ivMenu,ivCloseMenu;
    private ArrayList<MenuItem> menuItems;

    private TextView tvViolationsEmpty;
    private MultiButtonsPopupDialog gMultiButtonsPopupDialog;
    private Response.ErrorListener errorListener;
    
    private ViolationListAdapter violationListAdapter;
    
    private TextView tvVersion, tvPayNow;
    private ArrayList<ViolationInfo> unpaidViolationInfos;
    private int totalSelectedNumber = 0;
    private ImageView ivGoBack;

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit violation dashboard page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_violation_dashboard);
        FlurryAgent.logEvent("Enter violation dashboard page.");
        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLinear = (LinearLayout) findViewById(R.id.left_drawer);
        lvMenu = (ListView) findViewById(R.id.lv_menu);
        ivMenu = (ImageView)findViewById(R.id.iv_menu);
        ivCloseMenu = (ImageView)findViewById(R.id.iv_close_menu);

        lvViolations = (ListView) findViewById(R.id.lv_violation);
        tvViolationsEmpty = (TextView)findViewById(R.id.tv_violation_empty);
        
        tvVersion = (TextView)findViewById(R.id.tv_version);
        tvPayNow = (TextView)findViewById(R.id.tv_pay_now);

        setVersion();
        
        setupListener();
        setupMenuList();

        initViolationDataAndLoad();
        Toast.makeText(this, getString(R.string.toast_violation_logged_in), Toast.LENGTH_LONG).show();
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

    private void initViolationDataAndLoad()
    {
        if(TollRoadsApp.getInstance().violationLoginResponse != null
                && TollRoadsApp.getInstance().violationLoginResponse.getUnpaid_vios_info() != null
                && TollRoadsApp.getInstance().violationLoginResponse.getUnpaid_vios_info().size() > 0)
        {
            unpaidViolationInfos = TollRoadsApp.getInstance().violationLoginResponse.getUnpaid_vios_info();
        }
        totalSelectedNumber = 0;
        loadViolationsList();
    }

    private void setupListener()
    {
        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(ViolationDashboardActivity.this, LandingPageActivity.class,
                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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

        tvPayNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectUnpaidViolationRequest();
            }
        });

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

    private ViolationListAdapter getViolationMenuAdapter() {
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

        for (int i = 0; i < unpaidViolationInfos.size(); i++) {
            ViolationInfo violationInfo = unpaidViolationInfos.get(i);
            if(violationInfo != null) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                String dateTime = violationInfo.getViolation_date();
                int spaceIndex = dateTime.indexOf(" ");
                String date = dateTime.substring(0, spaceIndex);
                String time = dateTime.substring(spaceIndex+1);

                map.put("tv_date", date);
                map.put("tv_time", time);
                map.put("tv_description", violationInfo.getToll_point());
                map.put("tv_amount", violationInfo.getAmount_due());
                map.put("tv_plate", violationInfo.getPlate_state() +" "+violationInfo.getPlate_number());
                map.put("tv_due_day", getString(R.string.violation_due_by, violationInfo.getDue_date()));
                data.add(map);
            }
        }

        ViolationListAdapter violationListAdapter = new ViolationListAdapter(
                this, data,
                R.layout.item_violation_menu,
                new String[] { "tv_date", "tv_time", "tv_description","tv_amount","tv_plate", "tv_due_day"}, new int[] { R.id.tv_date,
                R.id.tv_time, R.id.tv_description, R.id.tv_amount,R.id.tv_plate, R.id.tv_due_day});

        return violationListAdapter;
    }

    class ViolationListAdapter extends SimpleAdapter {
        Context mContext;
        private LayoutInflater l_Inflater;

        public ViolationListAdapter(Context context,
                                      List<? extends Map<String, ?>> data, int resource,
                                      String[] from, int[] to) {
            super(context, data, resource, from, to);

            mContext = context;
            l_Inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            if(unpaidViolationInfos != null) {
                return unpaidViolationInfos.size();
            }
            else
            {
                return 0;
            }
        }

        @Override
        public View getView(int position, View convertView, final ViewGroup parent) {
            ViolationInfo violationInfo = unpaidViolationInfos.get(position);
            if (convertView == null) {
                convertView = l_Inflater.inflate(
                        R.layout.item_violation_menu, null);
            }
            LinearLayout llViolation = (LinearLayout)convertView.findViewById(R.id.ll_violation);
            TextView tvDate = (TextView)convertView.findViewById(R.id.tv_date);
            TextView tvTime = (TextView)convertView.findViewById(R.id.tv_time);
            TextView tvDescription = (TextView)convertView.findViewById(R.id.tv_description);
            TextView tvAmount = (TextView)convertView.findViewById(R.id.tv_amount);
            TextView tvPlate = (TextView)convertView.findViewById(R.id.tv_plate);
            TextView tvDueDay = (TextView)convertView.findViewById(R.id.tv_due_day);

            String dateTime = violationInfo.getViolation_date();
            int spaceIndex = dateTime.indexOf(" ");
            String date = dateTime.substring(0, spaceIndex);
            String time = dateTime.substring(spaceIndex+1);

            tvDate.setText(date);
            tvTime.setText(time);
            tvDescription.setText(violationInfo.getToll_point());
            tvAmount.setText(violationInfo.getAmount_due());

            String plate = violationInfo.getPlate_state() +" "+violationInfo.getPlate_number();
            tvPlate.setText(plate);
            tvDueDay.setText(getString(R.string.violation_due_by, violationInfo.getDue_date()));

            if(violationInfo.isSelected())
            {
                llViolation.setBackgroundColor(getColor(R.color.colorViolationSelectedBG));
            }
            else
            {
                llViolation.setBackgroundColor(getColor(R.color.colorLightGray));
            }
            llViolation.setTag(position);
            llViolation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = (int)v.getTag();

                    if(position < unpaidViolationInfos.size())
                    {
                        ViolationInfo violationInfo = unpaidViolationInfos.get(position);
                        if(violationInfo != null)
                        {
                            if(violationInfo.isSelected())
                            {
                                violationInfo.setSelected(false);
                                if(totalSelectedNumber > 0) {
                                    totalSelectedNumber = totalSelectedNumber - 1;
                                }
                                else
                                {
                                    totalSelectedNumber = 0;
                                }
                                v.setBackgroundColor(getResources().getColor(R.color.colorLightGray));
                            }
                            else
                            {
                                violationInfo.setSelected(true);
                                totalSelectedNumber = totalSelectedNumber + 1;
                                v.setBackgroundColor(getResources().getColor(R.color.colorViolationSelectedBG));
                            }
                        }
                        if(totalSelectedNumber > 0)
                        {
                            tvPayNow.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            tvPayNow.setVisibility(View.GONE);
                        }
                    }
                }
            });

            return convertView;
        }
    }

    private void loadViolationsList()
    {
        if(unpaidViolationInfos != null
                && unpaidViolationInfos.size() > 0)
        {
            tvViolationsEmpty.setVisibility(View.GONE);
            lvViolations.setVisibility(View.VISIBLE);

            if(violationListAdapter == null)
            {
                violationListAdapter = getViolationMenuAdapter();
                lvViolations.setAdapter(violationListAdapter);
            }
            else
            {
                violationListAdapter.notifyDataSetChanged();
            }
        }
        else
        {
            tvViolationsEmpty.setVisibility(View.VISIBLE);
            lvViolations.setVisibility(View.GONE);
        }
    }

    private void refreshUnpaidViolationData(ArrayList<ViolationInfo> newUnpaidViolationInfos)
    {
        if(newUnpaidViolationInfos != null && unpaidViolationInfos != null)
        {
            totalSelectedNumber = 0;
            for(ViolationInfo newViolationInfo: newUnpaidViolationInfos)
            {
                if(newViolationInfo != null && newViolationInfo.getViolation_number() != null)
                {
                    for (ViolationInfo violationInfo : unpaidViolationInfos) {
                        if(violationInfo != null && violationInfo.getViolation_number()!= null) {
                            if (violationInfo.getViolation_number().equals(newViolationInfo.getViolation_number())) {
                                newViolationInfo.setSelected(violationInfo.isSelected());
                                break;
                            }
                        }
                    }

                    if(newViolationInfo.isSelected())
                    {
                        totalSelectedNumber++;
                    }
                }
            }
            unpaidViolationInfos.clear();
            unpaidViolationInfos.addAll(newUnpaidViolationInfos);
        }
    }

    private void getUnpaidViolationRequest()
    {
        Response.Listener listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, response.toString());
                        if(checkResponse(response.toString())) {
                            Gson gson = new GsonBuilder().serializeNulls().create();
                            ViolationLoginResponse violationLoginResponse = gson.fromJson(response.toString(), ViolationLoginResponse.class);

                            if(violationLoginResponse != null) {
                                refreshUnpaidViolationData(violationLoginResponse.getUnpaid_vios_info());
                                loadViolationsList();
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

        ServerDelegate.getUnpaidViolationRequest(Resource.URL_VIOLATION_PAYMENT, listener, errorListener);
    }

    private String populateSelectedViolationIDs()
    {
        StringBuilder stringBuilder = new StringBuilder();

        if(unpaidViolationInfos != null)
        {
            for(ViolationInfo violationInfo: unpaidViolationInfos)
            {
                if(violationInfo != null && violationInfo.isSelected())
                {
                    stringBuilder.append(violationInfo.getViolation_number());
                    stringBuilder.append(",");
                }
            }

            if(stringBuilder.length() >= 1) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }
        }

        return stringBuilder.toString();
    }

    private void selectUnpaidViolationRequest()
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
                            Gson gson = new GsonBuilder().serializeNulls().create();
                            TollRoadsApp.getInstance().selectedUnpaidViolationsResponse = gson.fromJson(response.toString(), SelectedUnpaidViolationsResponse.class);

                            TollRoadsApp.getInstance().paySelectedViolationsRequest = new PaySelectedViolationsRequest();
                            TollRoadsApp.getInstance().paySelectedViolationsRequest.setVioIDs(populateSelectedViolationIDs());

                            gotoActivity(ViolationDashboardActivity.this, ViolationContactInfoActivity.class);
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

        showProgressDialog();

        String url = Resource.URL_VIOLATION_PAYMENT;
        url = url +"?"+ ServerDelegate.getCommonUrlExtra();
        url = url +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_SELECTED_UNPAID_VIOLATION;
        url = url +"&"+Resource.KEY_VIOLATION_IDS +"="+populateSelectedViolationIDs();

        ServerDelegate.commonGetRequest(url, listener, errorListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getUnpaidViolationRequest();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            gotoActivity(ViolationDashboardActivity.this, LandingPageActivity.class,
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            return  true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
