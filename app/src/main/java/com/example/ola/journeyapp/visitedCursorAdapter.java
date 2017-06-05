package com.example.ola.journeyapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class visitedCursorAdapter extends CursorAdapter {

    public visitedCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    public void bindView(View view, Context context, Cursor cursor) {
        TextView name = (TextView) view.findViewById(R.id.place_name_visited);
        ImageView photo = (ImageView)view.findViewById(R.id.place_photo_visited);

        String title = cursor.getString(cursor.getColumnIndex("Name"));

        name.setText(title);

        byte[] photo_from_base = cursor.getBlob(cursor.getColumnIndex("Photo"));

        if(photo_from_base != null){
            Bitmap bitmap = BitmapFactory.decodeByteArray(photo_from_base, 0, photo_from_base.length);
            photo.setImageBitmap(bitmap);
        }


    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.visited_row, parent, false);
    }
}
