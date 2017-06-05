package com.example.ola.journeyapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import com.google.android.gms.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    private String place_name;
    private SQLiteDatabase db;
    private String[] to_visit_list;


    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationManager locationManager;
    private LocationSettingsRequest mLocationSettingsRequest;
    private Location location;
    private double myLati;
    private double myLongi;
    private Marker myMarker;

    private HashMap<Marker, MarkerInfo> eventMarkerMap;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 100 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 10; // 15 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        eventMarkerMap = new HashMap<Marker, MarkerInfo>();


        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(MIN_TIME_BW_UPDATES)        // 15 seconds, in milliseconds
                .setFastestInterval(1 * 1000)
                .setExpirationDuration(MIN_DISTANCE_CHANGE_FOR_UPDATES); // 1 second, in milliseconds

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);



        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {



            @Override

            public void onInfoWindowClick(Marker marker) {

                MarkerInfo eventInfo = eventMarkerMap.get(marker);

                Intent i = new Intent(MainActivity.this, visitedDetail.class);
                i.putExtra("ID", (int)eventInfo.getId());

                Log.e("!!!!!!!!!!!!", String.valueOf(eventInfo.getId()));
                startActivity(i);

        }});

       // mMap.setMyLocationEnabled(true);

//        MarkerOptions a = new MarkerOptions()
//                .position(new LatLng(0, 0))
//                .title("Ty")
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
//
//        myMarker = mMap.addMarker(a);

        setToVisit();
        setVisited();
    }

    public void goToVisit(View view) {
        Intent intent = new Intent(this, toVisitList.class);
        startActivity(intent);

    }

    public void goToVisitedList(View v) {
        Intent i = new Intent(this, visitedLIst.class);
        startActivity(i);
    }

    public void goToCamera(View v) {
        Intent intent = new Intent(this, addVisited.class);

        Geocoder geocoder = new Geocoder(this);
        place_name = "Unknown localization";


        try {
            List<Address> addresses = geocoder.getFromLocation(myLati, myLongi, 1);
            if (addresses.size() > 0) {
                place_name = addresses.get(0).getLocality() + ", " + addresses.get(0).getThoroughfare();
            }

        } catch (IOException e) {

        }

        intent.putExtra("name", place_name);
        intent.putExtra("lati", myLati);
        intent.putExtra("longi", myLongi);
        startActivity(intent);
    }

    public void setToVisit() {

        getArrayFromDataBase();

        for (int i = 0; i < to_visit_list.length; i++) {
            String[] row = to_visit_list[i].split(";");
            String name = row[0];
            double lati = Double.valueOf(row[1]);
            double longi = Double.valueOf(row[2]);

            LatLng pos = new LatLng(lati, longi);
            mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title("To visit: " + name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .alpha(0.7f));
        }
    }

    public void setVisited() {

        try {

            SQLiteOpenHelper databaseHelper = new DatabaseHelper(this);
            db = databaseHelper.getReadableDatabase();
            Cursor cursor = db.query("visited", new String[]{"_id", "Name", "Latitude", "Longitude"}, null, null, null, null, null);

            double longi;
            double lati;
            String name;

            while (cursor.moveToNext()) {
                longi = cursor.getDouble(3);
                lati = cursor.getDouble(2);
                name = cursor.getString(1);
                LatLng pos = new LatLng(lati, longi);

                Marker a = mMap.addMarker(new MarkerOptions()
                        .position(pos)
                        .title("Visited: " + name)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));


                int id = cursor.getInt(0);
                Log.e("!!!!!!!!!!!!", String.valueOf(id));
                eventMarkerMap.put(a, new MarkerInfo(id));
            }

        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(this, "Baza danych jest niedostępna.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void getArrayFromDataBase() {
        try {

            SQLiteOpenHelper databaseHelper = new DatabaseHelper(this);
            db = databaseHelper.getReadableDatabase();
            Cursor cursor = db.query("toVisit", new String[]{"_id", "Name", "Latitude", "Longitude"}, null, null, null, null, null);
            to_visit_list = new String[cursor.getCount()];

            double longi;
            double lati;
            String name;

            int i = 0;
            while (cursor.moveToNext()) {
                longi = cursor.getDouble(3);
                lati = cursor.getDouble(2);
                name = cursor.getString(1);
                to_visit_list[i] = name + ';' + Double.toString(lati) + ';' + Double.toString(longi);
                i++;
            }

        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(this, "Baza danych jest niedostępna.", Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    public void onRestart() {
        super.onRestart();
        setToVisit();
        setVisited();
    }


    public void onResume() {
        super.onResume();


        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
        if (mMap != null) {
            mMap.clear();

//            MarkerOptions a = new MarkerOptions()
//                    .position(new LatLng(0, 0))
//                    .title("Ty")
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
//
//            myMarker = mMap.addMarker(a);

            setToVisit();
            setVisited();
        }

        mGoogleApiClient.connect();
    }

    public void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    private void handleNewLocation(Location location) {

        Log.e("!!!!!!!!!!!!!!!!!!!", "handle");

        myLati = location.getLatitude();
        myLongi = location.getLongitude();

        LatLng position = new LatLng(myLati, myLongi);

      //  myMarker.setPosition(position);

//        MarkerOptions a = new MarkerOptions()
//                .position(new LatLng(myLati, myLongi))
//                .title("Ty")
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
//
//        myMarker = mMap.addMarker(a);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 18));

    }


    @Override
    public void onLocationChanged(Location location) {
        Log.e("!!!!!!", "onCHangeLocation");
        handleNewLocation(location);
        calculateDistance();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }

       location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
       //location = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        if (location != null) {
            Log.e("!!!!!!!!!!!!!", "onConnected");
            handleNewLocation(location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void calculateDistance() {

        Location myLocation = new Location("");
        myLocation.setLatitude(myLati);
        myLocation.setLongitude(myLongi);

        for(int i = 0; i < to_visit_list.length; i++) {
            String[] row = to_visit_list[i].split(";");
            String name = row[0];
            double lati = Double.valueOf(row[1]);
            double longi = Double.valueOf(row[2]);

            Location loc = new Location("");
            loc.setLatitude(lati);
            loc.setLongitude(longi);

            float disnatnceInMeters = myLocation.distanceTo(loc);

            if(disnatnceInMeters < 1000){
                sendNotification(name, disnatnceInMeters);
            }



        }
    }

    private void sendNotification(String Name, float distance){

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setAutoCancel(true);
        mBuilder.setSmallIcon(R.drawable.aparat);
        mBuilder.setContentTitle("You are close to:");
        mBuilder.setContentText(Name +": "+ distance);


        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);

        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        mBuilder.build().flags |= Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(12345, mBuilder.build());

    }
}
