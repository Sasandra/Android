package com.example.ola.journeyapp;


import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;


public class DatabaseHelper  extends SQLiteOpenHelper{

    private static final String DB_NAME = "locations";
    private static final int DB_VERSION = 1;

    DatabaseHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create table toVisit (_id Integer Primary Key Autoincrement Unique,"+
                    "Name text," +
                    "Latitude double," +
                    "Longitude double);");

        db.execSQL("Create table visited (_id Integer Primary Key Autoincrement,"+
                "Name text," +
                "Latitude double," +
                "Longitude double," +
                "Note text," +
                "Photo BLOB," +
                "Date DATETIME);");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public static void insertToVisit(SQLiteDatabase db, String name, double lati, double longi){
        ContentValues toVisit = new ContentValues();
        toVisit.put("Name", name);
        toVisit.put("Latitude", lati);
        toVisit.put("Longitude", longi);
        db.insert("toVisit", null, toVisit);
    }

    public static void insertVisited(SQLiteDatabase db, String name, double lati, double longi, String note, int id, int p_id){
        ContentValues visited = new ContentValues();
        visited.put("Name", name);
        visited.put("Latitude", lati);
        visited.put("Longitude", longi);
        visited.put("Note", note);
        visited.put("PhotoId", p_id);
        db.insert("visited", null, visited);
    }

    public static void deleteToVisit(SQLiteDatabase db, int id){
        db.delete("toVisit", "_id = ?", new String[] {Integer.toString(id)});
    }


}
