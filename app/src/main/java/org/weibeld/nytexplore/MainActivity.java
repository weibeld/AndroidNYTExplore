package org.weibeld.nytexplore;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    // NYTimes Article Search API
    // Documentation:
    //   - http://developer.nytimes.com/article_search_v2.json
    //   - https://github.com/NYTimes/public_api_specs/tree/master/article_search
    // NYT article search API returns maximum 10 results at a time (page 0 = 1-10, page 1 = 11-20, etc.)
    // The highest page number that can be requested is 120
    private static final String API_KEY = "923ff50fda8a4f10befc9489619ea1c3";  // GET query parameter api-key
    private static final String API_BASE_URL = "https://api.nytimes.com/svc/search/v2/";
    private static final String API_IMAGE_BASE_URL = "http://www.nytimes.com/";
    private static final String API_CALL = "articlesearch.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
