package com.Tapp.Anonymeet.GPS;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.Tapp.Anonymeet.FireBaseChat.MessagesActivity;
import com.Tapp.Anonymeet.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class FindPeopleActivity extends AppCompatActivity {

    private Toolbar toolbar;
    static boolean isRunning;
    ViewPager pager;
    static FindPeopleFragment f1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_people_activity);

        FragmentManager manager = getSupportFragmentManager();
        f1 =  new FindPeopleFragment();
        pager = (ViewPager) findViewById(R.id.viewPager);
        pager.setAdapter(new FragmentPagerAdapter(manager) {
            @Override
            public Fragment getItem(int position) {
                return f1;
            }

            @Override
            public int getCount() {
                return 1;
            }
        });

        toolbar = (Toolbar) findViewById(R.id.toolBar2);
        toolbar.setTitle("Find People");
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();

        isRunning = true;
        try {
            LocationListenerService.cancelNotification();
        } catch (NullPointerException e) {
        }
    }

    public void onStop() {
        super.onStop();

        FindPeopleFragment.exit();
    }

    public static void updateMessage() {
        f1.updateMessage();
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public static void updateList() {
        f1.updateList();
    }

    public static void clearAdapter() {
        f1.clearAdapter();
    }

    public void enableLocationServices(View view) {
        if (LocationListenerService.providerEnabled || LocationListenerService.getApi() == null) return;

        if (Build.VERSION.SDK_INT >= 22) locationChecker(LocationListenerService.getApi(), this);

        else if (!LocationListenerService.providerEnabled) {
            final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);

            LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            GpsStatus.Listener listener = new GpsStatus.Listener() {
                @Override
                public void onGpsStatusChanged(int event) {
                    if (event == GpsStatus.GPS_EVENT_STARTED) finishActivity(0);
                }
            };

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            manager.addGpsStatusListener(new GpsStatus.Listener() {
                @Override
                public void onGpsStatusChanged(int event) {
                    if (event == GpsStatus.GPS_EVENT_STARTED) finishActivity(0);
                }
            });
            manager.removeGpsStatusListener(listener);
        }
    }

    public void locationChecker(GoogleApiClient mGoogleApiClient, final Activity activity) {

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                                     @Override
                                     public void onResult(LocationSettingsResult result) {
                                         final Status status = result.getStatus();
                                         final LocationSettingsStates state = result.getLocationSettingsStates();
                                         switch (status.getStatusCode()) {
                                             case LocationSettingsStatusCodes.SUCCESS:
                                                 break;
                                             case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                                 try {
                                                     status.startResolutionForResult(
                                                             activity, 1000);
                                                 } catch (IntentSender.SendIntentException e) {
                                                 }
                                                 break;
                                             case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                                 break;
                                         }
                                     }
                                 }
        );
    }

    public void goToMessagesActivity(View view) {
        startActivity(new Intent(this, MessagesActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gps_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout_item)
            logoutMessage();

        return true;
    }

    private void logoutMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure?");
        builder.setMessage("You will lose all of your data.");
        builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                f1.onLogout();
            }
        });
        builder.setNegativeButton("Cancel", null);

        Dialog dialog = builder.create();
        dialog.show();
    }
}