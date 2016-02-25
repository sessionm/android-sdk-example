/*
 * Copyright (c) 2015 SessionM. All rights reserved.
 */

package com.sessionm.example.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.sessionm.api.SessionM;
import com.sessionm.api.message.data.MessageData;
import com.sessionm.api.message.feed.data.FeedMessageData;
import com.sessionm.api.user.UserActivitiesManager;
import com.sessionm.example.view.FeedImageView;
import com.sessionm.example.view.SEApplication;
import com.sessionm.example.R;
import com.sessionm.example.view.PromotionDetailsActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Adapter class to draw the Promotions Message List and handle Feed Message events
public class PromotionsFeedListAdapter extends BaseAdapter {

    private Activity activity;
    private LayoutInflater inflater;
    private List<MessageData> messagesList = new ArrayList<>();
    ImageLoader imageLoader = SEApplication.getInstance().getImageLoader();

    public PromotionsFeedListAdapter(Activity activity, List<MessageData> messageDataList) {
        this.activity = activity;
        this.messagesList.addAll(messageDataList);
    }

    @Override
    public int getCount() {
        return messagesList.size();
    }

    @Override
    public Object getItem(int location) {
        return messagesList.get(location);
    }

    //Update the Promotions List Data, will be used in your Main Activity
    public void updateData(List<MessageData> data) {
        this.messagesList.clear();
        this.messagesList.addAll(data);
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
            convertView = inflater.inflate(R.layout.feed_item_promotion, null);

        if (imageLoader == null)
            imageLoader = SEApplication.getInstance().getImageLoader();



        FeedImageView iconImageView = (FeedImageView) convertView
                .findViewById(R.id.promotion_icon_image);

        TextView headerTextView = (TextView) convertView.findViewById(R.id.promotion_header_text);
        TextView subHeaderTextView = (TextView) convertView
                .findViewById(R.id.promotion_subheader_text);
        TextView descriptionTextView = (TextView) convertView
                .findViewById(R.id.promotion_detail_text);
        TextView valueTextView = (TextView) convertView
                .findViewById(R.id.promotion_value_text);

        FeedImageView feedImageView = (FeedImageView) convertView
                .findViewById(R.id.promotion_main_image);

        final FeedMessageData item = (FeedMessageData) messagesList.get(position);

        //Returns the MessageData header, String
        headerTextView.setText(item.getHeader());

        //Returns the MessageData sub header, String
        subHeaderTextView.setText(item.getSubHeader());

        //There is no need to draw the description if it was not set
        if (!TextUtils.isEmpty(item.getDescription())) {
            //Returns the MessageData description, String
            descriptionTextView.setText(item.getDescription());
            descriptionTextView.setVisibility(View.VISIBLE);
        } else {
            descriptionTextView.setVisibility(View.GONE);
        }

        //TODO: set value, might be points
        valueTextView.setText("100");

        //Any customized value in data field
        /*JSONObject data = item.getData();
        if (data != null) {
            String value = data.optString("value");
            valueTextView.setText(value);
        }*/

        //There is no need to draw the image if there is not icon URL
        if (item.getIconURL() != null && !item.getIconURL().equals("null")) {
            //Returns the MessageData image URL, String
            iconImageView.setImageUrl(item.getIconURL(), imageLoader);
            iconImageView.setVisibility(View.VISIBLE);
        } else {
            iconImageView.setVisibility(View.GONE);
        }

        //There is no need to draw the image if there is not image URL
        if (item.getImageURL() != null && !item.getImageURL().equals("null")) {
            //Returns the MessageData image URL, String
            feedImageView.setImageUrl(item.getImageURL(), imageLoader);
            feedImageView.setVisibility(View.VISIBLE);
            feedImageView
                    .setResponseObserver(new FeedImageView.ResponseObserver() {
                        @Override
                        public void onError() {
                        }

                        @Override
                        public void onSuccess() {
                        }
                    });
        } else {
            feedImageView.setVisibility(View.GONE);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.notifyTapped();
                showPromotionDetails(item);

            }
        });

        item.notifySeen();
        return convertView;
    }

    private void showPromotionDetails(MessageData data){
        Intent intent = new Intent(activity, PromotionDetailsActivity.class);
        intent.putExtra("messageID", data.getMessageID());
        activity.startActivity(intent);
    }

    //Starts the Receipt Upload Activity when the user taps on a Feed Message from the list
    public void uploadReceipt() {
        Map<String, String> additionalAttributes = new HashMap<>();
        //Title for the Receipt Upload Activity
        additionalAttributes.put(UserActivitiesManager.RECEIPT_ATTRIBUTE_TITLE, "Attributes");
        // Description for the Upload Receipt Activity
        additionalAttributes.put(UserActivitiesManager.RECEIPT_ATTRIBUTE_DESCRIPTION, "Please provide additional attributes for this receipt.");
        //Set description to display on the Upload Receipt Activity
        additionalAttributes.put("Product Name", "Please provide the name of this product.");
        additionalAttributes.put("Serial Number", "Please provide serial number of this product.");
        SessionM.getInstance().getUserActivitiesManager().setUploadReceiptActivityColors(null, null, null, null, null);
        //Start the Upload Receipt Activity
        SessionM.getInstance().getUserActivitiesManager().startUploadReceiptActivity(activity, additionalAttributes);
    }
}