package com.example.or.anonymeet.GPS;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.or.anonymeet.FireBaseChat.MessagesActivity;
import com.example.or.anonymeet.R;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class GPSActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 1000;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;

    private Firebase onlineUsers;
    private String userId;
    private ListView listView;
    private Toolbar toolbar;
    LocationManager lm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gps_layout);
//
        toolbar = (Toolbar) findViewById(R.id.toolBar2);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Find People");

        lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        onlineUsers = new Firebase("https://anonymeetapp.firebaseio.com/OnlineUsers");
        userId = getIntent().getStringExtra("userId");

        buildGoogleApiClient();

        listView = (ListView) findViewById(R.id.listView);

        onlineUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> iter = dataSnapshot.getChildren();

                Collection<String> list = new ArrayList<String>();
                for (DataSnapshot item : iter) {
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    double lat = 0;
                    double longt = 0;
                    try {
                        lat = (double) item.child("lat").getValue();
                        longt = (double) item.child("long").getValue();
                    } catch (NullPointerException e) {

                    }
                    Address address = null;
                    try {
                        address = geocoder.getFromLocation(lat, longt, 1).get(0);
                        list.add(address.getAddressLine(0));
                    } catch (IOException e) {
                    }catch ( IndexOutOfBoundsException er){}
                }
                final String[] arr = list.toArray(new String[list.size()]);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.select_dialog_item, arr);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //Intent intent = new Intent(getApplicationContext(), OtherClass);
                        //intent.putExtra("userId", arr[position]);
                        //startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        locationProvider();
    }

    public void locationProvider() {

        if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Location Services Not Active");
            builder.setMessage("Please enable Location Services and GPS");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });

            builder.setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            builder.setCancelable(false);

            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gps_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.logout_item) onlineUsers.unauth();

        if (item.getItemId() == R.id.or_item) Snackbar.make(listView, "Yes!!!", Snackbar.LENGTH_SHORT).show();

        return super.onOptionsItemSelected(item);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }

        super.onPause();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();

        onlineUsers.child(userId).runTransaction(new Transaction.Handler() {
            public Transaction.Result doTransaction(MutableData mutableData) {
                mutableData.setValue(null);
                return Transaction.success(mutableData);
            }
            public void onComplete(FirebaseError error, boolean b, DataSnapshot data) {
            }
        });
        super.onStop();
    }

            @Override
            public void onConnected(Bundle connectionHint) {

                if (mCurrentLocation == null) {
                    mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                }

                startLocationUpdates();
            }

            @Override
            public void onLocationChanged(Location location) {
                mCurrentLocation = location;
                onlineUsers.child(userId).child("long").setValue(location.getLongitude());
                onlineUsers.child(userId).child("lat").setValue(location.getLatitude());
            }

            @Override
            public void onConnectionSuspended(int cause) {
                mGoogleApiClient.connect();
            }

            @Override
            public void onConnectionFailed(ConnectionResult result) {
            }

    public void goToMessagesActivity(View view) {
        startActivity(new Intent(this, MessagesActivity.class));
    }
}