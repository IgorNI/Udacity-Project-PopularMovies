package com.example.ni.popularmovies.bean;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ni on 2017/3/1.
 */

public class PopularMovie implements Parcelable{
    /* 电影名称 */
    private int movieId;
    /* 电影名称 */
    private String movieName;
    /* 电影评分 */
    private double movieVote;
    /* 电影欢迎程度 */
    private double moviePopularity;
    /* 电影是否被搜藏，1为true，0为false */
    private int movieMarked;
    /* 电影评价 */
    private String movieOverview;
    /* 电影上映日期 */
    private String movieDate;
    /* 电影海报地址 */
    private String moviePostPath;
    /* 电影时长 */
    private int movieRunTime;
    private List<String> keyList = new ArrayList<>();

    /* 预告片json */
    public String videosJson;
    /* 评论json */
    public String reviewsJson;


    public static final int MARKED = 1;
    public static final int UNMARKED = 0;

    public PopularMovie() {
        super();
    }


    protected PopularMovie(Parcel in) {
        movieId = in.readInt();
        movieName = in.readString();
        movieVote = in.readDouble();
        moviePopularity = in.readDouble();
        movieMarked = in.readInt();
        movieOverview = in.readString();
        movieDate = in.readString();
        moviePostPath = in.readString();
        movieRunTime = in.readInt();
        keyList = in.createStringArrayList();
        videosJson = in.readString();
        reviewsJson = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(movieId);
        dest.writeString(movieName);
        dest.writeDouble(movieVote);
        dest.writeDouble(moviePopularity);
        dest.writeInt(movieMarked);
        dest.writeString(movieOverview);
        dest.writeString(movieDate);
        dest.writeString(moviePostPath);
        dest.writeInt(movieRunTime);
        dest.writeStringList(keyList);
        dest.writeString(videosJson);
        dest.writeString(reviewsJson);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PopularMovie> CREATOR = new Creator<PopularMovie>() {
        @Override
        public PopularMovie createFromParcel(Parcel in) {
            return new PopularMovie(in);
        }

        @Override
        public PopularMovie[] newArray(int size) {
            return new PopularMovie[size];
        }
    };

    public void setMoviePostPath(String moviePostPath) {
        this.moviePostPath = moviePostPath;
    }

    public void setMovieDate(String movieDate) {
        this.movieDate = movieDate;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public void setMovieMarked(int movieMarked) {
        this.movieMarked = movieMarked;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public void setMovieOverview(String movieOverview) {
        this.movieOverview = movieOverview;
    }

    public void setMoviePopularity(double moviePopularity) { this.moviePopularity = moviePopularity;}

    public void setMovieVote(double movieVote) {
        this.movieVote = movieVote;
    }

    public void setMovieRunTime(int movieRunTime) { this.movieRunTime = movieRunTime;}

    public void setKeyList(List<String> keyList) {
        this.keyList = keyList;
    }

    public List<String> getKeyList() {
        return keyList;
    }

    public String getVideosJson() {
        return videosJson;
    }

    public String getReviewsJson() {
        return reviewsJson;
    }

    public void setVideosJson(String videosJson) {
        this.videosJson = videosJson;
    }

    public void setReviewsJson(String reviewsJson) {
        this.reviewsJson = reviewsJson;
    }

    public double getMoviePopularity() {
        return moviePopularity;
    }

    public double getMovieVote() {
        return movieVote;
    }

    public int getMovieMarked() {
        return movieMarked;
    }

    public int getMovieId() {
        return movieId;
    }

    public String getMovieDate() {
        return movieDate;
    }

    public String getMovieName() {
        return movieName;
    }

    public String getMovieOverview() {
        return movieOverview;
    }

    public String getMoviePostPath() {
        return moviePostPath;
    }

    public int getMovieRunTime() {
        return movieRunTime;
    }

}
