package com.seta.tollroaddroid.app;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.seta.tollroaddroid.app.api.Convert;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.json.CalRatesResponse;
import com.seta.tollroaddroid.app.json.LocInfo;
import com.seta.tollroaddroid.app.json.RoadInfo;
import com.seta.tollroaddroid.app.utilities.Constants;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TollCalculatorActivity extends BaseActivity implements OnMapReadyCallback {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private TextView tvOttResult, tvEveningPeakResult, tvOffPeakResult, tvWeekendsResult;
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private boolean mapIsReady = false;
    private Response.ErrorListener errorListener;
    private Response.Listener ratesListener;

    private int routeIndex = 0;
    private int entryIndex = 0;
    private int exitIndex = 0;
    private Spinner roadSpinner, entrySpinner, exitSpinner, paymentSpinner, vehicleSpinner;
    private Marker entryMarker, exitMarker;
    private List<LocInfo> locList = new ArrayList<LocInfo>();
    private List<RoadInfo> roadList = new ArrayList<RoadInfo>();
    private List<LocInfo> subLocList = new ArrayList<LocInfo>();
    private List<List<LocInfo>> subLocLists = new ArrayList<List<LocInfo>>();
    private LinearLayout llOttResult,llAccountResult;
    private ImageView ivTCAStart, ivTCAEnd;

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit Calculator_OTT page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toll_caculator);
        FlurryAgent.logEvent("Enter Calculator_OTT page.");

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvOttResult = (TextView)findViewById(R.id.tv_ott_result);
        tvEveningPeakResult = (TextView)findViewById(R.id.tv_evening_peak_result);
        tvOffPeakResult = (TextView)findViewById(R.id.tv_off_peak_result);
        tvWeekendsResult = (TextView)findViewById(R.id.tv_weekends_result);

        roadSpinner = (Spinner)findViewById(R.id.road_spinner);
        entrySpinner = (Spinner)findViewById(R.id.start_spinner);
        exitSpinner = (Spinner)findViewById(R.id.end_spinner);
        paymentSpinner = (Spinner)findViewById(R.id.pay_method_spinner);
        vehicleSpinner = (Spinner)findViewById(R.id.vehicle_type_spinner);

        llOttResult = (LinearLayout)findViewById(R.id.ll_ott_result);
        llAccountResult = (LinearLayout)findViewById(R.id.ll_account_result);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        ivTCAStart = (ImageView)findViewById(R.id.iv_tca_start);
        ivTCAEnd = (ImageView)findViewById(R.id.iv_tca_end);

        setupListener();
        mapFragment.getMapAsync(this);

        getLocList();

        try {
            MapsInitializer.initialize(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initWidgets()
    {
        if(routeIndex < subLocLists.size()) {
            subLocList = subLocLists.get(routeIndex);

            scaleMap(subLocList);
            initEntrySpinner();
            initExitSpinner();
        }
        llOttResult.setVisibility(View.GONE);
        llAccountResult.setVisibility(View.GONE);
    }

    public class EntryAdapter extends ArrayAdapter<String>{

        public EntryAdapter(Context context, int textViewResourceId,
                                   List<String> objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getDropDownView(int position, View cnvtView, ViewGroup prnt){
            View mView = super.getView(position, cnvtView, prnt);

            TextView mytext= (TextView)mView.findViewById(android.R.id.text1);

            if(position == entryIndex)
            {
                mytext.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
            else
            {
                mytext.setTextColor(Color.BLACK);
            }

            ViewGroup.LayoutParams params = mView.getLayoutParams();
            if(selectedByExit(position))
            {
                params.height = 1;
            }
            else
            {
                params.height = Convert.dpToPx(36);
            }
            mView.setLayoutParams(params);

            return mView;

        }
    }
    public class ExitAdapter extends ArrayAdapter<String>{

        public ExitAdapter(Context context, int textViewResourceId,
                            List<String> objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getDropDownView(int position, View cnvtView, ViewGroup prnt){
            View mView = super.getView(position, cnvtView, prnt);

            TextView mytext= (TextView)mView.findViewById(android.R.id.text1);

            if(position == exitIndex)
            {
                mytext.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
            else
            {
                mytext.setTextColor(Color.BLACK);
            }

            ViewGroup.LayoutParams params = mView.getLayoutParams();
            if(selectedByEntry(position))
            {
                params.height = 1;
            }
            else
            {
                params.height = Convert.dpToPx(36);
            }
            mView.setLayoutParams(params);
            return mView;
        }
    }

    private boolean isUnknown(int position)
    {
        boolean ret = false;
        if(position > 0 && position <= subLocList.size())
        {
            LocInfo locInfo = subLocList.get(position-1);

            if(locInfo.getLoc_id() == Constants.UNKNOWN_LOC_ID || locInfo.getLoc_name().equalsIgnoreCase("unknown"))
            {
                ret = true;
            }
        }
        return  ret;
    }

    private boolean selectedByEntry(int position)
    {
        boolean ret = false;
        if(position > 0 && entryIndex == position)
        {
            if(!isUnknown(position))
            {
                ret = true;
            }
        }
        return  ret;
    }

    private boolean selectedByExit(int position)
    {
        boolean ret = false;
        if(position > 0 && exitIndex == position)
        {
            if(!isUnknown(position))
            {
                ret = true;
            }
        }
        return  ret;
    }

    private void initEntrySpinner()
    {
        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add(getString(R.string.select_entry_point));
        
        for(int i =0; i< subLocList.size(); i++)
        {
            LocInfo locInfo = subLocList.get(i);

            categories.add(locInfo.getLoc_name());
        }

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new EntryAdapter(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        entrySpinner.setAdapter(dataAdapter);
    }

    private void initExitSpinner()
    {
        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add(getString(R.string.select_exit_point));

        for(int i =0; i< subLocList.size(); i++)
        {
            LocInfo locInfo = subLocList.get(i);

            categories.add(locInfo.getLoc_name());
        }

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ExitAdapter(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        exitSpinner.setAdapter(dataAdapter);
    }
    
    private void controlStartIcon()
    {
        if(entryIndex > 0) {
            String entryString = null;
            if (entryIndex <= subLocList.size()) {
                entryString = subLocList.get(entryIndex - 1).getLoc_name();
            }

            if (entryString == null || entryString.isEmpty()
                    || entryString.toLowerCase().equals("unknown")) {
                ivTCAStart.setVisibility(View.GONE);
            } else {
                ivTCAStart.setVisibility(View.VISIBLE);
            }
        }
        else {
            ivTCAStart.setVisibility(View.VISIBLE);
        }
    }

    private void controlEndIcon()
    {
        if(exitIndex > 0) {
            String exitString = null;
            if (exitIndex <= subLocList.size()) {
                exitString = subLocList.get(exitIndex - 1).getLoc_name();
            }

            if (exitString == null || exitString.isEmpty() || exitString.toLowerCase().equals("unknown")) {
                ivTCAEnd.setVisibility(View.GONE);
            } else {
                ivTCAEnd.setVisibility(View.VISIBLE);
            }
        }
        else {
            ivTCAEnd.setVisibility(View.VISIBLE);
        }
    }
    
    private void setupListener()
    {
        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
        ratesListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                closeProgressDialog();
                try {
                    if (response != null) {
                        if(checkResponse(response)) {
                            Gson gson = new GsonBuilder().serializeNulls().create();
                            CalRatesResponse calRatesResponse = gson.fromJson(response,
                                    CalRatesResponse.class);

                            String selectedPayment = (String) paymentSpinner.getSelectedItem();
                            selectedPayment = selectedPayment.toLowerCase();

                            if (selectedPayment.contains("one")) {
                                llOttResult.setVisibility(View.VISIBLE);
                                llAccountResult.setVisibility(View.GONE);
                                tvOttResult.setText(calRatesResponse.getOtt_rate());
                            } else {
                                llOttResult.setVisibility(View.GONE);
                                llAccountResult.setVisibility(View.VISIBLE);
                                tvEveningPeakResult.setText(calRatesResponse.getPeak_rate());
                                tvOffPeakResult.setText(calRatesResponse.getOffpeak_rate());
                                tvWeekendsResult.setText(calRatesResponse.getWeekend_rate());
                            }
                        }
                    } else {
                        showToastMessage(getString(R.string.network_error_retry));
                    }
                } catch (Exception e) {
                    showToastMessage(getString(R.string.network_error_retry));
                }
            }
        };

        entrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                entryIndex = position;
                getRates();
                controlEntryMarker(position);
                scaleMap();
                controlStartIcon();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        exitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                exitIndex = position;
                getRates();
                controlExitMarker(position);
                scaleMap();
                controlEndIcon();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        roadSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (routeIndex != position) {
                    routeIndex = position;
                    entryIndex = 0;
                    exitIndex = 0;
                    initWidgets();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        paymentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getRates();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        vehicleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                getRates();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void controlEntryMarker(int position)
    {
        if(position > 0)
        {
            LocInfo locInfo = subLocList.get(position-1);
            if(locInfo.getLat() != 0.0 && locInfo.getLongitude() != 0.0) {
                LatLng latLng = new LatLng(locInfo.getLat(), locInfo.getLongitude());

                if (entryMarker == null) {
                    entryMarker = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_tca_start_big))
                            .flat(true)
                            .title("")
                            .snippet("")
                            .position(latLng));
                } else {
                    entryMarker.setPosition(latLng);
                    entryMarker.setVisible(true);
                }
            }
        }
        else
        {
            if(entryMarker != null)
            {
                entryMarker.setVisible(false);
            }
        }
    }

    private void controlExitMarker(int position)
    {
        if(position > 0)
        {
            LocInfo locInfo = subLocList.get(position-1);
            if(locInfo.getLat() != 0.0 && locInfo.getLongitude() != 0.0) {
                LatLng latLng = new LatLng(locInfo.getLat(), locInfo.getLongitude());
                if (exitMarker == null) {
                    exitMarker = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_tca_end_big))
                            .flat(true)
                            .title("")
                            .snippet("")
                            .position(latLng));
                } else {
                    exitMarker.setPosition(latLng);
                    exitMarker.setVisible(true);
                }
            }
        }
        else
        {
            if(exitMarker != null)
            {
                exitMarker.setVisible(false);
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMyLocationEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mapIsReady = true;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(Resource.CENTER_LATITUDE,Resource.CENTER_LONGITUDE),
                Resource.DEFAULT_ZOOM_LEVEL));
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                if (subLocLists.size() > 0) {
                    subLocList = subLocLists.get(routeIndex);
                    scaleMap(subLocList);
                }
            }
        });
    }

    private void scaleMap(List<LocInfo> subLocList)
    {
        if(!mapIsReady)
        {
            return;
        }
        try {
            LatLngBounds.Builder mBounds = new LatLngBounds.Builder();

            for (int i = 0; i < subLocList.size(); i++) {
                LocInfo locInfo = subLocList.get(i);
                if(locInfo.getLat() != 0.0 && locInfo.getLongitude() != 0.0) {
                    LatLng latLng = new LatLng(locInfo.getLat(), locInfo.getLongitude());
                    mBounds.include(latLng);
                }
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mBounds.build(), 16));
        }
        catch (Exception exception)
        {
            Log.i(LOG_TAG,"Exception:"+exception.toString());
        }
    }

    private void scaleMap()
    {
        if(!mapIsReady || (entryIndex == 0 && exitIndex == 0))
        {
            return;
        }
        try {
            if(exitIndex > 0 && entryIndex >0) {

                LocInfo startLocInfo = subLocList.get(entryIndex -1);
                LocInfo endLocInfo = subLocList.get(exitIndex -1);
                LatLngBounds.Builder mBounds = new LatLngBounds.Builder();

                if (startLocInfo.getLat() != 0.0  && startLocInfo.getLongitude() != 0.0 ) {
                    LatLng latLng = new LatLng(startLocInfo.getLat(), startLocInfo.getLongitude());
                    mBounds.include(latLng);
                }

                if (endLocInfo.getLat() != 0.0 && endLocInfo.getLongitude() != 0.0) {
                    LatLng latLng = new LatLng(endLocInfo.getLat(), endLocInfo.getLongitude());
                    mBounds.include(latLng);
                }

                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mBounds.build(), Convert.dpToPx(40)));

            }
            else
            {
                LatLng latLng = null;
                if(exitIndex > 0)
                {
                    LocInfo endLocInfo = subLocList.get(exitIndex -1);
                    if (endLocInfo.getLat() != 0.0 && endLocInfo.getLongitude() != 0.0) {
                        latLng = new LatLng(endLocInfo.getLat(), endLocInfo.getLongitude());
                    }
                }
                else if(entryIndex > 0)
                {
                    LocInfo startLocInfo = subLocList.get(entryIndex -1);
                    if (startLocInfo.getLat() != 0.0  && startLocInfo.getLongitude() != 0.0 ) {
                        latLng = new LatLng(startLocInfo.getLat(), startLocInfo.getLongitude());
                    }
                }
                if(latLng != null)
                {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,11));
                }
            }
        } catch (Exception exception) {

        }
    }
