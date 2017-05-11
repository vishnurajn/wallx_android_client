package com.yingyang.wallx.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.yingyang.wallx.models.ItemCategory;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandlerCategory extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "mwp_CategoryDB";

    private static final String TABLE_NAME = "Category";
    private static final String KEY_ID = "id";
    private static final String KEY_CAT_ID = "catid";
    private static final String KEY_CAT_NAME = "catname";
    private static final String KEY_CAT_IMAGE = "catimage";


    public DatabaseHandlerCategory(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_CAT_ID + " TEXT,"
                + KEY_CAT_NAME + " TEXT,"
                + KEY_CAT_IMAGE + " TEXT,"
                + "UNIQUE(" + KEY_CAT_ID + ")"
                + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);

    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    //Adding Record in Database

    public void AddtoFavoriteCate(ItemCategory pj) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_CAT_ID, pj.getCategoryId());
        values.put(KEY_CAT_NAME, pj.getCategoryName());
        values.put(KEY_CAT_IMAGE, pj.getCategoryImage());

        // Inserting Row
        db.insert(TABLE_NAME, null, values);
        db.close(); // Closing database connection

    }

    // Getting All Data
    public List<ItemCategory> getAllData() {
        List<ItemCategory> dataList = new ArrayList<ItemCategory>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ItemCategory contact = new ItemCategory();
                //contact.setId(Integer.parseInt(cursor.getString(0)));
                contact.setCategoryId(cursor.getString(1));
                contact.setCategoryName(cursor.getString(2));
                contact.setCategoryImage(cursor.getString(3));

                // Adding contact to list
                dataList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return dataList;
    }

    //getting single row

    public List<ItemCategory> getFavRow(String id) {
        List<ItemCategory> dataList = new ArrayList<ItemCategory>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE catid=" + id;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ItemCategory contact = new ItemCategory();
                //contact.setId(Integer.parseInt(cursor.getString(0)));
                contact.setCategoryId(cursor.getString(1));
                contact.setCategoryName(cursor.getString(2));
                contact.setCategoryImage(cursor.getString(3));

                // Adding contact to list
                dataList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return dataList;
    }

    //for remove favorite

    public void RemoveFav(ItemCategory contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_CAT_ID + " = ?",
                new String[]{String.valueOf(contact.getCategoryId())});
        db.close();
    }

    public enum DatabaseManager {
        INSTANCE;
        private SQLiteDatabase db;
        private boolean isDbClosed = true;
        DatabaseHandlerCategory dbHelper;

        public void init(Context context) {
            dbHelper = new DatabaseHandlerCategory(context);
            if (isDbClosed) {
                isDbClosed = false;
                this.db = dbHelper.getWritableDatabase();
            }

        }


        public boolean isDatabaseClosed() {
            return isDbClosed;
        }

        public void closeDatabase() {
            if (!isDbClosed && db != null) {
                isDbClosed = true;
                db.close();
                dbHelper.close();
            }
        }
    }
}
