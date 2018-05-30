package com.example.otgsensor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.View;
import android.widget.Toast;

/**
 * Created by 123 on 2018/3/19.
 */

public class MyDatabaseHelper extends SQLiteOpenHelper {

    public static final String CREATE_DATA = "create table Data("+"id integer primary key autoincrement, "
    +"date INTEGER,"
    +"temp FLOAT,"
    +"humidity FLOAT,"
    +"pressure FLOAT,"
    +"illumination FLOAT,"
    +"soil_t FLOAT,"+"soil_h FLOAT,"+"uv FLOAT,"
    +"longitude FLOAT,"
    +"latitude FLOAT)";

    private Context mContext;


    public MyDatabaseHelper(Context context, String name,
                            SQLiteDatabase.CursorFactory factory, int version){
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_DATA);
        Toast.makeText(mContext, "Create succeeded", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }
}
