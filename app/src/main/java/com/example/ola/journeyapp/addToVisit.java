package com.example.ola.journeyapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class addToVisit extends FragmentActivity implements OnMapReadyCallback {

    private EditText nameText;
    private SQLiteDatabase db;
    private SQLiteOpenHelper databaseHelper;
    private GoogleMap mMap;
    private List<Address> addresses;
    private Geocoder geocoder;
    private double latitude;
    private double longitude;
    private Marker marker;

    private ListView places;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_to_visit_2);

        nameText = (EditText) findViewById(R.id.add_name);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.add_map);
        mapFragment.getMapAsync(this);


        databaseHelper = new DatabaseHelper(this);
        places = (ListView) findViewById(R.id.places_list);
        places.setAdapter(new placesArrayAdapter(this, new ArrayList<Address>()));


        places.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Address addrr = (Address)parent.getAdapter().getItem(position);
                latitude = addrr.getLatitude();
                longitude = addrr.getLongitude();

                LatLng pos = new LatLng(latitude, longitude);

                mMap.clear();
                marker = mMap.addMarker(new MarkerOptions().position(pos).title(""));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 12));

            }
        });

        geocoder = new Geocoder(this);

        if (!isOnline()) {
            showNetworkAlert();
        }

        final Button cancelButton = (Button) findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        final Button resetButton = (Button) findViewById(R.id.reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                nameText.setText("");

                mMap.clear();
                LatLng sydney = new LatLng(0, 0);
                marker = mMap.addMarker(new MarkerOptions().position(sydney).title(""));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 0));

                places.setAdapter(null);
            }
        });

        final Button findButton = (Button) findViewById(R.id.find);
        findButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String city = nameText.getText().toString();
                places.setAdapter(null);

                try {
                    addresses = geocoder.getFromLocationName(city, 10);
                    if (addresses.size() == 1) {
                        latitude = addresses.get(0).getLatitude();
                        longitude = addresses.get(0).getLongitude();

                        LatLng position = new LatLng(latitude, longitude);

                        if (marker != null) {
                            marker.setPosition(position);
                        } else {
                            mMap.clear();
                            marker = mMap.addMarker(new MarkerOptions().position(position).title(""));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                        }

                        mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 12));

                    }else if(addresses.size() > 1){
                        ArrayList<Address> array = new ArrayList<Address>(addresses.size());
                        array.addAll(addresses);
                        placesArrayAdapter adapter = new placesArrayAdapter(addToVisit.this, array);

                        places.setAdapter(adapter);

                    }
                    else{
                        Toast.makeText(addToVisit.this, "I can't find location :(", Toast.LENGTH_LONG).show();
                    }

                } catch (IOException e) {
                    // handle the exception
                }

            }
        });

        final Button submitButton = (Button) findViewById(R.id.save);
        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String name = nameText.getText().toString();

                if (!name.equals("")) {
                    db = databaseHelper.getWritableDatabase();

                    ContentValues new_to_visit = new ContentValues();

                    new_to_visit.put("Name", name);
                    new_to_visit.put("Latitude", latitude);
                    new_to_visit.put("Longitude", longitude);

                    db.insert("toVisit", null, new_to_visit);

                    finish();
                    db.close();

                } else {

                    Toast toast = Toast.makeText(addToVisit.this, "You must type a place!", Toast.LENGTH_SHORT);
                    toast.show();

                }

            }
        });
    }

    private void askForPermissions() {

        if (ActivityCompat.checkSelfPermission(this, INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(addToVisit.this, INTERNET)) {
                ActivityCompat.requestPermissions(addToVisit.this, new String[]{INTERNET}, 0);
            } else {
                ActivityCompat.requestPermissions(addToVisit.this, new String[]{INTERNET}, 0);
            }
        }


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng sydney = new LatLng(latitude, longitude);
        marker = mMap.addMarker(new MarkerOptions().position(sydney).title(""));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {

                mMap.clear();
                marker = null;

                places.setAdapter(null);

                MarkerOptions marker = new MarkerOptions().position(
                        new LatLng(point.latitude, point.longitude)).title("");

                latitude = point.latitude;
                longitude = point.longitude;

                try{
                    addresses = geocoder.getFromLocation(latitude,longitude,1);
                    if(addresses.size() > 0){
                        if(addresses.get(0).getFeatureName() != null){
                            nameText.setText(addresses.get(0).getCountryName() +", " + addresses.get(0).getFeatureName());
                        }
                        else{
                            nameText.setText(addresses.get(0).getCountryName());
                        }
                    }
                }catch (IOException e){

                }

                mMap.addMarker(marker);
            }
        });

    }

    public void onDestroy() {
        super.onDestroy();
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void showNetworkAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("Network settings");

        alertDialog.setMessage("Network is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }
}
