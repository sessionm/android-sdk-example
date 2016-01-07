/*
 * Copyright (c) 2015 SessionM. All rights reserved.
 */

package com.sessionm.example.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.sessionm.api.message.feed.data.FeedMessageData;
import com.sessionm.api.user.order.data.Order;
import com.sessionm.api.user.receipt.data.Receipt;
import com.sessionm.example.view.FeedImageView;
import com.sessionm.example.view.SEApplication;
import com.sessionm.example.R;
import com.sessionm.example.view.OfferDetailsActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//Adapter class to draw Rewards List and handle Receipt Image events
public class RewardsFeedListAdapter extends BaseAdapter {

    private Activity activity;
    private LayoutInflater inflater;
    private List<Receipt> receiptsList = new ArrayList<>();
    private String mTAG = "RewardsFragment";
    ImageLoader imageLoader = SEApplication.getInstance().getImageLoader();

    public RewardsFeedListAdapter(Activity activity, List<Receipt> receipts) {
        this.activity = activity;
        this.receiptsList.addAll(receipts);
    }

    @Override
    public int getCount() {
        return receiptsList.size();
    }

    @Override
    public Object getItem(int location) {
        return receiptsList.get(location);
    }

    //Update the the list of Receipt items, will be used in your MainActivity
    public void updateData(List<Receipt> data) {
        this.receiptsList.clear();
        this.receiptsList.addAll(data);
        this.notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.feed_item_reward, null);

        if (imageLoader == null)
            imageLoader = SEApplication.getInstance().getImageLoader();

        TextView headerTextView = (TextView) convertView.findViewById(R.id.reward_header_text);
        TextView subHeaderTextView = (TextView) convertView
                .findViewById(R.id.reward_subheader_text);
        TextView descriptionTextView = (TextView) convertView
                .findViewById(R.id.reward_detail_text);
        TextView valueTextView = (TextView) convertView
                .findViewById(R.id.reward_value_text);

        FeedImageView feedImageView = (FeedImageView) convertView
                .findViewById(R.id.reward_main_image);

        final Receipt receipt = receiptsList.get(position);

        if (receipt.getStatus().equals(Receipt.ReceiptStatusType.APPROVED)) {
            final Order order = receipt.getOrder();
            if (order != null) {
                //show rewards tile information based on receipt
                headerTextView.setText(order.getHeader());
                subHeaderTextView.setText(order.getSubheader());

                if (!TextUtils.isEmpty(order.getDescription())) {
                    //Returns the SessionM Offer item's description, String
                    descriptionTextView.setText(order.getDescription());
                    descriptionTextView.setVisibility(View.VISIBLE);
                } else {
                    descriptionTextView.setVisibility(View.GONE);
                }

                if (order.getImageURL() != null && !order.getImageURL().equals("null")) {
                    //Returns the SessionM Offer item's image URL, String
                    feedImageView.setImageUrl(order.getImageURL(), imageLoader);
                    feedImageView.setVisibility(View.VISIBLE);
                } else {
                    feedImageView.setVisibility(View.GONE);
                }

                valueTextView.setVisibility(View.GONE);

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showOfferDetails(order);
                    }
                });
               /* convertView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        SessionM.getInstance().logAction("upload_order_status", 1, receipt.getPayloads());
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("retailer", "BestBuy");
                            jsonObject.put("reason", "Duplicate Code!");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        order.updateStatus(Order.OrderStatus.REDEMPTION_ERROR, jsonObject);
                        return false;
                    }
                });*/
            }
        }
        //Use promotion data
        else {
            final FeedMessageData promotion = receipt.getPromotion();
            if (promotion != null) {
                //show rewards tile information based on receipt
                headerTextView.setText(promotion.getHeader());
                subHeaderTextView.setText(promotion.getSubHeader());

                if (!TextUtils.isEmpty(promotion.getDescription())) {
                    //Returns the SessionM Offer item's description, String
                    descriptionTextView.setText(promotion.getDescription());
                    descriptionTextView.setVisibility(View.VISIBLE);
                } else {
                    descriptionTextView.setVisibility(View.GONE);
                }

                valueTextView.setVisibility(View.VISIBLE);
                JSONObject data = promotion.getData();
                if (data != null) {
                    String value = data.optString("value");
                    valueTextView.setText(value);
                }

                if (promotion.getImageURL() != null && !promotion.getImageURL().equals("null")) {
                    //Returns the SessionM Offer item's image URL, String
                    feedImageView.setImageUrl(promotion.getImageURL(), imageLoader);
                    feedImageView.setVisibility(View.VISIBLE);
                } else {
                    feedImageView.setVisibility(View.GONE);
                }

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Open details page in SessionM Portal Activity.
                        receipt.presentDetailsPage();
                        receipt.notifyTapped();
                    }
                });
            }
        }

        LinearLayout receiptStatusLayout = (LinearLayout) convertView.findViewById(R.id.submission_status_layout);
        TextView receiptStatusText = (TextView) convertView.findViewById(R.id.submission_status_result_text);
        //Returns the status of the Receipt, String
        receiptStatusText.setText(receipt.getStatus().toString());

        receipt.notifySeen();
        return convertView;
    }

    private void handleDeepLinkAd(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(android.net.Uri.parse(url));
        activity.startActivity(intent);
    }

    public void offlineMode(List<Receipt> data){
        this.receiptsList.clear();
        Log.d(mTAG, "Device is offline getting a list of approved receipts to display.");
        for (int i = 0; i < data.size(); i++) {
            Receipt receipt = data.get(i);
            if (receipt.getStatus() == Receipt.ReceiptStatusType.APPROVED) {
                this.receiptsList.add(receipt);
            }
        }
        Log.d(mTAG, "Number of Approved Receipts" + receiptsList.size());
        this.notifyDataSetChanged();
    }

    private void showOfferDetails(Order order){
        Intent order_detail = new Intent(activity, OfferDetailsActivity.class);
        order_detail.putExtra("orderID", order.getID());
        activity.startActivity(order_detail);
    }
}