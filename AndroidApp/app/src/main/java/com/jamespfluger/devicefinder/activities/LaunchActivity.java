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
import com.jamespfluger.devicefinder.notifications.NotificationForge;
import com.jamespfluger.devicefinder.settings.ConfigManager;
import com.jamespfluger.devicefinder.settings.SettingsManager;
import com.jamespfluger.devicefinder.utilities.AmazonLoginHelper;

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SettingsManager.setInstance(getApplicationContext());
        ConfigManager.setInstance(getApplicationContext());
        NotificationForge.initializeNotificationChannel(getApplicationContext());

        ConfigManager.refreshFirebaseToken();

        setContentView(R.layout.activity_launch);
        switchToActivity(LoginActivity.class);
        //selectActivityToLaunch();
    }

    private void selectActivityToLaunch() {
        Scope[] scopes = {ProfileScope.userId()};

        AuthorizationManager.getToken(this, scopes, new Listener<AuthorizeResult, AuthError>() {
            @Override
            public void onSuccess(AuthorizeResult result) {
                if (result.getAccessToken() != null && ConfigManager.getAlexaUserIdConfig() != null) {
                    AmazonLoginHelper.setUserId(getApplicationContext());
                    switchToActivity(DevicesConfigActivity.class);
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
