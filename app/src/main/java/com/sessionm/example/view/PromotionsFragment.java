/*
 * Copyright (c) 2015 SessionM. All rights reserved.
 */

package com.sessionm.example.view;

import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.sessionm.api.SessionM;
import com.sessionm.api.message.data.MessageData;
import com.sessionm.example.controller.PromotionsFeedListAdapter;
import com.sessionm.example.R;

import java.util.List;

/**
 * Fragment of SessionM List of MessageData
 */
public class PromotionsFragment extends BaseScrollAndRefreshFragment {
    private static final String TAG = "FeedListActivity";

    private SwipeRefreshLayout swipeRefreshLayout;
    private ObservableListView listView;
    private PromotionsFeedListAdapter listAdapter;
    //private List of SessionM MessageData
    private List<MessageData> messagesList;
    //Offline textview
    TextView offlinePromoTextView;

    public static PromotionsFragment newInstance() {
        PromotionsFragment f = new PromotionsFragment();
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_promotions, container, false);
        ViewCompat.setElevation(rootView, 50);

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        listView = (ObservableListView) rootView.findViewById(R.id.message_feed_list);
        messagesList = SessionM.getInstance().getMessageManager().getMessagesList();
        offlinePromoTextView = (TextView) rootView.findViewById(R.id.promotion_offline);
        listAdapter = new PromotionsFeedListAdapter(getActivity(), messagesList);
        listView.setAdapter(listAdapter);
        listView.setScrollViewCallbacks(this);
        updateOfflineLayout();
        return rootView;
    }

    //Method for updating the List Fragment with the SessionM MessageData list
    public void updateMessagesList() {
        swipeRefreshLayout.setRefreshing(false);
        messagesList = SessionM.getInstance().getMessageManager().getMessagesList();
        listAdapter.updateData(messagesList);
    }

    public void updateOfflineLayout() {
        SessionM.State state = SessionM.getInstance().getSessionState();
        //Check is session is started
        if (state.equals(SessionM.State.STARTED_ONLINE)) {
            //Do whatever you want
            listView.setVisibility(View.VISIBLE);
            offlinePromoTextView.setVisibility(View.GONE);
        }
        //What to do when a Session does not start, handling international users, no WI-Fi connection
        if (state.equals(SessionM.State.STARTED_OFFLINE)) {
            listView.setVisibility(View.GONE);
            offlinePromoTextView.setVisibility(View.VISIBLE);
        }
    }
}