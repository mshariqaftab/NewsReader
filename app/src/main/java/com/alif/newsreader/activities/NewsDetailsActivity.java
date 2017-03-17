package com.alif.newsreader.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.alif.newsreader.R;

public class NewsDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_details);
        Bundle bundle = getIntent().getExtras();
        final WebView webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);

        if (bundle != null) {
            String URL = bundle.getString("URL");
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
    }
}
