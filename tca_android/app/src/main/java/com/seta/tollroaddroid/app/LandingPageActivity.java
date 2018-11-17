package com.seta.tollroaddroid.app;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.seta.tollroaddroid.app.adapters.MenuItemAdapter;
import com.seta.tollroaddroid.app.api.Convert;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.json.OTTUserInfoRequest;
import com.seta.tollroaddroid.app.model.MenuItem;
import com.seta.tollroaddroid.app.utilities.Constants;

import java.util.ArrayList;

public class LandingPageActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();
    private DrawerLayout drawer;
    private LinearLayout drawerLinear;
    private ListView lvMenu;
    private ImageView ivMenu,ivCloseMenu;
    private ArrayList<MenuItem> menuItems;
    private TextView tvLogin, tvSignUp, tvPayTollNow, tvPayViolation;
    private LinearLayout llTollCalculator;
    private Animation slideInBottom, slideOutTop;
    private ImageView ivBg1;
    private LinearLayout llControllers;
    private int nWelcomeScreenDisplay = 500;

    private Uri uri;
    private String token;
    private TextView tvVersion;

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit Landing page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);
        FlurryAgent.logEvent("Enter Landing page.");

        TollRoadsApp.getInstance().updateAndroidSecurityProvider(this);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLinear = (LinearLayout) findViewById(R.id.left_drawer);
        lvMenu = (ListView) findViewById(R.id.lv_menu);
        ivMenu = (ImageView)findViewById(R.id.iv_menu);
        ivCloseMenu = (ImageView)findViewById(R.id.iv_close_menu);
        tvLogin = (TextView)findViewById(R.id.tv_log_in);
        tvSignUp = (TextView)findViewById(R.id.tv_sign_up);
        tvPayTollNow = (TextView)findViewById(R.id.tv_pay_toll_now);
        llTollCalculator = (LinearLayout)findViewById(R.id.ll_toll_calculator);
        tvPayViolation = (TextView)findViewById(R.id.tv_pay_violation);
        ivBg1 = (ImageView)findViewById(R.id.iv_bg1);

        llControllers = (LinearLayout)findViewById(R.id.ll_controllers);

        tvVersion = (TextView)findViewById(R.id.tv_version);
        setVersion();
        setupListener();
        setupMenuList();
        setupAnimation();

        Handler gHandler = new Handler();
        Runnable gRunnable = new Runnable() {
            @Override
            public void run()
            {
//                if(!TollRoadsApp.getInstance().getToken().isEmpty()) {
//                    gotoActivity(LandingPageActivity.this, MyAccountActivity.class);
//                    finish();
//                }
//                else
                {
                    ivBg1.startAnimation(slideInBottom);
                    ivBg1.setVisibility(View.VISIBLE);
                }
            }
        };
        if(TollRoadsApp.getInstance().gbSplashed)
        {
            animationEndAction();
        }
        else {
            TollRoadsApp.getInstance().gbSplashed = true;
            gHandler.postDelayed(gRunnable, nWelcomeScreenDisplay);
        }

        Intent intent = getIntent();
        if(intent != null)
        {
            uri = intent.getData();
            Log.i(LOG_TAG, "uri:" + uri);

            token = intent.getStringExtra("token");

            if(token == null && uri != null)
            {
                Uri uriParse=Uri.parse(intent.getDataString());
                token = uriParse.getQueryParameter("token");
            }

            if(token != null && !token.isEmpty())
            {
                Bundle bundle = new Bundle();
                bundle.putString("token",token);
                gotoActivity(LandingPageActivity.this, ResetPasswordActivity.class, bundle);
            }
        }
    }

    private void setVersion()
    {
        PackageInfo pInfo = null;
        String version = "";

        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(),0);
            version = getString(R.string.version)+" "+ pInfo.versionName +" "
                    +getString(R.string.build)+" "+ String.valueOf(pInfo.versionCode);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        tvVersion.setText(version);
    }

    private void setupAnimation()
    {
        //slideInBottom = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);

        DisplayMetrics gDm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(gDm);
        Convert.mDensity = getResources().getDisplayMetrics().density;
        int height = (gDm.heightPixels - Convert.dpToPx(25))/2;

        slideInBottom = new TranslateAnimation(0,
                0,
                gDm.heightPixels - Convert.dpToPx(25),
                0);
        slideInBottom.setDuration(2000);
        slideInBottom.setFillAfter(true);

        //slideOutTop = AnimationUtils.loadAnimation(this, R.anim.slide_out_top);
        slideOutTop = new TranslateAnimation(0,
                0,
                0,
                -height);
        slideOutTop.setDuration(2000);
        slideOutTop.setFillAfter(true);

        slideInBottom.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                llControllers.setVisibility(View.VISIBLE);
                ivMenu.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void animationEndAction()
    {
        DisplayMetrics gDm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(gDm);
        Convert.mDensity = getResources().getDisplayMetrics().density;

        ivBg1.setVisibility(View.VISIBLE);

        llControllers.setVisibility(View.VISIBLE);
        ivMenu.setVisibility(View.VISIBLE);
    }

    private void setupListener()
    {
        ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(drawerLinear);
            }
        });
        ivCloseMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(drawerLinear);
            }
        });
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginActivity();
            }
        });
        llTollCalculator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTollCalculatorActivity();
            }
        });
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TollRoadsApp.getInstance().gFromOTT = false;
                gotoActivity(v.getContext(), SignUpAccountInfoActivity.class);
            }
        });
        tvPayTollNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TollRoadsApp.getInstance().ottTrips.clear();
                TollRoadsApp.getInstance().ottTotalAmount = 0.0;
                TollRoadsApp.getInstance().gVehicleFound = Constants.VEHICLE_FOUND_TYPE_NOT_EXIST;

                TollRoadsApp.getInstance().gShowOTTCaching = false;

                //TollRoadsApp.getInstance().gOTTUserInfoRequest = new OTTUserInfoRequest();
                gotoActivity(v.getContext(), OTTVehicleInfoActivity.class  //OTTCalculateTollActivity.class
                    );
            }
        });
        tvPayViolation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showViolationLoginActivity();
            }
        });
    }

    private void setupMenuList() {
        menuItems = new ArrayList<MenuItem>();
        
        MenuItem compareAccountsItem = new MenuItem();
        compareAccountsItem.setName(getString(R.string.compare_accounts));
        MenuItem helpItem = new MenuItem();
        helpItem.setName(getString(R.string.help));
        MenuItem feedbackItem = new MenuItem();
        feedbackItem.setName(getString(R.string.feedback));
        MenuItem privacyItem = new MenuItem();
        privacyItem.setName(getString(R.string.privacy));
        MenuItem termOfUseItem = new MenuItem();
        termOfUseItem.setName(getString(R.string.term_of_use));
        
        menuItems.add(compareAccountsItem);
        menuItems.add(helpItem);
        menuItems.add(feedbackItem);
        menuItems.add(privacyItem);
        menuItems.add(termOfUseItem);

        lvMenu.setAdapter(new MenuItemAdapter(this, -1, menuItems));
        
        lvMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position) {
                    case 0:
                        showCompareAccountsActivity();
                        break;
                    case 1:
                        showHelpActivity();
                        break;
                    case 2:
                        showFeedbackActivity();
                        break;
                    case 3:
                        showPrivacyActivity();
                        break;
                    case 4:
                        showTermsActivity();
                        break;

                    default:
                        break;
                }
                drawer.closeDrawer(drawerLinear);
            }
        });
    }

    private void showCompareAccountsActivity() {
        Bundle bundle = new Bundle();
        bundle.putString(Resource.KEY_URL, Resource.COMPARE_ACCOUNT_URL);
        bundle.putString(Resource.KEY_TITLE, getString(R.string.compare_accounts));

        gotoActivity(this,WebActivity.class, bundle);
    }
    private void showHelpActivity() {
        Intent intent = new Intent();
        intent.setClass(this, HelpActivity.class);

        startActivity(intent);
    }
    private void showFeedbackActivity() {
        Intent intent = new Intent();
        intent.setClass(this, FeedbackActivity.class);

        startActivity(intent);
    }
    private void showPrivacyActivity() {
        Bundle bundle = new Bundle();
        bundle.putString(Resource.KEY_URL, Resource.PRIVACY_URL);
        bundle.putString(Resource.KEY_TITLE, getString(R.string.privacy));

        gotoActivity(this,WebActivity.class, bundle);
    }
    private void showTermsActivity() {
        Bundle bundle = new Bundle();
        bundle.putString(Resource.KEY_URL, Resource.TERMS_URL);
        bundle.putString(Resource.KEY_TITLE, getString(R.string.term_of_use));

        gotoActivity(this,WebActivity.class, bundle);
    }

    private void showLoginActivity() {
        Intent intent = new Intent();
        intent.setClass(this, LoginActivity.class);
        //intent.setClass(this, RecentInvoicesActivity.class);

        startActivity(intent);
    }

    private void showTollCalculatorActivity() {
        Intent intent = new Intent();
        intent.setClass(this, TollCalculatorActivity.class);

        startActivity(intent);
    }

    private void showViolationLoginActivity() {
        Intent intent = new Intent();
        intent.setClass(this, ViolationLogInActivity.class);
        //intent.setClass(this, RecentInvoicesActivity.class);

        startActivity(intent);
    }
}
