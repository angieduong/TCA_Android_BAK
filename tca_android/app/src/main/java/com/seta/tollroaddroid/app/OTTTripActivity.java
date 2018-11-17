package com.seta.tollroaddroid.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.seta.tollroaddroid.app.api.Convert;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.custom.MultiButtonsPopupDialog;
import com.seta.tollroaddroid.app.json.AccountInfo;
import com.seta.tollroaddroid.app.json.CommonResponse;
import com.seta.tollroaddroid.app.json.LocInfo;
import com.seta.tollroaddroid.app.json.OTTUserInfoRequest;
import com.seta.tollroaddroid.app.json.OttTrip;
import com.seta.tollroaddroid.app.json.RoadInfo;
import com.seta.tollroaddroid.app.json.TripInfo;
import com.seta.tollroaddroid.app.utilities.Constants;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OTTTripActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private TextView tvGoNext;
    private AccountInfo accountInfo = new AccountInfo();
    private TextView tvTotalCharge;


    private LinearLayout llTripRental, llTrip;

    private RelativeLayout layoutDatePicker;
    private DatePicker datePicker;
    private TextView tvDatePickerConfirm,tvDatePickerCancel;
    private TextView tvStartDate,tvEndDate;
    private boolean selectStartDate = true;
    private Response.ErrorListener errorListener;

    private ListView lvTrip;
    private LinearLayout llAddAnotherTrip;
    private List<OttTrip> ottTrips = TollRoadsApp.getInstance().ottTrips;

    private List<LocInfo> locList = new ArrayList<LocInfo>();
    private List<RoadInfo> roadList = new ArrayList<RoadInfo>();
    private View.OnClickListener tripDateOnClickListener;
    private boolean tripDateSelected = false;
    private int tripDateIndex = 0;
    private TextView selectedTripDate = null;
    private TripListAdapter tripListAdapter;
    private AdapterView.OnItemSelectedListener spRoadItemSelectedListener;
    private AdapterView.OnItemSelectedListener spStartItemSelectedListener;
    private AdapterView.OnItemSelectedListener spEndItemSelectedListener;
    private View.OnClickListener removeTripListener;
    private List<List<LocInfo>> subLocLists = new ArrayList<List<LocInfo>>();
    private MultiButtonsPopupDialog gMultiButtonsPopupDialog;
    private TextView tvContactHint,tvRoundTripHint;

    class ViewHolder
    {
        int index;
        Spinner spStart;
        Spinner spEnd;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("VehicleFound", TollRoadsApp.getInstance().gVehicleFound);
        outState.putSerializable("OTTUserInfoRequest", TollRoadsApp.getInstance().gOTTUserInfoRequest);
        outState.putSerializable("OttTrips", (Serializable) ottTrips);
        outState.putBoolean("ShowOTTCaching", TollRoadsApp.getInstance().gShowOTTCaching);
    }

    private void reAddTrips()
    {
        for(int i=0; i < ottTrips.size(); i++)
        {
            OttTrip ottTrip = ottTrips.get(i);
            if(!ottTrip.getTrip_date().isEmpty() && ottTrip.getFrom_loc_id() != -1 &&
                    ottTrip.getTo_loc_id() != -1)
            {
                ottTrip.setTrip_num(0);
                addTripReq(ottTrip);
            }
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        if(intent != null)
        {
            boolean needReAddTrips = intent.getBooleanExtra("re-add-trips",false);
            ottTrips = TollRoadsApp.getInstance().ottTrips;

            if(TollRoadsApp.getInstance().gVehicleFound != Constants.VEHICLE_FOUND_TYPE_RENTAL) {
                if(needReAddTrips)
                {
                    reAddTrips();
                }
            }
        }
        super.onNewIntent(intent);
    }

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit OTT_2_Contact page.");
        super.onDestroy();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ott_trip);
        FlurryAgent.logEvent("Enter OTT_2_Contact page.");

        if(savedInstanceState != null)
        {
            TollRoadsApp.getInstance().gOTTUserInfoRequest = (OTTUserInfoRequest) savedInstanceState.getSerializable("OTTUserInfoRequest");
            TollRoadsApp.getInstance().gVehicleFound = savedInstanceState.getInt("VehicleFound");
            TollRoadsApp.getInstance().ottTrips= (List<OttTrip>) savedInstanceState.getSerializable("OttTrips");
            TollRoadsApp.getInstance().gShowOTTCaching = savedInstanceState.getBoolean("ShowOTTCaching");
        }
        ottTrips = TollRoadsApp.getInstance().ottTrips;
        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvGoNext = (TextView) findViewById(R.id.tv_go_next);

        tvTotalCharge = (TextView)findViewById(R.id.tv_total_charge);
        llTripRental = (LinearLayout)findViewById(R.id.ll_trip_rental);
        llTrip = (LinearLayout)findViewById(R.id.ll_trip);

        layoutDatePicker = (RelativeLayout)findViewById(R.id.layout_datePicker);
        datePicker = (DatePicker)findViewById(R.id.datePicker);
        tvDatePickerConfirm = (TextView)findViewById(R.id.tv_datePicker_ok);
        tvDatePickerCancel = (TextView)findViewById(R.id.tv_datePicker_cancel);
        tvStartDate = (TextView)findViewById(R.id.tv_start_date);
        tvEndDate = (TextView)findViewById(R.id.tv_end_date);

        lvTrip = (ListView)findViewById(R.id.lv_trip);
        llAddAnotherTrip = (LinearLayout)findViewById(R.id.ll_add_another_trip);

        tvContactHint = (TextView)findViewById(R.id.tv_contact_info_hint);
        tvRoundTripHint = (TextView)findViewById(R.id.tv_round_trip_hint);

        if(ottTrips.size() == 0) {
            OttTrip ottTrip = new OttTrip();
            ottTrips.add(ottTrip);
        }
        else
        {
            // not rental car
            if(TollRoadsApp.getInstance().gVehicleFound != Constants.VEHICLE_FOUND_TYPE_RENTAL) {
                reAddTrips();
            }
        }
        //7 days before and 7 days after
        //datePicker.setMinDate(System.currentTimeMillis() - Resource.ONE_DAY*7L);
        //datePicker.setMaxDate(System.currentTimeMillis() + Resource.ONE_DAY*7L);
        setupListener();

        if(savedInstanceState == null)
        {
            if(TollRoadsApp.getInstance().gShowOTTCaching) {
                initWithCaching();
            }
            Calendar c = Calendar.getInstance();

            String today = TollRoadsApp.formatDateBySystem(c.getTimeInMillis());
            String dateStr = c.get(Calendar.YEAR) + "-" + String.format("%02d", c.get(Calendar.MONTH) + 1)
                    + "-" + String.format("%02d", c.get(Calendar.DAY_OF_MONTH));

            tvStartDate.setTag(dateStr);
            tvStartDate.setText(today);

            Intent intent = getIntent();
            if(intent != null)
            {
                double amountDue = intent.getDoubleExtra(Resource.KEY_AMOUNT_DUE, 0.0);
                if(amountDue > 0.0) {
                    showTotalAmount(amountDue);
                }
            }

        }
    }

    private void initWithCaching()
    {
        OTTUserInfoRequest ottUserInfoRequest = TollRoadsApp.getInstance().gCachingOTTRequest;

    }

    private void generateRoadList()
    {
        roadList.clear();
        if(locList.size() > 0)
        {
            for(int i =0; i<locList.size(); i++)
            {
                LocInfo locInfo = locList.get(i);
                boolean match = false;
                int tollRoad = locInfo.getToll_road();

                for(int j=0; j<roadList.size(); j++)
                {
                    if(roadList.get(j).getToll_road() == tollRoad)
                    {
                        match = true;
                        break;
                    }
                }
                if(!match) {
                    RoadInfo roadInfo = new RoadInfo();
                    roadInfo.setRoad_name(locInfo.getRoad_name());
                    roadInfo.setToll_road(locInfo.getToll_road());

                    roadList.add(roadInfo);
                }
            }
            sortRoadList(roadList);

           // subLocLists
            for(int j=0; j<roadList.size(); j++)
            {
                RoadInfo roadInfo = roadList.get(j);
                int tollRoad = roadInfo.getToll_road();
                List<LocInfo> subLocList = new ArrayList<LocInfo>();
                for(int i =0; i<locList.size(); i++)
                {
                    if(locList.get(i).getToll_road() == tollRoad)
                    {
                        subLocList.add(locList.get(i));
                    }
                }
                sortLocList(subLocList);
                subLocLists.add(subLocList);
            }
        }
    }

    class TripListAdapter extends SimpleAdapter {
        Context mContext;
        private LayoutInflater l_Inflater;


        public TripListAdapter(Context context,
                                    List<? extends Map<String, ?>> data, int resource,
                                    String[] from, int[] to) {
            super(context, data, resource, from, to);

            mContext = context;
            l_Inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return ottTrips.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = l_Inflater.inflate(
                        R.layout.item_ott_trip, null);
                viewHolder = new ViewHolder();
                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder)convertView.getTag();
            }
