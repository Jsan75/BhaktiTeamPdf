package com.example.pdfrenderer;

import static com.example.pdfrenderer.DatabaseHelper.COLUMN_HEIGHT;
import static com.example.pdfrenderer.DatabaseHelper.COLUMN_PAGE_NUMBER;
import static com.example.pdfrenderer.DatabaseHelper.COLUMN_WIDTH;
import static com.example.pdfrenderer.DatabaseHelper.COLUMN_WORD;
import static com.example.pdfrenderer.DatabaseHelper.COLUMN_WORD_COUNT;
import static com.example.pdfrenderer.DatabaseHelper.COLUMN_X;
import static com.example.pdfrenderer.DatabaseHelper.COLUMN_Y;
import static com.example.pdfrenderer.DatabaseHelper.TABLE_NAME;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.List;
import java.util.ArrayList;

public class WordSearch {

    public static void insertWordsLocation(Context context, List<WordLocation> wordLocations) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            db.beginTransaction();
            for (WordLocation wordLocation : wordLocations) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_WORD, wordLocation.getWord());
                values.put(COLUMN_WORD_COUNT, wordLocation.getWordCount());
                values.put(COLUMN_PAGE_NUMBER, wordLocation.getPageNumber());
                values.put(COLUMN_X, wordLocation.getX());
                values.put(COLUMN_Y, wordLocation.getY());
                values.put(COLUMN_WIDTH, wordLocation.getWidth());
                values.put(COLUMN_HEIGHT, wordLocation.getHeight());
                db.insert(TABLE_NAME, null, values);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("WordSearch", "Error inserting words", e);
        } finally {

            db.endTransaction();
            db.close();
        }
    }

    public static List<WordLocation> searchWords(Context context, String searchTerm, int wordCount) {
        List<WordLocation> wordLocations = new ArrayList<>();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {COLUMN_WORD, COLUMN_WORD_COUNT, COLUMN_PAGE_NUMBER, COLUMN_X, COLUMN_Y, COLUMN_WIDTH, COLUMN_HEIGHT};
        String selection;
        String[] selectionArgs;

        if (wordCount == 1) {
            selection = COLUMN_WORD_COUNT + " = ? AND " + COLUMN_WORD + " LIKE ?";
            selectionArgs = new String[]{String.valueOf(wordCount), "%" + searchTerm + "%"};
        } else if (wordCount == 2) {
            selection = COLUMN_WORD_COUNT + " = ? AND " + COLUMN_WORD + " LIKE ?";
            selectionArgs = new String[]{String.valueOf(wordCount), "%" + searchTerm + "%"};
        } else if (wordCount == 3) {
            selection = COLUMN_WORD_COUNT + " = ? AND " + COLUMN_WORD + " LIKE ?";
            selectionArgs = new String[]{String.valueOf(wordCount), "%" + searchTerm + "%"};
        } else if (wordCount > 3) {
            selection = COLUMN_WORD_COUNT + " > ? AND " + COLUMN_WORD + " LIKE ?";
            selectionArgs = new String[]{String.valueOf(wordCount), "%" + searchTerm + "%"};
        } else {
            // Handle invalid wordCount here (e.g., show all items)
            selection = COLUMN_WORD + " LIKE ?";
            selectionArgs = new String[]{"%" + searchTerm + "%"};
        }

        String orderBy = COLUMN_PAGE_NUMBER + " ASC";

        Cursor cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, orderBy);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String word = cursor.getString(cursor.getColumnIndex(COLUMN_WORD));
                @SuppressLint("Range") int pageNumber = cursor.getInt(cursor.getColumnIndex(COLUMN_PAGE_NUMBER));
                @SuppressLint("Range") float x = cursor.getFloat(cursor.getColumnIndex(COLUMN_X));
                @SuppressLint("Range") float y = cursor.getFloat(cursor.getColumnIndex(COLUMN_Y));
                @SuppressLint("Range") float width = cursor.getFloat(cursor.getColumnIndex(COLUMN_WIDTH));
                @SuppressLint("Range") float height = cursor.getFloat(cursor.getColumnIndex(COLUMN_HEIGHT));

                WordLocation result = new WordLocation(word, wordCount, pageNumber, x, y, width, height);
                wordLocations.add(result);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return wordLocations;
    }
}

