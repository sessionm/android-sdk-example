package com.example.sessionmexample;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sessionm.api.AchievementData;
import com.sessionm.api.SessionM;

import java.util.ArrayList;

public class AchievementListActivity extends ListActivity {
    private ImageLoader imageLoader;
    private DisplayImageOptions displayOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        displayOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ArrayList<AchievementData> achievements = new ArrayList<AchievementData>(SessionM.getInstance().getUser().getAchievementsList());
        MyArrayAdapter adapter = new MyArrayAdapter(this, achievements);
        setListAdapter(adapter);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        imageLoader = ImageLoader.getInstance();
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

    public class MyArrayAdapter extends ArrayAdapter<AchievementData> {
        private final Context context;
        private final ArrayList<AchievementData> values;

        public MyArrayAdapter(Context context, ArrayList<AchievementData> values) {
            super(context, R.layout.achievement_row, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.achievement_row, parent, false);
            TextView textView_message = (TextView) rowView.findViewById(R.id.achievementMessage);
            TextView textView_name = (TextView) rowView.findViewById(R.id.achievementName);
            TextView textView_points = (TextView) rowView.findViewById(R.id.achievementPoints);
            TextView textView_earned_times = (TextView) rowView.findViewById(R.id.achievementEarnedTimes);
            TextView textView_unclaimed_count = (TextView) rowView.findViewById(R.id.achievementUnclaimedCount);
            TextView textView_last_earn_date = (TextView) rowView.findViewById(R.id.achievementLastEarnedDate);
            ImageView imageView_icon = (ImageView) rowView.findViewById(R.id.achievementIcon);

            AchievementData a = values.get(position);
            textView_name.setText("Name: " + a.getName());
            textView_message.setText("Message: " + a.getInstructions());
            textView_points.setText("" + a.getMpointValue());
            textView_earned_times.setText("Times Earned: " + a.getTimesEarned());
            textView_unclaimed_count.setText("Unclaimed Count: " + a.getUnclaimedCount());
            textView_last_earn_date.setText("Last Earned Date: " + a.lastEarnedDate());
            imageLoader.displayImage(a.getAchievementIconURL(), imageView_icon, displayOptions);

            return rowView;
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        /*JSONObject item = (JSONObject) getListAdapter().getItem(position);
        try {
            SessionM.getInstance().setExpandedPresentationMode(true);
            SessionM.getInstance().presentActivity(SessionM.ActivityType.PORTAL, item.getString("url"));
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
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
