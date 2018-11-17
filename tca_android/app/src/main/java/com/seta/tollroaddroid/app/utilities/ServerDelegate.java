package com.seta.tollroaddroid.app.utilities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.seta.tollroaddroid.app.TollRoadsApp;
import com.seta.tollroaddroid.app.api.Resource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by thomas on 2016-02-29.
 */
public class ServerDelegate {

    public static String getCommonUrlExtra()
    {
        String urlExtra = Resource.PARAM_TOKEN+"="+ TollRoadsApp.getInstance().getToken();
        urlExtra = urlExtra +"&"+Resource.PARAM_UNIQUE +"="+TollRoadsApp.getInstance().getUniqueID();
        urlExtra = urlExtra +"&"+Resource.PARAM_TIMESTAMP +"="+System.currentTimeMillis()/1000;
        urlExtra = urlExtra +"&"+Resource.PARAM_APP_VERSION+"="+TollRoadsApp.getInstance().getAppVersion();
        return  urlExtra;
    }

    public static void loginRequest(String loginRequestUrl,String loginParams, Response.Listener listener, Response.ErrorListener errorListener)
    {
        loginParams = loginParams +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_LOGIN;
        loginParams = loginParams +"&"+Resource.PARAM_APP_VERSION+"="+TollRoadsApp.getInstance().getAppVersion();
        loginParams = loginParams +"&"+Resource.PARAM_UNIQUE +"="+TollRoadsApp.getInstance().getUniqueID();
        loginParams = loginParams +"&"+Resource.PARAM_LANGUAGE +"="+Resource.LOCAL_LANGUAGE;
        loginParams = loginParams +"&"+Resource.PARAM_TIMESTAMP +"="+System.currentTimeMillis()/1000;

        final String params = loginParams;
        Log.i("login","Url:"+loginRequestUrl+",param:"+params);
        StringRequest loginReq = new StringRequest(Request.Method.POST,
                loginRequestUrl,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return params.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        loginReq.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(loginReq);
    }

    public static void sendAccountRequest(String url,Response.Listener listener, Response.ErrorListener errorListener)
    {
        url = url +"?"+getCommonUrlExtra();
        url = url +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_GET;
        Log.i("sendAccountRequest","Url:"+url);
        JsonObjectRequest accountReq = new JsonObjectRequest(Request.Method.GET,
                url,
                listener, errorListener) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        accountReq.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(accountReq);
    }

    public static void commonJsonArrayRequest(String url,Response.Listener listener, Response.ErrorListener errorListener)
    {
        JsonArrayRequest commonReq = new JsonArrayRequest(Request.Method.GET,
                url,
                listener, errorListener) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        commonReq.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(commonReq);
    }

    public static void commonJsonRequest(String url,Response.Listener listener, Response.ErrorListener errorListener)
    {
        JsonObjectRequest commonReq = new JsonObjectRequest(Request.Method.GET,
                url,
                listener, errorListener) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        commonReq.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(commonReq);
    }

    public static void getRecentTolls(String url,Response.Listener listener, Response.ErrorListener errorListener)
    {
        url = url + "?"+getCommonUrlExtra();
        url = url +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_GET_RECENT_TOLL_AND_FEES;
        Log.i("getRecentTolls", "Url:" + url);
        JsonObjectRequest recentTollsReq = new JsonObjectRequest(Request.Method.GET,
                url,
                listener, errorListener) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };

        recentTollsReq.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(recentTollsReq);
    }

