package com.example.ola.journeyapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Ola on 2017-05-29.
 */

public class visitedDetail extends Activity {

    private TextView place_name;
    private TextView place_lati;
    private TextView place_longi;
    private TextView place_note;
    private ImageView place_photo;
    private TextView place_date;
    private Bitmap photo_to_show;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.visited_detail);

        int id = (Integer)getIntent().getExtras().get("ID");

        place_name = (TextView)findViewById(R.id.detail_name);
        place_lati = (TextView)findViewById(R.id.detail_lati);
        place_longi = (TextView)findViewById(R.id.detail_longi);
        place_note = (TextView)findViewById(R.id.detail_note);
        place_photo = (ImageView)findViewById(R.id.detail_photo);
        place_date = (TextView)findViewById(R.id.detail_date);

        try{
            SQLiteOpenHelper databaseHelper = new DatabaseHelper(this);
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            Cursor cursor = db.query("visited", new String[]{"Name", "Latitude", "Longitude", "Note", "Photo", "Date"}, "_id = ?", new String[]{Integer.toString(id)}, null, null, null);
            if(cursor.moveToFirst()){


                String name = cursor.getString(0);
                double lati = cursor.getDouble(1);
                double longi = cursor.getDouble(2);
                String note = cursor.getString(3);
                byte[] photo_from_base = cursor.getBlob(4);

                place_name.setText(name);
                place_lati.setText(Double.toString(lati));
                place_longi.setText(Double.toString(longi));
                place_date.setText(cursor.getString(5));

                if(!note.equals("")){
                    place_note.setText(note);
                }else{
                    place_note.setText("You didn't add note :(");
                }


                if(photo_from_base != null){
                    photo_to_show = BitmapFactory.decodeByteArray(photo_from_base, 0, photo_from_base.length);
                    place_photo.setImageBitmap(photo_to_show);
                }

            }
            cursor.close();
            db.close();

        }catch(SQLiteException e){
            Toast toast = Toast.makeText(this, "Baza danych jest niedostępna.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void showPhotoDetail(View v){
        Intent i = new Intent(this, PhotoDetails.class);
        i.putExtra("photo", photo_to_show);
        startActivity(i);
    }




}
