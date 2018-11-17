package com.seta.tollroaddroid.app.api;

/**
 * Created by Thomas on 2015-12-15.
 */
public interface Resource {
    //public final static String APP_DOMAIN = "http://206.169.136.93/ws2/v2/"; //"http://206.169.136.93/customer/";//
    //public final static String APP_DOMAIN = "https://beta.thetollroads.com/ws2/v2/";//staging
    public final static String APP_DOMAIN = "https://secure.thetollroads.com/ws2/v2/"; //prod

    public static final String CALL_CENTER_NUMBER = "(949)727-4800";
    public static final String FEEDBACK_EMAIL = "mobileapp@thetollroads.com";//"apptestdrive@thetollroads.com";//"support@tca.com";
    public static final String PLAY_STORE_LINK = "market://details?id=com.seta.tollroaddroid.app";
    public static final String PRIVACY_URL = "http://www.thetollroads.com/footer/privacy";
    public static final String TERMS_URL = "http://secure.thetollroads.com/mobileterms.html";
    public static final String LEGAL_URL = "http://secure.thetollroads.com/mobileterms.html";
    public static final String COMPARE_ACCOUNT_URL = "https://secure.thetollroads.com/customer/inc/011_16A_Compare_Accounts.pdf";//http://192.168.64.93/customer/inc/011_16A_Compare_Accounts.pdf";//http://www.thetollroads.com/accounts/compare";
    public static final String SIGN_UP_AGREEMENT_URL = "https://secure.thetollroads.com/customer/html/acct_license_agreement.html";//http://www.thetollroads.com/sites/default/files/licence_agreement_feb_2014.pdf";
    public static final String FAQ_URL = "http://www.thetollroads.com/help/faq";
    public static final String SERVICE_CENTERS__URL = "http://www.thetollroads.com/help/service-centers";

    public static final double CENTER_LATITUDE = 33.824536;
    public static final double CENTER_LONGITUDE = -117.716594;
    public static final int DEFAULT_ZOOM_LEVEL = 10;

    public final static String LOCAL_LANGUAGE = "en";

    public static String URL_LOGIN = APP_DOMAIN + "login";

    public static String URL_VIOLATION_LOGIN = APP_DOMAIN + "vioLogin";

    public static String URL_VIOLATION_PAYMENT = APP_DOMAIN + "vioPayment";

    public static String URL_ACCOUNT = APP_DOMAIN + "account";

    public static String URL_RESET_PWD = APP_DOMAIN + "reset";

    public static String URL_LOGOUT = APP_DOMAIN + "logout";

    public static String URL_VEHICLE = APP_DOMAIN + "vehicle";

    public static String URL_TRANSPONDER = APP_DOMAIN + "transponder";

    public static String URL_PAYMENT = APP_DOMAIN + "payment";

    public static String URL_LOCATION = APP_DOMAIN + "map";

    public static String URL_PAY_GO = APP_DOMAIN + "payGo2";

    public static String URL_ROUTES = "https://www.thetollroads.com/ontheroads/toll-calculator/routes/getRoutes";

    public static String URL_RATES = "https://www.thetollroads.com/ontheroads/toll-calculator/rates/getRates";

    public static String URL_ENROLLMENT = APP_DOMAIN + "enrollment";

    public static String PARAM_TOKEN = "tokenID";

    public static String PARAM_UNIQUE = "uniqueID";

    public static String PARAM_CLOSE_DATE = "close_date";

    public static String PARAM_USER_NAME = "username";

    public static String PARAM_PASSWORD = "password";

    public static String PARAM_PASSWORD_CURRENT = "currentPwd";

    public static String PARAM_PASSWORD_NEW = "newPwd";

    public static String PARAM_PRIVATE_VALUE = "privateVal";

    public static String PARAM_ACTION = "action";

    public static String PARAM_TIMESTAMP = "timestamp";

    public static String PARAM_VEHICLE_ID = "vehicle_id";

    public static String PARAM_VEHICLE_TYPE = "vehicle_type";

    public static String PARAM_PLATE = "plate";

    public static String PARAM_YEAR = "year";

    public static String PARAM_STATE = "state";

    public static String PARAM_MAKE = "make";

    public static String PARAM_COLOR = "color";

    public static String PARAM_MODEL = "model";

    public static String PARAM_START_DATE = "start_date";

    public static String PARAM_END_DATE = "end_date";

