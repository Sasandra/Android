package com.example.ola.journeyapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;


public class PhotoDetails extends Activity {

    private ImageView full_photo;
    private Bitmap photo;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_detail);

        full_photo = (ImageView)findViewById(R.id.full_photo);
        photo = (Bitmap) getIntent().getExtras().get("photo");
        if(photo != null) {
            full_photo.setImageBitmap(photo);
        }

    }
}
