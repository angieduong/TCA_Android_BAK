package com.seta.tollroaddroid.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.seta.tollroaddroid.app.api.Resource;
import com.seta.tollroaddroid.app.json.SecQuestion;
import com.seta.tollroaddroid.app.json.SignUpRequest;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class SignUpSecurityActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack, ivPinHint;
    private TextView tvGoNext;
    private Response.ErrorListener errorListener;

    private Spinner spSecQuestion1, spSecQuestion2, spSecQuestion3;
    private EditText etAnswer1, etAnswer2, etAnswer3, etPin;
    private ArrayList<SecQuestion> secQuestionList;
    private int secQuestionIndex1=0, secQuestionIndex2=1, secQuestionIndex3=2;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("CurrentSignUpReq", TollRoadsApp.getInstance().gSignUpRequest);
    }

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit SignUp_Security_"+TollRoadsApp.getInstance().getSignUpAccountType()+" page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_security);
        if(savedInstanceState != null)
        {
            TollRoadsApp.getInstance().gSignUpRequest = (SignUpRequest) savedInstanceState.getSerializable("CurrentSignUpReq");
        }
        FlurryAgent.logEvent("Enter SignUp_Security_"+TollRoadsApp.getInstance().getSignUpAccountType()+" page.");

        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        tvGoNext = (TextView) findViewById(R.id.tv_go_next);
        ivPinHint = (ImageView)findViewById(R.id.iv_pin_hint);

        spSecQuestion1 = (Spinner)findViewById(R.id.sp_question_1);
        spSecQuestion2 = (Spinner)findViewById(R.id.sp_question_2);
        spSecQuestion3 = (Spinner)findViewById(R.id.sp_question_3);

        etAnswer1 = (EditText)findViewById(R.id.et_answer_1);
        etAnswer2 = (EditText)findViewById(R.id.et_answer_2);
        etAnswer3 = (EditText)findViewById(R.id.et_answer_3);

        etPin = (EditText)findViewById(R.id.et_pin);

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

        getSecQuestions();
        setupListener();
    }

    private void setupListener()
    {
        ivPinHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToastMessage(getString(R.string.pin_hint));
            }
        });

        spSecQuestion1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != secQuestionIndex1) {
                    secQuestionIndex1 = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spSecQuestion2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != secQuestionIndex2) {
                    secQuestionIndex2 = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spSecQuestion3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != secQuestionIndex3) {
                    secQuestionIndex3 = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(v.getContext(), SignUpAccountInfoActivity.class,
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            }
        });

        tvGoNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation()) {
                    setRequest();
                    gotoActivity(v.getContext(), SignUpContactInfoActivity.class, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            gotoActivity(getApplicationContext(), SignUpAccountInfoActivity.class,
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            return  true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setRequest()
    {
        SignUpRequest signUpRequest = TollRoadsApp.getInstance().gSignUpRequest;

        if(!etPin.getText().toString().isEmpty())
        {
            signUpRequest.setAccount_pin(Uri.encode(etPin.getText().toString()));
        }

        if(secQuestionList != null) {
            if (!etAnswer1.getText().toString().isEmpty()) {
                if (secQuestionIndex1 < secQuestionList.size()) {
                    signUpRequest.setSecQuestion1ID(secQuestionList.get(secQuestionIndex1).getQuestion_id());
                }
                signUpRequest.setSecAnswer1(Uri.encode(etAnswer1.getText().toString()));
            }

            if (!etAnswer2.getText().toString().isEmpty()) {
                if (secQuestionIndex2 < secQuestionList.size()) {
                    signUpRequest.setSecQuestion2ID(secQuestionList.get(secQuestionIndex2).getQuestion_id());
                }
                signUpRequest.setSecAnswer2(Uri.encode(etAnswer2.getText().toString()));
            }

            if (!etAnswer3.getText().toString().isEmpty()) {
                if (secQuestionIndex3 < secQuestionList.size()) {
                    signUpRequest.setSecQuestion3ID(secQuestionList.get(secQuestionIndex3).getQuestion_id());
                }
                signUpRequest.setSecAnswer3(Uri.encode(etAnswer3.getText().toString()));
            }
        }
    }

    private boolean checkValidation()
    {
        boolean ret = true;
        if(etPin.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.pin_empty_warning));
        }
        else {
            if (etAnswer1.getText().length() == 0) {
                ret = false;
                showToastMessage(getString(R.string.answer1_empty_warning));
            } else if (etAnswer2.getText().length() == 0) {
                ret = false;
                showToastMessage(getString(R.string.answer2_empty_warning));
            } else if (etAnswer3.getText().length() == 0) {
                ret = false;
                showToastMessage(getString(R.string.answer3_empty_warning));
            }
        }
        return ret;
    }

    private int getSecQuestionIndex(String id)
    {
        int index = 0;
        if(id != null && !id.isEmpty() && secQuestionList != null) {
            for (int i =0; i< secQuestionList.size(); i++) {
                if(secQuestionList.get(i).getQuestion_id().equals(id))
                {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }
    private void getSecQuestions()
    {
        showProgressDialog();
        Response.Listener listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                closeProgressDialog();
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, response);

                        response = response.replace(",}","}");
                        //if(checkResponse(response)) 
                        {
                            Gson gson = new GsonBuilder().serializeNulls().create();
//                            logInInforResponse = gson.fromJson(response.toString(),
//                                    LogInInforResponse.class);
//                            secQuestionList = logInInforResponse.getSecurity_question_list();
                            secQuestionIndex1 = 0;
                            secQuestionIndex2= 1;
                            secQuestionIndex3= 2;
                            initWidgetValue();
                            setupListener();
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

        ServerDelegate.getSecQuestions(Resource.URL_ACCOUNT, listener, errorListener);
    }

    public class SecQuestionAdapter1 extends ArrayAdapter<String>{

        public SecQuestionAdapter1(Context context, int textViewResourceId,
                            List<String> objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getDropDownView(int position, View cnvtView, ViewGroup prnt){
            View mView = super.getView(position, cnvtView, prnt);

            TextView myText= (TextView)mView.findViewById(android.R.id.text1);

            if(position == secQuestionIndex1)
            {
                myText.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
            else
            {
                myText.setBackgroundColor(Color.WHITE);
            }

            ViewGroup.LayoutParams params = mView.getLayoutParams();
            if(position == secQuestionIndex2 || position == secQuestionIndex3) {
                params.height = 1;
            }
            else
            {
                params.height = -2;//Convert.dpToPx(36);
            }
            mView.setLayoutParams(params);

            return mView;
        }
    }
    
    private void initSecQuestionSpinner1()
    {
        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();

        for(int i =0; i< secQuestionList.size(); i++)
        {
            SecQuestion secQuestion = secQuestionList.get(i);

            categories.add(secQuestion.getQuestion_text());
        }

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new SecQuestionAdapter1(this, R.layout.multiline_spinner_dropdown_item, categories); //item_sec_question_spinner

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(R.layout.multiline_spinner_dropdown_item);

        // attaching data adapter to spinner
        spSecQuestion1.setAdapter(dataAdapter);
        spSecQuestion1.setSelection(secQuestionIndex1);
    }

    public class SecQuestionAdapter2 extends ArrayAdapter<String>{

        public SecQuestionAdapter2(Context context, int textViewResourceId,
                                   List<String> objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getDropDownView(int position, View cnvtView, ViewGroup prnt){
            View mView = super.getView(position, cnvtView, prnt);

            TextView myText= (TextView)mView.findViewById(android.R.id.text1);

            if(position == secQuestionIndex2)
            {
                myText.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
            else
            {
                myText.setBackgroundColor(Color.WHITE);
            }

            ViewGroup.LayoutParams params = mView.getLayoutParams();
            if(position == secQuestionIndex1 || position == secQuestionIndex3) {
                params.height = 1;
            }
            else
            {
                params.height = -2;//Convert.dpToPx(36);
            }
            mView.setLayoutParams(params);

            return mView;
        }
    }

    private void initSecQuestionSpinner2()
    {
        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();

        for(int i =0; i< secQuestionList.size(); i++)
        {
            SecQuestion secQuestion = secQuestionList.get(i);

            categories.add(secQuestion.getQuestion_text());
        }

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new SecQuestionAdapter2(this, R.layout.multiline_spinner_dropdown_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(R.layout.multiline_spinner_dropdown_item);

        // attaching data adapter to spinner
        spSecQuestion2.setAdapter(dataAdapter);
        spSecQuestion2.setSelection(secQuestionIndex2);
    }

    public class SecQuestionAdapter3 extends ArrayAdapter<String>{

        public SecQuestionAdapter3(Context context, int textViewResourceId,
                                   List<String> objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getDropDownView(int position, View cnvtView, ViewGroup prnt){
            View mView = super.getView(position, cnvtView, prnt);

            TextView myText= (TextView)mView.findViewById(android.R.id.text1);

            if(position == secQuestionIndex3)
            {
                myText.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }
            else
            {
                myText.setBackgroundColor(Color.WHITE);
            }

            ViewGroup.LayoutParams params = mView.getLayoutParams();
            if(position == secQuestionIndex2 || position == secQuestionIndex1) {
                params.height = 1;
            }
            else
            {
                params.height = -2;//Convert.dpToPx(36);
            }
            mView.setLayoutParams(params);

            return mView;
        }
    }

    private void initSecQuestionSpinner3()
    {
        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();

        for(int i =0; i< secQuestionList.size(); i++)
        {
            SecQuestion secQuestion = secQuestionList.get(i);

            categories.add(secQuestion.getQuestion_text());
        }

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new SecQuestionAdapter3(this, R.layout.multiline_spinner_dropdown_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(R.layout.multiline_spinner_dropdown_item);

        // attaching data adapter to spinner
        spSecQuestion3.setAdapter(dataAdapter);
        spSecQuestion3.setSelection(secQuestionIndex3);
    }
    private void initWidgetValue()
    {
        initSecQuestionSpinner1();
        initSecQuestionSpinner2();
        initSecQuestionSpinner3();
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
