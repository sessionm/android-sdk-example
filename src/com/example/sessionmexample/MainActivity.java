package com.example.sessionmexample;

/**
 * This class extends SessionM BaseActivity to shows how to integrate SessionM SDK.
 *
 * SessionM provides a rewards portal inside your app where users can go to learn about the achievements
 * you have setup and redeem their mPOINTS for rewards. This portal is opened by the mPOINTS rewards button,
 * which displays a local notification with the user's unclaimed achievement count.
 *
 * There are several ways you can integrate the mPOINTS rewards button, which you will see in this sample class:
 *  - In The Navigation Drawer (Recommended)
 *  - On The Home Screen
 *  - With A Welcome Message
 *
 * Your app comes pre-configured with visitation achievements. If everything is working, you should see the "Daily Tap In"
 * achievement the first time you start your app. These are awarded automatically when the user opens your app.
 *
 * You can see more achievements implementation ideas in AchievementActivity class.
 */

import java.util.HashMap;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.sessionm.api.BaseActivity;
import com.sessionm.api.PortalButton;
import com.sessionm.api.SessionM;
import com.sessionm.api.SessionM.ActivityType;
import com.sessionm.api.User;

/**
 * This class extends from SessionM BaseActivity.
 */
public class MainActivity extends BaseActivity {

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private Switch switch_status;
    private MyArrayAdapter myAdapter;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mListTitles;
    private HashMap<Integer, String> mListCount;
    private boolean startFromNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);

        //Set up the navigation drawer
        mTitle = mDrawerTitle = getTitle();
        mListTitles = getResources().getStringArray(R.array.lists_array);
        mListCount = new HashMap<Integer, String>();
        for (int i = 0; i < mListTitles.length; i++) {
            mListCount.put(i, "");
        }
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        myAdapter = new MyArrayAdapter(this, R.layout.drawer_list_item, mListTitles, mListCount);
        mDrawerList.setAdapter(myAdapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            startFromNotification = bundle.getBoolean("startFromNotification");
        }
        else
            showWelcomeDialog(this);
        if (savedInstanceState == null) {
            selectItem(0);
        }
    }

    /**
     * Update navigation drawer badge in onResume().
     */
    protected void onResume() {
        super.onResume();
        updateDrawerBadge();
        if (startFromNotification) {
            SessionM.getInstance().presentActivity(ActivityType.PORTAL);
            startFromNotification = false;
        }
    }

    /**
     * Override method in SessionListener which is implemented in BaseActivity to listen on user state.
     * Each time user info is updated, update the badge value.
     */
    @Override
    public void onUserUpdated(SessionM instance, User user) {
        updateDrawerBadge();
        pushNotification();
    }

    /**
     * Update navigation drawer badge value, which is the unclaimed achievements count.
     */
    private void updateDrawerBadge() {
        int count = SessionM.getInstance().getUser().getUnclaimedAchievementCount();
        if (count != 0) {
            mListCount.put(1, Integer.toString(count));
        }
        myAdapter.notifyDataSetChanged();
    }

    /**
     * Override method in SessionListener which is implemented in BaseActivity to listen on session state.
     * It is recommended that if the session is not ONLINE(can be network error, non-US user error), disable the reward button
     * until the session is ONLINE again.
     */
    @Override
    public void onSessionStateChanged(SessionM session, SessionM.State state){
        myAdapter.notifyDataSetChanged();
    }

    private void setUserStatusListener() {
        switch_status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SessionM.getInstance().getUser().setOptedOut(MainActivity.this, isChecked);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments
        Fragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putInt(ListFragment.ARG_NUMBER, position);
        fragment.setArguments(args);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_content, fragment).commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mListTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public static class ListFragment extends Fragment {
        public static final String ARG_NUMBER = "arg_number";

        public ListFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int i = getArguments().getInt(ARG_NUMBER);
            View rootView = inflater.inflate(R.layout.fragment_home,
                    container, false);
            /**
             * Shows how to customize portal button resource.
             *
             * The rewards button defaults to the mPOINTS Gift Box image, but if you wish to use another image,
             * add the drawable resources to your project and add the following code, replacing R.drawable.sessionm
             * with the name of your drawable.
             */
            PortalButton customizedPortalButton = (PortalButton) rootView.findViewById(R.id.home_logo);
            customizedPortalButton.setResourceId(R.drawable.m_large);

            switch (i) {
                case 1:
                    /**
                     * Place the following code in any cell of the navigation drawer
                     */
                    SessionM.getInstance().presentActivity(ActivityType.PORTAL);
                    break;
                case 2:
                    Intent achievementsIntent = new Intent(this.getActivity(), AchievementActivity.class);
                    startActivity(achievementsIntent);
                    break;
                case 3:
                    Intent listIntent = new Intent(this.getActivity(), AchievementListActivity.class);
                    if(SessionM.getInstance().getSessionState().equals(SessionM.State.STARTED_ONLINE))
                        startActivity(listIntent);
                    else
                        Toast.makeText(this.getActivity(), "Session not started!", Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    showWelcomeDialog(this.getActivity());
                    break;
                default:
                    break;
            }
            return rootView;
        }
    }

    /**
     * Customized cell, add your own badge icon here
     */
    public class MyArrayAdapter extends ArrayAdapter<String> {
        private final int resource;
        private final Context context;
        private final String[] objects;
        private final HashMap<Integer, String> count;

        public MyArrayAdapter(Context context, int resource, String[] objects, HashMap<Integer, String> count) {
            super(context, resource, objects);
            this.resource = resource;
            this.context = context;
            this.objects = objects;
            this.count = count;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(resource, parent,
                    false);
            TextView textView = (TextView) rowView.findViewById(R.id.list_text);

            ImageView imageView = (ImageView) rowView.findViewById(R.id.list_icon);
            textView.setText(objects[position]);
            // Using TextView now, should be replaced by customized badgeView
            TextView badgeView = (TextView) rowView.findViewById(R.id.list_count);
            badgeView.setTextColor(Color.WHITE);
            badgeView.setText(count.get(position));
            String s = objects[position];
            imageView.setImageResource(R.drawable.ic_launcher);
            //Switch for OptIn/OptOut mPOINTS
            if (position == 5) {
                switch_status = (Switch) rowView.findViewById(R.id.switch_optout);
                switch_status.setVisibility(View.VISIBLE);
                switch_status.setChecked(SessionM.getInstance().getUser().isOptedOut());
                setUserStatusListener();
            }
            //Disable mPoints items if session is not online
            if(!isEnabled(position)){
                textView.setTextColor(Color.GRAY);
                badgeView.setText("");
                if(switch_status != null)
                    switch_status.setEnabled(false);
            }
            return rowView;
        }

        /**
         * Check if session is ONLINE for mPoints related items
         * @param position list view item position
         * @return bool
         */
        @Override
        public boolean isEnabled(int position){
            return !(position == 1 || position == 5) || SessionM.getInstance().getSessionState() == SessionM.State.STARTED_ONLINE;
        }
    }

    /**
     * Sample code shows how to integrate the mPOINTS rewards button with the welcome dialog.
     */
    public static void showWelcomeDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.dialog_welcome).setTitle(R.string.dialog_title)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SessionM.getInstance().presentActivity(ActivityType.PORTAL);
                    }
                }).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.create().show();
    }

    /**
     * Sample code shows how to pop up mPoints achievements in Notification center.
     */
    public void pushNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.m)
                        .setContentTitle("New Achievement!")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentText("Claim your achievement");
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra("startFromNotification", true);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        int mId = 1;
        mNotificationManager.notify(mId, mBuilder.build());
    }
}
