package com.alif.newsreader.activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alif.newsreader.R;
import com.alif.newsreader.adapter.DividerItemDecoration;
import com.alif.newsreader.adapter.GoogleFeed;
import com.alif.newsreader.adapter.NewsFeedAdapter;
import com.alif.newsreader.adapter.SimpleSectionedRecyclerViewAdapter;
import com.alif.newsreader.receiver.NetworkReceiver;
import com.alif.newsreader.util.Constant;
import com.alif.newsreader.util.GoogleNewsXmlParser;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = MainActivity.class.getSimpleName();
    public static final String WIFI = "1";
    public static final String ANY = "2";

    // Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = false;
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;
    // Whether the display should be refreshed.
    public static boolean refreshDisplay = true;

    // The user's current network preference setting.
    public static String sPref = null;

    // The BroadcastReceiver that tracks network connectivity changes.
    private NetworkReceiver receiver = new NetworkReceiver();

    private List<GoogleFeed> newsFeedList = new ArrayList<>();

    private RecyclerView newsFeedRecyclerView;

    private NewsFeedAdapter adapter = null;

    private ProgressBar loading;

    private WebView errorMsgWebView;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);

        // Custom ActionBar
        View mCustomView = mInflater.inflate(R.layout.custom_actionbar, null);
        TextView mTitleTextView = (TextView) mCustomView.findViewById(R.id.title_text);
        mTitleTextView.setText(R.string.app_title);

        getSupportActionBar().setCustomView(mCustomView);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        // Registers BroadcastReceiver to track network connection changes.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkReceiver();
        this.registerReceiver(receiver, filter);

        // progress bar
        loading = (ProgressBar) findViewById(R.id.loading);

        // RecyclerView to load news feed in listView form
        newsFeedRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        newsFeedRecyclerView.setLayoutManager(mLayoutManager);
        newsFeedRecyclerView.setItemAnimator(new DefaultItemAnimator());
        newsFeedRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        newsFeedRecyclerView.setVisibility(View.GONE);
        // The specified network connection is not available. Displays error message in webview.
        errorMsgWebView = (WebView) findViewById(R.id.webview);
        errorMsgWebView.setVisibility(View.GONE);

        mAdView = (AdView) findViewById(R.id.adView);

        AdRequest adRequest = new AdRequest.Builder().addTestDevice(Constant.ADS_ID).build();
        mAdView.loadAd(adRequest);
    }

    // Refreshes the display if the network connection and the
    // pref settings allow it.
    @Override
    public void onStart() {
        super.onStart();

        // Gets the user's network preference settings
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Retrieves a string value for the preferences. The second parameter
        // is the default value to use if a preference value is not found.
        sPref = sharedPrefs.getString("listPref", "1");

        updateConnectedFlags();

        // Only loads the page if refreshDisplay is true. Otherwise, keeps previous
        // display. For example, if the user has set "Wi-Fi only" in prefs and the
        // device loses its Wi-Fi connection midway through the user using the app,
        // you don't want to refresh the display--this would force the display of
        // an error page instead of google.com content.
        if (refreshDisplay) {
            loadPage();
        }
    }

    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            this.unregisterReceiver(receiver);
        }
        if (mAdView != null) {
            mAdView.destroy();
        }
    }

    // Checks the network connection and sets the wifiConnected and mobileConnected
    // variables accordingly.
    private void updateConnectedFlags() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();

        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            wifiConnected = false;
            mobileConnected = false;
        }
    }

    // Uses AsyncTask subclass to download the XML feed from google.com.
    // This avoids UI lock up. To prevent network operations from
    // causing a delay that results in a poor user experience, always perform
    // network operations on a separate thread from the UI.
    private void loadPage() {
        if (((sPref.equals(ANY)) && (wifiConnected || mobileConnected))
                || ((sPref.equals(WIFI)) && (wifiConnected))) {
            // call to load google news feed
            fetchGoogleNewsFeed();
        } else {
            showErrorPage();
        }
    }

    // Displays an error if the app is unable to load content.
    private void showErrorPage() {
        errorMsgWebView.setVisibility(View.VISIBLE);
        errorMsgWebView.loadData(getResources().getString(R.string.connection_error),
                "text/html", null);
        loading.setVisibility(View.GONE);
    }

    // Populates the activity's options menu.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.network_menu, menu);
        return true;
    }

    // Handles the user's menu selection.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent settingsActivity = new Intent(getBaseContext(), NetworkSettingsActivity.class);
                startActivity(settingsActivity);
                return true;
            case R.id.refresh:
                loadPage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Method to fetch google news feed asynchronously
     */

    private void fetchGoogleNewsFeed() {
        final GoogleNewsXmlParser googleNewsXmlParser = new GoogleNewsXmlParser();
        AndroidNetworking.get(Constant.URL)
                .setPriority(Priority.LOW)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            InputStream stream = new ByteArrayInputStream(response.getBytes("UTF-8"));

                            // get list of news feeds
                            newsFeedList = googleNewsXmlParser.parse(stream);

                            adapter = new NewsFeedAdapter(MainActivity.this, newsFeedList);

                            //This is the code to provide a sectioned list
                            List<SimpleSectionedRecyclerViewAdapter.Section> sections = new ArrayList<>();

                            // News Feed Header
                            if (newsFeedList.size() != 0)
                                sections.add(new SimpleSectionedRecyclerViewAdapter.Section(0, newsFeedList.get(0).getNewsCategory()));

                            //Add your adapter to the sectionAdapter
                            SimpleSectionedRecyclerViewAdapter.Section[] sectionArr = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
                            SimpleSectionedRecyclerViewAdapter mSectionedAdapter = new
                                    SimpleSectionedRecyclerViewAdapter(MainActivity.this, R.layout.section, R.id.section_text, adapter);
                            mSectionedAdapter.setSections(sections.toArray(sectionArr));

                            //Apply mSectionedAdapter adapter to the RecyclerView
                            newsFeedRecyclerView.setAdapter(mSectionedAdapter);

                            Log.d(DEBUG_TAG, "MainActivity::fetchGoogleNewsFeed newsFeedList size is:" + newsFeedList.size());

                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                                newsFeedRecyclerView.setVisibility(View.VISIBLE);
                                loading.setVisibility(View.GONE);
                            }
                        } catch (IOException | XmlPullParserException | ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d(DEBUG_TAG, "Error: " + anError.toString());
                    }
                });
    }
}
