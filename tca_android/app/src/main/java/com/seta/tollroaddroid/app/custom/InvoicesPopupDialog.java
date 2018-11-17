package com.seta.tollroaddroid.app.custom;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.seta.tollroaddroid.app.InvoiceActivity;
import com.seta.tollroaddroid.app.R;
import com.seta.tollroaddroid.app.RecentInvoicesActivity;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.json.Invoice;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.seta.tollroaddroid.app.BaseActivity.setTotalHeightToListView;


public class InvoicesPopupDialog extends Dialog {
	private Context mContext = null;
	private InvoicesPopupDialog gInvoicesPopupDialog = null;
	private List<Invoice> recentInvoices = new ArrayList<Invoice>();
	private ListView lvMenu;
	private SimpleAdapter recentPaymentListAdapter;

	public InvoicesPopupDialog(Context context){
		super(context);
		this.mContext = context;
	}

	public InvoicesPopupDialog(Context context, int theme) {
        super(context, theme);
        this.mContext = context;
        gInvoicesPopupDialog = this;
    }
	
    @Override
    public void dismiss() {

    	super.dismiss();
    }
    
	Context getCurContext()
	{
		return this.mContext;
	}
	
	public InvoicesPopupDialog createDialog(Context context, List<Invoice> invoices){
		//gInvoicesPopupDialog = new MultiButtonsPopupDialog(context,R.style.CustomProgressDialog);
		gInvoicesPopupDialog.setContentView(R.layout.dialog_popup_invoices);
		gInvoicesPopupDialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		gInvoicesPopupDialog.setCancelable(true);

		ImageView ivBack = (ImageView) gInvoicesPopupDialog.findViewById(R.id.iv_go_back);
		ivBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				gInvoicesPopupDialog.dismiss();
			}
		});
		recentInvoices = invoices;
		this.mContext = context;

		lvMenu = (ListView) gInvoicesPopupDialog.findViewById(R.id.lv_menu);
		loadRecentInvoicesMenu();

		lvMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(position < recentInvoices.size())
				{
					Invoice invoice = recentInvoices.get(position);
					Bundle bundle = new Bundle();
					bundle.putString(Resource.KEY_URL, invoice.getInvoiceUrl());
					bundle.putString(Resource.KEY_TITLE, mContext.getString(R.string.invoices));
					bundle.putString(Resource.KEY_INVOICE_ID, invoice.getInvoiceId());

					Intent intent = new Intent(mContext, InvoiceActivity.class);
					intent.putExtras(bundle);
					mContext.startActivity(intent);
				}
			}
		});
		return gInvoicesPopupDialog;
	}

	private void loadRecentInvoicesMenu()
	{
		recentPaymentListAdapter = getRecentPaymentMenuAdapter();
		lvMenu.setAdapter(recentPaymentListAdapter);

		setTotalHeightToListView(lvMenu);
	}

	private String getMonthName(String month)
	{
		int monthNum = Integer.parseInt(month);

		if(monthNum > 0)
		{
			monthNum = monthNum - 1;
		}

		return DateFormatSymbols.getInstance(Locale.US).getMonths()[monthNum];

	}

	//format 01/16/2017 to January 16,2017
	private String formatDate(String date)
	{
		if(date == null || date.isEmpty())
		{
			return "";
		}
		else
		{
			String[] dateSplit = date.split("/");
			if(dateSplit.length != 3)
			{
				return "";
			}
			else
			{
				return getMonthName(dateSplit[0]) + " "+ dateSplit[1]+", "+ dateSplit[2];
			}
		}
	}

	private SimpleAdapter getRecentPaymentMenuAdapter() {
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

		for (int i = 0; i < recentInvoices.size(); i++) {
			Invoice recentInvoice = recentInvoices.get(i);

			HashMap<String, Object> map = new HashMap<String, Object>();
			String date = recentInvoice.getInvoiceDate();

			//String[] dateSplit = date.split("/");

			map.put("tv_date", formatDate(date));

			data.add(map);
		}

		return new SimpleAdapter(
				mContext, data,
				R.layout.item_invoice_popup_menu,
				new String[] { "tv_date"},
				new int[] {R.id.tv_date});
	}
	

	
    public void onWindowFocusChanged(boolean hasFocus){    	
    	if (gInvoicesPopupDialog == null){
    		return;
    	}
    }

	
}
