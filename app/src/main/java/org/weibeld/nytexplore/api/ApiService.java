package org.weibeld.nytexplore.api;

import org.weibeld.nytexplore.model.ApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by dw on 22/02/17.
 */

public interface ApiService {

    // NYTimes Article Search API
    // Documentation:
    //   - http://developer.nytimes.com/article_search_v2.json
    //   - https://github.com/NYTimes/public_api_specs/tree/master/article_search
    // NYT article search API returns maximum 10 results at a time (page 0 = 1-10, page 1 = 11-20, etc.)
    // The highest page number that can be requested is 120
    String API_KEY = "923ff50fda8a4f10befc9489619ea1c3";
    String API_BASE_URL = "https://api.nytimes.com/svc/search/v2/";
    String API_IMAGE_BASE_URL = "http://www.nytimes.com/";

    @GET("articlesearch.json")
    Call<ApiResponse> findArticles(
            @Query("q") String query,
            @Query("fq") String filteredQuery,
            @Query("begin_date") String beginDate,
            @Query("end_date") String endDate,
            @Query("sort") String sort,
            @Query("page") Integer page);
}
