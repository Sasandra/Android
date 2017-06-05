package com.example.ola.journeyapp;


import android.*;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import com.google.android.gms.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;


public class MainActivity1 extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {



    private GoogleMap mMap;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location myLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private boolean mRequestLocationUpdates = false;
    private LocationRequest mLocationRequest;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 100 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 10; // 15 seconds

    private double myLati;
    private double myLongi;

    private String[] to_visit_list;
    private Marker myMarker;
    private SQLiteDatabase db;
    private HashMap<Marker, MarkerInfo> eventMarkerMap;
    private String place_name;
    private  BitmapDescriptor markerIcon;


    protected void onCreate(Bundle savedInstanceState) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity1.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if(checkPlayServices()){
            buildGoogleApiClient();
            createLocationRequest();
        }

        Drawable circleDrawable = getResources().getDrawable(R.drawable.dot);
        markerIcon = getMarkerIconFromDrawable(circleDrawable);

        eventMarkerMap = new HashMap<Marker, MarkerInfo>();

    }

    protected void onStart() {
        if(mGoogleApiClient != null){
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    public void onRestart() {
        super.onRestart();
        setToVisit();
        setVisited();
    }

    protected void onResume(){
        super.onResume();
        checkPlayServices();
        mGoogleApiClient.connect();

        if(mGoogleApiClient.isConnected() && mRequestLocationUpdates){
            startLocationUpdates();
        }

        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
        if (mMap != null) {

            mMap.clear();


            MarkerOptions a = new MarkerOptions()
                    .position(new LatLng(myLati, myLongi))
                    .title("Ty")
                    .icon(markerIcon);

            myMarker = mMap.addMarker(a);

            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(myLati, myLongi)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLati, myLongi), 18));


            setToVisit();
            setVisited();
        }
    }

    protected void onStop(){
        super.onStop();

        if(mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

    protected void onPause(){
        super.onPause();
        stopLocationUpdates();
    }

    private void handleLocation(){

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity1.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        myLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(myLastLocation != null){
            myLati = myLastLocation.getLatitude();
            myLongi = myLastLocation.getLongitude();

            Log.e("!!!!!!", String.valueOf(myLati) + " " + String.valueOf(myLongi));

            LatLng position = new LatLng(myLati, myLongi);

            if(myMarker == null){
                MarkerOptions a = new MarkerOptions()
                    .position(new LatLng(myLati, myLongi))
                    .title("Ty")
                    .icon(markerIcon);

                myMarker = mMap.addMarker(a);

            }else{
                myMarker.setPosition(position);
            }


            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(myLati, myLongi)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLati, myLongi), 18));

        }else{
            //włącz lokalizacje
        }
    }

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void togglePeriodLocationUpdates(){
        if(!mRequestLocationUpdates){
            mRequestLocationUpdates = true;
            startLocationUpdates();
        }else{
            mRequestLocationUpdates = false;
            stopLocationUpdates();
        }
    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    protected void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(MIN_TIME_BW_UPDATES);
        mLocationRequest.setFastestInterval(1 * 1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(MIN_DISTANCE_CHANGE_FOR_UPDATES);
    }

    private boolean checkPlayServices(){
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }else{
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    protected void startLocationUpdates(){

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(MainActivity1.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                MarkerInfo eventInfo = eventMarkerMap.get(marker);

                if(eventInfo != null){
                    Intent i = new Intent(MainActivity1.this, visitedDetail.class);
                    i.putExtra("ID", (int)eventInfo.getId());

                    Log.e("!!!!!!!!!!!!", String.valueOf(eventInfo.getId()));
                    startActivity(i);

                }
            }});


        setToVisit();
        setVisited();

    }

    @Override
    public void onLocationChanged(Location location) {
        myLastLocation = location;
        handleLocation();
        calculateDistance();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        handleLocation();
        if(mRequestLocationUpdates){
            startLocationUpdates();
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(myLati, myLongi)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLati, myLongi), 18));
        togglePeriodLocationUpdates();

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

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

            if(disnatnceInMeters < 200){
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

        Intent resultIntent = new Intent(this, MainActivity1.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity1.class);

        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mBuilder.build().flags |= Notification.FLAG_AUTO_CANCEL;

        mNotificationManager.notify(12345, mBuilder.build());

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
            cursor.close();

        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(this, "Baza danych jest niedostępna.", Toast.LENGTH_SHORT);
            toast.show();
        }

        db.close();

    }

    public void take_my_location(View v){
        handleLocation();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(myLati, myLongi)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLati, myLongi), 18));
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

            cursor.close();

        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(this, "Baza danych jest niedostępna.", Toast.LENGTH_SHORT);
            toast.show();
        }

        db.close();

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
}
