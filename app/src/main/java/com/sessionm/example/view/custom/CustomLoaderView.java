package com.sessionm.example.view.custom;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sessionm.example.R;

/**
 * Copyright (c) 2015 SessionM. All rights reserved.
 */

public class CustomLoaderView extends com.sessionm.api.CustomLoaderView {

    private RelativeLayout loadingLayout;
    private RelativeLayout failedLayout;
    private RelativeLayout unavailableLayout;
    Context context;

    public CustomLoaderView(Context context) {
        super();
        this.context = context;
        createCustomLoaderLayout();
    }

    public void createCustomLoaderLayout() {
        //Main container layout
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        RelativeLayout loadingContainerLayout = new RelativeLayout(context);
        loadingContainerLayout.setLayoutParams(params);

        //Loading layout in LOADING state
        if (loadingLayout == null)
            loadingLayout = new RelativeLayout(context);
        loadingLayout.setLayoutParams(params);
        //Set custom loader background if you want
        loadingLayout.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryDark));

        RelativeLayout.LayoutParams titleLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        titleLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        titleLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        TextView titleTextView = new TextView(context);
        titleTextView.setText("Loading...");
        titleTextView.setLayoutParams(titleLayoutParams);

        titleTextView.setTextColor(Color.BLACK);
        titleTextView.setId(1);

        RelativeLayout.LayoutParams progrssBarLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        progrssBarLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        progrssBarLayoutParams.addRule(RelativeLayout.BELOW, titleTextView.getId());
        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setLayoutParams(progrssBarLayoutParams);
        loadingLayout.addView(titleTextView);
        loadingLayout.addView(progressBar);

        //Failed layout in FAILED state
        if (failedLayout == null)
            failedLayout = new RelativeLayout(context);
        failedLayout.setLayoutParams(params);

        TextView failedTextView = new TextView(context);
        failedTextView.setText("Failed");
        failedTextView.setLayoutParams(titleLayoutParams);
        failedTextView.setTextSize(40);
        failedTextView.setTextColor(Color.BLACK);
        failedTextView.setId(2);

        RelativeLayout.LayoutParams retryButtonLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        retryButtonLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        retryButtonLayoutParams.addRule(RelativeLayout.BELOW, failedTextView.getId());
        Button retryBtn = new Button(context);
        retryBtn.setText("Retry");
        retryBtn.setLayoutParams(retryButtonLayoutParams);
        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Call reload method if failed.
                reloadPortalContent();
            }
        });
        failedLayout.addView(failedTextView);
        failedLayout.addView(retryBtn);
        failedLayout.setVisibility(View.GONE);

        //Unavailable layout in UNAVAILABLE state
        if (unavailableLayout == null)
            unavailableLayout = new RelativeLayout(context);
        unavailableLayout.setLayoutParams(params);

        TextView unavailableTextView = new TextView(context);
        unavailableTextView.setText("Unavailable");
        unavailableTextView.setLayoutParams(titleLayoutParams);
        unavailableTextView.setTextSize(40);
        unavailableTextView.setTextColor(Color.BLACK);
        unavailableTextView.setId(3);

        RelativeLayout.LayoutParams dismissButtonLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        dismissButtonLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        dismissButtonLayoutParams.addRule(RelativeLayout.BELOW, unavailableTextView.getId());
        Button dismissBtn = new Button(context);
        dismissBtn.setText("Close");
        dismissBtn.setLayoutParams(dismissButtonLayoutParams);
        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dismiss the custom loader if unavailable.
                dismissPortal();
            }
        });

        unavailableLayout.addView(unavailableTextView);
        unavailableLayout.addView(dismissBtn);
        unavailableLayout.setVisibility(View.GONE);

        //Close button
        RelativeLayout.LayoutParams closeButtonLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        closeButtonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        closeButtonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        Button closeBtn = new Button(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            closeBtn.setBackground(context.getDrawable(R.drawable.close));
        }
        else
            closeBtn.setBackground(context.getResources().getDrawable(R.drawable.close));
        closeBtn.setLayoutParams(closeButtonLayoutParams);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissPortal();
            }
        });

        loadingContainerLayout.addView(loadingLayout);
        loadingContainerLayout.addView(failedLayout);
        loadingContainerLayout.addView(unavailableLayout);
        loadingContainerLayout.addView(closeBtn);

        //Call setCustomLoader() method to set up custom loader view.
        setCustomLoader(loadingContainerLayout);
    }

    @Override
    public void updateLoaderViewOnStatusChanged(LoaderViewStatus status) {
        super.updateLoaderViewOnStatusChanged(status);
        if (status.equals(LoaderViewStatus.LOADING)) {
            loadingLayout.setVisibility(View.VISIBLE);
            failedLayout.setVisibility(View.GONE);
            unavailableLayout.setVisibility(View.GONE);
        } else if (status.equals(LoaderViewStatus.FAILED)) {
            loadingLayout.setVisibility(View.GONE);
            failedLayout.setVisibility(View.VISIBLE);
            unavailableLayout.setVisibility(View.GONE);
        } else if (status.equals(LoaderViewStatus.UNAVAILABLE)) {
            loadingLayout.setVisibility(View.GONE);
            failedLayout.setVisibility(View.GONE);
            unavailableLayout.setVisibility(View.VISIBLE);
        }
    }
}
