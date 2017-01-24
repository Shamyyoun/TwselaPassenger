package com.twsela.client;

/**
 * Created by Shamyyoun on 18/12/15.
 * A class contains constants for the application and for some of the utility classes.
 */
public class Const {
    // App Level Constants:--------------
    public static final String LOG_TAG = "TwselaClient";
    public static final String SHARED_PREFERENCES_FILE_NAME = "TwselaClient";
    public static final String APP_FILES_DIR = "/.twsela_client";
    public static final String END_POINT = "http://35.167.31.161:3300";
    public static final int DEFAULT_ITEM_ID = -1; // this is used to add a default item in lists used in adapter
    public static final int INITIAL_ZOOM_LEVEL = 15;
    public static final int MAP_REFRESH_RATE = 6 * 1000; // map refresh rate in milliseconds
    public static final int MIN_LOADING_DRIVERS_ZOOM_LEVEL = 12; // min zoom level of the map to start loading drivers
    public static final int TRIP_REQUEST_TIMEOUT = 60 * 1000;
    public static final int GOOGLE_MAX_ORIGINS = 10; // for google distanceMatrix api

    // Server Constants:--------------------
    public static final int SER_CODE_200 = 200;
    public static final String SER_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String SER_DATE_FORMAT = "yyyy/MM/dd";
    public static final String SER_TIME_FORMAT = "HH:mm:ss";

    // Permission Requests:------------------
    public static final int PERM_REQ_LOCATION = 1;

    // API Routes:---------------------------
    public static final String ROUTE_PASSENGER = "passenger";
    public static final String ROUTE_DRIVER = "driver";
    public static final String ROUTE_TRIP = "trip";
    public static final String ROUTE_LOGIN = "login";
    public static final String ROUTE_NEAR_DRIVERS = "nearDrivers";
    public static final String ROUTE_REQUEST_TRIP = "requestTrip";
    public static final String ROUTE_GET_DETAILS_BY_ID = "getDetailsById";

    // API Params:---------------------------
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_GCM = "gcm";
    public static final String PARAM_ID = "id";

    // SharePrefs Keys:---------------------
    public static final String SP_USER = "user";

    // Activity Requests:-------------------
    public static final int REQ_FROM_SEARCH = 1;
    public static final int REQ_TO_SEARCH = 2;
    public static final int REQ_ENABLE_GPS = 3;

    // Keys:--------------------------------
    public static final String KEY_KEY = "key";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_ID = "id";
    public static final String KEY_STATUS = "status";

    // Notification IDs:--------------------
    public static final int NOTI_TRIP_CHANGED = 1;

    // Others:-----------------------------
    public static final String TAG_DISTANCE_MATRIX = "distancematrix";
    public static final String TAG_DIRECTIONS = "directions";
}
