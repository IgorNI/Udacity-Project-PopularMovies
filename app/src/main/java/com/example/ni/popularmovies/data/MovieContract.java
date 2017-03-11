package com.example.ni.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ni on 2017/2/20.
 *
 * Defines table and column names for the movie database.
 */

public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.example.ni.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIE = "movie";

      /* Inner class that defines the table contents of the location table */
    public static final class MovieEntry implements BaseColumns {
          /* movie表的基本位置URI */
          public static final Uri CONTENT_URI =  BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();
          /* movie表某一目录的uri */
          public static final String CONTENT_TYPE =
                  ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
          /* movie表某一item的uri，类似于某一单独电影的信息 */
          public static final String CONTENT_ITEM_TYPE =
                  ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

          /* 表名为movie */
          public static final String TABLE_NAME = "movie";
          /* id字段 */
          public static final String COLUMN_MOVIE_ID = "movie_id";
          /* name字段 */
          public static final String COLUMN_MOVIE_NAME = "movie_name";
          /* date字段 */
          public static final String COLUMN_MOVIE_DATE = "movie_date";
          /* 评分字段 */
          public static final String COLUMN_MOVIE_VOTE = "movie_vote";
          /* 受欢迎程度的字段 */
          public static final String COLUMN_MOVIE_POPULARITY = "movie_popularity";
          /* 是否收藏的字段 */
          public static final String COLUMN_MOVIE_MARKED = "movie_marked";
          /* 简介的字段 */
          public static final String COLUMN_MOVIE_OVERVIEW = "movie_overview";
          /* 海报地址的字段 */
          public static final String COLUMN_MOVIE_POSTER_PATH = "movie_poster_path";
          /* 电影时长的字段 */
          public static final String COLUMN_MOVIE_RUN_TIME = "movie_runtime";
          /* 电影预告片的json字段 */
          public static final String COLUMN_MOVIE_TRAILERS = "movie_trailers";
          /* 电影评论的json字段 */
          public static final String COLUMN_MOVIE_REVIEWS = "movie_reviews";

          /* 根据id获取电影 */
          public static Uri buildMovieDetailUri(long id) {
              return ContentUris.withAppendedId(CONTENT_URI,id);
          }

          /* 获取收藏的电影 */
          public static Uri buildMovieMarkedUri(int marked) {
              return ContentUris.withAppendedId(CONTENT_URI,marked);
          }

          public static int getMovieIdFromUri(Uri uri) {
              return Integer.parseInt(uri.getPathSegments().get(1));
          }

          public static int getMovieMarkedFromUri(Uri uri) {
              return Integer.parseInt(uri.getPathSegments().get(1));
          }
      }


}
