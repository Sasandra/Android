package com.example.ola.journeyapp;

import android.*;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;


import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class PrepareActivity extends Activity {

    private static final int acces_all = 1;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] perms = new String[]{ACCESS_FINE_LOCATION, INTERNET, WRITE_EXTERNAL_STORAGE, CAMERA};


        ActivityCompat.requestPermissions(PrepareActivity.this, perms, acces_all);

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode){
            case acces_all:
                if (hasAllPermissionsGranted(grantResults)) {
                    Intent i = new Intent(this, MainActivity1.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    ActivityCompat.finishAffinity(PrepareActivity.this);
                    break;
                }

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;

        }
    }

    public boolean hasAllPermissionsGranted(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }
}
