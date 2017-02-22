package org.weibeld.nytexplore;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.weibeld.nytexplore.api.ApiService;
import org.weibeld.nytexplore.api.ApiServiceSingleton;
import org.weibeld.nytexplore.databinding.ActivityMainBinding;
import org.weibeld.nytexplore.model.ApiResponse;
import org.weibeld.nytexplore.model.Doc;
import org.weibeld.nytexplore.model.Multimedium;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    Activity mActivity;
    ActivityMainBinding b;

    ArrayList<Doc> mArticles;
    ArticleAdapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mActivity = this;

        mArticles = new ArrayList<Doc>();

        // Set adapter for the RecyclerView
        mAdapter = new ArticleAdapter(mArticles);
        b.recyclerView.setAdapter(mAdapter);

        // Set LayoutManager for the RecyclerView
        mLayoutManager = new GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false);
        //mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        b.recyclerView.setLayoutManager(mLayoutManager);

        b.btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchTerm = b.etFind.getText().toString();
                Call<ApiResponse> call = ApiServiceSingleton.getInstance().findArticles(searchTerm);
                call.enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        ArrayList<Doc> articles = (ArrayList<Doc>) response.body().getResponse().getDocs();
                        mArticles.clear();
                        mArticles.addAll(articles);
                        mAdapter.notifyDataSetChanged();
                        for (Doc doc : mArticles) {
                            if (doc.getHeadline() != null && doc.getHeadline().getMain() != null)
                                Log.d(LOG_TAG, doc.getHeadline().getMain());
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                    }
                });
            }
        });
    }



    public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

        private ArrayList<Doc> mData;

        public ArticleAdapter(ArrayList<Doc> data) {
            mData = data;
        }

        // Called when a new view for an item must be created. This method does not return the view of
        // the item, but a ViewHolder, which holds references to all the elements of the view.
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // The view for the item
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_article, parent, false);
            // Create a ViewHolder for this view and return it
            return new ViewHolder(itemView);
        }

        // Populate the elements of the passed view (represented by the ViewHolder) with the data of
        // the item at the specified position.
        @Override
        public void onBindViewHolder(ViewHolder vh, int position) {
            Doc article = mData.get(position);

            vh.tvTitle.setText(getSafeString(article.getHeadline().getMain()));

            ArrayList<Multimedium> multimedia = (ArrayList<Multimedium>) article.getMultimedia();
            String thumbUrl = "";
            for (Multimedium m : multimedia) {
                if (m.getType().equals("image") && m.getSubtype().equals("thumbnail")) {
                    thumbUrl = ApiService.API_IMAGE_BASE_URL + m.getUrl();
                    break;
                }
            }
            Glide.with(mActivity).load(thumbUrl).into(vh.ivThumb);

        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        private String getSafeString(String str) {
            if (str == null)
                return "";
            else
                return str;
        }


        public class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView ivThumb;
            public TextView tvTitle;

            // Create a viewHolder for the passed view (item view)
            public ViewHolder(View view) {
                super(view);
                ivThumb = (ImageView) view.findViewById(R.id.ivThumb);
                tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            }
        }

    }
}
