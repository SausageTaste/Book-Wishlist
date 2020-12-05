package com.sausagetaste.book_wishlist;

import android.content.ContentValues;
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
        public Integer id = null;

        public String title = null;
        public String cover_url = null;

        public String note = null;

        public String url_origin = null;
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

                    "note TEXT," +

                    "url_origin TEXT" +
                ");"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int old_version, int new_version) {
            db.execSQL("DROP TABLE IF EXISTS books");
        }


        public void insert(final BookRecord info) {
            assert null == info.id;
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("INSERT INTO books VALUES (" +
                "null, \"" +
                info.title + "\", \"" +
                info.cover_url + "\", \"" +
                info.note + "\", \"" +
                info.url_origin +
            "\")");
        }

        public void update_row(final BookRecord info) {
            assert null != info.id;
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues cv = new ContentValues();

            if (null != info.title) {
                cv.put("title", info.title);
            }
            if (null != info.cover_url) {
                cv.put("cover_url", info.cover_url);
            }
            if (null != info.note) {
                cv.put("note", info.note);
            }
            if (null != info.url_origin) {
                cv.put("url_origin", info.url_origin);
            }

            db.update("books", cv, "_id = ?", new String[]{Integer.toString(info.id)});
        }

        public BookRecord search_with_title(final String title) {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT _id, title, cover_url, note, url_origin FROM books WHERE title=\"" + title + "\";", null);
            BookRecord result = null;

            while (cursor.moveToNext()) {
                if (null == result) {
                    result = new BookRecord();
                }

                result.id = cursor.getInt(0);
                result.title = cursor.getString(1);
                result.cover_url = cursor.getString(2);
                result.note = cursor.getString(3);
                result.url_origin = cursor.getString(4);
            }

            return result;
        }

        public BookRecord search_with_id(final int id) {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT _id, title, cover_url, note, url_origin FROM books WHERE _id=\"" + id + "\";", null);
            BookRecord result = null;

            while (cursor.moveToNext()) {
                if (null == result) {
                    result = new BookRecord();
                }

                result.id = cursor.getInt(0);
                result.title = cursor.getString(1);
                result.cover_url = cursor.getString(2);
                result.note = cursor.getString(3);
                result.url_origin = cursor.getString(4);
            }

            return result;
        }

        public void get_all_id_title_pairs(final Vector<Integer> id_list, final Vector<String> title_list) {
            SQLiteDatabase db = this.getReadableDatabase();

            Cursor cursor = db.rawQuery("SELECT _id, title FROM books", null);
            while (cursor.moveToNext()) {
                id_list.add(cursor.getInt(0));
                title_list.add(cursor.getString(1));
            }
        }

        public boolean delete_by_id(final int id) {
            SQLiteDatabase db = this.getWritableDatabase();
            return db.delete("books", "_id = ?", new String[]{Integer.toString(id)}) > 0;
        }

    }


    //// Attributes

    private final DBHelper helper;

    //// Methods

    DBManager(final Context context) {
        this.helper = new DBHelper(context);
    }

    public boolean override_record(final BookRecord data) {
        if (null == data.id) {
            helper.insert(data);
        }
        else {
            helper.update_row(data);
        }

        return true;
    }

    public BookRecord get_record_with_title(final String title) {
        return helper.search_with_title(title);
    }

    public BookRecord get_record_with_id(final int id) {
        return helper.search_with_id(id);
    }

    public void get_all_titles(final Vector<Integer> id_list, final Vector<String> title_list) {
        helper.get_all_id_title_pairs(id_list, title_list);
    }

    public void delete_by_id(final int id) {
        this.helper.delete_by_id(id);
    }

}
