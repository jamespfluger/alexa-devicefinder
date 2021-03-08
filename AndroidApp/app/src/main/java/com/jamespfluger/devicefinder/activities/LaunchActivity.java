package com.jamespfluger.devicefinder.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.api.Listener;
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager;
import com.amazon.identity.auth.device.api.authorization.AuthorizeResult;
import com.amazon.identity.auth.device.api.authorization.ProfileScope;
import com.amazon.identity.auth.device.api.authorization.Scope;
import com.jamespfluger.devicefinder.R;
import com.jamespfluger.devicefinder.utilities.AmazonLoginHelper;
import com.jamespfluger.devicefinder.utilities.PermissionsRequester;
import com.jamespfluger.devicefinder.utilities.PreferencesManager;

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferencesManager preferencesManager = new PreferencesManager(getApplicationContext());
        preferencesManager.refreshDeviceId();

        setContentView(R.layout.activity_launch);
        selectActivityToLaunch();
    }

    private void selectActivityToLaunch() {
        Scope[] scopes = {ProfileScope.userId()};

        AuthorizationManager.getToken(this, scopes, new Listener<AuthorizeResult, AuthError>() {
            Intent intentToLaunch;

            @Override
            public void onSuccess(AuthorizeResult result) {
                PermissionsRequester permissionsRequester = new PermissionsRequester();
                permissionsRequester.requestPermissions(LaunchActivity.this);

                if (result.getAccessToken() != null) {
                    AmazonLoginHelper.setUserId(getApplicationContext());
                    switchToActivity(LoginActivity.class);
                } else {
                    switchToActivity(LoginActivity.class);
                }
            }

            @Override
            public void onError(AuthError ae) {
                switchToActivity(LoginActivity.class);
            }
        });
    }

    private void switchToActivity(Class<?> newActivity) {
        Intent newIntent = new Intent(this, newActivity);
        startActivity(newIntent);
        finish();
    }
}