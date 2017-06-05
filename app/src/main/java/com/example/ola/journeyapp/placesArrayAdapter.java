package com.example.ola.journeyapp;

import android.content.Context;
import android.location.Address;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class placesArrayAdapter extends ArrayAdapter<Address>{

    private ArrayList<Address> places_list;

    public placesArrayAdapter(Context context, ArrayList<Address> places) {
        super(context, 0, places);
        places_list = places;
    }

    public View getView(int position, View convertView, ViewGroup parent){

        Address addr = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row, parent, false);
        }

        TextView place_name = (TextView) convertView.findViewById(R.id.name);
        TextView place_lati = (TextView) convertView.findViewById(R.id.lati);
        TextView place_longi = (TextView) convertView.findViewById(R.id.longi);

        place_name.setText(addr.getAddressLine(0));
        place_lati.setText(String.valueOf(addr.getLatitude()));
        place_longi.setText(String.valueOf(addr.getLongitude()));

        return convertView;
    }

    @Override
    public Address getItem(int pos){
        return places_list.get(pos);
    }
}
