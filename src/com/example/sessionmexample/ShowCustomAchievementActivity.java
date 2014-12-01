package com.example.sessionmexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.sessionm.api.AchievementData;
import com.sessionm.api.BaseActivity;
import com.sessionm.api.SessionM;

/**
 * This class shows how to auto present an achievement which is gained from another activity.
 *
 * You can log action in {@link AchievementActivity} for either "Custom Achievement" or "Log Action" to
 * get an achievement first. Then you can present it in this activity anywhere you want.
 *
 * 1. If you want to auto present our standard achievement(saying, achievement gained from "Log Action"),
 * just call
 * SessionM..getInstance().setAutopresentMode(true);
 * then
 * SessionM.getInstance().presentActivity(SessionM.ActivityType.ACHIEVEMENT);
 * It is recommended to be called in onResume().
 *
 * 2. If you are using your custom achievement, the better way is to pass the customAchievement to
 * current activity, then call its present method.
 *
 * You can also get the latest gained achievement by calling
 * SessionM.getInstance().getUnclaimedAchievement();
 * This would returns either standard or custom achievement, but we CANNOT guarantee your custom
 * achievement can be presented if you do so since it's deeply customized. In this case, we recommend
 * you to call its present method as a better implementation.
 *
 * Note: getUnclaimedAchievement() only get the latest gained achievement, if the user can get several
 * achievements before presenting, you still need to save and pass the specific custom achievement
 * if you need to present it.
 *
 * Please see code for more details.
 */

public class ShowCustomAchievementActivity extends BaseActivity implements CustomAchievement.CustomAchievementListener{

    private CustomAchievement customAchievement;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_custom_achievement);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.show_custom_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShowCustomAchievementActivity.this, AchievementActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        SessionM.getInstance().setAutopresentMode(true);
        //for "Log Action" achievement
        AchievementData a = SessionM.getInstance().getUnclaimedAchievement();
        //for custom achievement
        customAchievement = getAvailableCustomAchievement(AchievementActivity.customAchievement);
        if(a != null)
            SessionM.getInstance().presentActivity(SessionM.ActivityType.ACHIEVEMENT);
        if(customAchievement != null)
            customAchievement.present();
    }

    private CustomAchievement getAvailableCustomAchievement(CustomAchievement customAchievement){
        ViewGroup mainLayout = (ViewGroup) this.findViewById(R.id.parent_layout);
        if(mainLayout != null && customAchievement != null) {
            customAchievement.setAchievementViewGroup(mainLayout);
            customAchievement.setAchievementListener(this);
        }
        return customAchievement;
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

    @Override
    public void onDismiss(CustomAchievement customAchievement) {
        if (this.customAchievement != null) {
            this.customAchievement.setAchievementListener(null);
            this.customAchievement = null;
        }
    }

    @Override
    public void onPresent(CustomAchievement customAchievement) {

    }
}
