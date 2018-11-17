package com.seta.tollroaddroid.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.json.PaySelectedViolationsRequest;
import com.seta.tollroaddroid.app.json.SelectedUnpaidViolationsResponse;
import com.seta.tollroaddroid.app.json.ViolationInfo;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ViolationConfirmationActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private TextView tvTotalCharge,tvHome, tvPaidViolationTitle, tvConfirmationMessage;
    
    private TextView tvContactName, tvPhone, tvEmail;

    private ScrollView svSummary;
    
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
        FlurryAgent.logEvent("Exit Violation_Confirmation page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_violation_confirmation);
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

        FlurryAgent.logEvent("Enter Violation_Confirmation page.");

        getWindowManager().getDefaultDisplay().getMetrics(gDm);

        svSummary = (ScrollView)findViewById(R.id.sv_summary);

        tvHome = (TextView)findViewById(R.id.tv_home);
        tvTotalCharge = (TextView)findViewById(R.id.tv_total_charge);
        
        tvContactName = (TextView)findViewById(R.id.tv_contact_name);
        tvPhone = (TextView)findViewById(R.id.tv_phone);
        tvEmail = (TextView)findViewById(R.id.tv_email);

        lvViolations = (ListView) findViewById(R.id.lv_violation);

        tvPaidViolationTitle = (TextView)findViewById(R.id.tv_paid_violation_title);

        tvConfirmationMessage = (TextView)findViewById(R.id.tv_confirmation_message);

        Intent intent = getIntent();
        if(intent != null)
        {
            String confMessage = intent.getStringExtra(Resource.KEY_CONF_MESSAGE);
            if(confMessage != null)
            {
                tvConfirmationMessage.setText(confMessage);
            }
        }

        setupListener();
    }

    private void setupListener()
    {
        tvHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(v.getContext(), LandingPageActivity.class, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            gotoActivity(ViolationConfirmationActivity.this, LandingPageActivity.class, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            return  true;
        }
        return super.onKeyDown(keyCode, event);
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
        loadSelectedViolationsList();

        tvPaidViolationTitle.setText(getString(R.string.violations_paid, selectedUnpaidViolations.size()));

        if(TollRoadsApp.getInstance().selectedUnpaidViolationsResponse != null &&
                TollRoadsApp.getInstance().selectedUnpaidViolationsResponse.getSel_unpaid_vios_total_amount_due() != null)
        {
            tvTotalCharge.setText(getString(R.string.total_amount_due,
                    TollRoadsApp.getInstance().selectedUnpaidViolationsResponse.getSel_unpaid_vios_total_amount_due()));

            //tvTotalCharge.setVisibility(View.VISIBLE);
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
