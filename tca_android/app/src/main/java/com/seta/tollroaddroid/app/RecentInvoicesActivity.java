package com.seta.tollroaddroid.app;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.custom.InvoicesPopupDialog;
import com.seta.tollroaddroid.app.custom.MultiButtonsPopupDialog;
import com.seta.tollroaddroid.app.json.Invoice;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecentInvoicesActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private Response.ErrorListener errorListener;
    private List<Invoice> recentInvoices = new ArrayList<Invoice>();
    private ListView lvMenu;

    private List<List<Invoice>> recentInvoicesList = new ArrayList<List<Invoice>>();
    private View.OnClickListener monthOnClickListener;

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit Account_Payment page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_invoices);
        FlurryAgent.logEvent("Enter Account_Payment page.");

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        lvMenu = (ListView)findViewById(R.id.lv_menu);
        monthOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Invoice> invoiceList = (List<Invoice>) v.getTag();
                if(invoiceList != null)
                {
                    if(invoiceList.size() == 1) {
                        Invoice invoice = invoiceList.get(0);
                        Bundle bundle = new Bundle();
                        bundle.putString(Resource.KEY_URL, invoice.getInvoiceUrl());
                        bundle.putString(Resource.KEY_TITLE, getString(R.string.invoices));
                        bundle.putString(Resource.KEY_INVOICE_ID, invoice.getInvoiceId());

                        gotoActivity(v.getContext(), InvoiceActivity.class, bundle);
                    }
                    else
                    {
                        InvoicesPopupDialog invoicesPopupDialog = new InvoicesPopupDialog(v.getContext(), R.style.CustomProgressDialog);
                        invoicesPopupDialog.createDialog(v.getContext(), invoiceList);
                        invoicesPopupDialog.show();
                    }
                }
            }
        };

        lvMenu.setOnItemClickListener(null);

        setupListener();

        getRecentInvoices();
        //loadMockData();

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

    }

    private InvoiceListAdapter recentPaymentListAdapter;
    private InvoiceListAdapter getRecentPaymentMenuAdapter() {
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

        for (int i = 0; i < recentInvoicesList.size(); i++) {
            Invoice recentInvoice = recentInvoicesList.get(i).get(0);

            HashMap<String, Object> map = new HashMap<String, Object>();
            String date = recentInvoice.getInvoiceDate();

            String[] dateSplit = date.split("/");
            map.put("tv_year", dateSplit[2]);
            map.put("tv_month", dateSplit[0]);
            map.put("tv_count", recentInvoicesList.get(i).size());
            data.add(map);
        }

        return new InvoiceListAdapter(
                this, data,
                R.layout.item_recent_invoice_menu,
                new String[] { "tv_year", "tv_month", "tv_count"},
                new int[] { R.id.tv_year,R.id.tv_month, R.id.tv_count});
    }

    private boolean inSameYear(Invoice preInvoice, Invoice curInvoice)
    {
        String[] dateSplit1 = curInvoice.getInvoiceDate().split("/");
        String[] dateSplit2 = preInvoice.getInvoiceDate().split("/");
        if(dateSplit1[2].equals(dateSplit2[2]))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private boolean inSameMonth(Invoice preInvoice, Invoice curInvoice)
    {
        String[] dateSplit1 = curInvoice.getInvoiceDate().split("/");
        String[] dateSplit2 = preInvoice.getInvoiceDate().split("/");
        if(dateSplit1[0].equals(dateSplit2[0]) && dateSplit1[2].equals(dateSplit2[2]))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private String getMonthName(String month)
    {
//        Calendar cal=Calendar.getInstance();
//        SimpleDateFormat month_date = new SimpleDateFormat("MMMM", Locale.US);
        int monthNum = Integer.parseInt(month);

        if(monthNum > 0)
        {
            monthNum = monthNum - 1;
        }
//        cal.set(Calendar.MONTH, monthNum);
//        return month_date.format(cal.getTime());
        return DateFormatSymbols.getInstance(Locale.US).getMonths()[monthNum];

    }

    class InvoiceListAdapter extends SimpleAdapter {
        Context mContext;
        private LayoutInflater l_Inflater;


        public InvoiceListAdapter(Context context,
                               List<? extends Map<String, ?>> data, int resource,
                               String[] from, int[] to) {
            super(context, data, resource, from, to);

            mContext = context;
            l_Inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return recentInvoicesList.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = l_Inflater.inflate(
                        R.layout.item_recent_invoice_menu, null);
            }

            TextView tvYear = (TextView) convertView.findViewById(R.id.tv_year);
            TextView tvMonth = (TextView) convertView.findViewById(R.id.tv_month);
            TextView tvCount = (TextView) convertView.findViewById(R.id.tv_count);
            RelativeLayout rlMonth = (RelativeLayout) convertView.findViewById(R.id.rl_month);

            Invoice curInvoice = recentInvoicesList.get(position).get(0);

            String[] dateSplit = curInvoice.getInvoiceDate().split("/");
            tvYear.setText(dateSplit[2]);

            if(recentInvoicesList.get(position).size() > 1)
            {
                tvCount.setText(String.valueOf(recentInvoicesList.get(position).size()));
                tvCount.setVisibility(View.VISIBLE);
            }
            else
            {
                tvCount.setVisibility(View.GONE);
            }

            tvMonth.setText(getMonthName(dateSplit[0]));
            rlMonth.setTag(recentInvoicesList.get(position));

            rlMonth.setOnClickListener(monthOnClickListener);
            if(position > 0)
            {
                if(inSameYear(recentInvoicesList.get(position-1).get(0), curInvoice))
                {
                    tvYear.setVisibility(View.GONE);
                }
                else
                {
                    tvYear.setVisibility(View.VISIBLE);
                }
            }
            else
            {
                tvYear.setVisibility(View.VISIBLE);
            }

            convertView.setClickable(false);
            convertView.setFocusableInTouchMode(false);

            convertView.setOnTouchListener(null);
            convertView.setOnClickListener(null);
            // super.getView(position, convertView, parent);
            return convertView;//mView;//
        }
    }
    
    private void loadRecentInvoicesMenu()
    {
        recentPaymentListAdapter = getRecentPaymentMenuAdapter();
        lvMenu.setAdapter(recentPaymentListAdapter);

        setTotalHeightToListView(lvMenu);
    }
    private String url1 = "\""+"http://206.169.136.93/customer/pdfServlet?cmd=viewInv&invoiceID=1148017&accessCode=SRFSUFTGAQ-TPNSYWUN"+"\"";//"\""+"https://images.template.net/wp-content/uploads/2015/12/24060701/CrawfordTech-Bank-Statement-Sample.pdf" +"\"";
    private String mockResponse = " {\"success\":1,\"status\":200,\"message\":\"\",\"tokenID\":\"6606999XWTkKuPthdxTSZ5UY646F0VxxAT6hkbV\",\"uniqueID\":" +
            TollRoadsApp.getInstance().getUniqueID()+",\"info\":[{\"invoice_id\":\"111222\",\"invoice_date\":\"01/18/2017\",\"invoice_amount\":15.86,\"balance_due\":0.00,\"url\":"+url1+"}," +
            "{\"invoice_id\":\"116181\",\"invoice_date\":\"01/16/2017\",\"invoice_amount\":21.26,\"balance_due\":0.00,\"url\":"+url1+"}," +
            "{\"invoice_id\":\"116181\",\"invoice_date\":\"12/16/2016\",\"invoice_amount\":21.26,\"balance_due\":0.00,\"url\":"+url1+"}," +
            "{\"invoice_id\":\"116181\",\"invoice_date\":\"11/18/2016\",\"invoice_amount\":21.26,\"balance_due\":0.00,\"url\":"+url1+"}," +
            "{\"invoice_id\":\"116186\",\"invoice_date\":\"12/18/2016\",\"invoice_amount\":21.26,\"balance_due\":0.00,\"url\":"+url1+"}]}";

    private void getRecentInvoices()
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
                            if(response.has(Resource.KEY_INFO))
                            {
                                String info = response.optString(Resource.KEY_INFO);
                                Gson gson = new GsonBuilder().serializeNulls().create();
                                Type listType = new TypeToken<ArrayList<Invoice>>() {}.getType();

                                recentInvoices = gson.fromJson(info, listType);
                                sortRecentInvoices(recentInvoices);
                                groupRecentInvoices();
                                loadRecentInvoicesMenu();
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

        showProgressDialog();
        ServerDelegate.getRecentInvoices(Resource.URL_PAYMENT, listener, errorListener);
    }

    private int compareString(String s1, String s2)
    {
        int int1 = Integer.parseInt(s1);
        int int2 = Integer.parseInt(s2);
        
        return int1 -int2;
    }

    private void groupRecentInvoices()
    {
        int j=0; // item index
        for(int i=0; i< recentInvoices.size(); i++)
        {
            if(i == 0)
            {
                List<Invoice> recentInvoiceItem = new ArrayList<Invoice>();
                recentInvoiceItem.add(recentInvoices.get(i));
                recentInvoicesList.add(recentInvoiceItem);
            }
            else
            {
                if(inSameMonth(recentInvoices.get(i-1), recentInvoices.get(i)))
                {
                    recentInvoicesList.get(j).add(recentInvoices.get(i));
                }
                else
                {
                    List<Invoice> recentInvoiceItem = new ArrayList<Invoice>();
                    recentInvoiceItem.add(recentInvoices.get(i));
                    recentInvoicesList.add(recentInvoiceItem);
                    j++;
                }
            }
        }
    }

    private void sortRecentInvoices(List<Invoice> mInvoice)
    {
        Comparator<Invoice> comp = new Comparator<Invoice>() {
            public int compare(Invoice p1, Invoice p2) {
                try {
                    if ((p1 == null) || (p2 == null)) {
                        return 0;
                    }
                    else
                    {
                        String[] dateSplit1 = p1.getInvoiceDate().split("/");
                        String[] dateSplit2 = p2.getInvoiceDate().split("/");
                        if(dateSplit1.length != 3 || dateSplit2.length != 3 )
                        {
                            return 0; 
                        }
                        else
                        {
                            if(!dateSplit1[2].equals(dateSplit2[2]))
                            {
                                return compareString(dateSplit1[2], dateSplit2[2]);
                            }
                            else if(!dateSplit1[0].equals(dateSplit2[0]))
                            {
                                return compareString(dateSplit1[0], dateSplit2[0]);
                            }
                            else
                            {
                                return compareString(dateSplit1[1], dateSplit2[1]); 
                            }
                        }
                    }
                }
                catch (Exception exception)
                {
                    return 0;
                }
            }

        };
        Collections.sort(mInvoice, comp);
    }
    
    private void loadMockData()
    {
        JSONObject response = null;
        try {
            response = new JSONObject(mockResponse);
            Log.d(LOG_TAG, response.toString());

            if(checkResponse(response.toString())) {
                if(response.has(Resource.KEY_INFO))
                {
                    String info = response.optString(Resource.KEY_INFO);
                    Gson gson = new GsonBuilder().serializeNulls().create();
                    Type listType = new TypeToken<ArrayList<Invoice>>() {}.getType();

                    recentInvoices = gson.fromJson(info, listType);
                    sortRecentInvoices(recentInvoices);
                    groupRecentInvoices();
                    loadRecentInvoicesMenu();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
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

}
