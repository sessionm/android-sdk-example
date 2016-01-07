/*
 * Copyright (c) 2015 SessionM. All rights reserved.
 */

package com.sessionm.example.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.sessionm.api.SessionM;
import com.sessionm.example.R;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private static final String TEST_AUTH_PROVIDER = "sessionm";
    private static final String TEST_AUTH_TOKEN = "test";

    LinearLayout loginLayout;
    LinearLayout profileLayout;
    EditText emailEditText;
    EditText passwordEditText;
    EditText confirmPwdEditText;
    EditText yobEditText;
    EditText genderEditText;
    EditText zipCodeEditText;
    Switch typeSwitch;
    Button button;
    Button logoutButton;
    Button authLoginButton;
    FeedImageView imageView;

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        loginLayout = (LinearLayout) view.findViewById(R.id.login_signup_layout);
        profileLayout = (LinearLayout) view.findViewById(R.id.profile_layout);
        emailEditText = (EditText) view.findViewById(R.id.login_email_edittext);
        passwordEditText = (EditText) view.findViewById(R.id.login_password_edittext);
        confirmPwdEditText = (EditText) view.findViewById(R.id.signup_confirm_password_edittext);
        yobEditText = (EditText) view.findViewById(R.id.signup_yob_edittext);
        genderEditText = (EditText) view.findViewById(R.id.signup_gender_edittext);
        zipCodeEditText = (EditText) view.findViewById(R.id.signup_zip_code_edittext);
        typeSwitch = (Switch) view.findViewById(R.id.login_or_signup_switch);
        button = (Button) view.findViewById(R.id.login_button);
        logoutButton = (Button) view.findViewById(R.id.logout_button);
        authLoginButton = (Button) view.findViewById(R.id.auth_login_button);
        imageView = (FeedImageView) view.findViewById(R.id.profile_image);

        typeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    confirmPwdEditText.setVisibility(View.VISIBLE);
                    yobEditText.setVisibility(View.VISIBLE);
                    genderEditText.setVisibility(View.VISIBLE);
                    zipCodeEditText.setVisibility(View.VISIBLE);
                    button.setText("Sign up");
                } else {
                    confirmPwdEditText.setVisibility(View.GONE);
                    yobEditText.setVisibility(View.GONE);
                    genderEditText.setVisibility(View.GONE);
                    zipCodeEditText.setVisibility(View.GONE);
                    button.setText("Log in");
                }
            }
        });

        button.setOnClickListener(this);
        logoutButton.setOnClickListener(this);
        authLoginButton.setOnClickListener(this);
        setCurrentLayout(SessionM.getInstance().getUser().isRegistered());
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_button:
                if (typeSwitch.isChecked()) {
                    Map<String, String> userData = new HashMap<String, String>();
                    userData.put(SessionM.USER_DATA_EMAIL_KEY, emailEditText.getText().toString());
                    userData.put(SessionM.USER_DATA_PASSWORD_KEY, passwordEditText.getText().toString());
                    userData.put(SessionM.USER_DATA_BIRTH_YEAR_KEY, yobEditText.getText().toString());
                    userData.put(SessionM.USER_DATA_GENDER_KEY, genderEditText.getText().toString());
                    userData.put(SessionM.USER_DATA_ZIP_CODE_KEY, zipCodeEditText.getText().toString());
                    if (!SessionM.getInstance().signUpUserWithData(userData))
                        Toast.makeText(getActivity(), "Failed: Invalid input.", Toast.LENGTH_SHORT).show();
                } else {
                    if (!SessionM.getInstance().logInUserWithEmail(emailEditText.getText().toString(), passwordEditText.getText().toString()))
                        //if (!SessionM.getInstance().logInUserWithEmail(TEST_USERNAME, TEST_PASSWORD))
                        Toast.makeText(getActivity(), "Failed: Invalid input.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.logout_button:
                SessionM.getInstance().logOutUser();
                break;
            case R.id.auth_login_button:
                SessionM.getInstance().authenticateWithToken(TEST_AUTH_PROVIDER, TEST_AUTH_TOKEN);
                break;
            default:
                break;
        }
    }

    public void setCurrentLayout(boolean isLoggedIn) {
        if (isLoggedIn && loginLayout.getVisibility() == View.VISIBLE) {
            profileLayout.setVisibility(View.VISIBLE);
            loginLayout.setVisibility(View.GONE);
            //setProfileImage(SessionM.getInstance().getUser().getPhotoUrl());
        } else if (!isLoggedIn && loginLayout.getVisibility() == View.GONE) {
            profileLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
        }
    }
}
