package com.ms.newsreader.activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.icons.MaterialDrawerFont;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.ms.newsreader.R;
import com.ms.newsreader.adapter.DividerItemDecoration;
import com.ms.newsreader.adapter.GoogleFeed;
import com.ms.newsreader.adapter.NewsFeedAdapter;
import com.ms.newsreader.adapter.SimpleSectionedRecyclerViewAdapter;
import com.ms.newsreader.receiver.NetworkReceiver;
import com.ms.newsreader.util.Constant;
import com.ms.newsreader.util.GoogleNewsXmlParser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    public static final String WIFI = "Wi-Fi";
    public static final String ANY = "Any";

    // Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = false;
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;
    // Whether the display should be refreshed.
    public static boolean refreshDisplay = true;

    // The user's current network preference setting.
    public static String networkPreference = null;

    // The BroadcastReceiver that tracks network connectivity changes.
    private NetworkReceiver receiver = new NetworkReceiver();

    private List<GoogleFeed> newsFeedList = new ArrayList<>();

    private RecyclerView newsFeedRecyclerView;

    private NewsFeedAdapter adapter = null;

    private ProgressBar loading;

    private WebView errorMsgWebView;

    private AdView mAdView;

    SharedPreferences sharedPrefs;

    Drawer drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(false);

        SecondaryDrawerItem item1 = new SecondaryDrawerItem()
                .withIcon(MaterialDrawerFont.Icon.mdf_person).withIdentifier(1).withName(R.string.nav_item_technology);
        SecondaryDrawerItem item2 = new SecondaryDrawerItem()
                .withIcon(FontAwesome.Icon.faw_github).withIdentifier(2).withName(R.string.nav_item_business);
        SecondaryDrawerItem item3 = new SecondaryDrawerItem()
                .withIcon(FontAwesome.Icon.faw_apple).withIdentifier(2).withName(R.string.nav_item_entertainment);
        SecondaryDrawerItem item4 = new SecondaryDrawerItem()
                .withIcon(FontAwesome.Icon.faw_amazon).withIdentifier(2).withName(R.string.nav_item_sports);
        SecondaryDrawerItem item5 = new SecondaryDrawerItem()
                .withIcon(FontAwesome.Icon.faw_yahoo).withIdentifier(2).withName(R.string.nav_item_health);
        SecondaryDrawerItem item6 = new SecondaryDrawerItem()
                .withIcon(MaterialDrawerFont.Icon.mdf_expand_more).withIdentifier(2).withName(R.string.nav_item_world);
        SecondaryDrawerItem item7 = new SecondaryDrawerItem()
                .withIcon(MaterialDrawerFont.Icon.mdf_expand_more).withIdentifier(2).withName(R.string.nav_item_science);

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.news)
                .withSelectionListEnabledForSingleProfile(false)
                .withProfileImagesVisible(false)
                .build();

        // add navigation drawer
        drawer = new DrawerBuilder().
                withToolbar(mToolbar)
                .withFullscreen(true)
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(true)
                .withAccountHeader(headerResult)
                .withActionBarDrawerToggleAnimated(true)
                .withSelectedItem(-1)
                .addDrawerItems(
                        item1,
                        new DividerDrawerItem(),
                        item2,
                        new DividerDrawerItem(),
                        item3,
                        new DividerDrawerItem(),
                        item4,
                        new DividerDrawerItem(),
                        item5,
                        new DividerDrawerItem(),
                        item6,
                        new DividerDrawerItem(),
                        item7)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Timber.d(String.format("%s %d", "Clicked Item: ", position));
                        String newsType = getNewsType(position);
                        loadPage(newsType);
                        return true;
                    }
                })
                .build();


        // This static call will reset default values only on the first ever read
        PreferenceManager.setDefaultValues(getBaseContext(), R.xml.preferences, false);

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

        // Admob ads
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(Constant.ADS_ID).build();
        mAdView.loadAd(adRequest);

        // Gets the user's network preference settings
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Timber.tag(MainActivity.class.getSimpleName());
        Timber.d("Activity Created");
    }

    // Refreshes the display if the network connection and the
    // pref settings allow it.
    @Override
    public void onStart() {
        super.onStart();
        updateConnectedFlags();

        // Retrieves a string value for the preferences. The second parameter
        // is the default value to use if a preference value is not found.
        networkPreference = sharedPrefs.getString("listPref", "Wi-Fi");
        Timber.v(networkPreference);

        // Only loads the page if refreshDisplay is true. Otherwise, keeps previous
        // display. For example, if the user has set "Wi-Fi only" in prefs and the
        // device loses its Wi-Fi connection midway through the user using the app,
        // you don't want to refresh the display--this would force the display of
        // an error page instead of google.com content.
        if (refreshDisplay) {
            loadPage("");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings){
          startActivity(new Intent(this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
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

        // Set network preferences in SharedPreferences.
        SharedPreferences.Editor prefsEditor = sharedPrefs.edit();
        if (wifiConnected) {
            prefsEditor.putString("listPref", "Wi-Fi");
        } else {
            prefsEditor.putString("listPref", "Any");
        }
        prefsEditor.commit();
    }

    /**
     * Method to use for loading RSS feeds
     *
     * @param newsType String news category
     */
    private void loadPage(String newsType) {
        if (((networkPreference.equals(ANY)) && (wifiConnected || mobileConnected))
                || ((networkPreference.equals(WIFI)) && (wifiConnected))) {
            // call to load google news feed
            fetchGoogleNewsFeed(newsType);

            // close navigation drawer
            DrawerLayout drawerLayout = drawer.getDrawerLayout();
            drawerLayout.closeDrawers();
            loading.setVisibility(View.VISIBLE);
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


    /**
     * Downloading of the XML feed from google.com.
     * causing a delay that results in a poor user experience, always perform
     * network operations on a separate thread from the UI.
     */
    private void fetchGoogleNewsFeed(String newsType) {
        final GoogleNewsXmlParser googleNewsXmlParser = new GoogleNewsXmlParser();
        AndroidNetworking.get(String.format("%s%s", Constant.URL, newsType))
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

                            //This is to provide a sectioned list
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
                        Timber.d(String.format("%s %s", "Error: ", anError.toString()));
                    }
                });
    }


    private String getNewsType(int position) {
        String type = "";
        switch (position) {
            case 1:
                type = Constant.NEWS_FEED_TECHNOLOGY;
                break;
            case 3:
                type = Constant.NEWS_FEED_BUSINESS;
                break;
            case 5:
                type = Constant.NEWS_FEED_ENTERTAINMENT;
                break;
            case 7:
                type = Constant.NEWS_FEED_SPORTS;
                break;
            case 9:
                type = Constant.NEWS_FEED_HEALTH;
                break;
            case 11:
                type = Constant.NEWS_FEED_WORLD;
                break;
            case 13:
                type = Constant.NEWS_FEED_SCIENCE;
                break;
        }
        return type;
    }

}