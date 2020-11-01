package com.jamespfluger.alexadevicefinder.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.jamespfluger.alexadevicefinder.R;
import com.jamespfluger.alexadevicefinder.activities.ui.about.AboutFragment;
import com.jamespfluger.alexadevicefinder.activities.ui.device.DeviceFragment;
import com.jamespfluger.alexadevicefinder.activities.ui.home.HomeFragment;
import com.jamespfluger.alexadevicefinder.api.ApiService;
import com.jamespfluger.alexadevicefinder.api.ManagementInterface;
import com.jamespfluger.alexadevicefinder.models.UserDevice;
import com.jamespfluger.alexadevicefinder.utilities.PreferencesManager;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class DevicesConfigActivity extends AppCompatActivity {
    private AppBarConfiguration mAppBarConfiguration;
    private PreferencesManager preferencesManager;
    private ArrayList<UserDevice> allUserDevices = new ArrayList<UserDevice>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferencesManager = new PreferencesManager(getApplicationContext());

        setContentView(R.layout.activity_device_navigation);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final NavigationView navigationView = findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);

        // Passing each menu ID as a set of Ids because each menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home).setOpenableLayout(drawer).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Create the normal menu items
        final Menu menu = navigationView.getMenu();
        createDefaultMenuItems(drawer, menu);
        /* TODO:
            - Load all devices from API into global list
            - Somehow associate menu items with IDs
            - Pass device into fragments
         */

        getDevices();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.kebab_menu, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    private void getDevices() {
        ManagementInterface managementApi = ApiService.createManagementInstance();
        Call<ArrayList<UserDevice>> userCall = managementApi.getAllUserDevices(preferencesManager.getUserId());
        userCall.enqueue(new Callback<ArrayList<UserDevice>>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    allUserDevices = (ArrayList<UserDevice>) response.body();
                    populateDeviceList();
                } else {
                    try {
                        String errorMessage = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Toast.makeText(DevicesConfigActivity.this, response.code() + " - " + errorMessage, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call call, Throwable t) {

            }
        });
    }

    private void createDefaultMenuItems(final DrawerLayout drawer, final Menu menu) {
        MenuItem homeItem = menu.add(R.id.defaultGroup, View.generateViewId(), Menu.NONE, "Home");
        MenuItem aboutItem = menu.add(R.id.defaultGroup, View.generateViewId(), Menu.NONE, "About");

        homeItem.setOnMenuItemClickListener(buildMenuItemClickListener(new HomeFragment()));
        aboutItem.setOnMenuItemClickListener(buildMenuItemClickListener(new AboutFragment()));
    }

    private void clearAllChecks(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setChecked(false);
        }
    }

    private void populateDeviceList() {
        final NavigationView navigationView = findViewById(R.id.nav_view);
        final Menu menu = navigationView.getMenu();

        for (final UserDevice device : allUserDevices) {
            MenuItem newDeviceMenuItem = menu.add(R.id.devicesGroup, View.generateViewId(), Menu.NONE, device.getDeviceName());
            newDeviceMenuItem.setOnMenuItemClickListener(buildMenuItemClickListener(new DeviceFragment(device)));
        }
    }

    private MenuItem.OnMenuItemClickListener buildMenuItemClickListener(final Fragment newFragment) {
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final NavigationView navigationView = findViewById(R.id.nav_view);
        final Menu menu = navigationView.getMenu();

        return new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.nav_host_fragment, newFragment, "").commit();
                clearAllChecks(menu);
                item.setChecked(true);
                drawer.close();
                return true;
            }
        };
    }
}