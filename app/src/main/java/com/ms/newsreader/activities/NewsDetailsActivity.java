package com.ms.newsreader.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.ms.newsreader.R;
import com.ms.newsreader.util.Constant;

public class NewsDetailsActivity extends AppCompatActivity implements
        ShareActionProvider.OnShareTargetSelectedListener{

    private Intent shareIntent=new Intent(Intent.ACTION_SEND);

    String URL = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_details);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        final WebView webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);

        if (bundle != null) {
            URL = bundle.getString(Constant.NEWS_FEED);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    ProgressBar pb = (ProgressBar) findViewById(R.id.progress_bar);
                    pb.setVisibility(View.VISIBLE);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    ProgressBar pb = (ProgressBar) findViewById(R.id.progress_bar);
                    pb.setVisibility(View.INVISIBLE);
                    webView.setVisibility(View.VISIBLE);
                }
            });
            webView.loadUrl(URL);
        }

        shareIntent.setType("text/plain");
    }

    // Populates the activity's options menu.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.sharing_menu, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        mShareActionProvider.setShareIntent(shareIntent);
        mShareActionProvider.setOnShareTargetSelectedListener(this);

        // Return true to display menu
        return true;
    }

    // Handles the user's menu selection.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_share:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Returns a share intent
     */
    private Intent getDefaultShareIntent() {
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check this");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, URL);
        return shareIntent;
    }

    @Override
    public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {
        /** Setting a share intent */
        source.setShareIntent(getDefaultShareIntent());
        return false;
    }
}