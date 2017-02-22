package org.weibeld.nytexplore;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import org.weibeld.nytexplore.api.ApiServiceSingleton;
import org.weibeld.nytexplore.databinding.ActivityMainBinding;
import org.weibeld.nytexplore.model.ApiResponse;
import org.weibeld.nytexplore.model.Doc;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    Activity mActivity;
    ActivityMainBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mActivity = this;

        b.btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchTerm = b.etFind.getText().toString();
                Call<ApiResponse> call = ApiServiceSingleton.getInstance().findArticles(searchTerm);
                call.enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        ArrayList<Doc> docs = (ArrayList<Doc>) response.body().getResponse().getDocs();
                        for (Doc doc : docs) {
                            if (doc.getSnippet() != null)
                                Log.d(LOG_TAG, doc.getSnippet());
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                    }
                });
            }
        });



    }
}