//vehicle_class=2&from_loc_id=1&to_loc_id=8&toll_road=&dir_id=&start_date=&rate_type=2&action=calcRates
    private void getRates()
    {
        if(entryIndex > 0 && exitIndex >0) {
            showProgressDialog();
            LocInfo startLocInfo = subLocList.get(entryIndex -1);
            LocInfo endLocInfo = subLocList.get(exitIndex -1);
            int entryId = startLocInfo.getLoc_id();
            int exitId = endLocInfo.getLoc_id();
            int tollRoad = roadList.get(routeIndex).getToll_road();
            String vehicle;
            String selectedPayment = (String)paymentSpinner.getSelectedItem();
            int payment;
            if(vehicleSpinner.getSelectedItemPosition() == 0)
            {
                vehicle = "2";
            }
            else if(vehicleSpinner.getSelectedItemPosition() == 1)
            {
                vehicle = "3";
            }
            else
            {
                vehicle = "5";
            }
            selectedPayment = selectedPayment.toLowerCase();
            if(selectedPayment.contains("one"))
            {
                payment = Constants.RATE_TYPE_OTT;
            }
            else if(selectedPayment.contains("fastrak"))
            {
                payment = Constants.RATE_TYPE_FASTRAK;
            }
            else
            {
                payment = Constants.RATE_TYPE_EXPRESSACCOUNT;
            }

            String url = Resource.URL_PAY_GO;
            String params = "vehicle_class=" + vehicle;
            params = params +"&from_loc_id="+entryId;
            params = params +"&to_loc_id="+ exitId;
            params = params +"&toll_road="+ tollRoad;
            params = params +"&rate_type="+ payment;
            ServerDelegate.calcRatesReq(url, params, ratesListener, errorListener);
        }
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

    private void initRoadSpinner()
    {
        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();

        for(int i =0; i< roadList.size(); i++)
        {
            RoadInfo roadInfo = roadList.get(i);

            categories.add(roadInfo.getRoad_name());
        }

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        roadSpinner.setAdapter(dataAdapter);
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
        initRoadSpinner();

    }

    private void getLocList()
    {
        showProgressDialog();
        Response.Listener listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                closeProgressDialog();
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, response.toString());

                        if(checkResponse(response.toString())) {
                            if(response.has(Resource.KEY_INFO))
                            {
                                String info = response.optString(Resource.KEY_INFO);
                                Gson gson = new GsonBuilder().serializeNulls().create();
                                Type listType = new TypeToken<ArrayList<LocInfo>>() {}.getType();

                                locList = gson.fromJson(info, listType);
                                generateRoadList();
                                initWidgets();
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

        ServerDelegate.getLocList(Resource.URL_PAY_GO, listener, errorListener);
    }
}
