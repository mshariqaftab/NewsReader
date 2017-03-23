package com.ms.newsreader.activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
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
import com.ms.newsreader.fragments.NewsFragment;
import com.ms.newsreader.receiver.NetworkReceiver;
import com.ms.newsreader.util.Constant;

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

    private AdView mAdView;

    SharedPreferences sharedPrefs;

    Drawer drawer;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Load Ads
        initAds();

        // Initialize navigation drawer
        initNavigationDrawer();

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        // This static call will reset default values only on the first ever read
        PreferenceManager.setDefaultValues(getBaseContext(), R.xml.preferences, false);

        // Registers BroadcastReceiver to track network connection changes.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkReceiver();
        this.registerReceiver(receiver, filter);

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
            //loadPage("");
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
        if (item.getItemId() == R.id.action_settings) {
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
        prefsEditor.apply();
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

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(new NewsFragment().newInstance(Constant.NEWS_FEED_TOP_STORIES), Constant.TAB_TOP_STORIES_KEY);
        adapter.addFragment(new NewsFragment().newInstance(Constant.NEWS_FEED_BUSINESS), Constant.TAB_BUSINESS_KEY);
        adapter.addFragment(new NewsFragment().newInstance(Constant.NEWS_FEED_SPORTS), Constant.TAB_SPORT_KEY);
        adapter.addFragment(new NewsFragment().newInstance(Constant.NEWS_FEED_WORLD), Constant.TAB_WORLD_KEY);
        adapter.addFragment(new NewsFragment().newInstance(Constant.NEWS_FEED_HEALTH), Constant.TAB_HEALTH_KEY);
        adapter.addFragment(new NewsFragment().newInstance(Constant.NEWS_FEED_SCIENCE), Constant.TAB_SCIENCE_KEY);
        adapter.addFragment(new NewsFragment().newInstance(Constant.NEWS_FEED_TECHNOLOGY), Constant.TAB_TECHNOLOGY_KEY);
        adapter.addFragment(new NewsFragment().newInstance(Constant.NEWS_FEED_ENTERTAINMENT), Constant.TAB_ENTERTAINMENT_KEY);

        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


    private void initNavigationDrawer() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        SecondaryDrawerItem item1 = new SecondaryDrawerItem()
                .withIcon(MaterialDrawerFont.Icon.mdf_person).withIdentifier(1).withName(R.string.nav_item_top_story);

        SecondaryDrawerItem item2 = new SecondaryDrawerItem()
                .withIcon(MaterialDrawerFont.Icon.mdf_person).withIdentifier(1).withName(R.string.nav_item_business);
        SecondaryDrawerItem item3 = new SecondaryDrawerItem()
                .withIcon(FontAwesome.Icon.faw_github).withIdentifier(2).withName(R.string.nav_item_sports);
        SecondaryDrawerItem item4 = new SecondaryDrawerItem()
                .withIcon(FontAwesome.Icon.faw_apple).withIdentifier(2).withName(R.string.nav_item_world);
        SecondaryDrawerItem item5 = new SecondaryDrawerItem()
                .withIcon(FontAwesome.Icon.faw_amazon).withIdentifier(2).withName(R.string.nav_item_health);
        SecondaryDrawerItem item6 = new SecondaryDrawerItem()
                .withIcon(FontAwesome.Icon.faw_yahoo).withIdentifier(2).withName(R.string.nav_item_science);
        SecondaryDrawerItem item7 = new SecondaryDrawerItem()
                .withIcon(MaterialDrawerFont.Icon.mdf_expand_more).withIdentifier(2).withName(R.string.nav_item_technology);
        SecondaryDrawerItem item8 = new SecondaryDrawerItem()
                .withIcon(MaterialDrawerFont.Icon.mdf_expand_more).withIdentifier(2).withName(R.string.nav_item_entertainment);

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
                        item7,
                        new DividerDrawerItem(),
                        item8)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        slideTab(position);
                        return true;
                    }
                })
                .build();
    }


    private void slideTab(int position) {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        int i = position - 1;
        if (i != 0) {
            i = i / 2;
        }
        TabLayout.Tab tab = tabLayout.getTabAt(i);
        assert tab != null;
        tab.select();
        drawer.closeDrawer();
    }

    private void initAds(){
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));
        AdRequest adRequest = new AdRequest.Builder().
                addTestDevice(Constant.ADS_ID).build();
        // Load ads into Interstitial Ads
        mInterstitialAd.loadAd(adRequest);

        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                showInterstitial();
            }
        });

        mAdView = (AdView)findViewById(R.id.adView);
        AdRequest bannerAdRequest = new AdRequest.Builder().addTestDevice(Constant.ADS_ID).build();
        mAdView.loadAd(bannerAdRequest);
    }

    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

}