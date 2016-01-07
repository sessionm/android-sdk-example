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

import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.sessionm.api.SessionM;
import com.sessionm.api.user.receipt.data.Receipt;
import com.sessionm.example.controller.RewardsFeedListAdapter;
import com.sessionm.example.R;

import java.util.List;

//Fragment of SessionM Rewards
public class RewardsFragment extends BaseScrollAndRefreshFragment {

    private static final String TAG = "FeedListActivity";

    private SwipeRefreshLayout swipeRefreshLayout;
    private ObservableListView listView;
    private RewardsFeedListAdapter listAdapter;
    private List<Receipt> receiptList;

    public static RewardsFragment newInstance() {
        RewardsFragment f = new RewardsFragment();
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rewards, container, false);
        ViewCompat.setElevation(rootView, 50);

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        listView = (ObservableListView) rootView.findViewById(R.id.rewards_feed_list);
        receiptList = SessionM.getInstance().getUserActivitiesManager().getReceipts();
        listAdapter = new RewardsFeedListAdapter(getActivity(), receiptList);
        listView.setAdapter(listAdapter);
        listView.setScrollViewCallbacks(this);
        return rootView;
    }

    //Method for updating the List Fragment with the SessionM Orders list
    public void updateRewardsList() {
        swipeRefreshLayout.setRefreshing(false);
        receiptList = SessionM.getInstance().getUserActivitiesManager().getReceipts();
        listAdapter.updateData(receiptList);
    }

    public void offlineApproved() {
        listAdapter.offlineMode(receiptList);
    }
}
