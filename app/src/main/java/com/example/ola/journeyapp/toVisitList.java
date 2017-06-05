package com.example.ola.journeyapp;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class toVisitList extends Activity {

    private SQLiteDatabase db;
    private Cursor cursor;
    private myCursorAdapter myCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.to_visit_list);

        ListView tovisit = (ListView)findViewById(R.id.tovisit);


        try{
            SQLiteOpenHelper databaseHelper = new DatabaseHelper(this);
            db = databaseHelper.getReadableDatabase();
            cursor = db.query("toVisit", new String[] {"_id", "Name","Latitude", "Longitude"}, null, null, null, null, null);

            myCursorAdapter = new myCursorAdapter(this, cursor, 0);

            tovisit.setAdapter(myCursorAdapter);

        }catch(SQLiteException e){
            Toast toast = Toast.makeText(this, "Baza danych jest niedostępna.", Toast.LENGTH_SHORT);
            toast.show();
        }


        tovisit.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            public boolean onItemLongClick(final AdapterView<?> p, View v, final int po, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(toVisitList.this);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure you want to delete?");
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int ii) {
                        SQLiteOpenHelper databaseHelper = new DatabaseHelper(toVisitList.this);
                        db = databaseHelper.getWritableDatabase();
                        Cursor cursor = (Cursor) p.getItemAtPosition(po);

                        long ID = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                        db.delete("toVisit", "_id"+ "=" + ID, null);
                        Cursor cursor2 = db.query("toVisit", new String[] {"_id", "Name","Latitude", "Longitude"}, null, null, null, null, null);
                        myCursorAdapter.changeCursor(cursor2);

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

    }





    public void goToAddVisit(View view){
        Intent intent = new Intent(this, addToVisit.class);
        startActivity(intent);

    }

    public void onRestart(){
        super.onRestart();

        try{
            SQLiteOpenHelper databaseHelper = new DatabaseHelper(this);
            db = databaseHelper.getReadableDatabase();
            Cursor new_cursor = db.query("toVisit", new String[] {"_id", "Name","Latitude", "Longitude"}, null, null, null, null, null);

            ListView tovisit = (ListView)findViewById(R.id.tovisit);
            CursorAdapter cursorAdapter = (CursorAdapter)tovisit.getAdapter();

            cursorAdapter.changeCursor(new_cursor);

        }catch(SQLiteException e){
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
