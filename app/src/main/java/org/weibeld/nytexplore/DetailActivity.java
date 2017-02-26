package org.weibeld.nytexplore;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.weibeld.nytexplore.databinding.ActivityDetailBinding;

public class DetailActivity extends AppCompatActivity {

    ActivityDetailBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        b = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        // Display ProgressBar until page is loaded
        b.progressBar.setIndeterminate(true);
        b.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                b.progressBar.setVisibility(View.GONE);
                b.webView.setVisibility(View.VISIBLE);
                super.onPageFinished(view, url);
            }
        });
        b.webView.loadUrl(getIntent().getStringExtra(getString(R.string.EXTRA_ARTICLE_URL)));
    }

    // Make navigation button in app bar behave like the device back button, i.e. return to previous
    // activity without calling onCreate, in this way, search results in MainActivity are maintained
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Change the device back button to a back button of the embedded web browser
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (b.webView.canGoBack()) {
                        b.webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }
}
