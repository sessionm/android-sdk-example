/*
 * Copyright (c) 2015 SessionM. All rights reserved.
 */

package com.sessionm.example.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.sessionm.api.AchievementData;
import com.sessionm.api.SessionListener;
import com.sessionm.api.SessionM;
import com.sessionm.api.User;
import com.sessionm.api.message.data.MessageData;
import com.sessionm.api.message.feed.ui.ActivityFeedActivity;
import com.sessionm.example.util.Utility;
import com.sessionm.example.R;

//Having the MainActivity implement the SessionM SessionListener allows the developer to listen on the SessionM Session State and update the activity:
//- when the Session.State changes (Starting, Started_online, Started_offline, Stopped, Stopping)
//- if the session fails to start (Started_offline)
//- when the SessionM User object is updated
//- when the Feed Message Data list has been updated
//- when the User Activities Data list has been updated
//- when a receipt image has been updated or uploaded
//- if a push notification is available
//- when a user has unclaimed achievements

public class MainActivity extends AppCompatActivity implements SessionListener, ViewPager.OnPageChangeListener{

    private ViewPager pager;

    private PromotionsFragment messageFragment;
    private RewardsFragment rewardsFragment;
    private AchievementFragment achievementFragment;
    private ProfileFragment profileFragment;
    private ActionBar actionBar;

    SessionM sessionM = SessionM.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        pager.addOnPageChangeListener(this);

        // Bind the tabs to the ViewPager
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setIndicatorColor(Color.WHITE);
        tabs.setViewPager(pager);

        setUpAccountLayout(sessionM.getUser().isRegistered());
    }



    public class MyPagerAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = {"Promotions", "Submissions", "Achievements", "Profile"};

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = PromotionsFragment.newInstance();
            switch (position) {
                case 0:
                    messageFragment = PromotionsFragment.newInstance();
                    fragment = messageFragment;
                    break;
                case 1:
                    rewardsFragment = RewardsFragment.newInstance();
                    fragment = rewardsFragment;
                    break;
                case 2:
                    achievementFragment = AchievementFragment.newInstance();
                    fragment = achievementFragment;
                    break;
                case 3:
                    profileFragment = ProfileFragment.newInstance();
                    fragment = profileFragment;
                    break;
            }
            return fragment;
        }
    }

    private void setUpAccountLayout(boolean userRegistered) {
        if (profileFragment != null) {
            profileFragment.setCurrentLayout(userRegistered);
            Utility.hideKeyboard(getWindow().getDecorView());
        }
    }

    //Listen for changes in the Session State
    @Override
    public void onSessionStateChanged(SessionM sessionM, SessionM.State state) {
        if (messageFragment != null)
            messageFragment.updateOfflineLayout();
        if (rewardsFragment != null)
            rewardsFragment.offlineApproved();
    }

    @Override
    public void onSessionFailed(SessionM sessionM, int i) {

    }

    //Listen for changes to the user
    @Override
    public void onUserUpdated(SessionM sessionM, User user) {
        if (user != null) {
            SessionM.EnrollmentResultType resultType = sessionM.getEnrollmentResult();
            //Authentication failed
            if (resultType.equals(SessionM.EnrollmentResultType.FAILURE)) {
                String errorMessage = SessionM.getInstance().getResponseErrorMessage();
                Toast.makeText(this, "Authentication Failed! " + errorMessage, Toast.LENGTH_SHORT).show();
                return;
            }
            if (achievementFragment != null)
                achievementFragment.updateCustomPortalButton();
            setUpAccountLayout(user.isRegistered());
        }
    }

    @Override
    public void onUnclaimedAchievement(SessionM sessionM, AchievementData achievementData) {
        if (achievementFragment != null)
            achievementFragment.showCustomAchievement(achievementData);
    }

    //Update Promotions Tab ListView based on latest promotions
    @Override
    public void onMessageUpdated(SessionM sessionM, MessageData messageData) {
        if (messageFragment != null)
            messageFragment.updateMessagesList();
    }

    //Update Submissions Tab ListView based on latest receipts.
    @Override
    public void onUserActivitiesUpdated(SessionM sessionM) {
        if (messageFragment != null && rewardsFragment != null) {
            messageFragment.updateMessagesList();
            rewardsFragment.updateRewardsList();
        }
    }

    @Override
    public void onNotificationMessage(SessionM sessionM, MessageData messageData) {

    }

    @Override
    public void onReceiptUpdated(SessionM sessionM, String s) {

    }

    @Override
    public void onOrderStatusUpdated(SessionM sessionM, String s) {
        if (s == null)
            Toast.makeText(this, "Updated Order Status!", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Error: " + s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (actionBar != null && !actionBar.isShowing() && (position == 2 || position == 3))
            actionBar.show();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SessionM.RECEIPT_UPLOAD_RESULT_CODE) {
            if (resultCode == RESULT_OK)
                Toast.makeText(this, "Receipt uploaded!", Toast.LENGTH_SHORT).show();
            else if (resultCode == RESULT_CANCELED) {
                String errorMessage = "Back button";
                if (intent != null)
                    errorMessage = intent.getStringExtra("result");
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.portal:
                sessionM.presentActivity(SessionM.ActivityType.PORTAL);
                return true;
            case R.id.feed:
                startActivity(new Intent(this, ActivityFeedActivity.class));
                return true;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
