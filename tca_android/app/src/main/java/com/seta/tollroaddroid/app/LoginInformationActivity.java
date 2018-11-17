package com.seta.tollroaddroid.app;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.seta.tollroaddroid.app.json.LogInInforResponse;
import com.seta.tollroaddroid.app.json.SecQuestion;
import com.seta.tollroaddroid.app.utilities.ServerDelegate;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class LoginInformationActivity extends BaseActivity {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private ImageView ivGoBack,ivPinHint;
    private TextView tvSave;
    private Response.ErrorListener errorListener;
    private LogInInforResponse logInInforResponse;
    private Spinner spSecQuestion1, spSecQuestion2, spSecQuestion3;
    private EditText etUserName, etCurrentPassword, etNewPassword, etVerifyPassword;
    private EditText etAnswer1, etAnswer2, etAnswer3, etPin;
    private ArrayList<SecQuestion> secQuestionList;
    private int secQuestionIndex1,secQuestionIndex2,secQuestionIndex3;

    @Override
    protected void onDestroy() {
        FlurryAgent.logEvent("Exit Account_LogIn page.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_information);
        FlurryAgent.logEvent("Enter Account_LogIn page.");

        tvSave = (TextView)findViewById(R.id.tv_save);
        ivGoBack = (ImageView)findViewById(R.id.iv_go_back);
        ivPinHint = (ImageView)findViewById(R.id.iv_pin_hint);
        etUserName = (EditText)findViewById(R.id.et_user_name);
        etCurrentPassword = (EditText)findViewById(R.id.et_current_password);
        etNewPassword = (EditText)findViewById(R.id.et_password);
        etVerifyPassword = (EditText)findViewById(R.id.et_re_type);

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
        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getSecQuestions();
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
                    tvSave.setVisibility(View.VISIBLE);
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
                    tvSave.setVisibility(View.VISIBLE);
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
                    tvSave.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                tvSave.setVisibility(View.VISIBLE);
            }
        };
        etUserName.addTextChangedListener(textWatcher);
        etPin.addTextChangedListener(textWatcher);
        etAnswer1.addTextChangedListener(textWatcher);
        etAnswer2.addTextChangedListener(textWatcher);
        etAnswer3.addTextChangedListener(textWatcher);
        etCurrentPassword.addTextChangedListener(textWatcher);
        etNewPassword.addTextChangedListener(textWatcher);
        etVerifyPassword.addTextChangedListener(textWatcher);
        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidation()) {
                    String params = populateParams();
                    showProgressDialog();
                    updateLogInInfo(Resource.URL_ACCOUNT, params);
                }
            }
        });
    }

    private String populateParams()
    {
        String params = "currentPwd=" + Uri.encode(etCurrentPassword.getText().toString());

        params = params + "&username=" + Uri.encode(etUserName.getText().toString());

        if(!etNewPassword.getText().toString().isEmpty()) {
            params = params + "&newPwd="+ Uri.encode(etNewPassword.getText().toString());
        }

        if(!etPin.getText().toString().isEmpty())
        {
            params = params + "&pin="+ Uri.encode(etPin.getText().toString());
        }

        if(!etAnswer1.getText().toString().isEmpty()) {
            if(secQuestionIndex1 < secQuestionList.size())
            {
                params = params + "&secQuestion1ID="+ 
                        secQuestionList.get(secQuestionIndex1).getQuestion_id();
            }
            params = params + "&secAnswer1="+ Uri.encode(etAnswer1.getText().toString());
        }

        if(!etAnswer2.getText().toString().isEmpty()) {
            if(secQuestionIndex2 < secQuestionList.size())
            {
                params = params + "&secQuestion2ID="+
                        secQuestionList.get(secQuestionIndex2).getQuestion_id();
            }
            params = params + "&secAnswer2="+ Uri.encode(etAnswer2.getText().toString());
        }

        if(!etAnswer3.getText().toString().isEmpty()) {
            if(secQuestionIndex3 < secQuestionList.size())
            {
                params = params + "&secQuestion3ID="+
                        secQuestionList.get(secQuestionIndex3).getQuestion_id();
            }
            params = params + "&secAnswer3="+ Uri.encode(etAnswer3.getText().toString());
        }
        return  params;
    }

    private void updateLogInInfo(String url,String params)
    {
        Response.Listener listener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                closeProgressDialog();
                try
                {
                    if(response != null) {
                        Log.d(LOG_TAG, "response:" + response);

                        if(checkResponse(response)) {
                            if(TollRoadsApp.getInstance().getRememberLogIn()
                                    && TollRoadsApp.getInstance().getUserName().equals(logInInforResponse.getUsername()))
                            {
                                TollRoadsApp.getInstance().setUserName(etUserName.getText().toString());
                            }

                            showToastMessage(getString(R.string.login_information_saved));

                            setResult(RESULT_OK);
                            finish();
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

        ServerDelegate.updateLoginInfo(url, params, listener, errorListener);
    }

    private boolean checkValidation()
    {
        boolean ret = true;
        if(etUserName.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.user_name_empty_warning));
        }
        else if(etCurrentPassword.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.current_password_empty_warning));
        }
        else if(etNewPassword.getText().length() != 0 &&
                !etNewPassword.getText().toString().equals(etVerifyPassword.getText().toString()))
        {
            ret = false;
            showToastMessage(getString(R.string.password_not_match_warning));
        }
        else if(etPin.getText().length() == 0)
        {
            ret = false;
            showToastMessage(getString(R.string.pin_empty_warning));
        }
        else if(etAnswer1.getText().length() != 0 || etAnswer2.getText().length() != 0 ||
                etAnswer3.getText().length() != 0 ) {
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
                        if(checkResponse(response)) {
                            Gson gson = new GsonBuilder().serializeNulls().create();
                            logInInforResponse = gson.fromJson(response.toString(),
                                    LogInInforResponse.class);
                            secQuestionList = logInInforResponse.getSecurity_question_list();
                            secQuestionIndex1 = getSecQuestionIndex(logInInforResponse.getQuestion1ID());
                            secQuestionIndex2= getSecQuestionIndex(logInInforResponse.getQuestion2ID());
                            secQuestionIndex3= getSecQuestionIndex(logInInforResponse.getQuestion3ID());
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

//        ArrayAdapter<String> adapter = new SecQuestionAdapter1(this,R.layout.multiline_spinner_dropdown_item,categories);
//        adapter.setDropDownViewResource(R.layout.multiline_spinner_dropdown_item);
//        spSecQuestion1.setAdapter(adapter);


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
        if(logInInforResponse != null)
        {
            etUserName.setText(logInInforResponse.getUsername());
            initSecQuestionSpinner1();
            initSecQuestionSpinner2();
            initSecQuestionSpinner3();
            etPin.setText(logInInforResponse.getPin());
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