//            View mView = super.getView(position, convertView, parent);
//            viewHolder = (ViewHolder)mView.getTag();
//            if(viewHolder == null)
//            {
//                viewHolder = new ViewHolder();
//            }
            TextView tvTripIndex = (TextView) convertView.findViewById(R.id.tv_trip_index);
            TextView tvTripDate = (TextView) convertView.findViewById(R.id.tv_trip_date);
            ImageView ivRemove = (ImageView) convertView.findViewById(R.id.iv_remove);
            Spinner spRoad = (Spinner) convertView.findViewById(R.id.sp_road);
            Spinner spStart = (Spinner) convertView.findViewById(R.id.sp_start);
            Spinner spEnd = (Spinner) convertView.findViewById(R.id.sp_end);
            TextView tvTripCost = (TextView) convertView.findViewById(R.id.tv_trip_cost);
            OttTrip ottTrip = ottTrips.get(position);

            ivRemove.setTag(position);
            if(ottTrips.size() <= 1)
            {
                ivRemove.setVisibility(View.GONE);
            }
            else
            {
                ivRemove.setVisibility(View.VISIBLE);
            }

            ivRemove.setOnClickListener(removeTripListener);

            viewHolder.index = position;
            viewHolder.spStart = spStart;
            viewHolder.spEnd = spEnd;

            tvTripIndex.setText(getString(R.string.trip_index, position + 1));

            tvTripDate.setText(TollRoadsApp.formatDateBySystemFromServer(ottTrip.getTrip_date()));
            tvTripDate.setTag(position);
            tvTripDate.setOnClickListener(tripDateOnClickListener);

            populateRoadSpinner(spRoad);
            spRoad.setTag(viewHolder);
            spRoad.setOnItemSelectedListener(null);
            int selectedRoadIndex = getRoadIndex(ottTrip.getToll_road());
            spRoad.setSelection(selectedRoadIndex);
            spRoad.setOnItemSelectedListener(spRoadItemSelectedListener);

            if(!roadList.isEmpty()) {
                RoadInfo roadInfo = roadList.get(selectedRoadIndex);
                ottTrip.setToll_road(roadInfo.getToll_road());
                ottTrip.setRoad(roadInfo.getRoad_name());
                ottTrip.setRoadIndex(selectedRoadIndex);
            }

            //init spStart and spEnd
            List<LocInfo> subLocList = subLocLists.get(selectedRoadIndex);//getSubLocList(roadInfo.getToll_road());
            int fromIndex = ottTrip.getFromIndex();
            int toIndex = ottTrip.getToIndex();
            spStart.setTag(position);
            spEnd.setTag(position);
            populateEntryLocSpinner(spStart, subLocList, position);
            populateExitLocSpinner(spEnd, subLocList, position);
            spStart.setSelection(fromIndex);
            spEnd.setSelection(toIndex);

            spStart.setOnItemSelectedListener(spStartItemSelectedListener);
            spEnd.setOnItemSelectedListener(spEndItemSelectedListener);

            if(Math.abs(ottTrip.getTrip_amount()) >= 0.000001)
            {
                tvTripCost.setText(String.valueOf(ottTrip.getTrip_amount()));
            }
            else
            {
                tvTripCost.setText("");
            }
            // super.getView(position, convertView, parent);
            return convertView;//mView;//
        }
    }

    private List<LocInfo> getSubLocList(int tollRoad)
    {
        List<LocInfo> subLocList = new ArrayList<LocInfo>();
        for(int i=0; i< locList.size(); i++)
        {
            LocInfo locInfo = locList.get(i);
            if(locInfo.getToll_road() == tollRoad)
            {
                subLocList.add(locInfo);
            }
        }
        sortLocList(subLocList);
        return subLocList;
    }

    private int getRoadIndex(int tollRoad)
    {
        int index = 0;
        for(int i=0; i < roadList.size(); i++)
        {
            if(roadList.get(i).getToll_road() == tollRoad)
            {
                index = i;
                break;
            }
        }
        return index;
    }

    public class EntryAdapter extends ArrayAdapter<String>{
        int entryTripIndex;
        List<LocInfo> locList;

        public EntryAdapter(Context context, int textViewResourceId,
                            List<String> objects, int index,List<LocInfo> subLocList) {
            super(context, textViewResourceId, objects);
            entryTripIndex = index;
            locList = subLocList;
        }

        @Override
        public View getDropDownView(int position, View cnvtView, ViewGroup prnt){
            View mView = super.getView(position, cnvtView, prnt);

            TextView myText= (TextView)mView.findViewById(android.R.id.text1);

            ViewGroup.LayoutParams params = mView.getLayoutParams();
            OttTrip ottTrip = ottTrips.get(entryTripIndex);
            int fromLocId = ottTrip.getFrom_loc_id();

            if(position == 0)
            {
                if (fromLocId == -1) {
                    myText.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    myText.setTextColor(Color.BLACK);
                }

                params.height = Convert.dpToPx(36);
            }
            else {
                LocInfo locInfo = locList.get(position - 1);

                int toLocId = ottTrip.getTo_loc_id();
                int locId = locInfo.getLoc_id();

                if (locId == fromLocId) {
                    myText.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    myText.setTextColor(Color.BLACK);
                }

                if (locId == toLocId && locId != Constants.UNKNOWN_LOC_ID) {
                    params.height = 1;
                } else {
                    params.height = Convert.dpToPx(36);
                }
                myText.setTag(locInfo);
            }
            mView.setLayoutParams(params);

            return mView;

        }
    }

    public class ExitAdapter extends ArrayAdapter<String>{
        int entryTripIndex;
        List<LocInfo> locList;

        public ExitAdapter(Context context, int textViewResourceId,
                            List<String> objects, int index,List<LocInfo> subLocList) {
            super(context, textViewResourceId, objects);
            entryTripIndex = index;
            locList = subLocList;
        }

        @Override
        public View getDropDownView(int position, View cnvtView, ViewGroup prnt){
            View mView = super.getView(position, cnvtView, prnt);

            TextView myText= (TextView)mView.findViewById(android.R.id.text1);

            ViewGroup.LayoutParams params = mView.getLayoutParams();
            OttTrip ottTrip = ottTrips.get(entryTripIndex);
            int toLocId = ottTrip.getTo_loc_id();
            if(position == 0)
            {
                if (toLocId == -1) {
                    myText.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    myText.setTextColor(Color.BLACK);
                }
                params.height = Convert.dpToPx(36);
            }
            else {

                LocInfo locInfo = locList.get(position - 1);

                int fromLocId = ottTrip.getFrom_loc_id();

                int locId = locInfo.getLoc_id();

                if (locId == toLocId) {
                    myText.setTextColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    myText.setTextColor(Color.BLACK);
                }

                if (locId == fromLocId && locId != Constants.UNKNOWN_LOC_ID) {
                    params.height = 1;
                } else {
                    params.height = Convert.dpToPx(36);
                }
                myText.setTag(locInfo);
            }
            mView.setLayoutParams(params);

            return mView;

        }
    }

    private void populateEntryLocSpinner(Spinner spLoc, List<LocInfo> subLocList,int position)
    {
        if(spLoc != null && subLocList!= null) {
            List<String> categories = new ArrayList<String>();
            categories.add(getString(R.string.select_point));

            for (int i = 0; i < subLocList.size(); i++) {
                LocInfo locInfo = subLocList.get(i);

                categories.add(locInfo.getLoc_name());
            }

            // Creating adapter for spinner
            ArrayAdapter<String> dataAdapter = new EntryAdapter(this,
                    android.R.layout.simple_spinner_item, categories,position,subLocList);

            // Drop down layout style - list view with radio button
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // attaching data adapter to spinner
            spLoc.setAdapter(dataAdapter);
        }
    }
    private void populateExitLocSpinner(Spinner spLoc, List<LocInfo> subLocList,int position)
    {
        if(spLoc != null && subLocList!= null) {
            List<String> categories = new ArrayList<String>();
            categories.add(getString(R.string.select_point));

            for (int i = 0; i < subLocList.size(); i++) {
                LocInfo locInfo = subLocList.get(i);

                categories.add(locInfo.getLoc_name());
            }

            // Creating adapter for spinner
            ArrayAdapter<String> dataAdapter = new ExitAdapter(this,
                    android.R.layout.simple_spinner_item, categories,position,subLocList);

            // Drop down layout style - list view with radio button
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // attaching data adapter to spinner
            spLoc.setAdapter(dataAdapter);
        }
    }
    private void populateRoadSpinner(Spinner spRoad)
    {
        if(spRoad != null) {
            List<String> categories = new ArrayList<String>();

            for (int i = 0; i < roadList.size(); i++) {
                RoadInfo roadInfo = roadList.get(i);

                categories.add(roadInfo.getRoad_name());
            }

            // Creating adapter for spinner
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

            // Drop down layout style - list view with radio button
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // attaching data adapter to spinner
            spRoad.setAdapter(dataAdapter);
        }
    }

    private TripListAdapter getRecentTollMenuAdapter() {
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

        for (int i = 0; i < ottTrips.size(); i++) {
            OttTrip ottTrip = ottTrips.get(i);

            HashMap<String, Object> map = new HashMap<String, Object>();

            map.put("tv_trip_index", getString(R.string.trip_index, i+1));
            map.put("tv_trip_date", ottTrip.getTrip_date());

            data.add(map);
        }

        TripListAdapter tripListAdapter = new TripListAdapter(
                this, data,
                R.layout.item_ott_trip,
                new String[] { "tv_trip_index", "tv_trip_date"}, new int[] { R.id.tv_trip_index,
                R.id.tv_trip_date});

        return tripListAdapter;
    }

    private void loadTripMenu()
    {
        if(tripListAdapter == null) {
            tripListAdapter = getRecentTollMenuAdapter();
            lvTrip.setAdapter(tripListAdapter);
        }
        else
        {
            tripListAdapter.notifyDataSetChanged();
        }
        setTotalHeightToListView(lvTrip);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            gotoActivity(getApplicationContext(), OTTCalculateTollActivity.class, //OTTVehicleInfoActivity.class,
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            return  true;
        }
        return super.onKeyDown(keyCode, event);
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
                gotoActivity(v.getContext(), OTTCalculateTollActivity.class, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
        });

        tripDateOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tripDateSelected = true;
                int position = (Integer)v.getTag();
                tripDateIndex = position;
                selectedTripDate = (TextView)v;

                String dateStr = ottTrips.get(position).getTrip_date();
                if(dateStr.isEmpty())
                {
                    Calendar c = Calendar.getInstance();

                    // set current date into datepicker
                    datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), null);
                }
                else
                {
                    String[] dateArray = dateStr.split("-");
                    int year = Integer.valueOf(dateArray[0]);
                    int month = Integer.valueOf(dateArray[1])-1;
                    int day = Integer.valueOf(dateArray[2]);
                    datePicker.init(year, month, day, null);
                }
                layoutDatePicker.setVisibility(View.VISIBLE);
            }
        };

        tvStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tripDateSelected = false;
                selectStartDate = true;

                String dateStr = (String)tvStartDate.getTag();
                if(dateStr == null || dateStr.isEmpty()){
                    Calendar c = Calendar.getInstance();

                    // set current date into datepicker
                    datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), null);
                } else {
                    String[] dateArray = dateStr.split("-");
                    int year = Integer.valueOf(dateArray[0]);
                    int month = Integer.valueOf(dateArray[1]) - 1;
                    int day = Integer.valueOf(dateArray[2]);
                    datePicker.init(year, month, day, null);
                }
                layoutDatePicker.setVisibility(View.VISIBLE);
            }
        });
        tvEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStartDate = false;
                tripDateSelected = false;
                String dateStr = (String)tvEndDate.getTag();
                if(dateStr == null || dateStr.isEmpty()){
                    Calendar c = Calendar.getInstance();

                    // set current date into datepicker
                    datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), null);
                } else {
                    String[] dateArray = dateStr.split("-");
                    int year = Integer.valueOf(dateArray[0]);
                    int month = Integer.valueOf(dateArray[1]) - 1;
                    int day = Integer.valueOf(dateArray[2]);
                    datePicker.init(year, month, day, null);
                }
                layoutDatePicker.setVisibility(View.VISIBLE);
            }
        });
        layoutDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutDatePicker.setVisibility(View.GONE);
            }
        });
        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                return;
            }
        });
        tvDatePickerConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dateStr = datePicker.getYear() + "-" + String.format("%02d", datePicker.getMonth() + 1) + "-" + String.format("%02d", datePicker.getDayOfMonth());
                Calendar calendar = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());

                String systemFormat = TollRoadsApp.formatDateBySystem(calendar.getTimeInMillis());

                if (tripDateSelected) {
                    if (selectedTripDate != null) {
                        int position = (Integer) selectedTripDate.getTag();
                        selectedTripDate.setText(systemFormat);

                        if (position < ottTrips.size()) {
                            OttTrip ottTrip = ottTrips.get(position);
                            ottTrip.setTrip_date(dateStr);

                            if (ottTrip.getFromIndex() != 0 && ottTrip.getToIndex() != 0) {
                                if (ottTrip.getTrip_num() != 0) {
                                    //update trip
                                    updateTripReq(ottTrip);
                                } else {
                                    //add trip
                                    addTripReq(ottTrip);
                                }
                            }
                        }
                    }
                } else {
                    if (selectStartDate) {
                        tvStartDate.setText(systemFormat);
                        tvStartDate.setTag(dateStr);
                    } else {
                        tvEndDate.setText(systemFormat);
                        tvEndDate.setTag(dateStr);
                    }
                }
                layoutDatePicker.setVisibility(View.GONE);
            }
        });
        tvDatePickerCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutDatePicker.setVisibility(View.GONE);
            }
        });
        spRoadItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ViewHolder viewHolder = (ViewHolder)parent.getTag();
                int tripIndex = viewHolder.index;
                OttTrip ottTrip = null;
                if(tripIndex < ottTrips.size()) {
                    ottTrip = ottTrips.get(tripIndex);
                }
                if(ottTrip != null && ottTrip.getRoadIndex() != position && position < roadList.size())
                {
                    Spinner spStart = viewHolder.spStart;
                    Spinner spEnd = viewHolder.spEnd;

                    RoadInfo roadInfo = roadList.get(position);

                    List<LocInfo> subLocList = subLocLists.get(position);//getSubLocList(roadInfo.getToll_road());

                    if(tripIndex < ottTrips.size()) {
                        ottTrip.setToll_road(roadInfo.getToll_road());
                        ottTrip.setRoad(roadInfo.getRoad_name());
                        ottTrip.setRoadIndex(position);
                        ottTrip.setFromIndex(0);
                        ottTrip.setFrom_loc_id(-1);
                        ottTrip.setToIndex(0);
                        ottTrip.setTo_loc_id(-1);
                        ottTrip.setTrip_amount(0.0);
                        if(ottTrip.getTrip_num() != 0)
                        {
                            //delete trip
                            delTripReq(ottTrip);
                        }
                        loadTripMenu();
                    }
                    populateEntryLocSpinner(spStart, subLocList, tripIndex);
                    populateExitLocSpinner(spEnd, subLocList, tripIndex);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        spStartItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int tripIndex = (Integer) parent.getTag();
                OttTrip ottTrip = ottTrips.get(tripIndex);
                int roadIndex = ottTrip.getRoadIndex();
                List<LocInfo> subLocList = subLocLists.get(roadIndex);

                if (position > 0 && ottTrip.getFromIndex() != position) {
                    LocInfo locInfo = subLocList.get(position-1);

                    ottTrip.setFrom_loc_id(locInfo.getLoc_id());
                    ottTrip.setFrom_loc_name(locInfo.getLoc_name());
                    if(ottTrip.getToIndex() != 0 && !ottTrip.getTrip_date().isEmpty())
                    {
                        if(ottTrip.getTrip_num() != 0)
                        {
                            //update trip
                            updateTripReq(ottTrip);
                        }
                        else
                        {
                            //add trip
                            addTripReq(ottTrip);
                        }
                    }
                    control91FreewayWarning(locInfo.getLoc_id());
                }
                else
                {
                    if(position == 0)
                    {
                        ottTrip.setFrom_loc_id(-1);
                    }
                }
                ottTrip.setFromIndex(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };

        spEndItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int tripIndex = (Integer)parent.getTag();
                OttTrip ottTrip = ottTrips.get(tripIndex);
                int roadIndex = ottTrip.getRoadIndex();
                List<LocInfo> subLocList = subLocLists.get(roadIndex);

                if(position > 0 && ottTrip.getToIndex() != position)
                {
                    LocInfo locInfo = subLocList.get(position-1);
                    ottTrip.setTo_loc_id(locInfo.getLoc_id());
                    ottTrip.setTo_loc_name(locInfo.getLoc_name());
                    if(ottTrip.getFromIndex() != 0 && !ottTrip.getTrip_date().isEmpty())
                    {
                        if(ottTrip.getTrip_num() != 0)
                        {
                            //update trip
                            updateTripReq(ottTrip);
                        }
                        else
                        {
                            //add trip
                            addTripReq(ottTrip);
                        }
                    }

                    control91FreewayWarning(locInfo.getLoc_id());
                }
                else
                {
                    if(position == 0)
                    {
                        ottTrip.setTo_loc_id(-1);
                    }
                }
                ottTrip.setToIndex(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        llAddAnotherTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OttTrip ottTrip = new OttTrip();
                ottTrips.add(ottTrip);
                loadTripMenu();
            }
        });
        removeTripListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (Integer)v.getTag();
                if(position < ottTrips.size()) {
                    OttTrip ottTrip = ottTrips.get(position);
                    if (ottTrip.getTrip_num() != 0) {
                        delTripReq(ottTrip);
                    }
                    ottTrips.remove(position);
                    loadTripMenu();
                }
            }
        };

        tvGoNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation()) {
                    showProgressDialog();
                    if(checkTrips()) {
                        //String params = populateUserInfoParams();
                        //OTTUserInfoRequest(Resource.URL_PAY_GO, params);

                        if(TollRoadsApp.getInstance().gVehicleFound == Constants.VEHICLE_FOUND_TYPE_RENTAL) {
                            String addRentalParams = populateAddRentalParams();

                            addRentalDatesReq(Resource.URL_PAY_GO, addRentalParams);
                        }
                        else
                        {
                            closeProgressDialog();
                            gotoActivity(v.getContext(), OTTContactInfoActivity.class, //OTTPaymentInfoActivity.class,
                                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        }

                    }
                }
//                else
//                {
//                    if(etEmail.getText().length() == 0)
//                    {
//                        showUpConfirmDialog();
//                    }
//                }
                //populateAddRentalParams();
                //gotoActivity(v.getContext(), OTTPaymentInfoActivity.class, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
        });
    }

    private void showUp91FreewayWarning()
    {
        showDialog(getString(R.string.attention), getString(R.string.freeway_warning),
                getString(R.string.ok), null,
                getString(R.string.sign_up_for_fastrak), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        gotoActivity(getApplicationContext(), SignUpAccountInfoActivity.class,
                                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    }
                }, false);
    }

    private void control91FreewayWarning(int locId)
    {
        if(locId == 110)
        {
            showUp91FreewayWarning();
        }
    }

    private boolean checkTrips()
    {
        boolean ret = true;
        // not rental car
        if(TollRoadsApp.getInstance().gVehicleFound != Constants.VEHICLE_FOUND_TYPE_RENTAL) {
            if(ottTrips != null && !ottTrips.isEmpty()) {
                for (int i = 0; i < ottTrips.size(); i++) {
                    OttTrip ottTrip = ottTrips.get(i);
                    if(ottTrip.isError()) {
                        if (!ottTrip.getTrip_date().isEmpty() && ottTrip.getFrom_loc_id() != -1 &&
                                ottTrip.getTo_loc_id() != -1) {
                            TollRoadsApp.getInstance().cancelPendingRequests(Constants.tag_add_trip);

                            if (ottTrip.getTrip_num() == 0) {
                                addTripReqEx(ottTrip);
                            } else {
                                updateTripReqEx(ottTrip);
                            }
                            ret = false;
                            break;
                        }
                    }
                }
            }
        }

        return ret;
    }

    private void showUpConfirmDialog()
    {
        gMultiButtonsPopupDialog = new MultiButtonsPopupDialog(this,R.style.CustomProgressDialog);
        gMultiButtonsPopupDialog.createDialog(this);
        gMultiButtonsPopupDialog.CutomizeTitle(View.GONE, "");

        gMultiButtonsPopupDialog.CutomizeContent(View.VISIBLE, getString(R.string.ott_missing_email_hint));
        gMultiButtonsPopupDialog.CutomizeExtraButton(View.GONE, 0, "", 0, null);
        gMultiButtonsPopupDialog.CutomizePositiveButton(View.VISIBLE, 0, getString(R.string.ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                String params = populateUserInfoParams();
                OTTUserInfoRequest(Resource.URL_PAY_GO, params);
            }
        });
        gMultiButtonsPopupDialog.CutomizeNeutralButton(View.GONE, 0, "", null);
        gMultiButtonsPopupDialog.CutomizeNegativeButton(View.VISIBLE, 0, getString(R.string.cancel), null);

        gMultiButtonsPopupDialog.show();
    }

    private String populateRequestParams()
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
    
    private String populateUserInfoParams()
    {
        String params = populateRequestParams();
        
        return params;
    }

    private String populateAddRentalParams()
    {
        String startDate = (String)tvStartDate.getTag();
        if(startDate == null)
        {
            startDate = "";
        }

        String endDate = (String)tvEndDate.getTag();
        if(endDate == null)
        {
            endDate = "";
        }

        String params = "rental_start_date="+ startDate;
        params = params + "&rental_end_date="+ endDate;

        TollRoadsApp.getInstance().gOTTUserInfoRequest.setRental_start_date(startDate);
        TollRoadsApp.getInstance().gOTTUserInfoRequest.setRental_end_date(endDate);
        return params;
    }

    private long getTimeStamp(String dateString)
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

    private long getTodayTimeStamp()
    {
        GregorianCalendar curCal = new GregorianCalendar();
        Calendar c = Calendar.getInstance();
        curCal.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        return curCal.getTimeInMillis();
    }

    private boolean checkRentalValidation()
    {
        boolean ret = true;
        if(tvStartDate.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.start_date_empty_warning));
        }
        else if(tvEndDate.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.end_date_empty_warning));
        }

        return ret;
    }

    private boolean checkTripValidation()
    {
        boolean ret = true;

        for(int i= 0; i< ottTrips.size(); i++)
        {
            OttTrip ottTrip = ottTrips.get(i);
            if(ottTrip.getTrip_date().isEmpty())
            {
                ret = false;
                showToastMessage(getString(R.string.trip_date_empty_warning));
                break;
            }
            else if(ottTrip.getFrom_loc_id() == -1)
            {
                ret = false;
                showToastMessage(getString(R.string.start_point_empty_warning));
                break;
            }
            else if(ottTrip.getTo_loc_id() == -1)
            {
                ret = false;
                showToastMessage(getString(R.string.end_point_empty_warning));
                break;
            }
        }
        return ret;
    }

    private boolean checkValidation()
    {
        boolean ret = true;

        if(TollRoadsApp.getInstance().gVehicleFound == Constants.VEHICLE_FOUND_TYPE_RENTAL)
        {
            if(!checkRentalValidation())
            {
                ret = false;
            }
        }
        else {
            if(!checkTripValidation()) {
                ret = false;
            }
        }

        return ret;
    }

    private void OTTUserInfoRequest(String url,String params)
    {
        Response.Listener listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, "response:" + response);

                        if(checkResponse(response)) {
                            if(TollRoadsApp.getInstance().gVehicleFound == Constants.VEHICLE_FOUND_TYPE_RENTAL) {
                                String params = populateAddRentalParams();

                                addRentalDatesReq(Resource.URL_PAY_GO, params);
                            }
                            else
                            {
                                closeProgressDialog();
                                gotoActivity(OTTTripActivity.this, OTTPaymentInfoActivity.class, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
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

        ServerDelegate.OTTUserInfoRequest(url, params, listener, errorListener);
    }

    private void addRentalDatesReq(String url,String params)
    {
        Response.Listener listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                closeProgressDialog();
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, "response:" + response);

                        if(checkResponse(response)) {
                            gotoActivity(OTTTripActivity.this, OTTPaymentInfoActivity.class, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
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

        ServerDelegate.addRentalDatesReq(url, params, listener, errorListener);
    }

    private void initWidgetValue()
    {
        if(TollRoadsApp.getInstance().gVehicleFound == Constants.VEHICLE_FOUND_TYPE_RENTAL)
        {
            tvTotalCharge.setVisibility(View.GONE);
            llTripRental.setVisibility(View.VISIBLE);
            llTrip.setVisibility(View.GONE);

            tvContactHint.setText(getString(R.string.rental_car_title));
            tvRoundTripHint.setVisibility(View.GONE);
        }
        else {
            llTrip.setVisibility(View.VISIBLE);
            llTripRental.setVisibility(View.GONE);

            tvContactHint.setText(getString(R.string.ott_contact_info_hint1));
            tvRoundTripHint.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initWidgetValue();
        if(roadList.isEmpty()) {
            getLocList();
        }
    }

    @Override
    public void onPause() {

        super.onPause();
    }
    private void sortLocList(List<LocInfo> mLocList)
    {
        Comparator<LocInfo> comp = new Comparator<LocInfo>() {
            public int compare(LocInfo p1, LocInfo p2) {
                try {
                    if ((p1 == null) || (p2 == null)) {
                        return 0;
                    }
                    else
                    {
                        return p1.getLoc_id() - p2.getLoc_id();
                    }
                }
                catch (Exception exception)
                {
                    return 0;
                }
            }

        };
        Collections.sort(mLocList, comp);
    }

    private DialogInterface.OnClickListener exitListener;
    private void getLocList()
    {
        showProgressDialog();
        if(exitListener == null)
        {
            exitListener = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int position)
                {
                    finish();
                }
            };
        }

        Response.Listener listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                closeProgressDialog();
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, response.toString());

                        Gson gson = new GsonBuilder().serializeNulls().create();
                        CommonResponse commonResponse = gson.fromJson(response.toString(),
                                CommonResponse.class);

                        if(commonResponse.getSuccess() != 1)
                        {
                            showDialog(getString(R.string.dialog_title_error),
                                    commonResponse.getMessage(), getString(R.string.ok),
                                    exitListener, false);
                        }
                        else {
                            if(response.has(Resource.KEY_INFO))
                            {
                                String info = response.optString(Resource.KEY_INFO);
                                Type listType = new TypeToken<ArrayList<LocInfo>>() {}.getType();

                                locList = gson.fromJson(info, listType);
                                generateRoadList();
                                loadTripMenu();
                            }
                        }
                    }
                    else
                    {
                        showDialog(getString(R.string.dialog_title_error),
                                getString(R.string.network_error_retry), getString(R.string.ok),
                                exitListener, false);
                    }
                }
                catch (Exception e)
                {
                    showDialog(getString(R.string.dialog_title_error),
                            getString(R.string.network_error_retry), getString(R.string.ok),
                            exitListener, false);
                }

            }
        };
        Response.ErrorListener locListErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                closeProgressDialog();

                if(error != null) {
                    Log.d(LOG_TAG, "Error: " + error.getMessage());
                    if(error.networkResponse != null) {
                        try {
                            String responseBody = new String(error.networkResponse.data, "utf-8");

                            showDialog(getString(R.string.dialog_title_error),
                                    responseBody, getString(R.string.ok),
                                    exitListener, false);

                        }catch (UnsupportedEncodingException Exceptionerror) {

                        }
                    }
                    else
                    {
                        showDialog(getString(R.string.dialog_title_error),
                                getString(R.string.network_error_retry), getString(R.string.ok),
                                exitListener, false);
                    }
                }
            }
        };
        ServerDelegate.getLocList(Resource.URL_PAY_GO, listener, locListErrorListener);
    }

    private void showTotalAmount(double amount)
    {
        if(tvTotalCharge.getVisibility() != View.VISIBLE)
        {
            tvTotalCharge.setVisibility(View.VISIBLE);
        }
        tvTotalCharge.setText(getString(R.string.total_charge, amount));

        if(TollRoadsApp.getInstance().gOTTUserInfoRequest != null)
        {
            TollRoadsApp.getInstance().gOTTUserInfoRequest.setAmount(amount);
            TollRoadsApp.getInstance().gOTTUserInfoRequest.setTotal_amount(amount);
            TollRoadsApp.getInstance().gOTTUserInfoRequest.setTotalAmount(amount);
        }
    }

    private boolean isSameDate(String tripInfoDate, String ottTripDate)
    {
        boolean ret = false;
        if(tripInfoDate.equals(ottTripDate))
        {
            ret = true;
        }
        else
        {
            String[] dateSet1 = tripInfoDate.split("/");
            String[] dateSet2 = ottTripDate.split("-");

            if(dateSet1.length == dateSet2.length && dateSet1.length == 3)
            {
                if(dateSet1[2].equals(dateSet2[0]) && dateSet1[0].equals(dateSet2[1])
                        && dateSet1[1].equals(dateSet2[2]))
                {
                    ret = true;
                }
            }
        }
        return ret;
    }

    private boolean isSameTrip(TripInfo tripInfo, OttTrip ottTrip)
    {
        boolean ret = false;
        if(isSameDate(tripInfo.getTrip_date(),ottTrip.getTrip_date())
                && tripInfo.getFrom_loc_id() == ottTrip.getFrom_loc_id()
                && tripInfo.getTo_loc_id() == ottTrip.getTo_loc_id())
        {
            ret = true;
        }
        return ret;
    }

    private void addTripReq(final OttTrip ottTrip)
    {
        String params = "dir_id=3";

        params = params + "&from_loc_id=" + ottTrip.getFrom_loc_id();
        params = params + "&to_loc_id=" + ottTrip.getTo_loc_id();
        params = params + "&toll_road=" + ottTrip.getToll_road();
        params = params + "&start_date=" + ottTrip.getTrip_date();
        showProgressDialog();
        Response.Listener listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                closeProgressDialog();
                ottTrip.setError(true);
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, "response:" + response);
                        if(checkResponse(response)) {
                            JSONObject jsonObject = new JSONObject(response);
                            if(jsonObject.has(Resource.KEY_AMOUNT_DUE))
                            {
                                TollRoadsApp.getInstance().ottTotalAmount = jsonObject.optDouble(Resource.KEY_AMOUNT_DUE,0.0);
                                showTotalAmount(TollRoadsApp.getInstance().ottTotalAmount);
                            }
//                            if(jsonObject.has(Resource.KEY_INFO))
//                            {
//                                String info = jsonObject.optString(Resource.KEY_INFO);
//                                Gson gson = new GsonBuilder().serializeNulls().create();
//                                Type listType = new TypeToken<ArrayList<TripInfo>>() {}.getType();
//
//                                List<TripInfo> tripInfos = gson.fromJson(info, listType);
//                                for(int i =tripInfos.size()-1; i>=0 ; i--)
//                                {
//                                    TripInfo tripInfo = tripInfos.get(i);
//                                    if(isSameTrip(tripInfo,ottTrip))
//                                    {
//                                        ottTrip.setTrip_num(tripInfo.getTrip_num());
//                                        ottTrip.setTrip_amount(tripInfo.getTrip_amount());
//                                        break;
//                                    }
//                                }
//                            }
                            ottTrip.setTrip_num(jsonObject.optInt(Resource.KEY_TRIP_NUM));
                            ottTrip.setTrip_amount(jsonObject.optDouble(Resource.KEY_TRIP_AMOUNT, 0.0));
                            ottTrip.setError(false);
                            loadTripMenu();
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

        ServerDelegate.addTripReq(Resource.URL_PAY_GO, params, listener, errorListener);
    }

    private void delTripReq(OttTrip ottTrip)
    {
        String params = "trip_num=" + ottTrip.getTrip_num();
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
                            JSONObject jsonObject = new JSONObject(response);
                            if(jsonObject.has(Resource.KEY_AMOUNT_DUE))
                            {
                                TollRoadsApp.getInstance().ottTotalAmount = jsonObject.optDouble(Resource.KEY_AMOUNT_DUE,0.0);
                                showTotalAmount(TollRoadsApp.getInstance().ottTotalAmount);
                            }
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

        ServerDelegate.delTripReq(Resource.URL_PAY_GO, params, listener, errorListener);
    }

    private void updateTripReq(final OttTrip ottTrip)
    {
        String params = "dir_id=3";

        params = params + "&trip_num=" + ottTrip.getTrip_num();
        params = params + "&from_loc_id=" + ottTrip.getFrom_loc_id();
        params = params + "&to_loc_id=" + ottTrip.getTo_loc_id();
        params = params + "&toll_road=" + ottTrip.getToll_road();
        params = params + "&start_date=" + ottTrip.getTrip_date();
        showProgressDialog();
        Response.Listener listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                closeProgressDialog();
                ottTrip.setError(true);
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, "response:" + response);
                        if(checkResponse(response)) {
                            JSONObject jsonObject = new JSONObject(response);
                            if(jsonObject.has(Resource.KEY_AMOUNT_DUE))
                            {
                                TollRoadsApp.getInstance().ottTotalAmount = jsonObject.optDouble(Resource.KEY_AMOUNT_DUE,0.0);
                                showTotalAmount(TollRoadsApp.getInstance().ottTotalAmount);
                            }
                            if(jsonObject.has(Resource.KEY_INFO))
                            {
                                String info = jsonObject.optString(Resource.KEY_INFO);
                                Gson gson = new GsonBuilder().serializeNulls().create();
                                Type listType = new TypeToken<ArrayList<TripInfo>>() {}.getType();

                                List<TripInfo> tripInfos = gson.fromJson(info, listType);
                                for(int i =tripInfos.size()-1; i>=0 ; i--)
                                {
                                    TripInfo tripInfo = tripInfos.get(i);
                                    if(isSameTrip(tripInfo,ottTrip))
                                    {
                                        ottTrip.setTrip_num(tripInfo.getTrip_num());
                                        ottTrip.setTrip_amount(tripInfo.getTrip_amount());
                                        break;
                                    }
                                }
                            }
                            ottTrip.setError(false);
                            loadTripMenu();
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

        ServerDelegate.updateTripReq(Resource.URL_PAY_GO, params, listener, errorListener);
    }

    private void getTripList()
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

        ServerDelegate.getTripList(Resource.URL_PAY_GO, listener, errorListener);
    }

    private void addTripReqEx(final OttTrip ottTrip)
    {
        String params = "dir_id=3";

        params = params + "&from_loc_id=" + ottTrip.getFrom_loc_id();
        params = params + "&to_loc_id=" + ottTrip.getTo_loc_id();
        params = params + "&toll_road=" + ottTrip.getToll_road();
        params = params + "&start_date=" + ottTrip.getTrip_date();

        Response.Listener listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                ottTrip.setError(true);
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, "response:" + response);
                        if(checkResponse(response)) {
                            JSONObject jsonObject = new JSONObject(response);
                            if(jsonObject.has(Resource.KEY_AMOUNT_DUE))
                            {
                                TollRoadsApp.getInstance().ottTotalAmount = jsonObject.optDouble(Resource.KEY_AMOUNT_DUE,0.0);
                                showTotalAmount(TollRoadsApp.getInstance().ottTotalAmount);
                            }
                            ottTrip.setTrip_num(jsonObject.optInt(Resource.KEY_TRIP_NUM));
                            ottTrip.setTrip_amount(jsonObject.optDouble(Resource.KEY_TRIP_AMOUNT, 0.0));
                            ottTrip.setError(false);
                            if(checkTrips()) {
                                String params = populateUserInfoParams();

                                loadTripMenu();
                                OTTUserInfoRequest(Resource.URL_PAY_GO, params);
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

        ServerDelegate.addTripReq(Resource.URL_PAY_GO, params, listener, errorListener);
    }

    private void updateTripReqEx(final OttTrip ottTrip)
    {
        String params = "dir_id=3";

        params = params + "&trip_num=" + ottTrip.getTrip_num();
        params = params + "&from_loc_id=" + ottTrip.getFrom_loc_id();
        params = params + "&to_loc_id=" + ottTrip.getTo_loc_id();
        params = params + "&toll_road=" + ottTrip.getToll_road();
        params = params + "&start_date=" + ottTrip.getTrip_date();

        Response.Listener listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                ottTrip.setError(true);
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, "response:" + response);
                        if(checkResponse(response)) {
                            JSONObject jsonObject = new JSONObject(response);
                            if(jsonObject.has(Resource.KEY_AMOUNT_DUE))
                            {
                                TollRoadsApp.getInstance().ottTotalAmount = jsonObject.optDouble(Resource.KEY_AMOUNT_DUE,0.0);
                                showTotalAmount(TollRoadsApp.getInstance().ottTotalAmount);
                            }
                            if(jsonObject.has(Resource.KEY_INFO))
                            {
                                String info = jsonObject.optString(Resource.KEY_INFO);
                                Gson gson = new GsonBuilder().serializeNulls().create();
                                Type listType = new TypeToken<ArrayList<TripInfo>>() {}.getType();

                                List<TripInfo> tripInfos = gson.fromJson(info, listType);
                                for(int i =tripInfos.size()-1; i>=0 ; i--)
                                {
                                    TripInfo tripInfo = tripInfos.get(i);
                                    if(isSameTrip(tripInfo,ottTrip))
                                    {
                                        ottTrip.setTrip_num(tripInfo.getTrip_num());
                                        ottTrip.setTrip_amount(tripInfo.getTrip_amount());
                                        break;
                                    }
                                }
                            }
                            ottTrip.setError(false);
                            if(checkTrips()) {
                                String params = populateUserInfoParams();

                                loadTripMenu();
                                OTTUserInfoRequest(Resource.URL_PAY_GO, params);
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

        ServerDelegate.updateTripReq(Resource.URL_PAY_GO, params, listener, errorListener);
    }

    private void sortRoadList(List<RoadInfo> roadList)
    {
        Comparator<RoadInfo> comp = new Comparator<RoadInfo>() {
            public int compare(RoadInfo p1, RoadInfo p2) {
                try {
                    if ((p1 == null) || (p2 == null)) {
                        return 0;
                    }
                    else
                    {
                        return p1.getToll_road() - p2.getToll_road();
                    }
                }
                catch (Exception exception)
                {
                    return 0;
                }
            }

        };
        Collections.sort(roadList, comp);
    }
}
