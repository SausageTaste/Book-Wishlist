package com.sausagetaste.book_wishlist;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Vector;


public class DBManager {

    //// Definitions

    private static final String DB_NAME = "book_info.db";
    private static final int DB_VERSION = 2;

    public static class BookRecord {
        public String title;
        public String cover_url;

        public String note;
    }

    private class DBHelper extends SQLiteOpenHelper {

        DBHelper(final Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(
                "CREATE TABLE books (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title TEXT, " +
                    "cover_url TEXT, " +
                    "note TEXT" +
                ");"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int old_version, int new_version) {
            db.execSQL("DROP TABLE IF EXISTS books");
        }

        public void insert(final BookRecord info) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("INSERT INTO books VALUES (" + "null, \"" + info.title + "\", \"" + info.cover_url + "\", \"" + info.note + "\")");
        }

        public BookRecord search(final String title) {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT title, cover_url, note FROM books WHERE title=\"" + title + "\";", null);
            BookRecord result = null;

            while (cursor.moveToNext()) {
                if (null == result) {
                    result = new BookRecord();
                }

                result.title = cursor.getString(0);
                result.cover_url = cursor.getString(1);
                result.note = cursor.getString(2);
            }

            return result;
        }

        public void select_all_in_column(final String column_name, final Vector<String> result) {
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cursor = db.rawQuery("SELECT \"" + column_name + "\" FROM books", null);
            while (cursor.moveToNext()) {
                result.add(cursor.getString(0));
            }
        }

    }


    //// Attributes

    private final DBHelper helper;

    //// Methods

    DBManager(final Context context) {
        this.helper = new DBHelper(context);
    }

    public boolean override_record(final BookRecord data) {
        helper.insert(data);
        return true;
    }

    public BookRecord get_record(final String title) {
        return helper.search(title);
    }

    public void get_all_titles(final Vector<String> result) {
        helper.select_all_in_column("title", result);
    }

}
