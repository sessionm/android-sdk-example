/*
 * Copyright (c) 2015 SessionM. All rights reserved.
 */

package com.sessionm.example.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.sessionm.api.SessionM;
import com.sessionm.api.geofence.GeofenceManager;
import com.sessionm.example.BuildConfig;
import com.sessionm.example.R;
import com.sessionm.example.util.Utility;
import com.sessionm.example.view.custom.CustomLoaderView;

public class SettingsActivity extends Activity{

    private static final String VERSION_NUM = BuildConfig.VERSION_NAME;
    private static final int BUILD_NUM = BuildConfig.VERSION_CODE;
    private static final int TEST_EVENT_TRIGGER_CAP = 10;

    private boolean geofenceEnabled;
    private boolean pushNotificationEnabled;
    private int testEventTriggerCount = 0;

    private SessionM sessionM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sessionM = SessionM.getInstance();
        geofenceEnabled = Utility.getLocalStatusBoolean(Utility.GEOFENCE_ENABLED_KEY);
        pushNotificationEnabled = sessionM.getPushNotificationEnabled();
        String[] settingsList = getResources().getStringArray(R.array.settings_array);
        SettingsListArrayAdapter settingsListArrayAdapter = new SettingsListArrayAdapter(this, settingsList);
        ListView listView = (ListView) this.findViewById(R.id.settings_listview);
        listView.setAdapter(settingsListArrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });
    }

    public class SettingsListArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final String[] values;

        public SettingsListArrayAdapter(Context context, String[] values) {
            super(context, R.layout.settings_item_row, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.settings_item_row, parent, false);
            TextView nameTextView = (TextView) rowView.findViewById(R.id.settings_row_name_textView);
            final CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.settings_row_checkbox);

            nameTextView.setText(values[position]);
            switch (position) {
                //Geofence
                case 0:
                    checkBox.setChecked(geofenceEnabled);
                    checkBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            geofenceEnabled = !geofenceEnabled;
                            checkBox.setChecked(geofenceEnabled);
                            if (geofenceEnabled)
                                GeofenceManager.startGeofenceService(getApplicationContext(), null);
                            else
                                GeofenceManager.stopGeofenceService(getApplicationContext());
                        }
                    });
                    break;
                //Push notification
                case 1:
                    //TODO: update logic here
                    checkBox.setChecked(pushNotificationEnabled);
                    checkBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pushNotificationEnabled = !pushNotificationEnabled;
                            checkBox.setChecked(pushNotificationEnabled);
                            sessionM.setPushNotificationEnabled(pushNotificationEnabled);
                        }
                    });
                    break;
                //Custom loader view
                case 2:
                    checkBox.setChecked(SEApplication.sampleCustomLoaderView != null);
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked)
                                SEApplication.sampleCustomLoaderView = new CustomLoaderView(getApplicationContext());
                            else {
                                SEApplication.sampleCustomLoaderView.removeCustomLoader();
                                SEApplication.sampleCustomLoaderView = null;
                            }
                        }
                    });
                    break;
                //User opt out
                case 3:
                    checkBox.setChecked(sessionM.getUser().isOptedOut());
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked)
                                sessionM.getUser().setOptedOut(getApplicationContext(), true);
                            else
                                sessionM.getUser().setOptedOut(getApplicationContext(), false);
                        }
                    });
                    break;
                //SDK version
                case 4:
                    nameTextView.setText("SDK Version: " + sessionM.getSDKVersion());
                    checkBox.setVisibility(View.GONE);
                    break;
                //App version
                case 5:
                    nameTextView.setText("App Version: " + VERSION_NUM);
                    checkBox.setVisibility(View.GONE);
                    rowView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            testEventTriggerCount++;
                            if (testEventTriggerCount == TEST_EVENT_TRIGGER_CAP)
                                sessionM.logAction("push_trigger");
                        }
                    });
                    break;
                //Exit
                case 6:
                    checkBox.setVisibility(View.GONE);
                    rowView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finishAffinity();
                        }
                    });
                    break;
            }
            return rowView;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Utility.persistStatusBoolean(Utility.PUSH_NOTIFICATION_ENABLED_KEY, pushNotificationEnabled);
        Utility.persistStatusBoolean(Utility.GEOFENCE_ENABLED_KEY, geofenceEnabled);
    }
}
