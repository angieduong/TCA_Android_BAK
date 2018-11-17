package com.seta.tollroaddroid.app;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.custom.MultiButtonsPopupDialog;
import com.seta.tollroaddroid.app.json.Transponder;
import com.seta.tollroaddroid.app.utilities.Constants;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranspondersActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private Response.ErrorListener errorListener;
    private List<Transponder> transponders = new ArrayList<Transponder>();
    private ListView lvMenu;
    private TextView tvRequestTransponders, tvRequestReturn, tvRequestReplace, tvReportLostOrStolen;
    private int selectedNum = 0;

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit Account_Transponders page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transponders);
        FlurryAgent.logEvent("Enter Account_Transponders page.");

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        lvMenu = (ListView)findViewById(R.id.lv_menu);
        tvRequestTransponders = (TextView)findViewById(R.id.tv_request_transponders);
        tvRequestReturn = (TextView)findViewById(R.id.tv_request_return);
        tvRequestReplace = (TextView)findViewById(R.id.tv_request_exchange);
        tvReportLostOrStolen = (TextView)findViewById(R.id.tv_report_lost_or_stolen);

        setupListener();
        getTransponders();
    }

    private void setupListener()
    {
        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvRequestTransponders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(v.getContext(), RequestTranspondersActivity.class);
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

                        }catch (UnsupportedEncodingException exceptionError) {

                        }
                    }
                    else
                    {
                        showToastMessage(getString(R.string.network_error_retry));
                    }
                }
            }
        };

        tvRequestReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportRequest(Constants.REPORT_TRANSPONDER_RETURN);
            }
        });
        tvRequestReplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportRequest(Constants.REPORT_TRANSPONDER_REPLACE);
            }
        });

        tvReportLostOrStolen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(transponders.size() == 0)
                {
                    showToastMessage(getString(R.string.transponder_empty_warning));
                }
                else if(selectedNum == 0)
                {
                    showToastMessage(getString(R.string.report_empty_warning));
                }
                else
                {
                    showUpReportLostOrStolenConfirmDialog();
                }
            }
        });
    }

    private MultiButtonsPopupDialog gMultiButtonsPopupDialog;

    private void showUpReportLostOrStolenConfirmDialog()
    {
        String lostOrStolen = getString(R.string.lost_or_stolen);
        String content = getString(R.string.report_stolen_lost_confirmation,lostOrStolen,getTransponderIds(),lostOrStolen);

        gMultiButtonsPopupDialog = new MultiButtonsPopupDialog(this,R.style.CustomProgressDialog);
        gMultiButtonsPopupDialog.createDialog(this);
        gMultiButtonsPopupDialog.CutomizeTitle(View.VISIBLE, getString(R.string.report_lost_or_stolen));

        gMultiButtonsPopupDialog.CutomizeContent(View.VISIBLE, content);
        gMultiButtonsPopupDialog.CutomizeExtraButton(View.VISIBLE, 0, getString(R.string.yes), Color.RED, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportRequest(Constants.REPORT_TRANSPONDER_LOST_OR_STOLEN);
            }
        });
        gMultiButtonsPopupDialog.CutomizePositiveButton(View.GONE, 0, "", null);
        gMultiButtonsPopupDialog.CutomizeNeutralButton(View.GONE, 0, "", null);
        gMultiButtonsPopupDialog.CutomizeNegativeButton(View.VISIBLE, 0, getString(R.string.no), null);

        gMultiButtonsPopupDialog.show();
    }

    private void reportRequest(int type)
    {
        if(transponders.size() == 0)
        {
            showToastMessage(getString(R.string.transponder_empty_warning));
        }
        else if(selectedNum == 0)
        {
            showToastMessage(getString(R.string.report_empty_warning));
        }
        else
        {
            String params = populateParams(type);
            reportTransponders(Resource.URL_TRANSPONDER,params,type);
        }
    }

    private String getTransponderIds()
    {
        String params = "";
        int j = 0;
        for(int i =0; i < transponders.size(); i++)
        {
            Transponder transponder = transponders.get(i);
            if(transponder.isChecked())
            {
                j = j+1;
                if(j == 1)
                {
                    params = params + transponder.getTransponder_number();
                }
                else
                {
                    params = params + ", " + transponder.getTransponder_number();
                }
            }
        }

        if(j > 1)
        {
            params = getString(R.string.transponders_lower_case) + params;
        }
        else
        {
            params = getString(R.string.transponder) + params;
        }

        return  params;
    }
    private String populateParams(int type)
    {
        String params;
        if(type == Constants.REPORT_TRANSPONDER_LOST_OR_STOLEN)
        {
            type = Constants.REPORT_TRANSPONDER_LOST;
        }
        params = "type=" + type;

        int j = 0;
        for(int i =0; i < transponders.size(); i++)
        {
            Transponder transponder = transponders.get(i);
            if(transponder.isChecked())
            {
                j = j+1;
                if(j == 1)
                {
                    params = params + "&transponder_number=" + transponder.getTransponder_number();
                }
                else
                {
                    params = params + "&transponder_number" + j +"="+ transponder.getTransponder_number();
                }
            }
        }

        return  params;
    }

    private void reportTransponders(String url,String params, final int type)
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
                            String message;
                            switch (type)
                            {
                                case Constants.REPORT_TRANSPONDER_LOST_OR_STOLEN:
                                    message = getString(R.string.report_successful);
                                    break;
                                case Constants.REPORT_TRANSPONDER_LOST:
                                    message = getString(R.string.report_successful);
                                    break;
                                case Constants.REPORT_TRANSPONDER_STOLEN:
                                    message = getString(R.string.report_successful);
                                    break;
                                case Constants.REPORT_TRANSPONDER_RETURN:
                                    message = getString(R.string.request_successful);
                                    break;
                                case Constants.REPORT_TRANSPONDER_REPLACE:
                                    message = getString(R.string.request_successful);
                                    break;

                                default:
                                    message = getString(R.string.report_successful);
                                    break;
                            }
                            showToastMessage(message);
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

        ServerDelegate.reportTransponders(url, params, listener, errorListener);
    }

    private TransponderListAdapter transponderListAdapter;
    private TransponderListAdapter getTransponderMenuAdapter() {
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

        for (int i = 0; i < transponders.size(); i++) {
            Transponder transponder = transponders.get(i);

            HashMap<String, Object> map = new HashMap<String, Object>();

            map.put("tv_transponder_number", transponder.getTransponder_number());
            map.put("tv_transponder_code", transponder.getTransponder_code());
            data.add(map);
        }

        TransponderListAdapter transponderListAdapter = new TransponderListAdapter(
                this, data,
                R.layout.item_transponder_menu,
                new String[] { "tv_transponder_number","tv_transponder_code"},
                new int[] { R.id.tv_transponder_number, R.id.tv_transponder_code});

        return transponderListAdapter;
    }

    class TransponderListAdapter extends SimpleAdapter {
        Context mContext;
        private LayoutInflater l_Inflater;

        public TransponderListAdapter(Context context,
                                      List<? extends Map<String, ?>> data, int resource,
                                      String[] from, int[] to) {
            super(context, data, resource, from, to);

            mContext = context;
            l_Inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return transponders.size();
        }

        @Override
        public View getView(int position, View convertView, final ViewGroup parent) {
            Transponder transponder = transponders.get(position);

            if (convertView == null) {
                convertView = l_Inflater.inflate(
                        R.layout.item_transponder_menu, null);
            }

            TextView tvTransponderNumber = (TextView) (convertView
                    .findViewById(R.id.tv_transponder_number));
            TextView tvTransponderCode = (TextView) (convertView
                    .findViewById(R.id.tv_transponder_code));
            CheckBox checkBox = (CheckBox) (convertView
                    .findViewById(R.id.cb_transponder));

            tvTransponderNumber.setText(transponder.getTransponder_number());
            tvTransponderCode.setText(transponder.getTransponder_code());
            checkBox.setChecked(transponder.isChecked());
            Log.e(LOG_TAG, "position:" + position + ",is checked:" + transponder.isChecked());
            checkBox.setTag(position);
//            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    int position = (Integer) buttonView.getTag();
//                    if (position < transponders.size()) {
//                        transponders.get(position).setChecked(isChecked);
//                        Log.e(LOG_TAG, "position:" + position + ",set checked:" + transponders.get(position).isChecked());
//                    }
//                }
//            });
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = (Integer) v.getTag();
                    if (position < transponders.size()) {
                        Transponder transponder = transponders.get(position);
                        CheckBox checkBox = (CheckBox)v;

                        if(!transponder.isChecked())
                        {
                            if(selectedNum < 5) {
                                selectedNum++;
                                transponder.setChecked(!transponder.isChecked());
                                checkBox.setChecked(transponder.isChecked());
                            }
                        }
                        else
                        {
                            if(selectedNum > 0)
                            {
                                selectedNum--;
                            }
                            transponder.setChecked(!transponder.isChecked());
                            checkBox.setChecked(transponder.isChecked());
                        }

                        Log.e(LOG_TAG, "position:" + position + ",set checked:" + transponder.isChecked());
                    }
                }
            });
            return convertView;
        }
    }

    private void loadTransponderMenu()
    {
        if(transponderListAdapter == null)
        {
            transponderListAdapter = getTransponderMenuAdapter();
            lvMenu.setAdapter(transponderListAdapter);
        }
        else
        {
            transponderListAdapter.notifyDataSetInvalidated();
        }
        setTotalHeightToListView(lvMenu);
    }
    
    private void getTransponders()
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
                            if(response.has(Resource.KEY_TRANSPONDER_LIST))
                            {
                                String info = response.optString(Resource.KEY_TRANSPONDER_LIST);
                                Gson gson = new GsonBuilder().serializeNulls().create();
                                Type listType = new TypeToken<ArrayList<Transponder>>() {}.getType();

                                transponders = gson.fromJson(info, listType);
//                                Transponder transponder = new Transponder();
//                                transponder.setTransponder_number("1");
//                                transponders.add(transponder);
//                                transponder = new Transponder();
//                                transponder.setTransponder_number("2");
//                                transponders.add(transponder);
//                                transponder =new Transponder();
//                                transponder.setTransponder_number("3");
//                                transponders.add(transponder);
//                                transponder =new Transponder();
//                                transponder.setTransponder_number("4");
//                                transponders.add(transponder);
//                                transponder =new Transponder();
//                                transponder.setTransponder_number("5");
//                                transponders.add(transponder);
//                                transponder =new Transponder();
//                                transponder.setTransponder_number("6");
//                                transponders.add(transponder);
                                loadTransponderMenu();
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

        ServerDelegate.getTransponders(Resource.URL_TRANSPONDER, listener, errorListener);
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
