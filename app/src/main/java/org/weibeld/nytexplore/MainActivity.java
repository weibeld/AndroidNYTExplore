package org.weibeld.nytexplore;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;

import org.weibeld.nytexplore.adapter.ArticleAdapter;
import org.weibeld.nytexplore.api.ApiServiceSingleton;
import org.weibeld.nytexplore.databinding.ActivityMainBinding;
import org.weibeld.nytexplore.decor.SpacesItemDecoration;
import org.weibeld.nytexplore.model.ApiResponse;
import org.weibeld.nytexplore.model.Doc;
import org.weibeld.nytexplore.util.Util;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.support.v7.widget.SearchView.OnQueryTextListener;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static final String TAG_FILTER_DIALOG = "filter";

    Activity mActivity;
    ActivityMainBinding b;

    ArrayList<Doc> mArticles;
    ArticleAdapter mAdapter;
    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mActivity = this;
        mArticles = new ArrayList<>();
        mProgressDialog = setupProgressDialog();
        mAdapter = new ArticleAdapter(mArticles, this);

        // Set up RecyclerView
        b.recyclerView.setAdapter(mAdapter);
        b.recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        b.recyclerView.addItemDecoration(new SpacesItemDecoration(16));

        //query("Clinton", null, null, null, null, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        setupSearchView(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                DialogFragment newFragment = new FilterDialogFragment();
                newFragment.show(getFragmentManager(), TAG_FILTER_DIALOG);
                return true;
            default:
                return false;
        }
    }

    // TODO: check if using ProgressBar is better
    private ProgressDialog setupProgressDialog() {
        ProgressDialog p = new ProgressDialog(mActivity);
        p.setIndeterminate(true);
        p.setMessage(getString(R.string.progress_loading));
        return p;
    }

    private void setupSearchView(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
            // Called when query is submitted (by pressing "Search" button on keyboard)
            // Note: empty search queries detected by the SearchView itself and ignored
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.clear();
                query(query, null, null, null, null, null);
                searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void query(String q, String fq, String beginDate, String endDate, String sort, Integer page) {
        mProgressDialog.show();
        Call<ApiResponse> call = ApiServiceSingleton.getInstance().query(q, fq, beginDate, endDate, sort, page);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                mProgressDialog.dismiss();
                try {
                    ArrayList<Doc> articles = (ArrayList<Doc>) response.body().getResponse().getDocs();
                    if (articles.isEmpty())
                        Util.toastLong(mActivity, getString(R.string.toast_no_results));
                    else
                        mAdapter.append(articles);
                }
                catch (NullPointerException e) {
                    fail(e);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                mProgressDialog.dismiss();
                fail(t);
            }

            private void fail(Throwable t) {
                // TODO: check for SocketTimeoutException (if connection is slow)
                // TODO: check for UnknownHostException (if there is no Internet connection)
                Util.toastLong(mActivity, "Query failed: " + t.getClass().getSimpleName());
                t.printStackTrace();
            }
        });
    }
}
