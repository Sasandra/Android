package com.example.ola.journeyapp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class addVisited extends Activity {

    private static final int CAMERA_REQUEST = 1888;
    private Bitmap photo;
    private ImageView imageView;
    private EditText place_n;
    private EditText note;
    private double lati;
    private double longi;
    private String place_name;
    private String note_to_save;
    private SQLiteDatabase db;
    private SQLiteOpenHelper databaseHelper;


    public String fname;
    public String name;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_visited);

        imageView = (ImageView)findViewById(R.id.photo);
        place_n = (EditText)findViewById(R.id.place_name);
        note = (EditText)findViewById(R.id.note);
        lati = (double)getIntent().getExtras().get("lati");
        longi = (double)getIntent().getExtras().get("longi");
        place_name = (String)getIntent().getExtras().get("name");

        databaseHelper = new DatabaseHelper(this);

        place_n.setText(place_name);


    }

    public void takePhoto(View v){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(addVisited.this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(addVisited.this, new String[]{Manifest.permission.CAMERA}, 0);
            } else {
                ActivityCompat.requestPermissions(addVisited.this, new String[]{Manifest.permission.CAMERA}, 0);
            }
        }else{
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, CAMERA_REQUEST);
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);


            if (ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(addVisited.this, WRITE_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions(addVisited.this, new String[]{WRITE_EXTERNAL_STORAGE}, 0);
                } else {
                    ActivityCompat.requestPermissions(addVisited.this, new String[]{WRITE_EXTERNAL_STORAGE}, 0);
                }
            }
        }
    }

    public void add_visited(View v){
        note_to_save = note.getText().toString();
        place_name = place_n.getText().toString();

        if(!place_name.equals("")){
            db = databaseHelper.getWritableDatabase();

            ContentValues new_visited = new ContentValues();

            new_visited.put("Name", place_name);
            new_visited.put("Latitude", lati);
            new_visited.put("Longitude", longi);
            new_visited.put("Note", note_to_save);
            new_visited.put("Date", getDateTime());

            if(photo != null){
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                new_visited.put("Photo",byteArray);
            }



            db.insert("visited", null, new_visited);
            db.close();

            saveImage(new BitmapDrawable(getResources(), photo));


        }else{

            Toast toast = Toast.makeText(addVisited.this, "You must type a place!", Toast.LENGTH_SHORT);
            toast.show();

        }


        finish();
    }

    public void showPhoto(View v){
        Intent i = new Intent(this, PhotoDetails.class);
        i.putExtra("photo", photo);
        startActivity(i);
    }

    public void saveImage(BitmapDrawable bitmap){

        String root = Environment.getExternalStorageDirectory().toString();

        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        fname = "Image-" + n + ".jpg";
        name = fname;
        File file = new File (myDir, fname);

        if (file.exists ()) {
            file.delete();

        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }



}
