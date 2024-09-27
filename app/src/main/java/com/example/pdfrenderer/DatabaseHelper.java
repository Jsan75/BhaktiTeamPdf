package com.example.pdfrenderer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "WordLocations.db";
    public static final int DATABASE_VERSION = 1;

    // Define table and column names
    public static final String TABLE_NAME = "WordLocations";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_WORD = "word";
    public static final String COLUMN_PAGE_NUMBER = "page_number";
    public static final String COLUMN_X = "x";
    public static final String COLUMN_Y = "y";
    public static final String COLUMN_WIDTH = "width";
    public static final String COLUMN_HEIGHT = "height";
    public static final String COLUMN_WORD_COUNT = "word_count";


    // Define the CREATE TABLE SQL statement
    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_WORD + " TEXT, " +
                    COLUMN_WORD_COUNT + " INTEGER, " +
                    COLUMN_PAGE_NUMBER + " INTEGER, " +
                    COLUMN_X + " FLOAT, " +
                    COLUMN_Y + " FLOAT, " +
                    COLUMN_WIDTH + " FLOAT, " +
                    COLUMN_HEIGHT + " FLOAT);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
