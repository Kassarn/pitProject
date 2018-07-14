package com.example.ivan.pitproject;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Ivan on 9.4.2018 г..
 */

public class DataBaseHelper  extends SQLiteOpenHelper {
    private final  static  String DATABASE_NAME  =  "Holes.db";
    private final  static  String TABLE_NAME  =  "holes_table";
    public final  static  String COL_1 =  "ID";
    private final  static  String COL_2 =  "SIZE";
    private final  static  String COL_3 =  "DEPTH";
    private final  static  String COL_4 =  "LATTITUDE";
    private final  static  String COL_5 =  "LONGITUDE";
    DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME +" (ID INTEGER PRIMARY KEY AUTOINCREMENT,SIZE TEXT,DEPTH TEXT,LATTITUDE TEXT,LONGITUDE TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
    }
    public boolean insertData(String size,String depth,String lattitude,String longitude) {
        SQLiteDatabase db =this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2,size);
        contentValues.put(COL_3,depth);
        contentValues.put(COL_4,lattitude);
        contentValues.put(COL_5,longitude);
       long result = db.insert(TABLE_NAME,null,contentValues);
       if(result==-1) {
           return  false;
       }
    return  true;
    }

    public Cursor  getAllData() {
        SQLiteDatabase db =this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME,null);
        return  res;
    }

    public Integer  deleteData(String id) {

        SQLiteDatabase db =this.getWritableDatabase();
      return  db.delete(TABLE_NAME,"ID = ?",new String[] {id});
    }
}
