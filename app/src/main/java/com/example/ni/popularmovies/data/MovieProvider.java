package com.example.ni.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by ni on 2017/2/20.
 */

// FIXME: 2017/2/22  NI 尚未通过验证
public class MovieProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    static final int MOVIE = 100;
    static final int MOVIE_WITH_ID = 101; // 表的_id
    static final int MOVIE_WITH_MARKED = 102; // 电影收藏
    static final int MOVIE_WITH_RATE = 103; // 电影评分
    static final int MOVIE_WITH_POPULARITY = 104; // 电影欢迎程度
    static final int MOVIE_WITH_MOVIEID = 105; // 电影id
    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder ;
    private MovieDbHelper mOpenHelper;
    static{
        sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
        sWeatherByLocationSettingQueryBuilder.setTables(MovieContract.MovieEntry.TABLE_NAME);
    }

    // movie.movie_marked = ?
    private static final String sMovieMarkedSelection =
            MovieContract.MovieEntry.TABLE_NAME +
                    "." + MovieContract.MovieEntry.COLUMN_MOVIE_MARKED + " = ?";

    // movie.movie_id = ?
    private static final String sMovieIdSelection =
            MovieContract.MovieEntry.TABLE_NAME +
                    "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?";

    // 构造根据是否收藏的sql查询语句
    private Cursor getMovieByMarked(Uri uri, String[] projection, String sortOrder) {
        int movieMarked = MovieContract.MovieEntry.getMovieMarkedFromUri(uri);
        String[] selectionArgs;
        String selection;
        selection = sMovieMarkedSelection;
        selectionArgs = new String[] {String.valueOf(movieMarked)};
        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    // 构造根据movieId的sql查询语句
    private Cursor getMovieByMovieId(Uri uri, String[] projection, String sortOrder) {
        String movieId = MovieContract.MovieEntry.getMovieIdFromUri(uri) + "";
        String selection = sMovieIdSelection;
        String[] selectionArgs = new String[] {movieId};
        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;
        uriMatcher.addURI(authority, MovieContract.PATH_MOVIE,MOVIE);
        uriMatcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", MOVIE_WITH_ID); // 根据_Id查询的uri
        uriMatcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", MOVIE_WITH_MARKED); // 根据是否收藏进行查询的uri
        uriMatcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", MOVIE_WITH_RATE); // 根据评分高低查询
        uriMatcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", MOVIE_WITH_POPULARITY); // 根据受欢迎程度查询
        uriMatcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", MOVIE_WITH_MOVIEID); // 根据电影id查询
        return uriMatcher;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // 这些是用过contentProvider获得的参数
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = null;
        switch (sUriMatcher.match(uri)) {
            case MOVIE:
                cursor = db.query(MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case MOVIE_WITH_ID:
                String id = uri.getPathSegments().get(1);
                String mSelection = "_id=?";
                String[] mSelectionArgs = new String[] {id};
                cursor = db.query(MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case MOVIE_WITH_MOVIEID:
                break;
            case MOVIE_WITH_MARKED:
                cursor = getMovieByMarked(uri,projection,sortOrder);
                break;
            case MOVIE_WITH_RATE:
                cursor = getMovieByMarked(uri,projection,sortOrder);
                break;
            case MOVIE_WITH_POPULARITY:
                cursor = getMovieByMarked(uri,projection,sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_WITH_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIE_WITH_MARKED:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }
    
    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case MOVIE:
                long id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI,id);
                }else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri,null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int delete;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE_WITH_MOVIEID:
                String id = uri.getPathSegments().get(1);
                delete = db.delete(MovieContract.MovieEntry.TABLE_NAME, MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?",new String[] {id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (delete != 0) {
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return delete;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int id = 0;
        switch (match) {
            case MOVIE:
                id = db.update(MovieContract.MovieEntry.TABLE_NAME,values,selection,selectionArgs);
                break;
            case MOVIE_WITH_MOVIEID:
                String movieId = uri.getPathSegments().get(1);
                id = db.update(MovieContract.MovieEntry.TABLE_NAME,values,MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?",new String[] {movieId});
                break;
        }
        if (id != 0) {
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return id;
    }

    /**
     * 批量写入数据，效率高
     * */
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int numValues = 0;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        try {
            //数据库操作
            db.beginTransaction(); //开始事务
            numValues = values.length;
            for (int i = 0; i < numValues; i++) {
                if (db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values[i]) < 0) {
                    return 0;
                }
            }
            db.setTransactionSuccessful(); //别忘了这句 Commit
        } finally {
            db.endTransaction(); //结束事务
        }
        Log.i("insert", "bulkInsert: " + numValues);
        return numValues;
    }

    @Override
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
