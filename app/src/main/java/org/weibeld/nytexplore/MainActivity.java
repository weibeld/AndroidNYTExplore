package org.weibeld.nytexplore;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

import static android.support.v7.widget.SearchView.OnQueryTextListener;

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
        //mLayoutManager = new GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false);
        mLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        b.recyclerView.setLayoutManager(mLayoutManager);

        SpacesItemDecoration decoration = new SpacesItemDecoration(16);
        b.recyclerView.addItemDecoration(decoration);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu
        getMenuInflater().inflate(R.menu.main, menu);

        // Set up SearchView
        MenuItem menuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
            // Called when query is submitted (by pressing "Search" button on keyboard)
            // Note: empty search queries detected by the SearchView itself and ignored
            @Override
            public boolean onQueryTextSubmit(String query) {
                Call<ApiResponse> call = ApiServiceSingleton.getInstance().findArticles(query);
                call.enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        ArrayList<Doc> articles = (ArrayList<Doc>) response.body().getResponse().getDocs();
                        mArticles.clear();
                        mArticles.addAll(articles);
                        mAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                    }
                });
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
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


    /*

    Decorator which adds spacing around the tiles in a Grid layout RecyclerView. Apply to a RecyclerView with:
        SpacesItemDecoration decoration = new SpacesItemDecoration(16);
        mRecyclerView.addItemDecoration(decoration);

    Feel free to add any value you wish for SpacesItemDecoration. That value determines the amount of spacing.
    Source: http://blog.grafixartist.com/pinterest-masonry-layout-staggered-grid/
    */
    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private final int mSpace;
        public SpacesItemDecoration(int space) {
            this.mSpace = space;
        }
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = mSpace;
            outRect.right = mSpace;
            outRect.bottom = mSpace;
            outRect.top = mSpace;
            // Add top margin only for the first item to avoid double space between items
//            if (parent.getChildAdapterPosition(view) == 0)
//                outRect.top = mSpace;
        }
    }
}
