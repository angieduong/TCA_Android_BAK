package com.seta.tollroaddroid.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.seta.tollroaddroid.app.api.Resource;

public class FeedbackActivity extends FragmentActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack;
    private TextView tvLikeApp, tvProblem;

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit Menu_Feedback page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        FlurryAgent.logEvent("Enter Menu_Feedback page.");

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvLikeApp = (TextView)findViewById(R.id.tv_like_app);
        tvProblem = (TextView)findViewById(R.id.tv_problem);

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
        tvLikeApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(Resource.PLAY_STORE_LINK));

                startActivity(intent);
            }
        });

        tvProblem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TollRoadsApp.sendAnEmail(v.getContext(), Resource.FEEDBACK_EMAIL);
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
