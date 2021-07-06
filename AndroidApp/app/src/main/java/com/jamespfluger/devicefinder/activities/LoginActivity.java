package com.jamespfluger.devicefinder.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.api.authorization.AuthCancellation;
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager;
import com.amazon.identity.auth.device.api.authorization.AuthorizeListener;
import com.amazon.identity.auth.device.api.authorization.AuthorizeRequest;
import com.amazon.identity.auth.device.api.authorization.AuthorizeResult;
import com.amazon.identity.auth.device.api.authorization.ProfileScope;
import com.amazon.identity.auth.device.api.workflow.RequestContext;
import com.jamespfluger.devicefinder.R;
import com.jamespfluger.devicefinder.utilities.AmazonLoginHelper;

public class LoginActivity extends AppCompatActivity {
    private RequestContext requestContext;
    private Button loginButton;
    private TextView loginText;
    private ProgressBar loginProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestContext = RequestContext.create(getApplicationContext());
        requestContext.registerListener(new AuthorizeListener() {
            @Override
            public void onSuccess(AuthorizeResult authorizeResult) {
                AmazonLoginHelper.setUserId(getApplicationContext());
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), R.string.amazon_login_success, Toast.LENGTH_SHORT).show());
                switchToActivity();
            }

            @Override
            public void onError(AuthError authError) {
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), R.string.amazon_login_error, Toast.LENGTH_SHORT).show();
                    setLoggingInState(false);
                });
            }

            @Override
            public void onCancel(AuthCancellation authCancellation) {
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), R.string.amazon_login_cancelled, Toast.LENGTH_SHORT).show();
                    setLoggingInState(false);
                });
            }
        });

        setContentView(R.layout.activity_login);
        initializeUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestContext.onResume();
    }

    private void initializeUI() {
        loginProgressBar = findViewById(R.id.loginProgressBar);
        loginButton = findViewById(R.id.loginButton);
        loginText = findViewById(R.id.loginText);

        loginButton.setOnClickListener(view -> {
            AuthorizationManager.authorize(
                    new AuthorizeRequest.Builder(requestContext)
                            .addScopes(ProfileScope.userId())
                            .build()
            );
            setLoggingInState(true);
        });
    }

    private void setLoggingInState(final boolean loggingIn) {
        if (loggingIn) {
            loginButton.setVisibility(Button.GONE);
            loginText.setVisibility(TextView.GONE);
            loginProgressBar.setVisibility(ProgressBar.VISIBLE);
        } else {
            loginButton.setVisibility(Button.VISIBLE);
            loginText.setVisibility(TextView.VISIBLE);
            loginProgressBar.setVisibility(ProgressBar.GONE);
        }
    }

    private void switchToActivity() {
        Intent newIntent = new Intent(this, PermissionsActivity.class);
        startActivity(newIntent);
        finish();
    }
}
