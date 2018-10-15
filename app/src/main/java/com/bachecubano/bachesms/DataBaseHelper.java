package com.bachecubano.bachesms;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Calendar;

class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bache_sms.db";
    private static final String SENT_SMS_TABLE = "sent_sms";
    private static final int DATABASE_VERSION = 1;

    private static final String COL_ID = "id";
    public static final String COL_TO_PHONE = "to_phone";
    public static final String COL_SMS_TEXT = "sms_text";
    public static final String COL_DATE_SENT = "sent_time";

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + SENT_SMS_TABLE + " (" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_TO_PHONE + " TEXT, " + COL_SMS_TEXT + " TEXT, " + COL_DATE_SENT + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SENT_SMS_TABLE);
        db.execSQL("CREATE TABLE " + SENT_SMS_TABLE + "(" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_TO_PHONE + " TEXT, " + COL_SMS_TEXT + " TEXT, " + COL_DATE_SENT + " TEXT)");
    }

    //Insert data in DataBase from sent SMS Data
    void insertData(String to, String sms_text) {

        String my_date = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_TO_PHONE, to);
        contentValues.put(COL_SMS_TEXT, sms_text);
        contentValues.put(COL_DATE_SENT, my_date);

        db.insert(SENT_SMS_TABLE, null, contentValues);
        db.close();
    }

    //Get all Data
    Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + SENT_SMS_TABLE + " ORDER BY " + COL_ID + " ASC", null);
    }

    //Delete specific row
    /*
    int deleteData(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(SENT_SMS_TABLE, COL_ID + "=?", new String[]{id});
    }
    */
}
