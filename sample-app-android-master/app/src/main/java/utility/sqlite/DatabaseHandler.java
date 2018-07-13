package utility.sqlite;

/**
 * Created by diyaayaad on 4/2/16.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "ZOOM_DB";

    //TODO isRegistered, addOrEditSDKUser

    // SDK User Table
    private static final String TABLE_NAME = "SDK_USER";

    //Columns for SDK Users Table
    private static final String USER_ID = "USER_ID";
    private static final String WEB_DOMAIN = "WEB_DOMAIN";
    private static final String ZOOM_TOKEN = "ZOOM_TOKEN";
    private static final String APP_SECRET= "APP_SECRET";
    private static final String APP_KEY = "APP_KEY";
    //these columns for Rest call uses
    private static final String API_KEY = "API_KEY";
    private static final String API_SECRET = "API_SECRET";
    private static final String USER_EMAIL = "USER_EMAIL";



    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE = "CREATE TABLE " + TABLE_NAME + "("
                + USER_ID + " TEXT PRIMARY KEY," + WEB_DOMAIN+ " TEXT,"
                + ZOOM_TOKEN + " TEXT,"+ APP_SECRET+ " TEXT,"+ APP_KEY +" TEXT,"+USER_EMAIL+" TEXT,"+
                API_KEY+" TEXT,"+API_SECRET+" TEXT)";

        db.execSQL(CREATE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    public boolean isSDKUserExist(){
        User user = getUser();
        return user != null;
    }

    public User getUser(){
        String selectQuery = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        User us=null;


        if (cursor.moveToFirst()) {
            do {
                us = new User();
                us.setUSER_ID(cursor.getString(0));
                us.setWEB_DOMAIN(cursor.getString(1));
                us.setZOOM_TOKEN(cursor.getString(2));
                us.setAPP_SECRET(cursor.getString(3));
                us.setAPP_KEY(cursor.getString(4));
                us.setUSER_EMAIL(cursor.getString(5));
                us.setAPI_KEY(cursor.getString(6));
                us.setAPI_SECRET(cursor.getString(7));
            } while (cursor.moveToNext());
        }

        return us;
    }


    public void addOrEditUser(String userID , String webDomain, String zoomToken, String appSecret, String appKey,
                              String user_email,String apiKey,String apiSecret){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        if(isSDKUserExist()){
            db.delete(TABLE_NAME, "USER_ID=?", new String[]{getUser().getUSER_ID()});
        }

        values.put(USER_ID, userID);
        values.put(WEB_DOMAIN,webDomain);
        values.put(ZOOM_TOKEN, zoomToken);
        values.put(APP_SECRET, appSecret);
        values.put(APP_KEY, appKey);
        values.put(USER_EMAIL,user_email);
        values.put(API_KEY,apiKey);
        values.put(API_SECRET,apiSecret);
        // Inserting Row
        db.insert(TABLE_NAME, null, values);
        db.close();


    }


}
