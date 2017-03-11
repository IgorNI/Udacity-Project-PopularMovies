package common;

/**
 * Created by ni on 16-8-21.
 */
public class ComUri {
    /**获取电影信息的基础url*/
    public static final String BASE_MOVIE_URL = "http://api.themoviedb.org/3/movie/";
    public static final String COMMON_STR = "?api_key=";
    /**base url of the image*/
    public static final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/w342/";
    public static final String TITLE = "original_title"; // 电影名称
    public static final String POSTER_PATH = "poster_path";// 电影海报
    public static final String OVERVIEW = "overview"; // 剧情简介
    public static final String VOTE = "vote_average"; // 评分
    public static final String DATE = "release_date"; // 上映日期
    public static final String VIDEOS = "/videos"; // 预告片
    public static final String REVIEWS = "/reviews"; // 评论
    public static final String RUNTIME = "runtime"; // 电影时长
    /**json 字段*/
    public static final String JSON_RESULTS = "results"; // results 字段
    public static final String JSON_POSTER_PATH = "poster_path";
    public static final String JSON_ORIGINAL_TITLE = "original_title";
    public static final String JSON_OVER_VIEW = "overview";
    public static final String JSON_VOTE_AVERAGE = "vote_average";
    public static final String JSON_DATE = "release_date";
    public static final String JSON_MOVIE_ID  ="id";
    public static final String JSON_MOVIE_POPULARITY = "popularity";
    public static final String JSON_MOVIE_RUNTIME = "runtime";
    public static final String JSON_MOVIE_TRAILER_KEY = "key";
    public static final String JSON_MOVIE_VIDEOS = "videos";
    public static final String JSON_MOVIE_REVIEWS = "reviews";
}
