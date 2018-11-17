package com.seta.tollroaddroid.app.utilities;

/**
 * Created by thomas on 2016-02-29.
 */
public class Constants {

    public static final int MIN_PASSWORD_LENGTH = 5;

    public static final int MAX_INTERVAL_FOR_RENTAL_VEHICLE = 6*24*60*60*1000; //6 DAYS

    public static final int ELECTRONIC_CHECK_TYPE = 5;
    public static final int CREDIT_CARD_TYPE = 6;
    public static final int GOOGLE_PAY_TYPE = 7;

    public final static int MAKE_PAYMENT_REQUEST_CODE = 100;
    public final static int NEW_PAYMENT_REQUEST_CODE = 101;
    public final static int UPDATE_PAYMENT_METHOD_REQUEST_CODE = 102;
    public final static int NEW_VEHICLE_REQUEST_CODE = 103;
    public final static int UPDATE_VEHICLE_METHOD_REQUEST_CODE = 104;
    public final static int ONE_TIME_PAYMENT = 101;

    //report transponder type
    public final static int REPORT_TRANSPONDER_LOST = 1;
    public final static int REPORT_TRANSPONDER_STOLEN = 2;
    public final static int REPORT_TRANSPONDER_REPLACE = 3;
    public final static int REPORT_TRANSPONDER_LOST_AND_REPLACE = 4;
    public final static int REPORT_TRANSPONDER_STOLEN_AND_REPLACE = 5;
    public final static int REPORT_TRANSPONDER_RETURN = 6;
    public final static int REPORT_TRANSPONDER_LOST_OR_STOLEN = 7;

//    Rate_Type Mappings:
//    1 = FasTrak
//    2 = ExpressAccount
//    3 = One-Time-Toll
    public final static int RATE_TYPE_FASTRAK = 1;
    public final static int RATE_TYPE_EXPRESSACCOUNT = 2;
    public final static int RATE_TYPE_OTT = 3;

    public final static String  FROM_OTT = "from_ott";

    public final static String tag_add_trip = "add_trip_req";

    //account type
    public final static int ACCOUNT_TYPE_FASTRAK_INDIVIDUAL = 1;
    public final static int ACCOUNT_TYPE_FASTRAK_COMMERCIAL = 2;
    public final static int ACCOUNT_TYPE_PREPAID_EXPRESS = 3;
    public final static int ACCOUNT_TYPE_CHARGE_EXPRESS = 4;
    public final static int ACCOUNT_TYPE_INVOICE_EXPRESS = 5;

    //vehicle found type in OTT
    public final static int VEHICLE_FOUND_TYPE_NOT_EXIST = 0;
    public final static int VEHICLE_FOUND_TYPE_EXIST = 1;
    public final static int VEHICLE_FOUND_TYPE_RENTAL = 2;

    //vehicle type
    public final static int VEHICLE_TYPE_INDIVIDUAL = 0;
    public final static int VEHICLE_TYPE_RENTAL = 1;

    public final static int UNKNOWN_LOC_ID = 999;
}
