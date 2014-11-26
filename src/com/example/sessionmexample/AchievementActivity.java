package com.example.sessionmexample;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.sessionmexample.CustomAchievement.AchievementPresentationStyle;
import com.example.sessionmexample.CustomAchievement.CustomAchievementListener;
import com.sessionm.api.AchievementData;
import com.sessionm.api.SessionListener;
import com.sessionm.api.SessionM;
import com.sessionm.api.SessionM.ActivityType;
import com.sessionm.api.SessionM.State;
import com.sessionm.api.User;

/**
 * This class shows how to handle activity lifecycle manually with SessionM SDK.
 * If you can't extend your activity from our BaseActivity, you can make lifecycle calls directly.
 * Simply call each of onActivityStart(), onActivityStop(), onActivityResume(), onActivityPause() from your
 * onStart(), onStop(), onResume(), onPause() methods in your activity class.
 *
 * This class also shows how to show achievements and implement custom achievements.
 * Once you've added your achievement in developer portal, you call logAction() method. You should see
 * an additional achievement appear in your app.
 *
 * To add additional achievements simply create more achievements in the developer portal and then add
 * the corresponding logAction() call in your app.
 *
 * For more achievements placement ideas, please checkout http://www.sessionm.com/documentation/achievement-placement.php
 */
public class AchievementActivity extends Activity implements CustomAchievementListener, OnClickListener {

    private BadgeView portalBadger;

    private CustomAchievement customAchievement;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);
        View rewardsButton = this.findViewById(R.id.rewards_btn);
        rewardsButton.setOnClickListener(this);
        findViewById(R.id.action_btn).setOnClickListener(this);
        findViewById(R.id.custom_action_btn).setOnClickListener(this);
        portalBadger = new BadgeView(this, rewardsButton);
        SessionM.getInstance().setSessionListener(new MySessionListener());
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rewards_btn:
                if (SessionM.getInstance().getSessionState() == State.STARTED_ONLINE) {
                    SessionM.getInstance().presentActivity(
                            ActivityType.PORTAL);
                } else {
                    Toast.makeText(
                            AchievementActivity.this,
                            "SessionM Portal is currently unavailable.",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.custom_action_btn:
                SessionM.getInstance().logAction("custom example");
                break;
            case R.id.action_btn:
                /**
                 * Call this method whenever you want the achievement to appear.
                 */
                SessionM.getInstance().logAction("example");
                break;
            default:
                break;
        }
    }

    /**
     * Handles activity lifecycle.
     */
    @Override
    protected void onStart() {
        super.onStart();
        SessionM.getInstance().onActivityStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SessionM.getInstance().onActivityResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SessionM.getInstance().onActivityPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SessionM.getInstance().onActivityStop(this);
    }

    @Override
    protected void onDestroy() {
        super.onPause();
        if (customAchievement != null) {
            customAchievement.dismiss(true);
        }
        customAchievement = null;
    }

    /**
     * Implements SessionListener to listen on SessionM session states
     */
    private class MySessionListener implements SessionListener {
        @Override
        public void onSessionFailed(SessionM session, int error) {
            Toast.makeText(AchievementActivity.this,
                    "Session failed with error code: " + error,
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onUserUpdated(SessionM session, final User user) {
            /*
             * update the button with the new achievement count
             */
            int unclaimedCount = user.getUnclaimedAchievementCount();
            if (unclaimedCount <= 0) {
                portalBadger.hide();
            } else {
                portalBadger.setText("" + unclaimedCount);
                portalBadger.show();
                //Manually update achievements list. See API doc for more info.
                SessionM.getInstance().updateAchievementsList();
            }
            if (user.isOptedOut()) {
                Toast.makeText(AchievementActivity.this,
                        "User is opted out",
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onSessionStateChanged(SessionM session, State state) {
            if (state == SessionM.State.STARTED_ONLINE) {
                // When in started online you should enable your button
                ImageButton rewardsButton = (ImageButton) ((Activity) AchievementActivity.this)
                        .findViewById(R.id.rewards_btn);
                rewardsButton.setImageResource(R.drawable.icn_mportal_on);
                rewardsButton.setEnabled(true);
                rewardsButton.setVisibility(View.VISIBLE);
                Toast.makeText(AchievementActivity.this, "Session started",
                        Toast.LENGTH_SHORT).show();

            } else {
                if (state == SessionM.State.STOPPED) {
                    Toast.makeText(AchievementActivity.this, "Session stopped",
                            Toast.LENGTH_SHORT).show();

                } else if (state == SessionM.State.STARTING) {
                    Toast.makeText(AchievementActivity.this, "Session starting",
                            Toast.LENGTH_SHORT).show();
                }
                // When the session is stopped you can enable the button and
                // change the background image.
                ImageButton rewardsButton = (ImageButton) ((Activity) AchievementActivity.this)
                        .findViewById(R.id.rewards_btn);
                rewardsButton.setImageResource(R.drawable.icn_mportal_on);
                rewardsButton.setEnabled(true);
                rewardsButton.setVisibility(View.GONE);
            }
        }

        @Override
        public void onUnclaimedAchievement(SessionM session, AchievementData achievement) {
            if (achievement != null && achievement.isCustom()) {
                ViewGroup mainLayout = (ViewGroup) AchievementActivity.this.findViewById(R.id.parent_layout);
                if (mainLayout != null) {
                    customAchievement = (CustomAchievement) getLayoutInflater()
                            .inflate(R.layout.custom_achievement, null);
                    customAchievement.setAchievementData(achievement);
                    customAchievement.setAchievementListener(AchievementActivity.this);
                    customAchievement.setAchievementViewGroup(mainLayout);
                    customAchievement.setAchievementPresentationStyle(AchievementPresentationStyle.SLIDE_IN_SLIDE_OUT);
                    if (session.isAutopresentMode()) {
                        customAchievement.present();
                    }
                }
            } else {
                customAchievement = null;
            }
        }
    }

    @Override
    public void onDismiss(CustomAchievement customAchievement) {
        if (this.customAchievement != null) {
            this.customAchievement.setAchievementListener(null);
            this.customAchievement = null;
        }
    }

    @Override
    public void onPresent(CustomAchievement customAchievement) {
        // called when the achievement is presented.
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
