package com.example.ni.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ni on 2017/2/20.
 */

public class MovieDbHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 11;
    private static final String DATABASE_NAME = "movie.db";
    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /* 创建表 */
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME + " ("
                + MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY,"
                + MovieContract.MovieEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL,"
                + MovieContract.MovieEntry.COLUMN_MOVIE_NAME + " TEXT NOT NULL,"
                + MovieContract.MovieEntry.COLUMN_MOVIE_VOTE + " REAL NOT NULL,"
                + MovieContract.MovieEntry.COLUMN_MOVIE_POPULARITY + " REAL NOT NULL,"
                + MovieContract.MovieEntry.COLUMN_MOVIE_MARKED + " INTEGER NOT NULL,"
                + MovieContract.MovieEntry.COLUMN_MOVIE_DATE + " TEXT NOT NULL,"
                + MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW + " TEXT NOT NULL,"
                + MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_PATH + " TEXT NOT NULL,"
                + MovieContract.MovieEntry.COLUMN_MOVIE_RUN_TIME + " INTEGER,"
                + MovieContract.MovieEntry.COLUMN_MOVIE_TRAILERS + " TEXT,"
                + MovieContract.MovieEntry.COLUMN_MOVIE_REVIEWS + " TEXT"
                + " );";
        db.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    /**
     * 更新数据库
     * @param db 数据库
     * @param newVersion 新版本
     * @param oldVersion 旧版本
     * */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
