/*
 * Copyright (c) 2015 SessionM. All rights reserved.
 */

package com.sessionm.example.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Zhenhao Lei on 11/12/15.
 */
public class Utility {
    public static final String GEOFENCE_ENABLED_KEY = "geofence_enable";
    public static final String PUSH_NOTIFICATION_ENABLED_KEY = "push_notification_enable";

    private static Context _context;
    public static SharedPreferences _prefs;
    private static final String PREFERENCE_FILE_NAME = "com.sessionm.sample";

    public static void initialize(Context context) {
        _prefs = context.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
        _context = context;
    }

    public static void persistStatusBoolean(String key, boolean value) {
        SharedPreferences.Editor prefEditor = _prefs.edit();
        prefEditor.putBoolean(key, value);
        prefEditor.apply();
    }

    public static Boolean getLocalStatusBoolean(String key) {
        return _prefs.getBoolean(key, false);
    }

    public static void hideKeyboard(View v) {
        InputMethodManager keyboard = (InputMethodManager) _context.getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

}
