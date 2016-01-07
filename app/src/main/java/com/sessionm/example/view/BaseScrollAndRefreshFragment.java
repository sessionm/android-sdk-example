/*
 * Copyright (c) 2015 SessionM. All rights reserved.
 */

package com.sessionm.example.view;

import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.sessionm.api.SessionM;

public class BaseScrollAndRefreshFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, ObservableScrollViewCallbacks {

    //Scroll list methods to show/hide tool bar
    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null) {
            if (scrollState == ScrollState.UP) {
                if (ab.isShowing()) {
                    ab.hide();
                }
            } else if (scrollState == ScrollState.DOWN) {
                if (!ab.isShowing()) {
                    ab.show();
                }
            }
        }
    }

    @Override
    public void onRefresh() {
        //Manually refresh messages list. Listen on {@link SessionListener#onMessageUpdated(SessionM, MessageData)} for result.
        SessionM.getInstance().getMessageManager().refreshMessagesList();
        //Manually refresh user activities. Listen on {@link SessionListener#onUserActivitiesUpdated(SessionM)} for result.
        SessionM.getInstance().getUserActivitiesManager().refreshUserActivities();
    }
}
