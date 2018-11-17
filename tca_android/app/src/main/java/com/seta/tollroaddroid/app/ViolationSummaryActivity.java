package com.seta.tollroaddroid.app;

import android.annotation.TargetApi;
import android.content.Context;
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
import android.view.LayoutInflater;
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
import com.seta.tollroaddroid.app.custom.MultiButtonsPopupDialog;
import com.seta.tollroaddroid.app.json.AutoPopulatePaymentInfo;
import com.seta.tollroaddroid.app.json.AutoPopulateVehicleAndContactInfo;
import com.seta.tollroaddroid.app.json.OTTUserInfoRequest;
import com.seta.tollroaddroid.app.json.OttTrip;
import com.seta.tollroaddroid.app.json.PaySelectedViolationsRequest;
import com.seta.tollroaddroid.app.json.SelectedUnpaidViolationsResponse;
import com.seta.tollroaddroid.app.json.ViolationInfo;
import com.seta.tollroaddroid.app.model.MenuItem;
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
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class ViolationSummaryActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;

    private TextView tvTotalCharge,tvPay, tvSelectViolationTitle, tvPaymentType;
    
    
    private TextView tvCardNo, tvNameOnCard, tvExpirationDate, tvZipCode,tvCVV;
    private TextView tvRoutingNo, tvFirstName, tvLastName, tvAccountNo;
    private TextView tvContactName, tvPhone, tvEmail;

    private ScrollView svSummary;
    
    private LinearLayout llCreditCard, llECheck, llGooglePay;
    
    
    private DisplayMetrics gDm = new DisplayMetrics();

    private ListView lvViolations;
    
    private ViolationListAdapter violationListAdapter;

    private ArrayList<ViolationInfo> selectedUnpaidViolations = new ArrayList<ViolationInfo>();
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("SelectedUnpaidViolationsResponse", TollRoadsApp.getInstance().selectedUnpaidViolationsResponse);
        outState.putSerializable("PaySelectedViolationsRequest", TollRoadsApp.getInstance().paySelectedViolationsRequest);
    }

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit Violation_Summary page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_violation_summary);
        if(savedInstanceState != null)
        {
            TollRoadsApp.getInstance().selectedUnpaidViolationsResponse = (SelectedUnpaidViolationsResponse)
                    savedInstanceState.getSerializable("SelectedUnpaidViolationsResponse");

            TollRoadsApp.getInstance().paySelectedViolationsRequest = (PaySelectedViolationsRequest)
                    savedInstanceState.getSerializable("PaySelectedViolationsRequest");
        }

        if(TollRoadsApp.getInstance().selectedUnpaidViolationsResponse != null
            && TollRoadsApp.getInstance().selectedUnpaidViolationsResponse.getSel_unpaid_vios_info() != null)
        {
            selectedUnpaidViolations = TollRoadsApp.getInstance().selectedUnpaidViolationsResponse.getSel_unpaid_vios_info();
        }

        FlurryAgent.logEvent("Enter Violation_Summary page.");

        getWindowManager().getDefaultDisplay().getMetrics(gDm);

        svSummary = (ScrollView)findViewById(R.id.sv_summary);
        
        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvPay = (TextView)findViewById(R.id.tv_pay);
        tvTotalCharge = (TextView)findViewById(R.id.tv_total_charge);
        llCreditCard = (LinearLayout)findViewById(R.id.ll_credit_card);
        llECheck = (LinearLayout)findViewById(R.id.ll_echeck);
        llGooglePay = (LinearLayout)findViewById(R.id.ll_google_pay);

        tvCardNo = (TextView)findViewById(R.id.tv_card_number);
        tvNameOnCard = (TextView)findViewById(R.id.tv_name_on_card);
        tvExpirationDate = (TextView)findViewById(R.id.tv_exp_date);
        tvZipCode = (TextView)findViewById(R.id.tv_zip_code);
        tvCVV = (TextView)findViewById(R.id.tv_cvv);

        tvRoutingNo = (TextView)findViewById(R.id.tv_routing_no);
        tvFirstName = (TextView)findViewById(R.id.tv_first_name);
        tvLastName = (TextView)findViewById(R.id.tv_last_name);
        tvAccountNo = (TextView)findViewById(R.id.tv_account_no);

        tvContactName = (TextView)findViewById(R.id.tv_contact_name);
        tvPhone = (TextView)findViewById(R.id.tv_phone);
        tvEmail = (TextView)findViewById(R.id.tv_email);

        lvViolations = (ListView) findViewById(R.id.lv_violation);

        tvSelectViolationTitle = (TextView)findViewById(R.id.tv_select_violation_title);
        tvPaymentType = (TextView)findViewById(R.id.tv_payment_type);

        setupListener();
    }

    private void setupListener()
    {
        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //gotoActivity(v.getContext(), ViolationPaymentInfoActivity.class, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                finish();
            }
        });

        tvPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                violationPayReq();
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
//            gotoActivity(getApplicationContext(), ViolationPaymentInfoActivity.class,
//                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            finish();
            return  true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private String populateParams()
    {
        String params = "";

        if(TollRoadsApp.getInstance().paySelectedViolationsRequest != null) {
            Gson gson = new GsonBuilder().serializeNulls().create();
            String jsonStr = gson.toJson(TollRoadsApp.getInstance().paySelectedViolationsRequest,
                    PaySelectedViolationsRequest.class);
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

    private void violationPayReq()
    {
        String params = populateParams();

        params = params +"&"+ServerDelegate.getCommonUrlExtra();
        params = params +"&"+Resource.KEY_ACTION+"="+ Resource.ACTION_PAY_SELECT_VIOLATION;

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
        Response.Listener listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                closeProgressDialog();
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, "response:" + response);
                        if(checkResponse(response.toString())) {
                            String confMessage = response.optString(Resource.KEY_CONF_MESSAGE);
                            Bundle bundle = new Bundle();
                            bundle.putString(Resource.KEY_CONF_MESSAGE, confMessage);

                            gotoActivity(getApplicationContext(), ViolationConfirmationActivity.class,
                                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK,
                                    bundle);
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

        String url = Resource.URL_VIOLATION_PAYMENT+"?"+params;
        ServerDelegate.commonPostRequest(url, listener, errorListener);
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
        PaySelectedViolationsRequest paySelectedViolationsRequest = TollRoadsApp.getInstance().paySelectedViolationsRequest;
        if(paySelectedViolationsRequest != null) {
            if (paySelectedViolationsRequest.getPayment_type() == Constants.CREDIT_CARD_TYPE) {
                llCreditCard.setVisibility(View.VISIBLE);
                llECheck.setVisibility(View.GONE);
                llGooglePay.setVisibility(View.GONE);

                tvPaymentType.setVisibility(View.VISIBLE);
                tvPaymentType.setText(getString(R.string.card_information));
                String maskCardNumber = maskNumber(paySelectedViolationsRequest.getCard_number(), 4);

                tvCardNo.setText(maskCardNumber);
                tvNameOnCard.setText(paySelectedViolationsRequest.getCard_holder_name());
                tvExpirationDate.setText(paySelectedViolationsRequest.getExpired_date());
                tvZipCode.setText(paySelectedViolationsRequest.getZip_code());
                tvCVV.setText(String.valueOf(paySelectedViolationsRequest.getCvv2()));

            }
            else if (paySelectedViolationsRequest.getPayment_type() == Constants.ELECTRONIC_CHECK_TYPE) {
                llCreditCard.setVisibility(View.GONE);
                llECheck.setVisibility(View.VISIBLE);
                llGooglePay.setVisibility(View.GONE);

                tvPaymentType.setVisibility(View.VISIBLE);
                tvPaymentType.setText(getString(R.string.check_information));

                String maskRoutingNumber = maskNumber(paySelectedViolationsRequest.getRouting_number(), 3);
                String maskAccountNumber = maskNumber(paySelectedViolationsRequest.getAccount_number(), 3);

                tvRoutingNo.setText(maskRoutingNumber);
                tvFirstName.setText(paySelectedViolationsRequest.getFirst_name());
                tvLastName.setText(paySelectedViolationsRequest.getLast_name());
                tvAccountNo.setText(maskAccountNumber);
            }
            else
            {
                tvPaymentType.setVisibility(View.GONE);
                llCreditCard.setVisibility(View.GONE);
                llECheck.setVisibility(View.GONE);
                llGooglePay.setVisibility(View.VISIBLE);
            }
        }
    }


    private void initContactInfo()
    {
        PaySelectedViolationsRequest paySelectedViolationsRequest = TollRoadsApp.getInstance().paySelectedViolationsRequest;

        if(paySelectedViolationsRequest != null) {
            tvContactName.setText(paySelectedViolationsRequest.getAddress_contact());

            tvPhone.setText(paySelectedViolationsRequest.getPrimary_phone());
            tvEmail.setText(paySelectedViolationsRequest.getEmail_address());
        }
    }
    private void initWidgetValue()
    {
        initContactInfo();
        initPaymentWidget();
        loadSelectedViolationsList();

        tvSelectViolationTitle.setText(getString(R.string.violations_to_pay, selectedUnpaidViolations.size()));

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
        
        svSummary.post(new Runnable() {
            @Override
            public void run() {
                svSummary.scrollTo(0,0);
            }
        });
    }


    private ViolationListAdapter getViolationMenuAdapter() {
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

        for (int i = 0; i < selectedUnpaidViolations.size(); i++) {
            ViolationInfo violationInfo = selectedUnpaidViolations.get(i);
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
            if(selectedUnpaidViolations != null) {
                return selectedUnpaidViolations.size();
            }
            else
            {
                return 0;
            }
        }

        @Override
        public View getView(int position, View convertView, final ViewGroup parent) {
            ViolationInfo violationInfo = selectedUnpaidViolations.get(position);
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

            return convertView;
        }
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

    private void loadSelectedViolationsList()
    {
        if(selectedUnpaidViolations != null
                && selectedUnpaidViolations.size() > 0)
        {
            lvViolations.setVisibility(View.VISIBLE);

            if(violationListAdapter == null)
            {
                violationListAdapter = getViolationMenuAdapter();
                lvViolations.setAdapter(violationListAdapter);

                setListViewTotalHeight(lvViolations,gDm.widthPixels);
            }
            else
            {
                violationListAdapter.notifyDataSetChanged();
            }
        }
        else
        {
            lvViolations.setVisibility(View.GONE);
        }
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