    public static String PARAM_TRANSPONDER_NUMBER = "transponder_number";

    public static String PARAM_TYPE = "type";

    public static String PARAM_QUANTITY = "quantity";

    public static String PARAM_ACCOUNT_ADDRESS1 = "address1";

    public static String PARAM_ACCOUNT_ADDRESS2 = "address2";

    public static String PARAM_ACCOUNT_ADDRESS3 = "address3";

    public static String PARAM_ACCOUNT_ADDRESS_CONTACT = "address_contact";

    public static String PARAM_ACCOUNT_ADDRESS_CITY = "address_city";

    public static String PARAM_ACCOUNT_ADDRESS_STATE = "address_state";

    public static String PARAM_ACCOUNT_ZIPCODE = "zipcode";

    public static String PARAM_ACCOUNT_TYPE = "type";

    public static String PARAM_ACCOUNT_FAX_PHONE = "fax_phone";

    public static String PARAM_ACCOUNT_HOME_PHONE = "home_phone";

    public static String PARAM_ACCOUNT_WORK_PHONE = "work_phone";

    public static String PARAM_ACCOUNT_CELL_PHONE = "cell_phone";

    public static String PARAM_ACCOUNT_EMAIL_ADDRESS = "email_address";

    public static String PARAM_ACCOUNT_STATEMENT_DELIVERY_METHOD = "statement_delivery_method";

    public static String PARAM_ACCOUNT_RECEIVE_PROMOTION_MATERIAL = "receive_promotion_material";

    public static String PARAM_PAYMENT_ID = "paymethod_id";

    public static String PARAM_PAYMENT_TYPE = "payment_type";

    public static String PARAM_PAYMENT_METHOD = "payment_method";

    public static String PARAM_PAYMENT_ORDER = "payment_order";

    public static String PARAM_ROUTING_NUMBER = "routing_number";

    public static String PARAM_ACCOUNT_NUMBER = "account_number";

    public static String PARAM_PAYMENT_FIRST_NAME = "first_name";

    public static String PARAM_PAYMENT_LAST_NAME = "last_name";

    public static String PARAM_CARD_NUMBER = "card_number";

    public static String PARAM_CARD_HOLDER_NAME = "card_holder_name";

    public static String PARAM_EXPIRED_DATE = "expired_date";

    public static String PARAM_PAYMENT_ZIPCODE = "zip_code";

    public static String PARAM_AMOUNT = "amount";

    public static String PARAM_ACTION_PAY = "pay";

    public static String PARAM_ACTION_NEWPAY = "newpay";

    public static String PARAM_CVV2 = "cvv2";

    public static String PARAM_CHECK_USERNAME = "account_username";

    public static String PARAM_TOLL_DATE = "start_date";

    public static String PARAM_TOLL_ROAD = "toll_road";

    public static String PARAM_FROM_LOC = "from_loc_id";

    public static String PARAM_TO_LOC = "to_loc_id";

    public static String PARAM_TOLLROAD = "toll_road";

    public static String PARAM_DIR_ID = "dir_id";

    public static String PARAM_LOC_ID = "loc_id";

    public static String PARAM_LOC_NAME = "loc_name";

    public static String PARAM_ROAD_NAME = "road_name";

    public static String PARAM_LANGUAGE = "language";

    public static String PARAM_APP_VERSION = "appVersion";

    public static String PARAM_TRIP_NUM = "trip_num";

    public static String PARAM_TRIP_DATE = "trip_date";

    public static String PARAM_TRIP_AMOUNT = "trip_amount";

    public static String PARAM_INVOICE_ID = "invoice_id";

    public static String ACTION_REPORT = "report";

    public static String ACTION_UPDATE = "update";

    public static String ACTION_UPDATE_LOGIN_INFO = "updateLoginInfo";

    public static String ACTION_REPLACE = "replace";

    public static String ACTION_GET_BALANCE = "balance";

    public static String ACTION_GET = "get";

    public static String ACTION_GETLIST = "getlist";
    public static String ACTION_LIST = "list";

    public static String ACTION_GET_NUM_OF_TRANS = "numoftrans";

    public static String ACTION_GET_RECENT_TOLL_AND_FEES = "recenttoll";

    public static String ACTION_GET_RECENT_PAYMENT = "recentpay";

    public static String ACTION_GET_RECENT_INVOICE = "recentinv";

