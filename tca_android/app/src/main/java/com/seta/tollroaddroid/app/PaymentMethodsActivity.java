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
import com.seta.tollroaddroid.app.json.PaymentMethod;
import com.seta.tollroaddroid.app.utilities.Constants;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PaymentMethodsActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private Response.ErrorListener errorListener;
    private List<PaymentMethod> paymentMethods = new ArrayList<PaymentMethod>();
    private ListView lvMenu;
    private LinearLayout llAddAnotherPaymentMethod;
    private MultiButtonsPopupDialog gMultiButtonsPopupDialog;

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit Account_Payment_Method page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_methods);
        FlurryAgent.logEvent("Enter Account_Payment_Method page.");

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        lvMenu = (ListView)findViewById(R.id.lv_menu);
        llAddAnotherPaymentMethod = (LinearLayout)findViewById(R.id.ll_add_another_payment_method);

        setupListener();
        getPaymentMethods();
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
                if (position < paymentMethods.size()) {
                    TollRoadsApp.getInstance().gPaymentMethod = paymentMethods.get(position);
                    gotoActivityForResult(view.getContext(), PaymentMethodDetailActivity.class,
                            Constants.UPDATE_PAYMENT_METHOD_REQUEST_CODE);
                }
            }
        });

        lvMenu.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //At least need one payment method;
                if (position < paymentMethods.size()) {
                    TollRoadsApp.getInstance().gPaymentMethod = paymentMethods.get(position);
                    showUpDeleteConfirmDialog();
                    return true;
                }
                return false;
            }
        });
        llAddAnotherPaymentMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivityForResult(v.getContext(), NewPaymentMethodActivity.class, Constants.NEW_PAYMENT_REQUEST_CODE);
            }
        });
    }

    private void showUpDeleteConfirmDialog()
    {
        gMultiButtonsPopupDialog = new MultiButtonsPopupDialog(this,R.style.CustomProgressDialog);
        gMultiButtonsPopupDialog.createDialog(this);
        gMultiButtonsPopupDialog.CutomizeTitle(View.VISIBLE, getString(R.string.remove_payment_method_title));

        gMultiButtonsPopupDialog.CutomizeContent(View.VISIBLE, getString(R.string.remove_payment_method_content));
        gMultiButtonsPopupDialog.CutomizeExtraButton(View.VISIBLE, 0, getString(R.string.delete), Color.RED, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                String params = "paymethod_id="+TollRoadsApp.getInstance().gPaymentMethod.getPaymethod_id();

                deletePaymentMethodRequest(Resource.URL_PAYMENT, params);
            }
        });
        gMultiButtonsPopupDialog.CutomizePositiveButton(View.GONE, 0, "", null);
        gMultiButtonsPopupDialog.CutomizeNeutralButton(View.GONE, 0, "", null);
        gMultiButtonsPopupDialog.CutomizeNegativeButton(View.VISIBLE, 0, getString(R.string.cancel), null);

        gMultiButtonsPopupDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if((requestCode == Constants.NEW_PAYMENT_REQUEST_CODE && resultCode == RESULT_OK)
            ||(requestCode == Constants.UPDATE_PAYMENT_METHOD_REQUEST_CODE && resultCode == RESULT_OK))
        {
            getPaymentMethods();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private SimpleAdapter paymentMethodListAdapter;
    private SimpleAdapter getPaymentMethodMenuAdapter() {
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

        for (int i = 0; i < paymentMethods.size(); i++) {
            PaymentMethod paymentMethod = paymentMethods.get(i);

            HashMap<String, Object> map = new HashMap<String, Object>();
            if(paymentMethod.getPayment_type() != 5) {
                map.put("iv_type", R.drawable.ic_tca_payment);
                map.put("tv_type", getString(R.string.credit_card));
                map.put("tv_number", paymentMethod.getCard_number());
            }
            else
            {
                map.put("iv_type", R.drawable.ic_tca_echeck);
                map.put("tv_type", getString(R.string.electronic_check));
                map.put("tv_number", paymentMethod.getRouting_number());
            }
            String order;
            switch(i)
            {
                case 0:
                    order = getString(R.string.first);
                    break;
                case 1:
                    order = getString(R.string.second);
                    break;
                case 2:
                    order = getString(R.string.third);
                    break;
                default:
                    order = getString(R.string.first);
                    break;
            }
            map.put("tv_order", order);
            data.add(map);
        }

        SimpleAdapter paymentMethodListAdapter = new SimpleAdapter(
                this, data,
                R.layout.item_payment_method_menu,
                new String[] { "iv_type", "tv_type", "tv_number","tv_order"}, new int[] { R.id.iv_type,
                R.id.tv_type, R.id.tv_number, R.id.tv_order});

        return paymentMethodListAdapter;
    }

    private void loadPaymentMethodMenu()
    {

        paymentMethodListAdapter = getPaymentMethodMenuAdapter();
        lvMenu.setAdapter(paymentMethodListAdapter);

        setTotalHeightToListView(lvMenu);
        if(paymentMethods.size() < 3)
        {
            llAddAnotherPaymentMethod.setVisibility(View.VISIBLE);
        }
        else
        {
            llAddAnotherPaymentMethod.setVisibility(View.GONE);
        }
    }
    
    private void getPaymentMethods()
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
                            if(response.has(Resource.KEY_PAYMENT_METHOD_LIST))
                            {
                                String info = response.optString(Resource.KEY_PAYMENT_METHOD_LIST);
                                Gson gson = new GsonBuilder().serializeNulls().create();
                                Type listType = new TypeToken<ArrayList<PaymentMethod>>() {}.getType();

                                paymentMethods = gson.fromJson(info, listType);
                                loadPaymentMethodMenu();
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

        ServerDelegate.getPaymentMethods(Resource.URL_PAYMENT, listener, errorListener);
    }

    private void deletePaymentMethodRequest(String url,String params)
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
                            getPaymentMethods();
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
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {

        super.onPause();
    }

}
