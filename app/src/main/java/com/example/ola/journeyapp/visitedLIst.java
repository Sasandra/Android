package com.example.ola.journeyapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class visitedLIst extends Activity {
    private SQLiteDatabase db;
    private Cursor cursor;
    private visitedCursorAdapter visitedCursorAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.visited_list);

        ListView tovisit = (ListView) findViewById(R.id.visited_list);


        try {
            SQLiteOpenHelper databaseHelper = new DatabaseHelper(this);
            db = databaseHelper.getReadableDatabase();
            cursor = db.query("visited", new String[]{"_id", "Name", "Latitude", "Longitude", "Note", "Photo"}, null, null, null, null, null);

            if(cursor.getCount() == 0){
                Toast.makeText(visitedLIst.this, "List is empty :(", Toast.LENGTH_LONG).show();
            }

            visitedCursorAdapter = new visitedCursorAdapter(this, cursor, 0);

            tovisit.setAdapter(visitedCursorAdapter);

        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(this, "Baza danych jest niedostępna.", Toast.LENGTH_SHORT);
            toast.show();
        }


        tovisit.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            public boolean onItemLongClick(final AdapterView<?> p, View v, final int po, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(visitedLIst.this);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure you want to delete?");
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int ii) {
                        SQLiteOpenHelper databaseHelper = new DatabaseHelper(visitedLIst.this);
                        db = databaseHelper.getWritableDatabase();
                        Cursor cursor = (Cursor) p.getItemAtPosition(po);

                        long ID = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                        db.delete("visited", "_id"+ "=" + ID, null);
                        Cursor cursor2 = db.query("visited", new String[]{"_id", "Name", "Latitude", "Longitude", "Note", "Photo"}, null, null, null, null, null);
                        visitedCursorAdapter.changeCursor(cursor2);

                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int ii) {
                                dialog.dismiss();
                            }
                        }
                );
                builder.show();
                return true;
            }
        });


        tovisit.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(final AdapterView<?> adapter, View v, final int position,
                                    long id){
                Intent i = new Intent(visitedLIst.this, visitedDetail.class);
                i.putExtra("ID", (int)id);
                startActivity(i);
            }
        });
    }


    public void onRestart(){
        super.onRestart();

        ListView tovisit = (ListView) findViewById(R.id.visited_list);

        try {
            SQLiteOpenHelper databaseHelper = new DatabaseHelper(this);
            db = databaseHelper.getReadableDatabase();
            cursor = db.query("visited", new String[]{"_id", "Name", "Latitude", "Longitude", "Note", "Photo"}, null, null, null, null, null);

            visitedCursorAdapter = new visitedCursorAdapter(this, cursor, 0);

            tovisit.setAdapter(visitedCursorAdapter);

        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(this, "Baza danych jest niedostępna.", Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    public void onDestroy(){
        super.onDestroy();
        db.close();
        cursor.close();
    }

}