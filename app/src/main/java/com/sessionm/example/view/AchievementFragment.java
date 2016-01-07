package com.sessionm.example.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.sessionm.api.AchievementData;
import com.sessionm.api.SessionM;
import com.sessionm.api.SessionM.ActivityType;
import com.sessionm.api.SessionM.State;
import com.sessionm.api.User;
import com.sessionm.example.R;
import com.sessionm.example.view.custom.CustomAchievementView;
import com.sessionm.example.view.custom.CustomAchievementView.AchievementPresentationStyle;
import com.sessionm.example.view.custom.CustomAchievementView.CustomAchievementListener;
import com.sessionm.ui.BadgeView;

/**
 * This class shows how to handle activity lifecycle manually with SessionM SDK.
 * If you can't extend your activity from our BaseActivity, you can make lifecycle calls directly.
 * Simply call each of onActivityStart(), onActivityStop(), onActivityResume(), onActivityPause() from your
 * onStart(), onStop(), onResume(), onPause() methods in your activity class.
 * <p/>
 * This class also shows how to show achievements and implement custom achievements.
 * Once you've added your achievement in developer portal, you call logAction() method. You should see
 * an additional achievement appear in your app.
 * <p/>
 * To add additional achievements simply create more achievements in the developer portal and then add
 * the corresponding logAction() call in your app.
 * <p/>
 * For more achievements placement ideas, please checkout http://www.sessionm.com/documentation/achievement-placement.php
 */
public class AchievementFragment extends Fragment implements CustomAchievementListener, OnClickListener {

    private BadgeView portalBadger;

    private CustomAchievementView customAchievementView;

    public static AchievementFragment newInstance() {
        AchievementFragment f = new AchievementFragment();
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_achievement, container, false);
        ImageButton rewardsBtn = (ImageButton) rootView.findViewById(R.id.rewards_btn);
        Button actionBtn = (Button) rootView.findViewById(R.id.action_btn);
        Button customActionBtn = (Button) rootView.findViewById(R.id.custom_action_btn);
        rewardsBtn.setOnClickListener(this);
        actionBtn.setOnClickListener(this);
        customActionBtn.setOnClickListener(this);
        portalBadger = new BadgeView(getActivity(), rewardsBtn);
        updateCustomPortalButton();

        return rootView;
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
                            getActivity(),
                            "SessionM Portal is currently unavailable.",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.custom_action_btn:
                SessionM.getInstance().logAction("custom_action");
                break;
            case R.id.action_btn:
                /**
                 * Call this method whenever you want the achievement to appear.
                 */
                SessionM.getInstance().logAction("first_action");
                break;
            default:
                break;
        }
    }

    public void showCustomAchievement(AchievementData achievement) {
        if (achievement != null && achievement.isCustom()) {
            ViewGroup mainLayout = (ViewGroup) getActivity().findViewById(R.id.main_parent_layout);
            if (mainLayout != null) {
                customAchievementView = (CustomAchievementView) getActivity().getLayoutInflater()
                        .inflate(R.layout.custom_achievement, null);
                customAchievementView.setAchievementData(achievement);
                customAchievementView.setAchievementListener(AchievementFragment.this);
                customAchievementView.setAchievementViewGroup(mainLayout);
                customAchievementView.setAchievementPresentationStyle(AchievementPresentationStyle.SLIDE_IN_SLIDE_OUT);
                if (SessionM.getInstance().isAutopresentMode()) {
                    customAchievementView.present();
                }
            }
        } else {
            customAchievementView = null;
        }
    }

    public void updateCustomPortalButton() {
        User user = SessionM.getInstance().getUser();
        int unclaimedCount = user.getUnclaimedAchievementCount();
        if (unclaimedCount <= 0) {
            portalBadger.hide();
        } else {
            portalBadger.setText("" + unclaimedCount);
            portalBadger.show();
        }
    }

    @Override
    public void onDismiss(CustomAchievementView customAchievement) {
        if (this.customAchievementView != null) {
            this.customAchievementView.setAchievementListener(null);
            this.customAchievementView = null;
        }
    }

    @Override
    public void onPresent(CustomAchievementView customAchievement) {
        // called when the achievement is presented.
    }

    @Override
    public void onDestroy() {
        super.onPause();
        if (customAchievementView != null) {
            customAchievementView.dismiss(true);
        }
        customAchievementView = null;
    }
}
