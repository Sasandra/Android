package com.example.ola.journeyapp;


import android.content.Context;
import android.database.Cursor;
import android.renderscript.Double2;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class myCursorAdapter extends CursorAdapter{
    private LayoutInflater cursorInflater;

    public myCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    public void bindView(View view, Context context, Cursor cursor) {
        TextView name = (TextView) view.findViewById(R.id.name);
        TextView longitude = (TextView) view.findViewById(R.id.longi);
        TextView latitude = (TextView) view.findViewById(R.id.lati);

        String title = cursor.getString( cursor.getColumnIndex("Name"));
        double lati = cursor.getDouble(cursor.getColumnIndex("Latitude"));
        double longi = cursor.getDouble(cursor.getColumnIndex("Longitude"));

        name.setText(title);
        longitude.setText(Double.toString(longi));
        latitude.setText(Double.toString(lati));
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.row, parent, false);
    }

}
