/*
 * Copyright (c) 2015 SessionM. All rights reserved.
 */

package com.sessionm.example.view;

import android.app.Application;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.sessionm.api.SessionM;
import com.sessionm.api.SessionMActivityLifecycleCallbacks;
import com.sessionm.example.R;
import com.sessionm.example.util.LruBitmapCache;
import com.sessionm.example.util.Utility;
import com.sessionm.example.view.custom.CustomLoaderView;

public class SEApplication extends Application{
    private static final String TAG = "AppController";
    private final SessionMActivityLifecycleCallbacks mCallbacks = new SessionMActivityLifecycleCallbacks();

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    LruBitmapCache mCache;

    private static SEApplication instance;
    public static CustomLoaderView sampleCustomLoaderView;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        Utility.initialize(this);
        //Creates SessionM activity lifecycle callbacks to handle activities lifecycle
        registerActivityLifecycleCallbacks(mCallbacks);
        final SessionM sessionM = SessionM.getInstance();
        sessionM.setApplicationContext(this);
        //Sets the Google Cloud Messaging Sender ID, to register the app to be able to receive push notifications
        sessionM.setGCMSenderID(getString(R.string.gcm_sender_id));
        //Enables SessionM to receive push notifications, generates and sends a token to the server so the device can receive push notifications
        sessionM.setPushNotificationEnabled(true);
        //Enables SessionM to receive Feed Message Data (MessageData, FeedMessageData)
        sessionM.setMessagesEnabled(true);
        //Enables SessionM to receive user activities data (receipts, orders, etc.)
        sessionM.setUserActivitiesEnabled(true);
        //Sets server
        //sessionM.setServerType(SessionM.SERVER_TYPE_CUSTOM, "https://api.custom.server");
    }

    public static SEApplication getInstance() {
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(this);
        }
        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(getRequestQueue(), getLruBitmapCache());
        }
        return mImageLoader;
    }

    public LruBitmapCache getLruBitmapCache() {
        if (mCache == null) {
            mCache = new LruBitmapCache();
        }
        return mCache;
    }

    public <T> void addRequest(Request<T> request, String tag) {
        request.setTag(tag);
        getRequestQueue().add(request);
    }

    public <T> void addRequest(Request<T> request) {
        addRequest(request, TAG);
    }

    public void cancelPendingRequest(Object tag) {
        getRequestQueue().cancelAll(tag);
    }

}
