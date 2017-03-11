package test;

import android.test.AndroidTestCase;

import com.example.ni.popularmovies.data.MovieContract;

/**
 * Created by ni on 2017/2/21.
 */

public class MovieProviderTest extends AndroidTestCase{

    public void testType() {
        String type = mContext.getContentResolver().getType(MovieContract.MovieEntry.CONTENT_URI);
        assertEquals("Error: the WeatherEntry CONTENT_URI should return WeatherEntry.CONTENT_TYPE",
                MovieContract.MovieEntry.CONTENT_TYPE,type);

    }
}
