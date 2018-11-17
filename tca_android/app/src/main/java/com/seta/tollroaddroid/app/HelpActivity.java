package com.seta.tollroaddroid.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.seta.tollroaddroid.app.api.Resource;

public class HelpActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private TextView tvFaq, tvServiceCenters;

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit Menu_Help page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        FlurryAgent.logEvent("Enter Menu_Help page.");

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvFaq = (TextView)findViewById(R.id.tv_faq);
        tvServiceCenters = (TextView)findViewById(R.id.tv_service_centers);

        setupListener();
    }

    private void setupListener()
    {
        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tvFaq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(Resource.KEY_URL, Resource.FAQ_URL);
                bundle.putString(Resource.KEY_TITLE, getString(R.string.faq));

                gotoActivity(v.getContext(),WebActivity.class, bundle);
            }
        });

        tvServiceCenters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(Resource.KEY_URL, Resource.SERVICE_CENTERS__URL);
                bundle.putString(Resource.KEY_TITLE, getString(R.string.service_centers));

                gotoActivity(v.getContext(),WebActivity.class, bundle);
            }
        });
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