    public static String ACTION_REQUEST_ADDITIONAL_TRANS = "reqtrans";

    public static String ACTION_DELETE = "delete";

    public static String ACTION_CHECK_USERNAME = "checkUsername";

    public static String ACTION_CHECK_USER_PASS = "checkUserPass";

    public static String ACTION_LOGIN = "login";

    public static String ACTION_ENROLL = "enroll";

    public static String ACTION_PAY = "pay";

    public static String ACTION_CHECK_PLATE = "checkPlate";

    public static String ACTION_USER_INFO = "userinfo";

    public static String ACTION_ADD_RENTAL_DATES = "addRentalDates";

    public static String ACTION_VEH_INFO = "vehinfo";

    public static String ACTION_ADD = "add";

    public static String ACTION_SEC_QUESTIONS = "secQuestions";

    public static String ACTION_LOC_LIST = "loclist";

    public static String ACTION_ADD_TRIP = "addtrip";

    public static String ACTION_DEL_TRIP = "deltrip";

    public static String ACTION_TRIP_LIST = "triplist";

    public static String ACTION_UPDATE_TRIP = "updatetrip";

    public static String ACTION_UPDATE_SEC_QUESTIONS = "updateSecQuestions";

    public static String ACTION_CALC_RATES = "calcRates";

    public static String ACTION_GET_QUESTION_ID = "getQuestionIDs";

    public static String ACTION_CHECK_SEC_QUESTIONS = "checkSecAnswers";

    public static String ACTION_RESET_PASSWORD = "resetPassword";

    public static String ACTION_CHECK_PAY_METHOD= "checkPayMethod";

    public static String ACTION_CHECK_PAYMENT = "checkPayment";

    public static String ACTION_CHECK_PAY_TAG = "checkPayTag";

    public static String ACTION_CHECK_VEHICLE = "checkVehicle";

    public static String ACTION_CHECK_CONTACT = "checkContact";

    public static String ACTION_CHECK_PAY = "checkPay";

    public static String ACTION_PAY_METHOD= "payMethod";

    public static String ACTION_NEW_PAY = "newpay";

    public static String ACTION_GENERATE_STATEMENT = "genstmt";

    public static String ACTION_EMAIL_STATEMENT = "emailstmt";

    public static String ACTION_CALC_TRIP_INFO = "calcTripInfo2";

    public static String ACTION_CALC_SAVE_OPTION = "calcSaveOption";

    public static String ACTION_SAVE_OPTION_PAY = "saveOptionPay";

    public static String ACTION_PAY_SELECT_VIOLATION = "paySelVios";

    //JSON key
    public static String KEY_CONF_MESSAGE = "conf_message";

    public static String KEY_AMOUNT_DUE = "amount_due";
    public static String KEY_TRIP_NUM = "trip_num";
    public static String KEY_TRIP_AMOUNT = "trip_amount";

    public static String KEY_SUCCESS = "success";

    public static String KEY_ACCESS_TOKEN = "tokenID";

    public static String KEY_UNIQUE = "uniqueID";

    public static String KEY_MESSAGE = "message";

    public static String KEY_ACCOUNT_MESSAGE = "account_message";

    public static String KEY_ACCOUNT_NUMBER = "account_number";

    public static String KEY_STATUS = "status";

    public static String KEY_TRANSPONDER_NUMBER_TRANSPONDER = "number_transponder";

    public static String KEY_TRANSPONDER_NUMBER_ORDER = "number_order";

    public static String KEY_TRANSPONDER_LIST = "transponder_list";

    public static String KEY_TRANSPONDER_CODE = "transponder_code";

    public static String KEY_TRANSPONDER_ASSIGNED_DATE = "assigned_date";

    public static String KEY_TRANSPONDER_DEPOSIT_AMOUNT = "deposit_amount";

    public static String KEY_TRANSPONDER_NUMBER = "transponder_number";

    public static String KEY_INFO = "info";

    public static String KEY_INFO_LIST = "info_list";

    public static String KEY_ACCOUNT_TYPE = "account_type";

    public static String KEY_FUL_NAME = "full_name";

    public static String KEY_BALANCE = "balance";

    public static String KEY_BALANCE2 = "balance2";

    public static String KEY_CLOSEDATE = "close_date";

    public static String KEY_ADDRESS1 = "address1";

    public static String KEY_ADDRESS2 = "address2";

    public static String KEY_ADDRESS3 = "address3";