    public static void getVehicles(String url,Response.Listener listener, Response.ErrorListener errorListener)
    {
        url = url + "?"+getCommonUrlExtra();
        url = url +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_LIST;
        Log.i("getVehicles", "Url:" + url);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,
                url,
                listener, errorListener) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void getRecentPayments(String url,Response.Listener listener, Response.ErrorListener errorListener)
    {
        url = url + "?"+getCommonUrlExtra();
        url = url +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_GET_RECENT_PAYMENT;
        Log.i("getRecentPayments", "Url:" + url);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,
                url,
                listener, errorListener) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }
    public static void getPaymentMethods(String url,Response.Listener listener, Response.ErrorListener errorListener)
    {
        url = url + "?"+getCommonUrlExtra();
        url = url +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_GETLIST;

        Log.i("getPaymentMethods","Url:"+url);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,
                url,
                listener, errorListener) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void getTransponders(String url,Response.Listener listener, Response.ErrorListener errorListener)
    {
        url = url + "?"+getCommonUrlExtra();
        url = url +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_GET;

        Log.i("getTransponders","Url:"+url);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,
                url,
                listener, errorListener) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void checkUserRequest(String url, String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_CHECK_USER_PASS;
        params = params +"&"+Resource.PARAM_TIMESTAMP +"="+System.currentTimeMillis()/1000;
        params = params +"&"+Resource.PARAM_UNIQUE +"="+TollRoadsApp.getInstance().getUniqueID();
        params = params +"&"+Resource.PARAM_APP_VERSION+"="+TollRoadsApp.getInstance().getAppVersion();
        final String checkUserNameParams = params;
        Log.i("checkUserRequest","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return checkUserNameParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void signUpRequest(String url,String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_ENROLL;
        params = params +"&"+Resource.PARAM_TIMESTAMP +"="+System.currentTimeMillis()/1000;
        params = params +"&"+Resource.PARAM_UNIQUE +"="+TollRoadsApp.getInstance().getUniqueID();
        params = params +"&"+Resource.PARAM_APP_VERSION+"="+TollRoadsApp.getInstance().getAppVersion();
        final String signUpRequestParams = params;
        Log.i("signUpRequest","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return signUpRequestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void makePaymentRequest(String url,String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        params = params +"&"+ getCommonUrlExtra();
        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_PAY;

        final String requestParams = params;
        Log.i("makePaymentRequest","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void updateRequest(String url, String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        params = params +"&"+ getCommonUrlExtra();
        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_UPDATE;

        final String requestParams = params;
        Log.i("updateRequest","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void deleteRequest(String url, String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        params = params +"&"+ getCommonUrlExtra();
        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_DELETE;

        final String requestParams = params;
        Log.i("deleteRequest","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void checkPlateRequest(String url,String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_VEH_INFO;//ACTION_CHECK_PLATE;
        params = params +"&"+Resource.PARAM_TIMESTAMP +"="+System.currentTimeMillis()/1000;
        params = params +"&"+Resource.PARAM_UNIQUE +"="+TollRoadsApp.getInstance().getUniqueID();
        params = params +"&"+Resource.PARAM_APP_VERSION+"="+TollRoadsApp.getInstance().getAppVersion();
        final String requestParams = params;
        Log.i("checkPlateRequest","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void OTTUserInfoRequest(String url,String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        params = params +"&"+ getCommonUrlExtra();
        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_USER_INFO;

        final String requestParams = params;
        Log.i("OTTUserInfoRequest","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }
    public static void addRentalDatesReq(String url,String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        params = params +"&"+ getCommonUrlExtra();
        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_ADD_RENTAL_DATES;

        final String requestParams = params;
        Log.i("addRentalDatesReq","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void addRequest(String url, String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        params = params +"&"+ getCommonUrlExtra();
        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_ADD;

        final String requestParams = params;
        Log.i("addRequest","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void requestTransponders(String url, String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        params = params +"&"+ getCommonUrlExtra();
        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_REQUEST_ADDITIONAL_TRANS;

        final String requestParams = params;
        Log.i("requestTransponders","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }
    public static void reportTransponders(String url, String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        params = params +"&"+ getCommonUrlExtra();
        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_REPORT;

        final String requestParams = params;
        Log.i("requestTransponders","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void getSecQuestions(String url,Response.Listener listener, Response.ErrorListener errorListener)
    {
        url = url + "?"+getCommonUrlExtra();
        url = url +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_SEC_QUESTIONS;
        Log.i("getSecQuestions", "Url:" + url);
        StringRequest req = new StringRequest(Request.Method.GET,
                url,
                listener, errorListener) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void updateLoginInfo(String url, String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        params = params +"&"+ getCommonUrlExtra();
        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_UPDATE_LOGIN_INFO;

        final String requestParams = params;
        Log.i("updateLoginInfo","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }
    public static void getLocList(String url,Response.Listener listener, Response.ErrorListener errorListener)
    {
        url = url + "?"+getCommonUrlExtra();

        url = url +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_LOC_LIST;
        Log.i("getLocList","Url:"+url);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,
                url,
                listener, errorListener) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void addTripReq(String url,String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        params = params +"&"+ getCommonUrlExtra();
        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_ADD_TRIP;

        final String requestParams = params;
        Log.i("addTripReq","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req, Constants.tag_add_trip);
    }

    public static void delTripReq(String url,String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        params = params +"&"+ getCommonUrlExtra();
        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_DEL_TRIP;

        final String requestParams = params;
        Log.i("delTripReq","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void getTripList(String url,Response.Listener listener, Response.ErrorListener errorListener)
    {
        url = url + "?"+getCommonUrlExtra();
        url = url +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_TRIP_LIST;
        Log.i("getTripList","Url:"+url);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,
                url,
                listener, errorListener) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void updateTripReq(String url,String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        params = params +"&"+ getCommonUrlExtra();
        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_UPDATE_TRIP;

        final String requestParams = params;
        Log.i("updateTripReq","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void ottPayReq(String url,String params, Response.Listener listener,
                                 Response.ErrorListener errorListener, boolean isRental)
    {
        params = params +"&"+ getCommonUrlExtra();
        if(isRental) {
            params = params + "&" + Resource.PARAM_ACTION + "=" + Resource.ACTION_PAY_METHOD;
        }
        else {
            params = params + "&" + Resource.PARAM_ACTION + "=" + Resource.ACTION_SAVE_OPTION_PAY;//Resource.ACTION_PAY;
        }
        final String requestParams = params;
        Log.i("ottPayReq","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void updateSecQuestionsReq(String url,String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        params = params +"&"+ getCommonUrlExtra();
        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_UPDATE_SEC_QUESTIONS;

        final String requestParams = params;
        Log.i("updateSecQuestionsReq","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }
    public static void logOutReq()
    {
        String params = getCommonUrlExtra();
        String url = Resource.URL_LOGOUT;

        final String requestParams = params;
        Log.i("logOutReq","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("logOutReq","response:"+response);
                    }
                }, null) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void calcRatesReq(String url,String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        params = params +"&"+ getCommonUrlExtra();
        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_CALC_RATES;

        final String requestParams = params;
        Log.i("calcRatesReq","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void forgotPasswordReq(String url,String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        params = params +"&"+ getCommonUrlExtra();
        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_GET_QUESTION_ID;

        final String requestParams = params;
        Log.i("forgotPasswordReq","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void answerSecQuestionsReq(String url,String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        params = params +"&"+ getCommonUrlExtra();
        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_CHECK_SEC_QUESTIONS;

        final String requestParams = params;
        Log.i("answerSecQuestionsReq","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void resetPasswordRequest(String url,String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        params = params +"&"+Resource.PARAM_UNIQUE +"="+TollRoadsApp.getInstance().getUniqueID();
        params = params +"&"+Resource.PARAM_TIMESTAMP +"="+System.currentTimeMillis()/1000;
        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_RESET_PASSWORD;
        params = params +"&"+Resource.PARAM_APP_VERSION+"="+TollRoadsApp.getInstance().getAppVersion();
        final String requestParams = params;
        Log.i("resetPasswordRequest","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void checkPayment(String url, String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        params = params +"&"+ getCommonUrlExtra();

        if(TollRoadsApp.getInstance().gVehicleFound == Constants.VEHICLE_FOUND_TYPE_RENTAL) {
            params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_CHECK_PAY_METHOD;
        }
        else {
            params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_CHECK_PAYMENT;
        }

        final String requestParams = params;

        Log.i("checkPayment","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void checkPayTag(String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        String url = Resource.URL_ENROLLMENT;

        //params = params +"&"+ getCommonUrlExtra();

        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_CHECK_PAY_TAG;
        params = params +"&"+Resource.PARAM_TIMESTAMP +"="+System.currentTimeMillis()/1000;
        params = params +"&"+Resource.PARAM_UNIQUE +"="+TollRoadsApp.getInstance().getUniqueID();
        params = params +"&"+Resource.PARAM_APP_VERSION+"="+TollRoadsApp.getInstance().getAppVersion();
        final String requestParams = params;

        Log.i("checkPayTag","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void checkVehicle(String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        String url = Resource.URL_ENROLLMENT;

        //params = params +"&"+ getCommonUrlExtra();

        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_CHECK_VEHICLE;
        params = params +"&"+Resource.PARAM_TIMESTAMP +"="+System.currentTimeMillis()/1000;
        params = params +"&"+Resource.PARAM_UNIQUE +"="+TollRoadsApp.getInstance().getUniqueID();

        final String requestParams = params;

        Log.i("checkVehicle","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void checkContact(String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        String url = Resource.URL_ENROLLMENT;

        //params = params +"&"+ getCommonUrlExtra();
        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_CHECK_CONTACT;
        params = params +"&"+Resource.PARAM_TIMESTAMP +"="+System.currentTimeMillis()/1000;
        params = params +"&"+Resource.PARAM_UNIQUE +"="+TollRoadsApp.getInstance().getUniqueID();

        final String requestParams = params;

        Log.i("checkContact","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void oneTimePaymentRequest(String url, String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        params = params +"&"+ getCommonUrlExtra();
        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_NEW_PAY;

        final String requestParams = params;
        Log.i("oneTimePaymentRequest","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }
    public static String getFormattedDateFromTimestamp(long timestampInMilliSeconds) {
        Date date = new Date();
        date.setTime(timestampInMilliSeconds);
        String formattedDate = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss").format(date);
        return formattedDate;
    }

    public static void appendLog(String logString) {
        Log.i("logtag", "Environment.getExternalStorageDirectory().getAbsolutePath() = " + Environment.getExternalStorageDirectory().getAbsolutePath());

        String packageName =  TollRoadsApp.getInstance().getPackageName();
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator  + "Android" + File.separator + "data" + File.separator + packageName + File.separator + "files";
        File myFilesDir = new File(dir);

        if(!myFilesDir.exists()) {
            myFilesDir.mkdirs();
        }

        File logFile = new File(myFilesDir.getAbsolutePath(), "log.file");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(getFormattedDateFromTimestamp(System.currentTimeMillis()) + ": " + logString);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void sendLog(Context context)
    {
        String packageName =  TollRoadsApp.getInstance().getPackageName();
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator  + "Android" + File.separator + "data" + File.separator + packageName + File.separator + "files";
        File myFilesDir = new File(dir);

        if(!myFilesDir.exists()) {
            myFilesDir.mkdirs();
        }

        File logFile = new File(myFilesDir.getAbsolutePath(), "log.file");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        Uri path = Uri.fromFile(logFile);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"thomas.huang@greenowlmobile.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Toll Roads Logs");
        intent.putExtra(Intent.EXTRA_TEXT, "Device Info:\n" +
                DeviceManager.getInstance().getDeviceInfo(true) + "\n");

        // the attachment
        intent.putExtra(Intent.EXTRA_STREAM, path);

        context.startActivity(Intent.createChooser(intent, "Send Logs"));
    }

    public static void getRecentInvoices(String url,Response.Listener listener, Response.ErrorListener errorListener)
    {
        url = url + "?"+getCommonUrlExtra();
        url = url +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_GET_RECENT_INVOICE;
        Log.i("getRecentTolls", "Url:" + url);
        JsonObjectRequest recentTollsReq = new JsonObjectRequest(Request.Method.GET,
                url,
                listener, errorListener) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };

        recentTollsReq.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(recentTollsReq);
    }

    public static void sendEmailInvoiceRequest(String url,Response.Listener listener,
                                        Response.ErrorListener errorListener, String invoiceID)
    {
        url = url + "?"+getCommonUrlExtra();
        url = url +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_EMAIL_INVOICE;
        url = url +"&"+Resource.PARAM_INVOICE_ID +"="+invoiceID;
        Log.i("getRecentTolls", "Url:" + url);
        JsonObjectRequest recentTollsReq = new JsonObjectRequest(Request.Method.GET,
                url,
                listener, errorListener) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };

        recentTollsReq.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(recentTollsReq);
    }

    public static void getStatement(String url,Response.Listener listener, Response.ErrorListener errorListener,
                                    String startDate,String endDate)
    {
        url = url + "?"+getCommonUrlExtra();
        url = url +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_GENERATE_STATEMENT;
        url = url +"&"+Resource.PARAM_START_DATE +"="+startDate;
        url = url +"&"+Resource.PARAM_END_DATE +"="+endDate;
        Log.i("getStatement", "Url:" + url);
        JsonObjectRequest recentTollsReq = new JsonObjectRequest(Request.Method.GET,
                url,
                listener, errorListener) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };

        recentTollsReq.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(recentTollsReq);
    }
    public static void sendEmailStatementRequest(String url,Response.Listener listener,
                         Response.ErrorListener errorListener, String startDate,String endDate)
    {
        url = url + "?"+getCommonUrlExtra();
        url = url +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_EMAIL_STATEMENT;
        url = url +"&"+Resource.PARAM_START_DATE +"="+startDate;
        url = url +"&"+Resource.PARAM_END_DATE +"="+endDate;
        Log.i("getRecentTolls", "Url:" + url);
        JsonObjectRequest recentTollsReq = new JsonObjectRequest(Request.Method.GET,
                url,
                listener, errorListener) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };

        recentTollsReq.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(recentTollsReq);
    }

    public static void calcTripInfo(String url, String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        params = params +"&"+ getCommonUrlExtra();
        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_CALC_TRIP_INFO;

        final String requestParams = params;

        Log.i("checkPayment","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void calcSaveOptionRequest(String url, String params, Response.Listener listener, Response.ErrorListener errorListener)
    {
        params = params +"&"+ getCommonUrlExtra();
        params = params +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_CALC_SAVE_OPTION;

        final String requestParams = params;

        Log.i("calcSaveOptionRequest","Url:"+url+",param:"+params);
        StringRequest req = new StringRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public byte[] getBody() {

                return requestParams.getBytes();
            }
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        req.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(req);
    }

    public static void getUnpaidViolationRequest(String url,Response.Listener listener, Response.ErrorListener errorListener)
    {
        url = url +"?"+getCommonUrlExtra();
        url = url +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_UNPAID_VIOLATION;
        Log.i("volley","getUnpaidViolationRequest Url:"+url);
        JsonObjectRequest accountReq = new JsonObjectRequest(Request.Method.GET,
                url,
                listener, errorListener) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        accountReq.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(accountReq);
    }

    public static void commonGetRequest(String url,Response.Listener listener, Response.ErrorListener errorListener)
    {
//        url = url +"?"+getCommonUrlExtra();
//        url = url +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_UNPAID_VIOLATION;
        Log.i("volley","commonGetRequest Url:"+url);
        JsonObjectRequest accountReq = new JsonObjectRequest(Request.Method.GET,
                url,
                listener, errorListener) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        accountReq.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(accountReq);
    }

    public static void commonPostRequest(String url,Response.Listener listener, Response.ErrorListener errorListener)
    {
//        url = url +"?"+getCommonUrlExtra();
//        url = url +"&"+Resource.PARAM_ACTION +"="+Resource.ACTION_UNPAID_VIOLATION;
        Log.i("volley","commonGetRequest Url:"+url);
        JsonObjectRequest accountReq = new JsonObjectRequest(Request.Method.POST,
                url,
                listener, errorListener) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded";
            }
        };
        accountReq.setRetryPolicy(
                new DefaultRetryPolicy(
                        500000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );
        // Adding request to request queue
        TollRoadsApp.getInstance().addToRequestQueue(accountReq);
    }
}
