package com.seta.tollroaddroid.app.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.seta.tollroaddroid.app.ContactInformationActivity;
import com.seta.tollroaddroid.app.MakePaymentActivity;
import com.seta.tollroaddroid.app.PaymentMethodsActivity;
import com.seta.tollroaddroid.app.R;
import com.seta.tollroaddroid.app.TollRoadsApp;
import com.seta.tollroaddroid.app.TranspondersActivity;
import com.seta.tollroaddroid.app.VehiclesActivity;
import com.seta.tollroaddroid.app.json.AccountInfo;
import com.seta.tollroaddroid.app.json.AlertBanner;

import java.util.ArrayList;

/**
 * Created by thomashuang on 16-04-04.
 */
public class AlertBannerFragment extends Fragment {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private TextView tvAlertBanner;

    private int currentAlertIndex = 0;
    private int nDisplayInterval = 5000;
    private Runnable gRunnable = null;
    private Handler gHandler = new Handler();

    public AlertBannerFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_alert_banner, container, false);

        tvAlertBanner = (TextView)rootView.findViewById(R.id.tv_alert);

        setupListener();
        refresh();
        gRunnable = new Runnable() {
            @Override
            public void run()
            {
                if(hasAlertMessage()) {
                    ArrayList<AlertBanner> alertBanners = TollRoadsApp.getInstance().accountInfo.
                            getAlert_list();
                    if(currentAlertIndex < alertBanners.size()-1)
                    {
                        currentAlertIndex++;
                    }
                    else
                    {
                        currentAlertIndex = 0;
                    }
                    refresh();
                }
            }
        };

        if(hasAlertMessage()) {
            gHandler.postDelayed(gRunnable, nDisplayInterval);
        }
        return rootView;
    }

    private boolean hasAlertMessage()
    {
        AccountInfo accountInfo = TollRoadsApp.getInstance().accountInfo;
        if(accountInfo != null && !accountInfo.getAlert_list().isEmpty())
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    private void setupListener() {
        tvAlertBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccountInfo accountInfo = TollRoadsApp.getInstance().accountInfo;
                if (accountInfo != null && !accountInfo.getAlert_list().isEmpty()) {
                    ArrayList<AlertBanner> alertBanners = accountInfo.getAlert_list();
                    if (currentAlertIndex < alertBanners.size()) {
                        parseAndGotoActivity(alertBanners.get(currentAlertIndex));
                    }
                }
            }
        });
    }

    private void gotoActivity(Class<?> cla)
    {
        Intent intent = new Intent(getActivity(), cla);
        startActivity(intent);
    }

    private boolean parseAndGotoActivity(AlertBanner alertBanner)
    {
        boolean ret = false;
        String action = alertBanner.getAlert_action();
        if(action != null && !action.isEmpty())
        {
//            String lowerAction = action.toLowerCase();
//            if(lowerAction.contains("display"))
//            {
//                //remove current alert
//            }
//            else
            {
                String lowerAlertType = alertBanner.getAlert_type().toLowerCase();
                if(lowerAlertType.contains("payment"))
                {
                    gotoActivity(MakePaymentActivity.class);
                    ret = true;
                }
                else if(lowerAlertType.contains("pay_method"))
                {
                    gotoActivity(PaymentMethodsActivity.class);
                    ret = true;
                }
                else if(lowerAlertType.contains("vehicle"))
                {
                    gotoActivity(VehiclesActivity.class);
                    ret = true;
                }
                else if(lowerAlertType.contains("transponder"))
                {
                    gotoActivity(TranspondersActivity.class);
                    ret = true;
                }
                else if(lowerAlertType.contains("contact"))
                {
                    gotoActivity(ContactInformationActivity.class);
                    ret = true;
                }
            }
        }
        return ret;
    }
    public void refresh()
    {
        if(tvAlertBanner != null)
        {
            AccountInfo accountInfo = TollRoadsApp.getInstance().accountInfo;
            if(accountInfo != null && !accountInfo.getAlert_list().isEmpty())
            {
                ArrayList<AlertBanner> alertBanners = accountInfo.getAlert_list();
                if(currentAlertIndex >= alertBanners.size())
                {
                    currentAlertIndex = 0;
                }
                tvAlertBanner.setText(alertBanners.get(currentAlertIndex).getAlert_message());
                tvAlertBanner.setVisibility(View.VISIBLE);
                gHandler.postDelayed(gRunnable, nDisplayInterval);
            }
            else
            {
                tvAlertBanner.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onDestroy() {
        if(gHandler != null)
        {
            gHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }
}