    public static String KEY_ADDRESS_CITY = "address_city";

    public static String KEY_ZIP_CODE = "zipcode";

    public static String KEY_ADDRESS_STATE = "address_state";

    public static String KEY_ADDRESS_CONTACT = "address_contact";

    public static String KEY_FAX_PHONE = "fax_phone";

    public static String KEY_WORK_PHONE = "work_phone";

    public static String KEY_CELL_PHONE = "cell_phone";

    public static String KEY_HOME_PHONE = "home_phone";

    public static String KEY_EMAIL_ADDRESS = "email_address";

    public static String KEY_STATEMENT_DELIVERY_METHOD = "statement_delivery_method";

    public static String KEY_RECEIVE_PROMOTION_MATERIAL = "receive_promotion_material";

    public static String KEY_VEHICLE_LIST = "vehicle_list";

    public static String KEY_SECURITY_QUESTION_LIST = "security_question_list";

    public static String KEY_VEHICLE_ID = "id";

    public static String KEY_VEHICLE_PLATE = "plate";

    public static String KEY_VEHICLE_STATE = "state";

    public static String KEY_VEHICLE_YEAR = "year";

    public static String KEY_VEHICLE_MAKE = "make";

    public static String KEY_VEHICLE_MODEL = "model";

    public static String KEY_VEHICLE_COLOR = "color";

    public static String KEY_VEHICLE_START_DATE = "start_date";

    public static String KEY_VEHICLE_END_DATE = "end_date";

    public static String KEY_PAYMENT_METHOD_LIST = "paymethod_list";

    public static String KEY_PAYMETHOD_ID = "paymethod_id";

    public static String KEY_PAYMENT_TYPE = "payment_type";

    public static String KEY_PAYMENT_ORDER = "payment_order";

    public static String KEY_CARD_NUMBER = "card_number";

    public static String KEY_EXPIRED_DATE = "expired_date";

    public static String KEY_CARD_HOLDER_NAME = "card_holder_name";

    public static String KEY_PAYMENT_ZIPCODE = "zip_code";

    public static String KEY_ROUTING_NUMBER = "routing_number";

    public static String KEY_PAYMENT_FIRST_NAME = "first_name";

    public static String KEY_PAYMENT_LAST_NAME = "last_name";

    public static String KEY_DATE = "date";

    public static String KEY_TIME = "time";

    public static String KEY_ROAD_LOCATION = "road_location";

    public static String KEY_TOLL = "toll";

    public static String KEY_DISCOUNT = "discount";

    public static String KEY_DATE_TIME = "datetime";

    public static String KEY_PAYMENT = "description";

    public static String KEY_AMOUNT = "amount";

    public static String KEY_INVOICE_DATE = "invoice_date";

    public static String KEY_INVOICE_ID = "invoice_id";

    public static String KEY_INVOICE_AMOUNT = "invoice_amount";

    public static String KEY_BALANCE_DUE = "balance_due";

    public static String KEY_INFO_NAME = "info_name";

    public static String KEY_INFO_DESC = "info_desc";

    public static String KEY_LONGXY = "long";

    public static String KEY_LATXY = "lat";

    public static String KEY_ICON_TYPE = "icon_type";

    public static String KEY_OFF_PEAK = "off_peak";

    public static String KEY_URL = "url";

    public static String KEY_TITLE = "title";

    public static String KEY_DESCRIPTION = "description";

    public static String KEY_ONE_TIME_PAYMENT = "one_time_payment";

    public static String KEY_VIOLATION_IDS = "vioIDs";

    public static String ACTION_EMAIL_INVOICE = "emailinv";

    public static String ACTION_UNPAID_VIOLATION = "unpaidVios";

    public static String ACTION_SELECTED_UNPAID_VIOLATION = "selectedUnpaidVios";

    public static String KEY_ACTION = "action";

    public static int CALCULATE_TOLL_FOR_ME = 0;
    public static int CALCULATE_TOLL_MYSELF = 1;

    //save_card_type; //3: not save; 1: save for 30 days or less; 2: save forever
    public static int SAVE_CARD_NOT_SAVE = 3;
    public static int SAVE_CARD_SAVE_30 = 1;
    public static int SAVE_CARD_SAVE_FOREVER = 2;

    public static boolean enableAutoPopulatePayment = false;

    public static long ONE_DAY = 1000*60*60*24L;
}
