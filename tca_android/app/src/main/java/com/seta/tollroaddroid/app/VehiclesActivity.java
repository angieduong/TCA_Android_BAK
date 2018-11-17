package com.seta.tollroaddroid.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.custom.MultiButtonsPopupDialog;
import com.seta.tollroaddroid.app.json.Vehicle;
import com.seta.tollroaddroid.app.utilities.Constants;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VehiclesActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private Response.ErrorListener errorListener;
    private List<Vehicle> vehicles = new ArrayList<Vehicle>();
    private ListView lvMenu;
    private LinearLayout llAddAnotherVehicle;
    private MultiButtonsPopupDialog gMultiButtonsPopupDialog;

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit Account_Vehicles page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicles);
        FlurryAgent.logEvent("Enter Account_Vehicles page.");

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        lvMenu = (ListView)findViewById(R.id.lv_menu);
        llAddAnotherVehicle = (LinearLayout)findViewById(R.id.ll_add_another_vehicle);

        setupListener();
        getVehicles();
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

        lvMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < vehicles.size()) {
                    TollRoadsApp.getInstance().gVehicle = vehicles.get(position);
                    gotoActivityForResult(view.getContext(), VehicleDetailActivity.class,
                            Constants.UPDATE_VEHICLE_METHOD_REQUEST_CODE);
                }
            }
        });

        lvMenu.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //At least need one payment method;
                if (position < vehicles.size()){
                    TollRoadsApp.getInstance().gVehicle = vehicles.get(position);
                    showUpDeleteConfirmDialog();
                    return true;
                }
                return false;
            }
        });

        llAddAnotherVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivityForResult(v.getContext(), NewVehicleActivity.class,
                        Constants.NEW_VEHICLE_REQUEST_CODE);
            }
        });
    }

    private void showUpDeleteConfirmDialog()
    {
        gMultiButtonsPopupDialog = new MultiButtonsPopupDialog(this,R.style.CustomProgressDialog);
        gMultiButtonsPopupDialog.createDialog(this);
        gMultiButtonsPopupDialog.CutomizeTitle(View.VISIBLE, getString(R.string.remove_vehicle_title));

        gMultiButtonsPopupDialog.CutomizeContent(View.VISIBLE, getString(R.string.remove_vehicle_content));
        gMultiButtonsPopupDialog.CutomizeExtraButton(View.VISIBLE, 0, getString(R.string.delete), Color.RED, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                String params = "vehicle_id=" + TollRoadsApp.getInstance().gVehicle.getId();

                deleteRequest(Resource.URL_VEHICLE, params);
            }
        });
        gMultiButtonsPopupDialog.CutomizePositiveButton(View.GONE, 0, "", null);
        gMultiButtonsPopupDialog.CutomizeNeutralButton(View.GONE, 0, "", null);
        gMultiButtonsPopupDialog.CutomizeNegativeButton(View.VISIBLE, 0, getString(R.string.cancel), null);

        gMultiButtonsPopupDialog.show();
    }
    private void deleteRequest(String url,String params)
    {
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
                            showToastMessage(getString(R.string.successfully_deleted));
                            getVehicles();
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

        ServerDelegate.deleteRequest(url, params, listener, errorListener);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Constants.UPDATE_VEHICLE_METHOD_REQUEST_CODE ||
                requestCode == Constants.NEW_VEHICLE_REQUEST_CODE)
        {
            if(resultCode == RESULT_OK)
            {
                getVehicles();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private SimpleAdapter vehicleListAdapter;
    private SimpleAdapter getRecentTollMenuAdapter() {
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

        for (int i = 0; i < vehicles.size(); i++) {
            Vehicle vehicle = vehicles.get(i);

            HashMap<String, Object> map = new HashMap<String, Object>();

            map.put("tv_year", String.valueOf(vehicle.getYear()));
            map.put("tv_make", vehicle.getMake());
            map.put("tv_plate", vehicle.getPlate());
            data.add(map);
        }

        SimpleAdapter vehicleListAdapter = new SimpleAdapter(
                this, data,
                R.layout.item_vehicle_menu,
                new String[] { "tv_year", "tv_make", "tv_plate"}, new int[] { R.id.tv_year,
                R.id.tv_make, R.id.tv_plate });

        return vehicleListAdapter;
    }

    private void loadVehicleMenu()
    {
        vehicleListAdapter = getRecentTollMenuAdapter();
        lvMenu.setAdapter(vehicleListAdapter);
        setTotalHeightToListView(lvMenu);

        llAddAnotherVehicle.setVisibility(View.VISIBLE);
    }
    
    private void getVehicles()
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
                            if(response.has(Resource.KEY_VEHICLE_LIST))
                            {
                                String info = response.optString(Resource.KEY_VEHICLE_LIST);
                                Gson gson = new GsonBuilder().serializeNulls().create();
                                Type listType = new TypeToken<ArrayList<Vehicle>>() {}.getType();

                                vehicles = gson.fromJson(info, listType);
                                loadVehicleMenu();
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

        ServerDelegate.getVehicles(Resource.URL_VEHICLE, listener, errorListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {

        super.onPause();
    }

}
