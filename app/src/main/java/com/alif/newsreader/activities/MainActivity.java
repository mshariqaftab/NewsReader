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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alif.newsreader.R;
import com.alif.newsreader.adapter.DividerItemDecoration;
import com.alif.newsreader.adapter.NewsFeedAdapter;
import com.alif.newsreader.receiver.NetworkReceiver;
import com.alif.newsreader.adapter.GoogleFeed;
import com.alif.newsreader.util.Util;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

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

    private NewsFeedAdapter adapter;

    ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.custom_actionbar, null);
        TextView mTitleTextView = (TextView) mCustomView.findViewById(R.id.title_text);
        mTitleTextView.setText(R.string.app_title);

        getSupportActionBar().setCustomView(mCustomView);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        // Registers BroadcastReceiver to track network connection changes.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkReceiver();
        this.registerReceiver(receiver, filter);

        loading = (ProgressBar) findViewById(R.id.loading);

        newsFeedRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        adapter = new NewsFeedAdapter(newsFeedList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        newsFeedRecyclerView.setLayoutManager(mLayoutManager);
        newsFeedRecyclerView.setItemAnimator(new DefaultItemAnimator());
        newsFeedRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        newsFeedRecyclerView.setAdapter(adapter);
        newsFeedRecyclerView.setVisibility(View.GONE);


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
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            this.unregisterReceiver(receiver);
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
            Toast.makeText(this, "Async loading call goes here.. ", Toast.LENGTH_SHORT).show();
            // AsyncTask subclass
            loadXmlFromNetwork();
        } else {
            showErrorPage();
        }
    }

    // Displays an error if the app is unable to load content.
    private void showErrorPage() {
        setContentView(R.layout.activity_main);

        // The specified network connection is not available. Displays error message.
       /* WebView myWebView = (WebView) findViewById(R.id.webview);
        myWebView.loadData(getResources().getString(R.string.connection_error),
                "text/html", null);*/
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

    // Uploads XML from google.com, parses it, and combines it with
    // HTML markup. Returns HTML string.
    private void loadXmlFromNetwork() {
        AndroidNetworking.get(Util.URL)
                .setPriority(Priority.LOW)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject responseObj = XML.toJSONObject(response);
                            JSONObject rssObj = responseObj.getJSONObject("rss");
                            JSONObject channelObj = rssObj.getJSONObject("channel");
                            JSONArray detailsArr = channelObj.getJSONArray("item");
                            for (int i = 0; i < detailsArr.length(); i++) {
                                JSONObject childJSONObject = detailsArr.getJSONObject(i);
                                GoogleFeed googleFeed = new GoogleFeed();
                                googleFeed.setNewsTitle(childJSONObject.getString("title"));
                                googleFeed.setLink(childJSONObject.getString("link"));
                                googleFeed.setNewsCategory(childJSONObject.getString("category"));
                                googleFeed.setDescription(childJSONObject.getString("description"));
                                newsFeedList.add(googleFeed);
                                Log.d(DEBUG_TAG, googleFeed.toString());
                            }
                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                                newsFeedRecyclerView.setVisibility(View.VISIBLE);
                                loading.setVisibility(View.GONE);
                            }
                        } catch (JSONException e) {
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
